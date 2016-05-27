package com.riromain.oak.colorpickeroak2.object;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rrinie on 11.04.16.
 */
public class OakFunctionRequest {
    private final String deviceId;
    private final List<String> arguments;
    private final String functionName;

    public OakFunctionRequest(final String deviceId,
                              final String functionName,
                              final String... argument) {
        this.deviceId = deviceId;
        this.functionName = functionName;
        this.arguments = Arrays.asList(argument);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<String> getArguments() {
        return arguments;
    }
}
