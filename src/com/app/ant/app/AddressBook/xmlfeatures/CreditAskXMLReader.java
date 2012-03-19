package com.app.ant.app.AddressBook.xmlfeatures;

import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.creditask.CreditAskModel;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 12.12.11
 * Time: 10:34
 * To change this template use File | Settings | File Templates.
 */
public class CreditAskXMLReader extends XMLReader {
    CreditAskModel m;

    public CreditAskXMLReader(String path) {
        super(path);
        m = new CreditAskModel();
        m.setUuid(getTextFromElement(Common.ID));

        m.setCustomerCode(getTextFromElement("f_PayerCode"));
        m.setLawForm(getTextFromElement("f_LegalForm"));
        m.setName(getTextFromElement("f_PayerName"));
        m.setSalePointName(getTextFromElement("f_DelName"));
        m.setInn(getTextFromElement("inn"));
        m.setCurrentDeferral(getTextFromElement("CurrentDelay"));
        String f_cash = getTextFromElement("f_Cash");
        if (f_cash != null)
            m.setBankExistence(Boolean.valueOf(f_cash));
        String f_midleValue = getTextFromElement("f_MidleValue");
        if (f_midleValue != null)
            m.setAverageBooking(Integer.parseInt(f_midleValue));
        String f_salesPoint = getTextFromElement("f_SalesPoint");
        if (f_midleValue != null)
            m.setSalePointsQuantity(Integer.parseInt(f_salesPoint));
        m.setPlanDaysDeferral(getTextFromElement("f_WishDelay"));
        String f_shipDay = getTextFromElement("f_ShipDay");
        if (f_shipDay != null)
            m.setBetween(Integer.parseInt(f_shipDay));
        m.setCurrentCreditLimit(getTextFromElement("CurrentLimit"));
        String f_calcLimit = getTextFromElement("f_CalcLimit");
        if (f_calcLimit != null)
            m.setCalculatedCreditLimit(Long.parseLong(f_calcLimit));
        m.setRequiredCreditLimit(getTextFromElement("f_RequestLimit"));
    }

    public CreditAskModel getM() {
        return m;
    }
}
