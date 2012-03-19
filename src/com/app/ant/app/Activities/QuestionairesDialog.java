package com.app.ant.app.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ViewGroup;

import com.app.ant.R;
import com.app.ant.app.BusinessLayer.Questionnaire;
import com.app.ant.app.ServiceLayer.ErrorHandler;


public class QuestionairesDialog extends DialogBase 
{
	private Questionnaire questionnaire;
	
    public interface OnProcessQuestionnaire 
    {    	
        abstract void onQuestionnaireLoaded(Questionnaire questionnaire);
        abstract void onQuestionnaireConfirmed(Questionnaire questionnaire);
    }

	//--------------------------------------------------------------	
	public Dialog onCreate(final Context context, long questionnaireID, String title, 
							final OnProcessQuestionnaire processQuestionnaireCallback,
						 	final DialogInterface.OnClickListener cancelClickListener)
    {
    	try
    	{
			//create dialog
			Dialog dlg = super.onCreate(context, R.layout.questionnaire_dialog, R.id.questionnaire, 
 										"", DIALOG_FLAG_HAVE_OK_BUTTON | DIALOG_FLAG_HAVE_CANCEL_BUTTON | DIALOG_FLAG_NO_TITLE );
			
			super.setCancelClickListener(cancelClickListener);
			
			super.setOkClickListener(new DialogInterface.OnClickListener() 
			{					
				@Override public void onClick(DialogInterface dialog, int which) 
				{
					if(processQuestionnaireCallback!=null)
						processQuestionnaireCallback.onQuestionnaireConfirmed(questionnaire);				
				}
			});	
			
			ViewGroup questPlacement = (ViewGroup) super.findViewById(R.id.questionsPlacement);			
		
			questionnaire = new Questionnaire(questionnaireID, null, false);
			
			questPlacement.removeAllViews();
			questionnaire.addViews(context, questPlacement, title);
			
			if(processQuestionnaireCallback!=null)
				processQuestionnaireCallback.onQuestionnaireLoaded(questionnaire);
			
			return dlg;
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in QuestionairesDialog.onCreate", ex);
		}
    	
    	return null;
    }  
}

