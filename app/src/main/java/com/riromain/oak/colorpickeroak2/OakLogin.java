package com.riromain.oak.colorpickeroak2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.riromain.oak.colorpickeroak2.object.OakAccountInfo;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;

/**
 * Created by Rinie Romain on 10/04/2016.
 */
public class OakLogin extends AppCompatActivity {

    /**
     * Keep track of oak login task
     */
    private HttpAsynchTask mOakLoginTask = null;

    private EditText mEmailAddressView;
    private EditText mPasswordView;
    private View mEmailLoginFormView;
    private View mEmailLoginProgressView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_oak);
        SharedPreferences pref = getSharedPreferences(PrefConst.PREFS_NAME, 0);
        //Set up the login form
        mEmailAddressView = (EditText) findViewById(R.id.emailOakAccount);
        mEmailAddressView.setText(pref.getString(PrefConst.ACCOUNT_EMAIL_KEY, ""));

        mPasswordView = (EditText) findViewById(R.id.passwordOakAccount);
        mPasswordView.setText(pref.getString(PrefConst.ACCOUNT_PASSWORD_KEY, ""));
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.loginOakAccount || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        Button mEmailSignInButton = (Button) findViewById(R.id.email_log_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
        mEmailLoginFormView = findViewById(R.id.email_login_form);
        mEmailLoginProgressView = findViewById(R.id.email_login_progress);
        ParticleCloudSDK.init(this);
        if (pref.contains(PrefConst.ACCOUNT_EMAIL_KEY) && pref.contains(PrefConst.ACCOUNT_PASSWORD_KEY)) {
            attemptLogin();
        }
    }

    private void startDeviceSelectionActivity() {
        startActivity(new Intent(this, DeviceSelectionActivity.class));
    }
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
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
            showProgress(true);
            saveCredentialInAccountManager(email, password);
            mOakLoginTask = new HttpAsynchTask();
            mOakLoginTask.execute(new OakAccountInfo(email, password));
        }
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

    private class HttpAsynchTask extends AsyncTask<OakAccountInfo, Void, ParticleCloudException> {
        @Override
        protected ParticleCloudException doInBackground(final OakAccountInfo... params) {
            try {
                ParticleCloudSDK.getCloud().logIn(params[0].getEmail(), params[0].getPassword());
                return null;
            } catch (ParticleCloudException e) {
                e.printStackTrace();
                return e;
            }
        }

        @Override
        protected void onPostExecute(final ParticleCloudException resp) {
            //  mAuthTask = null;
            mOakLoginTask = null;
            showProgress(false);

            if (resp == null) {
                startDeviceSelectionActivity();
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