package com.riromain.oak.colorpickeroak2.object;

/**
 * Created by rrinie on 25.01.16.
 */
public class GetVariableInfo {
    private final OakInfo oakInfo;
    private final String variableId;

    public GetVariableInfo(String deviceId,
                           String accessToken,
                           String newValue) {
        this(new OakInfo(deviceId, accessToken), newValue);
    }
    public GetVariableInfo(OakInfo oakInfo,
                            String variableId) {
        this.oakInfo = oakInfo;
        this.variableId = variableId;
    }

    public String getDeviceId() {
        return oakInfo.getDeviceId();
    }

    public String getAccessToken() {
        return oakInfo.getAccessToken();
    }

    public String getVariableId() {
        return variableId;
    }

    public OakInfo getOakInfo() {
        return oakInfo;
    }
}
