package com.app.ant.app.AddressBook.gui.creditask;

import com.app.ant.app.AddressBook.gui.BaseModel;
import com.app.ant.app.AddressBook.gui.DataModel;

import java.io.Serializable;

public class CreditAskModel extends BaseModel {
    private String customerCode;
    private String lawForm = null;
    private String name = null;
    private String salePointName = null;
    private String inn = null;

    private String currentDeferral = null;
    private boolean bankExistence = false;
    private int averageBooking = 0;
    private int salePointsQuantity = 0;
    private String planDaysDeferral = null;
    private int between = 1;
    private String currentCreditLimit = null;
    private long calculatedCreditLimit = 0;
    private String requiredCreditLimit = null;

    private String comment;

    public String getCustomerCode() {
        return customerCode;
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

    public String getInn() {
        return inn;
    }

    public String getCurrentDeferral() {
        return currentDeferral;
    }

    public boolean isBankExistence() {
        return bankExistence;
    }


    public String getPlanDaysDeferral() {
        return planDaysDeferral;
    }


    public String getCurrentCreditLimit() {
        return currentCreditLimit;
    }



    public String getRequiredCreditLimit() {
        return requiredCreditLimit;
    }

    public String getComment() {
        return comment;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
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

    public void setInn(String inn) {
        this.inn = inn;
    }

    public void setCurrentDeferral(String currentDeferral) {
        this.currentDeferral = currentDeferral;
    }

    public void setBankExistence(boolean bankExistence) {
        this.bankExistence = bankExistence;
    }


    public void setPlanDaysDeferral(String planDaysDeferral) {
        this.planDaysDeferral = planDaysDeferral;
    }


    public void setCurrentCreditLimit(String currentCreditLimit) {
        this.currentCreditLimit = currentCreditLimit;
    }

    public void setRequiredCreditLimit(String requiredCreditLimit) {
        this.requiredCreditLimit = requiredCreditLimit;
    }

    public int getAverageBooking() {
        return averageBooking;
    }

    public int getSalePointsQuantity() {
        return salePointsQuantity;
    }

    public int getBetween() {
        return between;
    }

    public void setAverageBooking(int averageBooking) {
        this.averageBooking = averageBooking;
    }

    public void setSalePointsQuantity(int salePointsQuantity) {
        this.salePointsQuantity = salePointsQuantity;
    }

    public void setBetween(int between) {
        this.between = between;
    }

    public long getCalculatedCreditLimit() {
        return calculatedCreditLimit;
    }

    public void setCalculatedCreditLimit(long calculatedCreditLimit) {
        this.calculatedCreditLimit = calculatedCreditLimit;
    }

    @Override
    public boolean verification() {
        return true;
    }
}
