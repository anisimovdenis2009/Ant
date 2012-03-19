package com.app.ant.app.AddressBook.database;

import android.app.Service;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 03.10.11
 * Time: 14:41
 * To change this template use File | Settings | File Templates.
 */
public class DataBaseService extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        ClientCardDataHelper dataHelper = new ClientCardDataHelper(this);
        super.onStart(intent, startId);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
