package com.app.ant.app.AddressBook.xmlfeatures;

import com.app.ant.app.AddressBook.util.DOMUtil;
import org.jdom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 25.11.11
 * Time: 17:38
 * To change this template use File | Settings | File Templates.
 */
public abstract class XMLReader {
    protected Element root;

    protected XMLReader(String path) {
        root = DOMUtil.getRootElement(path);

    }

    protected String getTextFromElement(String element) {
        String text = null;
        Element child = root.getChild(element);
        if (child != null)
            text = child.getText();
        return text;
    }
}
