package com.app.ant.app.ServiceLayer;

import android.util.Log;


public class ErrorHandler
{
	public static final int	LOG_ERROR	= 1;
	public static final int	LOG_DEBUG	= 5;

	private static boolean	showErrors	= false;

	public static void CatchError(String strSrcMsg, Exception ex, int priority)
	{
		String strExMsg = ex == null ? "" : ex.toString();
		strExMsg = strExMsg == null ? "" : strExMsg;
		strSrcMsg = strSrcMsg == null ? "" : strSrcMsg;

		int logLevel = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_LOG_LEVEL, 5);

		if (logLevel >= priority)
		{
			Log.d(strSrcMsg, strExMsg);
			int logType = (priority == LOG_DEBUG) ? MLog.LOG_TYPE_DEBUG_TEXT : MLog.LOG_TYPE_ERROR;
			Log.e("CatchError", strSrcMsg + " " + strExMsg);
			MLog.WriteLog(logType, strSrcMsg + " " + strExMsg);
		}

		//if (showErrors) MessageBox.show(AntContext.getInstance().getContext(), strSrcMsg, strExMsg);
	}

	public static void CatchError(String strSrcMsg, int priority)
	{
		CatchError(strSrcMsg, null);
	}

	public static void CatchError(String strSrcMsg, Exception ex)
	{
		CatchError(strSrcMsg, ex, LOG_ERROR);
	}

}
