package com.giorgosmih.flightitineraries;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.ProgressBar;

public class MyProgressDialog extends DialogFragment {

    private String title;
    private String message;

    public void setData(String t, String msg){
        title = t;
        message = msg;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        ProgressBar progBar = new ProgressBar(getActivity());
        builder.setView(progBar);

        // Create the AlertDialog object and return it
        Dialog d = builder.create();
        d.setCancelable(false);
        d.setCanceledOnTouchOutside(false);

        return d;
    }
}
