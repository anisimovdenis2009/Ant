package com.app.ant.app.ServiceLayer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import com.app.ant.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;


public class BluetoothClient 
{
	//calling context
	static Context mContext = null;
	static ProgressDialog progressDialog = null;
	
	/** Callback: ��������� ����������� ������ ��������� �������� � �������� ����������� �� Bluetooth*/ 	   
    public interface OnBluetoothConnectedListener 
    {    	
        abstract void onBluetoothConnected(InputStream is, OutputStream os);
    }
    
    //----------------------------------------------------------------------------
    public static void connectBluetoothDevice(Context context, String deviceAddress, OnBluetoothConnectedListener connectionListener)
    {
        try
        {
        	Log.d("Bluetooth", "connectBluetoothDevice");
        	
        	mContext = context;
        	progressDialog = displayProgressDialog(context, context.getResources().getString(R.string.cash_reg_inProgress));
        	
        	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        	BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        	//BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice("10:00:E8:C0:BE:03");	//cash register 
        	//BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice("00:02:72:B0:64:6B");	//bluetake
        	
            ConnectThread thread = new ConnectThread(bluetoothDevice, mBluetoothAdapter, connectionListener);
            thread.start();
        }
        catch(Exception ex)
        {
        	Log.d("Bluetooth", "Exception in connectBluetoothDevice");
        	dismissProgressDialog(progressDialog);
        }      
    }
    
	// --------------------------------------------------------------
	public static ProgressDialog displayProgressDialog(Context context, String message)
	{
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(message);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.show();
		
		return progressDialog;
	}
	
	// --------------------------------------------------------------
	public static void dismissProgressDialog(ProgressDialog progressDialog)
	{
		if(progressDialog!=null)
			progressDialog.dismiss();
	}	
	
    //-----------------------------------------------------------------------------
    
    private static class ConnectThread extends Thread 
    {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	    private final BluetoothAdapter mmAdapter;
	    private final OnBluetoothConnectedListener connectionListener;
	
	    public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter, OnBluetoothConnectedListener connectionListener) 
	    {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	        mmAdapter = adapter;
	        this.connectionListener = connectionListener;
	
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try 
	        {
	        	Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}); 
	        	tmp = (BluetoothSocket) m.invoke(device, 1); 
	            //tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
	        } 
	        catch (Exception e) 
	        { 
	        }
	        mmSocket = tmp;
	    }
	
	    public void run() 
	    {
	    	Log.d("Bluetooth", "ConnectTread::run");
	    	try
	    	{
		        // Cancel discovery because it will slow down the connection
		        mmAdapter.cancelDiscovery();
	
		        try 
		        {
		            // Connect the device through the socket. This will block
		            // until it succeeds or throws an exception
		            mmSocket.connect();
		        } 
		        catch (IOException connectException) 
		        {
		        	Log.d("Bluetooth", "Exception in ConnectTread::run mmSocket.connect();");
		        	
		            // Unable to connect; close the socket and get out
		            try 
		            {
		                mmSocket.close();
		            } 
		            catch (IOException closeException) 
		            { 
		            	Log.d("Bluetooth", "Exception in ConnectTread::run mmSocket.close();");
		            }
		            
		            throw new RuntimeException(connectException);
		        }
	
		        // Do work to manage the connection (in a separate thread)
		        manageConnectedSocket(mmSocket);
	    	}
		   catch(Exception ex)
		   {
			   Log.d("Bluetooth", "Exception in ConnectTread::run");   
		   }
		   finally
		   {
		        //dismiss progress dialog
		        if(progressDialog!=null && mContext!=null)
		        {
					((Activity)mContext).runOnUiThread(
							new Runnable() 
							{
								public void run() 
								{
									dismissProgressDialog(progressDialog);
								}
							});
		        }
		   }
	    }
	
	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() 
	    {
	        try 
	        {
	            mmSocket.close();
	        } 
	        catch (IOException e) { }
	    }
	    
	    private void manageConnectedSocket(BluetoothSocket socket)
	    {
	    	Log.d("Bluetooth", "ConnectTread::manageConnectedSocket");
	    	
	    	try
	    	{
	    		InputStream is = socket.getInputStream();    		
	    		OutputStream os = socket.getOutputStream();    		

	    		if(connectionListener!=null)
	    			connectionListener.onBluetoothConnected(is, os);
	    	}
	    	catch(Exception ex)
	    	{
	    		Log.d("Bluetooth", "Exception in ConnectTread::manageConnectedSocket");
	    	}
	    	finally
	    	{
	    		cancel();
	    	}
	    }

   	}
}