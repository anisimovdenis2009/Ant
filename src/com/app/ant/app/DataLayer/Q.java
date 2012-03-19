package com.app.ant.app.DataLayer;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 23.01.12
 * Time: 15:57
 * To change this template use File | Settings | File Templates.
 */
import com.app.ant.app.Activities.DocSaleSelectGroupDialog.ItemGroup;
import com.app.ant.app.BusinessLayer.Address;
import com.app.ant.app.BusinessLayer.Document;
import com.app.ant.app.BusinessLayer.Plans;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Settings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Сборник SQL запросов
 * @author perevertaylo.y
 *
 */
public class Q
{
	public static final char	RECORD_STATE_ACTIVE		= 'A';
	public static final char	RECORD_STATE_DELETED	= 'D';
	public static final char	RECORD_STATE_CLOSED		= 'C';
	public static final char	RECORD_STATE_UNKNOWN	= 'U';

	public static final int	RECORD_SENT	= 1;
	public static final int	RECORD_NOT_SENT	= 0;

	public static String getSqlDay(Date date)
	{
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
	}

	public static String getSqlDayStart(Date date)
	{
		return getSqlDay(date) + " 00:00:00";
	}

	public static String getSqlDayEnd(Date date)
	{
		return getSqlDay(date) + " 23:59:59";
	}

	public static String getSqlBetweenDayStartAndEnd(Date date)
	{
		String sql = " BETWEEN '" + getSqlDayStart(date) + "' AND '" + getSqlDayEnd(date) + "' ";
		return sql;
	}
	//---------------------------------------------------------------------------------------------------------
	public static String settings_getSyncServersUrl()
	{
		String inetServer = Settings.getInstance().getPropertyFromSettings(Settings.PROPNAME_GPRS_ROOT_URL, "http://nnsrv30.alidi.ru:8080/anthillservice");
		String localServer = Settings.getInstance().getPropertyFromSettings(Settings.PROPNAME_LOCAL_ROOT_URL, "http://fo.savserv.kiev.ua:800/anthillservice");
		String customServer = Settings.getInstance().getPropertyFromSettings(Settings.PROPNAME_CUSTOM_ROOT_URL, inetServer);
		//TODO names of servers to resources
		String strSql = "SELECT 1 as _id, '" + inetServer + "' as ServerUrl, 'Интернет сервер' as ServerName " +
						"union " +
						"SELECT 2, '" + localServer + "', 'Локальный сервер' " +
						"union " +
						"SELECT 3, '" + customServer + "', 'Альтернативный сервер' " +
						"union " +
						"SELECT 4, 'http://earth3.savserv.kiev.ua:8080/anthillservice', 'Local DEV server' ";
		return strSql;
	}

	public static String settings_get()
	{
		return "SELECT Property, DefaultValue, Value FROM Settings";
	}

	//---------------------------------------------------------------------------------------------------------
	public static String columnInfo_getUpdateCommandText(int ordinal, int width, int flags, int id)
	{
		String str = String.format("UPDATE GridColumns SET Ordinal = %1$s, Width = %2$s, Flags =%3$s, Sent = 0  WHERE ID = %4$s", ordinal, width, flags, id);
		return str;
	}

	//---------------------------------------------------------------------------------------------------------
	public static String mobileLog_getInsertCommandText(Long salerId, String deviceId, Integer logType, String logText)
	{
		return String.format("insert into MobileLog (SalerID, DeviceID, LogType, LogText, LogDate, Sent) values (%1s, '%2s', %3s, '%4s', datetime('now', 'localtime'), 0)", salerId, deviceId, logType, logText);
	}

	//---------------------------------------------------------------------------------------------------------
	public static String getMSU(String curdetsTable, String orders, boolean addAsMSU)
	{
		String sql = String.format("round(sum(coalesce(%s.SUF*%s,0)),5)/1000 %s ", curdetsTable, orders, addAsMSU ? " as MSU ": "");
		return sql;
	}

	//---------------------------------------------------------------------------------------------------------
	public static String getSum(String detsTable, String curdetsTable)
	{
		String sql = String.format("round(sum(coalesce(%s.Price*( (100-coalesce(%s.Discount,0))/100)*%s.Orders*(1+coalesce(%s.VATID,0)),0)),2)", detsTable, detsTable, detsTable, curdetsTable);
		return sql;
	}

	//---------------------------------------------------------------------------------------------------------
	public static String getActualSaleDocFilter(String docTable)
	{
		Date todayDate = Calendar.getInstance().getTime();
		String actualSaleDocsFilter = String.format(" %s.DocType in ('%s','%s') AND %s.State in ('%s','%s') AND %s.CreateDate %s",
				docTable, Document.DOC_TYPE_SALE, Document.DOC_TYPE_CLAIM,
				docTable, Document.DOC_STATE_FINISHED, Document.DOC_STATE_SENT,
				docTable, getSqlBetweenDayStartAndEnd(todayDate));

		return actualSaleDocsFilter;
	}

	public static String getActualSaleDocFilterNotSent(String docTable)
	{
		Date todayDate = Calendar.getInstance().getTime();
		String actualSaleDocsFilter = String.format(" %s.DocType in ('%s','%s') AND %s.State = '%s' AND %s.CreateDate %s",
				docTable, Document.DOC_TYPE_SALE, Document.DOC_TYPE_CLAIM,
				docTable, Document.DOC_STATE_FINISHED,
				docTable, getSqlBetweenDayStartAndEnd(todayDate));

		return actualSaleDocsFilter;
	}

    //--------------------------------------------------------------
	//get sum and MSU by ItemGroup or by Item
    public static String getTodaySaleSums(boolean getGroups, long groupID, boolean getForAddress, long addrID, boolean getForCurrentDocument, long docID)
    {
    	String orders = getForCurrentDocument? " coalesce(sum(dets.OrdersI),0) + coalesce(sum(dets.OrdersII),0) AS Orders  ": " sum(dets.Orders) AS Orders ";
    	String selectFields = getGroups? " g.ItemGroupID, g.ItemGroupName, g.ParentGroupID, g.GroupType, parent.GroupType AS ParentGroupType "
				: " dets.ItemID as _id, curdets.ScreenName ";

    	//rowSum = (price*( (100-discount1)/100))*orders * (1+VATID)
    	String calcSum = getForCurrentDocument ?
    				"round(sum(coalesce(dets.Price*( (100-coalesce(dets.DiscountI,0))/100)*dets.OrdersI*(1+coalesce(curdets.VATID,0)),0)),2) + " +
    				"round(sum(coalesce(dets.Price*( (100-coalesce(dets.DiscountII,0))/100)*dets.OrdersII*(1+coalesce(curdets.VATID,0)),0)),2) "
    				: "round(sum(coalesce(dets.Price*( (100-coalesce(dets.Discount,0))/100)*dets.Orders*(1+coalesce(curdets.VATID,0)),0)),2)";
    	String castSum = String.format( "CAST(CAST( %s AS money) AS varchar) AS Sum", calcSum);
    	String MSU = getForCurrentDocument ? Q.getMSU("curdets", " coalesce(dets.OrdersI,0) + coalesce(dets.OrdersII,0) ", true) : Q.getMSU("curdets", "dets.Orders", true);
    	String select = String.format( " SELECT %s, %s, %s, %s ", selectFields, orders, castSum, MSU);

    	String from = getForCurrentDocument? "  FROM CurDocDetails dets " : "  FROM Documents d " ;
    	String groupFilter = getGroups? " ": String.format(" AND g.ItemGroupID=%d", groupID);
    	String joinDocDetails = getForCurrentDocument? "" : " INNER JOIN DocDetails dets ON d.DocID = dets.DocID ";
    	String additionalJoin = getGroups? " LEFT JOIN ItemGroups parent ON g.ParentGroupID=parent.ItemGroupID " : " ";
    	String joins = 	String.format(" INNER JOIN CurDocDetails curdets ON dets.ItemID = curdets.ItemID AND curdets.DocID=0" +
										" INNER JOIN ItemAttributes ia ON dets.ItemID = ia.ItemID " +
										" INNER JOIN ItemGroups g ON g.ItemGroupID = ia.ItemGroupID %s %s", groupFilter, additionalJoin);

    	String addrFilter = getForAddress ? String.format(" d.AddrID=%d AND ", addrID):"";
    	String where = getForCurrentDocument? String.format(" WHERE dets.DocID=%d ", docID)
    										: String.format(" WHERE %s %s ", addrFilter, Q.getActualSaleDocFilter("d"));

    	String groupBy = String.format(" GROUP BY %s ", getGroups? " g.ItemGroupID, g.ItemGroupName, g.ParentGroupID, g.GroupType, parent.GroupType " : " dets.ItemID, curdets.ScreenName ");
    	String orderBy = getGroups? " ORDER BY g.SortID ": " ORDER BY curdets.SortID ";

    	String sql = select + from + joinDocDetails + joins + where + groupBy + orderBy;

    	return sql;
    }

    //-----------------------------------------------------------------
    public static String getItemGroupFilter(String tableAlias, ItemGroup itemGroup)
    {
    	String res = "";

		if(itemGroup.haveDocGridCondition)
		{
			//add filter defined in table
			if( itemGroup.docGridCondition.length() != 0 )
				res = itemGroup.docGridCondition;
		}
		else
		{
			//filter items by ItemGroupID using ItemAttributes table
			res = String.format(" exists (SELECT ItemID FROM ItemAttributes WHERE ItemID = %s.ItemID AND ItemGroupID=%d) ", tableAlias, itemGroup.id) ;
		}

		return res;
    }

    //-----------------------------------------------------------------
    /*public static String getPlanSummaries(boolean fromVisit)
    {
		Date todayDate = Calendar.getInstance().getTime();
		String addrFilter = fromVisit ? String.format(" AND p.AddrID=%d ", AntContext.getInstance().getAddrID()):"";

		String sql = String.format(" SELECT p.PlanTypeID, types.PlanTypeName, " +
	       			" sum(dets.PlanQnt) AS PlanQntSum, " +
	       			" sum(dets.FactQnt) AS FactQntSum " +
	       	  " FROM Plans p " +
	       	  		" INNER JOIN PlanTypes types ON p.PlanTypeID = types.PlanTypeID " +
	       	  		" LEFT JOIN PlanDetails dets ON dets.PlanID = p.PlanID " +
	       	  "	WHERE dets.UnitID=%d AND (p.PlanTypeID=%d OR p.PlanDate " + Q.getSqlBetweenDayStartAndEnd(todayDate) + " ) " + addrFilter +
	       	  " GROUP BY p.PlanTypeID, types.PlanTypeName " +
	       	  " ORDER BY p.PlanTypeID ",
	       	  Plans.MSU_UNIT_ID, Plans.PLAN_TYPE_MONTH_SALES );

		return sql;
    }*/

    //-----------------------------------------------------------------
    public static String getTodayOrders(boolean visitSummaries)
    {
    	String docAddrFilter = visitSummaries ? String.format(" AND d.AddrID=%d ",AntContext.getInstance().getAddrID()):"";

		String sql = String.format(" SELECT count(DISTINCT d.DocID) AS DocCount, sum(coalesce(d.SumAll, 0)) AS SumAll, " +
	            " sum(coalesce(dets.Orders, 0)) as Orders," + Q.getMSU("curdets", "dets.Orders", true) +
				" FROM Documents d " +
				" INNER JOIN DocDetails dets ON d.DocID = dets.DocID " +
				" INNER JOIN CurDocDetails curdets ON dets.ItemID = curdets.ItemID AND curdets.DocID=0 " +
				" WHERE %s %s", Q.getActualSaleDocFilter("d"), docAddrFilter);

		return sql;
    }

    //-----------------------------------------------------------------
    public static String getTodayNotSentOrdersCount()
    {
		String sql = String.format(" SELECT count(DISTINCT d.DocID) AS DocCount " +
				" FROM Documents d " +
				" 	   INNER JOIN DocDetails dets ON d.DocID = dets.DocID " +
				" WHERE %s ", Q.getActualSaleDocFilterNotSent("d"));

		return sql;
    }

    //-----------------------------------------------------------------
    public static String getPlanFact(int planType, boolean visitSummaries, boolean fromCurrentDocument, long curDocID)
    {
    	Address addr = AntContext.getInstance().getAddress();
    	return getPlanFact(planType, visitSummaries, fromCurrentDocument, curDocID, AntContext.getInstance().getAddrID(), addr == null ? 0 : addr.channelID);
    }
    /*
     * Запрос на получение выполнения планов по насыщаемости, PowerSKU, новинкам и ОПД
     * рассчитывается факт по сегодняшним документам
     * @param planType тип плана: PLAN_TYPE_DAY_SATURABILITY, PLAN_TYPE_MONTH_POWER_SKU, PLAN_TYPE_NOVELTY или PLAN_TYPE_DAY_DISTRIBUTION
     */
    public static String getPlanFact(int planType, boolean visitSummaries, boolean fromCurrentDocument, long curDocID, long byAddrId, long channelId)
    {
    	if(planType == Plans.PLAN_TYPE_MONTH_POWER_SKU && !visitSummaries) //PowerSKU рассчитывается только для адреса
    		return "";

    	boolean isDayDistr = (planType == Plans.PLAN_TYPE_DAY_DISTRIBUTION);
    	boolean isNovelty = (planType == Plans.PLAN_TYPE_NOVELTY);

    	//проверка, входит ли товар в группу новинок
    	long noveltyItemGroupID = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_ITEM_GROUP_NOVELTY, 4047);
    	String itemFilterNovelty = isNovelty ?
    			String.format(" AND exists (select * from ItemAttributes ia where ia.ItemID = i.ItemID and ia.ItemGroupID = %d) \n", noveltyItemGroupID)
    			: "";

    	//проверка, продавался ли товар ранее; если продавался, не включаем в статистику
    	String itemFilter = String.format(
    						" AND not exists " +
							" (SELECT * " +
							" FROM AddrItems ai " +
							" 	INNER JOIN Items i2 ON i2.ItemID = ai.ItemID " +
							" WHERE ai.AddrID = %s AND i2.ProductID = i.ProductID AND (ai.ProductSaledQntCurMonth "
									+ (isDayDistr ? "+ ai.ProductPrevMonthQnt + ai.ProductPrevMonth2Qnt" : "") + ") > 0) \n",
							fromCurrentDocument ? String.format("%d", byAddrId) : " d.AddrID ");

    	String docFilter = "";
    	if(fromCurrentDocument)
    	{
    		docFilter = String.format(" dets.DocID= %d AND (dets.OrdersI>0 OR dets.OrdersII>0) \n", curDocID);
    	}
    	else
    	{
    		String docAddrFilter = visitSummaries ? String.format(" AND d.AddrID=%d \n", byAddrId) : "";
    		docFilter = Q.getActualSaleDocFilter("d") + docAddrFilter;
    	}

    	String itemChannelsJoin = (planType == Plans.PLAN_TYPE_MONTH_POWER_SKU) ?
					 			String.format(" INNER JOIN ItemChannels ic ON ic.ItemID=dets.ItemID AND ic.ChannelID = %d AND ic.TypeID=1 \n", channelId)
							 	: "";

		String sql =  " SELECT count(DISTINCT i.ProductID) AS ProductCount \n" +
					  ( fromCurrentDocument ? " FROM CurDocDetails dets \n" :  " FROM Documents d INNER JOIN DocDetails dets ON d.DocID = dets.DocID \n" ) +
						   itemChannelsJoin +
					  "	   INNER JOIN Items i ON dets.ItemID = i.ItemID \n" +
					  " WHERE " + docFilter + itemFilterNovelty + itemFilter +
					  (visitSummaries ? "" : " GROUP BY d.AddrID \n");

		if(!visitSummaries)
			sql = String.format("select sum(x.ProductCount) as ProductCount from (%s) x", sql);

		return sql;
    }

    //-----------------------------------------------------------------
    public static String getRespiteFilter()
    {
    	String respiteFilter = " (julianday(Date('now')) >= (julianday(d.DocDate) + d.Respite)) ";
    	return respiteFilter;
    }

    //-----------------------------------------------------------------
    public static String getDocStateFilter()
    {
    	String docStateFilter = String.format( " d.State!='%s' AND d.State!='%s' ", Document.DOC_STATE_PREVIOUS, Document.DOC_STATE_DELETED );
    	return docStateFilter;
    }

    //-----------------------------------------------------------------
    public static String getSalarySumSql(Double salaryRate)
    {
    	return String.format("  select ps.Name, round(%s*coalesce(x.koef, 0)*ps.Coeficient, 2) + coalesce(ps.CustomSum, 0) as mZP , " +
    						  "					round(%s*coalesce(y.dayFact, 0)/(case when x.monthPlan = 0 then 0.01 else coalesce(x.monthPlan, 1) end)*ps.Coeficient, 2) as dZP " +
							  " from PlanSalary ps " +
							  "	    left join " +
							  "			( " +
							  "			SELECT pd.ItemGroupID, (sum(pd.FactQnt)/case when sum(pd.PlanQnt) = 0 then 0.01 else sum(pd.PlanQnt) end) as koef, sum(pd.PlanQnt) as monthPlan " +
							  "			FROM Plans p " +
							  "				 inner join PlanDetails pd on p.PlanID = pd.PlanID " +
							  "			WHERE p.PlanTypeID = " + Plans.PLAN_TYPE_MONTH_SALES +
							  "					and pd.UnitID = 3 " +
							  "			group by pd.ItemGroupID " +
							  "			) x on x.ItemGroupID = ps.ItemGroupID " +
							  "	     left join " +
							  "			( " +
							  "			SELECT pd.ItemGroupID, sum(pd.FactQnt) as dayFact " +
							  "			FROM Plans p " +
							  "				 inner join PlanDetails pd on p.PlanID = pd.PlanID " +
							  "			WHERE p.PlanTypeID = " + Plans.PLAN_TYPE_VISIT_SALES +
							  "					and pd.UnitID = 3 " +
							  "			group by pd.ItemGroupID " +
							  "			) y on y.ItemGroupID = ps.ItemGroupID ", salaryRate, salaryRate);
    }

    //-----------------------------------------------------------------
    public static String getRecalcDayPlanSql(Long addrId, String dateBetween)
    {
    	//пересчитывает планы с PlanTypeID=2 (Plans.PLAN_TYPE_VISIT_SALES)
    	String selectDocumentsFact = String.format(
				        " (SELECT coalesce(%s,0) \n" +
				        " FROM Documents d \n" +
				        " inner join DocDetails dd on d.DocID = dd.DocID \n" +
				        " inner join CurDocDetails cd ON dd.ItemID = cd.ItemID AND cd.DocID = 0 \n" +
				        " inner join ItemAttributes ia on ia.ItemGroupID = pd.ItemGroupID and cd.ItemID = ia.ItemID \n" +
				        " WHERE d.AddrID = p.AddrID and d.State in ('%s', '%s') and d.CreateDate %s ) \n",
				        "%s", //this entry will be replaced later
				        Document.DOC_STATE_FINISHED, Document.DOC_STATE_SENT, dateBetween);

    	String selectDocumentsMSUFact = String.format(selectDocumentsFact, getMSU("cd", "dd.Orders", false));
    	String selectDocumentsMoneyFact = String.format(selectDocumentsFact, getSum("dd", "cd"));

    	String getFactQnt = String.format( " CASE pd.UnitID WHEN %d THEN %s WHEN %d THEN %s ELSE 0 END AS FactQnt ",
    										Plans.MSU_UNIT_ID, selectDocumentsMSUFact,
    										Plans.GRIVNA_UNIT_ID, selectDocumentsMoneyFact );

    	ArrayList<String> excludeColumns = new ArrayList<String>();
		excludeColumns.add("FactQnt");
		String columns = Db.getInstance().getTableColumnNames("PlanDetails", null, excludeColumns);
		String columnsPD = Db.getInstance().getTableColumnNames("PlanDetails", "pd", excludeColumns);

    	String sql = String.format(" replace into PlanDetails ( %s, FactQnt  ) \n" +
    			" SELECT %s, %s \n" +
    			" FROM Plans p \n" +
				"     inner join PlanDetails pd on p.PlanID = pd.PlanID \n" +
				" WHERE p.PlanTypeID = %d \n" +
						(addrId == null ? " " : " and p.AddrID = " + addrId.toString()) +
				"       and p.planDate %s ",
				columns, columnsPD, getFactQnt, Plans.PLAN_TYPE_VISIT_SALES, dateBetween);

    	return sql;
    }

    //-----------------------------------------------------------------
    public static String getRecalcDayPlanSql1(String dateBetween)
    {
    	//пересчитывает планы с PlanTypeID=5 (Plans.PLAN_TYPE_DAY_SALES)
    	ArrayList<String> excludeColumns = new ArrayList<String>();
		excludeColumns.add("FactQnt");
		String columns = Db.getInstance().getTableColumnNames("PlanDetails", null, excludeColumns);
		String columnsPD = Db.getInstance().getTableColumnNames("PlanDetails", "pd", excludeColumns);

    	String sql = String.format(
    				" replace into PlanDetails ( %s, FactQnt  ) " +
    				 " SELECT %s, " +
				        " ( SELECT sum(pd2.FactQnt) " +
				        "  FROM Plans p " +
				        "  		inner join PlanDetails pd2 on p.PlanID = pd2.PlanID " +
				        "  WHERE p.PlanTypeID = %d and p.planDate %s " +
				              " AND pd2.ItemGroupID = pd.ItemGroupID AND pd2.UnitID = pd.UnitID )" +
				     " FROM Plans p " +
				     " 		INNER JOIN PlanDetails pd on p.PlanID = pd.PlanID " +
				     " WHERE p.PlanTypeID = %d and p.planDate %s ",
				     columns, columnsPD, Plans.PLAN_TYPE_VISIT_SALES, dateBetween, Plans.PLAN_TYPE_DAY_SALES, dateBetween);

    	return sql;
    }

    //-----------------------------------------------------------------
    public static String getRouteJoin(Calendar routeDate, boolean leftJoin)
    {
    	Date useRouteDate = routeDate.getTime();

    	return String.format(" %s JOIN Routes r ON a.AddrID = r.AddrID  AND r.Date %s AND (r.State is null OR r.State!='%s') ",
									leftJoin ? "LEFT": "INNER", Q.getSqlBetweenDayStartAndEnd(useRouteDate), Q.RECORD_STATE_DELETED);

    }

    //-----------------------------------------------------------------
    public static String getRouteFilter(Calendar routeDate, boolean excludeRoute)
    {
    	String where = excludeRoute ? " AND r.AddrID IS NULL " : "";
		return " AND EXISTS ( SELECT a.AddrID " +
		  				" FROM Addresses a " +
		  				getRouteJoin(routeDate, excludeRoute) +
		  				" WHERE a.ClientID = c.ClientID" + where + ")";
    }
    //-----------------------------------------------------------------------
    public static final String DEFAULT_PRICE_ID_FOR_CLIENT = "select PriceID from ClientPrice where ClientID = ";

    public static final String CLIENT_LIST_SELECT = "select distinct A.AddrID AddrID, A.ClientID ClientID, trim(C.NamePrint) || ', ' || trim(A.AddrName) AddrName \n" +
            ", \n" +
            "case\n" +
            "when GS.State = 'A' then 1\n" +
            "else 0\n" +
            "end AddrGSState\n" +
            "from Addresses A, Clients C, AddressAttributes AT, Attributes T \n" +
            "left join AddressAttributes GS on GS.AddrID = A.AddrID and GS.AttrID = %Address.ATTR_GOLD_MAG%\n" +
            "where A.ClientID = C.ClientID\n" +
            "and AT.AddrID = A.AddrID\n" +
            "and T.AttrTypeID = 1\n" +
            "and AT.AddrID = A.AddrID\n" +
            "and AT.AttrID = T.AttrID\n" +
            "and AT.AttrID = T.AttrID\n" +
            "order by 3\n" +
            "\n";
   /* public static final String CLIENT_LIST_SELECT = "select distinct A.AddrID AddrID, A.ClientID ClientID, trim(C.NamePrint) || \", \" || trim(A.AddrName) AddrName from Addresses A, Clients C, AddressAttributes AT, Attributes T \n" +
            "where A.ClientID = C.ClientID and AT.AddrID = A.AddrID and T.AttrTypeID = 1 and AT.AddrID = A.AddrID and AT.AttrID = T.AttrID order by 3";
*/
}
