package com.app.ant.app.Activities;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.Activities.ExpandableAdapterForArray.IChildItem;
import com.app.ant.app.Activities.ExpandableAdapterForArray.IGroupItem;
import com.app.ant.app.BusinessLayer.Document;
import com.app.ant.app.BusinessLayer.Plans;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Settings;

import java.util.*;


public class PlansForm extends AntActivity 
{

	ExpandableListView mPlanList;	

	private boolean fromVisit = false;
    //--------------------------------------------------------------
    @Override  public void onCreate(Bundle savedInstanceState) 
    {
    	try
    	{
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        overridePendingTransition(0,0);
	        setContentView(R.layout.plans);
	        
	        //check form params
	        Bundle params = getIntent().getExtras();	        
			if(params!=null && params.containsKey(Document.PARAM_NAME_FROM_VISIT))
	        {
	        	fromVisit = params.getBoolean(Document.PARAM_NAME_FROM_VISIT); 
	        }

			mPlanList = (ExpandableListView) findViewById(R.id.plansExpandableList);
			
			// See expander_ic_maximized.9.png,	expander_ic_minimized.9.png, expander_group  
			mPlanList.setGroupIndicator(getResources().getDrawable(R.drawable.expandable_list_icon_selector));
			
			if(fromVisit)
			{
				ImageButton buttonNextStep = (ImageButton) findViewById(R.id.buttonNextStep);
				buttonNextStep.setVisibility(View.VISIBLE);
		        buttonNextStep.setOnClickListener( new View.OnClickListener() 
				{			
					@Override public void onClick(View v) 
					{	 
				    	try
				    	{
				    		AntContext.getInstance().getTabController().onNextStepPressed(PlansForm.this);
				    	}
						catch(Exception ex)
						{
							ErrorHandler.CatchError("Exception in DocListForm.buttonNextStep.onClick", ex);
						}					
					}
				});			
			}
	                
	        Fill();
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in plansForm.onCreate", ex);
		}    	
    }

    //--------------------------------------------------------------
    @Override public void onBackPressed() 
    {
    	try
    	{
    		if(fromVisit)	
    			AntContext.getInstance().getTabController().onBackPressed(this);
    		else
    			this.finish();
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in PlansForm.onBackPressed", ex);
		}    	
    }

    //--------------------------------------------------------------
    /** �������� ���������� � ���������� �����, � ����� ������ ����������� �� ��������� UI (������� ������� ��������)*/
    public static class PlanGroup implements IGroupItem
    {
    	long id;
    	String name;
    	boolean superHeader;

    	boolean haveSummary;
    	String value;
    	String fulfilled;
    	String percent;    	
    	
    	public PlanGroup( long id, String name, boolean superHeader )
    	{
    		this.id = id;
    		this.name = name;
    		this.superHeader = superHeader;
    		this.haveSummary = false;
    	}
    	
    	public void setStats( String value, String fulfilled, String percent)
    	{
    		this.value = value;
    		this.fulfilled = fulfilled;
    		this.percent = percent;
    		haveSummary = true;
    	}
    	
    	public Long getID() { return id; }
    	
    	public boolean isEnabled() { return true; }
    	
    	public void fillView(View view, int viewId, boolean isExpanded)
    	{
    		if(viewId == R.id.textGroupName && view instanceof TextView)
    			((TextView) view).setText(name);
    		
    		if(viewId == R.id.textGroupName || viewId == R.id.textValue || viewId == R.id.textFulfilled || viewId == R.id.textPercent)
    		{
    			if(superHeader == true)
    				view.setBackgroundColor(Color.rgb(241, 240, 156));
    			else
    				view.setBackgroundColor(0xFFFFFFFF);    			
    		}
    		
    		if(viewId == R.id.imgMarkExpanded)
    		{
    			int visibility = (!superHeader && isExpanded) ? View.VISIBLE : View.GONE;
    			view.setVisibility(visibility);
    		}
    				
    		if(viewId == R.id.imgMarkCollapsed)
    		{
    			int visibility = (!superHeader && !isExpanded) ? View.VISIBLE : View.GONE;
    			view.setVisibility(visibility);
    		}

    		if(viewId == R.id.summaryLayout)
    			view.setVisibility(haveSummary?View.VISIBLE:View.GONE);    			

    		if(haveSummary)
    		{
        		if(viewId == R.id.textValue)
        			((TextView) view).setText(value);
        		if(viewId == R.id.textFulfilled)
        			((TextView) view).setText(fulfilled);
        		if(viewId == R.id.textPercent)
        			((TextView) view).setText(String.format("%s%%", percent));		    			
    		}
    	}
    }
    
    //--------------------------------------------------------------
    /** �������� ���������� � ���������� �����, � ����� ������ ����������� �� ��������� UI (����������� ������� ��������)*/
    public static class PlanChild implements IChildItem
    {
    	long id;
    	String name;
    	String value;
    	String fulfilled;
    	String percent;
    	
    	public PlanChild( long id, String name, String value, String fulfilled, String percent)
    	{
    		this.id = id;
    		this.name = name;
    		this.value = value;
    		this.fulfilled = fulfilled;
    		this.percent = percent;
    	}
    	
    	public Long getID() { return id; }
    	
    	public boolean isEnabled() { return true; }
    	
    	public void fillView(View view, int viewId, boolean isExpanded)
    	{
    		if(viewId == R.id.textItemName && view instanceof TextView)
    			((TextView) view).setText(name);
    		if(viewId == R.id.textValue && view instanceof TextView)
    			((TextView) view).setText(value);
    		if(viewId == R.id.textFulfilled && view instanceof TextView)
    			((TextView) view).setText(String.format("%s", fulfilled));
    		if(viewId == R.id.textPercent && view instanceof TextView)
    			((TextView) view).setText(String.format("%s%%", percent));  		
    	}
    }
    
    //--------------------------------------------------------------
    public static void getTodayStats(Map<Long, Long > todayOrders, Map<Long, Double > todaySums, Map<Long, Double > todayMSUs, 
    									boolean getForAddress, long addrID, boolean getForCurrentDocument, long docID)
    {
    	String sql = Q.getTodaySaleSums(true, 0, getForAddress, addrID, getForCurrentDocument, docID );	    	
    	Cursor groupsCursor = Db.getInstance().selectSQL(sql);
    	
    	int idxItemGroupID = groupsCursor.getColumnIndex("ItemGroupID");
    	int idxItemGroupOrders = groupsCursor.getColumnIndex("Orders");
		int idxItemGroupSum = groupsCursor.getColumnIndex("Sum");
		int idxMSU = groupsCursor.getColumnIndex("MSU");
		
		for(int i=0; i<groupsCursor.getCount(); i++)
		{	
			groupsCursor.moveToPosition(i);			
		
			long itemGroupID = groupsCursor.getInt(idxItemGroupID);	
			String strSum = groupsCursor.getString(idxItemGroupSum);
			long orders = groupsCursor.getLong(idxItemGroupOrders);
			double sum = Convert.toDouble(strSum, 0.0);
			double MSU = groupsCursor.getDouble(idxMSU);
			
			if(todayOrders!=null)
				todayOrders.put(itemGroupID, orders);
			if(todaySums!=null)
				todaySums.put(itemGroupID, sum);
			if(todayMSUs!=null)
				todayMSUs.put(itemGroupID, MSU);
		}    	
    }
    
    //--------------------------------------------------------------    
    public static void getPlanData(boolean fromVisit, boolean onlyMSU, ArrayList<IGroupItem> groups, Map<Long, ArrayList<IChildItem> > children)
    {
    	//get statistics about today's documents
    	Map<Long, Double > todaySums = new HashMap< Long, Double > ();
    	Map<Long, Double > todayMSUs = new HashMap< Long, Double > ();
    	getTodayStats(null, todaySums, todayMSUs, fromVisit, fromVisit ? AntContext.getInstance().getAddrID() : 0, false, 0);
    	
    	double MSU = 0;
    	{
			String sql = Q.getTodayOrders(fromVisit);
			Cursor cursor = Db.getInstance().selectSQL(sql);
			
			if(cursor!=null)
			{
				if(cursor.getCount()>0 && cursor.moveToFirst())
				{
					MSU = cursor.getDouble(cursor.getColumnIndex("MSU"));
				}
				cursor.close();
			}
    	}
    	
    	//get plan summary 
		//String sql = Q.getPlanSummaries(fromVisit);			
		//Cursor summariesCursor = Db.getInstance().selectSQL(sql);    	
    	
    	//get plans
		
    	String addrFilter = fromVisit ? String.format(" p.AddrID = %d AND ", AntContext.getInstance().getAddrID()) : "";
    	
    	Date todayDate = Calendar.getInstance().getTime();		
		String earliestDate = "select min(p.PlanDate) from Plans p where " + addrFilter + " p.PlanTypeID = 2 and p.PlanDate >= '" + Q.getSqlDay(todayDate) + "'";
		String strEarliestDate = Db.getInstance().getDataStringValue(earliestDate, Convert.dateToString(Calendar.getInstance()));
		Calendar planDate = Convert.isNullOrBlank(strEarliestDate) ?  Calendar.getInstance() : Convert.getDateFromString(strEarliestDate);
		
		String sql = " SELECT p.PlanTypeID, types.PlanTypeName, dets.UnitName, dets.UnitID, i.ItemGroupID, i.ItemGroupName, " +  
	        				" sum(dets.PlanQnt) AS PlanQntSum, " +
	        				" sum(dets.FactQnt) AS FactQntSum " +
        				//" round(100*sum(dets.factQnt)/sum(dets.PlanQnt),2) AS Percent " + 
        			 " FROM Plans p" +  
	        				" INNER JOIN PlanTypes types ON p.PlanTypeID = types.PlanTypeID " + 
	        				" LEFT JOIN PlanDetails dets ON dets.PlanID = p.PlanID " +
	        				" LEFT JOIN ItemGroups i ON dets.ItemGroupID = i.ItemGroupID " +
	        		 " WHERE " + addrFilter + " (p.PlanTypeID = 1 OR p.PlanDate " + Q.getSqlBetweenDayStartAndEnd(planDate.getTime()) + " ) " +
	        		 " GROUP BY p.PlanTypeID, types.PlanTypeName, dets.UnitName, dets.UnitID, i.ItemGroupID, i.ItemGroupName " +
	        		 " ORDER BY p.PlanTypeID, i.ItemGroupID";

		Cursor cursor = Db.getInstance().selectSQL(sql);
		
		int idxPlanTypeID = cursor.getColumnIndex("PlanTypeID");
		int idxPlanTypeName = cursor.getColumnIndex("PlanTypeName");
		int idxUnitName = cursor.getColumnIndex("UnitName");
		int idxUnitID = cursor.getColumnIndex("UnitID");
		int idxItemGroupID = cursor.getColumnIndex("ItemGroupID");
		int idxItemGroupName = cursor.getColumnIndex("ItemGroupName");
		int idxPlanQnt = cursor.getColumnIndex("PlanQntSum");
		int idxFactQnt = cursor.getColumnIndex("FactQntSum");

		long currentPlanType = -1;
		long currentItemGroupID = -1;
		long currentItemGroupIdx = -1;
		PlanGroup currentFirstLevelGroup = null;  
		
		long totalsItemGroupID = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_ITEM_GROUP_PLAN_TOTAL, -1);
		
		for(int i=0; i < cursor.getCount(); i++)
		{	
			cursor.moveToPosition(i);			
			
			long planTypeID = cursor.getInt(idxPlanTypeID);			
			String planTypeName = cursor.getString(idxPlanTypeName);
			long itemGroupID = cursor.getInt(idxItemGroupID);
			String itemGroupName = cursor.getString(idxItemGroupName);
			String unitName = cursor.getString(idxUnitName);
			long unitID = cursor.getInt(idxUnitID);
			double planQnt = cursor.getDouble(idxPlanQnt);
			double factQnt = cursor.getDouble(idxFactQnt);
			
			//add highest-level group (corresponding to planTypeID, planTypeName)
			if( currentPlanType != planTypeID)
			{
				currentPlanType = planTypeID;
				if(onlyMSU)
					currentItemGroupIdx = i;
				
				PlanGroup group = new PlanGroup( onlyMSU ? currentItemGroupIdx : -1, planTypeName, true);
				
				//add summary info if it is present
				/*if(summariesCursor!=null)
				{
					int idxSumPlanTypeId = summariesCursor.getColumnIndex("PlanTypeID");

					for(int j=0; j<summariesCursor.getCount(); j++)
					{
						summariesCursor.moveToPosition(j);
						int sumPlanTypeID = summariesCursor.getInt(idxSumPlanTypeId);
						if(currentPlanType == sumPlanTypeID)					
						{
							double sumPlanQnt = summariesCursor.getDouble(summariesCursor.getColumnIndex("PlanQntSum"));
							double sumFactQnt = summariesCursor.getDouble(summariesCursor.getColumnIndex("FactQntSum")) + MSU;
							String percent = sumPlanQnt!=0 ? Convert.moneyToString(100*sumFactQnt/sumPlanQnt):"--";
							
							group.setStats( Convert.msuToString(sumPlanQnt), Convert.msuToString(sumFactQnt), percent);
						}
					}					
				}*/
				
				currentFirstLevelGroup = group;
				groups.add(group);				
				currentItemGroupID = -1;				
			}
			
			//add group named with itemGroupName (channel name)
			if( currentItemGroupID != itemGroupID)
			{
				currentItemGroupID = itemGroupID;
				if(!onlyMSU) 
				{
					if(currentItemGroupID!=totalsItemGroupID)	//skip totals, they will be added later to 1-st level header
					{
						currentItemGroupIdx = i;
						groups.add(new PlanGroup(currentItemGroupIdx, itemGroupName, false)); //cursor index is unique so far
					}
				}
			}

			//
			//process child nodes
			//
			if(currentItemGroupID!=totalsItemGroupID)
			{	
				ArrayList<IChildItem> curChildren = children.get(currentItemGroupIdx);
				if(curChildren == null)
				{
					curChildren = new ArrayList <IChildItem>();
					children.put(currentItemGroupIdx, curChildren); //use cursor index to attach children to proper place
				}
				
				//take into account today's documents (add values to fact)
				if(unitID == Plans.MSU_UNIT_ID)
				{
					if(todayMSUs.containsKey(itemGroupID))
						factQnt = factQnt + todayMSUs.get(itemGroupID);
				}
				else
				{
					if(todaySums.containsKey(itemGroupID))
						factQnt = factQnt + todaySums.get(itemGroupID);	
				}	
				
				String percent = planQnt!=0 ? Convert.moneyToString(100*factQnt/planQnt):"--";  	
	
				if(curChildren!=null)
				{
					if(unitID == Plans.MSU_UNIT_ID)
						curChildren.add(new PlanChild(0, onlyMSU?itemGroupName:unitName, Convert.msuToString(planQnt), Convert.msuToString(factQnt), percent));
					else
						if(!onlyMSU)
							curChildren.add(new PlanChild(0, unitName, Convert.moneyToString(planQnt), Convert.moneyToString(factQnt), percent));
				}
			}
			else
			{
				//totals node, just add info to header
				if(currentFirstLevelGroup!=null && unitID == Plans.MSU_UNIT_ID)
				{
					factQnt = factQnt + MSU;
					String percent = planQnt!=0 ? Convert.moneyToString(100*factQnt/planQnt):"--";	
					currentFirstLevelGroup.setStats( Convert.msuToString(planQnt), Convert.msuToString(factQnt), percent);
				}
			}
		}
		
		//summariesCursor.close();
		//summariesCursor = null;    	
    }

    //--------------------------------------------------------------    
    private void Fill()
    {
		ArrayList<IGroupItem> groups = new ArrayList<IGroupItem>();		
		Map<Long, ArrayList<IChildItem> > children = new HashMap< Long, ArrayList<IChildItem> > ();

		getPlanData(fromVisit, false, groups, children);
		
		/*
		//
		//test data
		//
		  
		//groups 
		groups.add(new PlanGroup(0, "PlanGroup1"));
		groups.add(new PlanGroup(1, "PlanGroup2"));
		groups.add(new PlanGroup(2, "PlanGroup3"));
		
		//children
		ArrayList<IChildItem> children0 = new ArrayList <IChildItem>();
		ArrayList<IChildItem> children1 = new ArrayList <IChildItem>();
		ArrayList<IChildItem> children2 = new ArrayList <IChildItem>();
		
		children0.add(new PlanChild(0, "PlanChild0", "100", "50", "50"));
		children0.add(new PlanChild(1, "PlanChild1", "110", "10", "15"));
		children.put(0L, children0);
		
		children1.add(new PlanChild(2, "PlanChild2", "200", "100", "50"));
		children1.add(new PlanChild(3, "PlanChild3", "300", "30", "10"));
		children.put(1L, children1);
		*/		
		
		//create adapter
		int[] groupViewIds = new int[] { R.id.textGroupName, R.id.imgMarkExpanded, R.id.imgMarkCollapsed, R.id.summaryLayout, R.id.textValue, R.id.textFulfilled, R.id.textPercent };
		int[] childViewIds = new int[] { R.id.textItemName, R.id.textValue, R.id.textFulfilled, R.id.textPercent };
		
		ExpandableAdapterForArray adapter = new ExpandableAdapterForArray(this, groups, children, 
    						R.layout.plans_group_list_item,	R.layout.plans_child_list_item, groupViewIds, childViewIds);
    	
    	mPlanList.setAdapter(adapter);
    	
    	for(int i=0; i<groups.size(); i++)
    	{
   			mPlanList.expandGroup(i);
    	}
    	
    }
    //--------------------------------------------------------------
}

