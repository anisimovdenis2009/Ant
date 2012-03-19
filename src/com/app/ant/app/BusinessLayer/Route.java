package com.app.ant.app.BusinessLayer;

import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.Settings;

import java.util.Calendar;


public class Route
{
	//------------------------------------------------------------
	public static void addRouteItem(long addrId, Calendar date, int visitType)
	{
		long salerId = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_SALER_ID);
		String sqlDate = Convert.getSqlDateFromCalendar(date);
		
		String sqlInsert = "INSERT OR REPLACE into Routes (AddrID, Date, SalerID, Visited, VisitTypeID, State, Sent )" 
							+ " VALUES ( ?, ?, ?, ?, ?, ?, ? )";		
		Object[] bindArgs = new Object[] { addrId, sqlDate, salerId, 0, visitType, 
												Q.RECORD_STATE_ACTIVE, Q.RECORD_NOT_SENT }; 
		Db.getInstance().execSQL(sqlInsert, bindArgs);
	}
	
	//------------------------------------------------------------	
	public static void deleteRouteItem(long addrId, Calendar date)
	{
		String sql = String.format("UPDATE Routes SET State='%s', Sent=%d WHERE AddrID=%d AND Date " + Q.getSqlBetweenDayStartAndEnd(date.getTime()),
				Q.RECORD_STATE_DELETED, Q.RECORD_NOT_SENT, addrId); 

		Db.getInstance().execSQL(sql);
	}
}

