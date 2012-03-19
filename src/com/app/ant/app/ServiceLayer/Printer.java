package com.app.ant.app.ServiceLayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.app.ant.R;
import com.app.ant.app.Activities.BluetoothSelectDeviceForm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class Printer  
{
	/** ��� ��������� ��� ������*/
	public enum PrintableDocumentType { /** ����-�������*/ Bill, /**���������*/ Invoice, Undefined }	
	
	private static String deviceAddress;
	private static void setDeviceAddress(String address)  {	deviceAddress = address; }
	
	//-----------------------------------------------------------------------------------
	public Printer(Context context)
	{
		setupDeviceAddress(context);
	}
	
	//-----------------------------------------------------------------------------------
	public void setupDeviceAddress(Context context)
	{
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String deviceAddr = prefs.getString( context.getString(R.string.preferences_printer_addr_key), "");
        setDeviceAddress(deviceAddr);
	}

	//-----------------------------------------------------------------------------------
	public static void selectPrinter(Context context)
	{
		Bundle bundle = new Bundle();
		bundle.putInt(BluetoothSelectDeviceForm.PARAM_NAME_BLUETOOTH_DEVICE_TYPE, R.string.preferences_printer_addr_key);
		Intent selectDeviceIntent = new Intent(context, BluetoothSelectDeviceForm.class);
		selectDeviceIntent.putExtras(bundle);
		context.startActivity(selectDeviceIntent);
	}
	
	//-----------------------------------------------------------------------------------
	private byte[] stringToBytes(String value)
	{
		//convert string to byte array		
		byte[] retVal = new byte[value.length()];
		
    	for(int i=0; i<value.length(); i++)
    	{
    		int character = (int)value.charAt(i);
    		
    		//UTF16 should be converted to CP866
    		if( character>=0x410 && character<=0x43F)
    			character = character-0x390;
    		else if(character>=0x440 && character<=0x44F)
    			character = character-0x360;
    		else if(character == 0x401)	//JO
    			character = 0xf0;
    		else if(character == 0x451) //jo
    			character = 0xf1;
    		else if(character == 0x404) //JE
    			character = 0xf2;    		
    		else if(character == 0x454) //je
    			character = 0xf3;
    		else if(character == 0x407) //JI
    			character = 0xf4;
    		else if(character == 0x457) //ji
    			character = 0xf5;
    		else if(character == 0x406) //I
    			character = 'I';    		
    		else if(character == 0x456) //i
    			character = 'i';   		
    		else if(character == 0x2116) //N
    			character = 0xfc;   		
    		else if(character>128)
    				character = 0x2E;	//0x2E is '.'
    		
    		retVal[i] = (byte)(character&0xFF);
    	}
    	return retVal;    	
		
		//return EncodingUtils.getAsciiBytes (value);	
    	
	}
	
    //-----------------------------------------------------------------------------------
    private static byte[] appendBytes(byte[] array1, byte[] array2)
    {
		byte[] newArray = new byte[array1.length+array2.length];
		System.arraycopy(array1, 0, newArray, 0, array1.length);
		System.arraycopy(array2, 0, newArray, array1.length, array2.length);
    	
		return newArray;
    }
    
    //-----------------------------------------------------------------------------------
    private void printBytes(byte[] bytes, OutputStream os) throws IOException 
    {
    	byte[] escapeSequence = new byte[] {27, 15, 15}; //condensed font
    	bytes = appendBytes(escapeSequence, bytes);
    	
		os.write(bytes);
		os.flush();
    	
    }
	
    //-----------------------------------------------------------------------------------
    public void printInvoice(final Context context, final PrintableDocumentType docType, final long docId, final double vat)
    {    	
    	BluetoothClient.connectBluetoothDevice	( context, deviceAddress, new BluetoothClient.OnBluetoothConnectedListener()
		{
			public void onBluetoothConnected(InputStream is, OutputStream os)
			{
				try
				{
					String document = new PrintInvoice(docType, vat).getPrintableDocument(context, docId);
					byte[] bytes = stringToBytes(document);
	
					printBytes(bytes, os);
				}
				catch(IOException ex)
				{					
				}
			}
		});    	
    }

    //-----------------------------------------------------------------------------------
    public void printInvoices(final Context context, final PrintableDocumentType docType, 
    							final long docId1, final boolean printWithVat1, final boolean printWOVat1, 
    							final long docId2, final boolean printWithVat2, final boolean printWOVat2)
    {    	
    	BluetoothClient.connectBluetoothDevice	( context, deviceAddress, new BluetoothClient.OnBluetoothConnectedListener()
		{
			public void onBluetoothConnected(InputStream is, OutputStream os)
			{
				try
				{
					if(printWithVat1)
					{
						String document = new PrintInvoice(docType, 0.2).getPrintableDocument(context, docId1);
						printBytes(stringToBytes(document), os);
					}					
					if(printWOVat1)
					{
						String document = new PrintInvoice(docType, 0).getPrintableDocument(context, docId1);
						printBytes(stringToBytes(document), os);
					}
					if(printWithVat2)
					{
						String document = new PrintInvoice(docType, 0.2).getPrintableDocument(context, docId2);
						printBytes(stringToBytes(document), os);
					}					
					if(printWOVat2)
					{
						String document = new PrintInvoice(docType, 0).getPrintableDocument(context, docId2);
						printBytes(stringToBytes(document), os);
					}
				}
				catch(IOException ex)
				{					
				}
			}
		});    	
    }

}