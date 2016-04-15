package com.riromain.oak.colorpickeroak2.object;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rrinie on 15.04.16.
 */
public enum DeviceStatus implements Parcelable {
    CONNECTED,
    NOT_COMPATIBLE,
    DISCONNECTED;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(ordinal());
    }

    public static final Creator<DeviceStatus> CREATOR = new Creator<DeviceStatus>() {
        @Override
        public DeviceStatus createFromParcel(final Parcel source) {
            return DeviceStatus.values()[source.readInt()];
        }

        @Override
        public DeviceStatus[] newArray(final int size) {
            return new DeviceStatus[size];
        }
    };
}
