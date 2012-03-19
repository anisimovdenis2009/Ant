package com.app.ant.app.ServiceLayer;

import com.app.ant.R;
import com.app.ant.app.Activities.ClientForm;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class StepControllerClient extends StepController
{
    //--------------------------------------------------------------
	public StepControllerClient()
	{
		addStep(new StepInfo(0, R.string.visit_step_client, R.drawable.step_client, STEP_INVISIBLE, false));
	}
	
    //--------------------------------------------------------------
	@Override public boolean startStep(Context context, int stepNum, boolean fromTabController, Bundle params)
	{		
		switch(stepNum)
		{
			case 0:				
				context.startActivity(new Intent(context, ClientForm.class));
				FinishPrevActivity(context);
				break;
		}
		
		return super.startStep(context, stepNum, fromTabController, params);
	}
}