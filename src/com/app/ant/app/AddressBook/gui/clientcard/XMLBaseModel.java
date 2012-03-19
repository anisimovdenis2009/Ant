package com.app.ant.app.AddressBook.gui.clientcard;

import com.app.ant.app.AddressBook.pojos.InputItem;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 16.11.11
 * Time: 16:57
 * To change this template use File | Settings | File Templates.
 */
public class XMLBaseModel implements XMLModel {
    protected ArrayList<InputItem> items;

    public XMLBaseModel(String path) {
        items = new ArrayList<InputItem>();
        XMLLoader.load(items, path);
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
