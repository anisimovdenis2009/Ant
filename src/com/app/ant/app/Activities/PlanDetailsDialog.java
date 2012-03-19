package com.app.ant.app.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.BusinessLayer.Plans;
import com.app.ant.app.BusinessLayer.Plans.PlanItem;
import com.app.ant.app.Controls.DataGrid;
import com.app.ant.app.Controls.DataGrid.CellStyle;
import com.app.ant.app.Controls.DataGrid.CellStyleCollection;
import com.app.ant.app.Controls.DataGrid.GridColumns;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Settings;

import java.util.ArrayList;
import java.util.Calendar;


public class PlanDetailsDialog extends DialogBase
{
	private final int SWITCH_COLOR_INACTIVE = 0xFFFFFFFF;
	private final int SWITCH_COLOR_ACTIVE 	= 0xFF00FF00;
	
    TextView textMSULabel;
    TextView textHryvnaLabel;
    TextView textMonthLabel;
    TextView textDayLabel;
    TextView textItemGroup;
    
	private DataGrid mGrid;
	private Cursor mCursor;
    
    private Context context;
    
    boolean displayInMSU = true;
    boolean displayMonth = true;
    
	int planColumnIdx = 0;
	int factColumnIdx = 0;
	int sortIDColumnIdx = 0;
	
	ItemGroupDictionarySimple simpleItemGroupDictionary;
	
	//--------------------------------------------------------------
	public class ItemGroupDictionarySimple
	{		
		public class ItemGroup
		{
			public long id;
			public String name;
			
			public ItemGroup(long id, String name)
			{
				this.id = id;
				this.name = name;
			}
		}
		
		public ArrayList<ItemGroup> itemGroups = new ArrayList<ItemGroup>();
		private int currentItemGroup = 0;
		
		public ItemGroupDictionarySimple(int planType, Calendar planDate)
		{
			//String sql = "SELECT i.ItemGroupID, i.ItemGroupName FROM ItemGroups i";
			
			boolean monthPlan = (planType == Plans.PLAN_TYPE_MONTH_SALES);
			long totalsItemGroupID = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_ITEM_GROUP_PLAN_TOTAL, -1);
			
	    	String sql = " SELECT DISTINCT pd.ItemGroupID, i.ItemGroupName " +
	    				//" 		,CASE pd.ItemGroupID WHEN " + totalsItemGroupID + " then 0 else 1 end AS SortID " +	    	
						" FROM Plans p " + 
						"     INNER JOIN PlanDetails pd ON pd.PlanID = p.PlanID " +
						"	  LEFT JOIN ItemGroups i ON i.ItemGroupID = pd.ItemGroupID " + 
						" WHERE  p.PlanTypeID = " + planType + 
								(monthPlan? "": " AND p.planDate " + Q.getSqlBetweenDayStartAndEnd(planDate.getTime()) ) ;
	    				// + " ORDER BY SortID ";

			Cursor groupsCursor = Db.getInstance().selectSQL(sql);    	
	    	if(groupsCursor!=null)
	    	{
	    		int idColumnIdx = groupsCursor.getColumnIndex("ItemGroupID");
	    		int nameColumnIdx = groupsCursor.getColumnIndex("ItemGroupName");
	    		
	    		for(int i=0; i<groupsCursor.getCount(); i++)
	    		{
	    			groupsCursor.moveToPosition(i);
	    			long id = groupsCursor.getLong(idColumnIdx);
	    			String name = (id==totalsItemGroupID ? context.getResources().getString(R.string.plan_details_item_group_all) 
	    												  : groupsCursor.getString(nameColumnIdx));
	    			
	    			itemGroups.add(new ItemGroup(id, name));
	    		}
		    		
	    		groupsCursor.close();		    	
	    	}    	
			
		}
		
		public long getCurrentItemGroupID()
		{
			ItemGroup group = getCurrentItemGroup();
			return group == null ? 0 : group.id;
		}
		
		public ItemGroup getCurrentItemGroup()
		{
			if(currentItemGroup>=0 && currentItemGroup<itemGroups.size())
				return itemGroups.get(currentItemGroup);
			
			return null;
		}
		
		public boolean switchToNextItemGroup()
		{
			if(currentItemGroup+1 <itemGroups.size())
			{
				currentItemGroup++;
				return true;
			}
			return false;			
		}
		
		public boolean switchToPrevItemGroup()
		{
			if(currentItemGroup>0)
			{
				currentItemGroup--;
				return true;
			}
			return false;			
		}
		
	}	
    
	//--------------------------------------------------------------	
	public Dialog onCreate(final Context context)
    {
    	try
    	{
    		this.context = context;
    		
    		String title = context.getResources().getString(R.string.report_day_summaries_sale_plans);
			Dialog dlg = super.onCreate(context, R.layout.plan_details_dialog, R.id.planDetails, 
 								title, DIALOG_FLAG_HAVE_OK_BUTTON | DIALOG_FLAG_NO_TITLE );   		
			
	        textMSULabel = (TextView) findViewById(R.id.textSwitchMSU);
	        textHryvnaLabel = (TextView) findViewById(R.id.textSwitchHryvnas);
	        textMonthLabel = (TextView) findViewById(R.id.textSwitchMonth);
	        textDayLabel = (TextView) findViewById(R.id.textSwitchDay);
	        textItemGroup = (TextView) findViewById(R.id.textItemGroup);	        
			
	        initGrid(true);
	    	
	        //
			//switch between displayed units
	        //
	        ViewGroup switchUnits = (ViewGroup) findViewById(R.id.salePlansSwitchUnit);
	        switchUnits.setOnClickListener( new ViewGroup.OnClickListener() 
			{			
				@Override public void onClick(View v) 
				{
					try
					{
						displayInMSU = !displayInMSU;
						updateUnitLabels();

						initGrid(false);	//fully reinit grid because column types will be changed
					}
					catch(Exception ex)
					{
						ErrorHandler.CatchError("Exception in switchUnits.onClick", ex);			
					}
				}
			});        
	        updateUnitLabels();
	        
	        //
			//switch between Month and Day
	        //
	        {
		        ViewGroup switchPeriod = (ViewGroup) findViewById(R.id.switchMonthDay);
		        switchPeriod.setOnClickListener( new ViewGroup.OnClickListener() 
				{			
					@Override public void onClick(View v) 
					{
						try
						{
							displayMonth = !displayMonth;
							updatePeriodLabels();
	
							fillGrid(true);
						}
						catch(Exception ex)
						{
							ErrorHandler.CatchError("Exception in switchPeriod.onClick", ex);			
						}
					}
				});        
		        updatePeriodLabels();
	        }
	        
			//
	        //change the current group
	        //

	        {
		        ImageButton buttonPrevItemGroup = (ImageButton) findViewById(R.id.buttonPrevItemGroup);
		        buttonPrevItemGroup.setOnClickListener( new View.OnClickListener() 
				{				
					@Override public void onClick(View v) 
					{
						try
						{
							if(simpleItemGroupDictionary.switchToPrevItemGroup())
							{
								updateItemGroupName();
								fillGrid(false);
							}
						}
						catch(Exception ex)
						{
							ErrorHandler.CatchError("Exception in PlanDetailsDialog.buttonPrevItemGroup.onClick", ex);			
						}
					}
				});
	        }
	        
	        {
		        ImageButton buttonNextItemGroup = (ImageButton) findViewById(R.id.buttonNextItemGroup);
		        buttonNextItemGroup.setOnClickListener( new View.OnClickListener() 
				{				
					@Override public void onClick(View v) 
					{
						try
						{
							if(simpleItemGroupDictionary.switchToNextItemGroup())
							{
								updateItemGroupName();
								fillGrid(false);
							}
						}
						catch(Exception ex)
						{
							ErrorHandler.CatchError("Exception in PlanDetailsDialog.buttonNextItemGroup.onClick", ex);			
						}
					}
				});
	        }	        
	        
			
			return dlg;
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in RouteItemAddDialog.onCreate", ex);
		}
    	
    	return null;
    }
	
    //------------------------------------------------------------------------------------
    public void updateItemGroupName()
    {
    	ItemGroupDictionarySimple.ItemGroup itemGroup = simpleItemGroupDictionary.getCurrentItemGroup();
    	String itemGroupName = itemGroup!=null ? itemGroup.name : "";    	
    	textItemGroup.setText(itemGroupName);
    }
	
    //------------------------------------------------------------------------------------
    public void updatePeriodLabels()
    {
		textMonthLabel.setTextColor(displayMonth ? SWITCH_COLOR_ACTIVE:SWITCH_COLOR_INACTIVE);
		textDayLabel.setTextColor(displayMonth ? SWITCH_COLOR_INACTIVE:SWITCH_COLOR_ACTIVE);
    }
	
    //------------------------------------------------------------------------------------
    public void updateUnitLabels()
    {  	
		textMSULabel.setTextColor(displayInMSU ? SWITCH_COLOR_ACTIVE:SWITCH_COLOR_INACTIVE);
		textHryvnaLabel.setTextColor(displayInMSU ? SWITCH_COLOR_INACTIVE:SWITCH_COLOR_ACTIVE);
    }
	
    //--------------------------------------------------------------
    private void initGrid(boolean reinitItemGroups)
    {
        mGrid = (DataGrid) findViewById(R.id.dataGrid);
        
        int dataType = displayInMSU ? DataGrid.DATA_TYPE_MSU : DataGrid.DATA_TYPE_MONEY;
        
        DataGrid.ColumnInfo[] columns = new DataGrid.ColumnInfo[]
        {
        		new DataGrid.ColumnInfo(1, context.getResources().getString(R.string.plan_details_column_header_img), 
						DataGrid.DATA_TYPE_VARCHAR, 25, 
						DataGrid.GRID_COLUMN_IMAGE|DataGrid.GRID_COLUMN_FIXED|DataGrid.GRID_COLUMN_NO_VALUE|DataGrid.GRID_COLUMN_DENY_SORTING, 
						"Img", 1, (short)-1, ""),
        		new DataGrid.ColumnInfo(2, context.getResources().getString(R.string.plan_details_column_header_name), 
        							DataGrid.DATA_TYPE_VARCHAR, 295, DataGrid.GRID_COLUMN_DEFAULT|DataGrid.GRID_COLUMN_FIXED, "Name", 2, (short)-1, ""),
        		new DataGrid.ColumnInfo(3, context.getResources().getString(R.string.plan_details_column_header_plan),
        							dataType, 80, DataGrid.GRID_COLUMN_DEFAULT, "PlanQnt", 3, (short)-1, ""),
        		new DataGrid.ColumnInfo(4, context.getResources().getString(R.string.plan_details_column_header_fact), 
        							dataType, 80, DataGrid.GRID_COLUMN_DEFAULT, "FactQnt", 4, (short)-1, ""),
        		new DataGrid.ColumnInfo(5, context.getResources().getString(R.string.plan_details_column_header_percent), 
        							DataGrid.DATA_TYPE_MONEY, 60, DataGrid.GRID_COLUMN_CALCULABLE|DataGrid.GRID_COLUMN_DENY_SORTING, "Percent", 5, (short)-1, ""),
        };
        
        mGrid.setColumns(new GridColumns(columns));
        mGrid.setCellListener(new CellListener());
        //mGrid.setDefaultSortColumnByDbField("Name");
        //mGrid.setSortOrder(DataGrid.SortOrder.ASCENDING);
        mGrid.setIdentityColumn(0);
        mGrid.setSelectionEnabled(false);
        
        //get grid row height from settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //int gridRowHeight = Convert.toInt(prefs.getString(context.getString(R.string.preferences_grid_row_height_key), "40"), 40);        
        int gridTextSize = Convert.toInt(prefs.getString(context.getString(R.string.preferences_grid_text_size_key), "16"), 16);
        int gridFlingVelocity = Convert.toInt(prefs.getString(context.getString(R.string.preferences_grid_fling_velocity_key), "15"), 15);
        
        mGrid.setRowHeight(35);	//use fixed row height here
        mGrid.setTextSize(gridTextSize);
        mGrid.setHeaderTextSize(gridTextSize);
        mGrid.setFlingVelocity(gridFlingVelocity);
        
        fillGrid(reinitItemGroups);
    }
    
	//--------------------------------------------------------------    
    private String getPlanByAddrOrClientQuery(int planType, int unitID, long itemGroupID, Calendar planDate, boolean byClient)
    {
    	boolean monthPlan = (planType == Plans.PLAN_TYPE_MONTH_SALES);
    	
    	String select = byClient ? "0 AS SortID, 0 AS AddrID, 		c.NameScreen AS Name, " 
    							: " 1 AS SortID, a.AddrID AS AddrID, a.AddrName AS Name, ";
    	
    	String joinDayPlan = monthPlan ? " LEFT JOIN (" + getPlanByAddrOrClientQuery(Plans.PLAN_TYPE_VISIT_SALES, unitID, itemGroupID, planDate, byClient) + " ) x " +
    									 " ON " + (byClient ? " x.ClientID= c.ClientID ": "x.AddrID = a.AddrID ")	
    									: "";
    	
    	String sql = " SELECT a.ClientID AS ClientID," + select + " c.NameScreen AS ClientName, sum(pd.PlanQnt) as PlanQnt, " +
    				(monthPlan ? "sum(pd.FactQnt) + coalesce(x.FactQnt,0) AS FactQnt" :" sum(pd.FactQnt) as FactQnt") +
		" FROM Plans p " + 
		"     INNER JOIN PlanDetails pd ON pd.PlanID = p.PlanID " +
		"	  INNER JOIN Addresses a ON a.AddrID = p.AddrID " +
		" 	  INNER JOIN Clients c ON c.ClientID = a.ClientID " +
			  joinDayPlan +  
		" WHERE pd.ItemGroupID=" + itemGroupID +
				" AND p.PlanTypeID = " + planType + 
				" AND pd.UnitID = " + unitID +
				(monthPlan? "": " AND p.planDate " + Q.getSqlBetweenDayStartAndEnd(planDate.getTime()) ) +
		" GROUP BY a.ClientID," + (byClient ? " c.NameScreen " : " a.AddrID, a.AddrName ");	
    	
    	return sql;
    }
    
    //--------------------------------------------------------------
    private String getPlanQuery(int planType, int unitID, long itemGroupID, Calendar planDate)
    {
    	String sqlAddr = getPlanByAddrOrClientQuery(planType, unitID, itemGroupID, planDate, false);
    	String sqlClient = getPlanByAddrOrClientQuery(planType, unitID, itemGroupID, planDate, true);
    	String sql = sqlClient + " union " + sqlAddr + " order by ClientName, SortID ";   
    	
    	return sql;
    }
	
	//--------------------------------------------------------------
	/** ���������� ������ ��������/�������. ������, ������� ��� ���� � �������� �� �������� ����, �� ���������� � ������*/
	private void fillGrid(boolean reinitItemGroups)
	{
		//long totalsItemGroupID = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_ITEM_GROUP_PLAN_TOTAL, -1);		
		int unitID = displayInMSU ? Plans.MSU_UNIT_ID : Plans.GRIVNA_UNIT_ID;
		int planType = displayMonth ? Plans.PLAN_TYPE_MONTH_SALES : Plans.PLAN_TYPE_VISIT_SALES; 
		Calendar planDate = Calendar.getInstance();
		
		if(reinitItemGroups)
		{
			simpleItemGroupDictionary = new ItemGroupDictionarySimple(planType, planDate);
			updateItemGroupName();
		}		
		
		long itemGroupID = simpleItemGroupDictionary.getCurrentItemGroupID();		
		String sql = getPlanQuery(planType, unitID, itemGroupID, planDate);
	
    	mCursor = Db.getInstance().selectSQL(sql);    	
    	if(mCursor!=null)
    	{
    		planColumnIdx = mCursor.getColumnIndex("PlanQnt");
    		factColumnIdx = mCursor.getColumnIndex("FactQnt");
    		sortIDColumnIdx = mCursor.getColumnIndex("SortID");
    		
    		mGrid.setCursor(mCursor);
    	}    	
	}
	

    //--------------------------------------------------------------
    private class CellListener extends DataGrid.BaseCellListener 
    {
    	@Override public Object onCellCalculate(int row, int column, Object[] values) 
        { 
        	try
        	{
	        	String columnName = mGrid.getDbFieldForColumn(column);       	
	        	
	        	if(columnName.equals("Percent"))
	        	{   
	        		double plan = mCursor.getDouble(planColumnIdx);
	        		double fact = mCursor.getDouble(factColumnIdx);
	        		String percent = PlanItem.getPercentString(plan, fact); 
	        		
	        		return percent;
	        	}
        	}
    		catch(Exception ex)
    		{			
    			ErrorHandler.CatchError("Exception in docListForm.onCellCalculate", ex);
    		}   	        	

        	return null; 
        }
        
    	@Override public CellStyle onCalculateRowStyle() 
        {
        	try
        	{
	        	if(!mCursor.isNull(sortIDColumnIdx))
	        	{
	        		int sortID = mCursor.getInt(sortIDColumnIdx);
	        		CellStyle style = (CellStyle) CellStyleCollection.getDefault().clone();
	        		if(sortID == 0)
	        		{
	        			//������, ������������ �������
	        			style.bgColor = 0xFFFFFFA0;
	        			style.setImageIndex(AntContext.DEF_PLUS_IMG_IDX);
	        		}
	        		else
	        		{
	        			//�����, ����������� �������
	        			style.setImageIndex(AntContext.DEF_CHILD_IMG_IDX);
	        		}
	        			
	        		return style;
	        	}
        	}
        	catch(Exception ex)
        	{
        		ErrorHandler.CatchError("Exception in PlanDetailsDialog.onCalculateRowStyle", ex);	
        	}
        	
        	return CellStyleCollection.getDefault();   	
        }
        
    	@Override public Bitmap onGetImage(int imageIndex) 
        {
        	Bitmap img = AntContext.getInstance().getCachedImage(context, imageIndex);        	
        	return img; 
        }
    } 
    
    
    
}

