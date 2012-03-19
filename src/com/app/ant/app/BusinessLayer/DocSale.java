package com.app.ant.app.BusinessLayer;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import com.app.ant.app.Activities.DocDebtNotificationForm;
import com.app.ant.app.Activities.DocSaleHeaderDialog.Price;
import com.app.ant.app.Activities.DocSaleSelectGroupDialog.ItemGroup;
import com.app.ant.app.BusinessLayer.Plans.PlanItem;
import com.app.ant.app.Controls.DataGrid;
import com.app.ant.app.Controls.DataGrid.ColumnInfo;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Settings;

import java.lang.reflect.Field;
import java.util.*;


public class DocSale {
    public static final long ZERO_DOC_ID = 0;

    public long docId;
    public DocSaleHeader mDocHeader;
    public DocSaleDetails mDocDetails;
    //boolean isNew = true;

    public static long DEFAULT_GROUP_ID = -100;

    //totals
    public double totalAll = 0;
    public double total1 = 0;
    public double total2 = 0;

    public double totalVat1 = 0;
    public double totalVat2 = 0;
    public double totalVat = 0;

    public double totalNoVat1 = 0;
    public double totalNoVat2 = 0;
    public double totalNoVatAll = 0;

    public long totalItems = 0;
    public double totalCases = 0;
    public double totalPalettes = 0;
    public long totalOrders1 = 0;
    public long totalOrders2 = 0;
    public long totalOrders = 0;
    public long totalRemnantEntered = 0;
    public double totalMSU = 0;

    public PlanItem tolalPlanDistr = null;
    public PlanItem tolalPlanPowerSKU = null;
    public long tolalFactDistr = 0;
    public long tolalFactPowerSKU = 0;

    public boolean isEditable = true;

    //----------------------------------------------------HEADER--------------------------------------------------------------	

    /**
     * ���������� � ������������ ����� ��������� "�����/�������"
     */
    public class DocSaleHeader implements Cloneable {

        public Calendar docDate;
        public Calendar createDate;
        public char docType;
        public char docState;
        public String comments1 = "";
        public String comments2 = "";
        public String specMarks1 = "";
        public String specMarks2 = "";
        public int respite;
        public Price price;
        public String docNumber = "";
        public long contactID = DocDebtNotificationForm.DEFAULT_CONTACT_ID;
        public long prevContactID;
        public long addrID;
        public boolean welcomeDiscount;
        public long paymentID;

        public char docTypePrev; //type of document before editing		
        public long defaultPriceId = 0;
        public long savedDocID = 0;

        public DocSaleHeader() {
            addrID = AntContext.getInstance().getAddrID();
            long clientID = AntContext.getInstance().getClientID();
            Cursor cursor = Db.getInstance().selectSQL(Q.DEFAULT_PRICE_ID_FOR_CLIENT + addrID);
            if (cursor != null) {
                cursor.moveToFirst();
                try {
                    if (!cursor.isNull(0))
                        defaultPriceId = cursor.getInt(0);
                } catch (Exception e) {
                    Log.v("KAPPUC", "ble9t");
                }
                cursor.close();
            }

            if (defaultPriceId == 0)
                defaultPriceId = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_SALER_DEFAULT_PRICE_ID);


        }

        public DocSaleHeader clone() throws CloneNotSupportedException {
            DocSaleHeader cloned = (DocSaleHeader) super.clone();
            cloned.docDate = (Calendar) docDate.clone();
            cloned.createDate = (Calendar) createDate.clone();
            cloned.price = price.clone();

            return cloned;
        }


        public long insertNewDoc(char docType) {
            this.docType = docType;
            this.docTypePrev = Document.DOC_TYPE_UNKNOWN;
            createDate = Calendar.getInstance();
            docDate = Calendar.getInstance();
            docDate.add(Calendar.DAY_OF_MONTH, Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_DOC_ADD_DAYS_TO_DELIVERY, 1));
            price = new Price(defaultPriceId);
            docState = Document.DOC_STATE_NEW;
            contactID = DocDebtNotificationForm.DEFAULT_CONTACT_ID;
            long clientID = AntContext.getInstance().getClientID();
            long salerId = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_SALER_ID, 0);
            long visitID = AntContext.getInstance().getVisit().getVisitID();

            long newDocId = Document.getNewCurDocID();
            String sqlCreateDate = Convert.getSqlDateTimeFromCalendar(createDate);
            String sqlDate = Convert.getSqlDateTimeFromCalendar(docDate);

            String sqlInsert = "INSERT into CurDocuments (DocID, ClientID, AddrID, DocType, CreateDate, DocDate, " +
                    "PriceID, State, Comments, Comments2, SpecMarks, SpecMarks2, SalerID, VisitID, ContactID)" +
                    " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            Object[] bindArgs = new Object[]{newDocId, clientID, addrID, docType, sqlCreateDate, sqlDate,
                    price.id, docState, comments1, comments2, specMarks1, specMarks2, salerId, visitID, contactID};
            Db.getInstance().execSQL(sqlInsert, bindArgs);

            return newDocId;
        }

        public long loadDoc(long docId) {
            String sql = " SELECT DocType, AddrID, DocDate, CreateDate, DocNumber, State, Respite, PriceID, Comments, Comments2, SpecMarks, SpecMarks2, ContactID, IsNeedWelc,  PayTermID" +
                    " FROM CurDocuments WHERE DocID= " + docId;
            Cursor cursor = Db.getInstance().selectSQL(sql);
            if (cursor != null && cursor.getCount() != 0) {
                cursor.moveToPosition(0);
                //docNumber = Document.removeDocColorFromDocNumber(cursor.getString(cursor.getColumnIndex("DocNumber")));
                docNumber = cursor.getString(cursor.getColumnIndex("DocNumber"));
                respite = cursor.getInt(cursor.getColumnIndex("Respite"));
                long priceId = cursor.getLong(cursor.getColumnIndex("PriceID"));
                price = new Price(priceId);

                docType = Convert.getDocTypeFromString(cursor.getString(cursor.getColumnIndex("DocType")));
                docState = Convert.getDocStateFromString(cursor.getString(cursor.getColumnIndex("State")));
                docDate = Convert.getDateFromString(cursor.getString(cursor.getColumnIndex("DocDate")));
                createDate = Convert.getDateFromString(cursor.getString(cursor.getColumnIndex("CreateDate")));

                comments1 = cursor.getString(cursor.getColumnIndex("Comments"));
                comments2 = cursor.getString(cursor.getColumnIndex("Comments2"));
                specMarks1 = cursor.getString(cursor.getColumnIndex("SpecMarks"));
                specMarks2 = cursor.getString(cursor.getColumnIndex("SpecMarks2"));

                contactID = cursor.getLong(cursor.getColumnIndex("ContactID"));
                addrID = cursor.getLong(cursor.getColumnIndex("AddrID"));

                String IsNeedWelc = cursor.getString(cursor.getColumnIndex("IsNeedWelc"));
                if (IsNeedWelc == "Y")
                    welcomeDiscount = true;
                else welcomeDiscount = false;

                paymentID = cursor.getInt(cursor.getColumnIndex("PayTermID"));

                docTypePrev = docType;
            }

            if (cursor != null)
                cursor.close();

            return docId;
        }


        public void updateDbHeaderField(String fieldName, Object fieldValue) {
            String sqlUpdate = "UPDATE CurDocuments SET " + fieldName + "=? WHERE DocID=?";
            Object[] bindArgs = new Object[]{fieldValue, docId};
            Db.getInstance().execSQL(sqlUpdate, bindArgs);
        }

        public void onDateChanged() {
            String sqlDate = Convert.getSqlDateTimeFromCalendar(docDate);
            updateDbHeaderField("DocDate", sqlDate);
        }

        public void updateCloseDate() {
            String sqlDate = Convert.getSqlDateTimeFromCalendar(Calendar.getInstance());
            updateDbHeaderField("CloseDate", sqlDate);
        }

        public void updateSalerId() {
            long salerId = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_SALER_ID);
            updateDbHeaderField("SalerID", salerId);
        }

        public void updateDocNumber() {
            updateDbHeaderField("DocNumber", docNumber);
        }

        public void onDocTypeChanged() {
            updateDbHeaderField("DocType", docType);
        }

        public void onSumChanged() {
            updateDbHeaderField("SumWOVAT", totalNoVatAll);
            updateDbHeaderField("SumVAT", totalVat);
            updateDbHeaderField("SumAll", totalAll);
        }

        public void onDocStateChanged() {
            updateDbHeaderField("State", docState);
        }

        public void onRespiteChanged() {
            updateDbHeaderField("Respite", respite);
        }

        public void onPriceIdChanged() {
            updateDbHeaderField("PriceID", price.id);
        }

        public void onComments1Changed() {
            updateDbHeaderField("Comments", comments1);
            updateDbHeaderField("SpecMarks", specMarks1);
        }

        public void onComments2Changed() {
            updateDbHeaderField("Comments2", comments2);
            updateDbHeaderField("SpecMarks2", specMarks2);
        }

        public void onContactIDChanged() {
            updateDbHeaderField("ContactID", contactID);
        }

        public void onAddrIDChanged() {
            updateDbHeaderField("AddrID", addrID);
        }


    }

    ;

    //-------------------------------------------DETAILS--------------------------------------------------------

    /**
     * ���������� � ��������� ����� ��������� "�����/�������"
     */
    public class DocSaleDetails {

        public static final String EXPR = "@";
        public Address address;
        public ItemGroup itemGroup;

        public boolean checkRowExist(long docId, int itemId) {
            String sql = "select COUNT(*) from CurDocDetails where ItemID = " + itemId + " and DocID = " + docId;
            long rowCount = Db.getInstance().getDataLongValue(sql, 0);
            return (rowCount != 0);
        }

        //-------------------------------------
        //public void copyRowIfNotExist(int itemId)
        public void copyRow(int itemId) {
            //long docId =  DocSale.this.docId;
            //boolean rowExist = checkRowExist(docId, itemId);

            //if(!rowExist)
            {
                //copy database record
                String fieldsToCopy = "OrdersI, OrdersII, DiscountI, DiscountII, Price, UnitFactor, Quantity, VATID, PerCase, PerPall";
                String sqlInsert = String.format("INSERT INTO CurDocDetails(DocID, ItemID, %s )", fieldsToCopy)
                        + String.format(" SELECT %d AS DocID, %s AS ItemID, %s FROM CurDocDetails", docId, itemId, fieldsToCopy)
                        + " WHERE DocID = ? AND ItemID = ?";

                Object[] bindArgs = new Object[]{ZERO_DOC_ID, itemId};
                Db.getInstance().execSQL(sqlInsert, bindArgs);
            }
        }
        //-------------------------------------
        /*public void onFieldChanged(int itemId, String dbField, Object value)
          {
              copyRowIfNotExist(itemId);
              
              //update value
              String sql = String.format( "UPDATE CurDocDetails SET %s=? WHERE DocID=? AND ItemID=?", dbField) ;
              Object[] bindArgs = new Object[] { value, docId, itemId };
              Db.getInstance().execSQL(sql, bindArgs);
          }*/

        //-------------------------------------
        public void updateRowInDb(DataGrid grid, int itemId) {
            //long docId =  DocSale.this.docId;
            boolean rowExist = checkRowExist(docId, itemId);

            Map<Integer, Object[]> editedMap = grid.getEditedValues();

            Object[] values = editedMap.get(itemId);
            if (values != null) {
                if (mDocHeader.docType != Document.DOC_TYPE_REMNANTS) {
                    ColumnInfo quantityIColumn = grid.getColumns().getColumnByDbField("OrdersI");
                    //ColumnInfo quantityIIColumn = grid.getColumns().getColumnByDbField("OrdersII");
                    Object qI = null;
                    Object qII = null;

                    if (quantityIColumn.isEditable())
                        qI = values[quantityIColumn.getEditableIndex()];

                    /* if (quantityIIColumn.isEditable())
                  qII = values[quantityIIColumn.getEditableIndex()];*/

                    int quantityI = Convert.toInt(qI, 0);
                    int quantityII = Convert.toInt(qII, 0);

                    if (quantityI == 0 && quantityII == 0 && !rowExist)
                        return;
                }

                if (!rowExist)
                    copyRow(itemId);

                //insert editable values to database
                int nColumns = grid.getColumns().getLength();
                int nEditableColumns = grid.getColumns().getNumEditableColumns();
                int nValue = 0;

                String sql = "UPDATE CurDocDetails SET ";
                Object[] sqlValues = new Object[nEditableColumns + 1];

                for (int i = 0; i < nColumns; i++) {
                    ColumnInfo column = grid.getColumns().getColumn(i);
                    if (column.isEditable()) {
                        if (nValue != 0)
                            sql = sql + ",";
                        sql = sql + String.format("%s=?", column.getDbField());
                        sqlValues[nValue] = values[column.getEditableIndex()];
                        nValue++;
                    }
                }
                String price = "";
                Cursor cursor = grid.getmCursor();
                if (cursor.move(grid.getSelectedRow()))
                    price = String.valueOf(cursor.getFloat(cursor.getColumnIndex("Price")));

                sqlValues[nEditableColumns] = price;
                sql = sql + String.format(",Price=? WHERE DocID=%d AND ItemID=%d", docId, itemId);
                Log.v("SAVE", sql);
                Db.getInstance().execSQL(sql, sqlValues);
            }
        }

        public Cursor getItemsCursor(String sortColumn, String sortOrder, ItemGroup itemGroup, boolean filterQuantity, boolean filterStock, boolean upper, String docGridCondition) {
            return getItemsCursor(sortColumn, sortOrder, itemGroup, filterQuantity, filterStock, upper, docGridCondition, false, null);
        }

        public Cursor getItemsCursor(String sortColumn, String sortOrder, ItemGroup itemGroup, boolean filterQuantity, boolean filterStock, boolean upper, String docGridCondition, boolean booking) {
            return getItemsCursor(sortColumn, sortOrder, itemGroup, filterQuantity, filterStock, upper, docGridCondition, false, null);
        }

        //-------------------------------------
        public Cursor getItemsCursor(String sortColumn, String sortOrder, ItemGroup itemGroup, boolean filterQuantity, boolean filterStock, boolean upper, String docGridCondition, boolean booking, String adjasent) {
            this.itemGroup = itemGroup;
            String where = "";
            String whereSub = "";
            String orderBy = " ORDER BY " + sortColumn + " " + sortOrder + ", t1.ItemTypeID ASC";

            // Apply filters
            if (filterStock && !filterQuantity)
                where += " AND ((coalesce(r.Rest,0) - coalesce(r.SaledQnt,0)) > 0 OR t2.OrdersI > 0 OR t2.OrdersII > 0)";
            if (filterQuantity)
                where += " AND ((t2.OrdersI > 0) OR (t2.OrdersII > 0))";

            whereSub = where;

            String itemGroupFilter = Q.getItemGroupFilter("t1", itemGroup);
            if (docGridCondition != null)
                itemGroupFilter = itemGroupFilter + docGridCondition;

            String ob[] = itemGroupFilter.split(EXPR);
            boolean dogStart = itemGroupFilter.startsWith(EXPR);
            boolean dogEnd = itemGroupFilter.endsWith(EXPR);
            if (ob != null) {
                int changes = ob.length;
                if (changes > 1) {
                    ArrayList<String> replacemets = new ArrayList<String>();
                    HashMap<String, String> rep = new HashMap<String, String>();
                    int i1;
                    if (dogEnd)
                        i1 = changes;
                    else
                        i1 = changes - 1;

                    for (int i = 0; i < i1; i = i + 2) {
                        int i2 = i + 1;
                        if (dogStart)
                            i2 = i;
                        replacemets.add(ob[i2]);
                        String[] split = ob[i2].split("\\.");
                        String reflectValue = getReflectValue(split);
                        rep.put(ob[i2], reflectValue);
                    }
                    for (String replace : replacemets) {
                        String s = EXPR + replace + EXPR;
                        itemGroupFilter = itemGroupFilter.replace(s, rep.get(replace));
                    }
                }
            }

            if (itemGroupFilter.length() > 0)
                where += " AND " + itemGroupFilter;

            // Construct and execute query
            // t1 should be assigned to table to which filters are applied
            // in first SELECT t1 is for current document id, in second SELECT table t1 is for zero document

            String selectWhat = " t1.ScreenName AS ScreenName, t1.ItemName AS ItemName, t1.VATID AS VATID, "
                    + " t2.OrdersI AS OrdersI, t2.OrdersII AS OrdersII, "
                    + " t2.DiscountI as DiscountI, t2.DiscountII as DiscountII, "
                    + " t2.CurRemnant AS CurRemnant, "
                    + " t2.Price as PriceI, t2.Price as PriceII,"
                    + " t2.Orders AS Orders, t2.ExpDate AS ExpDate, "
                    + " prices.Price AS Price, t1.MinPrice AS MinPrice,"
                    + " t1.UnitFactor AS UnitFactor, "
                    + " t1.Saved AS Saved, t1.MinOrderQNT AS MinOrderQNT, "
                    + " t1.DivOrderQNT AS DivOrderQNT, t1.Remnant AS Remnant, t1.BarCode AS BarCode, t1.PerCase AS PerCase, "
                    + " t1.PerPall AS PerPall, t1.PrevDate AS PrevDate, ai.PrevWeekQnt AS PrevQuantity,"
                    + " ai.PrevWeek2Qnt AS PrevQuantity2, ai.NextOrder AS NextOrder, ai.LastRemnant AS LastRemnant, "
                    + " t1.ItemAttrFilter AS ItemAttrFilter, ai.NextOrder AS PredictedOrder, "
                    + " t1.ProdID AS ProdID, t1.ADS AS ADS, t1.ItemTypeID AS ItemTypeID, coalesce(it.ItemTypeName,'NA') ItemType, "
                    + " t1.ImageIndex AS ImageIndex, t1.StyleID AS StyleID, t1.ItemTypeStyleID AS ItemTypeStyleID, "
                    + " t1.ChannelMask AS ChannelMask, t1.ChannelMask2 AS ChannelMask2, t1.GroupID AS GroupID, "
                    + " t1.SUF AS SUF, t1.FUF AS FUF, "
                    + " ai.SaledQntCurMonth as SaledQuantity, ai.PrevMonthQnt AS PrevMonthQnt, ai.PrevMonth2Qnt AS PrevMonth2Qnt, ai.SaledQntCurDay AS SaledQntCurDay,"
                    + " ai.ProductSaledQntCurMonth as ProductSaledQuantity, ai.ProductPrevMonthQnt AS ProductPrevMonthQnt, ai.ProductPrevMonth2Qnt AS ProductPrevMonth2Qnt, ai.ProductSaledQntCurDay AS ProductSaledQntCurDay";

            address = AntContext.getInstance().getAddress();
            /*String channelCount = String.format(" (SELECT count(ic.ItemID) " +
                   " FROM ItemChannels ic " +
                   " WHERE ic.ChannelID = %d AND t1.ItemID = ic.ItemID AND ic.TypeID = 1) AS ChannelCount", address.channelID);
            */
            String subItemsCount = " (SELECT count(si.ChildItemID) " +
                    " FROM SubItems si " +
                    "                         INNER JOIN CurDocDetails cd ON si.ChildItemID = cd.ItemID and cd.DocID = 0 " +
                    " WHERE t1.ItemID = si.MainItemID) AS CountSub ";

            String sql = "";
            String selectSortColumns = ", t1.SortID AS SortID, t1.SortID2 AS SortID2, 1 as SubSortID";
            String selectIDColumns = " t1.ItemID AS ItemID, t1.ItemID AS ParentID, ";
            String digit = (upper) ? ">" : "=";
            String s = (booking) ? "INNER JOIN" : "LEFT JOIN";
            sql = " SELECT " + selectIDColumns + address.channelID + " as AddrChannel, coalesce(R.Rest,0)-coalesce(R.SaledQnt,0) AS Quantity, "
                    + subItemsCount + /*", " + channelCount +*/ ", " + selectWhat + selectSortColumns +
                    ", G.StyleID ListStID, " + "(select GR2.StyleID from ItemGroups GR2, ItemChannels IC " +
                    "where IC.ItemID = t1.ItemID " +
                    "and IC.ItemGroupID = GR2.ItemGroupID " +
                    "and IC.ChannelID = (select DefaultValue from Settings " +
                    "where Property = 'item_channel_none')) ListingStID, BR.ItemGroupID BrandID " +
                    " FROM CurDocDetails t1 " +
                    "                         INNER JOIN Prices prices ON prices.ItemID = t1.ItemID AND prices.PriceID = " + mDocHeader.price.id +
                    " " + s + " CurDocDetails t2 ON t1.ItemID = t2.ItemID AND t2.DocID = " + docId +
                    "                         LEFT JOIN AddrItems ai ON t1.ItemID=ai.ItemID AND ai.AddrID = " + address.addrID +
                    "                         LEFT JOIN ItemTypes it ON t1.ItemTypeID = it.ItemTypeID " +
                    "                         LEFT JOIN Rest r ON t1.ItemID = r.ItemID " +
                    " INNER JOIN ItemChannels C ON C.ItemID = t1.ItemID and C.TypeID = 1 " +
                    " INNER JOIN ItemGroups G ON C.ItemGroupID = G.ItemGroupID and G.ItemGroupID != (select DefaultValue from Settings where Property = 'item_channel_none') " +
                    " LEFT JOIN ItemGroups BR ON BR.GroupType = (select DefaultValue from Settings where Property = 'item_group_brands') " +
                    "    and BR.ItemGroupID > 0 " +
                    "    and exists (select 1 from ItemAttributes IA " +
                    "                where IA.ItemGroupID = BR.ItemGroupID " +
                    "                and IA.ItemID = t1.ItemID) " +
                    " WHERE t1.DocID = 0";
            if (adjasent == null)
                sql += " AND coalesce(ai.SoldForPeriod, 0) " + digit + " 0 ";

            sql += " AND C.ChannelID = " + address.channelID + " " + where;
            if (adjasent != null)
                sql += "and t1.ItemID in ( " +
                        "select A.ItemID " +
                        "FROM Items A, Items I " +
                        "where A.AdjacentGroup = I.AdjacentGroup  " +
                        "and A.ItemID != I.ItemID " +
                        "and A.ProductID != 0  " +
                        "and A.AdjacentGroup IS NOT NULL " +
                        "and I.ItemID = " + adjasent + ") ";
            sql += orderBy;

            Log.v("KAPPUC", sql);
            Cursor cursor = Db.getInstance().selectSQL(sql);
            return cursor;
        }

        private String getReflectValue(String[] split) {
            String res = null;
            Field field = null;
            try {
                Class<? extends DocSaleDetails> aClass = getClass();
                field = aClass.getField(split[0]);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            Object a = null;
            try {
                a = field.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            Class type = field.getType();

            Field channelID = null;
            try {
                channelID = type.getField(split[1]);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            try {
                Object ob = channelID.get(a);
                res = ob.toString();
            } catch (IllegalAccessException e) {

                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return res;
        }

        //-------------------------------------
        public void calculateTotals(boolean calculatePlans) {
            //reset totals
            totalAll = 0;
            total1 = 0;
            total2 = 0;

            totalVat1 = 0;
            totalVat2 = 0;
            totalVat = 0;

            totalNoVat1 = 0;
            totalNoVat2 = 0;
            totalNoVatAll = 0;

            totalItems = 0;
            totalCases = 0;
            totalPalettes = 0;
            totalOrders1 = 0;
            totalOrders2 = 0;
            totalOrders = 0;
            totalRemnantEntered = 0;
            totalMSU = 0;

            double totalNoVatToAssignVat1 = 0;
            double totalNoVatToAssignVat2 = 0;
            double totalNoVatToAssignVatAll = 0;

            //read data from db
            {
                String sql = String.format(" SELECT curdets.OrdersI, curdets.OrdersII, curdets.DiscountI, curdets.DiscountII, curdets.Price, " +
                        " coalesce(basedets.VATID,0) AS VATID, basedets.PerPall, basedets.PerCase, basedets.SUF " +
                        " FROM CurDocDetails curdets " +
                        " LEFT JOIN CurDocDetails basedets ON basedets.ItemID = curdets.ItemID AND basedets.DocID=0 " +
                        " WHERE curdets.DocID=%d AND (curdets.OrdersI>0 OR curdets.OrdersII > 0)"
                        , docId);

                Cursor cursor = Db.getInstance().selectSQL(sql);

                if (cursor != null && cursor.getCount() > 0) {
                    //calculate new summaries
                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToPosition(i);
                        long orders1 = cursor.getLong(cursor.getColumnIndex("OrdersI"));
                        long orders2 = cursor.getLong(cursor.getColumnIndex("OrdersII"));
                        double discount1 = cursor.getDouble(cursor.getColumnIndex("DiscountI"));
                        double discount2 = cursor.getDouble(cursor.getColumnIndex("DiscountII"));
                        double price = cursor.getDouble(cursor.getColumnIndex("Price"));
                        double vatId = cursor.getDouble(cursor.getColumnIndex("VATID"));
                        int perPall = cursor.getInt(cursor.getColumnIndex("PerPall"));
                        int perCase = cursor.getInt(cursor.getColumnIndex("PerCase"));
                        double SUF = cursor.getDouble(cursor.getColumnIndex("SUF"));

                        //compute sums
                        double priceNoVat1 = Convert.roundUpMoney(price * ((100 - discount1) / 100));
                        double priceNoVat2 = Convert.roundUpMoney(price * ((100 - discount2) / 100));

                        double rowNoVat1 = priceNoVat1 * orders1;
                        double rowNoVat2 = priceNoVat2 * orders2;

                        totalNoVat1 += rowNoVat1;
                        totalNoVat2 += rowNoVat2;
                        totalNoVatAll += rowNoVat1 + rowNoVat2;

                        if (vatId != 0) {
                            totalNoVatToAssignVat1 += rowNoVat1;
                            totalNoVatToAssignVat2 += rowNoVat2;
                            totalNoVatToAssignVatAll += rowNoVat1 + rowNoVat2;
                        }

                        //compute other totals
                        if (perCase != 0)
                            totalCases += (double) (orders1 + orders2) / (double) perCase;
                        if (perPall != 0)
                            totalPalettes += (double) (orders1 + orders2) / (double) perPall;

                        totalOrders1 += orders1;
                        totalOrders2 += orders2;

                        totalMSU = totalMSU + ((double) (orders1 + orders2) * SUF) / 1000;
                    }

                    totalItems = cursor.getCount();
                    totalOrders = totalOrders1 + totalOrders2;

                    //sums
                    totalVat1 = Convert.roundUpMoney(totalNoVatToAssignVat1 * 0.2);
                    totalVat2 = Convert.roundUpMoney(totalNoVatToAssignVat2 * 0.2);
                    totalVat = Convert.roundUpMoney(totalNoVatToAssignVatAll * 0.2);

                    total1 = totalNoVat1 + totalVat1;
                    total2 = totalNoVat2 + totalVat2;
                    totalAll = totalNoVatAll + totalVat;
                }

                if (cursor != null)
                    cursor.close();
            }

            //remnants
            if (mDocHeader.docType == Document.DOC_TYPE_REMNANTS) {
                String sql = String.format("SELECT COUNT(*) FROM CurDocDetails WHERE DocID=%d AND CurRemnant>0 ", docId);
                totalRemnantEntered = Db.getInstance().getDataLongValue(sql, 0);
            }

            if (calculatePlans)
                calculatePlanTotals();
        }

        //-------------------------------------		
        public void calculatePlanTotals() {
            //������������ ����� �� ������
            if (tolalPlanDistr == null) {
                tolalPlanDistr = Plans.getPlanValues(mDocHeader.addrID, Plans.PLAN_TYPE_MONTH_DISTRIBUTION, false, Plans.SKU_UNIT_ID, true);
                String sqlGeneralDistr = Q.getPlanFact(Plans.PLAN_TYPE_DAY_DISTRIBUTION, true, false, 0);
                Long factGeneralDistr = Db.getInstance().getDataLongValue(sqlGeneralDistr, 0);
                tolalPlanDistr.fact += factGeneralDistr;
            }
            if (tolalPlanPowerSKU == null) {
                tolalPlanPowerSKU = Plans.getPlanValues(mDocHeader.addrID, Plans.PLAN_TYPE_MONTH_POWER_SKU, false, Plans.SKU_UNIT_ID, true);
                String sqlFactPowSKU = Q.getPlanFact(Plans.PLAN_TYPE_MONTH_POWER_SKU, true, false, 0);
                Long factPSKU = Db.getInstance().getDataLongValue(sqlFactPowSKU, 0);
                tolalPlanPowerSKU.fact += factPSKU;
            }
            //Log.d("DocSaleForm ", "DocSale::calculateTotals plans start");

            String sqlGeneralDistr = Q.getPlanFact(Plans.PLAN_TYPE_DAY_DISTRIBUTION, true, true, docId); //TODO maybe cache this string
            tolalFactDistr = Db.getInstance().getDataLongValue(sqlGeneralDistr, 0);

            String sqlFactPowerSKU = Q.getPlanFact(Plans.PLAN_TYPE_MONTH_POWER_SKU, true, true, docId); //TODO maybe cache this string
            tolalFactPowerSKU = Db.getInstance().getDataLongValue(sqlFactPowerSKU, 0);

            //Log.d("DocSaleForm ", "DocSale::calculateTotals plans end");			
        }

        //-------------------------------------
        public void fillOrdersFromPredictedOrder(DataGrid grid, Cursor cursor) {
            int itemIdIdx = cursor.getColumnIndex("ItemID");
            int predictOrderIdx = cursor.getColumnIndex("PredictedOrder");
            int ordersColumn = grid.getColumns().getColumnIndexByDbField("OrdersI");
            int quantityColumn = cursor.getColumnIndex("Quantity");

            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.moveToPosition(i)) {
                    int predictedOrder = cursor.isNull(predictOrderIdx) ? 0 : cursor.getInt(predictOrderIdx);
                    int quantity = cursor.getInt(quantityColumn);

                    predictedOrder = Math.min(quantity, predictedOrder);

                    if (predictedOrder > 0) {
                        grid.setCellValue(i, ordersColumn, (Integer) predictedOrder);

                        int itemId = cursor.getInt(itemIdIdx);
                        updateRowInDb(grid, itemId);
                    }
                }
            }

        }

        //-------------------------------------
        public void moveBetweenForms(boolean moveItoII, DataGrid grid) {
            try {
                Db.getInstance().beginTransaction();

                String fromOrder = moveItoII ? "OrdersI" : "OrdersII";
                String toOrder = moveItoII ? "OrdersII" : "OrdersI";

                String fromDiscount = moveItoII ? "DiscountI" : "DiscountII";
                String toDiscount = moveItoII ? "DiscountII" : "DiscountI";

                //move values in db
                {
                    String sql = String.format("UPDATE CurDocDetails SET %s=%s, %s=%s WHERE DocID=%d ", toOrder, fromOrder, toDiscount, fromDiscount, docId);
                    Db.getInstance().execSQL(sql);
                }

                //reset original columns
                {
                    String sql = String.format("UPDATE CurDocDetails SET %s=NULL, %s=NULL WHERE DocID=%d ", fromOrder, fromDiscount, docId);
                    Db.getInstance().execSQL(sql);
                }

                //
                //move values in memory data array
                //
                int fromOrderIdx = grid.getColumns().getColumnByDbField(fromOrder).getEditableIndex();
                int toOrderIdx = grid.getColumns().getColumnByDbField(toOrder).getEditableIndex();
                int fromDiscountIdx = grid.getColumns().getColumnByDbField(fromDiscount).getEditableIndex();
                int toDiscountIdx = grid.getColumns().getColumnByDbField(toDiscount).getEditableIndex();

                //iterate through all entries
                Map<Integer, Object[]> valueMap = grid.getEditedValues();
                Collection<Object[]> collection = valueMap.values();

                Iterator<Object[]> it = collection.iterator();
                while (it.hasNext()) {
                    Object[] values = it.next();

                    values[toOrderIdx] = values[fromOrderIdx];
                    values[toDiscountIdx] = values[fromDiscountIdx];
                    values[fromOrderIdx] = null;
                    values[fromDiscountIdx] = null;
                }

                Db.getInstance().commitTransaction();
            } catch (Exception ex) {
                ErrorHandler.CatchError("Exception in DocSale.moveBetweenForms", ex);
                throw new RuntimeException(ex);
            } finally {
                Db.getInstance().endTransaction();
            }
        }

        public void onPriceIdChanged() {
            String fieldsToCopy = " OrdersI, OrdersII, DiscountI, DiscountII, UnitFactor, Quantity, VATID, PerCase, PerPall ";
            String fieldsToCopyFrom = " dets.OrdersI, dets.OrdersII, dets.DiscountI, dets.DiscountII, dets.UnitFactor, dets.Quantity, dets.VATID, dets.PerCase, dets.PerPall ";

            String sql = String.format(" INSERT OR REPLACE INTO CurDocDetails(DocID, ItemID, Price, %s) " +
                    " SELECT dets.DocID, dets.ItemID, prices.Price, %s" +
                    " FROM CurDocDetails dets " +
                    " LEFT JOIN Prices prices ON prices.ItemID = dets.ItemID AND prices.PriceID = %d " +
                    " WHERE dets.DocID = %d ",
                    fieldsToCopy, fieldsToCopyFrom, mDocHeader.price.id, docId);

            Db.getInstance().execSQL(sql);
        }
    }

    ;

    //-------------------------------------------------DocSale -------------------------------------------------------------
    public DocSale(Bundle params) {
        mDocHeader = new DocSaleHeader();
        mDocDetails = new DocSaleDetails();

        if (params.containsKey(Document.PARAM_NAME_DOCID)) {
            long docId = params.getLong(Document.PARAM_NAME_DOCID);
            loadDocument(docId);
        } else {
            char docType = params.getChar(Document.PARAM_NAME_DOCTYPE, Document.DOC_TYPE_CLAIM);
            newDocument(docType);
        }

        isEditable = (mDocHeader.docState != Document.DOC_STATE_CLOSED && mDocHeader.docState != Document.DOC_STATE_SENT);

        mDocDetails.calculateTotals(true);
    }

    //--------------------------------------------------------------	
    public void newDocument(char docType) {
        //insert new document record to CurDocuments
        docId = mDocHeader.insertNewDoc(docType);
        //isNew = true;
    }

    //--------------------------------------------------------------	
    public void loadDocument(long docId) {
        this.docId = mDocHeader.loadDoc(docId);
        //isNew = false;
    }

    //--------------------------------------------------------------
    public void rollbackSaledQnt(long docId) {
        //descrease saled quantity
        String sql = "REPLACE INTO Rest (ItemID, Rest, SaledQnt) " +
                " SELECT r.ItemID, r.Rest, coalesce(r.SaledQnt,0) - SUM(coalesce(d.Orders,0)) " +
                " FROM Documents doc " +
                " INNER JOIN DocDetails d ON d.DocID=doc.DocID" +
                " INNER JOIN Rest r ON r.ItemID=d.ItemID " +
                " WHERE doc.ParentDocID=? AND State = ? " +
                " GROUP BY r.ItemID, r.Rest, r.SaledQnt ";

        Object[] bindArgs = new Object[]{docId, Document.DOC_STATE_FINISHED};

        Db.getInstance().execSQL(sql, bindArgs);
    }

    public static void rollbackAddressItemsSaledQnt(long docId, boolean singleDocument) {
        //decrease SaledQntCurDay in AddressItems
        ArrayList<String> excludeColumns = new ArrayList<String>();
        excludeColumns.add("SaledQntCurDay");
        String columns = Db.getInstance().getTableColumnNames("AddrItems", null, excludeColumns);
        String columnsAI = Db.getInstance().getTableColumnNames("AddrItems", "ai", excludeColumns);

        String sql = "REPLACE INTO AddrItems ( " + columns + ", SaledQntCurDay) " +
                " SELECT " + columnsAI + ", coalesce(ai.SaledQntCurDay,0) - " + (singleDocument ? "" : "SUM") + " (coalesce(d.Orders,0)) " +
                " FROM Documents doc " +
                " INNER JOIN DocDetails d ON d.DocID=doc.DocID " +
                " INNER JOIN AddrItems ai ON ai.ItemID=d.ItemID AND ai.AddrID=doc.AddrID " +
                (singleDocument ? " WHERE d.DocID=? " : " WHERE doc.ParentDocID=? AND State = ? ") +
                (singleDocument ? "" : " GROUP BY " + columnsAI + ", ai.SaledQntCurDay ");

        Object[] bindArgs = singleDocument ? new Object[]{docId} : new Object[]{docId, Document.DOC_STATE_FINISHED};
        Db.getInstance().execSQL(sql, bindArgs);
    }

    //--------------------------------------------------------------	
    public void changeSaledQnt(boolean increase, long docId) {
        String sign = increase == true ? "+" : "-";

        //�������� �������� SaledQnt � ������� Rest
        String sql = "REPLACE INTO Rest (ItemID, Rest, SaledQnt) " +
                " SELECT r.ItemID, r.Rest, coalesce(r.SaledQnt,0) " + sign + "(coalesce(d.OrdersI,0)+coalesce(d.OrdersII,0)) " +
                " FROM CurDocDetails d " +
                " INNER JOIN Rest r ON r.ItemID=d.ItemID " +
                " WHERE d.DocID=" + docId;

        Db.getInstance().execSQL(sql);
    }

    public void changeAddressItemsSaledQnt(boolean increase, long docId) {
        String sign = increase == true ? "+" : "-";

        ArrayList<String> excludeColumns = new ArrayList<String>();
        excludeColumns.add("SaledQntCurDay");
        String columns = Db.getInstance().getTableColumnNames("AddrItems", null, excludeColumns);
        String columnsAI = Db.getInstance().getTableColumnNames("AddrItems", "ai", excludeColumns);

        //�������� �������� SaledQntCurrentDay � ������� AddrItems
        String sql = "REPLACE INTO AddrItems ( " + columns + ", SaledQntCurDay) " +
                " SELECT " + columnsAI + ", coalesce(ai.SaledQntCurDay,0) " + sign + "(coalesce(d.OrdersI,0)+coalesce(d.OrdersII,0)) " +
                " FROM CurDocDetails d " +
                " INNER JOIN AddrItems ai ON ai.ItemID=d.ItemID AND ai.AddrID = " + mDocHeader.addrID +
                " WHERE d.DocID=" + docId;

        Db.getInstance().execSQL(sql);
    }

    //--------------------------------------------------------------	
    public boolean finishDocument() {
        boolean result = true;

        try {
            Db.getInstance().beginTransaction();

            //
            // recalculate totals just for the case
            //
            mDocDetails.calculateTotals(true);

            //
            // mark opened documents as "previous"
            //
            if (mDocHeader.docState != Document.DOC_STATE_NEW)    //document is not new therefore opened
            {
                //DO NOT CHANGE ORDER OF THESE QUERIES
                if (mDocHeader.docTypePrev == Document.DOC_TYPE_SALE)
                    rollbackSaledQnt(docId);                //descrease saled quantity

                if (mDocHeader.docTypePrev == Document.DOC_TYPE_SALE || mDocHeader.docTypePrev == Document.DOC_TYPE_CLAIM)
                    rollbackAddressItemsSaledQnt(docId, false);    //decrease SaledQntCurDay in AddressItems

                //use ParentDocID to select related documents
                {
                    String sql = "UPDATE Documents SET State = ? WHERE ParentDocID = ? AND State = ?";
                    Object[] bindArgs = new Object[]{Document.DOC_STATE_PREVIOUS, docId, Document.DOC_STATE_FINISHED};
                    Db.getInstance().execSQL(sql, bindArgs);
                }
            }

            //
            // generate doc number for new document
            //
            if (mDocHeader.docState == Document.DOC_STATE_NEW)
                mDocHeader.docNumber = Document.getDocNumber(mDocHeader.docType);

            String docNumber1 = Document.addColorToDocNumber(mDocHeader.docNumber, Document.DOC_COLOR_WHITE);
            String docNumber2 = Document.addColorToDocNumber(mDocHeader.docNumber, Document.DOC_COLOR_BLACK);

            //
            // Mark document as finished
            //

            mDocHeader.docState = Document.DOC_STATE_FINISHED;
            mDocHeader.onDocStateChanged();
            mDocHeader.updateCloseDate(); //set CloseDate to now
            mDocHeader.updateSalerId(); //set salerId to current saler
            mDocHeader.updateDocNumber();

            //
            // Copy cur doc header and curdetails to documents and details 
            //
            long newDocId1 = Document.getMinDocId() - 1;
            long newDocId2 = newDocId1 - 1;

            //CurDocuments -> Documents 
            String strInsert = "INSERT INTO documents(DocID, ParentDocID, DocType, DocNumber, ClientID, AddrID, DocDate, CreateDate, CloseDate, " +
                    "State, Respite, SumAll, SumWOVAT, SumVAT, BWG, Comments, SpecMarks, PriceID, SalerID, VisitID, ContactID," +
                    "PayTermID, IsNeedWelc)";

            String strSelect = " SELECT ? AS DocID, ? AS ParentDocID, DocType, ? AS DocNumber, ClientID, AddrID,  DocDate, CreateDate, CloseDate, State, Respite, " +
                    "? AS SumAll, ? AS SumWOVAT, ? AS SumVAT, ? AS BWG, " +
                    " %s, %s, PriceID, SalerID, VisitID, ContactID, PayTermID, IsNeedWelc FROM CurDocuments";

            String strSelectI = String.format(strSelect, "Comments", "SpecMarks");
            Object[] bindArgsI = new Object[]{newDocId1, docId, docNumber1, total1, totalNoVat1, totalVat1, Document.DOC_COLOR_WHITE};

            String strSelectII = String.format(strSelect, "Comments2", "SpecMarks2");
            Object[] bindArgsII = new Object[]{newDocId2, docId, docNumber2, total2, totalNoVat2, totalVat2, Document.DOC_COLOR_BLACK};

            String strWhere = " WHERE DocID = " + docId;

            String strInsertI = strInsert + strSelectI + strWhere;
            String strInsertII = strInsert + strSelectII + strWhere;

            //CurDocDetails -> Details
            if (mDocHeader.docType == Document.DOC_TYPE_CLAIM || mDocHeader.docType == Document.DOC_TYPE_SALE) {
                String strInsertDets = "INSERT into DocDetails (DocID, ItemID, Quantity, UnitFactor, Price, Orders, Discount)";

                String strSelectDetsI = String.format(" SELECT %d AS DocID, ItemID, Quantity, UnitFactor, Price, OrdersI, DiscountI from CurDocDetails", newDocId1);
                String strSelectDetsII = String.format(" SELECT %d AS DocID, ItemID, Quantity, UnitFactor, Price, OrdersII, DiscountII from CurDocDetails", newDocId2);

                String strWhereDetsI = String.format(" WHERE DocID = %d AND OrdersI > 0", docId);
                String strWhereDetsII = String.format(" WHERE DocID = %d AND OrdersII > 0", docId);

                String strInsertDetsI = strInsertDets + strSelectDetsI + strWhereDetsI;
                String strInsertDetsII = strInsertDets + strSelectDetsII + strWhereDetsII;

                mDocHeader.savedDocID = 0; //savedDocID should contain any of docIds, just to have a hook to access second document. it is required for printing

                //execute statements
                if (totalOrders1 > 0) {
                    Db.getInstance().execSQL(strInsertI, bindArgsI);
                    Db.getInstance().execSQL(strInsertDetsI);
                    mDocHeader.savedDocID = newDocId1;
                }

                if (totalOrders2 > 0) {
                    Db.getInstance().execSQL(strInsertII, bindArgsII);
                    Db.getInstance().execSQL(strInsertDetsII);
                    mDocHeader.savedDocID = newDocId2;
                }

                if ((totalOrders1 > 0 || totalOrders2 > 0)) {
                    if (mDocHeader.docType == Document.DOC_TYPE_SALE)
                        changeSaledQnt(true, docId); //increase saled quantity

                    if (mDocHeader.docType == Document.DOC_TYPE_SALE || mDocHeader.docType == Document.DOC_TYPE_CLAIM)
                        changeAddressItemsSaledQnt(true, docId);
                }

                //
                // check document to be saved correctly
                //
                {
                    String sql = String.format(" SELECT sum(Orders) FROM DocDetails WHERE DocID IN(%d,%d)", newDocId1, newDocId2);
                    long totalOrdersCheck = Db.getInstance().getDataLongValue(sql, 0);

                    if (totalOrdersCheck != totalOrders)
                        result = false;
                }
            } else if (mDocHeader.docType == Document.DOC_TYPE_REMNANTS) {
                if (totalRemnantEntered > 0) {
                    String strInsertDets = "INSERT into DocDetails (DocID, ItemID, Remnant, ExpDate)";
                    String strSelectDets = String.format(" SELECT %d AS DocID, ItemID, CurRemnant, ExpDate from CurDocDetails", newDocId1);
                    String strWhereDets = String.format(" WHERE DocID = %d AND CurRemnant > 0", docId);
                    String remnantDets = strInsertDets + strSelectDets + strWhereDets;

                    Db.getInstance().execSQL(strInsertI, bindArgsI); //reuse from docSale
                    Db.getInstance().execSQL(remnantDets);
                }
            }

            isEditable = false;

            Db.getInstance().commitTransaction();

            //recalc plans
            Plans.recalcFacts();
        } catch (Exception ex) {
            ErrorHandler.CatchError("Exception in DocSale.finishDocument", ex);
            throw new RuntimeException(ex);
        } finally {
            Db.getInstance().endTransaction();
        }

        return result;
    }
    //--------------------------------------------------------------
    /*public long getPrevOrders(long itemID)
     {
         long rez = 0;
         if(mDocHeader.docState!=Document.DOC_STATE_NEW)
         {
             String sql = String.format(" SELECT SUM(coalesce(d.Orders,0)) " +  
                                        " FROM Documents doc " +
                                           " INNER JOIN DocDetails d ON d.DocID=doc.DocID" +						     
                                        " WHERE doc.ParentDocID=%s AND State = '%c' AND d.ItemID=%d", docId, Document.DOC_STATE_FINISHED, itemID);
     
             rez = Db.getInstance().getDataLongValue(sql, 0);
         }
         return rez;
     }*/

    //--------------------------------------------------------------
    public void cancelDocument() {
        mDocHeader.docState = Document.DOC_STATE_FINISHED;
        mDocHeader.onDocStateChanged();
    }

    //--------------------------------------------------------------
    public void onPriceIdChanged() {
        mDocHeader.onPriceIdChanged();
        mDocDetails.onPriceIdChanged();
    }

}