package com.app.ant.app.AddressBook.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 31.10.11
 * Time: 14:33
 * To change this template use File | Settings | File Templates.
 */
public class ClientCard extends ContentProvider{
    @Override
    public boolean onCreate() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getType(Uri uri) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
