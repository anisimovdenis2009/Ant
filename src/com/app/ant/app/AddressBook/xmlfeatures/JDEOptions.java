package com.app.ant.app.AddressBook.xmlfeatures;


import android.content.Context;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.components.Message;
import com.app.ant.app.AddressBook.util.DOMUtil;
import org.jdom.Element;

import java.io.File;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 26.10.11
 * Time: 16:58
 * To change this template use File | Settings | File Templates.
 */
public class JDEOptions {
    private static JDEOptions instance;
    public static final String RET_CAT_CODE = "RetCatCode";
    public static final String ITEM_D = "itemD";
    public static final String RET_CAT_CODES_CATEGORY = "RetCatCodesCategory";
    public static final String RET_DESCRIPTION = "RetDescription";

    Element root;
    //Element items;
    List items;

    public static JDEOptions getInstance(Context context) {
        if (instance == null) {
            instance = new JDEOptions(context);
        }
        return instance;
    }

    private JDEOptions(Context context) {
        File kk = new File(Common.KK);
        if (kk.exists()) {
            root = DOMUtil.getRootElement(kk);
            items = root.getChild("items").getChildren();
        } else Message.error(context,"Коды категорий не загружены").show();
    }

    public Map<String, String> getTypeArray(int i) {
        Map<String, String> result = null;
        boolean numberTrue = false;
        Element current = null;
        try {
            current = (Element) items.get(i);
            String rcc = getCodCat(current);
            //testing
            numberTrue = isRightNumber(i, rcc);
        } catch (IndexOutOfBoundsException e) {
            numberTrue = false;
        }
        //verific
        if (numberTrue) {
            result = getHashMap(current);
        } else {
            for (Object c : items) {
                String rcc = getCodCat((Element) c);
                if (isRightNumber(i, rcc)) {
                    result = getHashMap((Element) c);
                    break;
                }
            }
        }
        return result;
    }

    private String getCodCat(Element current) {
        return current.getChild(RET_CAT_CODE).getText();
    }

    private boolean isRightNumber(int i, String rcc) {
        boolean numberTrue;
        try {
            int val = Integer.parseInt(rcc);
            if (val == i) {
                numberTrue = true;
            } else {
                numberTrue = false;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            numberTrue = false;
        }
        return numberTrue;
    }

    private Map<String, String> getHashMap(Element current) {
        HashMap<String, String> result1 = new HashMap<String, String>();
        Element itemD = current.getChild(ITEM_D);
        List<Object> list = itemD.getChildren();

        for (Object detail : list) {
            String key = ((Element) detail).getChild(RET_CAT_CODES_CATEGORY).getText();
            String value = ((Element) detail).getChild(RET_DESCRIPTION).getText();
            result1.put(key, value);
        }
        Comparator<String> comparer = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        };
        /*if (sort) {
            Map<String, String> sorted = new TreeMap<String, String>(comparer);
            sorted.putAll(result1);
            return sorted;
        }*/
        return result1;
    }

    public static String[] getEnKeys(int i) {
        Set<String> strings = instance.getTypeArray(i).keySet();
        return strings.toArray(new String[0]);
    }

    public static String[] getEntries(int i) {
        return instance.getTypeArray(i).values().toArray(new String[0]);
    }


}
