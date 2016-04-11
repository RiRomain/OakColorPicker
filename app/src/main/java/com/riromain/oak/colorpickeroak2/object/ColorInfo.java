package com.riromain.oak.colorpickeroak2.object;

/**
 * Created by rrinie on 11.04.16.
 */
public class ColorInfo {
    private Integer redValue;
    private Integer greenValue;
    private Integer blueValue;
    private Integer whiteValue;
    private Integer intensity;

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

    public boolean areAllValueSet() {
        return null != redValue && null != greenValue && null != blueValue && null != whiteValue && null != intensity;
    }
}
