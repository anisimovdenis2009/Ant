package com.app.ant.app.AddressBook.gui.components;

import android.preference.DialogPreference;
import android.preference.Preference;

import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 29.09.11
 * Time: 14:47
 * To change this template use File | settings | File Templates.
 */
public interface ItemSaveListener {
    void onSaveItem(Preference parent,int number, String title);

    void onSaveItem(int number, Calendar title);
}
