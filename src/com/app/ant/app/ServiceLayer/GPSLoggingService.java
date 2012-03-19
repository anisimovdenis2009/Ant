package com.app.ant.app.ServiceLayer;

import com.app.ant.app.DataLayer.Db;
import android.location.GpsStatus;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;


public class GPSLoggingService extends Service 
{
    private LocationManager manager;
    private LocationListener listener;
    private GpsStatus.Listener statusListener; 
    
    final long MILLISECONDS_IN_MINUTE = 60000;
    private long minTime = 0;
    private float gpsDistance = 0;

    private int gpsStatus = 0;
    private IGPSStatusListener externalStatusListener;
    
    /** Callback: �������� � ���������� ��������� ���������*/
    public interface IGPSStatusListener 
    {
    	void onFinishLocationTracking(boolean bSuccess);
    }    
    
    //
    // Class for clients to access.  Because we know this service always
    // runs in the same process as its clients, we don't need to deal with
    // IPC.
    //
    public class GPSLoggingBinder extends Binder 
    {
    	GPSLoggingService getService() 
        {
            return GPSLoggingService.this;
        }
    }
    
    // This is the object that receives interactions from clients
    private final IBinder mBinder = new GPSLoggingBinder(); 
    

	public GPSLoggingService() 
	{
	}

	@Override public IBinder onBind(Intent intent) 
	{
		 return mBinder;
	}
	
	@Override public void onCreate() 
	{
		//
		// Subscribe to location updates
		//
		try
		{
			Log.d("GPS Logging ", "GPSLoggingService::onCreate");
			
	        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	        logLocation(location);
	
	        listener = new GPSLocationListener();
	        statusListener = new GPSStatusListener();

	        //minTime - time in milliseconds between location updates
	        //minDistance - distance in meters between location updates
	        gpsDistance = (float) Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_GPS_LOG_DISTANCE, 0);
	        int gpsInterval = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_GPS_LOG_INTERVAL, 10);
	        minTime = gpsInterval*60000; //try to save energy by increasing an interval between measures
	        
	        startLocationUpdates();	        
	        manager.addGpsStatusListener( statusListener );
	        
	        //
	        // track the battery level	        
	        //
	        BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() 
			  	{
					@Override	public void onReceive( Context context, Intent intent )
					{
						int level = intent.getIntExtra( "level", 0 );
						String message = String.format("battery level=%d", level);
						Log.d("GPS Logging ", message);
						MLog.WriteLog(MLog.LOG_TYPE_BATTERY_LEVEL, message);	
					}
			  	}; 
	        
	        registerReceiver( batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED) );
	        
		}
		catch(Exception ex)
		{
			ErrorHandler.CatchError("Exception in GPSLoggingService.onCreate", ex);
			Log.d("GPS Logging ", "Exception in GPSLoggingService::onCreate");
		}
	}
	
	public void onDestroy()
	{
		Log.d("GPS Logging ", "GPSLoggingService::onDestroy");
	}
	
    //--------------------------------------------------------------
	public void startLocationUpdates()
	{
		Log.d("GPS Logging ", "GPSLoggingService::startLocationUpdates");
		
		gpsStatus = 0;
        //manager.requestLocationUpdates(LocationManager.GPS_PROVIDER ,0L, gpsDistance, listener);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, 0, listener);		
	}
	
    //--------------------------------------------------------------
	public void stopLocationUpdates()
	{
		Log.d("GPS Logging ", "GPSLoggingService::stopLocationUpdates");
		
		gpsStatus = 0;
		manager.removeUpdates(listener);				
	}
	
    //--------------------------------------------------------------	
    public void registerStatusListener(IGPSStatusListener externalStatusListener)
    {
    	this.externalStatusListener = externalStatusListener;
    }
    
    //--------------------------------------------------------------    
    public void unRegisterStatusListener()
    {
    	this.externalStatusListener = null;
    }

    //--------------------------------------------------------------
	void logLocation(Location location)
	{
		if(location == null)
			return;
		
		//String message = String.format("lat=%f, long =%f", location.getLatitude(), location.getLongitude()); 
		//MLog.WriteLog(MLog.LOG_TYPE_DEBUG_TEXT, message);
		String message = String.format("coordinates lat=%f long=%f ", location.getLatitude(), location.getLongitude());
		Log.d("GPS Logging ", message);
		
		String sql = "INSERT INTO GpsLog (Lat, Long, LogTime) values (?, ?, datetime('now', 'localtime') )";
		Object[] bindArgs = new Object[] { location.getLatitude(), location.getLongitude()};		
		Db.getInstance().execSQL(sql, bindArgs);		
	}
   
    //--------------------------------------------------------------
    private class GPSLocationListener implements LocationListener
    {
        public void onLocationChanged(Location location) 
        {
        	try
        	{
        		logLocation(location);
        	}
        	catch(Exception ex)
        	{
        		ErrorHandler.CatchError("Exception in GPSLocationListener.onLocationChanged", ex);
        		Log.d("GPS Logging ", "Exception in GPSLoggingService::onLocationChanged");
        	}
        }

        public void onProviderDisabled(String provider) { }
        public void onProviderEnabled(String provider) { }
        public void onStatusChanged(String provider, int status, Bundle extras) { }
    }

    //--------------------------------------------------------------    
    private class GPSStatusListener implements GpsStatus.Listener 
    {
        @Override public void onGpsStatusChanged(int event) 
        {
        	try
        	{
	        	String eventName = "UNKNOWN";
	        	
	        	//preserve status=0 until we receive GPS_EVENT_STARTED event
	        	//this will assure that it wan't be affected by any event from previous gps session
	        	if(gpsStatus!=0)	  
	        		gpsStatus = event;
	        	
	            switch(event) 
	            {
		            case GpsStatus.GPS_EVENT_STARTED:
		            	gpsStatus = event;
		            	eventName = "GPS_EVENT_STARTED";
		                break;
		            case GpsStatus.GPS_EVENT_STOPPED:
		            	eventName = "GPS_EVENT_STOPPED";
		                break;
		            case GpsStatus.GPS_EVENT_FIRST_FIX:
		            	eventName = "GPS_EVENT_FIRST_FIX";
		            	if(externalStatusListener!=null)
		            		externalStatusListener.onFinishLocationTracking(true);
		                break;
		            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
		            	/*int nSatellites = 0;
		            	if(manager!=null)
		            	{
		            		GpsStatus status = manager.getGpsStatus(null);  
		            		if(status!=null)
		            		{
		            			nSatellites = status.getMaxSatellites();
		            		
		            			//Iterable<GpsSatellite> sats = status.getSatellites();
		            			//Iterator<GpsSatellite> it = sats.iterator();
		            			//while ( it.hasNext() )
		            			//{
		            			//	nSatellites++;
		            			//}
		                    } 	            		
		            	}
		            	eventName = String.format("GPS_EVENT_SATELLITE_STATUS Satellites:%d", nSatellites);*/
		            	
		            	eventName = "GPS_EVENT_SATELLITE_STATUS";
		                break;	            	
	            }
	            
	        	if(externalStatusListener!=null)
	        	{
	        		if( gpsStatus == GpsStatus.GPS_EVENT_STOPPED )        	
	        			externalStatusListener.onFinishLocationTracking(false);
	        	}
	            
	            Log.d("GPS Logging ", String.format("status event: %d %s", event, eventName));
        	}
        	catch(Exception ex)
        	{
        		ErrorHandler.CatchError("Exception in GPSLocationListener.onGpsStatusChanged", ex);
        		Log.d("GPS Logging ", "Exception in GPSLoggingService::onGpsStatusChanged");       		
        	}        	
        }    
    }

}