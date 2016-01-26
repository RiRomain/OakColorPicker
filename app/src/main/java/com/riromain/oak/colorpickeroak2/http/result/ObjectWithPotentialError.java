package com.riromain.oak.colorpickeroak2.http.result;

/**
 * Created by rrinie on 25.01.16.
 */
public interface ObjectWithPotentialError<T> {
    Integer getErrorCode();
    String getError();
    T getContent();
}
