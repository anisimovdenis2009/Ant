package com.app.ant.app.AddressBook.util;



/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 29.04.11
 * Time: 11:13
 * To change this template use File | settings | File Templates.
 */
public class Monitor {
    private final Object mutex = new Object();
    private boolean interrupt = false;
    private boolean onlyOne = false;

    public void doWait() {
        synchronized (mutex) {
            while (!interrupt) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!onlyOne)
                interrupt = false;
        }
    }

    public void doNotify() {
        synchronized (mutex) {
            interrupt = true;
            mutex.notify();
        }
    }

    public Monitor once() {
        onlyOne = true;
        return this;
    }
}
