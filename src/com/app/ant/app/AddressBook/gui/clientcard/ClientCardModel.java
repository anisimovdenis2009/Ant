package com.app.ant.app.AddressBook.gui.clientcard;

import android.os.Parcelable;
import com.app.ant.app.AddressBook.gui.BaseModel;
import com.app.ant.app.AddressBook.gui.DataModel;
import com.app.ant.app.AddressBook.gui.clientcard.jdeoptions.JDEDataModel;
import com.app.ant.app.AddressBook.gui.clientcard.pricesandlicenses.PriceAndLicenseModel;
import com.app.ant.app.AddressBook.pojos.DeliveryAddress;
import com.app.ant.app.AddressBook.pojos.LawAddress;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 27.09.11
 * Time: 16:07
 * To change this template use File | settings | File Templates.
 */
public class ClientCardModel extends BaseModel {

    //Contact features
    private ArrayList<Parcelable> contacts = null;

    //First screen values
    private boolean type = false;
    private String lawForm = null;
    private String name = null;
    private String salePointName = null;
    private String nationalNet = null;
    private String ogrn = null;
    private String inn = null;
    private String kpp = null;
    private boolean newPoint = false;

    //Second screen
    private LawAddress address;

    //Third screen values
    private String pointTypeString = null;
    private int pointTypeInt = 0;

    //Delivery address need to be introduced - Fouth
    private DeliveryAddress deliveryAddress = null;

    //Prices
    private PriceAndLicenseModel priceAndLicenseModel = null;

    //Seventh screen
    JDEDataModel jdeDataModel;
    //EithScreen
    private String comment = null;

    public ClientCardModel() {

    }

    public boolean verification() {
        /*      if (lawForm == null)
       return false;
   if (name == null)
       return false;
   if (ogrn == null)
       return false;
   if (inn == null)
       return false;
   if (pointTypeString == null)
       return false;
   if (deliveryAddress == null)
       return false;
   if (jdeDataModel == null)
       return false;
   if (jdeDataModel.getKkPG() == null)
       return false;
   if (jdeDataModel.getKk08() == null)
       return false;*/

        /* if (type && kpp == null)
       return false;*/
        return true;
    }

    public void validation() {
        if (lawForm != null) {
            lawForm.toUpperCase();
        }
    }

    //Getters
    public boolean isType() {
        return type;
    }

    public String getLawForm() {
        return lawForm;
    }

    public String getName() {
        return name;
    }

    public String getSalePointName() {
        return salePointName;
    }

    public String getNationalNet() {
        return nationalNet;
    }

    public String getOgrn() {
        return ogrn;
    }

    public String getInn() {
        return inn;
    }

    public String getKpp() {
        return kpp;
    }

    public boolean isNewPoint() {
        return newPoint;
    }

    public String getPointTypeString() {
        return pointTypeString;
    }

    /*public Calendar getLicenseStart() {
        return licenseStart;
    }

    public String getPriseColumn() {
        return priseColumn;
    }

    public boolean isDiscount() {
        return discount;
    }

    public Calendar getLicenseEnd() {
        return licenseEnd;
    }

    public String getLicenseSerial() {
        return licenseSerial;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }*/

    //Setters
    public void setType(boolean type) {
        this.type = type;
    }

    public void setLawForm(String lawForm) {
        this.lawForm = lawForm;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSalePointName(String salePointName) {
        this.salePointName = salePointName;
    }

    public void setNationalNet(String nationalNet) {
        this.nationalNet = nationalNet;
    }

    public void setOgrn(String ogrn) {
        this.ogrn = ogrn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public void setNewPoint(boolean newPoint) {
        this.newPoint = newPoint;
    }

    public void setPointTypeString(String pointTypeString) {
        this.pointTypeString = pointTypeString;
    }

    /*public void setPriseColumn(String priseColumn) {
        this.priseColumn = priseColumn;
    }

    public void setDiscount(boolean discount) {
        this.discount = discount;
    }

    public void setLicenseStart(Calendar licenseStart) {
        this.licenseStart = licenseStart;
    }

    public void setLicenseEnd(Calendar licenseEnd) {
        this.licenseEnd = licenseEnd;
    }

    public void setLicenseSerial(String licenseSerial) {
        this.licenseSerial = licenseSerial;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }*/

    public void setPointTypeInt(int value) {
        this.pointTypeInt = value;
    }

    public int getPointTypeInt() {
        return pointTypeInt;
    }

    public void setAddress(LawAddress address) {
        this.address = address;
    }

    public LawAddress getAddress() {
        return address;
    }

    public ArrayList<Parcelable> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<Parcelable> contacts) {
        this.contacts = contacts;
    }

    public DeliveryAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(DeliveryAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /* public String getLicenseIssue() {
        return licenseIssue;
    }

    public void setLicenseIssue(String licenseIssue) {
        this.licenseIssue = licenseIssue;
    }*/

    public JDEDataModel getJdeDataModel() {
        return jdeDataModel;
    }

    public void setPriceAndLicenseModel(PriceAndLicenseModel priceAndLicenseModel) {
        this.priceAndLicenseModel = priceAndLicenseModel;
    }

    public PriceAndLicenseModel getPriceAndLicenseModel() {
        return priceAndLicenseModel;
    }

    public void setJdeDataModel(JDEDataModel jdeDataModel) {
        this.jdeDataModel = jdeDataModel;
    }


}
