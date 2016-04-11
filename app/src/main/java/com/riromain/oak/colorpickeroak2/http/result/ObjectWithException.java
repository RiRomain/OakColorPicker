package com.riromain.oak.colorpickeroak2.http.result;

import io.particle.android.sdk.cloud.ParticleCloudException;

/**
 * Created by rrinie on 11.04.16.
 */
public final class ObjectWithException<T> {
    private Exception e;
    private T object;

    public ObjectWithException(final Exception e,
                               final T object) {
        this.e = e;
        this.object = object;
    }

    public Exception getException() {
        return e;
    }

    public T getObject() {
        return object;
    }
}
