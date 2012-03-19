package com.app.ant.app.Activities;

import com.app.ant.R;
import com.app.ant.app.ServiceLayer.ErrorHandler;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class PreferencesForm extends PreferenceActivity 
{
    //--------------------------------------------------------------
    @Override public void onCreate(Bundle savedInstanceState) 
    {
    	try
    	{
    		super.onCreate(savedInstanceState);        
    		addPreferencesFromResource(R.xml.preferences);    		    		
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in preferencesForm", ex);
		}  	    	    	
    }      
}
