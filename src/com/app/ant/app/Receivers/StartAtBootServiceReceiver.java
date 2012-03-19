package com.app.ant.app.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 08.02.12
 * Time: 17:37
 * To change this template use File | Settings | File Templates.
 */
public class StartAtBootServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent i = new Intent();
            i.setAction("com.gpsstart.StartAtBootService");
            context.startService(i);
        }
    }
}
