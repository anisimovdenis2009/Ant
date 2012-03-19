package com.app.ant.app.AddressBook.gui.clientcard.deliveryaddress;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.components.ItemSaveListener;
import com.app.ant.app.AddressBook.gui.components.MyEditPreference;
import com.app.ant.app.AddressBook.pojos.DeliveryAddress;
import com.app.ant.app.AddressBook.pojos.delivery.Kiosk;
import com.app.ant.app.AddressBook.pojos.delivery.Market;
import com.app.ant.app.AddressBook.pojos.delivery.Shop;
import com.app.ant.app.AddressBook.util.IOUtil;

import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 09.11.11
 * Time: 15:35
 * To change this template use File | Settings | File Templates.
 */
public class DeliveryAddressController extends PreferenceActivity implements ItemSaveListener {
    private PreferenceScreen preferenceScreen;
    private DeliveryAddressModel m;
    private DeliveryAddress data;

    private MyEditPreference indeks;
    private MyEditPreference area;
    private MyEditPreference departue;
    private MyEditPreference city;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        Intent intent = getIntent();

        if (intent.hasExtra(Common.POINT_TYPE)) {
            int type = intent.getIntExtra(Common.POINT_TYPE, 0);

            if (intent.hasExtra(Common.DELIVERY_ADDRESS_EXTRA))
                data = (DeliveryAddress) intent.getSerializableExtra(Common.DELIVERY_ADDRESS_EXTRA);
            else
                data = new DeliveryAddress(type);

            m = new DeliveryAddressModel(type);

            init();
        }
        data.setType(m.getType());
        setPreferenceScreen(preferenceScreen);

    }

    private void init() {
        preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        int i = 0;

        indeks = new MyEditPreference(this);
        indeks.setTitle("Индекс");
        indeks.setOnItemSaveListener(this);
        indeks.setSummary(getValue(i));
        indeks.setNumber(i++);


        area = new MyEditPreference(this,false);
        area.setTitle("Область");
        area.setOnItemSaveListener(this);
        area.setSummary(getValue(i));
        area.setNumber(i++);

        departue = new MyEditPreference(this,false);
        departue.setTitle("Район");
        departue.setOnItemSaveListener(this);
        departue.setSummary(getValue(i));
        departue.setNumber(i++);

        city = new MyEditPreference(this,false);
        city.setTitle("Город,Посёлок,Деревня,Хутор");
        city.setOnItemSaveListener(this);
        city.setSummary(getValue(i));
        city.setNumber(i++);

        preferenceScreen.addPreference(indeks);
        preferenceScreen.addPreference(area);
        preferenceScreen.addPreference(departue);
        preferenceScreen.addPreference(city);

        for (String title : m.getTitles()) {
            MyEditPreference t = new MyEditPreference(this);
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
        switch (m.getType()) {
            case DeliveryAddress.SHOP:
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
                        value = ((Shop) data.deliveryAddress).getStreet();
                        break;
                    case 5:
                        value = ((Shop) data.deliveryAddress).getHouse();
                        break;
                    case 6:
                        value = ((Shop) data.deliveryAddress).getBuilding();
                        break;
                    case 7:
                        value = ((Shop) data.deliveryAddress).getName();
                        break;
                    case 8:
                        value = ((Shop) data.deliveryAddress).getFloor();
                        break;
                    case 9:
                        value = ((Shop) data.deliveryAddress).getNumber();
                        break;
                }
                break;
            case DeliveryAddress.KIOSK:
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
                        value = ((Kiosk) data.deliveryAddress).getStreet();
                        break;
                    case 5:
                        value = ((Kiosk) data.deliveryAddress).getHouse();
                        break;
                    case 6:
                        value = ((Kiosk) data.deliveryAddress).getBuilding();
                        break;
                    case 7:
                        value = ((Kiosk) data.deliveryAddress).getOrientation();
                        break;
                }
                break;
            case DeliveryAddress.MARKET:
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
                        value = ((Market) data.deliveryAddress).getMarket();
                        break;
                    case 5:
                        value = ((Market) data.deliveryAddress).getStreet();
                        break;
                    case 6:
                        value = ((Market) data.deliveryAddress).getHouse();
                        break;
                    case 7:
                        value = ((Market) data.deliveryAddress).getBuilding();
                        break;
                    case 8:
                        value = ((Market) data.deliveryAddress).getOrientation();
                        break;
                }
                break;
        }
        return value;
    }

    @Override
    public void onSaveItem(Preference parent, int number, String title) {
        switch (m.getType()) {
            case DeliveryAddress.SHOP:
                switch (number) {
                    case 0:
                        if (IOUtil.validateIndeks(this, parent, title))
                            data.setIndeks(title);
                        break;
                    case 1:
                        data.setArea(title);
                        break;
                    case 2:
                        data.setDepartment(title);
                        break;
                    case 3:
                        data.setCity(title);
                        break;
                    case 4:
                        ((Shop) data.deliveryAddress).setStreet(title);
                        break;
                    case 5:
                        ((Shop) data.deliveryAddress).setHouse(title);
                        break;
                    case 6:
                        ((Shop) data.deliveryAddress).setBuilding(title);
                        break;
                    case 7:
                        ((Shop) data.deliveryAddress).setName(title);
                        break;
                    case 8:
                        ((Shop) data.deliveryAddress).setFloor(title);
                        break;
                    case 9:
                        ((Shop) data.deliveryAddress).setNumber(title);
                        break;
                }
                break;
            case DeliveryAddress.KIOSK:
                switch (number) {
                    case 0:
                        if (IOUtil.validateIndeks(this, parent, title))
                            data.setIndeks(title);
                        break;
                    case 1:
                        data.setArea(title);
                        break;
                    case 2:
                        data.setDepartment(title);
                        break;
                    case 3:
                        data.setCity(title);
                        break;
                    case 4:
                        ((Kiosk) data.deliveryAddress).setStreet(title);
                        break;
                    case 5:
                        ((Kiosk) data.deliveryAddress).setHouse(title);
                        break;
                    case 6:
                        ((Kiosk) data.deliveryAddress).setBuilding(title);
                        break;
                    case 7:
                        ((Kiosk) data.deliveryAddress).setOrientation(title);
                        break;
                }
                break;
            case DeliveryAddress.MARKET:
                switch (number) {
                    case 0:
                        if (IOUtil.validateIndeks(this, parent, title))
                            data.setIndeks(title);
                        break;
                    case 1:
                        data.setArea(title);
                        break;
                    case 2:
                        data.setDepartment(title);
                        break;
                    case 3:
                        data.setCity(title);
                        break;
                    case 4:
                        ((Market) data.deliveryAddress).setMarket(title);
                        break;
                    case 5:
                        ((Market) data.deliveryAddress).setStreet(title);
                        break;
                    case 6:
                        ((Market) data.deliveryAddress).setHouse(title);
                        break;
                    case 7:
                        ((Market) data.deliveryAddress).setBuilding(title);
                        break;
                    case 8:
                        ((Market) data.deliveryAddress).setOrientation(title);
                        break;
                }
                break;
        }
    }


    @Override
    public void onSaveItem(int number, Calendar title) {

    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Common.DELIVERY_ADDRESS_EXTRA_RESULT, data);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}