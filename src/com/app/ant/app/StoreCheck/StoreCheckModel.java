package com.app.ant.app.StoreCheck;

import android.database.Cursor;
import com.app.ant.app.BusinessLayer.Common;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.AntContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 14.03.12
 * Time: 17:51
 * To change this template use File | Settings | File Templates.
 */
public class StoreCheckModel {
    SimpleDateFormat sdf = new SimpleDateFormat(Common.DATE_FORMAT_NOW);
    SimpleDateFormat tdf = new SimpleDateFormat(Common.DATE_FORMAT_NOW_DATABASE);
    ArrayList<StoreCheckDocument> result = new ArrayList<StoreCheckDocument>();

    public StoreCheckModel() {
        String select = "SELECT distinct SpDocID, SpDate, SpDocCode FROM GsShelfParts\n" +
                "WHERE SpAddrID = " + AntContext.getInstance().getAddress().addrID;

        Cursor cursor = Db.getInstance().selectSQL(select);
        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                int spDocID = cursor.getInt(cursor.getColumnIndex("SpDocID"));
                String date = cursor.getString(cursor.getColumnIndex("SpDate"));
                Date common = null;
                try {
                    common = tdf.parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String code = cursor.getString(cursor.getColumnIndex("SpDocCode"));
                String showDate = "";
                if (common != null)
                    showDate = sdf.format(common);

                StoreCheckDocument document = new StoreCheckDocument(spDocID, common, showDate, code);
                result.add(document);
            }
            cursor.close();
        }
    }
}
