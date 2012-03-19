package com.app.ant.app.AddressBook.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import com.app.ant.app.AddressBook.Strings;


/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 28.04.11
 * Time: 16:29
 * To change this template use File | settings | File Templates.
 */
public class GUIUtil {

    public static int listItemHeight = 64;
    public static float density = 1;

    public static LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    static {
        l.weight = 1;
        l.gravity = Gravity.CENTER;
    }

    public static Dialog getWaitDialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(Strings.LOADING);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        return dialog;
    }
}

