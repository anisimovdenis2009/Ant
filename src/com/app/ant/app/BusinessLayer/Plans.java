package com.app.ant.app.BusinessLayer;

import android.database.Cursor;
import com.app.ant.app.Activities.ExpandableAdapterForArray.IChildItem;
import com.app.ant.app.Activities.ExpandableAdapterForArray.IGroupItem;
import com.app.ant.app.Activities.PlansForm;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.Settings;

import java.util.*;

public class Plans
{
	public static final int	PLAN_TYPE_MONTH_SALES			= 1;
	public static final int	PLAN_TYPE_VISIT_SALES			= 2;
	public static final int PLAN_TYPE_MONTH_SATURABILITY 	= 3;
	public static final int	PLAN_TYPE_MONTH_POWER_SKU		= 4;
	public static final int PLAN_TYPE_DAY_SALES 			= 5;
	public static final int PLAN_TYPE_DAY_SATURABILITY 		= 6;
	public static final int	PLAN_TYPE_MONTH_DISTRIBUTION	= 7;
	public static final int	PLAN_TYPE_DAY_DISTRIBUTION		= 8;
	public static final int	PLAN_TYPE_DAY_POWER_SKU			= 9;
	public static final int	PLAN_TYPE_NOVELTY				= 10;
	public static final int	PLAN_TYPE_GOLD_PROGRAMM			= 11;

	public static final int	PCS_UNIT_ID						= 1;
	public static final int SKU_UNIT_ID 					= 2;
	public static final int MSU_UNIT_ID 					= 3;
	public static final int GRIVNA_UNIT_ID 					= 4;
	
	//-------------------------------------------------------------------
	public static class PlanItem
	{
		public double plan = 0;
		public double fact = 0;
		public int unitID = SKU_UNIT_ID;
		
		public PlanItem() {}
		
		public PlanItem(double plan, double fact, int unitID)
		{
			this.plan = plan;
			this.fact = fact;
			this.unitID = unitID;
		}
		
		public String getPlanString() { return getValueString(plan, unitID); }
		public String getFactString() { return getValueString(fact, unitID); }
		public String getPercentString() {	return getPercentString(plan, fact); }
		
		public static String getValueString(double value, int unitID)
		{
			if(unitID == SKU_UNIT_ID || unitID == PCS_UNIT_ID)
				return String.format("%.0f", value);
			else if(unitID == MSU_UNIT_ID)
				return Convert.msuToString(value);
			else if(unitID == GRIVNA_UNIT_ID)
				return Convert.moneyToString(value);
			else
				return Convert.moneyToString(value);
		}	
		
		public static String getPercentString(double plan, double fact)
		{
			return (plan > 0 ? String.format("%s%%", Convert.moneyToString(100*fact/plan)) : "--");
		}		
	}

	//-------------------------------------------------------------------
	public static float[] getPlanPercents(List<PlanItem> planItems)
	{
		float[] result = new float[planItems.size()];
		for(int i=0; i<planItems.size(); i++)
		{
			PlanItem planItem = planItems.get(i);			
			result[i] = (float)(planItem.plan>0 ? 100*planItem.fact/planItem.plan : 0); 
		}
		return result;
	}
	
	//-------------------------------------------------------------------	
	public static void recalcFacts()
	{

		String between = Q.getSqlBetweenDayStartAndEnd(Calendar.getInstance().getTime());
		
        String sql = Q.getRecalcDayPlanSql(null, between);
        Db.getInstance().execSQL(sql);

		sql = Q.getRecalcDayPlanSql1(between);
		Db.getInstance().execSQL(sql);
	}

    //--------------------------------------------------------------
    public static void getPlanData2(boolean fromVisit, int unitID, ArrayList<IGroupItem> groups, Map<Long, ArrayList<IChildItem> > children)
    {   	
    	long totalsItemGroupID = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_ITEM_GROUP_PLAN_TOTAL, -1);

    	String addrFilter = fromVisit ? String.format(" p.AddrID = %d AND ", AntContext.getInstance().getAddrID()) : "";
    	
    	Date todayDate = Calendar.getInstance().getTime();		
		String earliestDate = String.format(" select min(p.PlanDate) from Plans p where %s p.PlanTypeID = %d and p.PlanDate >= '%s' ",
											addrFilter, Plans.PLAN_TYPE_VISIT_SALES, Q.getSqlDay(todayDate) );
		
		String strEarliestDate = Db.getInstance().getDataStringValue(earliestDate, Convert.dateToString(Calendar.getInstance()));
		Calendar planDate = Convert.isNullOrBlank(strEarliestDate) ?  Calendar.getInstance() : Convert.getDateFromString(strEarliestDate);

		//DayPlanSummaries											
    	String sqlDayPlan = " select p2.PlanTypeID as PlanTypeID, types.PlanTypeName as PlanTypeName, pd2.ItemGroupID as ItemGroupID, g.ItemGroupName as ItemGroupName " +  
    										(fromVisit ? ", p2.AddrID" : "") +
    										", sum(pd2.PlanQnt) as PlanQnt, sum(pd2.FactQnt) as FactQnt" +
    						" from Plans p2 " + 
    						"     inner join PlanDetails pd2 on pd2.PlanID = p2.PlanID " +
    						" 	  inner join PlanTypes types ON p2.PlanTypeID = types.PlanTypeID " +
    						"     inner join ItemGroups g on g.ItemGroupID = pd2.ItemGroupID " + //" AND g.ItemGroupID != " + totalsItemGroupID +
    						" where p2.PlanTypeID = " + (fromVisit ? Plans.PLAN_TYPE_VISIT_SALES : Plans.PLAN_TYPE_DAY_SALES)  + " and pd2.UnitID = " + unitID + " and p2.planDate " + Q.getSqlBetweenDayStartAndEnd(planDate.getTime()) +
    								(fromVisit ? " and p2.AddrID = " + AntContext.getInstance().getAddrID() : "") +
    						" group by p2.PlanTypeID, pd2.ItemGroupID " + (fromVisit ? ", p2.AddrID" : "");
    	
    	String sqlMonthPlan = "select p.PlanTypeID as PlanTypeID, types.PlanTypeName as PlanTypeName, pd.ItemGroupID as ItemGroupID, g.ItemGroupName as ItemGroupName" +
    									(fromVisit ? ", p.AddrID as AddrID" : "") +
    									", sum(pd.PlanQnt) as PlanQnt, round(sum(pd.FactQnt) + x.FactQnt, 5) as FactQnt" +
							"  from Plans p " + 
							"      inner join PlanDetails pd on p.PlanID = pd.PlanID " +
							"      inner join PlanTypes types ON p.PlanTypeID = types.PlanTypeID " +
							"      inner join ItemGroups g on g.ItemGroupID = pd.ItemGroupID " + 
							"      left join (" + sqlDayPlan + " ) x " + 
							"         on x.ItemGroupID = pd.ItemGroupID " + (fromVisit ? " and x.AddrID = p.AddrID" : "") +
							" where p.PlanTypeID = " + Plans.PLAN_TYPE_MONTH_SALES + " and pd.UnitID = " + unitID +  
									(fromVisit ? " and p.AddrID = " + AntContext.getInstance().getAddrID() : "") +
							" group by p.PlanTypeID, pd.ItemGroupID " + (fromVisit ? ", p.AddrID " : "");
    	
    	String sqlPlanTotal = /*sqlDayTotalPlan + " union " +*/ sqlMonthPlan + " union " + sqlDayPlan + " order by 1, 3";
    	
    	
    	Cursor cursor = Db.getInstance().selectSQL(sqlPlanTotal);
    	
		int idxPlanTypeID = cursor.getColumnIndex("PlanTypeID");
		int idxPlanTypeName = cursor.getColumnIndex("PlanTypeName");
		int idxItemGroupID = cursor.getColumnIndex("ItemGroupID");
		int idxItemGroupName = cursor.getColumnIndex("ItemGroupName");
		int idxPlanQnt = cursor.getColumnIndex("PlanQnt");
		int idxFactQnt = cursor.getColumnIndex("FactQnt");
		
		long currentPlanType = -1;
		
		for(int i=0; i < cursor.getCount(); i++)
		{	
			cursor.moveToPosition(i);
			
			long planTypeID = cursor.getInt(idxPlanTypeID);			
			String planTypeName = cursor.getString(idxPlanTypeName);
			long itemGroupID = cursor.getInt(idxItemGroupID);
			String itemGroupName = cursor.getString(idxItemGroupName);
			double planQnt = cursor.getDouble(idxPlanQnt);
			double factQnt = cursor.getDouble(idxFactQnt);
			
			String percent = planQnt != 0 ? Convert.moneyToString(100 * factQnt/planQnt) : "--";
			
			String planned = (unitID == Plans.MSU_UNIT_ID) ? Convert.msuToString(planQnt):Convert.moneyToString(planQnt);
			String fulfilled = (unitID == Plans.MSU_UNIT_ID) ? Convert.msuToString(factQnt):Convert.moneyToString(factQnt);			
			
			if( currentPlanType != planTypeID && itemGroupID == totalsItemGroupID)
			{
				currentPlanType = planTypeID;
				
				PlansForm.PlanGroup group = new PlansForm.PlanGroup(currentPlanType, planTypeName, true);
				group.setStats( planned, fulfilled, percent);
				groups.add(group);
			}
			else
			{
				ArrayList<IChildItem> curChildren = children.get(currentPlanType);
				if(curChildren == null)
				{
					curChildren = new ArrayList <IChildItem>();
					children.put(currentPlanType, curChildren); //use currentPlanType to attach children to proper place
				}				
				curChildren.add(new PlansForm.PlanChild(0, itemGroupName, planned, fulfilled, percent));
			}
		}    	
    }
	
    //--------------------------------------------------------------
	public static PlanItem getPlanValues(Long addrId, int planTypeID, boolean forDay, int unitID, boolean getFact)
	{
		boolean byAddress = addrId != null;
		
		PlanItem planItem = new PlanItem();
		planItem.unitID = unitID;
		
		String sql =  " SELECT coalesce(sum(pd.PlanQnt), 0) AS PlanQnt " + (getFact ? ", coalesce(sum(pd.FactQnt), 0) AS FactQnt ":"") +
		 			  " FROM Plans p " +
		 			  "		INNER JOIN PlanDetails pd ON p.PlanID = pd.PlanID " +
		 			  " WHERE p.PlanTypeID = " + planTypeID + 
		 			        (forDay ? " AND p.PlanDate " + Q.getSqlBetweenDayStartAndEnd(Calendar.getInstance().getTime()) : "") + 
		 			        (byAddress ? " AND p.AddrID = " + addrId : "") +
		 			        " AND pd.UnitID = " + unitID;
		
		Cursor cursor = Db.getInstance().selectSQL(sql);			
		if(cursor!=null)
		{
			if(cursor.getCount()>0 && cursor.moveToFirst())
			{
				planItem.plan = cursor.getLong(cursor.getColumnIndex("PlanQnt"));
				planItem.fact = getFact ? cursor.getLong(cursor.getColumnIndex("FactQnt")):0;
			}
			cursor.close();
		}
		
		return planItem;
	}
    
}