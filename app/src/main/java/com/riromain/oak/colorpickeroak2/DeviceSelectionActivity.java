package com.riromain.oak.colorpickeroak2;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.riromain.oak.colorpickeroak2.http.asynctask.GetDeviceList;
import com.riromain.oak.colorpickeroak2.object.adapter.ParticleDeviceAdapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import io.particle.android.sdk.cloud.ParticleDevice;

/**
 * Created by Rinie Romain on 10/04/2016.
 */
public class DeviceSelectionActivity extends ListActivity {

    private static final String TAG = "DeviceSelectionActivity";

    private ParticleDeviceAdapter myAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_device_selection);
        SharedPreferences pref = getSharedPreferences(PrefConst.PREFS_NAME, 0);
        myAdapter = new ParticleDeviceAdapter(this, new ArrayList<ParticleDevice>());
        if (pref.contains(PrefConst.ACCOUNT_EMAIL_KEY) && pref.contains(PrefConst.ACCOUNT_PASSWORD_KEY)) {
            GetDeviceList mDeviceGetTask = new GetDeviceList(myAdapter, getFragmentManager(), getApplicationContext());
            mDeviceGetTask.execute();
        } else {
            String error = "Device list cannot be called before giving credential!";
            ErrorDialog errorDialog = new ErrorDialog();
            Bundle args = new Bundle(1);
            args.putString(ErrorDialog.ERROR_INFO, error);
            errorDialog.setArguments(args);
            errorDialog.show(getFragmentManager(), "errordialog");
            errorDialog.onDismiss(new ReturnToLoginInterface(this));
        }
        ListView mDeviceEntryListView = (ListView) findViewById(android.R.id.list);
        mDeviceEntryListView.setAdapter(myAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ParticleDevice device = myAdapter.getItem(position);
      //  Toast.makeText(this, device.getName() + " selected", Toast.LENGTH_LONG).show();
        if (!device.isConnected()) {
            Toast.makeText(this, "Selected device is not connected - last connection: " + DateFormat.getDateTimeInstance().format(device.getLastHeard())
                    + ". Please select another device or check that device is powered on and connected to internet.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Set<String> functions = device.getFunctions();
        if (functions.isEmpty()) {
            Toast.makeText(this, "Selected device does not support require function [led], [value] and [intensity]",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Map<String, ParticleDevice.VariableType> variables = device.getVariables();
        if (variables.isEmpty()) {
            Toast.makeText(this, "Selected device does not support require variable [red], [green], [blue], [white] and [inten]",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Log.v(TAG, "device support Variables: " + variables.toString());
        Log.v(TAG, "device support function: " + functions.toString());
        SharedPreferences sharedPreferences = getSharedPreferences(PrefConst.PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrefConst.ACTIVE_DEVICE_ID_KEY, device.getID());
        editor.apply();
        startActivity(new Intent(this, MainOakActivity.class));
    }

    private class ReturnToLoginInterface implements DialogInterface {
        private final DeviceSelectionActivity deviceSelectionActivity;
        public ReturnToLoginInterface(final DeviceSelectionActivity deviceSelectionActivity) {
            this.deviceSelectionActivity = deviceSelectionActivity;
        }
        @Override
        public void cancel() {
            startOakLoginActivity();
        }
        @Override
        public void dismiss() {
            startOakLoginActivity();
        }
        private void startOakLoginActivity() {
            startActivity(new Intent(deviceSelectionActivity, OakLogin.class));
        }
    }
}
