package com.app.ant.app.Activities;

import java.util.List;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.widget.TableLayout;
import com.app.ant.R;
import com.app.ant.app.BusinessLayer.Plans;
import com.app.ant.app.BusinessLayer.Plans.PlanItem;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.ErrorHandler;

public class QualificationProgressDialog extends DialogBase
{
    private Context context;
    
	//--------------------------------------------------------------	
	public Dialog onCreate(final Context context, long addrID, List<PlanItem> progressItems)
    {
    	try
    	{
    		this.context = context;
    		
    		String title = context.getResources().getString(R.string.qualification_progress_title);
			Dialog dlg = super.onCreate(context, R.layout.qualification_progress_dialog, R.id.qualificationProgress, 
 								title, DIALOG_FLAG_HAVE_OK_BUTTON | DIALOG_FLAG_NO_TITLE );   		
			
			if(progressItems.size()>0)
			{				
				TableLayout progressPlacement = (TableLayout) findViewById(R.id.progressTable);
				
				ReportDaySummariesForm.addPlanHeaderRow(context, progressPlacement);
				
				for(int i=0; i < progressItems.size(); i++)
				{
					String name = "";
					if (i == 0)
						name = context.getString(R.string.qualification_progress_OPD);
					else if (i == 1)
						name = context.getString(R.string.qualification_progress_PowerSKU);
					else if (i == 2) 
						name = context.getString(R.string.qualification_progress_ShelfShare);
							
					ReportDaySummariesForm.addPlanRow(context, progressPlacement, name, progressItems.get(i));
				}
				
			}
			
			if(addrID!=-1)
			{
				TableLayout targetPlacement = (TableLayout) findViewById(R.id.targetTable);

				String sql = " SELECT q.QuestionText AS QuestionText, coalesce(qt.TargetValue,0) AS TargetValue, coalesce(qt.PrevResult,0) AS PrevResult " +  
							 " FROM QuestionTargets qt " +
							 "		LEFT JOIN Questions q ON qt.QuestionID = q.QuestionID " + 
							 " WHERE " +
							 " 		coalesce(qt.PrevResult,-1) < coalesce(qt.TargetValue,0) " +
							 " 		AND qt.AddrID = " + addrID +
							 " 		AND qt.TargetValue is not null ";
					
		        Cursor cursor = Db.getInstance().selectSQL(sql);
		        
				if(cursor != null)
				{	
					
					//Log.d("Cursor.cnt=", "" + cursor.getCount());
					if(cursor.getCount() > 0)
					{
						int questionTextIdx = cursor.getColumnIndex("QuestionText");
						int targetValueIdx = cursor.getColumnIndex("TargetValue");
						int prevResultIdx = cursor.getColumnIndex("PrevResult");
						
						ReportDaySummariesForm.addPlanHeaderRow(context, targetPlacement);
						
						for(int i = 0; i < cursor.getCount(); i++)
						{
							cursor.moveToPosition(i);
							
							PlanItem planItem = new PlanItem();
							String name = cursor.getString(questionTextIdx);
							planItem.plan = cursor.getLong(targetValueIdx);
							planItem.fact = cursor.getLong(prevResultIdx);
							planItem.unitID = (Plans.PCS_UNIT_ID);
							
							ReportDaySummariesForm.addPlanRow(context, targetPlacement, name, planItem);
						}
					}
					cursor.close();
				}
			}
			
			return dlg;
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in QualificationProgressDialog.onCreate", ex);
		}
    	
    	return null;
    }   
}

