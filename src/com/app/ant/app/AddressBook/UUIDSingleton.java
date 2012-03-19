package com.app.ant.app.AddressBook;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 29.11.11
 * Time: 16:14
 * To change this template use File | Settings | File Templates.
 */
public class UUIDSingleton {
    private static UUIDSingleton instance = null;

    private String id;

    private UUIDSingleton(String id) {
        this.id = id;
    }

    public static UUIDSingleton getInstance(String id) {
        if (instance == null)
            instance = new UUIDSingleton(id);
        return instance;
    }


    public String getId() {
        return id;
    }
}
