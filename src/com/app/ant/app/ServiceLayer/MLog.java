package com.app.ant.app.ServiceLayer;

import android.util.Log;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;



public class MLog 
{
	public static final int LOG_TYPE_ERROR = 1;
	public static final int LOG_TYPE_FATAL_ERROR = 2;
	public static final int LOG_TYPE_TEXT = 3;
	public static final int LOG_TYPE_DEBUG_TEXT = 4;
	public static final int LOG_TYPE_DOC_STEP = 5;
	public static final int LOG_TYPE_FEEDBACK = 6;
	public static final int LOG_TYPE_BATTERY_LEVEL = 7;
	
	public static int notWritedLogQnt = 0;
	
	public static void WriteLog(int logType, String logText)
	{		
		try
		{
			logText = logText.replace('"', '`');
			logText = logText.replace("'", "`");
			
			String strSql = Q.mobileLog_getInsertCommandText(AntContext.getInstance().getSalerId(), AntContext.getInstance().getDeviceId(), logType, logText);
			notWritedLogQnt++;
			if (notWritedLogQnt < 10) Db.getInstance().execSQL(strSql);
			notWritedLogQnt = 0;
		}
		catch(Exception ex)
		{
			try
			{
				Log.e("WriteLog.exception", logText);
				Log.e("WriteLog.exception", ex.toString());
			}
			catch (Exception exx){}
		}
	}

}
