package com.riromain.oak.colorpickeroak2.http;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.riromain.oak.colorpickeroak2.ErrorDialog;
import com.riromain.oak.colorpickeroak2.R;
import com.riromain.oak.colorpickeroak2.object.OakFunctionRequest;

import java.io.IOException;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;

/**
 * Created by rrinie on 11.04.16.
 */
public class ExecuteOakFunction extends AsyncTask<OakFunctionRequest, Void, Void> {
    private static final String TAG = "ExecuteOakFunction";
    private final Activity callingActivity;

    public ExecuteOakFunction(final Activity callingActivity) {
        this.callingActivity = callingActivity;
    }

    @Override
    protected Void doInBackground(final OakFunctionRequest... params) {
        OakFunctionRequest oakFunctionRequest = params[0];
        try {
            ParticleDevice device = ParticleCloudSDK.getCloud().getDevice(oakFunctionRequest.getDeviceId());
            device.callFunction(oakFunctionRequest.getFunctionName(), oakFunctionRequest.getArguments());
            return null;
        } catch (ParticleCloudException | ParticleDevice.FunctionDoesNotExistException | IOException exception) {
            Log.v(TAG, "Got error while executing Oak function [" + oakFunctionRequest.getFunctionName() + "] with argument ["
                    + oakFunctionRequest.getArguments() + "] - " + exception.getMessage(), exception);
            showErrorForException(exception, callingActivity.getText(R.string.error_fetch_actual_value));
            exception.printStackTrace();
        }
        return null;
    }

    private void showErrorForException(final Exception exception, final CharSequence customerText) {
        String error = customerText + " - Exception: " + exception.getCause() + " - " + exception.getMessage();
        Log.v(TAG, "Got error while retrieving set value - " + error, exception);
        ErrorDialog errorDialog = new ErrorDialog();
        Bundle args = new Bundle(1);
        args.putString(ErrorDialog.ERROR_INFO, error);
        errorDialog.setArguments(args);
        errorDialog.show(callingActivity.getFragmentManager(), "errordialog");
    }
}
