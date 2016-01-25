package com.riromain.oak.colorpickeroak2;

/**
 * Created by Rinie Romain on 24/01/2016.
 */
public class HttpGetStringResp {
    int errorCode;
    String error;
    String content;
    private HttpGetStringResp() {
    }

    public static HttpGetStringResp error(String error) {
        return new HttpGetStringResp(null, error, 0);
    }

    public static HttpGetStringResp errorWithCode(String error, int errorCode) {
        return new HttpGetStringResp(null, error, errorCode);
    }
    public static HttpGetStringResp success(String content) {
        return new HttpGetStringResp(content, null, 0);
    }

    private HttpGetStringResp(String content, String error, int errorCode) {
        this.content = content;
        this.error = error;
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getError() {
        return error;
    }

    public String getContent() {
        return content;
    }
}
