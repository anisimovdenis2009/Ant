package com.app.ant.app.AddressBook.gui.clientcard.lawcontacts.addcontact;

import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.clientcard.XMLLoader;
import com.app.ant.app.AddressBook.gui.clientcard.XMLModel;
import com.app.ant.app.AddressBook.pojos.InputItem;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 21.10.11
 * Time: 16:44
 * To change this template use File | Settings | File Templates.
 */
public class AddContactsModel implements XMLModel {
    private ArrayList<InputItem> items;


    public AddContactsModel() {
        items = new ArrayList<InputItem>();
        File file = new File(Common.ADD_CONTACT);
        if (file.exists())
            XMLLoader.load(items, file);
    }

    public ArrayList<InputItem> getItems() {
        return items;
    }

    public void setValue(int a, String value) {
        items.get(a).setValue(value);
    }

    @Override
    public ArrayList<String> getTitles() {
        return XMLLoader.getTitles(items);
    }
}
