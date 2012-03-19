package com.app.ant.app.AddressBook.xmlfeatures;

import com.app.ant.app.AddressBook.gui.BaseModel;
import com.app.ant.app.AddressBook.gui.DataModel;
import com.app.ant.app.AddressBook.gui.creditask.CreditAskModel;
import org.jdom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 22.11.11
 * Time: 15:24
 * To change this template use File | Settings | File Templates.
 */
public class CreditAskXMLCreator extends BaseXMLCreator {
    public static final String CREDIT_REQUEST = "CreditRequest";

    private CreditAskModel m;

    private Element f_WishDelay;
    private Element comment;
    private Element f_CalcLimit;
    private Element f_PayerCode;
    private Element f_PayerName;
    private Element f_DelName;
    private Element f_MidleValue;
    private Element f_SalesPoint;
    private Element f_ShipDay;
    private Element currentLimit;
    private Element f_RequestLimit;
    private Element currentDelay;
    private Element f_Cash;
    private Element f_LegalForm;
    private Element inn;

    public CreditAskXMLCreator(BaseModel model) {
        super(model, CREDIT_REQUEST);
        this.m = (CreditAskModel) model;
        init();
        populate();
    }


    protected void init() {
        f_WishDelay = new Element("f_WishDelay");
        comment = new Element("Comment");
        f_CalcLimit = new Element("f_CalcLimit");
        f_PayerCode = new Element("f_PayerCode");
        f_PayerName = new Element("f_PayerName");
        f_DelName = new Element("f_DelName");
        f_MidleValue = new Element("f_MidleValue");
        f_SalesPoint = new Element("f_SalesPoint");
        f_ShipDay = new Element("f_ShipDay");
        currentLimit = new Element("CurrentLimit");
        f_RequestLimit = new Element("f_RequestLimit");
        currentDelay = new Element("CurrentDelay");
        f_Cash = new Element("f_Cash");
        f_LegalForm = new Element("f_LegalForm");
        inn = new Element("inn");
    }


    protected void populate() {

        String ayerCode = m.getCustomerCode();
        if (ayerCode != null) {
            f_PayerCode.setText(ayerCode);
            root.addContent(f_PayerCode);
        }
        String egalForm = m.getLawForm();
        if (egalForm != null) {
            f_LegalForm.setText(egalForm);
            root.addContent(f_LegalForm);
        }
        String ayerName = m.getName();
        if (ayerName != null) {
            f_PayerName.setText(ayerName);
            root.addContent(f_PayerName);
        }
        String elName = m.getSalePointName();
        if (elName != null) {
            f_DelName.setText(elName);
            root.addContent(f_DelName);
        }
        String a = m.getInn();
        if (a != null) {
            inn.setText(a);
            root.addContent(inn);
        }
        String rentDelay = m.getCurrentDeferral();
        if (rentDelay != null) {
            currentDelay.setText(rentDelay);
            root.addContent(currentDelay);
        }
        String ash = String.valueOf(m.isBankExistence());
        if (ash != null) {
            f_Cash.setText(ash);
            root.addContent(f_Cash);
        }
        String idleValue = String.valueOf(m.getAverageBooking());
        if (idleValue != null) {
            f_MidleValue.setText(idleValue);
            root.addContent(f_MidleValue);
        }
        String alesPoint = String.valueOf(m.getSalePointsQuantity());
        if (alesPoint != null) {
            f_SalesPoint.setText(alesPoint);
            root.addContent(f_SalesPoint);
        }
        String ishDelay = m.getPlanDaysDeferral();
        if (ishDelay != null) {
            f_WishDelay.setText(ishDelay);
            root.addContent(f_WishDelay);
        }
        String hipDay = String.valueOf(m.getBetween());
        if (hipDay != null) {
            f_ShipDay.setText(hipDay);
            root.addContent(f_ShipDay);
        }
        String rentLimit = m.getCurrentCreditLimit();
        if (rentLimit != null) {
            currentLimit.setText(rentLimit);
            root.addContent(currentLimit);
        }
        String alcLimit = String.valueOf(m.getCalculatedCreditLimit());
        if (alcLimit != null) {
            f_CalcLimit.setText(alcLimit);
            root.addContent(f_CalcLimit);
        }
        String equestLimit = m.getRequiredCreditLimit();
        if (equestLimit != null) {
            f_RequestLimit.setText(equestLimit);
            root.addContent(f_RequestLimit);
        }
        String ment = m.getComment();
        if (ment != null) {
            comment.setText(ment);
            root.addContent(comment);
        }
    }


}
