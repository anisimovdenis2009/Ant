package com.app.ant.app.Activities;

import android.content.Context;
import android.database.Cursor;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.Convert;

import java.util.Calendar;


public class InfoPanelSaldo extends InfoPanelBase
{
	TextView textSaldo;
	TextView textOverdue;
	TextView textToday;
	TextView textPaid;
	
	
	protected void getFields()
	{
		textSaldo = (TextView) infoPanelLayout.findViewById(R.id.textSaldo);
		textOverdue = (TextView) infoPanelLayout.findViewById(R.id.textOverdue);
		textToday = (TextView) infoPanelLayout.findViewById(R.id.textToday);
		textPaid = (TextView) infoPanelLayout.findViewById(R.id.textPaid);
	}
	
	public void clearFields()
	{
		if(textSaldo!=null) textSaldo.setText("");
    	if(textOverdue!=null) textOverdue.setText("");
    	if(textToday!=null) textToday.setText("");
    	if(textPaid!=null) textPaid.setText("");
	}
	
	public void displayTotals()
	{
		
	}
	
	public void displayTotals(boolean unpaidOnly, boolean routeOnly, Calendar routeDate, long directionID)
	{
		String sql = DebtorListForm.getSaldoQuery(unpaidOnly, routeOnly, routeDate, true, directionID);
		
    	Cursor cursor = Db.getInstance().selectSQL(sql);
    	
    	clearFields();
    	
    	if(cursor!=null && cursor.getCount()>0)
    	{
	    	cursor.moveToPosition(0);

	    	if(textSaldo!=null) textSaldo.setText(Convert.moneyToString(cursor.getDouble(cursor.getColumnIndex("Saldo2"))));
	    	if(textOverdue!=null) textOverdue.setText(Convert.moneyToString(cursor.getDouble(cursor.getColumnIndex("Saldo6"))));
	    	if(textToday!=null) textToday.setText(Convert.moneyToString(cursor.getDouble(cursor.getColumnIndex("Saldo4"))));
	    	if(textPaid!=null) textPaid.setText(Convert.moneyToString(cursor.getDouble(cursor.getColumnIndex("PaidToday"))));
    	}

    	if(cursor!=null)
    		cursor.close();			
	}
	
	public void loadInfoPanel(Context context, InfoPanelBase prevPanel)
	{
		int rowHeight = Convert.dipToPixels(20);
		int minHeight = Convert.dipToPixels(30);
		int maxHeight = minHeight + rowHeight*2;
		
		int infoPanelResId = R.layout.info_panel_saldo;
		int infoPanelLayoutMainViewResId = R.id.infoPanelSaldo;
		
		//
		//create information panel
		//
		
		/*loadInfoPanel( context, prevPanel, R.id.infoPanelPlacement, infoPanelResId, infoPanelLayoutMainViewResId,
							rowHeight, maxHeight, minHeight);*/
		
		getFields();
	}		
}
