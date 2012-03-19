package com.app.ant.app.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.widget.CheckBox;
import android.widget.RadioButton;
import com.app.ant.R;
import com.app.ant.app.BusinessLayer.Document;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Printer;
import com.app.ant.app.ServiceLayer.Printer.PrintableDocumentType;


public class PrintDocForm extends DialogBase
{
	private long docIdI = 0;
	private long docIdII = 0;
	
    //--------------------------------------------------------------	
	public Dialog onCreate(final Context context, final long docId)
	{
		String title = context.getResources().getString(R.string.printer_title);
		
		Dialog dlg = super.onCreate(context, R.layout.print_invoice_form, R.id.printInvoiceForm, 
				 					title, DIALOG_FLAG_HAVE_OK_BUTTON | DIALOG_FLAG_HAVE_CANCEL_BUTTON );			

		//
		//init controls
		//
		final RadioButton radioBill = (RadioButton) super.findViewById(R.id.print_bill_option);
		final RadioButton radioInvoice = (RadioButton) super.findViewById(R.id.print_invoice_option);
		
		final CheckBox checkBoxVatI = (CheckBox) super.findViewById(R.id.print_withVAT_I);
		final CheckBox checkBoxWOVatI = (CheckBox) super.findViewById(R.id.print_WOVAT_I);
		final CheckBox checkBoxVatII = (CheckBox) super.findViewById(R.id.print_withVAT_II);
		final CheckBox checkBoxWOVatII = (CheckBox) super.findViewById(R.id.print_WOVAT_II);		
		
		radioBill.setChecked(true);
		radioInvoice.setChecked(false);		
		
		//
		// read params from database and set control state 
		//
		
		boolean haveDocI = false;
		boolean haveDocII = false;
		
		String sql =    " SELECT d.BWG AS BWG1, d.ParentDocID, d2.BWG AS BWG2, d2.DocID AS DocID2" +
						" FROM Documents d " +
						" LEFT JOIN Documents d2 ON d2.ParentDocID=d.ParentDocID AND d2.DocID!=d.docID " +
						" WHERE d.DocID= " + docId;
		
    	Cursor cursor = Db.getInstance().selectSQL(sql);
    	
    	if(cursor!=null)
    	{
    		if(cursor.getCount()!=0)
    		{
    			cursor.moveToPosition(0);
    			char BWG1 = Convert.getDocColorFromString(cursor.getString(cursor.getColumnIndex("BWG1")));

    			if(BWG1 == Document.DOC_COLOR_WHITE)
    			{
    				haveDocI = true;
    				docIdI = docId;
    			}
    			else if(BWG1 == Document.DOC_COLOR_BLACK)
    			{
    				haveDocII = true;
    				docIdII = docId;
    			}
    			
    			if(!cursor.isNull(cursor.getColumnIndex("DocID2")))
    			{
    				//process second document
    				long docId2 = cursor.getLong(cursor.getColumnIndex("DocID2"));
    				char BWG2 = Convert.getDocColorFromString(cursor.getString(cursor.getColumnIndex("BWG2")));    				
    				
        			if(BWG2 == Document.DOC_COLOR_WHITE)
        			{
        				haveDocI = true;
        				docIdI = docId2;
        			}
        			else if(BWG1 == Document.DOC_COLOR_BLACK)
        			{
        				haveDocII = true;
        				docIdII = docId2;
        			}
    			}   			
    		}
    		
    		cursor.close();
    		cursor = null;
    	}

    	//check if vat checkboxes should be enabled
    	boolean enableVatI = false;
    	boolean enableWOVatI = false;
    	boolean enableVatII = false;
    	boolean enableWOVatII = false;
    	
    	if(haveDocI)
    	{
    		if(Document.hasLinesWithVat(docIdI, 0.2))
    			enableVatI = true;
    		if(Document.hasLinesWithVat(docIdI, 0))
    			enableWOVatI = true;
    	}
    	
    	if(haveDocII)
    	{
    		if(Document.hasLinesWithVat(docIdII, 0.2))
    			enableVatII = true;
    		if(Document.hasLinesWithVat(docIdII, 0))
    			enableWOVatII = true;
    	}
    	
		checkBoxVatI.setChecked(enableVatI);					
		checkBoxWOVatI.setChecked(enableWOVatI);
		checkBoxVatII.setChecked(enableVatII);
		checkBoxWOVatII.setChecked(enableWOVatII);

		checkBoxVatI.setEnabled(enableVatI);					
		checkBoxWOVatI.setEnabled(enableWOVatI);
		checkBoxVatII.setEnabled(enableVatII);
		checkBoxWOVatII.setEnabled(enableWOVatII);		
		
		super.setOkClickListener(new DialogInterface.OnClickListener() 
		{					
			@Override public void onClick(DialogInterface dialog, int which) 
			{
				try
				{
					PrintableDocumentType docType = radioBill.isChecked() ? PrintableDocumentType.Bill : 
													(radioInvoice.isChecked() ? PrintableDocumentType.Invoice : PrintableDocumentType.Undefined);					
					
					if(docType!=PrintableDocumentType.Undefined)
					{
			        	new Printer(context).printInvoices(context, docType, 
														docIdI, checkBoxVatI.isChecked(), checkBoxWOVatI.isChecked(), 
														docIdII, checkBoxVatII.isChecked(), checkBoxWOVatII.isChecked());
					}
				}
				catch(Exception ex)
				{
					MessageBox.show(context, context.getResources().getString(R.string.printer_title), context.getResources().getString(R.string.printer_exception));
					ErrorHandler.CatchError("Exception in PrintDocForm.print.onClick", ex);					
				}
				
			}
		});

		return dlg;
	}
}	