package com.app.ant.app.BusinessLayer;

import android.database.Cursor;

import com.app.ant.app.DataLayer.Db;


public class Client  
{
	public long clientID;
	public String nameScreen;
	public String regDate;
	public String regNo;
	public String taxNo;
	public String subjCode;
	public String erpId;
	
	public Client(long clientID)
	{
		this.clientID = clientID;
		
		refreshProps();
	}

	public void refreshProps()
	{
		String sql = " SELECT c.NameScreen, c.NamePrint, c.RegNo, c.RegDate, c.TaxNo, c.Comment, c.BankInfo, c.SubjCode, c.ERPID " +
		 			 " FROM Clients c " + 
		 			 " WHERE c.ClientID = " + clientID;

		Cursor cursor = Db.getInstance().selectSQL(sql);
		if(cursor != null && cursor.getCount() > 0)
		{
			cursor.moveToPosition(0);
			
			nameScreen = cursor.getString(cursor.getColumnIndex("NameScreen"));
			regDate = cursor.getString(cursor.getColumnIndex("RegDate"));
			regNo = cursor.getString(cursor.getColumnIndex("RegNo"));
			taxNo = cursor.getString(cursor.getColumnIndex("TaxNo"));
			subjCode = cursor.getString(cursor.getColumnIndex("SubjCode"));
			erpId = cursor.getString(cursor.getColumnIndex("ERPID"));
		}
		
		if(cursor!=null)
			cursor.close();		
		
	}
}