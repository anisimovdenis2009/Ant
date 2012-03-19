package com.app.ant.app.AddressBook.gui.clientcard.deliveryaddress;

import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.clientcard.XMLLoader;
import com.app.ant.app.AddressBook.gui.clientcard.XMLModel;
import com.app.ant.app.AddressBook.pojos.DeliveryAddress;
import com.app.ant.app.AddressBook.pojos.InputItem;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 09.11.11
 * Time: 16:37
 * To change this template use File | Settings | File Templates.
 */
public class DeliveryAddressModel implements XMLModel {
    private int type;
    private ArrayList<InputItem> items;

    public DeliveryAddressModel(int type) {
        this.type = type;
        items = new ArrayList<InputItem>();
        switch (type) {
            case DeliveryAddress.SHOP:
                File file = new File(Common.SHOP);
                if (file.exists())
                    XMLLoader.load(items, file);
                break;
            case DeliveryAddress.KIOSK:
                File file1 = new File(Common.KIOSK);
                if (file1.exists())
                    XMLLoader.load(items, file1);
                break;
            case DeliveryAddress.MARKET:
                File file2 = new File(Common.MARKET);
                if (file2.exists())
                    XMLLoader.load(items, file2);
                break;

        }
    }

    @Override
    public ArrayList<InputItem> getItems() {
        return items;
    }

    @Override
    public void setValue(int a, String value) {
        items.get(a).setValue(value);
    }

    @Override
    public ArrayList<String> getTitles() {
        return XMLLoader.getTitles(items);
    }

    public int getType() {
        return type;
    }
}
