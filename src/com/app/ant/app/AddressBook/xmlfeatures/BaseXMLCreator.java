package com.app.ant.app.AddressBook.xmlfeatures;

import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.BaseModel;
import com.app.ant.app.AddressBook.gui.StartActivity;
import org.jdom.Document;
import org.jdom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 22.11.11
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
public class BaseXMLCreator {

    protected Document document;
    protected BaseModel m;

    protected Element root;
    protected Element id;
    protected Element mob_Id;
    protected Element mobVer;

    public BaseXMLCreator(BaseModel model, String rootName) {
        this.m = model;
        root = new Element(rootName);
        document = new Document(root);
        base(root);
    }

    protected void base(Element root) {
        id = new Element(Common.ID);
        mob_Id = new Element(Common.MOB_ID);
        mobVer = new Element(Common.MOB_VER);


        id.setText(m.getUuid());
        root.addContent(id);

        mob_Id.setText(StartActivity.getId());
        root.addContent(mob_Id);

        mobVer.setText(m.getMobVer());

        root.addContent(mobVer);
    }

/*    protected abstract void init();

    protected abstract void populate();*/

    public Document getDocument() {
        return document;
    }

    public Element getRoot() {
        return root;
    }
}
