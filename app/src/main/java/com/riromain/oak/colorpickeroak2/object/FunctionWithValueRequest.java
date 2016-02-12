package com.riromain.oak.colorpickeroak2.object;

/**
 * Created by rrinie on 25.01.16.
 */
public class FunctionWithValueRequest {
    private final OakInfo oakInfo;
    private final String newValue;
    private final String serviceId;

    public FunctionWithValueRequest(final OakInfo oakInfo,
                                    final String newValue,
                                    final String serviceId) {
        this.oakInfo = oakInfo;
        this.newValue = newValue;
        this.serviceId = serviceId;
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

    public String getServiceId() {
        return serviceId;
    }
}
