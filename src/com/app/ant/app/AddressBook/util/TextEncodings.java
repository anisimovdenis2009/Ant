package com.app.ant.app.AddressBook.util;

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 20.05.11
 * Time: 17:01
 * To change this template use File | settings | File Templates.
 */
public enum TextEncodings {
    ISO8859_1("ISO-8859-1", 1),
    UTF16("UTF-16", 2),
    UTF16BE("UTF-16BE", 2),
    UTF8("UTF-8", 1);

    private String charsetName;
    private int stopZeroCount;

    TextEncodings(String charsetName, int stopZeroOffset) {
        this.charsetName = charsetName;
        this.stopZeroCount = stopZeroOffset;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public int getStopZeroCount() {
        return stopZeroCount;
    }
}