package com.app.ant.app.AddressBook.options;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 27.06.11
 * Time: 18:01
 * To change this template use File | settings | File Templates.
 */
public class DefaultEncoding {
    private Map<String, String> DefEnc;

    public DefaultEncoding() {
        DefEnc = new LinkedHashMap<String, String>();
        DefEnc.put("AR", "Windows-1256");
        DefEnc.put("BG", "Windows-1252");
        DefEnc.put("CS", "Windows-1252");
        DefEnc.put("DA", "Windows-1250");
        DefEnc.put("DE", "Windows-1250");
        DefEnc.put("EL", "Windows-1253");
        DefEnc.put("EN", "Windows-1250");
        DefEnc.put("ES", "Windows-1250");
        DefEnc.put("FI", "Windows-1250");
        DefEnc.put("FR", "Windows-1257");
        DefEnc.put("HI", "MacDevanagari");
        DefEnc.put("HR", "Windows-1252");
        DefEnc.put("HU", "Windows-1252");
        DefEnc.put("IT", "Windows-1250");
        DefEnc.put("JA", "SHIFT-JIS");
        DefEnc.put("KO", "Windows-1257");
        DefEnc.put("LT", "Windows-1257");
        DefEnc.put("LV", "Windows-1257");
        DefEnc.put("NB", "Windows-1257");
        DefEnc.put("NL", "Windows-1250");
        DefEnc.put("PL", "Windows-1252");
        DefEnc.put("PT", "Windows-1250");
        DefEnc.put("RO", "Windows-1252");
        DefEnc.put("RU", "Windows-1251");
        DefEnc.put("SK", "Windows-1252");
        DefEnc.put("SL", "Windows-1252");
        DefEnc.put("SR", "Windows-1252");
        DefEnc.put("SV", "Windows-1257");
        DefEnc.put("TH", "Windows-874");
        DefEnc.put("TR", "Windows-1254");
        DefEnc.put("UK", "KOI8-U");
        DefEnc.put("VI", "Windows-1258");
        DefEnc.put("ZH", "GB2312");


    }

    public boolean ifHaveDefEnc(String local) {
        return DefEnc.containsKey(local);

    }

    public String getDefEnc(String def) {
        return DefEnc.get(def);  //To change body of created methods use File | settings | File Templates.
    }

}
