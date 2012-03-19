package com.app.ant.app.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import com.app.ant.R;
import com.app.ant.app.Activities.ExpandableAdapterForArray.IChildItem;
import com.app.ant.app.Activities.ExpandableAdapterForArray.IGroupItem;
import com.app.ant.app.BusinessLayer.Document;
import com.app.ant.app.BusinessLayer.Plans;
import com.app.ant.app.BusinessLayer.Plans.PlanItem;
import com.app.ant.app.Controls.DataGrid;
import com.app.ant.app.Controls.DataGrid.GridColumns;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.ServiceLayer.*;

import java.util.*;


public class ReportDaySummariesForm extends AntActivity 
{
	
	private final static int IDD_REPORT_ITEM_SUMMARIES = 0;
	private final static int IDD_PLAN_DETAILS_DIALOG = 1;
	
	private ViewGroup viewGroupSummariesAndPlans;
	private ViewGroup viewGroupSummaries;
	private ViewGroup viewGroupPlans;
	private ViewGroup viewGroupDocumentSummaries;
	private ExpandableListView groupExpandableList;	
	
	private TabController tabController = null;
	
	Date todayDate;

	//plans
	boolean plansFilled = false;
	
	//report for item groups
	boolean groupsFilled = false;	
	ItemGroup currentItemGroup;	
	ExpandableAdapterForArray itemGroupsAdapter = null;

	//doc list report
	boolean docListFilled = false;
	private DataGrid docListGrid;
	private Cursor docListCursor;
	Spinner spnDocTypes;
	char docTypeFilter = Document.DOC_TYPE_UNKNOWN;
	private ViewGroup viewDocTotals;
	TextView textDocCount;
	TextView textDocsOrders;
	TextView textDocsMSU;
	TextView textDocsSum;
	
	private boolean fromVisit = false;
	private boolean isVisitSummaries = false;
	private boolean isVisitTasks = false;
	long addrId = AntContext.getInstance().getAddrID();

	boolean displayInMSU = true;
    TextView textMSULabel;
    TextView textHryvnaLabel;
    TableLayout salePlansPlacement;
    
	long plannedVisitCount = 0;
	Long plannedGold = 0L;
	long visitCount = 0;
	long effectiveVisitCount = 0;	
    
	//--------------------------------------------------------------	
    @Override public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	overridePendingTransition(0,0);
    	onCreateEx(true);
    }
    
    private void onCreateEx(boolean firstRun)
    {
    	try
    	{
	        setContentView(R.layout.report_day_summaries_form);
	        
	        //check form params
	        Bundle params = getIntent().getExtras();	        
			if(params!=null && params.containsKey(Document.PARAM_NAME_FROM_VISIT))
	        {
	        	fromVisit = params.getBoolean(Document.PARAM_NAME_FROM_VISIT);
	        	isVisitSummaries = params.getBoolean(Document.PARAM_NAME_VISIT_SUMMARIES);
	        	isVisitTasks = params.getBoolean(Document.PARAM_NAME_VISIT_TASKS);
	        }
	        
	        viewGroupSummaries = (ViewGroup) findViewById(R.id.viewGroupSummaries);
	        viewGroupSummariesAndPlans = (ViewGroup) findViewById(R.id.viewGroupSummariesAndPlans);
	        viewGroupPlans = (ViewGroup) findViewById(R.id.viewGroupPlans);
	        groupExpandableList = (ExpandableListView) findViewById(R.id.itemGroupSummariesExpandableList);
	        viewGroupDocumentSummaries = (ViewGroup) findViewById(R.id.viewGroupDocumentSummaries);
	        TextView textCaption = (TextView) findViewById(R.id.textCaption);
	        ImageView imgCaption = (ImageView) findViewById(R.id.imgCaption);
	        textMSULabel = (TextView) findViewById(R.id.textSwitchMSU);
	        textHryvnaLabel = (TextView) findViewById(R.id.textSwitchHryvnas);

			groupsFilled = false;
			docListFilled = false;
	        
	        initStepBar();
	        
	    	todayDate = Calendar.getInstance().getTime();

	    	if(isVisitSummaries)
	    	{
	    		textCaption.setText(getResources().getString(R.string.form_title_report_visit_summaries));
	    		imgCaption.setImageResource(R.drawable.visit_summaries);
	    	}
	    	
	    	if(fromVisit)
	    	{
	    		//visitPlansLabel
	    		((TextView) findViewById(R.id.visitPlansLabel)).setVisibility(View.GONE);
	    		((TableLayout) findViewById(R.id.visitPlansTable)).setVisibility(View.GONE);
	    		
	    		ImageButton buttonNextStep = (ImageButton) findViewById(R.id.buttonNextStep);
				buttonNextStep.setVisibility(View.VISIBLE);
		        buttonNextStep.setOnClickListener( new View.OnClickListener() 
				{			
					@Override public void onClick(View v) 
					{	 
				    	try
				    	{
				    		AntContext.getInstance().getTabController().onNextStepPressed(ReportDaySummariesForm.this);
				    	}
						catch(Exception ex)
						{
							ErrorHandler.CatchError("Exception in DocListForm.buttonNextStep.onClick", ex);
						}					
					}
				});			
	    	}
	    	
	    	fill();
	    	
	    	if(isVisitTasks)
	    	{
	    		LinearLayout totalsLayout  = (LinearLayout) findViewById(R.id.totalsLayout);
	    		totalsLayout.setVisibility(View.GONE);
	    		LinearLayout salaryLayout  = (LinearLayout) findViewById(R.id.salaryLayout);
	    		salaryLayout.setVisibility(View.GONE);
	    		
	    		textCaption.setText(getResources().getString(R.string.form_title_aims));
	    		imgCaption.setImageResource(R.drawable.aim);
	    		
	    		//plans should be visible on first tab
				if(plansFilled == false)
				{
					fillPlans();
					plansFilled = true;								
				}
				
				//decrease summaries group weight from 1 to 0 to cleanup area for plans
				//viewGroupSummaries.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 3f));
				//make plans group visible
		        viewGroupPlans.setVisibility(View.VISIBLE);	    		
	    	}  
	        
    	}
		catch(Exception ex)
		{
			MessageBox.show(this, getResources().getString(R.string.form_title_report_day_summaries), getResources().getString(R.string.dialog_base_exceptionOnCreateDialog));
			ErrorHandler.CatchError("Exception in ReportDaySummariesForm.onCreate", ex);
		}    	
    }
    
    //needed to handle screen rotation (this activity should have android:configChanges="keyboardHidden|orientation" in manifest
    @Override public void onConfigurationChanged(Configuration newConfig) 
    {
    	super.onConfigurationChanged(newConfig);
    	onCreateEx(false);
    }

    
    //--------------------------------------------------------------
    private void switchToTab(int tab)
    {
		try
		{
			if(tab == 0)
			{
				viewGroupSummariesAndPlans.setVisibility(View.VISIBLE);
		        viewGroupSummaries.setVisibility(View.VISIBLE);		        
		        viewGroupPlans.setVisibility(isVisitTasks? View.VISIBLE:View.GONE);
		        groupExpandableList.setVisibility(View.GONE);
		        viewGroupDocumentSummaries.setVisibility(View.GONE);
			}
			else if(tab == 1)
			{
				if(plansFilled == false)
				{
					fillPlans();
					plansFilled = true;								
				}
				viewGroupSummariesAndPlans.setVisibility(View.VISIBLE);
		        viewGroupSummaries.setVisibility(View.GONE);
		        viewGroupPlans.setVisibility(View.VISIBLE);
		        groupExpandableList.setVisibility(View.GONE);
		        viewGroupDocumentSummaries.setVisibility(View.GONE);
			}			
			/*else if(tab == 2)
			{
				if(groupsFilled == false)
				{
					fillItemGroups();
					groupsFilled = true;								
				}
				viewGroupSummariesAndPlans.setVisibility(View.GONE);
		        viewGroupSummaries.setVisibility(View.GONE);
		        viewGroupPlans.setVisibility(View.GONE);
		        groupExpandableList.setVisibility(View.VISIBLE);
		        viewGroupDocumentSummaries.setVisibility(View.GONE);
			}*/
			else
			{
				if(docListFilled == false)
				{
					fillDocListTab();
					docListFilled = true;
				}
				viewGroupSummariesAndPlans.setVisibility(View.GONE);
		        viewGroupSummaries.setVisibility(View.GONE);
		        viewGroupPlans.setVisibility(View.GONE);
		        groupExpandableList.setVisibility(View.GONE);
		        viewGroupDocumentSummaries.setVisibility(View.VISIBLE);				
			}
		}
		catch(Exception ex)
		{
			MessageBox.show(ReportDaySummariesForm.this, getResources().getString(R.string.form_title_report_day_summaries), getResources().getString(R.string.report_day_summaries_exceptionPrepareReport));
			ErrorHandler.CatchError("Exception in ReportDaySummariesForm.onTabSelected", ex);
		}    	
    }
    
    //--------------------------------------------------------------    
    private void initStepBar()
    {  	
    	//init tabs
    	
    	//when activity is restarted (e.g in case of screen rotate, we should keep old tabController
    	if(tabController == null)
    	{
    		//create new for the first time
			tabController = new TabController();
			tabController.addTab((Context)this, 0, null, R.string.report_day_summaries_totals, TabController.TAB_FLAGS_DEFAULT);
			
			if(!isVisitTasks)
				tabController.addTab((Context)this, 1, null, R.string.report_day_summaries_plans, TabController.TAB_FLAGS_DEFAULT);
			
			if (!fromVisit)
			{
				/*tabController.addTab((Context)this, 2, null, R.string.report_day_summaries_items, TabController.TAB_FLAGS_DEFAULT);*/
				tabController.addTab((Context)this, 3, null, R.string.report_day_summaries_documents, TabController.TAB_FLAGS_DEFAULT);
			}
			
			if(fromVisit && !isVisitTasks)
			{
				tabController.selectTab(1);
				switchToTab(1);
			}
			else
				tabController.selectTab(0);
    	}
    	else
    	{
    		switchToTab(tabController.getSelectedTab());
    	}
		
		tabController.setTabEventListener(
				new TabController.ITabEventListener()
				{
					public void onTabSelected(int tab)
					{
						switchToTab(tab);
					}
				});
		
    	ViewGroup tabsPlacement = (ViewGroup) findViewById(R.id.tabsPlacement);
    	tabController.createTabs(this, tabsPlacement);
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
			ErrorHandler.CatchError("Exception in clientForm.onBackPressed", ex);
		}   	
    }
    //--------------------------------------------------------------
    public static void addPlanRow(Context context, ViewGroup parentView, String name, PlanItem planItem)
    {
    	addPlanRow(context, parentView, name, planItem.getPlanString(), planItem.getFactString(), planItem.getPercentString(),
    					true, Color.WHITE);
    }
    
    public static void addPlanHeaderRow(Context context, ViewGroup parentView)
    {
    	addPlanRow(context, parentView, "", context.getString(R.string.plan_header_plan), 
    				context.getString(R.string.plan_header_fact), context.getString(R.string.plan_header_percent),
    					false, Color.rgb(230, 230, 230));
    }    
    
    public static void addPlanRow(Context context, ViewGroup parentView, String name, String value, String fulfilled, String percent,
    							boolean displayBullet, int backgroundColor)
    {
		LayoutInflater inflater = ((Activity)context).getLayoutInflater();
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.plans_row, (ViewGroup) ((Activity)context).findViewById(R.id.plansRow));
		
		TableRow row = (TableRow) layout.findViewById(R.id.plansTableRow);
		
		TextView textName = (TextView) row.findViewById(R.id.textPlanName);
		TextView textValue = (TextView) row.findViewById(R.id.textPlanValue);
		TextView textFulfilled = (TextView) row.findViewById(R.id.textPlanFulfilled);
		TextView textPercent = (TextView) row.findViewById(R.id.textPlanPercent);
		ImageView imgBullet = (ImageView) row.findViewById(R.id.imgBullet);

		textName.setText(name);
		textValue.setText(value);
		textFulfilled.setText(fulfilled);
		textPercent.setText(percent);
		imgBullet.setVisibility(displayBullet? View.VISIBLE:View.INVISIBLE);
		row.setBackgroundColor(backgroundColor);
			
		layout.removeView(row);	
		parentView.addView(row);
    	
    }
    //--------------------------------------------------------------
    private void addSeparator(ViewGroup parentView)
    {
		TextView tv = new TextView(this);
		tv.setTextSize(4);
		parentView.addView(tv);    	
    }
    
    //--------------------------------------------------------------
    private void fill()
    { 	
    	//get controls  
    	TextView textSum = (TextView) findViewById(R.id.textSum);
    	TextView textDocCount = (TextView) findViewById(R.id.textDocCount);
    	TextView textOrders = (TextView) findViewById(R.id.textOrders);
    	TextView textMSU = (TextView) findViewById(R.id.textMSU);
    	
    	//
    	//get values from db
    	//
    	String docAddrFilter = isVisitSummaries ? String.format(" AND d.AddrID=%d ",addrId):"";
    	String visitAddrFilter = isVisitSummaries ? String.format(" AND v.AddrID=%d ",addrId):"";
    	String routeAddrFilter = isVisitSummaries ? String.format(" AND r.AddrID=%d ",addrId):"";
    	
    	//visit count    	
    	String sql = String.format("SELECT count(DISTINCT v.AddrID) FROM Visits v WHERE v.VisitStartDate %s %s ", Q.getSqlBetweenDayStartAndEnd(todayDate), visitAddrFilter);
    	visitCount = Db.getInstance().getDataLongValue(sql, 0);
    	
    	//effective visit count
    	sql = String.format( " SELECT count(DISTINCT v.AddrID) "+ 
    						 " FROM Visits v " +
    						 	  " INNER JOIN Documents d ON d.VisitID=v.VisitID AND d.SumAll > 100 " +
    						 " WHERE v.VisitStartDate %s %s ", Q.getSqlBetweenDayStartAndEnd(todayDate), visitAddrFilter);
    	effectiveVisitCount = Db.getInstance().getDataLongValue(sql, 0);

    	//sum
		sql = String.format("SELECT sum(coalesce(d.SumAll, 0)) FROM Documents d WHERE %s %s", Q.getActualSaleDocFilter("d"), docAddrFilter);
		double sum = Db.getInstance().getDataDoubleValue(sql, 0);

		//orders and MSU		
		long docCount = 0;
		long orders = 0;
		double MSU = 0;
		
		{
			sql = Q.getTodayOrders(isVisitSummaries);
			Cursor cursor = Db.getInstance().selectSQL(sql);
			
			if(cursor!=null)
			{
				if(cursor.getCount()>0 && cursor.moveToFirst())
				{
					docCount = cursor.getLong(cursor.getColumnIndex("DocCount"));
					orders = cursor.getLong(cursor.getColumnIndex("Orders"));
					MSU = Convert.roundUpFullMoney(cursor.getDouble(cursor.getColumnIndex("MSU")));
				}
				cursor.close();
			}
		}
		//paymentSum
		sql = String.format(" SELECT sum(coalesce(d.SumAll, 0)) FROM Documents d " +
							" WHERE d.DocType in ('%s') and d.State in ('%s','%s') AND d.CreateDate %s %s", 
							Document.DOC_TYPE_PAYMENT, Document.DOC_STATE_FINISHED, Document.DOC_STATE_SENT,
							Q.getSqlBetweenDayStartAndEnd(todayDate), docAddrFilter);
		double paymentSum = Db.getInstance().getDataDoubleValue(sql, 0);
				
		
		//planned visit count and planned payment sum
		double clientSaldo1 = 0.;
		double clientSaldo2 = 0.;
		double clientSaldo3 = 0.;
		double clientSaldo4 = 0.;
		
		{
			int goldAttrID = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_ATTR_ID_ADDRESS_GOLD, -1);			
			String selectGold = (goldAttrID==-1) ? ",0 AS gold " : ", count(aagold.AttrID) AS gold";
			String joinGold = (goldAttrID==-1) ? "" : " LEFT JOIN AddressAttributes aagold ON r.AddrID=aagold.AddrID AND aagold.AttrID="+goldAttrID;

			long defaultDirectionID = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_DEFAULT_DIRECTION_ID, 0);
			sql =   " SELECT count(r.AddrID) AS PlannedVisitCount, " +
							String.format(" sum(coalesce((select round(max(Saldo),2) from ClientSaldo where AddrID = r.AddrID and SaldoTypeID=2 and DirectionID=%d ), 0)) AS ClientSaldo1, ", defaultDirectionID) +
							String.format(" sum(coalesce((select round(max(Saldo),2) from ClientSaldo where AddrID = r.AddrID and SaldoTypeID=6 and DirectionID=%d ), 0)) AS ClientSaldo2, ", defaultDirectionID) +
							String.format(" sum(coalesce((select round(sum(Saldo),2) from ClientSaldo where AddrID = r.AddrID and SaldoTypeID=3 and DirectionID=%d ), 0)) AS ClientSaldo3, ", defaultDirectionID) +
							String.format(" sum(coalesce((select round(sum(Saldo),2) from ClientSaldo where AddrID = r.AddrID and SaldoTypeID=4 and DirectionID=%d ), 0)) AS ClientSaldo4 ", defaultDirectionID) +														
							selectGold +
					" FROM Routes r " +
						joinGold +
					" WHERE r.Date " + Q.getSqlBetweenDayStartAndEnd(todayDate) + routeAddrFilter;
			
			Cursor cursor = Db.getInstance().selectSQL(sql);
			
			if(cursor!=null)
			{
				if(cursor.getCount()>0 && cursor.moveToFirst())
				{
					plannedVisitCount = cursor.getLong(cursor.getColumnIndex("PlannedVisitCount"));
					clientSaldo1 = cursor.getDouble(cursor.getColumnIndex("ClientSaldo1"));
					clientSaldo2 = cursor.getDouble(cursor.getColumnIndex("ClientSaldo2"));
					clientSaldo3 = cursor.getDouble(cursor.getColumnIndex("ClientSaldo3"));
					clientSaldo4 = cursor.getDouble(cursor.getColumnIndex("ClientSaldo4"));					
										
					plannedGold = cursor.getLong(cursor.getColumnIndex("gold"));
				}
				cursor.close();
			}
		}
		
		//---------------------------- �����
		
		TableLayout debtsPlacement =  (TableLayout) findViewById(R.id.debtsTable);
		
		addPlanHeaderRow(this, debtsPlacement);
		
		addPlanRow(this, debtsPlacement, getResources().getString(R.string.report_day_summaries_debt),				
				Convert.moneyToString(Convert.roundUpMoney(clientSaldo1)), 
				Convert.moneyToString(Convert.roundUpMoney(paymentSum)),
				PlanItem.getPercentString(clientSaldo1, paymentSum),
				//clientSaldo1!=0 ? String.format("%s%%",Convert.moneyToString(100*paymentSum/clientSaldo1)):"--",
				true, Color.WHITE);		
		
		addPlanRow(this, debtsPlacement, getResources().getString(R.string.report_day_summaries_debt_expired),
				Convert.moneyToString(Convert.roundUpMoney(clientSaldo2)),	"--", "--", true, Color.WHITE );
		
		addPlanRow(this, debtsPlacement, getResources().getString(R.string.report_day_summaries_debt_seven_days),
				Convert.moneyToString(Convert.roundUpMoney(clientSaldo3)),	"--", "--", true, Color.WHITE );
		
		addPlanRow(this, debtsPlacement, getResources().getString(R.string.report_day_summaries_debt_today), 
				Convert.moneyToString(Convert.roundUpMoney(clientSaldo4)),	"--", "--", true, Color.WHITE );
		
		    	
	    //---------------------------- display values 
		textSum.setText( Convert.moneyToString(Convert.roundUpMoney(sum)));
    	textDocCount.setText(((Long)docCount).toString());
    	textOrders.setText(((Long)orders).toString());    	
    	textMSU.setText(String.format("%.4f", MSU));
    	
	    //---------------------------- display salary
    	TextView textSalaryBonusM = (TextView) findViewById(R.id.report_day_summaries_salary_sum);
    	TextView textSalaryBonusD = (TextView) findViewById(R.id.report_day_summaries_salary_sum_day);
    	//TextView textSalarySumPlusSalaryRate = (TextView) findViewById(R.id.report_day_summaries_salary_sum_plus_salary_rate);
    	TextView textSalaryPrognosis = (TextView) findViewById(R.id.report_day_summaries_salary_prognosis);
    	    	
    	TableLayout salaryTable = (TableLayout)findViewById(R.id.report_day_summaries_salary_table);    	
    	    	
    	double salaryRate = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_SALER_SALARY_RATE, 6000); //salary rate
    	
    	Double[] salaries = initSalaryRows(salaryTable, salaryRate);
    	
//		double salary = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_SALER_SALARY, 4000); //morning salary
    	double dSalary = salaries[1];
    	double mSalary = salaries[0];

		String currencyName = getResources().getString(R.string.currency_name);
    	String salaryMonth = Convert.moneyToString(mSalary) + " " + currencyName;
    	String salaryDay = Convert.moneyToString(dSalary) + " " + currencyName;
    	
    	int daysInMonth = Convert.getNumDaysInCurrentMonth();
    	int daysPassed = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    	double salaryPrognosis = (mSalary + dSalary)*daysInMonth/daysPassed; //double salaryPrognosis = (mSalary + dSalary)*daysInMonth/daysPassed + salaryRate;
    	
    	textSalaryBonusM.setText(salaryMonth);
    	textSalaryBonusD.setText(salaryDay);
    	//textSalarySumPlusSalaryRate.setText(Convert.moneyToString(mSalary + dSalary+ salaryRate) + " " + currencyName);
    	textSalaryPrognosis.setText(Convert.moneyToString(salaryPrognosis) + " " + currencyName);
    }
    
    //------------------------------------------------------------------------------------
    private void fillSalePlans()
    {
    	addPlanHeaderRow(this, salePlansPlacement);
    	
		ArrayList<IGroupItem> groups = new ArrayList<IGroupItem>();		
		Map<Long, ArrayList<IChildItem> > children = new HashMap< Long, ArrayList<IChildItem> > ();
		int unitID = displayInMSU? Plans.MSU_UNIT_ID: Plans.GRIVNA_UNIT_ID;
		boolean displayVisitValues = isVisitSummaries || isVisitTasks; 
		Plans.getPlanData2(displayVisitValues, unitID, groups, children);		
		
		for(int i=0; i<groups.size(); i++)
		{
			//inflate table header from resource file and add it to table
			LayoutInflater inflater = getLayoutInflater();
			LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.plans_row, (ViewGroup) findViewById(R.id.plansRow));
			
			TableRow row = (TableRow) layout.findViewById(R.id.plansTableRow);			
			{
				TextView textGroupName = (TextView) row.findViewById(R.id.textPlanName);
				TextView textValue = (TextView) row.findViewById(R.id.textPlanValue);
				TextView textFulfilled = (TextView) row.findViewById(R.id.textPlanFulfilled);
				TextView textPercent = (TextView) row.findViewById(R.id.textPlanPercent);
				ImageView imgBullet = (ImageView) row.findViewById(R.id.imgBullet);
				
				groups.get(i).fillView(textGroupName, R.id.textGroupName, false);
				groups.get(i).fillView(textValue, R.id.textValue, false);
				groups.get(i).fillView(textFulfilled, R.id.textFulfilled, false);
				groups.get(i).fillView(textPercent, R.id.textPercent, false);
				imgBullet.setVisibility(View.INVISIBLE);
				row.setBackgroundColor(Color.rgb(241, 240, 156));
				
				layout.removeView(row);	
				salePlansPlacement.addView(row);
			}		
			
			//add child entries
			ArrayList<IChildItem> childList = children.get(groups.get(i).getID());
			if(childList != null)
			{
				for(int j=0; j<childList.size(); j++)
				{
					LinearLayout childLayout = (LinearLayout) inflater.inflate(R.layout.plans_row, (ViewGroup) findViewById(R.id.plansRow));
					
					TableRow childRow = (TableRow) childLayout.findViewById(R.id.plansTableRow);
					TextView textName = (TextView) childRow.findViewById(R.id.textPlanName);
					TextView textValue = (TextView) childRow.findViewById(R.id.textPlanValue);
					TextView textFulfilled = (TextView) childRow.findViewById(R.id.textPlanFulfilled);
					TextView textPercent = (TextView) childRow.findViewById(R.id.textPlanPercent);
					
					childList.get(j).fillView(textName, R.id.textItemName, false);
					childList.get(j).fillView(textValue, R.id.textValue, false);
					childList.get(j).fillView(textFulfilled, R.id.textFulfilled, false);
					childList.get(j).fillView(textPercent, R.id.textPercent, false);
					
					childLayout.removeView(childRow);	
					salePlansPlacement.addView(childRow);
				}
			}			
		}

    }
    
    //------------------------------------------------------------------------------------
    public void updateUnitLabels()
    {
    	int msuLabelColor = displayInMSU ? 0xFF00FF00:0xFF000000;
    	int hryvnaLabelColor = displayInMSU ? 0xFF000000:0xFF00FF00;
    	
		textMSULabel.setTextColor(msuLabelColor);
		textHryvnaLabel.setTextColor(hryvnaLabelColor);
    }
    
    //------------------------------------------------------------------------------------
    /**
     * 
     * @param salaryTable - parent table
     * @param salaryRate - saler salary rate
     * @return Double array {monthSalaryBonus, daySalaryBonus}
     */
    private Double[] initSalaryRows(TableLayout salaryTable, Double salaryRate)
    {    	
    	String sql = Q.getSalarySumSql(salaryRate);    	
        Cursor cursor = Db.getInstance().selectSQL(sql);
        
        Double[] result = {0.0, 0.0};
        
        String currencyName = getResources().getString(R.string.currency_name);
        
		if(cursor != null && cursor.getCount() > 0)
		{				
			int idx = cursor.getColumnIndex("Name");
			int idx2 = cursor.getColumnIndex("mZP");
			int idx3 = cursor.getColumnIndex("dZP");
			
			for(int i = 0; i < cursor.getCount(); i++)
			{
				cursor.moveToPosition(i);													
				if (!cursor.isNull(idx))
				{
			    	LayoutInflater inflater = getLayoutInflater();
					LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.report_day_summaries_salary_row, (ViewGroup) findViewById(R.id.salaryRow));
			    	TableRow tr = (TableRow)layout.findViewById(R.id.salaryTableRow);
			    	
			    	TextView tvTitle = (TextView)tr.findViewById(R.id.salary_sum_title);
			    	TextView tvMonthSalary = (TextView)tr.findViewById(R.id.salary_sum_month);
			    	TextView tvDaySalary = (TextView)tr.findViewById(R.id.salary_sum_day);
			    	
			    	tvTitle.setText(cursor.getString(idx));
			    	
			    	Double monthSalaryByGroup = cursor.getDouble(idx2);
			    	Double daySalaryByGroup = cursor.getDouble(idx3);
			    	
			    	result[0] = result[0] + monthSalaryByGroup;
			    	result[1] = result[1] + daySalaryByGroup;
			    			
			    	tvMonthSalary.setText(String.format("%.2f", monthSalaryByGroup) + " " + currencyName);
			    	tvDaySalary.setText(String.format("%.2f", daySalaryByGroup) + " " + currencyName);
			    	
			    	layout.removeView(tr);			    	
			    	salaryTable.addView(tr, 0);
			    	tr.setBackgroundColor(Color.WHITE);
				}
			}
			
			//add column names
			LayoutInflater inflater = getLayoutInflater();
			LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.report_day_summaries_salary_row, (ViewGroup) findViewById(R.id.salaryRow));
	    	TableRow tr = (TableRow)layout.findViewById(R.id.salaryTableRow);
	    	
	    	TextView tvTitle = (TextView)tr.findViewById(R.id.salary_sum_title);
	    	TextView tvMonthSalary = (TextView)tr.findViewById(R.id.salary_sum_month);
	    	TextView tvDaySalary = (TextView)tr.findViewById(R.id.salary_sum_day);
	    		    		    			
	    	tvTitle.setText("");
	    	tvMonthSalary.setText(getString(R.string.report_day_summaries_salary_month_column_label));
	    	tvDaySalary.setText(getString(R.string.report_day_summaries_salary_day_column_label));
	    	
	    	layout.removeView(tr);	    	
	    	salaryTable.addView(tr, 0);
	    	tr.setBackgroundColor(Color.rgb(230, 230, 230));
		}
		return result;
    }
    
    //--------------------------------------------------------------
    public class ItemGroup implements IGroupItem, IChildItem 
    {
    	public long id;
        public String name;
        public long orders;
        public String sum;
        public double MSU;
        
        public ItemGroup(long id, String name, long orders, String sum, double MSU) 
        {
        	this.id = id;
            this.name = name;
            this.orders = orders;
            this.sum = sum;
            this.MSU = MSU;
        }
        
        public Long getID() { return id; }
        
        public boolean isEnabled() { return true; }
        
    	public void fillView(View view, int viewId, boolean isExpanded)
    	{
    		if(viewId == R.id.textGroupName && view instanceof TextView)
    			((TextView) view).setText(name);
    		if(viewId == R.id.textOrders && view instanceof TextView)
    			((TextView) view).setText(((Long)orders).toString());
    		if(viewId == R.id.textSum && view instanceof TextView)
    			((TextView) view).setText(sum);
    		if(viewId == R.id.textMSU && view instanceof TextView)
    			((TextView) view).setText(Convert.msuToString(MSU));    		
    		if(viewId == R.id.textPadding )
    			view.setVisibility(View.VISIBLE);
    		
    		if(viewId != R.id.textPadding)
    		{
    			view.setTag(this);
    			
	            view.setOnClickListener(
			            new View.OnClickListener() 
			    		{	
			    			@Override public void onClick(View v) 
			    			{
			    				currentItemGroup = (ItemGroup) v.getTag();
			    				
			    				showDialog(IDD_REPORT_ITEM_SUMMARIES);			    				
			    			}
			    		});
    		}
    	}    	
    }

    //--------------------------------------------------------------
    private void fillPlans()
    {
	    //---------------------------- visit plans
		TableLayout visitPlansPlacement =  (TableLayout) findViewById(R.id.visitPlansTable);
		
		if(!isVisitSummaries)
		{
			addPlanHeaderRow(this, visitPlansPlacement);
			
			addPlanRow(this, visitPlansPlacement, getResources().getString(R.string.report_day_summaries_visit_count), 
											((Long)plannedVisitCount).toString(), ((Long)visitCount).toString(), 
											plannedVisitCount!=0 ? String.format("%s%%",Convert.moneyToString(100*visitCount/plannedVisitCount)):"--",
											true, Color.WHITE	);
			addPlanRow(this, visitPlansPlacement, getResources().getString(R.string.report_day_summaries_effective_visit_count), 
										((Long)plannedVisitCount).toString(), ((Long)effectiveVisitCount).toString(), 
										plannedVisitCount!=0 ? String.format("%s%%",Convert.moneyToString(100*effectiveVisitCount/plannedVisitCount)):"--",
										true, Color.WHITE );
			//golden plan			
			Long generalAddrID = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_ADDR_PLAN_TOTAL, 199051);			
			PlanItem planGold = Plans.getPlanValues(generalAddrID, Plans.PLAN_TYPE_GOLD_PROGRAMM, false, Plans.PCS_UNIT_ID, true);						
			addPlanRow(this, visitPlansPlacement, getResources().getString(R.string.report_day_summaries_golden), planGold);
		}

	    //---------------------------- ����� �� ������������
		TableLayout plansPlacement =  (TableLayout) findViewById(R.id.plansTable);
		
		if(fromVisit)
		{
			addPlanHeaderRow(this, plansPlacement);
						
			//������� �������
			PlanItem planNovelty = Plans.getPlanValues(addrId, Plans.PLAN_TYPE_NOVELTY, false, Plans.SKU_UNIT_ID, true);			
			String  sqlFactNovelty = Q.getPlanFact(Plans.PLAN_TYPE_NOVELTY, fromVisit, false, 0);
			Long factNovelty = Db.getInstance().getDataLongValue(sqlFactNovelty, 0);
			planNovelty.fact += factNovelty;
			addPlanRow(this, plansPlacement, getResources().getString(R.string.report_day_summaries_novelty), planNovelty);
			
			//powerSKU 
			PlanItem planPowerSKU = Plans.getPlanValues(addrId, Plans.PLAN_TYPE_MONTH_POWER_SKU, false, Plans.SKU_UNIT_ID, true);			
			String  sqlFactPowSKU = Q.getPlanFact(Plans.PLAN_TYPE_MONTH_POWER_SKU, fromVisit, false, 0);
			Long factPSKU = Db.getInstance().getDataLongValue(sqlFactPowSKU, 0);
			planPowerSKU.fact += factPSKU;
			addPlanRow(this, plansPlacement, getResources().getString(R.string.report_day_summaries_powerSKU), planPowerSKU);			
						
			//����� �� ���
			PlanItem planGeneralDistr = Plans.getPlanValues(addrId, Plans.PLAN_TYPE_MONTH_DISTRIBUTION, false, Plans.SKU_UNIT_ID, true);						
			String  sqlGeneralDistr = Q.getPlanFact(Plans.PLAN_TYPE_DAY_DISTRIBUTION, fromVisit, false, 0);
			Long factGeneralDistr = Db.getInstance().getDataLongValue(sqlGeneralDistr, 0);
			planGeneralDistr.fact += factGeneralDistr;
			addPlanRow(this, plansPlacement, getResources().getString(R.string.report_day_summaries_general_distr_month), planGeneralDistr);
					
		}			
		else
		{
			//---------------------------- ����� �� ������������
			long totalsAddrID = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_ADDR_PLAN_TOTAL, 199051);
			PlanItem planSaturabilityMonth = Plans.getPlanValues(totalsAddrID, Plans.PLAN_TYPE_MONTH_SATURABILITY, false, Plans.SKU_UNIT_ID, true);
			PlanItem planSaturabilityDay = Plans.getPlanValues(totalsAddrID, Plans.PLAN_TYPE_DAY_SATURABILITY, true, Plans.SKU_UNIT_ID, false);
			
			String sql = Q.getPlanFact(Plans.PLAN_TYPE_DAY_SATURABILITY, fromVisit, false, 0);
			planSaturabilityDay.fact = Db.getInstance().getDataLongValue(sql, 0);
			planSaturabilityMonth.fact += planSaturabilityDay.fact;
			
			String nameSaturabilityMonth = getResources().getString(R.string.report_day_summaries_filling_month);
			String nameSaturabilityDay = getResources().getString(R.string.report_day_summaries_filling_day);
	
			addPlanHeaderRow(this, plansPlacement);
			addPlanRow(this, plansPlacement, nameSaturabilityMonth, planSaturabilityMonth);
			addPlanRow(this, plansPlacement, nameSaturabilityDay, planSaturabilityDay);
		}		
		//add empty line just as separator
		//addSeparator(plansPlacement);
		
	    //---------------------------- sale plans
		salePlansPlacement = (TableLayout) findViewById(R.id.salePlansTable);
		
		//switch between displayed units
        ViewGroup switchUnits = (ViewGroup) findViewById(R.id.salePlansSwitchUnit);
        switchUnits.setOnClickListener( new ViewGroup.OnClickListener() 
		{			
			@Override public void onClick(View v) 
			{
				try
				{
					displayInMSU = !displayInMSU;
					updateUnitLabels();
					
					salePlansPlacement.removeAllViews();
					fillSalePlans();
				}
				catch(Exception ex)
				{
					ErrorHandler.CatchError("Exception in switchUnits.onClick", ex);			
				}
			}
		});        
        updateUnitLabels();
        
        Button buttonPlanDetails = (Button) findViewById(R.id.buttonPlanDetails);
        if(isVisitSummaries || isVisitTasks )
        {
        	buttonPlanDetails.setVisibility(View.GONE);
        }
        else
        {
	        buttonPlanDetails.setOnClickListener( new View.OnClickListener() 
			{			
				@Override public void onClick(View v) 
				{	 
			    	try
			    	{
			    		showDialog(IDD_PLAN_DETAILS_DIALOG);
			    	}
					catch(Exception ex)
					{
						ErrorHandler.CatchError("Exception in ReportDaySummaries.buttonPlanDetails.onClick", ex);
					}					
				}
			});			
        }
				
        fillSalePlans();
    }

    //--------------------------------------------------------------
    private void fillItemGroups()
    {
    	if(itemGroupsAdapter==null)
    	{
	    	String sql = Q.getTodaySaleSums(true, 0, isVisitSummaries, isVisitSummaries? addrId:0, false, 0);	    	
	    	Cursor groupsCursor = Db.getInstance().selectSQL(sql);
	    	
			ArrayList<IGroupItem> groups = new ArrayList<IGroupItem>();		
			Map<Long, ArrayList<IChildItem> > childGroupMap = new HashMap< Long, ArrayList<IChildItem> > ();
	
			int idxItemGroupID = groupsCursor.getColumnIndex("ItemGroupID");
			int idxParentGroupID = groupsCursor.getColumnIndex("ParentGroupID");
			int idxItemGroupName = groupsCursor.getColumnIndex("ItemGroupName");
			int idxItemGroupOrders = groupsCursor.getColumnIndex("Orders");
			int idxItemGroupSum = groupsCursor.getColumnIndex("Sum");
			int idxItemGroupType = groupsCursor.getColumnIndex("GroupType");
			int idxParentGroupType = groupsCursor.getColumnIndex("ParentGroupType");
			int idxMSU = groupsCursor.getColumnIndex("MSU");
			
			for(int i=0; i<groupsCursor.getCount(); i++)
			{	
				groupsCursor.moveToPosition(i);			
				long itemGroupID = groupsCursor.getInt(idxItemGroupID);			
				String itemGroupName = groupsCursor.getString(idxItemGroupName);
				long orders = groupsCursor.getInt(idxItemGroupOrders);
				String sum = groupsCursor.getString(idxItemGroupSum);
				int parentGroupType = groupsCursor.getInt(idxParentGroupType);
				int itemGroupType = groupsCursor.getInt(idxItemGroupType);
				double MSU = groupsCursor.getDouble(idxMSU);
			
				if(groupsCursor.isNull(idxParentGroupID) || parentGroupType == DocSaleSelectGroupDialog.GROUP_TYPE_DOC_STEP )
				{
					//group, parent is null
					if( itemGroupType != DocSaleSelectGroupDialog.GROUP_TYPE_DOC_STEP)
						groups.add(new ItemGroup(itemGroupID, itemGroupName, orders, sum, MSU));
				}
				else
				{
					//child, parent is not null
					long parentGroupID = groupsCursor.getLong(idxParentGroupID);
					
					if(childGroupMap.get(parentGroupID)==null)
					{
						ArrayList<IChildItem> children = new ArrayList<IChildItem>();				
						childGroupMap.put(parentGroupID, children);
					}				
					
					if(childGroupMap.get(parentGroupID) !=null)
						childGroupMap.get(parentGroupID).add(new ItemGroup(itemGroupID, itemGroupName, orders, sum, MSU));					
				}			
			}//for
			
			if(groupsCursor!=null)
				groupsCursor.close();
			
			//create adapter
			int[] groupViewIds = new int[] { R.id.textPadding, R.id.textGroupName, R.id.textOrders,	
											R.id.textOrdersSuffix, R.id.textSum, R.id.textSumSuffix, R.id.textMSU };
			
			itemGroupsAdapter = new ExpandableAdapterForArray(this, groups, childGroupMap, 
									R.layout.report_day_summaries_list_item,	R.layout.report_day_summaries_list_item, groupViewIds, groupViewIds);
    	}	
		
		groupExpandableList.setAdapter(itemGroupsAdapter);		
    }
    
    //---------------------- pop-up dialog with items summaries ----------------------------------------
    public class ItemSummariesDialog extends DialogBase
    {
    	ListView itemList;
    	TextView textDialogTitle;
    	
    	public Dialog onCreate(Context context)
    	{
    		try
    		{
	    		String title = String.format("%s \"%s\"", getResources().getString(R.string.report_day_summaries_itemFormTitle), currentItemGroup.name);
	    		
	    		Dialog dlg = super.onCreate(context, R.layout.report_day_summaries_item_form, R.id.reportDaySummariesItemForm, 
	    							 				title, DIALOG_FLAG_HAVE_OK_BUTTON | DIALOG_FLAG_NO_TITLE);
	    		
	    		itemList = (ListView) findViewById(R.id.itemSummariesList);
	    		textDialogTitle = (TextView) findViewById(R.id.textDialogTitle);
	    		
	    		textDialogTitle.setText(title);
	    		
	    		fill(context);
	    		
	    		return dlg;
	    	}
			catch(Exception ex)
			{
		    	MessageBox.show(ReportDaySummariesForm.this, getResources().getString(R.string.form_title_report_day_summaries), getResources().getString(R.string.dialog_base_exceptionOnCreateDialog));
				ErrorHandler.CatchError("Exception in ItemSummariesDialog.onCreate", ex);
			}    		
    		
    		return null;
    	}
    	
    	private void fill( Context context)
    	{
    		//
    		//create cursor, adapter and set adapter to ListView
    		//
        	String sql = Q.getTodaySaleSums(false, currentItemGroup.id, isVisitSummaries, isVisitSummaries? addrId:0, false, 0);        	
        	Cursor itemsCursor = Db.getInstance().selectSQL(sql);        	
    		((Activity) context).startManagingCursor(itemsCursor);
    		
            String[] from = new String[] { "ScreenName", "Orders", "Sum", "MSU" }; 
            int[] to = new int[] { R.id.textGroupName, R.id.textOrders, R.id.textSum, R.id.textMSU };            
            SimpleCursorAdapter pricesAdapter = new SimpleCursorAdapter(context, R.layout.report_day_summaries_list_item, itemsCursor, from, to);
            itemList.setAdapter(pricesAdapter);    		
    	}
    }    
    
    //--------------------------------------------------------------    
    @Override protected Dialog onCreateDialog(int id)
    {
    	try
    	{
	    	switch(id)
	    	{
				case IDD_REPORT_ITEM_SUMMARIES:
				{
					ItemSummariesDialog itemSummariesDialog = new ItemSummariesDialog();
					Dialog dlg = itemSummariesDialog.onCreate(this);	
					itemSummariesDialog.setOkClickListener(new DialogInterface.OnClickListener() 
	    			{					
	    				@Override public void onClick(DialogInterface dialog, int which) 
	    				{
	    					removeDialog(IDD_REPORT_ITEM_SUMMARIES);
	    				}
	    			}); 
					return dlg;
				}
				case IDD_PLAN_DETAILS_DIALOG:
				{
					PlanDetailsDialog planDetailsDialog = new PlanDetailsDialog();
					Dialog dlg = planDetailsDialog.onCreate(this);	
					planDetailsDialog.setOkClickListener(new DialogInterface.OnClickListener() 
	    			{					
	    				@Override public void onClick(DialogInterface dialog, int which) 
	    				{
	    					removeDialog(IDD_PLAN_DETAILS_DIALOG);
	    				}
	    			});
					return dlg;
				}					
	    	}
    	}
		catch(Exception ex)
		{
	    	MessageBox.show(this, getResources().getString(R.string.form_title_report_day_summaries), getResources().getString(R.string.dialog_base_exceptionOnCreateDialog));
			ErrorHandler.CatchError("Exception in ReportDaySummariesForm.onCreateDialog", ex);
		}
    	
		return null;
    }
    
    //--------------------------------------------------------------
    private void fillDocListTab()
    {
    	
        //
        // fill doc types spinner
        //
        final String[] docTypeNames = new String[] 
	    {
    		getResources().getString(R.string.report_day_summaries_docTypeAll),
    		getResources().getString(R.string.document_docType_claim),
    		getResources().getString(R.string.document_docType_sale),
    		getResources().getString(R.string.document_docType_payment),
    		getResources().getString(R.string.document_docType_remnants),
	    };

        spnDocTypes = (Spinner) findViewById(R.id.spnDocTypes);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
        						R.layout.spinner_item /*android.R.layout.simple_spinner_item*/, docTypeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDocTypes.setAdapter(adapter);
        
        spnDocTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3)
			{				
				try
				{
					char newDocTypeFilter = Document.DOC_TYPE_UNKNOWN;
					switch(position)
					{
						case 0:
							newDocTypeFilter = Document.DOC_TYPE_UNKNOWN;
							break;
						case 1:	
							newDocTypeFilter = Document.DOC_TYPE_CLAIM;
							break;
						case 2:	
							newDocTypeFilter = Document.DOC_TYPE_SALE;
							break;
						case 3:	
							newDocTypeFilter = Document.DOC_TYPE_PAYMENT;
							break;
						case 4:	
							newDocTypeFilter = Document.DOC_TYPE_REMNANTS;
							break;
					}
				
					if(newDocTypeFilter!=docTypeFilter)
					{
						docTypeFilter = newDocTypeFilter;
						fillDocListTabWithData();
						
					}
				}
				catch(Exception ex)
				{
					ErrorHandler.CatchError("Exception in ReportDaySummariesForm::fillDocListTab", ex);
				}
			}

			@Override public void onNothingSelected(AdapterView<?> arg0)
			{			
			}
		});

        // totals
    	//viewGroupDocumentSummariesTotals
        viewDocTotals = (ViewGroup) findViewById(R.id.viewGroupDocumentSummariesTotals); 
    	textDocsOrders = (TextView) findViewById(R.id.textDocsOrders);
    	textDocsMSU = (TextView) findViewById(R.id.textDocsMSU);
    	textDocsSum = (TextView) findViewById(R.id.textDocsSum);
        
    	//
    	// fill grid
    	//
    	docListGrid = (DataGrid) findViewById(R.id.dataGridDocList);  
        
        DataGrid.ColumnInfo[] columns = new DataGrid.ColumnInfo[]
        {
        		new DataGrid.ColumnInfo(1, "DocID", DataGrid.DATA_TYPE_INTEGER, 50, DataGrid.GRID_COLUMN_HIDDEN, "DocID", 0, (short)-1, ""),
        		new DataGrid.ColumnInfo(2, getResources().getString(R.string.doc_list_column_header_docType), DataGrid.DATA_TYPE_VARCHAR, 100, DataGrid.GRID_COLUMN_DEFAULT, "ReadableDocType", 1, (short)-1, ""),
        		new DataGrid.ColumnInfo(3, getResources().getString(R.string.doc_list_column_header_client), DataGrid.DATA_TYPE_VARCHAR, 40, DataGrid.GRID_COLUMN_DEFAULT, "ClientName", 2, (short)-1, ""),
        		new DataGrid.ColumnInfo(4, getResources().getString(R.string.doc_list_column_header_address), DataGrid.DATA_TYPE_VARCHAR, 40, DataGrid.GRID_COLUMN_DEFAULT, "AddrName", 3, (short)-1, ""),
        		new DataGrid.ColumnInfo(5, getResources().getString(R.string.doc_list_column_header_sumWithVAT), DataGrid.DATA_TYPE_VARCHAR, 60, DataGrid.GRID_COLUMN_DEFAULT, "SumAll", 4, (short)-1, ""),
        		new DataGrid.ColumnInfo(6, getResources().getString(R.string.doc_list_column_header_sumWoVAT), DataGrid.DATA_TYPE_VARCHAR, 60, DataGrid.GRID_COLUMN_DEFAULT, "SumWOVAT", 5, (short)-1, ""),
        		new DataGrid.ColumnInfo(7, getResources().getString(R.string.doc_list_column_header_docNumber), DataGrid.DATA_TYPE_VARCHAR, 40, DataGrid.GRID_COLUMN_DEFAULT, "DocNumber", 6, (short)-1, ""),
        		new DataGrid.ColumnInfo(8, getResources().getString(R.string.doc_list_column_header_date), DataGrid.DATA_TYPE_DATE, 90, DataGrid.GRID_COLUMN_DEFAULT, "DocDate", 7, (short)-1, ""),
        		new DataGrid.ColumnInfo(9, getResources().getString(R.string.doc_list_column_header_created), DataGrid.DATA_TYPE_DATE, 90, DataGrid.GRID_COLUMN_DEFAULT, "CreateDate", 8, (short)-1, ""),
        		new DataGrid.ColumnInfo(10, getResources().getString(R.string.doc_list_column_header_comment), DataGrid.DATA_TYPE_VARCHAR, 40, DataGrid.GRID_COLUMN_DEFAULT, "Comment", 9, (short)-1, ""),
        		new DataGrid.ColumnInfo(11, getResources().getString(R.string.doc_list_column_header_salerInfo), DataGrid.DATA_TYPE_VARCHAR, 100, DataGrid.GRID_COLUMN_DEFAULT, "SalerName", 10, (short)-1, ""),
        		new DataGrid.ColumnInfo(12, getResources().getString(R.string.doc_list_column_header_docState), DataGrid.DATA_TYPE_VARCHAR, 100, DataGrid.GRID_COLUMN_DEFAULT, "ReadableDocState", 11, (short)-1, "")
        };
        GridColumns gridColumns = new GridColumns(columns); 
        
        //int columnsSetId = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_COLUMNS_SET_ID_DOC_LIST, 3);        
        //GridColumns gridColumns = new GridColumns(columnsSetId);        
        docListGrid.setColumns(gridColumns);

        docListGrid.setCellListener(new CellListener());
        docListGrid.setDefaultSortColumnByDbField("DocDate");
        
        docListGrid.setSortOrder(DataGrid.SortOrder.ASCENDING);
        docListGrid.setIdentityColumn(0); // TODO maybe move to settings       
        
        //get grid row height from settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int gridRowHeight = Convert.toInt(prefs.getString(getString(R.string.preferences_grid_row_height_key), "40"), 40);        
        docListGrid.setRowHeight(gridRowHeight);
                
        fillDocListTabWithData();


    }
    
    //--------------------------------------------------------------    
    private void fillDocListTabWithData()
    {
    	//
    	// Fill Grid    	
    	//
    	
    	String docTypeField = Document.getReadableDocTypes(this, "d");    	
    	String docStateField = Document.getReadableDocStates(this, "d");
    	//where
    	String docStateFilter = String.format( " d.State!='%s' AND d.State!='%s'", Document.DOC_STATE_PREVIOUS, Document.DOC_STATE_DELETED );
    	/*String where = String.format(" WHERE ClientID=%d AND AddrID=%d", AntContext.getInstance().getClientID(), AntContext.getInstance().getAddrID())
    									 + " AND " + docStateFilter;*/
    	String docAddrFilter = isVisitSummaries ? String.format(" AND d.AddrID=%d ",addrId):"";
    	String where = "WHERE " + docStateFilter + " AND d.CreateDate " + Q.getSqlBetweenDayStartAndEnd(todayDate) + docAddrFilter;
    	
    	if(docTypeFilter!=Document.DOC_TYPE_UNKNOWN)
    	{
    		where = where + String.format(" AND d.DocType='%s'", docTypeFilter);
    	}
    	
    	// sorting
    	String sortColumn = docListGrid.getDbFieldForColumn(docListGrid.getSortColumn());
    	String sortOrder = (docListGrid.getSortOrder() == DataGrid.SortOrder.ASCENDING)? "ASC": "DESC";
    	String orderBy = " ORDER BY " + sortColumn + " " + sortOrder;
    	//sql
    	String sql = "SELECT " + docTypeField + ", " + docStateField + ", d.DocType, d.State, d.DocID," +    	
    						" c.NameScreen AS ClientName, a.AddrName, d.SumAll, d.SumWOVAT, " +
    						" d.DocNumber, d.DocDate, d.CreateDate, " +
    						" (coalesce(d.Comments,'')||' '||coalesce(d.SpecMarks,'')) AS Comment, " + 
    						" d.SalerInfo, s.SalerName " +
    				 "FROM Documents d " +
    					 " LEFT JOIN Salers s ON d.SalerID = s.SalerID " + 
    				 	 " LEFT JOIN Clients c ON c.ClientID = d.ClientID " +
    				 	 " LEFT JOIN Addresses a ON a.AddrID = d.AddrID " +
    				  where +
    			      orderBy;
    	
    	docListCursor = Db.getInstance().selectSQL(sql);
    	startManagingCursor(docListCursor);
    	docListGrid.setCursor(docListCursor); 	
    	
    	//
    	// Fill totals
    	//
    	
    	if(docTypeFilter!=Document.DOC_TYPE_UNKNOWN)
    	{
			sql = " SELECT sum(dets.Orders) as Orders, sum(distinct d.SumAll) AS SumAll, " +
				Q.getMSU("curdets", "dets.Orders", true) +
				" FROM Documents d" +
				" LEFT JOIN DocDetails dets ON d.DocID = dets.DocID " +
				" LEFT JOIN CurDocDetails curdets ON dets.ItemID = curdets.ItemID AND curdets.DocID=0 " +
				where;
    	
			Cursor docSummariesCursor = Db.getInstance().selectSQL(sql);
			
			if(docSummariesCursor!=null)
			{
				if(docSummariesCursor.getCount()>0 && docSummariesCursor.moveToFirst())
				{
					long orders = docSummariesCursor.getLong(docSummariesCursor.getColumnIndex("Orders"));
					double MSU = docSummariesCursor.getDouble(docSummariesCursor.getColumnIndex("MSU"));
					double sum = docSummariesCursor.getDouble(docSummariesCursor.getColumnIndex("SumAll"));
					
					textDocsOrders.setText(((Long)orders).toString());
					textDocsMSU.setText( Convert.msuToString(MSU) );
					textDocsSum.setText( Convert.moneyToString(Convert.roundUpMoney(sum)) );
			
					viewDocTotals.setVisibility(View.VISIBLE);
				}
				docSummariesCursor.close();
			}
    	}
    	else
    	{
    		//hide totals
    		viewDocTotals.setVisibility(View.GONE);
    	}
    		
    }
    
    //--------------------------------------------------------------
    private class CellListener extends DataGrid.BaseCellListener 
    {
    	@Override public void onHeaderClicked(int column, boolean sortOrderChanged)
        {
        	try
        	{
        		//header click implies changes in sort order. we need to re-read Cursor and set it to DataGrid
        		fillDocListTabWithData();
        	}
    		catch(Exception ex)
    		{			
    			ErrorHandler.CatchError("Exception in ReportDaySummariesForm.onHeaderClicked", ex);
    		}    	        	
        }        
    } 
    
}

