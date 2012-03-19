package com.app.ant.app.AddressBook.gui.clientcard.lawcontacts.addcontact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.components.ItemSaveListener;
import com.app.ant.app.AddressBook.gui.components.MyEditPreference;
import com.app.ant.app.AddressBook.pojos.Contact;

import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 21.10.11
 * Time: 16:43
 * To change this template use File | Settings | File Templates.
 */
public class AddContactsController extends PreferenceActivity implements ItemSaveListener {
    private AddContactsModel m;
    //private LawContactModel model;
    private PreferenceScreen preferenceScreen;
    private Contact data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //model = (LawContactModel) getIntent().getSerializableExtra(Common.CONTACT_EXTRA);
        preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        data = new Contact();
        m = new AddContactsModel();
        init();
        setPreferenceScreen(preferenceScreen);
    }

    private void init() {
        preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        int i = 0;
        for (String title : m.getTitles()) {
            MyEditPreference t = new MyEditPreference(this);
            t.setTitle(title);
            t.setNumber(i);
            t.setOnItemSaveListener(this);
            preferenceScreen.addPreference(t);
            i++;
        }
    }

    @Override
    public void onSaveItem(Preference parent, int number, String title) {
        m.setValue(number, title);
        String value = title;
        switch (number) {
            case 0:
                data.setFirstLastName(value);
                break;
            case 1:
                data.setTelephone(value);
                break;
            case 2:
                data.setMobileTelephone(value);
                break;
            case 3:
                data.setEmail(value);
                break;
        }
    }

    @Override
    public void onSaveItem(int number, Calendar title) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onBackPressed() {
        //model.addContact(data);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Common.CONTACT_EXTRA_RESULT, data);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}