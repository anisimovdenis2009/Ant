package com.app.ant.app.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.ServiceLayer.ErrorHandler;


public class DocSaleStatsDialog  
{
	private DialogInterface.OnClickListener okClickListener = null;
	public void setOkClickListener(DialogInterface.OnClickListener listener) { okClickListener = listener; }

	//--------------------------------------------------------------	
    public Dialog onCreate(Context context) 
    {
    	try
    	{
			//create dialog
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			View layout = inflater.inflate(R.layout.doc_sale_statistics, (ViewGroup) ((Activity)context).findViewById(R.id.docSaleStats));
			
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setView(layout);
			builder.setMessage(((Activity)context).getResources().getString(R.string.form_title_statistics));
			
			String okText = ("Ok");		
			builder.setPositiveButton(okText, new DialogInterface.OnClickListener() 
			{					
				@Override public void onClick(DialogInterface dialog, int which) 
				{
					if(okClickListener != null)
						okClickListener.onClick(dialog, which);
				}
			});
			
			//get controls
			TextView textSum = (TextView) layout.findViewById(R.id.textSum);
			TextView textSumI = (TextView) layout.findViewById(R.id.textSumI);
			TextView textSumII = (TextView) layout.findViewById(R.id.textSumII);
			TextView textSumVAT = (TextView) layout.findViewById(R.id.textSumVAT);
			TextView textSumWoVAT = (TextView) layout.findViewById(R.id.textSumWoVAT);
			TextView textCases = (TextView) layout.findViewById(R.id.textCases);
			TextView textPalettes = (TextView) layout.findViewById(R.id.textPalettes);
			TextView textUnits = (TextView) layout.findViewById(R.id.textUnits);
			TextView textSumSUF = (TextView) layout.findViewById(R.id.textSumSUF);
			TextView textSumFUF = (TextView) layout.findViewById(R.id.textSumFUF);
			TextView textOrderItems = (TextView) layout.findViewById(R.id.textOrderItems);
			TextView textFormItems = (TextView) layout.findViewById(R.id.textFormItems);
			TextView textAllItems = (TextView) layout.findViewById(R.id.textAllItems);
			
			//set values
			textSum.setText("0");
			textSumI.setText("0");
			textSumII.setText("0");
			textSumVAT.setText("0");
			textSumWoVAT.setText("0");
			textCases.setText("0");
			textPalettes.setText("0");
			textUnits.setText("0");
			textSumSUF.setText("0");
			textSumFUF.setText("0");
			textOrderItems.setText("0");
			textFormItems.setText("0");
			textAllItems.setText("0");
	
			return builder.create();
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in docSaleStatsDialog.onCreate", ex);
		}

		return null;
    }
}

