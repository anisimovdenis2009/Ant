package com.app.ant.app.ServiceLayer;

import com.app.ant.R;
import android.content.Context;
import com.app.ant.app.StoreCheck.StoreCheckController;


public class TabControllerVisit extends TabController
{
	public static final int TAB_CLIENT = 0;
	public static final int TAB_VISIT = 1;
	public static final int TAB_ANALYTICS = 2;
	public static final int TAB_DOC_LIST = 3;
	public static final int TAB_STORE_CHECK = 4;

    //--------------------------------------------------------------
	public	TabControllerVisit()
	{
	}

	//--------------------------------------------------------------
	public void addAddressTabs(Context context)
	{
		currentTab = addTab(context, TAB_VISIT, new StepControllerVisit(), R.string.tab_visit, TAB_FLAGS_MENU); 
		addTab(context, TAB_CLIENT, new StepControllerClient(), R.string.tab_address, TAB_FLAGS_DEFAULT);
		addTab(context, TAB_ANALYTICS, new StepControllerAnalytics(), R.string.tab_analytics, TAB_FLAGS_DEFAULT);
		addTab(context, TAB_DOC_LIST, new StepControllerDocList(), R.string.tab_docList, TAB_FLAGS_DEFAULT);		
		addTab(context, TAB_STORE_CHECK, new StepControllerStoreCheck(), R.string.tab_strore_check, TAB_FLAGS_DEFAULT);

		getStepController(currentTab).startStep(context, StepControllerVisit.VISIT_STEP_MENU, true, null);
	}
}