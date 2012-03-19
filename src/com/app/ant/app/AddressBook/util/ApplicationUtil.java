package com.app.ant.app.AddressBook.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.app.ant.R;


/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 22.12.11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationUtil {
    public static String version = "1.0";
    public static String packageName;


    /**
     * Set version from manifest to ApplicationUtil.version field
     */
    public static void setVersionName(Context context) {
        version = getVersionName(context);
    }

    public static String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        String versionName = null;
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            packageName = pi.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static String getApplicationName(Context context) {
        return context.getString(R.string.app_name);
    }

}
