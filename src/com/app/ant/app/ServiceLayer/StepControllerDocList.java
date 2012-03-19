package com.app.ant.app.ServiceLayer;

import com.app.ant.R;
import com.app.ant.app.Activities.DocListForm;
import com.app.ant.app.BusinessLayer.Document;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class StepControllerDocList extends StepController
{
    //--------------------------------------------------------------
	public	StepControllerDocList()
	{
		addStep(new StepInfo(0, R.string.visit_step_docSaleList, R.drawable.step_doc_list,  STEP_INVISIBLE, false));
		addStep(new StepInfo(1, R.string.doc_list_button_editDoc, R.drawable.doc_edit, STEP_ACTION, false));
		addStep(new StepInfo(2, R.string.doc_list_button_deleteDoc, R.drawable.doc_delete, STEP_ACTION, false));
		addStep(new StepInfo(3, R.string.doc_list_button_payment, R.drawable.step_doc_payment, STEP_ACTION, false));
		addStep(new StepInfo(4, R.string.doc_list_button_printDoc, R.drawable.print, STEP_ACTION, false));
		addStep(new StepInfo(5, R.string.doc_list_button_discount, R.drawable.discount, STEP_ACTION | STEP_INVISIBLE, false));
	}
	
    //--------------------------------------------------------------
	@Override public boolean startStep(Context context, int stepNum, boolean fromTabController, Bundle params)
	{		
		switch(stepNum)
		{		
			case 0:	//default form
				context.startActivity(new Intent(context, DocListForm.class));
				FinishPrevActivity(context);
				break;
			case 1:
				if(context instanceof DocListForm)
					((DocListForm)context).onEditDoc();
				break;
			case 2:
				if(context instanceof DocListForm)
					((DocListForm)context).onDeleteDoc();
				break;				
			case 3:
				if(context instanceof DocListForm)
					((DocListForm)context).onCreateDoc(Document.DOC_TYPE_PAYMENT);
				break;
			case 4:
				if(context instanceof DocListForm)
					((DocListForm)context).onPrintDoc();
				break;			
		}
		
		return super.startStep(context, stepNum, fromTabController, params);
	}
}