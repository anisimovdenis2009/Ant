package com.app.ant.app.AddressBook.gui.clientcard;

import com.app.ant.app.AddressBook.options.OptionsUtil;
import com.app.ant.app.AddressBook.pojos.InputItem;
import com.app.ant.app.AddressBook.util.DOMUtil;
import org.jdom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 21.10.11
 * Time: 16:46
 * To change this template use File | Settings | File Templates.
 */
public class XMLLoader {
    public static void load(Collection<InputItem> items, String path) {
        load(items, new File(path));
    }

    public static void load(Collection<InputItem> items, File file) {
        OptionsUtil.loadAsync();
        Element creditCard = DOMUtil.getRootElement(file);
        Element textView = creditCard.getChild("EditView");
        for (Object item : textView.getChildren()) {
            items.add(new InputItem(((Element) item).getText()));
        }
    }

    public static ArrayList<String> getTitles(Collection<InputItem> items) {
        ArrayList<String> strings = new ArrayList<String>();
        for (InputItem i : items) {
            strings.add(i.getTitle());
        }
        return strings;
    }

    public static ArrayList<String> getValues(Collection<InputItem> items) {
        ArrayList<String> strings = new ArrayList<String>();
        for (InputItem i : items) {
            strings.add(i.getValue());
        }
        return strings;
    }
}
