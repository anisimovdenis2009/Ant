package com.app.ant.app.ServiceLayer;

import android.content.Context;
import android.database.Cursor;
import com.app.ant.app.DataLayer.Db;

import java.util.ArrayList;


public abstract class CashRegister 
{
	final static int MAX_CHECK_ITEMS = 100;
	
	//--------------------------------------------------------------------------

	public static class CheckItem
	{
		public int orders;
		double discount;
		double price;
		int vatIndex;
		String cashName; 
		int cashCode;
		
		public CheckItem()
		{			
		}
		
		public CheckItem(int orders, double discount, double price, int vatIndex, String cashName, int cashCode)
		{
			this.orders = orders;
			this.discount = discount;
			this.price = price;
			this.vatIndex = vatIndex;
			this.cashName = cashName;
			this.cashCode = cashCode;
		}
	}
	
	//--------------------------------------------------------------------------
	public static double printDocument(Context context, long docID, double checkSum, double docSum)
	{
		docSum = Math.min(checkSum, docSum);
		
		//
		//fill cashCodes table with items that aren't present there yet
		//
		{
			String sql = " SELECT max(CashCode) AS CashCode FROM CashCodes ";
			long cashCode = Db.getInstance().getDataLongValue(sql, 0);
			
			sql = " SELECT i.ItemID, coalesce(i.CashName,'Pampers') AS CashName, i.ItemTax " +    
						 " FROM Items i " + 
						 " INNER JOIN DocDetails dets ON i.ItemID = dets.ItemID " +
						 " WHERE dets.DocID = " + docID + "  AND dets.Orders > 0 " +
								" AND dets.ItemID NOT IN (SELECT cc.ItemID FROM CashCodes cc WHERE ItemID=dets.ItemID)";
			
	    	Cursor cursor = Db.getInstance().selectSQL(sql);
	    	if(cursor!=null && cursor.getCount()!=0)
	    	{
	    		try
	    		{
		    		Db.getInstance().beginTransaction();
		    		
		    		for(int i=0; i<cursor.getCount(); i++)
		    		{
		    			cursor.moveToPosition(i);
	
		    			long itemID = cursor.getLong(cursor.getColumnIndex("ItemID"));
			    		String cashName = cursor.getString(cursor.getColumnIndex("CashName"));
			    		double vat = cursor.getDouble(cursor.getColumnIndex("ItemTax"));
			    		
			    		cashCode++;
			    		
			    		sql = "INSERT INTO CashCodes (ItemID, CashCode, CashName, ItemTax) VALUES (?,?,?,?)";
						Object[] bindArgs = new Object[] { itemID, cashCode, cashName, vat };
						Db.getInstance().execSQL(sql, bindArgs);					
		    		}
		    		
		    		Db.getInstance().commitTransaction();
				}
				catch(Exception ex)
				{
					throw new RuntimeException(ex);
				}						
				finally
				{
					Db.getInstance().endTransaction();
				}	    		
	    	}   	
		}
		
		//
		// add lines to print cache
		//
		
		String sql = "SELECT cash.CashName, cash.ItemTax, dets.Price, cash.CashCode, dets.Discount, dets.Orders " +
        			" FROM Items i " +
             		" INNER JOIN DocDetails dets ON i.ItemID = dets.ItemID " +
             		" INNER JOIN CashCodes cash on i.ItemID = cash.ItemID " + 
             		" WHERE dets.DocID = " + docID + " AND dets.Orders > 0 " +
             		" ORDER BY dets.Price*dets.Orders";

    	Cursor cursor = Db.getInstance().selectSQL(sql);
    	if(cursor!=null && cursor.getCount()!=0)
    	{
    		ArrayList<CheckItem> checkItems = new ArrayList<CheckItem>();
    		double sumPrinted = 0;
    		boolean finishing = false;
    		CheckItem checkItem = new CheckItem();
    		int i;
    		double sumToPrint = 0;
    		
    		for(i=0; i<cursor.getCount(); i++)
    		{
    			cursor.moveToPosition(i);
    			
	    		int cashCode = cursor.getInt(cursor.getColumnIndex("CashCode"));
	    		String cashName = cursor.getString(cursor.getColumnIndex("CashName"));
	    		int orders = cursor.getInt(cursor.getColumnIndex("Orders"));
	    		double discountPercent = Convert.roundUpMoney(cursor.getDouble(cursor.getColumnIndex("Discount")));
	    		double price = Convert.roundUpMoney(cursor.getDouble(cursor.getColumnIndex("Price")));
	    		double vat = cursor.getDouble(cursor.getColumnIndex("ItemTax"));
	    		
	            int vatIndex = (vat == 0) ? 1 : 0;
	
	            double discount = 0;
	            double discountedPrice = Convert.roundUpMoney((1 + vat) * Convert.roundUpMoney(price * (1 - discountPercent / 100)));
	
	            double lineSum = Convert.roundUpMoney(discountedPrice*orders);
	            sumPrinted += lineSum;
	
	            if (docSum <= sumPrinted)
	            {
	                discount = Convert.roundUpMoney(docSum - sumPrinted);
	                finishing = true;
	            }
	            
	            checkItem = new CheckItem(orders, discount, discountedPrice, vatIndex, cashName, cashCode);
	            checkItems.add(checkItem);
	
	            if (finishing || i >= MAX_CHECK_ITEMS-1) 
	            	break;
    		}
    		
            if (docSum > sumPrinted && i < MAX_CHECK_ITEMS-1)
                checkItem.discount = Convert.roundUpMoney(docSum - sumPrinted);
            
            //
            //send data to cash register to print
            //
            sumToPrint = Convert.roundUpMoney(sumPrinted) + checkItem.discount;            
            CashRegisterDatecs.printFiscalCheck(context, checkItems, sumToPrint, false);            
            
            return sumToPrint;
    	}
    	
    	if(cursor!=null)
    		cursor.close();

		return 0;
	}
}

