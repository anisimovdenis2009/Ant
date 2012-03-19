package com.app.ant.app.AddressBook.util;

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 24.06.11
 * Time: 0:13
 * To change this template use File | settings | File Templates.
 */

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class DOMUtil {

    public static void spusk(Iterator itr, ArrayList<Integer> test) {
        while (itr.hasNext()) {
            Element current = (Element) itr.next();
            if ((current.getName().equals("")) || (current.getName() == "")) {
                test.add(Integer.parseInt(current.getValue()));
            }
            Iterator newit = (current.getChildren()).iterator();
            spusk(newit, test);
        }

    }

    public static Element getRootElement(String URI) {
        Document doc;
        Element root = null;
        SAXBuilder builder ;
        try {
            builder = new SAXBuilder();
            File file = new File(URI);
            doc = builder.build(file);
            root = doc.getRootElement();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    public static Element getRootElement(File file) {
        Document doc;
        Element root = null;
        SAXBuilder builder ;
        try {
            builder = new SAXBuilder();
            doc = builder.build(file);
            root = doc.getRootElement();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    public static void output(Document doc, String URI) {
        try {
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(doc, new FileWriter(URI));

        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
