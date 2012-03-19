package com.app.ant.app.AddressBook.xmlfeatures;

import android.os.Parcelable;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.clientcard.ClientCardModel;
import com.app.ant.app.AddressBook.gui.clientcard.jdeoptions.JDEDataModel;
import com.app.ant.app.AddressBook.pojos.Contact;
import com.app.ant.app.AddressBook.pojos.DeliveryAddress;
import com.app.ant.app.AddressBook.pojos.LawAddress;
import com.app.ant.app.AddressBook.pojos.delivery.Kiosk;
import com.app.ant.app.AddressBook.pojos.delivery.Market;
import com.app.ant.app.AddressBook.pojos.delivery.Shop;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 25.11.11
 * Time: 17:42
 * To change this template use File | Settings | File Templates.
 */
public class ClientCardXMLReader extends XMLReader {

    ClientCardModel m;

    public ClientCardXMLReader(String path) {
        super(path);
        m = new ClientCardModel();
        m.setUuid(getTextFromElement(Common.ID));
        parseBaseValues();
        parseFactAddress();
        parseContacts();
        parseJDEValues();

        m.setComment(getTextFromElement(Common.F_COMMENT));
    }

    private void parseJDEValues() {
        JDEDataModel m = new JDEDataModel();

        String outlet = getTextFromElement(Common.QNTOULET);
        m.setAutlet(outlet);
        String kko4 = getTextFromElement(Common.COVERING_TYPE_04);
        m.setKk04(kko4);
        String kko6 = getTextFromElement(Common.COVERING_TYPE_06);
        m.setKk06(kko6);
        String nestle = getTextFromElement(Common.CHANEL_TYPE_NESTLE);
        m.setKkNestle(nestle);
        String custpg = getTextFromElement(Common.CUST_TYPE_PG);
        m.setKkPG(custpg);
        String sectpg = getTextFromElement(Common.SECTOR_PG);
        m.setKkSectPG(sectpg);
        String belongcust = getTextFromElement(Common.BELONG_CUST);
        m.setKk22(belongcust);
        String remfil = getTextFromElement(Common.REMOTE_FILIAL);
        m.setKk24(remfil);
        String chanpg = getTextFromElement(Common.CHANEL_TYPE_PG);
        m.setKk25(chanpg);
        String chanpur = getTextFromElement(Common.CHANEL_TYPE_PURINA);
        m.setKk26(chanpur);
        String topcust = getTextFromElement(Common.TOP_CUST);
        m.setKk28(topcust);
        String supcode = getTextFromElement(Common.SUPPLIER_CODE);
        m.setShipingCode(supcode);
        String fchannelalk = getTextFromElement(Common.F_CHANEL_ALK_MIX);
        m.setKk14(fchannelalk);
        String kk08 = getTextFromElement(Common.KK_08);
        m.setKk08(kk08);
        String print = getTextFromElement(Common.F_PRINT_COMPLECT);
        m.setKk20(print);

        this.m.setJdeDataModel(m);
    }

    private void parseContacts() {
        ArrayList<Parcelable> contacts = new ArrayList<Parcelable>();

        String initCustomer = getTextFromElement(Common.INIT_CUSTOMER);
        String customerPhone = getTextFromElement(Common.CUSTOMER_PHONE);
        String customerGPhone = getTextFromElement(Common.CUSTOMER_G_PHONE);
        String email = getTextFromElement(Common.CUSTOMER_EMAIL);
        String[] cp = new String[0];
        if (customerPhone != null)
            cp = customerPhone.split("\\|");
        String[] cgp = new String[0];
        if (customerGPhone != null)
            cgp = customerGPhone.split("\\|");
        String[] eml = new String[0];
        if (email != null)
            eml = email.split("\\|");
        String[] ic = new String[0];
        if (initCustomer != null) {
            ic = initCustomer.split("\\|");
            int a = ic.length;
            int j = 0;
            if (ic != null && a > 0 && ic[0] != "")
                for (String s : ic) {
                    Contact c = new Contact(s);
                    if (cp.length != 0 && cp[0] != null && cp[0] != "") {
                        if ((cp.length >= j) && cp[j] != null && cp[j] != "")
                            c.setTelephone(cp[j]);
                    }
                    contacts.add(c);
                    j++;
                }
        }


        m.setContacts(contacts);
    }

    private void parseFactAddress() {
        m.setPointTypeString(getTextFromElement(Common.F_POINT_TYPE));
        String p = m.getPointTypeString();
        if (p.equals(Common.SHOP_CONST))
            m.setPointTypeInt(DeliveryAddress.SHOP);
        else if (p.equals(Common.KIOSK_CONST))
            m.setPointTypeInt(DeliveryAddress.KIOSK);
        else if (p.equals(Common.MARKET_CONST))
            m.setPointTypeInt(DeliveryAddress.MARKET);

        DeliveryAddress a = new DeliveryAddress(m.getPointTypeInt());
        a.setIndeks(getTextFromElement(Common.FACT_POST_INDEX));
        a.setArea(getTextFromElement(Common.FACT_REGION));
        a.setDepartment(getTextFromElement(Common.FACT_DISTRICT));
        a.setCity(getTextFromElement(Common.FACT_CITY));
        switch (m.getPointTypeInt()) {
            case DeliveryAddress.SHOP: {
                ((Shop) a.deliveryAddress).setStreet(getTextFromElement(Common.FACT_STREET));
                ((Shop) a.deliveryAddress).setHouse(getTextFromElement(Common.FACT_HOUSE));
                ((Shop) a.deliveryAddress).setBuilding(getTextFromElement(Common.FACT_CORP));
                ((Shop) a.deliveryAddress).setName(getTextFromElement(Common.SC_NAME));
                ((Shop) a.deliveryAddress).setFloor(getTextFromElement(Common.SC_FLOOR));
                ((Shop) a.deliveryAddress).setNumber(getTextFromElement(Common.SC_SHOP_NUM));
            }
            break;
            case DeliveryAddress.KIOSK: {
                ((Kiosk) a.deliveryAddress).setStreet(getTextFromElement(Common.FACT_STREET));
                ((Kiosk) a.deliveryAddress).setHouse(getTextFromElement(Common.FACT_HOUSE));
                ((Kiosk) a.deliveryAddress).setBuilding(getTextFromElement(Common.FACT_CORP));
                ((Kiosk) a.deliveryAddress).setOrientation(getTextFromElement(Common.EX_GUID));
            }
            break;
            case DeliveryAddress.MARKET: {
                ((Market) a.deliveryAddress).setMarket(getTextFromElement(Common.MARKET_NAME));
                ((Market) a.deliveryAddress).setStreet(getTextFromElement(Common.FACT_STREET));
                ((Market) a.deliveryAddress).setHouse(getTextFromElement(Common.FACT_HOUSE));
                ((Market) a.deliveryAddress).setBuilding(getTextFromElement(Common.PT_MARKET_NUM));
                ((Market) a.deliveryAddress).setOrientation(getTextFromElement(Common.EX_GUID));
                break;
            }
        }

        m.setDeliveryAddress(a);
    }

    private void parseBaseValues() {
        //is mandatory
        String textFromElement1 = getTextFromElement(Common.F_UFLAG);

        if (textFromElement1 != null)
            m.setType(Boolean.valueOf(textFromElement1));
        m.setLawForm(getTextFromElement(Common.F_FORM_OF_LAW));
        m.setName(getTextFromElement(Common.F_NAME));
        m.setSalePointName(getTextFromElement(Common.F_NAME_SALES_POINT));

        //is not mandatory may be new
        m.setNationalNet(getTextFromElement(Common.F_NET_ATTRIBUTE));
        m.setOgrn(getTextFromElement(Common.F_OGRN));
        m.setInn(getTextFromElement(Common.F_INN));
        m.setKpp(getTextFromElement(Common.F_KPP));

        String textFromElement = getTextFromElement(Common.F_IS_NEW);
        if (textFromElement != null) {
            boolean aBoolean = Boolean.valueOf(textFromElement);
            m.setNewPoint(aBoolean);
            if (aBoolean) {
                m.setAddress(getLawAddress());
            }
        }
    }

    private LawAddress getLawAddress() {
        LawAddress a = new LawAddress();
        a.setIndeks(getTextFromElement(Common.POST_INDEX));
        a.setArea(getTextFromElement(Common.REGION));
        a.setDepartment(getTextFromElement(Common.DISTRICT));
        a.setCity(getTextFromElement(Common.CITY));
        a.setStreet(getTextFromElement(Common.STREET));
        a.setHouse(getTextFromElement(Common.HOUSE));
        a.setBuilding(getTextFromElement(Common.CORPS));
        a.setAppartment(getTextFromElement(Common.FLAT));
        a.setOffice(getTextFromElement(Common.OFFICE));
        return a;
    }

    public ClientCardModel getM() {
        return m;
    }
}
