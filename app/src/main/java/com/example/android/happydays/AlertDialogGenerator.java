package com.example.android.happydays;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Created by usuario on 15/06/2015.
 */
public class AlertDialogGenerator {

    public void showAlertDialog(Context context, String message, String title){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
