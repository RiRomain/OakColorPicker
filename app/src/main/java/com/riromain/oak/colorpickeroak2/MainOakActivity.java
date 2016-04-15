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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.riromain.oak.colorpickeroak2.http.ExecuteOakFunction;
import com.riromain.oak.colorpickeroak2.http.result.ObjectWithException;
import com.riromain.oak.colorpickeroak2.object.ColorInfo;
import com.riromain.oak.colorpickeroak2.object.DeviceStatus;
import com.riromain.oak.colorpickeroak2.object.OakFunctionRequest;
import com.riromain.oak.colorpickeroak2.object.ParcelableDevice;
import com.riromain.oak.colorpickeroak2.object.adapter.ParticleDeviceAdapter;
import com.riromain.oak.colorpickeroak2.onclicklistener.OnOffOnClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;

public class MainOakActivity extends AppCompatActivity implements ColorPicker.OnColorChangedListener, OpacityBar.OnOpacityChangedListener {
    private static final String TAG = "MainOakActivity";

    public static final int STEP = 10;
    private View mRGBFormView;
    //TODO reactivate in case of a manual refresh option
    // private LinearLayout mProgressLayout;
    private ColorPicker colorPicker;
    private OpacityBar rgbIntensityBar;
    private OpacityBar whiteIntensityBar;
    private ParticleDeviceAdapter myAdapter;
    private ParcelableDevice activeDevice;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ParticleCloudSDK.init(this);
        setContentView(R.layout.activity_main_oak);

        mRGBFormView = findViewById(R.id.rgb_picker_container_form);
        mRGBFormView.setVisibility(View.VISIBLE);

        //TODO reactivate in case of a manual refresh option
        //mProgressLayout = (LinearLayout)findViewById(R.id.progress_bar_layout);
        //TODO delete on progress on this page, every load made on startup, should not be needed
        //showProgress(true);
        colorPicker = (ColorPicker) findViewById(R.id.rgbcolorpicker);
        rgbIntensityBar = (OpacityBar) findViewById(R.id.rgbintensitybar);
        colorPicker.addOpacityBar(rgbIntensityBar);
        whiteIntensityBar = (OpacityBar) findViewById(R.id.whiteintensitybar);

        Button onButton = (Button) findViewById(R.id.on_button);
        Button offButton = (Button) findViewById(R.id.off_button);
        onButton.setOnClickListener(new OnOffOnClickListener("on", this, activeDevice.getDeviceID()));
        offButton.setOnClickListener(new OnOffOnClickListener("off", this, activeDevice.getDeviceID()));


        //ParcelableDevice
        ArrayList<ParcelableDevice> parcelableDeviceList = getIntent().getExtras().getParcelableArrayList("ParcelableDevice");

        Spinner mDeviceSelectionSpinner = (Spinner) findViewById(R.id.device_selection_spinner);
        myAdapter = new ParticleDeviceAdapter(this, parcelableDeviceList);
        mDeviceSelectionSpinner.setAdapter(myAdapter);
        mDeviceSelectionSpinner.setOnItemSelectedListener(new OnSpinnerItemSelectedListener());

        colorPicker.setShowOldCenterColor(false);

        SharedPreferences sharedPreferences = getSharedPreferences(PrefConst.PREFS_NAME, 0);
        activeDevice = getActiveDevice(sharedPreferences.getString(PrefConst.ACTIVE_DEVICE_ID_KEY, ""), parcelableDeviceList);

        ColorInfo colorInfo = activeDevice.getColorInfo();
        colorPicker.setColor(Color.rgb(colorInfo.getRedValue(), colorInfo.getGreenValue(), colorInfo.getBlueValue()));
        rgbIntensityBar.setOpacity(colorInfo.getIntensity());
        whiteIntensityBar.setOpacity(colorInfo.getWhiteValue());


        rgbIntensityBar.setOnOpacityChangedListener(this);
        whiteIntensityBar.setOnOpacityChangedListener(new WhiteIntensityOnChangedListener());
        colorPicker.setOnColorChangedListener(this);
    }

    private ParcelableDevice getActiveDevice(final String wishedDeviceId, final List<ParcelableDevice> parcelableDeviceList) {
        for (ParcelableDevice device : parcelableDeviceList) {
            if (DeviceStatus.CONNECTED.equals(device.getStatus()) && wishedDeviceId.equals(device.getDeviceID())) {
                return device;
            }
        }
        for (ParcelableDevice device : parcelableDeviceList) {
            if (DeviceStatus.CONNECTED.equals(device.getStatus())) {
                return device;
            }
        }
        //TODO handle this error, should not happen, login screen preventing a start without active device.
        Log.e(TAG, "Error, we should not reach this point");
        return new ParcelableDevice();
    }

    private class WhiteIntensityOnChangedListener implements OpacityBar.OnOpacityChangedListener {
        @Override
        public void onOpacityChanged(int whiteValue) {
            ColorInfo colorInfo = activeDevice.getColorInfo();
            if (differenceIsWideEnough(whiteValue, colorInfo.getWhiteValue()) || 0 == whiteValue || 255 == whiteValue) {
                Log.v(TAG, "onSaturationChanged");
                Log.v(TAG, "setting new white value [" + whiteValue + " - old value was" + colorInfo.getWhiteValue() + "]");
                colorInfo.setWhiteValue(whiteValue);
                sendValueToBoard();
            }
        }
    }

    @Override
    public void onOpacityChanged(final int opacity) {
        ColorInfo colorInfo = activeDevice.getColorInfo();
        if (differenceIsWideEnough(opacity, colorInfo.getIntensity()) || 0 == opacity || 255 == opacity) {
            Log.v(TAG, "onOpacityChanged");
            Log.v(TAG, "setting new opacity [" + opacity + "] - old was [" + colorInfo.getIntensity() + "]");
            colorInfo.setIntensity(opacity);
            sendOpacityToBoard();
        }
    }

    @Override
    public void onColorChanged(final int color) {
        ColorInfo colorInfo = activeDevice.getColorInfo();
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        if (shouldRefresh(red, green, blue)) {
            Log.v(TAG, "onColorChanged");
            Log.v(TAG, "setting new value red [" + red + "] green [" + green + "] blue [" + blue + "]");
            Log.v(TAG, "old value was     red [" + colorInfo.getRedValue() + "] green [" + colorInfo.getGreenValue() + "] blue [" + colorInfo.getBlueValue() + "]");
            colorInfo.setRedValue(red);
            colorInfo.setGreenValue(green);
            colorInfo.setBlueValue(blue);
            sendValueToBoard();
        }
    }

    private void sendValueToBoard() {
        ColorInfo colorInfo = activeDevice.getColorInfo();
        String formattedValue = getFormattedValues(colorInfo.getRedValue(), colorInfo.getGreenValue(), colorInfo.getBlueValue(), colorInfo.getWhiteValue());
        Log.v(TAG, "sendingValue [" + formattedValue + "] to board");
        executeFunction(new OakFunctionRequest(activeDevice.getDeviceID(), "value", formattedValue));
    }

    private void sendOpacityToBoard() {
        ColorInfo colorInfo = activeDevice.getColorInfo();
        String formattedValue = getFormattedValues(colorInfo.getIntensity());
        Log.v(TAG, "sendingIntensity [" + formattedValue + "] to board");
        executeFunction(new OakFunctionRequest(activeDevice.getDeviceID(), "intensity", formattedValue));
    }

    private void executeFunction(final OakFunctionRequest info) {
        new ExecuteOakFunction(this).execute(info);
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
        ColorInfo colorInfo = activeDevice.getColorInfo();
        return differenceIsWideEnough(red, colorInfo.getRedValue())
            || differenceIsWideEnough(green, colorInfo.getGreenValue())
            || differenceIsWideEnough(blue, colorInfo.getBlueValue());
    }

    private boolean differenceIsWideEnough(final int newValue, final int oldValue) {
        int absoluteDifference = Math.abs(newValue - oldValue);
        return absoluteDifference > STEP;
    }

    private String fillWithLeadingZero(final int valueIn) {
        return String.format(Locale.ENGLISH, "%03d", valueIn);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
   /* @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
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
    }*/

    private class RefreshColorFromDevice extends AsyncTask<Void, Void, ObjectWithException<ColorInfo>> {


        @Override
        protected ObjectWithException<ColorInfo> doInBackground(final Void... params) {
            try {
                SharedPreferences pref = getSharedPreferences(PrefConst.PREFS_NAME, 0);

                if (!ParticleCloudSDK.getCloud().isLoggedIn()) {
                    ParticleCloudSDK.getCloud().logIn(pref.getString(PrefConst.ACCOUNT_EMAIL_KEY, ""), pref.getString(PrefConst.ACCOUNT_PASSWORD_KEY, ""));
                }
                ParticleDevice device = ParticleCloudSDK.getCloud().getDevice(activeDevice.getDeviceID());
                ColorInfo colorInfo = activeDevice.getColorInfo();
                colorInfo.setRedValue(device.getIntVariable("red"));
                colorInfo.setGreenValue(device.getIntVariable("green"));
                colorInfo.setBlueValue(device.getIntVariable("blue"));
                colorInfo.setWhiteValue(device.getIntVariable("white"));
                colorInfo.setIntensity(device.getIntVariable("inten"));
                return new ObjectWithException<>(null, colorInfo);
            } catch (ParticleCloudException | ParticleDevice.VariableDoesNotExistException | IOException e) {
                e.printStackTrace();
                Log.v(TAG, MainOakActivity.this.getText(R.string.error_fetch_actual_value).toString(), e);
                return new ObjectWithException<>(e, null);
            }
        }

        @Override
        protected void onPostExecute(final ObjectWithException<ColorInfo> resp) {
            Exception exception = resp.getException();
            if (null == exception) {
                ColorInfo colorInfo = resp.getObject();
                Log.v(TAG, "setValueRetriever");
                Log.v(TAG, "got value red [" + colorInfo.getRedValue() + "] green [" + colorInfo.getGreenValue() + "] blue ["
                        + colorInfo.getBlueValue() + "] white [" + colorInfo.getWhiteValue() + "] intensity [" + colorInfo.getIntensity() + "]");
                colorPicker.setColor(Color.rgb(colorInfo.getRedValue(), colorInfo.getGreenValue(), colorInfo.getBlueValue()));
                rgbIntensityBar.setOpacity(colorInfo.getIntensity());
                whiteIntensityBar.setOpacity(colorInfo.getWhiteValue());
            } else {
                showErrorForException(exception, MainOakActivity.this.getText(R.string.error_fetch_actual_value));
            }
        }
    }

    private void showErrorForException(final Exception exception, final CharSequence customerText) {
        String error = customerText + " - Exception: " + exception.getCause() + " - " + exception.getMessage();
        Log.v(TAG, "Got error while retrieving set value - " + error, exception);
        ErrorDialog errorDialog = new ErrorDialog();
        Bundle args = new Bundle(1);
        args.putString(ErrorDialog.ERROR_INFO, error);
        errorDialog.setArguments(args);
        errorDialog.show(getFragmentManager(), "errordialog");
    }

    private class OnSpinnerItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            activeDevice = myAdapter.getItem(position);
            SharedPreferences sharedPreferences = getSharedPreferences(PrefConst.PREFS_NAME, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PrefConst.ACTIVE_DEVICE_ID_KEY, activeDevice.getDeviceID());
            editor.apply();
            //TODO add a manual possibility to refresh set color
            //Refresh from the cloud is not needed, as we search the value at login
            //new RefreshColorFromDevice().execute();
            ColorInfo colorInfo = activeDevice.getColorInfo();
            colorPicker.setColor(Color.rgb(colorInfo.getRedValue(), colorInfo.getGreenValue(), colorInfo.getBlueValue()));
            rgbIntensityBar.setOpacity(colorInfo.getIntensity());
            whiteIntensityBar.setOpacity(colorInfo.getWhiteValue());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}
