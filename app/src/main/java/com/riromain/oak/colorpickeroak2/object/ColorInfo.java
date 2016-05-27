package com.riromain.oak.colorpickeroak2.object;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rrinie on 11.04.16.
 */
public class ColorInfo implements Parcelable {
    private Integer redValue;
    private Integer greenValue;
    private Integer blueValue;
    private Integer whiteValue;
    private Integer intensity;

    public ColorInfo() {}
    public ColorInfo(final Parcel in) {
        this.redValue = in.readInt();
        this.greenValue = in.readInt();
        this.blueValue = in.readInt();
        this.whiteValue = in.readInt();
        this.intensity = in.readInt();
    }

    public static final Parcelable.Creator<ColorInfo> CREATOR = new Parcelable.Creator<ColorInfo>()
    {
        @Override
        public ColorInfo createFromParcel(Parcel source)
        {
            return new ColorInfo(source);
        }

        @Override
        public ColorInfo[] newArray(int size)
        {
            return new ColorInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(redValue);
        dest.writeInt(greenValue);
        dest.writeInt(blueValue);
        dest.writeInt(whiteValue);
        dest.writeInt(intensity);
    }

    public Integer getRedValue() {
        return redValue;
    }

    public void setRedValue(final Integer redValue) {
        this.redValue = redValue;
    }

    public Integer getGreenValue() {
        return greenValue;
    }

    public void setGreenValue(final Integer greenValue) {
        this.greenValue = greenValue;
    }

    public Integer getBlueValue() {
        return blueValue;
    }

    public void setBlueValue(final Integer blueValue) {
        this.blueValue = blueValue;
    }

    public Integer getWhiteValue() {
        return whiteValue;
    }

    public void setWhiteValue(final Integer whiteValue) {
        this.whiteValue = whiteValue;
    }

    public Integer getIntensity() {
        return intensity;
    }

    public void setIntensity(final Integer intensity) {
        this.intensity = intensity;
    }
}
