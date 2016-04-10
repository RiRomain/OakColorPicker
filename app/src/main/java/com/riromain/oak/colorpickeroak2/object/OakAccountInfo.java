package com.riromain.oak.colorpickeroak2.object;

/**
 * Created by Rinie Romain on 10/04/2016.
 */
public class OakAccountInfo {
    private final String email;
    private final String password;

    public OakAccountInfo(final String email, final String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
