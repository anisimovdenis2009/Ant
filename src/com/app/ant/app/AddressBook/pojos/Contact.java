package com.app.ant.app.AddressBook.pojos;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 24.10.11
 * Time: 12:28
 * To change this template use File | Settings | File Templates.
 */
public class Contact implements Parcelable {
    private String firstLastName;
    private String telephone;
    private String mobileTelephone;
    private String email;

    public Contact() {
    }

    // Parcelling part
    public Contact(Parcel in) {
        String[] data = new String[4];
        in.readStringArray(data);
        this.firstLastName = data[0];
        this.telephone = data[1];
        this.mobileTelephone = data[2];
        this.email = data[3];
    }

    public Contact(String firstLastName) {
        this.firstLastName = firstLastName;
    }

    public Contact(String firstLastName, String telephone) {
        this.firstLastName = firstLastName;
        this.telephone = telephone;
    }

    public Contact(String firstLastName, String telephone, String mobileTelephone) {
        this.firstLastName = firstLastName;
        this.telephone = telephone;
        this.mobileTelephone = mobileTelephone;
    }

    public Contact(String firstLastName, String telephone, String mobileTelephone, String email) {
        this.firstLastName = firstLastName;
        this.telephone = telephone;
        this.mobileTelephone = mobileTelephone;
        this.email = email;
    }

    public String getFirstLastName() {
        return firstLastName;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getMobileTelephone() {
        return mobileTelephone;
    }

    public void setFirstLastName(String firstLastName) {
        this.firstLastName = firstLastName;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setMobileTelephone(String mobileTelephone) {
        this.mobileTelephone = mobileTelephone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int describeContents() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeStringArray(new String[]{this.firstLastName,
                this.telephone,
                this.mobileTelephone,
                this.email});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }


        public Contact[] newArray(int size) {
            return new Contact[size];
        }

    };
}
