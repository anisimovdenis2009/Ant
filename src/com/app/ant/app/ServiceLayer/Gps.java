package com.app.ant.app.ServiceLayer;

import android.content.Context;
import android.location.LocationManager;


public class Gps 
{

	public static boolean needGPSWarning(Context context)
	{
        int gpsEnabled = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_GPS_ENABLE, 0);
        
        if(gpsEnabled > 0)
        {
        	// Check if GPS is enabled        
        	LocationManager locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);        	
            if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
            	return true;
            }
        }
        
        return false; 
		
	}
}
