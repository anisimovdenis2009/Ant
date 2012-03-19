package com.app.ant.app.AddressBook.gui.viewcards;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 30.11.11
 * Time: 15:53
 * To change this template use File | Settings | File Templates.
 */
public class OpenFileModel {
    private File path;


    public OpenFileModel(File path) {
        this.path = path;
        //init
    }

    public OpenFileModel(String file) {
        this.path = new File(file);

    }
}
