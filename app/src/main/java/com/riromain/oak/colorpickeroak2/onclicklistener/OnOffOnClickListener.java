package com.riromain.oak.colorpickeroak2.onclicklistener;

import android.app.Activity;
import android.view.View;

import com.riromain.oak.colorpickeroak2.http.ExecuteOakFunction;
import com.riromain.oak.colorpickeroak2.object.OakFunctionRequest;

/**
 * Created by Rinie Romain on 12/04/2016.
 */
public class OnOffOnClickListener implements View.OnClickListener {
    private String arg;
    private Activity callingActivity;
    private String oakDeviceID;

    public OnOffOnClickListener(final String arg,
                                final Activity callingActivity,
                                final String oakDeviceID) {
        this.arg = arg;
        this.callingActivity = callingActivity;
        this.oakDeviceID = oakDeviceID;
    }

    @Override
    public void onClick(View v) {
        new ExecuteOakFunction(callingActivity)
                .execute(new OakFunctionRequest(oakDeviceID, "led", arg));
    }
}
