package com.app.ant.app.AddressBook.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.app.ant.app.AddressBook.gui.creditask.CreditAskModel;
import com.app.ant.app.AddressBook.gui.creditask.CreditCardXMLLoader;
import com.app.ant.app.AddressBook.pojos.InputItem;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 05.10.11
 * Time: 9:40
 * To change this template use File | Settings | File Templates.
 */
public class CreditAskDataHelper {
    private static final String TABLE_NAME = "CREDIT_ASK";
    private SQLiteDatabase db;
    private SQLiteStatement insertStmt;
    private static String INSERT = "insert into " + TABLE_NAME + " (";

    public CreditAskDataHelper(Context context) {
        OpenHelper openHelper = new OpenHelper(context, TABLE_NAME, "Create  CREDIT_ASK ([PayerKey] text,[LawFrom] text);");
        db = openHelper.getWritableDatabase();
    }

    public long insert(CreditCardXMLLoader m) {
        String columnNames = "";
        String columnParams = "";
        int i = 0;
        for (String columns : m.getTitles()) {
            if (i == 0) {
                columnNames = columns;
                columnParams = ") values (?";
            }
            columnNames += ", " + columns;
            columnParams += ",?";
            i++;
        }
        INSERT = INSERT +  columnNames + columnParams + ");";
        insertStmt = db.compileStatement(INSERT);
        i = 0;
        for (InputItem columns : m.getItems()) {
            insertStmt.bindString(i, columns.getValue());
            i++;
        }
        long z = insertStmt.executeInsert();
        return z;
    }
}
