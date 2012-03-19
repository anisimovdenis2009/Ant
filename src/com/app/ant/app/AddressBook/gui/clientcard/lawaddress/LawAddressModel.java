package com.app.ant.app.AddressBook.gui.clientcard.lawaddress;

import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.clientcard.XMLLoader;
import com.app.ant.app.AddressBook.gui.clientcard.XMLModel;
import com.app.ant.app.AddressBook.pojos.InputItem;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: vianisimov
 * Date: 20.10.11
 * Time: 12:03
 * To change this template use File | Settings | File Templates.
 */
public class LawAddressModel implements XMLModel {
    private ArrayList<InputItem> items;
    private String[] titles;

    public LawAddressModel() {
        items = new ArrayList<InputItem>();
        File lawAddressOptions = new File(Common.LAW_ADDRESS_OPTIONS);
        if (lawAddressOptions.exists())
            XMLLoader.load(items, lawAddressOptions);
    }

    public ArrayList<InputItem> getItems() {
        return items;
    }

    public void setValue(int a, String value) {
        items.get(a).setValue(value);
    }

    public ArrayList<String> getTitles() {
        return XMLLoader.getTitles(items);
    }
}
