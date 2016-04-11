package com.riromain.oak.colorpickeroak2.http.result;

import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloudException;

/**
 * Created by rrinie on 11.04.16.
 */
public final class ObjectWithParticleCloudException<T> {
    private ParticleCloudException e;
    private T object;

    public ObjectWithParticleCloudException(final ParticleCloudException e,
                                            final T object) {
        this.e = e;
        this.object = object;
    }

    public ParticleCloudException getException() {
        return e;
    }

    public T getObject() {
        return object;
    }
}
