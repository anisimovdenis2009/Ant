package com.app.ant.app.AddressBook.gui;

import android.provider.ContactsContract;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: AVI
 * Date: 26.12.11
 * Time: 10:59
 * To change this template use File | Settings | File Templates.
 */
public class BaseModel implements DataModel,Serializable{
    private String uuid;
    private String mobVer;

    public String getUuid() {
        return uuid;
    }

    public String getMobVer() {
        return mobVer;
    }

    public void setMobVer(String mobVer) {
        this.mobVer = mobVer;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean verification() {
        return false;
    }
}
