package com.app.ant.app.ServiceLayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.app.ant.R;
import com.app.ant.app.Activities.AntActivity;
import com.app.ant.app.Activities.ClientForm;
import com.app.ant.app.StoreCheck.StoreCheckController;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 14.03.12
 * Time: 16:36
 * To change this template use File | Settings | File Templates.
 */
public class StepControllerStoreCheck extends StepController{
    public StepControllerStoreCheck()
    {
        addStep(new StepController.StepInfo(0, R.string.visit_step_client, R.drawable.step_client, STEP_INVISIBLE, false));
    }

    //--------------------------------------------------------------
    @Override public boolean startStep(Context context, int stepNum, boolean fromTabController, Bundle params)
    {
        switch(stepNum)
        {
            case 0:
                context.startActivity(new Intent(context, StoreCheckController.class));
                FinishPrevActivity(context);
                break;
        }

        return super.startStep(context, stepNum, fromTabController, params);
    }
}
