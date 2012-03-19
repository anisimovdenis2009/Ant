package com.app.ant.app.AddressBook.gui.clientcard.lawcontacts;

import android.os.Parcelable;
import com.app.ant.app.AddressBook.pojos.Contact;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 24.10.11
 * Time: 11:29
 * To change this template use File | Settings | File Templates.
 */
public class LawContactModel implements Serializable {
    private ArrayList<Contact> contacts;

    public LawContactModel() {
        contacts = new ArrayList<Contact>();
    }

    public LawContactModel(ArrayList<Parcelable> parcelableArrayListExtra) {
        contacts = new ArrayList<Contact>(parcelableArrayListExtra.size());
        for (Parcelable a : parcelableArrayListExtra) {
            contacts.add((Contact) a);
        }
    }

    public void addContact(Contact p) {
        contacts.add(p);
    }

    public ArrayList<Contact> getContacts() {
        return contacts;
    }

    /*public static LawContactModel getInstance() {
        if(instance == null){
            instance = new LawContactModel();
        }
        return instance;
    }*/
}
