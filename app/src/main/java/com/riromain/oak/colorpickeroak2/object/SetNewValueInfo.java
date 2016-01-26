package com.riromain.oak.colorpickeroak2.object;

/**
 * Created by rrinie on 25.01.16.
 */
public class SetNewValueInfo {
    private final OakInfo oakInfo;
    private final String newValue;

    public SetNewValueInfo(String deviceId,
                           String accessToken,
                           String newValue) {
        this(new OakInfo(deviceId, accessToken), newValue);
    }
    public SetNewValueInfo(OakInfo oakInfo,
                            String newValue) {
        this.oakInfo = oakInfo;
        this.newValue = newValue;
    }

    public String getDeviceId() {
        return oakInfo.getDeviceId();
    }

    public String getAccessToken() {
        return oakInfo.getAccessToken();
    }

    public String getNewValue() {
        return newValue;
    }

    public OakInfo getOakInfo() {
        return oakInfo;
    }
}
