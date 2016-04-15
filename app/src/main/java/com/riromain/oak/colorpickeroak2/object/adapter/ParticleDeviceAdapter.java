package com.riromain.oak.colorpickeroak2.object.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.riromain.oak.colorpickeroak2.R;
import com.riromain.oak.colorpickeroak2.object.DeviceStatus;
import com.riromain.oak.colorpickeroak2.object.ParcelableDevice;

import java.util.List;

import io.particle.android.sdk.cloud.ParticleDevice;

/**
 * Created by rrinie on 11.04.16.
 */
public class ParticleDeviceAdapter extends ArrayAdapter<ParcelableDevice> {
    private static final String TAG = "ParticleDeviceAdapter";

    private final Context context;
    private List<ParcelableDevice> devicesList;

    public ParticleDeviceAdapter(final Context context,
                                 final List<ParcelableDevice> objects) {
        super(context, -1, objects);
        this.context = context;
        this.devicesList = objects;
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.device_entry_layout, parent, false);

        ParcelableDevice particleDevice = devicesList.get(position);
        String name = particleDevice.getDeviceName();
        String deviceID = particleDevice.getDeviceID();

        Log.v(TAG, "set device name " + name);
        TextView deviceNameView = (TextView) rowView.findViewById(R.id.device_name);
        deviceNameView.setText(name);
        Log.v(TAG, "set device ID " + deviceID);
        TextView deviceIdView = (TextView) rowView.findViewById(R.id.device_id);
        deviceIdView.setText(deviceID);
        DeviceStatus status = particleDevice.getStatus();
        Log.v(TAG, "set connection status " + status.name());
        if (DeviceStatus.DISCONNECTED.equals(status)) {
            rowView.setBackgroundColor(Color.RED);
        }
        if (DeviceStatus.NOT_COMPATIBLE.equals(status)) {
            rowView.setBackgroundColor(Color.YELLOW);
        }
        if (DeviceStatus.CONNECTED.equals(status)) {
            rowView.setBackgroundColor(Color.GREEN);
        }
        return rowView;
    }

    // It gets a View that displays in the drop down popup the data at the specified position
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    // It gets a View that displays the data at the specified position
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }
}