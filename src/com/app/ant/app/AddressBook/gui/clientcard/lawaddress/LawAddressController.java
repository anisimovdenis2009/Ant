package com.app.ant.app.AddressBook.gui.clientcard.lawaddress;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.components.ItemSaveListener;
import com.app.ant.app.AddressBook.gui.components.MyEditPreference;
import com.app.ant.app.AddressBook.pojos.LawAddress;
import com.app.ant.app.AddressBook.util.IOUtil;

import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: vianisimov
 * Date: 20.10.11
 * Time: 11:46
 * To change this template use File | Settings | File Templates.
 */
public class LawAddressController extends PreferenceActivity implements ItemSaveListener {
    private LawAddressModel m;
    private PreferenceScreen preferenceScreen;
    private LawAddress data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra(Common.LAW_ADDRESS_EDIT_EXTRA)) {
            data = (LawAddress) intent.getSerializableExtra(Common.LAW_ADDRESS_EDIT_EXTRA);
        } else {
            data = new LawAddress();
        }
        preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        m = new LawAddressModel();
        init();
        setPreferenceScreen(preferenceScreen);
    }

    private void init() {
        preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        int i = 0;
        for (String title : m.getTitles()) {
            MyEditPreference t = new MyEditPreference(this,false);
            t.setTitle(title);
            t.setSummary(getValue(i));
            t.setNumber(i);
            t.setOnItemSaveListener(this);
            preferenceScreen.addPreference(t);
            i++;
        }

    }

    private String getValue(int number) {
        String value = "";
        switch (number) {
            case 0:
                value = data.getIndeks();
                break;
            case 1:
                value = data.getArea();
                break;
            case 2:
                value = data.getDepartment();
                break;
            case 3:
                value = data.getCity();
                break;
            case 4:
                value = data.getStreet();
                break;
            case 5:
                value = data.getHouse();
                break;
            case 6:
                value = data.getBuilding();
                break;
            case 7:
                value = data.getAppartment();
                break;
            case 8:
                value = data.getOffice();
                break;
        }
        return value;
    }

    @Override
    public void onSaveItem(Preference parent, int number, String title) {
        m.setValue(number, title);
        String value = title;
        switch (number) {
            case 0:
                if (IOUtil.validateIndeks(this, parent, title))
                    data.setIndeks(title);
                break;
            case 1:
                data.setArea(value);
                break;
            case 2:
                data.setDepartment(value);
                break;
            case 3:
                data.setCity(value);
                break;
            case 4:
                data.setStreet(value);
                break;
            case 5:
                data.setHouse(value);
                break;
            case 6:
                data.setBuilding(value);
                break;
            case 7:
                data.setAppartment(value);
                break;
            case 8:
                data.setOffice(value);
                break;
        }
    }

    @Override
    public void onSaveItem(int number, Calendar title) {

        //String value = preferenceScreen.getPreference(number).getTitle().toString();

    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Common.LAW_ADDRESS_RESULT_EXTRA, data);
        setResult(Activity.RESULT_OK, resultIntent);
        /* if (getParent() == null) {
            setResult(Activity.RESULT_OK, resultIntent);
        } else {
            getParent().setResult(Activity.RESULT_OK, resultIntent);
        }*/
        finish();
    }
}
