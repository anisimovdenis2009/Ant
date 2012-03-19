package com.app.ant.app.ServiceLayer;

import android.content.Context;
import android.content.res.Resources;
import com.app.ant.R;
import com.app.ant.app.BusinessLayer.Document;
import com.app.ant.app.DataLayer.Q;

import java.text.SimpleDateFormat;
import java.util.Calendar;



public class Convert {
	
	public static String toString(Object obj, String def)
	{
		String rez = def;
		try
		{
			rez = obj.toString();
		}
		catch (Exception ex)
		{
			//TODO write to log
		}
		return rez;
	}
	
	public static int toInt(Object obj, int def)
	{
		int rez = def;
		try
		{
			String str = toString(obj, "");
			if (str.length() > 0) rez = Integer.parseInt(str);
		}
		catch (Exception ex)
		{
			//TODO write to log
		}
		return rez;
	}	
	
	public static long toLong(Object obj, long def)
	{
		long rez = def;
		try
		{
			String str = toString(obj, "");
			if (str.length() > 0)  rez = Long.parseLong(str);
		}
		catch (Exception ex)
		{
			//TODO write to log
		}
		return rez;
	}
	
	public static Double toDouble(Object obj, Double def)
	{
		Double rez = def;
		try
		{
			String str = toString(obj, "");
			if (str.length() > 0)  rez = Double.parseDouble(str);
		}
		catch (Exception ex)
		{
			//TODO write to log
		}
		return rez;
	}

	public static boolean isNull(String str) {
        return str == null ? true : false;
    }

    public static boolean isNullOrBlank(String param) {
        if (isNull(param) || param.trim().length() == 0) {
            return true;
        }
        return false;
    }
    
    //--------------------------------------------------------
	public static double roundUpMoney(double value)
	{
		double rez = 0;		

		try
		{
			/*long tmp = ((Double)(value * 100 + 0.000001)).longValue();
			rez = tmp;
			rez/=100;*/
			
			rez = ((double)Math.round(value*100.))/100.;
		}
		catch(Exception ex)
		{
			ErrorHandler.CatchError(String.format("Exception in roundUpMoney, value=%f", value), ex);
			throw new RuntimeException(ex);
		}
		
		return rez;
	}
	
	public static double roundUpFullMoney(double value)
	{
		double rez = 0;		

		try
		{		
			rez = ((double)Math.round(value*10000.))/10000.;
		}
		catch(Exception ex)
		{
			ErrorHandler.CatchError(String.format("Exception in roundUpMoney, value=%f", value), ex);
			throw new RuntimeException(ex);
		}
		
		return rez;
	}
	
	//--------------------------------------------------------
	public static String moneyToString(double money)
	{
		String rez = String.format("%.2f", money);
		return rez;
	}

	//--------------------------------------------------------
	public static String msuToString(double money)
	{
		String rez = String.format("%.5f", money);
		return rez;
	}

    //--------------------------------------------------------
	public static char getDocTypeFromString(String strDocType)
	{
		char docType = (strDocType.length()>0)? strDocType.charAt(0): Document.DOC_TYPE_UNKNOWN;
		return docType;
	}
	
	public static char getDocStateFromString(String strDocState)
	{
		char docState = (strDocState.length()>0)? strDocState.charAt(0): Document.DOC_STATE_UNKNOWN;
		return docState;
	}

	public static char getDocColorFromString(String strDocColor)
	{
		char docColor = (strDocColor!=null && strDocColor.length()>0)? strDocColor.charAt(0): Document.DOC_COLOR_UNKNOWN;
		return docColor;
	}
	
	public static char getRecordStateFromString(String strRecordState)
	{
		char recordState = (strRecordState!=null && strRecordState.length()>0)? strRecordState.charAt(0): Q.RECORD_STATE_UNKNOWN;
		return recordState;
	}	
	//--------------------- Date -----------------------------------
	public static Calendar getDateFromString(String strDate)
	{
		Calendar calendar = Calendar.getInstance();
		
		String[] formats = new String[] { "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss.SSS" };
		
		for(int i=0; i<formats.length; i++)
		{
			try
			{
				SimpleDateFormat formatter = new SimpleDateFormat(formats[i]);
				java.util.Date date = formatter.parse(strDate);
				calendar.setTime(date);
				return calendar;
			}
			catch(Exception ex)
			{		
				if(i == formats.length-1) 		//that was a last chance
					throw new RuntimeException(ex);	//no success on this point, throw an exception			
			}
		}
		
		return calendar;
	}

	public static String getSqlDateTimeFromCalendar( Calendar date) 
	{
		java.util.Date _date = date.getTime(); 
		
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(_date);

		//return new java.sql.Date(date.getTimeInMillis()); 
	}
	
	public static String getSqlDateFromCalendar( Calendar date) 
	{
		java.util.Date _date = date.getTime(); 
		
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        return formatter.format(_date);
	}
	
	public static String dateToString( Calendar date) 
	{
		java.util.Date _date = date.getTime(); 
		
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy (E)");
        return formatter.format(_date);
	}
	
	public static String dateTimeToString( Calendar date) 
	{
		java.util.Date _date = date.getTime(); 
		
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return formatter.format(_date);
	}		
	
	public static Calendar setTimeToZero(Calendar date)
	{
		Calendar zeroTime = (Calendar)date.clone();
		
		zeroTime.set(Calendar.HOUR_OF_DAY , 0);
		zeroTime.set(Calendar.MINUTE, 0);
		zeroTime.set(Calendar.SECOND, 0);
		zeroTime.set(Calendar.MILLISECOND, 0);
		
		return zeroTime;
	}

	public static long getDateDiffInDays(Calendar date1, Calendar date2)
	{
		long diff = setTimeToZero(date1).getTimeInMillis() - setTimeToZero(date2).getTimeInMillis();
		long days = getDaysFromMilliseconds(diff);		
		return days;
	}
	
	public static long getDaysFromMilliseconds(long ms)
	{
		long days = ms / (24 * 60 * 60 * 1000);		
		return days;
	}
	
	public static long getDateDiffInMinutes(Calendar date1, Calendar date2)
	{
		long diff = date1.getTimeInMillis() - date2.getTimeInMillis();
		long minutes = getMinutesFromMilliseconds(diff);	
		return minutes;
	}
	
	public static long getMinutesFromMilliseconds(long ms)
	{
		long minutes = ms / (60 * 1000);		
		return minutes;
	}

	//----------------- Screen density ------------------
	public static int pixelsToDip(int pixels)
	{
		// Convert the dps to pixels
		Context context = AntContext.getInstance().getContext();
		final float scale = context.getResources().getDisplayMetrics().density;
		int dip = (int) ( ((float)pixels)/scale + 0.5f );		
		return dip;
	}
	
	public static int dipToPixels(int dip)
	{
		// Convert the pixels to dps
		Context context = AntContext.getInstance().getContext();
		final float scale = context.getResources().getDisplayMetrics().density;
		int pixels = (int) (dip * scale + 0.5f);		
		return pixels;
	}
	
	//---------------- Money in words ---------------------
	private static String hundredsToString(Resources resources, long value, int order)
	{
		String result="";
		
		if(order>2 || value == 0)
			return result;
		
		int[] strSingular = {R.string.money_words_singular1,R.string.money_words_singular2,R.string.money_words_singular3};
		int[] strPlural = {R.string.money_words_plural1,R.string.money_words_plural2,R.string.money_words_plural3};
		int[] strAccusative = {R.string.money_words_accusative1,R.string.money_words_accusative2,R.string.money_words_accusative3};
		int[] strHundreds = {R.string.money_words_hundreds1,R.string.money_words_hundreds2,R.string.money_words_hundreds3,R.string.money_words_hundreds4,R.string.money_words_hundreds5,
															R.string.money_words_hundreds6,R.string.money_words_hundreds7,R.string.money_words_hundreds8,R.string.money_words_hundreds9};
		int[] strTens = {R.string.money_words_tens1,R.string.money_words_tens2,R.string.money_words_tens3,R.string.money_words_tens4,R.string.money_words_tens5,
															R.string.money_words_tens6,R.string.money_words_tens7,R.string.money_words_tens8,R.string.money_words_tens9};
		int[] strTeens = {R.string.money_words_teens1,R.string.money_words_teens2,R.string.money_words_teens3,R.string.money_words_teens4,R.string.money_words_teens5,
															R.string.money_words_teens6,R.string.money_words_teens7,R.string.money_words_teens8,R.string.money_words_teens9};
		int[] strTallies = {R.string.money_words_tallies1,R.string.money_words_tallies2,R.string.money_words_tallies3,R.string.money_words_tallies4,R.string.money_words_tallies5,
															R.string.money_words_tallies6,R.string.money_words_tallies7,R.string.money_words_tallies8,R.string.money_words_tallies9};
		
		int hundreds = (int) value / 100;
		int tens = (int) ((value % 100) / 10);
		int tallies = (int) (value % 10);
		String suffix = resources.getString(strAccusative[order]) + " ";
		
		if(hundreds != 0)
			result += resources.getString(strHundreds[hundreds - 1]) + " ";			

		if( tens == 1 && tallies != 0)
			result += resources.getString(strTeens[tallies - 1]) + " ";				
		else
		{
			if(tens != 0)
				result += resources.getString(strTens[tens - 1]) + " ";					

			if(tallies != 0)
			{
				result += resources.getString(strTallies[tallies - 1]) + " "; 

				if(tallies ==1 )
					suffix = resources.getString(strSingular[order]) + " ";
				else if(tallies < 5)
					suffix = resources.getString(strPlural[order]) + " ";
			}
		}
		
		result += suffix;		
		return result;
	}
	
	public static String moneyToStringInWords(Resources resources, double money)
	{
		String result = "";
		
		//
		// hryvnas
		//
		long hryvnas = (long)money;	//does truncate
		
		if(hryvnas == 0)
			result = resources.getString(R.string.money_words_zero_hryvnas) + " ";
		else
		{
			result += hundredsToString(resources, hryvnas%1000000000/1000000,2);
			result += hundredsToString(resources, hryvnas%1000000/1000,1);
			result += hundredsToString(resources, hryvnas%1000/1,0);
		}
		
		//
		// kopecks
		//
		
		long kopecks = (long)( roundUpMoney((money-hryvnas)*100));
		String kopecksWord = resources.getString(R.string.money_words_kopecks1);
		
		if( (kopecks<10 || kopecks>20) && kopecks%10!=0 && kopecks%10<5)	//all numbers ending by 1...5 except 11-15 
		{
			if(kopecks%10 == 1) 	//all numbers having 1 at end
				kopecksWord = resources.getString(R.string.money_words_kopecks2);
			else
				kopecksWord = resources.getString(R.string.money_words_kopecks3);
		}
		
		result = result + toString(kopecks, "0") + " " + kopecksWord;
		
		return result;
	}
	
	public static int getNumDaysInCurrentMonth()
	{
    	Calendar calendar = Calendar.getInstance();    	
    	calendar.set(Calendar.DAY_OF_MONTH, 1);
    	calendar.set(Calendar.HOUR_OF_DAY, 0);
    	calendar.set(Calendar.MINUTE, 0);
    	calendar.set(Calendar.SECOND, 0);
    	calendar.set(Calendar.MILLISECOND, 0);
    	calendar.roll(Calendar.DAY_OF_MONTH,-1);

    	return calendar.get(Calendar.DAY_OF_MONTH);
	}
}
