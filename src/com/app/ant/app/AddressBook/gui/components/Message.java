package com.app.ant.app.AddressBook.gui.components;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Vibrator;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.Toast;
import com.app.ant.app.AddressBook.Strings;
import com.app.ant.app.AddressBook.util.CommonHandler;

public class Message {
    private static final int MINUTES_IN_HOUR = 60;
    private static long[] vibrationPatternDouble = new long[]{500, 20, 100, 60, 100};
    private static Vibrator vibrator = null;
    private static final CharSequence EMPTY = " ";

    public static Dialog info(Context context, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(text);
        builder.setMessage(EMPTY);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setPositiveButton(Strings.OK, null);
        return builder.create();
    }

    public static Dialog error(Context context, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(text);
        builder.setMessage(EMPTY);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(Strings.OK, null);
        return builder.create();
    }

    public static Dialog errorLongText(Context context, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(Strings.ERROR_TITLE);
        builder.setMessage(text);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(Strings.OK, null);
        return builder.create();
    }

    public static Toast shortHint(String text, Context context) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
        return toast;
    }

    public static void hint(String text, Context context) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static void hintInLooper(final String text, final Context c) {
        CommonHandler h = new CommonHandler();
        h.post(new Runnable() {
            @Override
            public void run() {
                hint(text, c);
            }
        });
    }

    public static void hapticHint(String text, Context context) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        getVibrator(context).vibrate(vibrationPatternDouble, -1);
    }

    public static Vibrator getVibrator(Context context) {
        if (vibrator == null)
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        return vibrator;
    }

    public static Dialog optionalConfirm(Context context, String title, DialogInterface.OnClickListener listenerPositive,
                                         CompoundButton.OnCheckedChangeListener listenerOnChecked) {
        return customOptionalConfirm(context, title, null, Strings.DONT_ASK_AGAIN, listenerPositive, listenerOnChecked);
    }

    public static Dialog customOptionalConfirm(Context context, String title, String text, String option, DialogInterface.OnClickListener listenerPositive,
                                               CompoundButton.OnCheckedChangeListener listenerOnChecked) {
        AlertDialog.Builder builderConfirm = new AlertDialog.Builder(context);
        builderConfirm.setTitle(title);
        builderConfirm.setIcon(android.R.drawable.ic_dialog_info);
        builderConfirm.setMessage(text);
        builderConfirm.setPositiveButton(Strings.YES, listenerPositive);
        builderConfirm.setNegativeButton(Strings.NO, null);
        CheckBox checkBoxConfirm = new CheckBox(context);
        checkBoxConfirm.setText(option);
        checkBoxConfirm.setOnCheckedChangeListener(listenerOnChecked);
        builderConfirm.setView(checkBoxConfirm);
        return builderConfirm.create();
    }

    public static Dialog confirmation(Context context, String title, String text, DialogInterface.OnClickListener buttonListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        if (text != null)
            builder.setMessage(text);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(Strings.YES, buttonListener);
        builder.setNegativeButton(Strings.CANCEL, null);
        return builder.create();
    }

    public static Dialog confirmationYesNo(
            Context context, String title, String text, DialogInterface.OnClickListener buttonListener, boolean no
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        if (text != null)
            builder.setMessage(text);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(Strings.YES, buttonListener);
        if (no)
            builder.setNegativeButton(Strings.NO, null);
        else
            builder.setNegativeButton(Strings.CANCEL, null);

        return builder.create();
    }

    public static Dialog confirmationYesNo(
            Context context, String title, String text, DialogInterface.OnClickListener buttonListener, DialogInterface.OnClickListener buttonListener1, boolean no
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        if (text != null)
            builder.setMessage(text);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(Strings.YES, buttonListener);
        if (no)
            builder.setNegativeButton(Strings.NO, buttonListener1);
        else
            builder.setNegativeButton(Strings.CANCEL, null);

        return builder.create();
    }

    public static Dialog createContextMenu(Context context, String title, String[] items, DialogInterface.OnClickListener listener) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setItems(items, listener)
                .create();
    }

    public static Dialog getTimeDialog(Context context, String title, String text, DialogInterface.OnClickListener createListener, DialogInterface.OnClickListener cancelListener,
                                       int initialMinutes, TimePicker.OnTimeChangedListener timeChangelistener) {
        AlertDialog.Builder builderConfirm = new AlertDialog.Builder(context);
        builderConfirm.setTitle(title);
        builderConfirm.setMessage(text);
        builderConfirm.setIcon(android.R.drawable.ic_dialog_info);
        builderConfirm.setPositiveButton(Strings.SET, createListener);
        builderConfirm.setNegativeButton(Strings.CANCEL, cancelListener);
        TimePicker time = new TimePicker(context);
        time.setIs24HourView(true);
        time.setOnTimeChangedListener(timeChangelistener);
        time.setCurrentHour(initialMinutes / MINUTES_IN_HOUR);
        time.setCurrentMinute(initialMinutes % MINUTES_IN_HOUR);
        builderConfirm.setView(time);
        return builderConfirm.create();
    }

    public static Dialog createInputDialog(Context context, String title, View editText, DialogInterface.OnClickListener onOk) {
        return createInputDialog(context, title, null, editText, onOk);
    }

    public static Dialog createInputDialog(Context context, String title, String text, View editText, DialogInterface.OnClickListener onOk) {
        AlertDialog.Builder stringRequest = new AlertDialog.Builder(context);
        stringRequest.setTitle(title);
        stringRequest.setMessage(text);
        stringRequest.setIcon(android.R.drawable.ic_dialog_info);
        stringRequest.setPositiveButton(Strings.OK, onOk);
        stringRequest.setView(editText);
        return stringRequest.create();
    }
}
