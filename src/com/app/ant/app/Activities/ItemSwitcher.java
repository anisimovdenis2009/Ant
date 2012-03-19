package com.app.ant.app.Activities;

import java.util.ArrayList;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.Activities.MessageBox.MessageBoxButton;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.ErrorHandler;

public class ItemSwitcher
{		
	//------------------------------------------------------------------------------------
    public interface OnItemSwitch 
    {    	
        abstract void onItemSelected(ItemSwitcherElement item);
    }
	
    //------------------------------------------------------------------------------------
	public class ItemSwitcherElement
	{
		public long id;
		public String name;
		
		public ItemSwitcherElement(long id, String name)
		{
			this.id = id;
			this.name = name;
		}
	}
	
	//------------------------------------------------------------------------------------
	private ArrayList<ItemSwitcherElement> items = new ArrayList<ItemSwitcherElement>();
	private int currentItemIdx = 0; 
	
	private TextView textName;
	private int itemChoiceIdx = 0;
	
	//------------------------------------------------------------------------------------
	public ItemSwitcher(final Context context, String sql, long defaultItemID, View buttonPrev, View buttonNext, TextView textName, final OnItemSwitch itemSwitchCallback)
	{
		this.textName = textName;		
		
		Cursor cursor = Db.getInstance().selectSQL(sql);    	
    	if(cursor!=null)
    	{
    		int idColumnIdx = cursor.getColumnIndex("ItemID");
    		int nameColumnIdx = cursor.getColumnIndex("ItemName");
    		
    		for(int i=0; i<cursor.getCount(); i++)
    		{
    			cursor.moveToPosition(i);
    			long id = cursor.getLong(idColumnIdx);
    			String name = cursor.getString(nameColumnIdx);
    			
    			if(id == defaultItemID)
    				items.add(0,new ItemSwitcherElement(id, name));
    			else
    				items.add(new ItemSwitcherElement(id, name));
    		}
	    		
    		cursor.close();		    	
    	}
    	
    	updateName();
    	
    	if(buttonPrev!=null)
    	{    	
	        buttonPrev.setOnClickListener( new View.OnClickListener() 
			{				
				@Override public void onClick(View v) 
				{
					try
					{
						if(switchToPrevItem())
						{
							updateName();
							if(itemSwitchCallback!=null)
								itemSwitchCallback.onItemSelected(getCurrentItem());
						}
					}
					catch(Exception ex)
					{
						ErrorHandler.CatchError("Exception in ItemSwitcher.buttonPrevItem.onClick", ex);			
					}
				}
			});
    	}
    	
    	if(buttonNext!=null)
    	{    	
	        buttonNext.setOnClickListener( new View.OnClickListener() 
			{				
				@Override public void onClick(View v) 
				{
					try
					{
						if(switchToNextItem())
						{
							updateName();
							if(itemSwitchCallback!=null)
								itemSwitchCallback.onItemSelected(getCurrentItem());
						}
					}
					catch(Exception ex)
					{
						ErrorHandler.CatchError("Exception in ItemSwitcher.buttonNextItem.onClick", ex);			
					}
				}
			});
    	}
    	
    	if(textName!=null)
    	{
	        textName.setOnClickListener( new View.OnClickListener() 
			{				
				@Override public void onClick(View v) 
				{
					MessageBoxButton[] buttons = new MessageBoxButton[]
                    {
        				new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, context.getResources().getString(R.string.visit_start_visit_select),
  						new DialogInterface.OnClickListener()
  						{
  							@Override public void onClick(DialogInterface dialog, int which) 
  							{
  								currentItemIdx = itemChoiceIdx;
  								updateName();
  								if(itemSwitchCallback!=null)
  									itemSwitchCallback.onItemSelected(getCurrentItem());
  							}
  						}),
        				new MessageBoxButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(R.string.visit_start_visit_cancel),
  						new DialogInterface.OnClickListener()
  						{
  							@Override public void onClick(DialogInterface dialog, int which) { }
  						})								
                    };
          			                                          		
              		CharSequence[] itemNames = new CharSequence[items.size()];

              		for(int i=0; i<items.size(); i++ )
              		{
              			itemNames[i] = items.get(i).name;
              		}
					
		    		MessageBox.show(context, context.getResources().getString(R.string.debtor_list_select_direction), 
							context.getResources().getString(R.string.debtor_list_select_direction), 
							buttons,
							null,
							itemNames,
							currentItemIdx,
							new DialogInterface.OnClickListener() 
							{
						    	public void onClick(DialogInterface dialog, int item) 
						    	{
						    		itemChoiceIdx = item;
						    	}
							}
					);
				}
			});
    	}

	}
	
    //------------------------------------------------------------------------------------
    public void updateName()
    {   	
    	if(textName!=null)
    		textName.setText(getCurrentItemName());
    }
	
    //------------------------------------------------------------------------------------
	public long getCurrentItemID()
	{
		ItemSwitcherElement currentItem = getCurrentItem();
		return currentItem == null ? 0 : currentItem.id;
	}
	
    //------------------------------------------------------------------------------------
	public String getCurrentItemName()
	{
		ItemSwitcherElement currentItem = getCurrentItem();
		return currentItem == null ? "" : currentItem.name;
	}
	
    //------------------------------------------------------------------------------------
	private ItemSwitcherElement getCurrentItem()
	{
		if(currentItemIdx>=0 && currentItemIdx<items.size())
			return items.get(currentItemIdx);
		
		return null;
	}	
	
	//------------------------------------------------------------------------------------
	public boolean switchToNextItem()
	{
		if(currentItemIdx+1 <items.size())
		{
			currentItemIdx++;
			return true;
		}

		return false;
	}
	
	//------------------------------------------------------------------------------------
	public boolean switchToPrevItem()
	{
		if(currentItemIdx>0)
		{
			currentItemIdx--;
			return true;
		}
		
		return false;
	}		
}	
