package com.app.ant.app.AddressBook.gui.creditask;

import android.content.Context;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import com.app.ant.app.AddressBook.gui.components.MyEditPreference;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 29.09.11
 * Time: 10:02
 * To change this template use File | settings | File Templates.
 */
@Deprecated
public class CreditAskView {
    private Context context;
    private PreferenceManager manager;
    private PreferenceScreen preferenceScreen;
    private CreditCardXMLLoader model;


    public CreditAskView(Context context, PreferenceManager manager, CreditCardXMLLoader m) {
       this.context = context;
       this.manager = manager;
       this.model = m;

       init();
    }

    private void init() {
         preferenceScreen = manager.createPreferenceScreen(context);
        for(String title: model.getTitles()){
            MyEditPreference t = new MyEditPreference(context);
            t.setTitle(title);
            //t.setOnItemSaveListener(this);
            preferenceScreen.addPreference(t);
        }
    }

    public PreferenceScreen getPreferenceScreen() {
        return preferenceScreen;
    }
}
