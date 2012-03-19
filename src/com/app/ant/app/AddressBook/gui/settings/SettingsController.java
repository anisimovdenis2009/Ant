package com.app.ant.app.AddressBook.gui.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;
import com.app.ant.R;
import com.app.ant.app.AddressBook.gui.StartActivity;
import com.app.ant.app.AddressBook.gui.components.*;
import com.app.ant.app.AddressBook.options.Options;
import com.app.ant.app.AddressBook.options.OptionsUtil;
import com.app.ant.app.AddressBook.util.GUIFactory;
import com.app.ant.app.AddressBook.util.IOUtil;

import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 30.09.11
 * Time: 14:55
 * To change this template use File | settings | File Templates.
 */
public class SettingsController extends PreferenceActivity implements ItemSaveListener {
    private PreferenceScreen preferenceScreen;
    private MyPreference id;
    private MyEditPreference firstAddress;
    private MyEditPreference secondAddress;
    private MyEditPreference port;
    private MyEditPreference login;
    private MyEditPreference password;
    private MyCheckBoxPreference isRussia;
    private MyCheckBoxPreference isGPS;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int number = 0;
        preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        //m = new SettingsModel(Common.SETTINGS);
        init();
        populate();
        setPreferenceScreen(preferenceScreen);
    }

    private void init() {
        id = new MyPreference(this);
        id.setTitle("ID");
        String summary = StartActivity.getId();
        id.setSummary(summary);
        int i = 0;
        firstAddress = GUIFactory.myEditPreference(this, "Первый адрес FTP", i++);
        firstAddress.setSummary(Options.firstAddress);

        secondAddress = GUIFactory.myEditPreference(this, "Второй адрес FTP", i++);
        secondAddress.setSummary(Options.secondAddress);

        port = GUIFactory.myEditPreference(this, "Порт", i++);
        port.setSummary(String.valueOf(Options.port));

        login = GUIFactory.myEditPreference(this, "Логин", i++);
        login.setSummary(Options.login);

        password = GUIFactory.myEditPreference(this, "Пароль", i++);
        password.setSummary(Options.password);
        password.setLayoutResource(R.layout.preference_password);

        isRussia = GUIFactory.myCheckBoxPreference(this, "Включить версию для России", i++, "Россия", "Белоруссия");
        isRussia.setChecked(Options.isRussian);

        isGPS = GUIFactory.myCheckBoxPreference(this, "Включить возможность запоминать GPS координаты", i++, "Включено", "Выключено");
        isGPS.setChecked(Options.isGPS);
    }


    private void populate() {
        preferenceScreen.addPreference(id);
        preferenceScreen.addPreference(firstAddress);
        preferenceScreen.addPreference(secondAddress);
        preferenceScreen.addPreference(port);
        preferenceScreen.addPreference(login);
        preferenceScreen.addPreference(password);
        preferenceScreen.addPreference(isRussia);
        preferenceScreen.addPreference(isGPS);
    }

    /* private void init() {
        preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        int i = 0;
        for (String title : m.getTitles()) {
            MyEditPreference t = new MyEditPreference(this);
            t.setTitle(title);
            t.setSummary(getValue(i));
            t.setNumber(i);
            t.setOnItemSaveListener(this);
            preferenceScreen.addPreference(t);
            i++;
        }

    }*/

    @Override
    public void onSaveItem(Preference parent, int number, String title) {
        switch (number) {
            case 0:
                Options.firstAddress = title;
                break;
            case 1:
                Options.secondAddress = title;
                break;
            case 2:
                if (IOUtil.tryToParse(this, parent, title))
                    Options.port = Integer.parseInt(title);
                break;
            case 3:
                Options.login = title;
                break;
            case 4:
                Options.password = title;
                break;
            case 5:
                Options.isRussian = Boolean.valueOf(title);
                break;
            case 6:
                Options.isGPS = Boolean.valueOf(title);
                break;
        }
    }

    @Override
    public void onSaveItem(int number, Calendar title) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onBackPressed() {
        Message.confirmationYesNo(this, " ", "Сохранить измененные настройки в файл?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OptionsUtil.saveAsync();
                        SettingsController.super.onBackPressed();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SettingsController.super.onBackPressed();
                    }
                }, true
        ).show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(0, 0, 0, "Вернуть сохранённые значаения").setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(1, 1, 1, "Сохранить значения?").setIcon(R.drawable.ic_menu_archive);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                OptionsUtil.loadAsync();
                break;
            case 1:
                OptionsUtil.saveAsync();
                break;

        }
        return true;
    }
}