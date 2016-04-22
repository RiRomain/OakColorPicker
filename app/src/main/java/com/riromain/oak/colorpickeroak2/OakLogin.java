package com.riromain.oak.colorpickeroak2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.riromain.oak.colorpickeroak2.object.ColorInfo;
import com.riromain.oak.colorpickeroak2.object.DeviceStatus;
import com.riromain.oak.colorpickeroak2.object.ParcelableDevice;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;

/**
 * Created by Rinie Romain on 10/04/2016.
 */
public class OakLogin extends AppCompatActivity {

    /**
     * Keep track of oak login task
     */
    private OakLoginTask mOakLoginTask = null;
    private static final String TAG = "MainOakActivity";

    private ArrayList<ParcelableDevice> loadedDevice;
    private EditText mEmailAddressView;
    private EditText mPasswordView;
    private View mEmailLoginFormView;
    private View mEmailLoginProgressView;
    private LoadDeviceListTask mOakDeviceGetTask;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.preferences: {
                Intent intent = new Intent();
                intent.setClassName(this, "com.riromain.oak.colorpickeroak2.MyPreferenceActivity");
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_oak);
        SharedPreferences pref = getSharedPreferences(PrefConst.PREFS_NAME, 0);

        //Initialize the different variable
        mEmailAddressView = (EditText) findViewById(R.id.emailOakAccount);
        mEmailLoginFormView = findViewById(R.id.email_login_form);
        mEmailLoginProgressView = findViewById(R.id.email_login_progress);
        mPasswordView = (EditText) findViewById(R.id.passwordOakAccount);

        //Initialize cloud API and connect directly if credential are available
        boolean directLogin = pref.contains(PrefConst.ACCOUNT_EMAIL_KEY) && pref.contains(PrefConst.ACCOUNT_PASSWORD_KEY);
        if (directLogin) {
            showProgress(true);
        }
        ParticleCloudSDK.init(this);
        if (directLogin) {
            executeLogin(pref.getString(PrefConst.ACCOUNT_EMAIL_KEY, ""), pref.getString(PrefConst.ACCOUNT_PASSWORD_KEY, ""));
        }

        //Set up the login form with available credential
        mEmailAddressView.setText(pref.getString(PrefConst.ACCOUNT_EMAIL_KEY, ""));
        mPasswordView.setText(pref.getString(PrefConst.ACCOUNT_PASSWORD_KEY, ""));
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.loginOakAccount || id == EditorInfo.IME_NULL) {
                    validateCredentialAndAttemptLogin();
                    return true;
                }
                return false;
            }
        });
        Button mEmailSignInButton = (Button) findViewById(R.id.email_log_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateCredentialAndAttemptLogin();
            }
        });
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainOakActivity.class).putParcelableArrayListExtra("ParcelableDevice", loadedDevice));
    }
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void validateCredentialAndAttemptLogin() {
        if (mOakLoginTask != null) {
            return;
        }

        //Reset errors
        mEmailAddressView.setError(null);
        mPasswordView.setError(null);

        //Store values at the time of the login attempt.
        String email = mEmailAddressView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && password.length() < 3) {
            mPasswordView.setError("Password too short, please use more than 2 character.");
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailAddressView.setError(getString(R.string.error_field_required));
            focusView = mEmailAddressView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailAddressView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailAddressView;
            cancel = true;
        }

        if (cancel) {
            // There was a validation error; don't attempt login and focus the first form
            // field with an error
            focusView.requestFocus();
        } else {
            // Show progress spinner, and kick off a background task to perform
            // login attempt.
            saveCredentialInAccountManager(email, password);
            executeLogin(email, password);
        }
    }

    private void executeLogin(final String email, final String password) {
        showProgress(true);
        mOakLoginTask = new OakLoginTask(email, password);
        mOakLoginTask.execute();
    }

    private void saveCredentialInAccountManager(final String email, final String password) {
        SharedPreferences sharedPreferences = getSharedPreferences(PrefConst.PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrefConst.ACCOUNT_EMAIL_KEY, email);
        editor.putString(PrefConst.ACCOUNT_PASSWORD_KEY, password);
        editor.apply();
    }

    private boolean isEmailValid(final String email) {
        return email.length() > 5 && email.contains("@");
    }

    private class OakLoginTask extends AsyncTask<Void, Void, ParticleCloudException> {
        private final String emailAddress;
        private final String password;

        private OakLoginTask(final String emailAddress, final String password) {
            this.emailAddress = emailAddress;
            this.password = password;
        }

        @Override
        protected ParticleCloudException doInBackground(final Void... params) {
            try {
                ParticleCloudSDK.getCloud().logIn(emailAddress, password);
                return null;
            } catch (ParticleCloudException e) {
                e.printStackTrace();
                return e;
            }
        }

        @Override
        protected void onPostExecute(final ParticleCloudException resp) {
            mOakLoginTask = null;

            if (resp == null) {
                //There was no error, start to read available device
                mOakDeviceGetTask = new LoadDeviceListTask();
                mOakDeviceGetTask.execute();
            } else {
                String error = getText(R.string.error_login_particle) + " - Error code: " + resp.getBestMessage() + " - " + resp.getServerErrorMsg();
                ErrorDialog errorDialog = new ErrorDialog();
                Bundle args = new Bundle(1);
                args.putString(ErrorDialog.ERROR_INFO, error);
                errorDialog.setArguments(args);
                errorDialog.show(getFragmentManager(), "errordialog");
            }
        }
        @Override
        protected void onCancelled() {
            showProgress(false);
        }
    }

    private class LoadDeviceListTask extends AsyncTask<Void, Void, ParticleCloudException> {

        @Override
        protected ParticleCloudException doInBackground(final Void... params) {
            try {
                ArrayList<ParcelableDevice> rgbDriverList = new ArrayList<>();
                for (ParticleDevice device : ParticleCloudSDK.getCloud().getDevices()) {
                    rgbDriverList.add(convertToParcelableDevice(device));
                }
                loadedDevice = rgbDriverList;
                return null;
            } catch (ParticleCloudException e) {
                e.printStackTrace();
                return e;
            }
        }

        @NonNull
        private ParcelableDevice convertToParcelableDevice(final ParticleDevice device) throws ParticleCloudException {
            ParcelableDevice parcelableDevice = new ParcelableDevice();
            parcelableDevice.setDeviceID(device.getID());
            parcelableDevice.setDeviceName(device.getName());
            parcelableDevice.setLastConnection(DateFormat.getDateTimeInstance().format(device.getLastHeard()));
            if (!device.isConnected()) {
                parcelableDevice.setStatus(DeviceStatus.DISCONNECTED);
                return parcelableDevice;
            }
            Set<String> functions = device.getFunctions();
            if (functions.isEmpty()) {
                parcelableDevice.setStatus(DeviceStatus.NOT_COMPATIBLE);
                return parcelableDevice;
            }
            Map<String, ParticleDevice.VariableType> variables = device.getVariables();
            if (variables.isEmpty()) {
                parcelableDevice.setStatus(DeviceStatus.NOT_COMPATIBLE);
                return parcelableDevice;
            }
            Log.v(TAG, "device support Variables: " + variables.toString());
            Log.v(TAG, "device support function: " + functions.toString());
            ColorInfo colorInfo = new ColorInfo();
            try {
                colorInfo.setRedValue(device.getIntVariable("red"));
                colorInfo.setGreenValue(device.getIntVariable("green"));
                colorInfo.setBlueValue(device.getIntVariable("blue"));
                colorInfo.setWhiteValue(device.getIntVariable("white"));
                colorInfo.setIntensity(device.getIntVariable("inten"));
                Log.v(TAG, "got value red [" + colorInfo.getRedValue() + "] green [" + colorInfo.getGreenValue() + "] blue ["
                    + colorInfo.getBlueValue() + "] white [" + colorInfo.getWhiteValue() + "] intensity [" + colorInfo.getIntensity() + "]");
            }  catch (IOException e) {
                throw new ParticleCloudException(e);
            } catch (ParticleDevice.VariableDoesNotExistException e) {
                parcelableDevice.setStatus(DeviceStatus.NOT_COMPATIBLE);
                return parcelableDevice;
            }
            parcelableDevice.setStatus(DeviceStatus.CONNECTED);
            parcelableDevice.setColorInfo(colorInfo);
            return parcelableDevice;
        }

        @Override
        protected void onPostExecute(final ParticleCloudException exception) {
            //  mAuthTask = null;
            mOakDeviceGetTask = null;
            showProgress(false);

            if (null != exception) {
                String error = getText(R.string.error_fetching_device_particle) + " - Error code: " + exception.getMessage();
                ErrorDialog errorDialog = new ErrorDialog();
                Bundle args = new Bundle(1);
                args.putString(ErrorDialog.ERROR_INFO, error);
                errorDialog.setArguments(args);
                errorDialog.show(getFragmentManager(), "errordialog");
            } else if (!oneDeviceConnected(loadedDevice)) {
                ErrorDialog errorDialog = new ErrorDialog();
                Bundle args = new Bundle(1);
                args.putString(ErrorDialog.ERROR_INFO, getText(R.string.error_no_device_active) + "");
                errorDialog.setArguments(args);
                errorDialog.show(getFragmentManager(), "errordialog");
            } else {
                startMainActivity();
            }
        }
        @Override
        protected void onCancelled() {
            showProgress(false);
        };

    }

    private boolean oneDeviceConnected(final List<ParcelableDevice> devices) {
        for (ParcelableDevice device : devices) {
            if (DeviceStatus.CONNECTED.equals(device.getStatus())) {
                return true;
            }
        }
        return false;
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

            mEmailLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mEmailLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mEmailLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mEmailLoginProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mEmailLoginProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mEmailLoginProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mEmailLoginProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mEmailLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
