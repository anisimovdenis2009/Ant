package com.app.ant.app.AddressBook.gui.clientcard;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.format.Time;
import com.app.ant.R;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.Strings;
import com.app.ant.app.AddressBook.database.ClientCardDataHelper;
import com.app.ant.app.AddressBook.gui.BaseGUIActivity;
import com.app.ant.app.AddressBook.gui.clientcard.comments.CommentActivity;
import com.app.ant.app.AddressBook.gui.clientcard.deliveryaddress.DeliveryAddressController;
import com.app.ant.app.AddressBook.gui.clientcard.jdeoptions.JDEDataController;
import com.app.ant.app.AddressBook.gui.clientcard.jdeoptions.JDEDataModel;
import com.app.ant.app.AddressBook.gui.clientcard.lawaddress.LawAddressController;
import com.app.ant.app.AddressBook.gui.clientcard.lawcontacts.LawContactController;
import com.app.ant.app.AddressBook.gui.clientcard.pricesandlicenses.PriceAndLicenseController;
import com.app.ant.app.AddressBook.gui.clientcard.pricesandlicenses.PriceAndLicenseModel;
import com.app.ant.app.AddressBook.gui.components.*;
import com.app.ant.app.AddressBook.options.Options;
import com.app.ant.app.AddressBook.pojos.DeliveryAddress;
import com.app.ant.app.AddressBook.pojos.LawAddress;
import com.app.ant.app.AddressBook.util.GUIFactory;
import com.app.ant.app.AddressBook.util.IOUtil;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 27.09.11
 * Time: 14:28
 * To change this template use File | settings | File Templates.
 */
public class ClientCardController extends BaseGUIActivity implements ItemSaveListener, Preference.OnPreferenceClickListener {



    public static String gps;
    public static Location gpsLocation;
    


    private PreferenceScreen preferenceScreen;
    private MyPreference lawAddressScreen;
    private MyPreference jdeFeatures;
    private MyPreference contacts;
    private MyPreference comments;
    private MyPreference deliveryAddress;
    private MyPreference prices;
    private MyPreference gpsPreference;

    private MyCheckBoxPreference clientType;
    private MyCheckBoxPreference newClient;

    private MyEditPreference lowFrom;
    private MyEditPreference name;
    private MyEditPreference sailPointName;
    private MyEditPreference nationalNet;
    private MyEditPreference ogrn;
    private MyEditPreference inn;
    private MyEditPreference kpp;

    private MyListPreference pointType;
    private ClientCardModel m;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra(Common.CLIENT_CARD_EXTRA))
            m = (ClientCardModel) getIntent().getSerializableExtra(Common.CLIENT_CARD_EXTRA);
        else
            m = new ClientCardModel();

        if (getIntent().hasExtra(Common.CLIENT_CARD_EXTRA_CONTACTS)) {
            ArrayList<Parcelable> parcelableArrayListExtra = getIntent().getParcelableArrayListExtra(Common.CLIENT_CARD_EXTRA_CONTACTS);
            m.setContacts(parcelableArrayListExtra);
        }
        setType(0);
        setM(m);
        checkAndCreaterUuid();
        preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        init();
        populate();
        setPreferenceScreen(preferenceScreen);
    }

    protected void init() {
        int number = 0;
        clientType = new MyCheckBoxPreference(this);
        clientType.setTitle(Strings.POINT_TYPE);
        clientType.setNumber(number);
        clientType.setSummaryOn(Strings.LAW_OBJECT);
        clientType.setSummaryOff(Strings.FISICAL_OBJECT);
        clientType.setOnItemSaveListener(this);
        clientType.setChecked(m.isType(), false);


        lowFrom = new MyEditPreference(this, true);
        lowFrom.setTitle(Strings.LOW_FORM);
        lowFrom.setOnItemSaveListener(this);
        lowFrom.setNumber(++number);
        lowFrom.setOnItemSaveListener(this);
        lowFrom.setLayoutResource(R.layout.preference_red);
        lowFrom.setSummary(m.getLawForm());

        name = createEditPreference(++number, Strings.NAME_CLIENT_CARD, m.getName(), false);

        sailPointName = new MyEditPreference(this, false);
        sailPointName.setTitle(Strings.CLIENT_CARD_SALE_POINT_NAME);
        sailPointName.setNumber(++number);
        sailPointName.setOnItemSaveListener(this);
        sailPointName.setSummary(m.getSalePointName());


        nationalNet = new MyEditPreference(this, false);
        nationalNet.setTitle(Strings.CLIENT_CARD_NATIONAL_NET);
        nationalNet.setNumber(++number);
        nationalNet.setOnItemSaveListener(this);
        nationalNet.setSummary(m.getNationalNet());

        ogrn = new MyEditPreference(this);
        if (Options.isRussian)
            ogrn.setTitle(Strings.OGRN);
        else
            ogrn.setTitle(Strings.OGRN_BEL);
        ogrn.setNumber(++number);
        ogrn.setOnItemSaveListener(this);
        ogrn.setLayoutResource(R.layout.preference_red);
        ogrn.setSummary(m.getOgrn());

        inn = new MyEditPreference(this);
        if (Options.isRussian)
            inn.setTitle(Strings.INN);
        else
            inn.setTitle(Strings.INN_BEL);
        inn.setNumber(++number);
        inn.setOnItemSaveListener(this);
        inn.setLayoutResource(R.layout.preference_red);
        inn.setSummary(m.getInn());


        kpp = new MyEditPreference(this);
        kpp.setTitle(Strings.KPP);
        kpp.setNumber(++number);
        kpp.setOnItemSaveListener(this);
        kpp.setSummary(m.getKpp());

        newClient = new MyCheckBoxPreference(this);
        newClient.setLayoutResource(R.layout.preference);
        newClient.setTitle(Strings.IS_NEW_OBJECT);
        newClient.setSummaryOn(Strings.NEW);
        newClient.setSummaryOff(Strings.OLD);
        newClient.setNumber(++number);
        newClient.setOnItemSaveListener(this);
        newClient.setChecked(m.isNewPoint(), false);

        lawAddressScreen = new MyPreference(this);
        lawAddressScreen.setTitle(Strings.LAW_ADDRESS);
        lawAddressScreen.setSummary(Strings.CLIENT_CARD_NEW_CLIENT_ACCEPT);
        lawAddressScreen.setOnPreferenceClickListener(this);
        lawAddressScreen.setEnabled(m.isNewPoint());
        if (m.isNewPoint())
            lawAddressScreen.setLayoutResource(R.layout.preference);
        else
            lawAddressScreen.setLayoutResource(R.layout.preference_disabled);

        pointType = new MyListPreference(this);
        pointType.setTitle(Strings.POINT_TYPE);
        pointType.setNumber(++number);
        pointType.setOnItemSaveListener(this);
        pointType.setEntries(new String[]{Common.SHOP_CONST, Common.KIOSK_CONST, Common.MARKET_CONST,});
        pointType.setEntryValues(new String[]{"0", "1", "2"});
        pointType.setLayoutResource(R.layout.preference_red);
        String value = String.valueOf(m.getPointTypeInt());
        if (value != null) {
            pointType.setValue(value);
            pointType.setSummary(pointType.getEntry().toString());
        }

        deliveryAddress = new MyPreference(this);
        deliveryAddress.setTitle(Strings.DELIVERY_ADDRESS);
        deliveryAddress.setOnPreferenceClickListener(this);
        deliveryAddress.setLayoutResource(R.layout.preference_red);
        if (m.getDeliveryAddress() == null)
            deliveryAddress.setEnabled(false);

        contacts = new MyPreference(this);
        contacts.setTitle(Strings.CONTACTS);
        contacts.setOnPreferenceClickListener(this);

        prices = new MyPreference(this);
        prices.setTitle(Strings.CLIENT_TYPE_PRICE_CONDITIONS);
        prices.setOnPreferenceClickListener(this);

        jdeFeatures = new MyPreference(this);
        jdeFeatures.setTitle(Strings.CLIENT_CARD_ADDITIONAL_JDE);
        jdeFeatures.setOnPreferenceClickListener(this);
        jdeFeatures.setLayoutResource(R.layout.preference_red);

        comments = new MyPreference(this);
        comments.setTitle(Strings.COMMENT);
        comments.setOnPreferenceClickListener(this);

        gpsPreference = GUIFactory.myPreference(this, Strings.CLIENT_CARD_GPS_SAVE);


    }

    protected MyEditPreference createEditPreference(int number, String title, String value, boolean isMandatory) {
        MyEditPreference name = new MyEditPreference(this, false);
        name.setTitle(title);
        name.setNumber(number);
        name.setOnItemSaveListener(this);
        if (isMandatory)
            name.setLayoutResource(R.layout.preference_red);
        name.setSummary(value);
        if (value != null)
            name.setLayoutResource(R.layout.preference);
        return name;
    }

    protected void populate() {
        //First
        preferenceScreen.addPreference(clientType);
        preferenceScreen.addPreference(lowFrom);
        preferenceScreen.addPreference(name);
        preferenceScreen.addPreference(sailPointName);
        preferenceScreen.addPreference(nationalNet);
        preferenceScreen.addPreference(ogrn);
        preferenceScreen.addPreference(inn);
        if (Options.isRussian)
            preferenceScreen.addPreference(kpp);
        preferenceScreen.addPreference(newClient);
        preferenceScreen.addPreference(lawAddressScreen);

        //Second
        preferenceScreen.addPreference(pointType);
        preferenceScreen.addPreference(deliveryAddress);
        preferenceScreen.addPreference(contacts);
        if (Options.isRussian)
            preferenceScreen.addPreference(prices);
        preferenceScreen.addPreference(jdeFeatures);
        preferenceScreen.addPreference(comments);
        if (Options.isGPS)
            preferenceScreen.addPreference(gpsPreference);

    }

    private String getValue(int number) {
        String value = "";
        switch (number) {
            case 0:
                value = String.valueOf(m.isType());
                break;
            case 1:
                value = m.getLawForm();
                break;
            case 2:
                value = m.getName();
                break;
            case 3:
                value = m.getSalePointName();
                break;
            case 4:
                value = m.getNationalNet();
                break;
            case 5:
                value = m.getOgrn();
                break;
            case 6:
                value = m.getInn();
                break;
            case 7:
                value = m.getKpp();
                break;
            case 8:
                value = String.valueOf(m.isNewPoint());
                break;
            case 9:
                value = m.getPointTypeString();
                break;
        }
        return value;
    }

    @Override
    public void onSaveItem
            (Preference
                     parent, int number, String
                    title) {
        parent.setLayoutResource(R.layout.preference);
        switch (number) {
            case 0:
                m.setType(Boolean.parseBoolean(title));
                break;
            case 1:
                m.setLawForm(title);
                break;
            case 2:
                m.setName(title);
                break;
            case 3:
                m.setSalePointName(title);
                break;
            case 4:
                m.setNationalNet(title);
                break;
            case 5:
                if (Options.isRussian) {
                    if (IOUtil.validateOgrn(m.isType(), this, parent, title))
                        m.setOgrn(title);
                } else if (IOUtil.validateBELInn(true, this, parent, title))
                    m.setOgrn(title);
                break;
            case 6:
                if (Options.isRussian) {
                    if (IOUtil.validateInn(m.isType(), this, parent, title))
                        m.setInn(title);
                } else if (IOUtil.validateBELInn(m.isType(), this, parent, title))
                    m.setInn(title);
                break;
            case 7:
                if (IOUtil.validateKpp(m.isType(), this, parent, title))
                    m.setKpp(title);
                break;
            case 8:
                setNewPoint(title);
                break;
            case 9: {
                m.setPointTypeString(title);
                m.setPointTypeInt(Integer.parseInt(((ListPreference) parent).getValue()));
                m.setDeliveryAddress(null);
                deliveryAddress.setEnabled(true);
            }
            break;

            default:
                break;
        }
    }

    private void setNewPoint(String title) {
        m.setNewPoint(Boolean.parseBoolean(title));
        lawAddressScreen.setEnabled(m.isNewPoint());
    }


    @Override
    public void onSaveItem(int number, Calendar title) {

    }

    void saveToDatabase() {
        ClientCardDataHelper dataHelper = new ClientCardDataHelper(this);
        dataHelper.insert(m);
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == lawAddressScreen) {
            Intent intent = new Intent(this, LawAddressController.class);
            if (m.getAddress() != null)
                intent.putExtra(Common.LAW_ADDRESS_EDIT_EXTRA, m.getAddress());
            startActivityForResult(intent, 1);
            return true;
        } else if (preference == contacts) {
            Intent intent = new Intent(this, LawContactController.class);
            if (m.getContacts() != null)
                intent.putExtra(Common.CONTACT_EXTRA, m.getContacts());
            startActivityForResult(intent, 2);
            return true;
        } else if (preference == deliveryAddress) {
            Intent intent = new Intent(this, DeliveryAddressController.class);
            intent.putExtra(Common.POINT_TYPE, m.getPointTypeInt());
            if (m.getDeliveryAddress() != null)
                intent.putExtra(Common.DELIVERY_ADDRESS_EXTRA, m.getDeliveryAddress());
            startActivityForResult(intent, 3);
            return true;
        } else if (preference == jdeFeatures) {
            Intent intent = new Intent(this, JDEDataController.class);
            if (m.jdeDataModel != null)
                intent.putExtra(Common.JDE_DATA_EXTRA, m.jdeDataModel);
            startActivityForResult(intent, 4);
            return true;
        } else if (preference == comments) {
            Intent intent = new Intent(this, CommentActivity.class);
            intent.putExtra(Common.COMMENT_EXTRA, m.getComment());
            startActivityForResult(intent, 5);
            return true;
        } else if (preference == prices) {
            Intent intent = new Intent(this, PriceAndLicenseController.class);
            PriceAndLicenseModel priceAndLicenseModel = m.getPriceAndLicenseModel();
            if (priceAndLicenseModel != null) {
                intent.putExtra(Common.PRICE_EXTRA, priceAndLicenseModel);
            }
            startActivityForResult(intent, 6);
        } else if (preference == gpsPreference) {
            final Location loc = getLocation();
            if (loc != null) {
                String locC = "Широта = " + loc.getLatitude() + " Долгота = " + loc.getLongitude();
                long time1 = loc.getTime();
                Time t = new Time();
                t.set(time1);
                Message.confirmationYesNo(this, "GPS координаты следующие: ", " " + locC + " \n Время сохранения " + t.hour + "часов " + t.minute + "минут \n Cохранить их?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gpsLocation = loc;
                    }
                }, true).show();
            } else {
                Message.error(this, "Не удалось снять данные с GPS датчика").show();
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case 1:
                    LawAddress res = (LawAddress) data.getSerializableExtra(Common.LAW_ADDRESS_RESULT_EXTRA);
                    m.setAddress(res);
                    break;
                case 2:
                    m.setContacts(data.getParcelableArrayListExtra(Common.CONTACT_EXTRA_RESULT));
                    break;
                case 3:
                    m.setDeliveryAddress((DeliveryAddress) data.getSerializableExtra(Common.DELIVERY_ADDRESS_EXTRA_RESULT));
                    break;
                case 4:
                    m.jdeDataModel = (JDEDataModel) data.getSerializableExtra(Common.JDE_DATA_EXTRA_RESULT);
                    jdeFeatures.setLayoutResource(R.layout.preference);
                    break;
                case 5:
                    m.setComment(data.getStringExtra(Common.COMMENT_EXTRA));
                    break;
                case 6:
                    m.setPriceAndLicenseModel((PriceAndLicenseModel) data.getSerializableExtra(Common.PRICE_EXTRA));
                    break;
            }
    }

    private Location getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = null;
        if (locationManager != null) {
            checkLM(locationManager);
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        return lastKnownLocation;
    }

    private void checkLM(LocationManager locationManager) {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent myIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            startActivity(myIntent);
        }
    }

    private void startSearching() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            checkLM(locationManager);
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    gpsLocation = location;
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
    }
}