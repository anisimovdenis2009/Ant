package com.app.ant.app.AddressBook.database;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 03.10.11
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.app.ant.app.AddressBook.gui.clientcard.ClientCardModel;

public class ClientCardDataHelper {
    private static final String TABLE_NAME = "CLIENT_CARD";
    public static final String CREATE_TABLE_CLIENT_CARD= "CREATE TABLE CLIENT_CARD (KPP TEXT, INN TEXT, OGRN TEXT, NATIONAL_NET TEXT, SALE_POINT_TYPE TEXT, NAME TEXT, LAW_FORM TEXT, TYPE TEXT, id INTEGER PRIMARY KEY);";

    private SQLiteDatabase db;
    private SQLiteStatement insertStmt;

    public ClientCardDataHelper(Context context) {
        OpenHelper openHelper = new OpenHelper(context, TABLE_NAME,CREATE_TABLE_CLIENT_CARD);
        db = openHelper.getWritableDatabase();
    }

    public long insert(ClientCardModel m) {
        String INSERT = "insert into "
                + TABLE_NAME + " (TYPE, LAW_FORM, NAME, SALE_POINT_TYPE, NATIONAL_NET, OGRN, INN, KPP) values (?,?,?,?,?,?,?,?)";
        insertStmt = db.compileStatement(INSERT);
        insertStmt.bindString(1, String.valueOf(m.isType()));
        insertStmt.bindString(2, m.getLawForm());
        insertStmt.bindString(3, m.getName());
        insertStmt.bindString(4, m.getSalePointName());
        insertStmt.bindString(5, m.getNationalNet());
        insertStmt.bindString(6, m.getOgrn());
        insertStmt.bindString(7, m.getInn());
        insertStmt.bindString(8, m.getKpp());
        long z = insertStmt.executeInsert();
        return z;
    }
}
