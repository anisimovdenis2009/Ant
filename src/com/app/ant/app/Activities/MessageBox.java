package com.app.ant.app.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;


public class MessageBox
{
	/** ������ ��� ����������� �� ����������� ����*/
	public static class MessageBoxButton
	{
		int whichButton;
		CharSequence text;
		DialogInterface.OnClickListener listener;
		
		public MessageBoxButton(int whichButton, CharSequence text, DialogInterface.OnClickListener listener)
		{
			this.whichButton = whichButton;
			this.text = text;
			this.listener = listener;
		}
	}
	
	public static void show(Context context, String title, String message)
	{
		MessageBoxButton[] buttons = new MessageBoxButton[] 
		{ new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, "Ok", 
			new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{}
			})
		};
		
		show(context, title, message, buttons);
	}
	
	public static void show(Context context, String title, String message, MessageBoxButton[] buttons)
	{
		show(context, title, message, buttons, null, null, 0, null);
	}
	
	public static void show(Context context, String title, String message, MessageBoxButton[] buttons, 
							DialogInterface.OnCancelListener cancelListener, 
							CharSequence[] choiceItems,
							int checkedItem,
							DialogInterface.OnClickListener choiceListener)
	{
		try
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			if(choiceItems!=null)
			{
				builder.setSingleChoiceItems( choiceItems, checkedItem, choiceListener);				
			}
			
			AlertDialog alertDialog = builder.create();			
			alertDialog.setTitle(title);
			
			if(choiceItems==null)
				alertDialog.setMessage(message);
			
			if(buttons!=null)
			{
				for(int i=0; i<buttons.length; i++)
					alertDialog.setButton(buttons[i].whichButton, buttons[i].text, buttons[i].listener);
			}
			
			if(cancelListener!=null)
				alertDialog.setOnCancelListener( cancelListener );
			
			alertDialog.show();
		}
		catch(Exception ex)
		{			
			Log.e(title, message);
		}  	
	}	
}
