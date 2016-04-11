package com.riromain.oak.colorpickeroak2.http.asynctask;

import android.app.FragmentManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.riromain.oak.colorpickeroak2.ErrorDialog;
import com.riromain.oak.colorpickeroak2.R;
import com.riromain.oak.colorpickeroak2.http.result.ObjectListWithParticleCloudException;
import com.riromain.oak.colorpickeroak2.object.adapter.ParticleDeviceAdapter;

import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;

/**
 * Created by rrinie on 11.04.16.
 */
public class GetDeviceList extends AsyncTask<Void, Void, ObjectListWithParticleCloudException<ParticleDevice>> {
    private static final String TAG = "GetDeviceList";
    private final ParticleDeviceAdapter myAdapter;
    private final FragmentManager fragmentManager;
    private final Context applicationContext;

    public GetDeviceList(final ParticleDeviceAdapter myAdapter,
                         final FragmentManager fragmentManager,
                         final Context applicationContext) {
        this.myAdapter = myAdapter;
        this.fragmentManager = fragmentManager;
        this.applicationContext = applicationContext;
    }

    @Override
    protected ObjectListWithParticleCloudException<ParticleDevice> doInBackground(final Void... params) {
        try {
            Log.v(TAG, "getDeviceList.doInBackground");
            Log.v(TAG, "started");
            return new ObjectListWithParticleCloudException<>(null, ParticleCloudSDK.getCloud().getDevices());
        } catch (ParticleCloudException e) {
            Log.v(TAG, "getDeviceList.doInBackground");
            Log.v(TAG, "catch exception");
            e.printStackTrace();
            return new ObjectListWithParticleCloudException<>(e, null);
        }
    }

    @Override
    protected void onPostExecute(final ObjectListWithParticleCloudException<ParticleDevice> resp) {

        Log.v(TAG, "getDeviceList.onPostExecute");
        ParticleCloudException e = resp.getException();
        List<ParticleDevice> deviceList = resp.getDeviceList();
        if (null == e && null != deviceList) {
            Log.v(TAG, "UpdatingEntries - got " + deviceList.size() + " devices");
            myAdapter.upDateEntries(deviceList);
        } else if (null == deviceList) {
            Log.v(TAG, "UpdatingEntries - got an empty device list");
            String error = applicationContext.getText(R.string.error_fetching_device_particle) + " - Got an empty device list";
            ErrorDialog errorDialog = new ErrorDialog();
            Bundle args = new Bundle(1);
            args.putString(ErrorDialog.ERROR_INFO, error);
            errorDialog.setArguments(args);
            errorDialog.show(fragmentManager, "errordialog");
        } else {
            Log.v(TAG, "UpdatingEntries - error");
            String error = applicationContext.getText(R.string.error_fetching_device_particle)
                    + " - Error code: " + e.getBestMessage() + " - " + e.getServerErrorMsg();
            ErrorDialog errorDialog = new ErrorDialog();
            Bundle args = new Bundle(1);
            args.putString(ErrorDialog.ERROR_INFO, error);
            errorDialog.setArguments(args);
            errorDialog.show(fragmentManager, "errordialog");
        }
    }
}