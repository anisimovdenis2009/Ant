package com.app.ant.app.AddressBook.gui.creditask;

import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.options.OptionsUtil;
import com.app.ant.app.AddressBook.pojos.InputItem;
import com.app.ant.app.AddressBook.util.DOMUtil;
import org.jdom.Element;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 21.11.11
 * Time: 11:13
 * To change this template use File | Settings | File Templates.
 */
public class CreditCardXMLLoader {

    private ArrayList<InputItem> items;

    public CreditCardXMLLoader() {
        items = new ArrayList<InputItem>();
        OptionsUtil.loadAsync();
        Element creditCard = DOMUtil.getRootElement(Common.CREDIT_CARD_OPTIONS);
        Element textView = creditCard.getChild("EditView");
        for (Object item : textView.getChildren()) {
            items.add(new InputItem(((Element) item).getText()));
        }

    }

    public ArrayList<InputItem> getItems() {
        return items;
    }

    public ArrayList<String> getTitles() {
        ArrayList<String> strings = new ArrayList<String>();
        for(InputItem i:items){
           strings.add(i.getTitle());
        }
        return strings;
    }

    public ArrayList<String> getValues() {
        ArrayList<String> strings = new ArrayList<String>();
        for(InputItem i:items){
           strings.add(i.getValue());
        }
        return strings;
    }
}
