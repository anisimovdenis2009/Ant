package com.app.ant.app.AddressBook.util;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 10.06.11
 * Time: 17:04
 * To change this template use File | settings | File Templates.
 */
public class CommonHandler extends Handler {

    static HandlerThread handlerthread = new HandlerThread("CommonHandlerThread");

    static {
        handlerthread.start();
    }

    public CommonHandler() {
        super(handlerthread.getLooper());
    }
}
