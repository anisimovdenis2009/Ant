package com.app.ant.app.AddressBook.gui.clientcard.pricesandlicenses;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 28.11.11
 * Time: 13:50
 * To change this template use File | Settings | File Templates.
 */
public class PriceAndLicenseModel implements Serializable{
     //Sixth screen values
    private String priseColumn = null;
    private boolean discount = false;
    private Calendar licenseStart;
    private Calendar licenseEnd;
    private String licenseSerial = null;
    private String licenseNumber = null;
    private String licenseIssue = null;

    public String getPriseColumn() {
        return priseColumn;
    }

    public boolean isDiscount() {
        return discount;
    }

    public Calendar getLicenseStart() {
        return licenseStart;
    }

    public Calendar getLicenseEnd() {
        return licenseEnd;
    }

    public String getLicenseSerial() {
        return licenseSerial;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getLicenseIssue() {
        return licenseIssue;
    }

    public void setPriseColumn(String priseColumn) {
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
    }

    public void setLicenseIssue(String licenseIssue) {
        this.licenseIssue = licenseIssue;
    }
}
