package com.app.ant.app.BusinessLayer;

import com.app.ant.app.DataLayer.Db;

import android.database.Cursor;


public class Contact
{
	public long contactID;
	public String person;
	public String position;
	public String phone;
	public String phone2;
	public String email;

	public Contact()
	{
	}
	
	public Contact(Long id)
	{
		String sql = String.format("SELECT ContactID, FIO, Position, Phone, Phone2, Email "
				  + " FROM Contacts "
				  +	" WHERE ContactID = %s", id);
		
		Cursor cursor = Db.getInstance().selectSQL(sql);
		
		if (cursor != null && cursor.getCount() > 0)
		{
			getFromCursor(cursor, 0);
		}
		
		if (cursor != null)
			cursor.close();
	}
	
	public void getFromCursor(Cursor cursor, int cursorPos)
	{
		cursor.moveToPosition(cursorPos);
		contactID = cursor.getLong(cursor.getColumnIndex("ContactID"));
		person = cursor.getString(cursor.getColumnIndex("FIO"));
		position = cursor.getString(cursor.getColumnIndex("Position"));
		phone = cursor.getString(cursor.getColumnIndex("Phone"));
		phone2 = cursor.getString(cursor.getColumnIndex("Phone2"));
		email = cursor.getString(cursor.getColumnIndex("Email"));
	}	
	

}
