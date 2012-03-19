package com.app.ant.app.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.*;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.Activities.MessageBox.MessageBoxButton;
import com.app.ant.app.DataLayer.DataBaseHelper;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.*;
import com.app.ant.app.ServiceLayer.Settings;

import java.io.IOException;
import java.io.InputStream;


public class LoginForm extends AntActivity 
{
	private final static int IDD_LOGIN_POPUP = 1;
	//private ComponentName gpsService = null;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {           
    	try
    	{
	        super.onCreate(savedInstanceState);
            String provider = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

            if(!provider.contains("gps")){ //if gps is disabled
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                sendBroadcast(poke);
            }
	        setContentView(R.layout.login); //login window	        
	        AntContext.getInstance().setContext(getApplicationContext()); //global accessible app context

	        CheckAndCopyDatabase();
	        	        
	        if(startGPS() == true) //start GPS logging if enabled
	        	showDialog(IDD_LOGIN_POPUP);
	        
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in loginForm.onCreate", ex);
		}  	
    }
    
    //--------------------------------------------------------------
    protected boolean startGPS()
    {
    	//
    	// Check if GPS is enabled
    	//
    	try
    	{	    	
	    	if(Gps.needGPSWarning(this))
	        {        	
	    		  MessageBoxButton[] buttons = new MessageBoxButton[]
	              {
	  					new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.gps_warning_ok),
								new DialogInterface.OnClickListener()
								{
									@Override public void onClick(DialogInterface dialog, int which) 
									{
										Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
										startActivityForResult(gpsOptionsIntent, 0);
										//LoginForm.this.finish();
									}
								}),
	  					new MessageBoxButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.gps_warning_cancel),
								new DialogInterface.OnClickListener()
								{
									@Override public void onClick(DialogInterface dialog, int which) 
									{ 
										LoginForm.this.finish();
										onAppExit();
									}
								})								
	              };
	    		  
	    		  DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener()
	  			  {
						@Override public void onCancel(DialogInterface arg0) 
						{
							finish();
						}    				
	  			  };        		  
	    		                                       
	       		  MessageBox.show(this, getResources().getString(R.string.gps_warning),	getResources().getString(R.string.gps_warning_text), buttons, cancelListener, null, 0, null);
	       		  return false;
	        	
	        }  
	            
	        int gpsEnabled = 1; //Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_GPS_ENABLE, 0);
	        
	        if(gpsEnabled > 0)
	        {
		        //ComponentName comp = new ComponentName(getPackageName(),  GPSLoggingService.class.getName());
		        //gpsService = startService(new Intent().setComponent(comp));
	        	startService(new Intent(this, GPSLoggingService.class));
	        }
    	}
    	catch(Exception ex)
    	{
    		MessageBox.show(this, "Start GPS", ex.getLocalizedMessage());
    	}    	
        return true;
    }
    
    //--------------------------------------------------------------    
    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) 
    {     
      super.onActivityResult(requestCode, resultCode, data);      

      //start GPS logging if enabled
      if(startGPS())	        
    	  showDialog(IDD_LOGIN_POPUP);      
    }  
    
    //--------------------------------------------------------------    
    @Override protected Dialog onCreateDialog(int id)
    {
    	switch(id)
    	{
    		case IDD_LOGIN_POPUP:
    		{
    			LayoutInflater inflater = getLayoutInflater();
    			View layout = inflater.inflate(R.layout.loginpopup, (ViewGroup)findViewById(R.id.LoginPopUp));
   			
    			TextView saler_info = (TextView)layout.findViewById(R.id.login_saler);
    			saler_info.setText(Settings.getInstance().getPropertyFromSettings(Settings.PROPNAME_SALER_NAME, getResources().getString(R.string.login_sample_saler)));    			
    			
    			TextView app_info = (TextView)layout.findViewById(R.id.login_app_info);    			
    			app_info.setText(Api.getVersionName());
    			
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			
    			builder.setView(layout);
    			
    			builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() 
    			{					
					@Override public void onClick(DialogInterface dialog, int which) 
					{			
						removeDialog(IDD_LOGIN_POPUP);
						startActivity(new Intent(LoginForm.this, ClientListForm.class));
						finish();
					}
				});
				
    			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
    			{					
					@Override public void onClick(DialogInterface dialog, int which) 
					{
						removeDialog(IDD_LOGIN_POPUP);
						onAppExit();						
					}
				});
    			
    			builder.setOnCancelListener( new DialogInterface.OnCancelListener()
    			{
					@Override public void onCancel(DialogInterface arg0) 
					{
						onAppExit();
					}    				
    			});
    			
    			return builder.create();
    		}	
    		default:
    			return null;
    	}
    }    

    //--------------------------------------------------------------
    private void CheckAndCopyDatabase()
    {
    	boolean dbAlreadyExist = false;
    	
    	{
    		DataBaseHelper myDbHelper = new DataBaseHelper(this);
 
	        try 
	        {
	        	dbAlreadyExist = myDbHelper.createDataBase();
	        } 
	        catch (IOException ioe) 
	        { 
	        	MessageBox.show(this, "Ant database", "Error initializing database");
	        }
    	} 
       
    	if(!dbAlreadyExist)
    	{
        	try 
        	{
            	InputStream inputStream = getResources().openRawResource(R.raw.ant);        		
            	Synchronizer synchronizer = new Synchronizer(LoginForm.this);
            	synchronizer.copyDataBase(inputStream);
            	synchronizer = null;
            	inputStream.close();
        		
            	MessageBox.show(this, "Ant database", "Database is installed from app resources");                	
    		} 
        	catch (IOException ex) 
        	{
        		MessageBox.show(this, "Ant database", "Error copying new database " + ex.getLocalizedMessage());
        	}    		
    	}        
    	try
    	{
    		if(!Db.getInstance().getDbVersion().equals(Api.getVersionName()))
    		{
    			String msg =  getResources().getString(R.string.sync_db_version_is_falied);
    			MessageBox.show(this, "Ant database", msg);
    		}
    	}
    	catch(Exception ex)
    	{
    		MessageBox.show(this, "Ant database", "Error check db version" + ex.getLocalizedMessage());
    	}
    }

    public void onAppExit()
    {    	
    	stopService(new Intent(this, GPSLoggingService.class));
    	Db.getInstance().close();
    	finish();
    	
    	System.runFinalizersOnExit(true);
		System.exit(0);
    }
    
    @Override public void onDestroy()
    {
    	try
    	{    		
    		//stopService(new Intent(this, GPSLoggingService.class));
    		super.onDestroy();
    	}
    	catch(Exception ex)
    	{
    		ErrorHandler.CatchError("AboutForm.onDestroy", ex);
    	}    	
    }
}

