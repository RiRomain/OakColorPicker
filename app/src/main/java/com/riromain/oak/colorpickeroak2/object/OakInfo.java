package com.riromain.oak.colorpickeroak2.object;

/**
 * Created by rrinie on 25.01.16.
 */
public class OakInfo {
    private final String deviceId;
    private final String accessToken;

    public OakInfo(final String deviceId,
                   final String accessToken) {
        this.deviceId = deviceId;
        this.accessToken = accessToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
