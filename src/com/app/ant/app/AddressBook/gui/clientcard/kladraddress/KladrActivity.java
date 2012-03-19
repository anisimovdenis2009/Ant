package com.app.ant.app.AddressBook.gui.clientcard.kladraddress;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import com.app.ant.app.AddressBook.gui.components.ItemSaveListener;
import com.app.ant.app.AddressBook.gui.components.KladrListView;
import com.app.ant.app.AddressBook.gui.components.MyListPreference;
import com.app.ant.app.AddressBook.util.GUIFactory;

import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 16.01.12
 * Time: 13:15
 * To change this template use File | Settings | File Templates.
 */
public class KladrActivity extends PreferenceActivity implements ItemSaveListener {

    public static final String STATE_TITLE = "Федеральный округ";
    public static final String AREA_TITLE = "Область";
    public static final String DEPARTMENT_TITLE = "Район";

    private PreferenceScreen preferenceScreen;
    private KladrModel m;

    //GUI
    private MyListPreference state;
    private MyListPreference area;
    private MyListPreference department;
    private MyListPreference city;
    private MyListPreference vilage;
    private KladrListView street;

    private String dataBaseName;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        m = new KladrModel();
        init();
        setPreferenceScreen(preferenceScreen);
    }

    private void init() {
        int i = 0;
        state = GUIFactory.createList(this, i++, STATE_TITLE);
        state.setEntries(new String[]{m.NORTHWEST, m.VOLGANEAR});
        state.setEntryValues(new String[]{m.NORTHWEST, m.VOLGANEAR});
        preferenceScreen.addPreference(state);
        area = GUIFactory.createList(this, i++, AREA_TITLE);

        if (state.getSummary() == m.NORTHWEST) {
            area.setEntries(m.VOLGA_AREAS);
            area.setEntryValues(m.VOLGA_AREAS_ID);
        } else {
            area.setEntries(m.NORTHWEST_AREAS);
            area.setEntryValues(m.NORTHWEST_AREAS_ID);
        }
        preferenceScreen.addPreference(area);

        department = GUIFactory.createList(this, i++, DEPARTMENT_TITLE);
        preferenceScreen.addPreference(department);

        city = GUIFactory.createList(this, i++, "Город");
        preferenceScreen.addPreference(city);

        vilage = GUIFactory.createList(this, i++, "Населённый пункт");
        preferenceScreen.addPreference(vilage);

        street = new KladrListView(this);
        street.setTitle("Street");
        street.setNumber(i++);
        preferenceScreen.addPreference(street);

    }

    @Override
    public void onSaveItem(Preference parent, int number, String title) {
        switch (number) {
            case 1: {
                m.setArea(((ListPreference) parent).getValue());
                String select = "SELECT DISTINCT KL55KR,KL55KRD FROM kladrspb ORDER BY KL55KR";
                String name = "/mnt/sdcard/piteroblast.db";
                ComboData data = getComboData(select, name);
                department.setEntries(data.res);
                department.setEntryValues(data.codes);

                Toast.makeText(this, parent.getKey(), Toast.LENGTH_LONG).show();
            }
            break;
            case 2: {
                m.setDepartment(((ListPreference) parent).getValue());
                String select = "SELECT DISTINCT KL55KG,KL55KGD FROM kladrspb WHERE KL55KR = '" + m.getDepartment() + "' ORDER BY KL55KG";
                String name = "/mnt/sdcard/piteroblast.db";
                ComboData data = getComboData(select, name);
                city.setEntries(data.res);
                city.setEntryValues(data.codes);
            }
            break;
            case 3: {
                m.setCity(((ListPreference) parent).getValue());
                String select = "SELECT DISTINCT KL55KP,KL55KPD FROM kladrspb WHERE KL55KR = '" + m.getDepartment() + "' AND KL55KG = '" + m.getCity() + "' ORDER BY KL55KPD";
                String name = "/mnt/sdcard/piteroblast.db";
                ComboData data = getComboData(select, name);
                vilage.setEntries(data.res);
                vilage.setEntryValues(data.codes);
            }
            break;
            case 4: {
                m.setVilage(((ListPreference) parent).getValue());
                String select = "SELECT DISTINCT KL55KU,KL55KUD FROM kladrspb WHERE KL55KR = '" + m.getDepartment() + "' AND KL55KG = '" + m.getCity() + "' AND KL55KP = '" + m.getVilage() + "' ORDER BY KL55KUD";
                String name = "/mnt/sdcard/piteroblast.db";
                ComboData data = getComboData(select, name);
                street.setEntries(data.res);
                street.setEntryValues(data.codes);
            }
            break;

        }
    }

    private ComboData getComboData(String select, String name) {
        ComboData data = new ComboData();
        SQLiteDatabase db;
        db = openOrCreateDatabase(
                name
                , SQLiteDatabase.CREATE_IF_NECESSARY
                , null
        );
        Cursor cursor = db.rawQuery(select, null);
        if (cursor.moveToFirst()) {
            int i = 0;
            do {
                i++;
            }while(cursor.moveToNext());
            data.res = new String[i];
            data.codes = new String[i];
            i = 0;
            if (cursor.moveToFirst())
                do {
                    data.codes[i] = cursor.getString(0);
                    data.res[i] = data.codes[i] + " " + cursor.getString(1);
                    i++;
                } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return data;
    }

    @Override
    public void onSaveItem(int number, Calendar title) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    class ComboData {
        CharSequence[] res = new CharSequence[0];
        CharSequence[] codes = new CharSequence[0];
    }
}