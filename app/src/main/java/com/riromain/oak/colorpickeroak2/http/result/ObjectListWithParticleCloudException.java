package com.riromain.oak.colorpickeroak2.http.result;

import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloudException;

/**
 * Created by rrinie on 11.04.16.
 */
public final class ObjectListWithParticleCloudException<T> {
    private ParticleCloudException e;
    private List<T> deviceList;

    public ObjectListWithParticleCloudException(final ParticleCloudException e,
                                                 final List<T> deviceList) {
        this.e = e;
        this.deviceList = deviceList;
    }

    public ParticleCloudException getException() {
        return e;
    }

    public List<T> getDeviceList() {
        return deviceList;
    }
}
