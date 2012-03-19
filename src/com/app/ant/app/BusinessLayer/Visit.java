package com.app.ant.app.BusinessLayer;

import android.content.Context;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;

import java.util.Calendar;


public class Visit 
{
	private long visitID = 0;
	
	private boolean started;
	private boolean finished;

	private boolean needDocCountCheck;
	private int GPSConnectAttempts;
	
	public long getVisitID() { return visitID; }
	public boolean isStarted() { return started; }
	public boolean isFinished() { return finished; }
	public boolean requiresDocCountCheck() { return needDocCountCheck; }
	
	public int 	getConnectAttempts() { return GPSConnectAttempts; }
	public void increaseConnectAttempts() { GPSConnectAttempts++; }
	
	private Client client;
	private Address address;
	private int visitType;
	
	public Visit(Client client, Address address, int visitType)
	{
		this.client = client;
		this.address = address;		
		this.visitType = visitType;
		
		started = false;
		finished = false;
		visitID = -1;
		GPSConnectAttempts = 0;
	}

	//---------------------------------------------------	
	private static long getMaxVisitID()
    {
		String sql = "SELECT max(VisitID) AS VisitID FROM Visits";
		long res = Db.getInstance().getDataLongValue(sql, 0);
		
        return res;
    }
	
	//---------------------------------------------------
	public void start(Context context, int visitTypeID, boolean needDocCountCheck)
	{
		if(started == true)
			return;
		
		this.visitType = visitTypeID;
		this.needDocCountCheck = needDocCountCheck;
		visitID = getMaxVisitID() + 1;
		String sqlDate = Convert.getSqlDateTimeFromCalendar(Calendar.getInstance());
		
		String sql = "INSERT INTO Visits(VisitID, AddrID, VisitStartDate, VisitEndDate, VisitTypeMS) VALUES(?,?,?,?,?)";		
		Object[] bindArgs = new Object[] { visitID, AntContext.getInstance().getAddrID(), sqlDate, sqlDate, visitTypeID }; 
		Db.getInstance().execSQL(sql, bindArgs);		
		
		started = true;
	}

	//---------------------------------------------------	
	public void finish(Context context, long quitReason)
	{		
		if( !started || visitID == -1)
			return;
		
		String sqlDate = Convert.getSqlDateTimeFromCalendar(Calendar.getInstance());
		
		String sqlUpdate = "UPDATE Visits SET VisitEndDate=?, VisitEndReasonID=? WHERE VisitID=?"; 
		Object[] bindArgs = new Object[] { sqlDate, quitReason, visitID};
		Db.getInstance().execSQL(sqlUpdate, bindArgs);
		
		finished = true;
	}
	
	public Client getClient() { return client; }
	public Address getAddress()	{ return address; }
	public int getVisitType() { return visitType; }
	public void setVisitType(int visitType) { this.visitType = visitType; }
}
