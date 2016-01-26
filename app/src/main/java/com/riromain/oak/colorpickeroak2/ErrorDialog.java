package com.riromain.oak.colorpickeroak2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by rrinie on 25.01.16.
 */
public class ErrorDialog extends DialogFragment {
    public static final String ERROR_INFO = "com.riromain.oak.colorpickeroak2.errorinfo";

    public ErrorDialog() {
        super();
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String errorInfo = getArguments().getString(ERROR_INFO);
        if (null == errorInfo) {
            errorInfo = "No error info provided";
        }
        builder.setMessage(errorInfo)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO something
                    }
                });
        return builder.create();
    }
}
