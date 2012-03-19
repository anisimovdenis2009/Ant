package com.app.ant.app.Activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.app.ant.R;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Settings;


public class PreferencesSyncForm extends PreferenceActivity 
{
    //--------------------------------------------------------------
    @Override public void onCreate(Bundle savedInstanceState) 
    {
    	try
    	{
    		super.onCreate(savedInstanceState);        
    		addPreferencesFromResource(R.xml.sync_preferences);    		    		
    		
    		initReadOnlyPreference(getResources().getString(R.string.preference_key_last_sync_date), " - not sync - ");
    		
    		initReadOnlyPreferenceInKb(getResources().getString(R.string.preference_key_last_sync_size_recieve ), " 0 Mb ");
    		initReadOnlyPreferenceInKb(getResources().getString(R.string.preference_key_last_sync_size_send), " 0 Mb ");
    		initReadOnlyPreferenceInKb(getResources().getString(R.string.preference_key_month_sync_size_recieve), " 0 Mb ");
    		initReadOnlyPreferenceInKb(getResources().getString(R.string.preference_key_month_sync_size_send), " 0 Mb ");    		
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in preferencesSyncForm", ex);
		}  	    	    	
    }
        
    private void initReadOnlyPreference(String key, String defValue)
    {    	
		Preference pref = findPreference(key);    		
		pref.setSummary(Settings.getInstance().getStringSyncPreference(key, defValue));
    }
        
    private void initReadOnlyPreferenceInKb(String key, String defValue)
    {		
    	Preference pref = findPreference(key);
		Double size = Convert.toDouble(Settings.getInstance().getStringSyncPreference(key, defValue), 0.0);
		size = Convert.roundUpMoney(size / 1024 / 1024);
		pref.setSummary(size.toString() + " Mb");
    }        
}
