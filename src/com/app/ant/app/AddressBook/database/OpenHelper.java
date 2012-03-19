package com.app.ant.app.AddressBook.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.app.ant.app.AddressBook.Common;

class OpenHelper extends SQLiteOpenHelper {
    private String tableName;
    private String tableSQL;

        OpenHelper(Context context,String tableName,String tableSQL) {
            super(context, Common.DATABASE_NAME, null, Common.DATABASE_VERSION);
            this.tableName = tableName;
            this.tableSQL = tableSQL;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(tableSQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
            onCreate(db);
        }
    }
