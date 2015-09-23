package com.mbdilaver.stormy.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import com.mbdilaver.stormy.R;

/**
 * Created by MBD on 04.09.2015.
 */
public class AlertDialogFragment extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(context.getString(R.string.alertTitle))
                .setMessage(context.getString(R.string.alertMessage))
                .setPositiveButton(context.getString(R.string.alertButtonText), null);

        return builder.create();
    }


}
