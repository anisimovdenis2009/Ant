package com.app.ant.app.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.app.ant.R;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Settings;


public class DocSaleCommentsDialog  
{
	//cancel
	private DialogInterface.OnClickListener cancelClickListener = null;
	public void setCancelClickListener(DialogInterface.OnClickListener listener) { cancelClickListener = listener; }

	/**	��������� ��� ��������� ������ - �������� ����������*/  
    public interface OnInputCommentListener 
    {    	
        abstract void onCommentChanged(int commentIdx, String comments, String specMarks);
    } 
    private OnInputCommentListener inputCommentListener = null;	
	public void setInputCommentListener(OnInputCommentListener listener) { inputCommentListener = listener; }

	ViewGroup checkBoxPlacement;
	EditText textComment;
	
	private String mSpecMarks;	
	private String defaultSpecMarks;
	private String[] splittedMarks;
	
	private int mCommentIdx;
	//--------------------------------------------------------------	
    public Dialog onCreate(Context context, int commentIdx, String comments, String specMarks) 
    {
    	try
    	{
	    	mCommentIdx = commentIdx;
	    	mSpecMarks = (specMarks==null)?"" : specMarks;
	    	    	
			//create dialog
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			View layout = inflater.inflate(R.layout.doc_sale_comments, (ViewGroup) ((Activity)context).findViewById(R.id.docSaleComments));
			
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setView(layout);
			//builder.setMessage(((Activity)context).getResources().getString(R.string.form_title_comments));
			
			String cancelText = ((Activity)context).getResources().getString(R.string.doc_sale_comments_cancel);		
			builder.setNegativeButton(cancelText, new DialogInterface.OnClickListener() 
			{					
				@Override public void onClick(DialogInterface dialog, int which) 
				{
					if(cancelClickListener != null)
						cancelClickListener.onClick(dialog, which);
				}
			});	
			
			builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() 
			{					
				@Override public void onClick(DialogInterface dialog, int which) 
				{
					try
					{
						String comment = textComment.getText().toString(); 
						
						if(inputCommentListener != null)
							inputCommentListener.onCommentChanged(mCommentIdx, comment, mSpecMarks);
					}
					catch(Exception ex)
					{			
						ErrorHandler.CatchError("Exception in docSaleCommentsDialog", ex);
					}					
				}
			});
			
			
			checkBoxPlacement = (ViewGroup) layout.findViewById(R.id.checkBoxPlacement);
			textComment = (EditText) layout.findViewById(R.id.comment);
	
			if(comments!=null)
				textComment.setText(comments);
			
			Fill(context);
			
			return builder.create();
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in docSaleCommentsDialog.onCreate", ex);
		}
		
		return null;
    }
    
    //--------------------------------------------------------------    
    private void Fill(Context context)
    {
		//initialize list of checkboxes    	
    	defaultSpecMarks = Settings.getInstance().getPropertyFromSettings(Settings.PROPNAME_SPEC_MARKS);    	
    	splittedMarks = defaultSpecMarks.split(";"); 
    	
        CheckBox.OnCheckedChangeListener checkListener = new CheckBox.OnCheckedChangeListener() 
        {
    		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
    		{
    			try
    			{
	    			int idx = (Integer) buttonView.getTag();
	    			String mark = splittedMarks[idx];
	    			
	    			if(isChecked)
	    			{
		    			if(! mSpecMarks.contains(mark))
		    			{
		    				String prefix = mSpecMarks.length() == 0 ? "" : ";";
		    				mSpecMarks = mSpecMarks + prefix + mark;
		    			}
	    			}
	    			else
	    			{
	                    String prefix = mSpecMarks.lastIndexOf(";" + mark) == -1 ? "" : ";";
	                    String sufix = prefix == ";" || (mSpecMarks.compareTo(mark) == 0) ? "" : ";";
	                    mSpecMarks = mSpecMarks.replace(prefix + mark + sufix, "");    				
	    			}
    			}
    			catch(Exception ex)
    			{			
    				ErrorHandler.CatchError("Exception in docSaleCommentsDialog", ex);
    			}    			
    		}
        };
        
		for(int i=0; i<splittedMarks.length; i++)
		{
			CheckBox checkBox = new CheckBox(context);
			checkBox.setText(splittedMarks[i]);
			checkBox.setOnCheckedChangeListener(checkListener);
			checkBox.setTag(i);
			checkBox.setChecked( mSpecMarks.contains(splittedMarks[i]) );
			checkBoxPlacement.addView(checkBox);
		}
    	
    }

    //--------------------------------------------------------------    
}

