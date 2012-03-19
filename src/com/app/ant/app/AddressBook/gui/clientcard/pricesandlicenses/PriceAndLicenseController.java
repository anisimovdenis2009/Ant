package com.app.ant.app.AddressBook.gui.clientcard.pricesandlicenses;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import com.app.ant.R;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.components.*;
import com.app.ant.app.AddressBook.xmlfeatures.JDEOptions;

import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 28.11.11
 * Time: 13:48
 * To change this template use File | Settings | File Templates.
 */
public class PriceAndLicenseController extends PreferenceActivity implements ItemSaveListener {
    private MyListPreference priseColumn;
    private MyCheckBoxPreference discount;

    private MyCalendarView licenseStart;
    private MyCalendarView licenseEnd;
    private MyEditPreference licenseSerial;
    private MyEditPreference licenseNumber;
    private PreferenceScreen preferenceScreen;
    private PriceAndLicenseModel m;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        if (getIntent().hasExtra(Common.PRICE_EXTRA))
            m = (PriceAndLicenseModel) getIntent().getSerializableExtra(Common.PRICE_EXTRA);
        else
            m = new PriceAndLicenseModel();
        init();
        populate();
        setPreferenceScreen(preferenceScreen);
    }

    private void populate() {
        preferenceScreen.addPreference(priseColumn);
        preferenceScreen.addPreference(discount);
        preferenceScreen.addPreference(licenseStart);
        preferenceScreen.addPreference(licenseEnd);
        preferenceScreen.addPreference(licenseSerial);
        preferenceScreen.addPreference(licenseNumber);
    }

    private void init() {
        int number = 0;
        priseColumn = new MyListPreference(this);
        priseColumn.setTitle("Колонка прайса");
        priseColumn.setEntries(JDEOptions.getEntries(500));
        priseColumn.setEntryValues(JDEOptions.getEnKeys(500));
        priseColumn.setOnItemSaveListener(this);
        priseColumn.setLayoutResource(R.layout.preference_red);
        String value = getValue(number);
        if (value != null) {
            priseColumn.setValue(value);
            priseColumn.setSummary(priseColumn.getEntry().toString());
        }
        priseColumn.setNumber(number++);

        discount = new MyCheckBoxPreference(this);
        discount.setTitle("Расчёт скидки");
        discount.setOnItemSaveListener(this);
        discount.setNumber(number++);
        discount.setChecked(m.isDiscount());

        licenseSerial = new MyEditPreference(this);
        licenseSerial.setTitle("Серия лицензии");
        licenseSerial.setOnItemSaveListener(this);
        licenseSerial.setNumber(number++);
        licenseSerial.setSummary(m.getLicenseSerial());

        licenseNumber = new MyEditPreference(this);
        licenseNumber.setTitle("Номер лицензии");
        licenseNumber.setOnItemSaveListener(this);
        licenseNumber.setNumber(number++);
        licenseNumber.setSummary(m.getLicenseNumber());

        licenseStart = new MyCalendarView(this);
        licenseStart.setTitle("Дата выдачи лицензии");
        licenseStart.setNumber(number++);
        licenseStart.setOnItemSaveListener(this);
        licenseStart.setDefaultValue(m.getLicenseStart());

        licenseEnd = new MyCalendarView(this);
        licenseEnd.setNumber(number++);
        licenseEnd.setTitle("Срок действия лицензии");
        licenseEnd.setOnItemSaveListener(this);
        licenseEnd.setDefaultValue(m.getLicenseEnd());
    }

    @Override
    public void onSaveItem(Preference parent, int number, String title) {
        switch (number) {
            case 0:
                m.setPriseColumn(((ListPreference) parent).getValue());
                break;
            case 1:
                m.setDiscount(Boolean.parseBoolean(title));
                break;
            case 2:
                m.setLicenseSerial(title);
                break;
            case 3:
                m.setLicenseNumber(title);
                break;
        }
    }

    @Override
    public void onSaveItem(int number, Calendar title) {
        switch (number) {
            case 8:
                m.setLicenseStart(title);
                break;
            case 9:
                m.setLicenseEnd(title);
                break;
        }
    }


    private String getValue(int number) {
        String value = "";
        switch (number) {
            case 0:
                value = m.getPriseColumn();
                break;
            case 1:
                value = String.valueOf(m.isDiscount());
                break;
            case 2:
                value = m.getLicenseSerial();
                break;
            case 3:
                value = m.getLicenseNumber();
                break;
        }
        return value;
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Common.PRICE_EXTRA, m);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}