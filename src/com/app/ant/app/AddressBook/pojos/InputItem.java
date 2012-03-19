package com.app.ant.app.AddressBook.pojos;

import android.os.Parcelable;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 04.10.11
 * Time: 16:37
 * To change this template use File | Settings | File Templates.
 */
public class InputItem{
    private String title;
    private String value;

    public InputItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
