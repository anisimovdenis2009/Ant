package com.app.ant.app.AddressBook.gui.clientcard.jdeoptions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import com.app.ant.R;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.components.ItemSaveListener;
import com.app.ant.app.AddressBook.gui.components.MyEditPreference;
import com.app.ant.app.AddressBook.gui.components.MyListPreference;
import com.app.ant.app.AddressBook.options.Options;
import com.app.ant.app.AddressBook.util.GUIFactory;
import com.app.ant.app.AddressBook.xmlfeatures.JDEOptions;

import java.util.Calendar;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 08.11.11
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
public class JDEDataController extends PreferenceActivity implements ItemSaveListener {
    public static final String TOP_CLIENT = "Топ клиент";
    public static final String ADDRESS_CITY = "Филиал по адресной книге";
    public static final String PG_TYPE = "Тип клиента PG";

    private JDEDataModel m;
    private MyEditPreference autlet;
    private MyListPreference kk4;
    private MyListPreference kk6;
    private MyListPreference kkNestle;
    private MyListPreference kkProcter;
    private MyListPreference kkPG;
    private MyListPreference kk22;
    private MyListPreference kk24;
    private MyListPreference kk25;
    private MyListPreference kk26;
    private MyListPreference kk28;
    private MyEditPreference code;
    private MyListPreference kk14;
    private MyListPreference kk08;
    private MyListPreference kk20;

    private PreferenceScreen preferenceScreen;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra(Common.JDE_DATA_EXTRA))
            m = (JDEDataModel) intent.getSerializableExtra(Common.JDE_DATA_EXTRA);
        else
            m = new JDEDataModel();
        preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        init();
        populate();
        setPreferenceScreen(preferenceScreen);

    }

    private void init() {
        int number = 0;
        autlet = new MyEditPreference(this);
        autlet.setTitle("Количество аутлетов");
        autlet.setSummary(getValue(number));
        autlet.setNumber(number);
        autlet.setOnItemSaveListener(this);

        kk4 = createList(++number, 4, "kk 04 Тип покрытия");
        if (!Options.isRussian) {
/*            Map<String, String> typeArray = JDEOptions.getInstance().getTypeArray(444);
            kk4.setEntries(typeArray.values().toArray(new String[0]));
            kk4.setEntryValues(typeArray.keySet().toArray(new String[0]));*/
            kk4.setEntries(new String[]{"Другое", "Город - Преселлеры ", "Область - Преселлеры"});
            kk4.setEntryValues(new String[]{"8", "4", "6"});
        }

        kk6 = createList(++number, 6, "kk 06 Тип покрытия");
        if (!Options.isRussian) {
/*            Map<String, String> typeArray = JDEOptions.getInstance().getTypeArray(666);
            kk6.setEntries(typeArray.values().toArray(new String[0]));
            kk6.setEntryValues(typeArray.keySet().toArray(new String[0]));*/
            kk6.setEntries(new String[]{"Другое", "Преселлер A (корпоративный)", "Преселлер B (корпоративный)", "Сети"});
            kk6.setEntryValues(new String[]{"OT", "APS", "PS", "PC"});
        }

        kkNestle = createList(++number, 12, "Канал продаж Nestle");



          if (Options.isRussian) {
            kkProcter = createList(++number, 18, PG_TYPE);
        } else {
            kkProcter = GUIFactory.createList(this, ++number, PG_TYPE);
            kkProcter.setEntries(new String[]{"Аптеки",
                    "Гипермаркеты",
                    "Детские магазины",
                    "Дискаунтеры",
                    "Магазины Косметики и быт.химии",
                    "Мини Аптеки",
                    "Мини Детские магазины",
                    "Мини Косметики и быт.химии",
                    "Мини Парф.-косметич. магазины",
                    "Мини Хозяйственные магазины",
                    "Минимаркеты",
                    "Нет",
                    "Оптовики",
                    "Остальные клиенты",
                    "Открытые рынки",
                    "Парфюм.-косметические магазины",
                    "Стоматологические клиники",
                    "Супермаркеты",
                    "Хозяйственные магазины"});
            kkProcter.setEntryValues(new String[]{"VSP",
                    "VSH",
                    "VSB",
                    "VSD",
                    "VSS",
                    "MSP",
                    "MSB",
                    "MSS",
                    "MSC",
                    "VST",
                    "MSM",
                    " ",
                    "VNS",
                    "VNA",
                    "VNM",
                    "VSC",
                    "STM",
                    "VSU",
                    "VSO"});
            String value = getValue(number);
            if (value != null) {
                kkProcter.setValue(value);
                kkProcter.setSummary(kkProcter.getEntry().toString());
            }
        }
        kkProcter.setLayoutResource(R.layout.preference_red);

        kkPG = createList(++number, 19, "Сектор PG");
        kk22 = createList(++number, 22, "Принадлежание к ключевому клиенту");
        kk24 = createList(++number, 24, "Удалённость от филиала");
        kk25 = createList(++number, 25, "Канал продаж PG");
        kk26 = createList(++number, 26, "Канал продаж Purina");


        if (Options.isRussian) {
            kk28 = createList(++number, 28, TOP_CLIENT);
        } else {
            kk28 = GUIFactory.createList(this, ++number, TOP_CLIENT);
            kk28.setEntries(new String[]{"Не ТОП-клиент",
                    "Алми",
                    "Бигзз",
                    "Буслик",
                    "Гиппо",
                    "Евроопт",
                    "Корона",
                    "Линия",
                    "На недельку",
                    "Отличный",
                    "Прима-Сервис",
                    "Простор",
                    "Рублевский",
                    "Соседи"});
            kk28.setEntryValues(new String[]{" ",
                    "BAL",
                    "BGZ",
                    "BUS",
                    "BBV",
                    "BEU",
                    "BKR",
                    "BLN",
                    "BND",
                    "BOT",
                    "BPR",
                    "BPS",
                    "BRB",
                    "BSD"});
            String value = getValue(number);
            if (value != null) {
                kk28.setValue(value);
                kk28.setSummary(kk28.getEntry().toString());
            }
        }


        code = new MyEditPreference(this);
        code.setTitle("Код покуп/поставщика");
        code.setSummary(getValue(number));
        code.setNumber(++number);
        code.setOnItemSaveListener(this);

        kk14 = createList(++number, 14, "Тbg клиента/Канал продаж Алко,Микс");

        if (Options.isRussian)
            kk08 = createList(++number, 8, ADDRESS_CITY);
        else {
            kk08 = GUIFactory.createList(this, ++number, ADDRESS_CITY);
            kk08.setEntries(new String[]{"Минск", "Брест", "Витебск", "Гомель", "Гродно", "Могилев"});
            kk08.setEntryValues(new String[]{"21", "211", "212", "213", "214", "215"});
            String value = getValue(number);
            if (value != null) {
                kk08.setValue(value);
                kk08.setSummary(kk08.getEntry().toString());
            }
        }
        kk08.setLayoutResource(R.layout.preference_red);
        kk20 = createList(++number, 20, "Комплект документов");


        //initList(number);


    }

    public MyListPreference createList(int number, int kk, String title) {
        MyListPreference a;
        a = new MyListPreference(this);
        a.setTitle(title);
        a.setNumber(number);
        a.setOnItemSaveListener(this);
        a.setEntries(JDEOptions.getEntries(kk));
        a.setEntryValues(JDEOptions.getEnKeys(kk));
        String value = getValue(number);
        if (value != null) {
            a.setValue(value);
            a.setSummary(a.getEntry().toString());
        }
        return a;
    }


    private String getValue(int number) {
        switch (number) {
            case 0:
                return m.getAutlet();

            case 1:
                return m.getKk04();

            case 2:
                return m.getKk06();

            case 3:
                return m.getKkNestle();

            case 4:
                return m.getKkPG();

            case 5:
                return m.getKkSectPG();

            case 6:
                return m.getKk22();

            case 7:
                return m.getKk24();

            case 8:
                return m.getKk25();

            case 9:
                return m.getKk26();

            case 10:
                return m.getKk28();

            case 11:
                return m.getShipingCode();

            case 12:
                return m.getKk14();

            case 13:
                return m.getKk08();

            case 14:
                return m.getKk20();

            default:
                return null;
        }
    }


    private void populate() {
        preferenceScreen.addPreference(autlet);
        preferenceScreen.addPreference(kk4);
        preferenceScreen.addPreference(kk6);
        if (Options.isRussian)
            preferenceScreen.addPreference(kkNestle);
        preferenceScreen.addPreference(kkProcter);
        if (Options.isRussian)
            preferenceScreen.addPreference(kkPG);
        if (Options.isRussian)
            preferenceScreen.addPreference(kk22);
        if (Options.isRussian)
            preferenceScreen.addPreference(kk24);
        if (Options.isRussian)
            preferenceScreen.addPreference(kk25);
        if (Options.isRussian)
            preferenceScreen.addPreference(kk26);
        preferenceScreen.addPreference(kk28);
        if (Options.isRussian)
            preferenceScreen.addPreference(code);
        if (Options.isRussian)
            preferenceScreen.addPreference(kk14);
        preferenceScreen.addPreference(kk08);
        if (Options.isRussian)
            preferenceScreen.addPreference(kk20);

    }

    @Override
    public void onSaveItem(Preference parent, int number, String title) {
        switch (number) {
            case 0:
                m.setAutlet(title);
                break;
            case 1:
                m.setKk04(((ListPreference) parent).getValue());
                break;
            case 2:
                m.setKk06(((ListPreference) parent).getValue());
                break;
            case 3:
                m.setKkNestle(((ListPreference) parent).getValue());
                break;
            case 4:
                parent.setLayoutResource(R.layout.preference);
                m.setKkPG(((ListPreference) parent).getValue());
                break;
            case 5:
                m.setKkSectPG(((ListPreference) parent).getValue());
                break;
            case 6:
                m.setKk22(((ListPreference) parent).getValue());
                break;
            case 7:
                m.setKk24(((ListPreference) parent).getValue());
                break;
            case 8:
                m.setKk25(((ListPreference) parent).getValue());
                break;
            case 9:
                m.setKk26(((ListPreference) parent).getValue());
                break;
            case 10:
                m.setKk28(((ListPreference) parent).getValue());
                break;
            case 11:
                m.setShipingCode(title);
                break;
            case 12:
                m.setKk14(((ListPreference) parent).getValue());
                break;
            case 13:
                parent.setLayoutResource(R.layout.preference);
                m.setKk08(((ListPreference) parent).getValue());
                break;
            case 14:
                parent.setLayoutResource(R.layout.preference);
                m.setKk20(((ListPreference) parent).getValue());
                break;
        }
    }

    @Override
    public void onSaveItem(int number, Calendar title) {

    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Common.JDE_DATA_EXTRA_RESULT, m);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    class NameComparator implements Comparator {
        @Override
        public int compare(Object object1, Object object2) {
            String ob1 = (String) object1;
            String ob2 = (String) object2;
            return ob1.compareTo(ob2);
        }
    }
}