package com.riromain.oak.colorpickeroak2.http.result;

/**
 * Created by rrinie on 25.01.16.
 */
public class ObjectWithPotentialErrorImpl<T> implements ObjectWithPotentialError<T> {
    private T object;
    Integer errorCode;
    String error;

    public ObjectWithPotentialErrorImpl(final T object) {
        this(object, null, null);
    }

    public ObjectWithPotentialErrorImpl(final Integer errorCode,
                                        final String error) {
        this(null, errorCode, error);
    }

    public ObjectWithPotentialErrorImpl(final T object,
                                        final Integer errorCode,
                                        final String error) {
        this.error = error;
        this.errorCode = errorCode;
        this.object = object;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getError() {
        return error;
    }

    public T getContent() {
        return object;
    }
}
