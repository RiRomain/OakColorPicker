package com.riromain.oak.colorpickeroak2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.riromain.oak.colorpickeroak2.http.helper.HttpGETEntityAsString;
import com.riromain.oak.colorpickeroak2.http.helper.HttpPostNewValue;
import com.riromain.oak.colorpickeroak2.http.result.ObjectWithPotentialError;
import com.riromain.oak.colorpickeroak2.http.result.ObjectWithPotentialErrorImpl;
import com.riromain.oak.colorpickeroak2.object.GetVariableInfo;
import com.riromain.oak.colorpickeroak2.object.OakInfo;
import com.riromain.oak.colorpickeroak2.object.FunctionWithValueRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class MainOakActivity extends AppCompatActivity implements ColorPicker.OnColorChangedListener, OpacityBar.OnOpacityChangedListener {
    private static final String TAG = "MainOakActivity";

    public static final int STEP = 10;
    private View mRGBFormView;
    private LinearLayout mProgressLayout;
    private Integer oldRedValue;
    private Integer oldGreenValue;
    private Integer oldBlueValue;
    private Integer oldWhiteValue;
    private Integer oldIntensity;
    private ColorPicker colorPicker;
    private OpacityBar rgbIntensityBar;
    private OpacityBar whiteIntensityBar;
    private OakInfo oakInfo;
    private Button onButton;
    private Button offButton;

    private enum RGBW {
        RED,
        GREEN,
        BLUE,
        WHITE,
        INTENSITY;

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_oak);

        SharedPreferences pref = getSharedPreferences(PrefConst.PREFS_NAME, 0);
        String deviceId = pref.getString(PrefConst.DEVICE_ID_KEY, "");
        String accessToken = pref.getString(PrefConst.ACCESS_TOKEN_KEY, "");
        mRGBFormView = findViewById(R.id.rgb_picker_container_form);
        mRGBFormView.setVisibility(View.VISIBLE);

        mProgressLayout = (LinearLayout)findViewById(R.id.progress_bar_layout);
        showProgress(true);
        colorPicker = (ColorPicker) findViewById(R.id.rgbcolorpicker);
        rgbIntensityBar = (OpacityBar) findViewById(R.id.rgbintensitybar);
        colorPicker.addOpacityBar(rgbIntensityBar);
        whiteIntensityBar = (OpacityBar) findViewById(R.id.whiteintensitybar);

        onButton = (Button) findViewById(R.id.on_button);
        offButton = (Button) findViewById(R.id.off_button);
        onButton.setOnClickListener(new OnOffOnClickListener("on"));
        offButton.setOnClickListener(new OnOffOnClickListener("off"));

        colorPicker.setShowOldCenterColor(false);
        oakInfo = new OakInfo(deviceId, accessToken);
        retrieveActuallySetColor(oakInfo);
        rgbIntensityBar.setOnOpacityChangedListener(this);
        whiteIntensityBar.setOnOpacityChangedListener(new WhiteIntensityOnChangedListener());
        colorPicker.setOnColorChangedListener(this);
    }

    private class OnOffOnClickListener implements View.OnClickListener {
        private String arg;

        public OnOffOnClickListener(final String arg) {
            this.arg = arg;
        }

        @Override
        public void onClick(View v) {
            executeFunction(new FunctionWithValueRequest(oakInfo, arg, "led"));
        }
    }

    private class WhiteIntensityOnChangedListener implements OpacityBar.OnOpacityChangedListener {
        @Override
        public void onOpacityChanged(int whiteValue) {
            if (!allOldValueSet()) {
                //Value not finished to be retrieved yet, do nothing
                //Or, no change detected
                return;
            }
            if (differenceIsWideEnough(whiteValue, oldWhiteValue) || 0 == whiteValue || 255 == whiteValue) {
                Log.v(TAG, "onSaturationChanged");
                Log.v(TAG, "setting new white value [" + whiteValue + " - old value was" + oldWhiteValue + "]");
                oldWhiteValue = whiteValue;
                sendValueToBoard();
            }
        }
    }

    @Override
    public void onOpacityChanged(final int opacity) {
        if (!allOldValueSet()) {
            //Value not finished to be retrieved yet, do nothing
            //Or, no change detected
            return;
        }
        if (differenceIsWideEnough(opacity, oldIntensity) || 0 == opacity || 255 == opacity) {
            Log.v(TAG, "onOpacityChanged");
            Log.v(TAG, "setting new opacity [" + opacity + "] - old was [" + oldIntensity + "]");
            oldIntensity = opacity;
            sendOpacityToBoard();
        }
    }

    @Override
    public void onColorChanged(final int color) {
        if (!allOldValueSet()) {
            //Value not finished to be retrieved yet, do nothing
            return;
        }
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        if (shouldRefresh(red, green, blue)) {
            Log.v(TAG, "onColorChanged");
            Log.v(TAG, "setting new value red [" + red + "] green [" + green + "] blue [" + blue + "]");
            Log.v(TAG, "old value was     red [" + oldRedValue + "] green [" + oldGreenValue + "] blue [" + oldBlueValue + "]");
            oldRedValue = red;
            oldGreenValue = green;
            oldBlueValue = blue;
            sendValueToBoard();
        }
    }

    private boolean allOldValueSet() {
        return null != oldRedValue && null != oldGreenValue && null != oldBlueValue
                && null != oldIntensity && null != oldWhiteValue;
    }

    private void sendValueToBoard() {
        String formattedValue = getFormattedValues(oldRedValue, oldGreenValue, oldBlueValue, oldWhiteValue);
        Log.v(TAG, "sendingValue [" + formattedValue + "] to board");
        executeFunction(new FunctionWithValueRequest(oakInfo, formattedValue, "value"));
    }

    private void sendOpacityToBoard() {
        String formattedValue = getFormattedValues(oldIntensity);
        Log.v(TAG, "sendingIntensity [" + formattedValue + "] to board");
        executeFunction(new FunctionWithValueRequest(oakInfo, formattedValue, "intensity"));
    }

    private void executeFunction(final FunctionWithValueRequest info) {
        new HttpPostValueForFunctionTask().execute(info);
    }

    @NonNull
    private String getFormattedValues(final int... values) {
        StringBuilder sb = new StringBuilder();
        for (int value : values) {
            sb.append(fillWithLeadingZero(value));
        }
        return sb.toString();
    }

    private boolean shouldRefresh(final int red, final int green, final int blue) {
        return differenceIsWideEnough(red, oldRedValue)
            || differenceIsWideEnough(green, oldGreenValue)
            || differenceIsWideEnough(blue, oldBlueValue);
    }

    private boolean differenceIsWideEnough(final int newValue, final int oldValue) {
        int absoluteDifference = Math.abs(newValue - oldValue);
        return absoluteDifference > STEP;
    }

    private String fillWithLeadingZero(final int valueIn) {
        return String.format("%03d", valueIn);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRGBFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRGBFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRGBFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressLayout.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            mRGBFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void retrieveActuallySetColor(final OakInfo oakInfo) {
        new HttpAsynchTask(RGBW.RED).execute(new GetVariableInfo(oakInfo, "red"));
        new HttpAsynchTask(RGBW.GREEN).execute(new GetVariableInfo(oakInfo, "green"));
        new HttpAsynchTask(RGBW.BLUE).execute(new GetVariableInfo(oakInfo, "blue"));
        new HttpAsynchTask(RGBW.WHITE).execute(new GetVariableInfo(oakInfo, "white"));
        new HttpAsynchTask(RGBW.INTENSITY).execute(new GetVariableInfo(oakInfo, "inten"));
    }

    private class HttpAsynchTask extends AsyncTask<GetVariableInfo, Void, ObjectWithPotentialError<String>> {


        private RGBW RGBW;
        public HttpAsynchTask(final RGBW RGBW) {
            this.RGBW = RGBW;
        }
        @Override
        protected ObjectWithPotentialError<String> doInBackground(final GetVariableInfo... params) {
            ObjectWithPotentialError<String> resp = HttpGETEntityAsString.getVariable(params[0]);
            if (resp.getError() != null || resp.getErrorCode() != null) {
                return resp;
            }
            if (resp.getContent() == null) {
                return new ObjectWithPotentialErrorImpl<>(0, "Received response is empty");
            }
            try {
                JSONObject reader = new JSONObject(resp.getContent());
                return new ObjectWithPotentialErrorImpl<>(reader.getString("result"));
            } catch (JSONException e) {
                e.printStackTrace();
                return new ObjectWithPotentialErrorImpl<>(0, "Error while geting name " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(final ObjectWithPotentialError<String> resp) {

            if (resp.getErrorCode() == null && resp.getError() == null && null != resp.getContent()) {
                if (RGBW.RED.equals(RGBW)) {
                    oldRedValue = Integer.parseInt(resp.getContent());
                }
                if (RGBW.GREEN.equals(RGBW)) {
                    oldGreenValue = Integer.parseInt(resp.getContent());
                }
                if (RGBW.BLUE.equals(RGBW)) {
                    oldBlueValue = Integer.parseInt(resp.getContent());
                }
                if (RGBW.WHITE.equals(RGBW)) {
                    oldWhiteValue = Integer.parseInt(resp.getContent());
                }
                if (RGBW.INTENSITY.equals(RGBW)) {
                    oldIntensity = Integer.parseInt(resp.getContent());
                }
                if (allOldValueSet()) {
                    Log.v(TAG, "setValueRetriever");
                    Log.v(TAG, "got value red [" + oldRedValue + "] green [" + oldGreenValue + "] blue [" + oldBlueValue + "] white [" + oldWhiteValue + "] intensity [" + oldIntensity + "]");
                    colorPicker.setColor(Color.rgb(oldRedValue, oldGreenValue, oldBlueValue));
                    rgbIntensityBar.setOpacity(oldIntensity);
                    whiteIntensityBar.setOpacity(oldWhiteValue);
                    showProgress(false);
                }
                // finish();
            } else {
                String error = getText(R.string.error_fetch_actual_value) + " - Error code: " + resp.getErrorCode() + " - " + resp.getError();
                Log.v(TAG, "Got error while retrieving set value - " + error);
                ErrorDialog errorDialog = new ErrorDialog();
                Bundle args = new Bundle(1);
                args.putString(ErrorDialog.ERROR_INFO, error);
                errorDialog.setArguments(args);
                errorDialog.show(getFragmentManager(), "errordialog");
            }
        }
    }

    private class HttpPostValueForFunctionTask extends AsyncTask<FunctionWithValueRequest, Void, ObjectWithPotentialError<String>> {

        @Override
        protected ObjectWithPotentialError<String> doInBackground(final FunctionWithValueRequest... params) {
            return HttpPostNewValue.execute(params[0]);
        }

        @Override
        protected void onPostExecute(final ObjectWithPotentialError<String> resp) {

            if (resp.getErrorCode() != null && resp.getError() != null) {
                String error = getText(R.string.error_set_new_value) + " - Error code: " + resp.getErrorCode() + " - " + resp.getError();
                Log.v(TAG, "Got error while setting new value - " + error);
                ErrorDialog errorDialog = new ErrorDialog();
                Bundle args = new Bundle(1);
                args.putString(ErrorDialog.ERROR_INFO, error);
                errorDialog.setArguments(args);
                errorDialog.show(getFragmentManager(), "errordialog");
            }
        }
    }

}
