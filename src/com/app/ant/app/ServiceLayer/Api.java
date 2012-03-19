package com.app.ant.app.ServiceLayer;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.UUID;


public class Api
{

	public static String  getDeviceID()
	{		
		String deviceId = "DEVICEID001";		
		try
		{		
			final TelephonyManager tm = (TelephonyManager) AntContext.getInstance().getContext().getSystemService(Context.TELEPHONY_SERVICE);
			
//			String pseudoID = "35" +
//			Build.BOARD.length()%10 + Build.BRAND.length()%10 +
//			Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
//			Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
//			Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
//			Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
//			Build.TAGS.length()%10 + Build.TYPE.length()%10 +
//			Build.USER.length()%10;
//
//			Summary like: 356984215578214
			
			//bluetooth
			//BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			//String blueToothMac = bluetoothAdapter.getAddress();
			//need android.permission.BLUETOOTH
			
			//phoneNumber
			//String phoneNumber = tm.getLine1Number();
		    
			//WIFI
			//WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			//String wifiMac = wifiManager.getConnectionInfo().getMacAddress();
			//need android.permission.ACCESS_WIFI_STATE
			
			final String tmDevice, tmSerial, androidId;
		    tmDevice = "" + tm.getDeviceId(); //IMEI
		    tmSerial = "" + tm.getSimSerialNumber();
		    //A 64-bit number (as a hex string) that is randomly generated on the device�s first boot and should remain constant for the lifetime of the device.
		    androidId = "" + android.provider.Settings.Secure.getString(AntContext.getInstance().getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	
		    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
		    deviceId = deviceUuid.toString();
		}
		catch(Exception ex)
		{
			Log.e("Exception in Api::getDeviceID", ex.getLocalizedMessage());
			//ErrorHandler.CatchError("Exception in Api::getDeviceID",ex);
		}
	    return deviceId;
	}
	
	public static String getVersionName()
	{
		String result = "";
		PackageManager manager =  AntContext.getInstance().getContext().getPackageManager();
		PackageInfo info = null;
		try
		{
			info = manager.getPackageInfo(AntContext.getInstance().getContext().getPackageName(), 0);
			result = info.versionName; 
		}
		catch (Exception ex)
		{
			ErrorHandler.CatchError("Exception in Api::getVersionName", ex);
		}		
		return result;
	}
		
	public static int getVersionCode()
	{
		int result = 0;
		PackageManager manager =  AntContext.getInstance().getContext().getPackageManager();
		PackageInfo info = null;
		try
		{
			info = manager.getPackageInfo(AntContext.getInstance().getContext().getPackageName(), 0);
			result = info.versionCode; 
		}
		catch (Exception ex)
		{
			ErrorHandler.CatchError("Exception in Api::getVersionCode", ex);
		}		
		return result;
	}
	
	public static boolean isWiFiEnabled()
	{
		boolean result = false;
		try
		{
			WifiManager wifi = (WifiManager)AntContext.getInstance().getContext().getSystemService(Context.WIFI_SERVICE);
			result = wifi.isWifiEnabled();
		}
		catch (Exception ex) 
		{
			ErrorHandler.CatchError("Api.isWiFiEnabled", ex);
		}
		return result;
	}	

	public static boolean isOnline()
	{
		boolean result = false;
		try
		{
			ConnectivityManager cm = (ConnectivityManager) AntContext.getInstance().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = cm.getActiveNetworkInfo();
			if (networkInfo != null) result = networkInfo.isConnectedOrConnecting(); 
		}
		catch (Exception ex) 
		{
			ErrorHandler.CatchError("Api.isOnline", ex);
		}		
		return result;
	}
}
