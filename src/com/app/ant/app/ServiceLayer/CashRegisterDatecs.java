package com.app.ant.app.ServiceLayer;

import android.content.Context;
import android.util.Log;
import org.apache.http.util.EncodingUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class CashRegisterDatecs extends CashRegister 
{
	private static String deviceAddress;
	
	public static void setDeviceAddress(String address)  {	deviceAddress = address; }
	
	/** �������� ������������ �����*/
    public static class Commands
    {
    	//command counter
    	private static final int SEQ_MIN = 0x20;
    	private static final int SEQ_MAX = 0x7F;
    	
    	static int seq = SEQ_MIN;
    	
    	//
    	//tokens used in command
    	//
    	private static final int SOH = 0x01;
    	private static final int LEN = 0x20;
    	private static final int SEMI = 0x3b;
    	private static final int ENQ = 0x05;
    	private static final int ETX = 0x03;
    	private static final int HASH = 0x30;
    	private static final int[] PASS = new int[] { 0x30, 0x30, 0x30, 0x30, 0x30, 0x30 };
    	
    	//
        // command codes
        //
    	
    	private static final int VERSION = 0x80;
    	private static final int READ_DATE = 0x21;
    	private static final int XZREPORT =	0xa1; //-95 dec
        private static final int OPEN_DOCUMENT = 0x60;
        private static final int PERIODIC_REPORT = 0xa2;
        private static final int CLOSE_CHECK = 0x65;      
    	private static final int MONEY_IN = 0x6e;    	        
        private static final int BEGIN_NONFISCAL = 0x60;
        private static final int NONFISCAL_LINE = 0x61;		//91 dec
        private static final int CLOSE_NONFISCAL = 0x65;
        private static final int ADDINIONAL_REPORT = 0xa6;
        private static final int ABORT_FISCAL = 0x6b;        
        private static final int CLOSE_TAPE = 0xa1;        
        private static final int RESTART_DEVICE = 0xF3;
        
        private static final int BEGIN_FISCAL = 0x63;        
        private static final int ADD_ITEM = 0x24;
        private static final int SALE_ITEM = 0x64;
        private static final int DISCOUNT = 0x69;
        private static final int TOTALS = 0x6d;
        private static final int PAY = 0x67;                        
        private static final int CLOSE_FISCAL = 0x65;
        
        //-----------------------------------------------------------------------------------
    	
    	//
    	// commands
    	//
        
    	public static List<Integer> getCloseNonFiscalCommand()
    	{
    		return getCommand(CLOSE_NONFISCAL, "0;");
    	}        
        
    	public static List<Integer> getBeginFiscalCommand(int user, int cashDesk, boolean returnCheck)
    	{
    		String params = String.format("%d;%d;%d;", user, cashDesk, returnCheck ? 1 : 0);
    		return getCommand(BEGIN_FISCAL, params);
    	}
    	
    	public static List<Integer> getAddItemFiscalCommand(int article, String name, int vatIndex)
    	{
    		String params = String.format("%d;%s;%d;0;00;;", article, name, vatIndex);
    		return getCommand(ADD_ITEM, params);
    	}
    	
    	public static List<Integer> getSaleItemFiscalCommand(int article, double quantity, double price)
    	{
    		
    		String params = String.format("%d;%s;%s;", article, quantityToString(quantity), moneyToString(price));
    		return getCommand(SALE_ITEM, params);
    	}
    	
        public static List<Integer> getDiscountCommand(double discount)
        {
    		String params = String.format("%s;", moneyToString(discount));
    		return getCommand(DISCOUNT, params);
        }
        
        public static List<Integer> getTotalsCommand()
        {
    		return getCommand(TOTALS, "1;");
        }

        public static List<Integer> getPayCommand(double amount)
        {
    		String params = String.format("0;%s;", moneyToString(amount));
    		return getCommand(PAY, params);
        }
        
        public static List<Integer> getCloseFiscalCommand()
        {
    		return getCommand(CLOSE_FISCAL, "0;");
        }
        
        //-----------------------------------------------------------------------------------
    	
    	//
    	// helpers
    	//

        private static List<Integer> getCommand(int commandId)
    	{
    		return getCommand(commandId, "");
    	}
    	
    	private static List<Integer> getCommand(int commandId, String params)
    	{
    		List<Integer> command = beginCommand(commandId);
   		
    		if(params!=null && params.length()!=0)
    			appendString(command, params);    		
    		endCommand(command);
    		
    		return command;
    	}
    	
    	private static int getSeq()
    	{
    		int result = seq;
    		seq++;
    		if(seq>SEQ_MAX)
    			seq = SEQ_MIN;
    		
    		return result;
    	}
    	
        private static List<Integer> beginCommand(int commandId)
        {
        	List<Integer> result = new ArrayList<Integer>();            
            result.add(SOH);
            result.add(LEN);
            result.add(getSeq());	
            result.add(commandId);
            appendArray(result, PASS);
            result.add(SEMI);
            return result;
        }
        
        private static void appendArray(List<Integer> command, int[] bytes)
        {
        	for(int i=0; i<bytes.length; i++)
        		command.add(bytes[i]);
        }
        
        private static void appendString(List<Integer> command, String value)
        {
        	for(int i=0; i<value.length(); i++)
        	{
        		int character = (int)value.charAt(i);
        		
        		//UTF16 should be converted to ANSI
        		if( character>=0x410 && character<=0x44F)
        			character = character-848;
        		else
        			if(character>128)
        				character = 0x2E;	//0x2E is '.'	
        		command.add(character);
        	}
        }

        private static void endCommand(List<Integer> command)
        {
            command.add(ENQ);

            //minus 1 because length should not include <SOH>
            //also, length should be counted here because it doesn't include BCC and ETX
            command.set(1, (command.size()-1+LEN) );
            
            //compute checksum (BCC)
            {
	            int sum = 0;
	
	            for (int i = 1; i < command.size(); i++)
	            {
	                sum += command.get(i);
	            }
	
	            command.add(  (HASH + ((sum >> 12) & 0x000F) ));
	            command.add(  (HASH + ((sum >> 8) & 0x000F) ));
	            command.add(  (HASH + ((sum >> 4) & 0x000F)) );
	            command.add(  (HASH + (sum & 0x000F) ));
            }
            
            command.add(ETX);

        }
    	
    }
    
    //-----------------------------------------------------------------------------------
    private static void logIntBuffer(String message, List<Integer> values)
    {
    	for(int i=0; i<values.size(); i++)
    		message = message + " " + values.get(i);    	
    	Log.d("Datecs", message);
    }
    
    //-----------------------------------------------------------------------------------
    private static void logByteBuffer(String message, byte[] bytes)
    {
    	for(int i=0; i<bytes.length; i++)
    		message = message + " " + bytes[i];
    	Log.d("Datecs", message);
    }

    //-----------------------------------------------------------------------------------
    private static void logBytesAsString(String message, byte[] bytes)
    {
    	message = message + EncodingUtils.getAsciiString(bytes);
    	Log.d("Datecs", message);
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
    private static void executeCommand(List<Integer> command, InputStream is, OutputStream os)
    {
    	try
    	{
    		//convert list of Integers to byte array
	    	byte[] bytes = new byte[command.size()];
	    	for(int i=0; i<command.size();i++)
	    		bytes[i]= (byte) (command.get(i) & 0xff);

	    	//log
	    	logIntBuffer("Command int: ", command);
	    	logByteBuffer("Command bytes: ", bytes);
	    	logBytesAsString("Command str: ", bytes);
	    		    	
			//send command to device	    	
	    	boolean repeatCommand = false;
	    	int repeatCount = 0;
	    	final int MAX_REPEATS = 5;
	    	final int MAX_TIMEOUT = 5000; //5 seconds
	    	final int MAX_TIMEOUT_SYNCHRONIZED = 30000; //30 seconds
	    	
	    	do
	    	{
	    		//write command to output stream
		    	os.write(bytes);
		    	os.flush();
		    	
		    	//read and parse response
		    	byte[] result = new byte[0];
		    	repeatCommand = false;
		    	boolean cont = true;		    	
		    	long startTime = System.currentTimeMillis();
		    	long startTimeFixed = System.currentTimeMillis();
		    	
	    		do
	    		{
		    		int available = is.available();
		    		if(available>0)
		    		{
		    			byte[] in = new byte[available];
		    			int len = is.read(in,0,available);

		    			//
		    			//analyse input bytes
		    			//
		    			int count22in = 0;
		    			for(int i=0; i<result.length; i++)
		    			{
		    				if(result[i]==22)
		    					count22in++;
		    				else if(result[i]==Commands.SOH)
		    				{
		    					//we just received a SOH command, 
			    				//so extend the processing time (set start time to current)
			    				startTime = System.currentTimeMillis();
		    				}
		    			}

		    			if(count22in!=0 && count22in==result.length)
		    			{
		    				//input message contains sync command,
		    				//so extend the processing time (set start time to current)
		    				startTime = System.currentTimeMillis();
		    				Log.d("Datecs", "Sync received");
		    			}
		    			
		    			//
		    			//concatenate results
		    			//
		    			result = appendBytes(result, in);
		    			
		    			//
		    			//analyse result, repeat command if needed
		    			//
		    			int count21 = 0;
		    			int count22 = 0;
		    			for(int i=0; i<result.length; i++)
		    			{
		    				if(result[i]==21) 
		    					count21++;
		    				else if(result[i]==22)
		    					count22++;
		    				else if(result[i]==Commands.SOH)
		    				{
		    					//we have received a message. check if the message is full
		    					if(i+1<result.length)
		    					{
		    						int messageLength = result[i+1];
		    						if(messageLength>0x20)
		    						{
		    							messageLength = messageLength-0x20;
		    							int ETX_pos = i+messageLength + 4 + 1;	//add 4 for checksum, 1 for etx 
		    							if(ETX_pos<result.length && result[ETX_pos]==Commands.ETX)
		    							{
		    								//we received a correct message, so stop waiting
		    								cont = false;
		    								
		    								//log a message
			    							int fullMessageLen = 1+ messageLength + 4 + 1;
		    								byte[] response = new byte[fullMessageLen];
		    								System.arraycopy(result, i, response, 0, fullMessageLen);
		    				    	    	logByteBuffer("Response bytes: ", response);
		    				    	    	logBytesAsString("Response str: ", response);   			
		    							}
		    						}
		    					}
		    				}
		    			}

		    			if(count21!=0 && count21+count22 == result.length)
		    			{
			    			//message consists of 21 and 22 only, so repeat the same command
		    				if(repeatCount<MAX_REPEATS)
		    				{
		    					repeatCount++;
		    					Thread.currentThread().sleep(100,0);
		    					repeatCommand = true;
		    					cont = false;
		    					
		    					Log.d("Datecs", String.format("Repeat a same command %d", repeatCount));
		    				}
		    				else
		    				{
		    					//ERROR
		    					//we already repeated a command several times, so stop repeating		    					
			    				cont = false;	    					
			    				Log.d("Datecs", String.format("Repeated %d times, no success", repeatCount));
		    				}
		    			}
		    			else if(count22!=0 && count22 == result.length)
		    			{
		    				//22 is SYN, it means that cash register is busy with processing current command
		    				//so continue waiting for response
		    				
		    				//extend the processing time (set start time to current)
		    				//startTime = System.currentTimeMillis();
		    				if(System.currentTimeMillis() - startTimeFixed > MAX_TIMEOUT_SYNCHRONIZED)
		    				{
		    					//ERROR
		    					//cash register send synchronized message 22 but it lasts too long, so even big timeout is expired
		    					Log.d("Datecs", String.format("Timeout %d reached", MAX_TIMEOUT_SYNCHRONIZED));
		    					cont = false;		
		    				}
		    			}
		    		}
		    		
		    		Thread.currentThread().sleep(50,0);
		    		
		    		if(System.currentTimeMillis() - startTime > MAX_TIMEOUT)
		    		{
		    			//ERROR
		    			//timeout is reached, but no response receiver
		    			Log.d("Datecs", String.format("Timeout %d reached", MAX_TIMEOUT));
		    			cont = false;
		    		}
		    		
	    		} while(cont);
	    		
	    		if(result.length!=0)
	    		{
	    			//log    			
	    	    	logByteBuffer("Response bytes (full): ", result);
	    	    	logBytesAsString("Response str (full): ", result);    			
	    		}	    		
	    	}
	    	while(repeatCommand);
    	}
    	catch(Exception ex)
    	{
    		
    	}
    }
    
    //
    // Public methods
    //
    
    //-----------------------------------------------------------------------------------
    public static void getDate(Context context)
    {
    	BluetoothClient.connectBluetoothDevice ( context, deviceAddress, new BluetoothClient.OnBluetoothConnectedListener()
		{
			public void onBluetoothConnected(InputStream is, OutputStream os)
			{
				executeCommand(Commands.getCommand(Commands.READ_DATE), is, os);				
			}
		} );    	
    }
    
    
    //-----------------------------------------------------------------------------------
    public static void getVersion(Context context)
    {
    	BluetoothClient.connectBluetoothDevice ( context, deviceAddress, new BluetoothClient.OnBluetoothConnectedListener()
		{
			public void onBluetoothConnected(InputStream is, OutputStream os)
			{
				executeCommand(Commands.getCommand(Commands.VERSION), is, os);				
			}
		} );    	
    }

    //-----------------------------------------------------------------------------------
    public static void printXReport(Context context)
    {
    	BluetoothClient.connectBluetoothDevice ( context, deviceAddress, new BluetoothClient.OnBluetoothConnectedListener()
		{
			public void onBluetoothConnected(InputStream is, OutputStream os)
			{
				executeCommand(Commands.getCommand(Commands.XZREPORT, "1;"), is, os);				
			}
		} );    	
    }
    
    //-----------------------------------------------------------------------------------
    public static void printZReport(Context context)
    {
    	BluetoothClient.connectBluetoothDevice ( context, deviceAddress, new BluetoothClient.OnBluetoothConnectedListener() 
		{
			public void onBluetoothConnected(InputStream is, OutputStream os)
			{
				executeCommand(Commands.getCommand(Commands.XZREPORT, "0;"), is, os);				
			}
		});
    }
    
    //-----------------------------------------------------------------------------------
	public static String dateToString(Calendar date) 
	{
		java.util.Date _date = date.getTime(); 
		
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyy");
        return formatter.format(_date);
	}
    
    public static void printPeriodicReport(Context context, final Calendar startDate, final Calendar endDate, final boolean isShort)
    {
    	BluetoothClient.connectBluetoothDevice ( context, deviceAddress, new BluetoothClient.OnBluetoothConnectedListener() 
		{
			public void onBluetoothConnected(InputStream is, OutputStream os)
			{
				String params = String.format("%s;%s;%d;", dateToString(startDate), dateToString(endDate), isShort?0:1);
				
				executeCommand(Commands.getCommand(Commands.OPEN_DOCUMENT), is, os);				
				executeCommand(Commands.getCommand(Commands.PERIODIC_REPORT, params), is, os);
				executeCommand(Commands.getCommand(Commands.CLOSE_CHECK, "0;"), is, os);
			}
		});   	
    }
    //-----------------------------------------------------------------------------------
    private static String quantityToString(double number)
    {
    	DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    	decimalFormatSymbols.setDecimalSeparator('.');

        DecimalFormat formatter = new DecimalFormat("#0.000", decimalFormatSymbols);
        return formatter.format(number);    	
    }    
    
    private static String moneyToString(double number)
    {
    	DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    	decimalFormatSymbols.setDecimalSeparator('.');

        DecimalFormat formatter = new DecimalFormat("#0.00", decimalFormatSymbols);
        return formatter.format(number);    	
    }
    
    //-----------------------------------------------------------------------------------
    public static void printMoneyIn(Context context, final double amount)
    {
    	BluetoothClient.connectBluetoothDevice ( context, deviceAddress, new BluetoothClient.OnBluetoothConnectedListener() 
		{
			public void onBluetoothConnected(InputStream is, OutputStream os)
			{
				String params = String.format("0;%s;", moneyToString(amount));
				executeCommand(Commands.getCommand(Commands.MONEY_IN, params), is, os);				
			}
		});
    }
    
    //-----------------------------------------------------------------------------------    
    public static void printMoneyOut(Context context, final double amount)
    {
    	BluetoothClient.connectBluetoothDevice ( context, deviceAddress, new BluetoothClient.OnBluetoothConnectedListener() 
		{
			public void onBluetoothConnected(InputStream is, OutputStream os)
			{
				String params = String.format("0;-%s;", moneyToString(amount));
				executeCommand(Commands.getCommand(Commands.MONEY_IN, params), is, os);				
			}
		});
    }

    //-----------------------------------------------------------------------------------
    public static void printNonFiscal(Context context, final List<String> lines)
    {
    	BluetoothClient.connectBluetoothDevice ( context, deviceAddress, new BluetoothClient.OnBluetoothConnectedListener() 
		{
			public void onBluetoothConnected(InputStream is, OutputStream os)
			{
				executeCommand(Commands.getCommand(Commands.BEGIN_NONFISCAL), is, os);
				if(lines!=null)
				{
					for(int i=0; i<lines.size(); i++)
					{
						executeCommand(Commands.getCommand(Commands.NONFISCAL_LINE, lines.get(i)+";"), is, os);
					}
				}
				executeCommand(Commands.getCloseNonFiscalCommand(), is, os);
			}
		});
    }

    //-----------------------------------------------------------------------------------
    public static void printDepartmentsReport(Context context)
    {
    	BluetoothClient.connectBluetoothDevice ( context, deviceAddress, new BluetoothClient.OnBluetoothConnectedListener() 
		{
			public void onBluetoothConnected(InputStream is, OutputStream os)
			{
				executeCommand(Commands.getCommand(Commands.BEGIN_NONFISCAL), is, os);
				executeCommand(Commands.getCommand(Commands.ADDINIONAL_REPORT, "0;;;"), is, os);
				executeCommand(Commands.getCloseNonFiscalCommand(), is, os);
			}
		});
    }
    
    //-----------------------------------------------------------------------------------
    public static void printOperatorsReport(Context context)
    {
    	BluetoothClient.connectBluetoothDevice ( context, deviceAddress, new BluetoothClient.OnBluetoothConnectedListener() 
		{
			public void onBluetoothConnected(InputStream is, OutputStream os)
			{
				executeCommand(Commands.getCommand(Commands.BEGIN_NONFISCAL), is, os);
				executeCommand(Commands.getCommand(Commands.ADDINIONAL_REPORT, "1;;;"), is, os);
				executeCommand(Commands.getCloseNonFiscalCommand(), is, os);
			}
		});
    }
    
    //-----------------------------------------------------------------------------------
    public static void printCheckoutTape(Context context)
    {
    	BluetoothClient.connectBluetoothDevice ( context, deviceAddress, new BluetoothClient.OnBluetoothConnectedListener() 
		{
			public void onBluetoothConnected(InputStream is, OutputStream os)
			{
				executeCommand(Commands.getCommand(Commands.BEGIN_NONFISCAL), is, os);
				executeCommand(Commands.getCommand(Commands.ADDINIONAL_REPORT, "2;;;"), is, os);
				executeCommand(Commands.getCloseNonFiscalCommand(), is, os);
			}
		});
    }

    //-----------------------------------------------------------------------------------
    public static void abortFiscal(Context context)
    {
    	BluetoothClient.connectBluetoothDevice ( context, deviceAddress, new BluetoothClient.OnBluetoothConnectedListener() 
		{
			public void onBluetoothConnected(InputStream is, OutputStream os)
			{
				executeCommand(Commands.getCommand(Commands.ABORT_FISCAL, "0;"), is, os);
			}
		});
    }

    //-----------------------------------------------------------------------------------
    public static void closeCheckoutTape(Context context)
    {
    	BluetoothClient.connectBluetoothDevice ( context, deviceAddress, new BluetoothClient.OnBluetoothConnectedListener() 
		{
			public void onBluetoothConnected(InputStream is, OutputStream os)
			{
				executeCommand(Commands.getCommand(Commands.CLOSE_TAPE, "2;"), is, os);
			}
		});
    }

    //-----------------------------------------------------------------------------------
    public static void restartDevice(Context context)
    {    	
    	BluetoothClient.connectBluetoothDevice	( context, deviceAddress, new BluetoothClient.OnBluetoothConnectedListener()
		{
			public void onBluetoothConnected(InputStream is, OutputStream os)
			{
				executeCommand(Commands.getCommand(Commands.RESTART_DEVICE, ""), is, os);				
			}
		});    	
    }

    //-----------------------------------------------------------------------------------    
    public static void printFiscalCheck(Context context, final List<CheckItem> items, final double amount, final boolean returnCheck)
	{
    	BluetoothClient.connectBluetoothDevice
    	( context, deviceAddress,  
    			new BluetoothClient.OnBluetoothConnectedListener()
    			{
    				public void onBluetoothConnected(InputStream is, OutputStream os)
    				{
    					executeCommand(Commands.getBeginFiscalCommand(0,0, returnCheck), is, os);

    					for(int i=0; i<items.size(); i++)
    					{
    						CheckItem checkItem = items.get(i);
    						
    						executeCommand(Commands.getAddItemFiscalCommand(checkItem.cashCode, checkItem.cashName, checkItem.vatIndex), is, os);
    						executeCommand(Commands.getSaleItemFiscalCommand(checkItem.cashCode, checkItem.orders, checkItem.price), is, os);
    						
    						if(checkItem.discount!=0)
    							executeCommand(Commands.getDiscountCommand(checkItem.discount),is,os);
    						
    					}
    					
    					executeCommand(Commands.getTotalsCommand(), is, os);
    					executeCommand(Commands.getPayCommand(amount), is, os);
    					executeCommand(Commands.getCloseFiscalCommand(), is, os);
    					
    				}
    			}
		);    	
	}
	

}
