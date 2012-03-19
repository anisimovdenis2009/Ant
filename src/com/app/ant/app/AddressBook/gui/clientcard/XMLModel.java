package com.app.ant.app.AddressBook.gui.clientcard;

import com.app.ant.app.AddressBook.pojos.InputItem;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 21.10.11
 * Time: 18:03
 * To change this template use File | Settings | File Templates.
 */
public interface XMLModel {

    public ArrayList<InputItem> getItems();

    public void setValue(int a, String value);

    public ArrayList<String> getTitles();
}
