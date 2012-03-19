package com.app.ant.app.Activities;

import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Gps;
import com.app.ant.app.ServiceLayer.StepController.StepPanelType;


public class VisitForm extends AntActivity 
{
	
    //--------------------------------------------------------------	
    @Override public void onCreate(Bundle savedInstanceState) 
    {
    	/*try
    	{*/
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        overridePendingTransition(0,0);
	        setContentView(R.layout.visit);
	        
			TextView textClientName = (TextView) findViewById(R.id.textClientName);
			TextView textAddrName = (TextView) findViewById(R.id.textAddrName);
			textClientName.setText(AntContext.getInstance().getClient().nameScreen);
			textAddrName.setText(AntContext.getInstance().getAddress().addrName);
			
	        InitStepBar();
	        
	    	if(Gps.needGPSWarning(this))
	        {
    			MessageBox.show(this, getResources().getString(R.string.gps_warning), getResources().getString(R.string.gps_warning_text));	    		
	        }
    /*	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in VisitForm", ex);
		}   */
    }    
    
    //--------------------------------------------------------------    
    private void InitStepBar()
    {
    	//init steps
    	ViewGroup stepButtonPlacement = (ViewGroup) findViewById(R.id.stepButtonPlacement);
    	AntContext.getInstance().getStepController().CreateButtons(this, stepButtonPlacement, StepPanelType.VERTICAL);
    	
    	//init tabs
    	ViewGroup tabsPlacement = (ViewGroup) findViewById(R.id.tabsPlacement);
    	AntContext.getInstance().getTabController().createTabs(this, tabsPlacement);
    }
    
    //--------------------------------------------------------------
    @Override public void onBackPressed() 
    {
    	try
    	{
    		AntContext.getInstance().getTabController().onBackPressed(this);
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in clientForm.onBackPressed", ex);
		}   	
    }
    
}

