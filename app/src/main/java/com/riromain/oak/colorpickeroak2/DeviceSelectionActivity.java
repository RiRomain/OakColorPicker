package com.riromain.oak.colorpickeroak2;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.riromain.oak.colorpickeroak2.object.OakAccountInfo;

import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;

/**
 * Created by Rinie Romain on 10/04/2016.
 */
public class DeviceSelectionActivity extends ListActivity {

    private static final String TAG = "DeviceSelectionActivity";
    private ListView mDeviceEntryListView;

    private HttpAsynchTask mDeviceGetTask = null;
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_device_selection);
        mDeviceEntryListView = (ListView) findViewById(android.R.id.list);
        SharedPreferences pref = getSharedPreferences(PrefConst.PREFS_NAME, 0);
        myAdapter = new MyAdapter(this, new ArrayList<ParticleDevice>());
        if (pref.contains(PrefConst.ACCOUNT_EMAIL_KEY) && pref.contains(PrefConst.ACCOUNT_PASSWORD_KEY)) {
            mDeviceGetTask = new HttpAsynchTask(myAdapter);
            mDeviceGetTask.execute(new OakAccountInfo(pref.getString(PrefConst.ACCOUNT_EMAIL_KEY, ""), pref.getString(PrefConst.ACCOUNT_PASSWORD_KEY, "")));
        } else {
            String error = "Device list cannot be called before giving credential!";
            ErrorDialog errorDialog = new ErrorDialog();
            Bundle args = new Bundle(1);
            args.putString(ErrorDialog.ERROR_INFO, error);
            errorDialog.setArguments(args);
            errorDialog.show(getFragmentManager(), "errordialog");
        }
        mDeviceEntryListView.setAdapter(myAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ParticleDevice item = myAdapter.getItem(position);
        Toast.makeText(this, item.getName() + " selected", Toast.LENGTH_LONG).show();
    }

    private class DeviceListResp {
        private ParticleCloudException e;
        private List<ParticleDevice> deviceList;

        private DeviceListResp(ParticleCloudException e, List<ParticleDevice> deviceList) {
            this.e = e;
            this.deviceList = deviceList;
        }

        public ParticleCloudException getException() {
            return e;
        }

        public List<ParticleDevice> getDeviceList() {
            return deviceList;
        }
    }
    private class HttpAsynchTask extends AsyncTask<OakAccountInfo, Void, DeviceListResp> {
        private final MyAdapter myAdapter;

        private HttpAsynchTask(MyAdapter myAdapter) {
            this.myAdapter = myAdapter;
        }

        @Override
        protected DeviceListResp doInBackground(final OakAccountInfo... params) {
            try {
                Log.v(TAG, "getDeviceList.doInBackground");
                Log.v(TAG, "started");
                return new DeviceListResp(null, ParticleCloudSDK.getCloud().getDevices());
            } catch (ParticleCloudException e) {
                Log.v(TAG, "getDeviceList.doInBackground");
                Log.v(TAG, "catch exception");
                e.printStackTrace();
                return new DeviceListResp(e, null);
            }
        }

        @Override
        protected void onPostExecute(final DeviceListResp resp) {
            //  mAuthTask = null;
            mDeviceGetTask = null;
            //showProgress(false);

            Log.v(TAG, "getDeviceList.onPostExecute");
            ParticleCloudException e = resp.getException();
            List<ParticleDevice> deviceList = resp.getDeviceList();
            if (null == e && null != deviceList) {
                Log.v(TAG, "UpdatingEntries - got " + deviceList.size() + " devices");
                myAdapter.upDateEntries(deviceList);
            } else if (null == deviceList) {
                Log.v(TAG, "UpdatingEntries - got an empty device list");
                String error = getText(R.string.error_fetching_device_particle) + " - Got an empty device list";
                ErrorDialog errorDialog = new ErrorDialog();
                Bundle args = new Bundle(1);
                args.putString(ErrorDialog.ERROR_INFO, error);
                errorDialog.setArguments(args);
                errorDialog.show(getFragmentManager(), "errordialog");
            } else {
                Log.v(TAG, "UpdatingEntries - error");
                String error = getText(R.string.error_fetching_device_particle) + " - Error code: " + e.getBestMessage() + " - " + e.getServerErrorMsg();
                ErrorDialog errorDialog = new ErrorDialog();
                Bundle args = new Bundle(1);
                args.putString(ErrorDialog.ERROR_INFO, error);
                errorDialog.setArguments(args);
                errorDialog.show(getFragmentManager(), "errordialog");
            }
        }
    }

    private class MyAdapter extends ArrayAdapter<ParticleDevice> {

        private final Context context;
        private List<ParticleDevice> devicesList;

        public MyAdapter(final Context context,
                         final List<ParticleDevice> objects) {
            super(context, -1, objects);
            this.context = context;
            this.devicesList = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.device_entry_layout, parent, false);
            String name = devicesList.get(position).getName();
            Log.v(TAG, "MyAdapter - set device name " + name);
            TextView deviceNameView = (TextView) rowView.findViewById(R.id.device_name);
            deviceNameView.setText(name);
            String deviceID = devicesList.get(position).getID();
            Log.v(TAG, "MyAdapter - set device ID " + deviceID);
            TextView deviceIdView = (TextView) rowView.findViewById(R.id.device_id);
            deviceIdView.setText(deviceID);
            return rowView;
        }

        public void upDateEntries(List<ParticleDevice> entries) {
            Log.v(TAG, "MyAdapter.upDateEntries");
            devicesList.clear();
            if (null != entries) {
                Log.v(TAG, "Adding the " + entries.size() + " new entries");
                devicesList.addAll(entries);
            } else {
                Log.v(TAG, "Got 0 entries");
            }
            this.notifyDataSetChanged();
        }
    }
}
