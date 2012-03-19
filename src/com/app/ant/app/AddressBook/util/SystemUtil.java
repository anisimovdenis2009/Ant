package com.app.ant.app.AddressBook.util;

import android.os.Build;

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 28.04.11
 * Time: 16:04
 * To change this template use File | settings | File Templates.
 */
public class SystemUtil {
    public static String getPlatformVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getModelName() {
        return Build.MODEL;
    }
}
