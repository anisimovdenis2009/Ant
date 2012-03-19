package com.app.ant.app.Activities;


import com.app.ant.R;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.MLog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class FeedbackForm extends AntActivity
{
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    { 
    	super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	this.setContentView(R.layout.feedback_form);

    	final EditText editTheme = (EditText) findViewById(R.id.textTheme);		
		final EditText editMessege = (EditText) findViewById(R.id.textMessage);
		
		final Activity activity = this; 
		
		Button buttonSend = (Button) findViewById(R.id.buttonSend);
		buttonSend.setOnClickListener( new View.OnClickListener() 
		{			
			@Override public void onClick(View v) 
			{	
				try
				{
					String messageTheme = "Theme:" + editTheme.getText() + ";";
					String messageText = "Text:" + editMessege.getText();
					String message = messageTheme + messageText;
					
					MLog.WriteLog(MLog.LOG_TYPE_FEEDBACK, message);

					
					String toastMessage = getResources().getString(R.string.feedback_sent);
					Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show();

					editTheme.setText("");
					editMessege.setText("");
				}
				catch (Exception ex)
				{
					ErrorHandler.CatchError("FeedbackForm::buttonSend.setOnClickListener", ex);
				}
			}
		});
    }
    
    @Override 
    public void onBackPressed() 
    {
		this.finish();	
    }
}
