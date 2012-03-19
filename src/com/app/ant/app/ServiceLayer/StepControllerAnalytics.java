package com.app.ant.app.ServiceLayer;

import com.app.ant.R;
import com.app.ant.app.Activities.AnalyticsForm;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class StepControllerAnalytics extends StepController
{
    //--------------------------------------------------------------
	public	StepControllerAnalytics()
	{
		addStep(new StepInfo(0, R.string.client_addr_contacts, R.drawable.step_doc_header, STEP_INVISIBLE, false));
	}

	//--------------------------------------------------------------
	@Override public boolean startStep(Context context, int stepNum, boolean fromTabController, Bundle params)
	{		
		switch(stepNum)
		{
			case 0:
				context.startActivity(new Intent(context, AnalyticsForm.class));
				FinishPrevActivity(context);
				break;
		}
		
		return super.startStep(context, stepNum, fromTabController, params);
	}
	
}