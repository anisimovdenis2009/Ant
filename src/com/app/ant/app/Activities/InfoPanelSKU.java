package com.app.ant.app.Activities;

import com.app.ant.R;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.widget.TextView;


public class InfoPanelSKU extends InfoPanelBase
{
	TextView textItemName;
	TextView textExt;
	TextView textLicense;
	TextView textCashName;
	TextView textPerPall;
	TextView textPerCase;
	TextView textVolume;
	TextView textUnitWeight;
	TextView textQuantity;
	TextView textUnitName;
	TextView textKeepTime;
	TextView textVATID;
	
	String strUnitName = "";
	
	
	protected void getFields()
	{
		textItemName = (TextView) infoPanelLayout.findViewById(R.id.textItemName);
		textExt = (TextView) infoPanelLayout.findViewById(R.id.textItemExt);
		textLicense = (TextView) infoPanelLayout.findViewById(R.id.textItemLicense);
		textCashName = (TextView) infoPanelLayout.findViewById(R.id.textItemCashName);
		textPerPall = (TextView) infoPanelLayout.findViewById(R.id.textItemPerPall);
		textPerCase = (TextView) infoPanelLayout.findViewById(R.id.textItemPerCase);
		textVolume = (TextView) infoPanelLayout.findViewById(R.id.textItemExt);
		textUnitWeight = (TextView) infoPanelLayout.findViewById(R.id.textItemUnitWeight);
/*		
		textQuantity = (TextView) infoPanelLayout.findViewById(R.id.textItemQuantity);
		textUnitName = (TextView) infoPanelLayout.findViewById(R.id.textItemUnitName);
		textKeepTime = (TextView) infoPanelLayout.findViewById(R.id.textItemKeepTime);
*/		
		textVATID = (TextView) infoPanelLayout.findViewById(R.id.textItemVATID);
	}
	
	public void clearFields()
	{
		if(textItemName!=null) textItemName.setText("");
    	if(textExt!=null) textExt.setText("");
    	if(textLicense!=null) textLicense.setText("");
    	if(textCashName!=null) textCashName.setText("");
    	if(textPerPall!=null) textPerPall.setText("");
    	if(textPerCase!=null) textPerCase.setText("");
    	if(textVolume!=null) textVolume.setText("");
    	if(textUnitWeight!=null) textUnitWeight.setText("");
/*    	
    	if(textQuantity!=null) textQuantity.setText("");
    	if(textUnitName!=null) textUnitName.setText( strUnitName );
    	if(textKeepTime!=null) textKeepTime.setText("");
*/    	
    	if(textVATID!=null) textVATID.setText("");
	}
	

	public void displayTotals()
	{
    	//get data from Items table    			
		String itemID = Integer.toString(AntContext.getInstance().curItemId);
		
    	String sql =
			" SELECT i.ItemExt, i.License, i.CashName, i.ScreenName, i.ItemName, i.PerPall, i.PerCase, i.Volume, " +
					" round(i.UnitWeight,3) AS UnitWeight, i.ItemTax, i.ShelfLife, r.Rest-coalesce(r.SaledQnt,0) AS Rest " +
			" FROM Items i " +
			" LEFT JOIN Rest r ON r.ItemID = i.ItemID " +
			" WHERE i.ItemID = " + itemID;
    	
    	Cursor cursor = Db.getInstance().selectSQL(sql);
    	
    	clearFields();
    	
    	if(cursor!=null && cursor.getCount()>0)
    	{
	    	cursor.moveToPosition(0);

	    	if(textItemName!=null) textItemName.setText(cursor.getString(cursor.getColumnIndex("ItemName")));
	    	if(textExt!=null) textExt.setText(cursor.getString(cursor.getColumnIndex("ItemExt")));
	    	if(textLicense!=null) textLicense.setText(cursor.getString(cursor.getColumnIndex("License")));
	    	if(textCashName!=null) textCashName.setText(cursor.getString(cursor.getColumnIndex("CashName")));
	    	if(textPerPall!=null) textPerPall.setText(cursor.getString(cursor.getColumnIndex("PerPall")));
	    	if(textPerCase!=null) textPerCase.setText(cursor.getString(cursor.getColumnIndex("PerCase")));
	    	if(textVolume!=null) textVolume.setText(cursor.getString(cursor.getColumnIndex("Volume")));
	    	if(textUnitWeight!=null) textUnitWeight.setText(cursor.getString(cursor.getColumnIndex("UnitWeight")));
/*	    	
	    	if(textQuantity!=null) textQuantity.setText(cursor.getString(cursor.getColumnIndex("Rest")));
	    	if(textUnitName!=null) textUnitName.setText( strUnitName );
	    	if(textKeepTime!=null) textKeepTime.setText(cursor.getString(cursor.getColumnIndex("ShelfLife")));
*/	    	
	    	//VAT
	    	if(textVATID!=null)
	    	{
		    	int VATcolumn = cursor.getColumnIndex("ItemTax");
		    	if(!cursor.isNull(VATcolumn))
		    	{
		    		String VAT = String.format( "%d", (int) (cursor.getDouble(VATcolumn)*100));
		    		textVATID.setText(VAT);
		    	}
		    	else 
		    		textVATID.setText("");
	    	}
    	}

    	if(cursor!=null)
    		cursor.close();			
	}
	
	public void loadInfoPanel(Context context, InfoPanelBase prevPanel)
	{
		int rowHeight = Convert.dipToPixels(20);
		int minHeight = Convert.dipToPixels(30);
		int maxHeight = minHeight + rowHeight*6;
		
		int infoPanelResId = R.layout.item_info_panel;
		int infoPanelLayoutMainViewResId = R.id.itemInfoPanel;
		
		//
		//create information panel
		//
		
		/*loadInfoPanel( context, prevPanel, R.id.infoPanelPlacement, infoPanelResId, infoPanelLayoutMainViewResId,
							rowHeight, maxHeight, minHeight);*/
		
		getFields();

		strUnitName = ((Activity)context).getResources().getString(R.string.item_unitNameValue); 
	}		
}
