package com.riromain.oak.colorpickeroak2.object;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rrinie on 15.04.16.
 */
public class ParcelableDevice implements Parcelable {
    private String deviceID;
    private String deviceName;
    private String lastConnection;
    private DeviceStatus status;
    private ColorInfo colorInfo;

    public ParcelableDevice() {}

    protected ParcelableDevice(Parcel in) {
        deviceID = in.readString();
        deviceName = in.readString();
        lastConnection = in.readString();
        status = in.readParcelable(DeviceStatus.class.getClassLoader());
        colorInfo = in.readParcelable(ColorInfo.class.getClassLoader());
    }

    public static final Creator<ParcelableDevice> CREATOR = new Creator<ParcelableDevice>() {
        @Override
        public ParcelableDevice createFromParcel(Parcel in) {
            return new ParcelableDevice(in);
        }

        @Override
        public ParcelableDevice[] newArray(int size) {
            return new ParcelableDevice[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(deviceID);
        dest.writeString(deviceName);
        dest.writeString(lastConnection);
        dest.writeParcelable(status, flags);
        dest.writeParcelable(colorInfo, flags);
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(final String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(final String deviceName) {
        this.deviceName = deviceName;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(final DeviceStatus status) {
        this.status = status;
    }

    public ColorInfo getColorInfo() {
        return colorInfo;
    }

    public void setColorInfo(final ColorInfo colorInfo) {
        this.colorInfo = colorInfo;
    }

    public String getLastConnection() {
        return lastConnection;
    }

    public void setLastConnection(final String lastConnection) {
        this.lastConnection = lastConnection;
    }
}
