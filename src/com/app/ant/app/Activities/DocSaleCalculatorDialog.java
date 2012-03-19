package com.app.ant.app.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import com.app.ant.R;
import com.app.ant.app.Controls.DataGrid;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;

public class DocSaleCalculatorDialog  
{
    private static enum CalcOperation 
    {
        Plus, Minus, Multiply, Divide, None
    }
	
	public static final int FLAGS_NONE=0;
	public static final int FLAGS_SHOW_APPLY_TO_ALL_CHECKBOX=1;
	
	//cancel
	private DialogInterface.OnClickListener cancelClickListener = null;
	public void setCancelClickListener(DialogInterface.OnClickListener listener) { cancelClickListener = listener; }
	
	/**	��������� ��� ��������� ������ - �������� ����������*/   
    public interface OnCalcResultListener 
    {    	
        abstract void onCalcResult(Object calcValue, boolean applyToAll);
    } 
    private OnCalcResultListener calcResultListener = null;	
	public void setCalcResultListener(OnCalcResultListener listener) { calcResultListener = listener; }	
	
    private CalcOperation cOper = CalcOperation.None;        
    private CalcOperation cOperPrev = CalcOperation.None;

    private double dFirst = 0;
    private double dSecond = 0;	

	//--------------------------------------------------------------	
    public Dialog onCreate(Context context, String title, int flags, String value, int valueType) 
    {
    	try
    	{    		
    		//create dialog    	
	    	final Activity contextActivity = ((Activity)context);
	    	Resources res = contextActivity.getResources();    	
	    	
	    	LayoutInflater inflater = contextActivity.getLayoutInflater();
			View layout = inflater.inflate(R.layout.inputpopup, (ViewGroup)contextActivity.findViewById(R.id.InputPopUp));		
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setView(layout);
			builder.setMessage(title);
			
			final EditText inputValue = (EditText) layout.findViewById(R.id.InputValue);
			final CheckBox chkApplyToAll = (CheckBox) layout.findViewById(R.id.checkApplyToAll); 
			Button button1 = (Button) layout.findViewById(R.id.Button1);
			Button button2 = (Button) layout.findViewById(R.id.Button2);
			Button button3 = (Button) layout.findViewById(R.id.Button3);
			Button button4 = (Button) layout.findViewById(R.id.Button4);
			Button button5 = (Button) layout.findViewById(R.id.Button5);
			Button button6 = (Button) layout.findViewById(R.id.Button6);
			Button button7 = (Button) layout.findViewById(R.id.Button7);
			Button button8 = (Button) layout.findViewById(R.id.Button8);
			Button button9 = (Button) layout.findViewById(R.id.Button9);
			Button button0 = (Button) layout.findViewById(R.id.Button0);
			Button buttonPeriod = (Button) layout.findViewById(R.id.ButtonPeriod);
			
			Button buttonEquals = (Button) layout.findViewById(R.id.ButtonEquals);
			Button buttonBackspace = (Button) layout.findViewById(R.id.ButtonBackspace);
			Button buttonC = (Button) layout.findViewById(R.id.ButtonC);
			
			Button buttonMinus = (Button) layout.findViewById(R.id.ButtonMinus);
			Button buttonPlus = (Button) layout.findViewById(R.id.ButtonPlus);
			Button buttonMultiply = (Button) layout.findViewById(R.id.ButtonMultiply);
			Button buttonDivide = (Button) layout.findViewById(R.id.ButtonDivide);
			
			
			
			inputValue.setText((value==null)?"":value);
			inputValue.selectAll();
			final int _valueType = valueType; 
			
			chkApplyToAll.setVisibility( (flags & FLAGS_SHOW_APPLY_TO_ALL_CHECKBOX) > 0 ? View.VISIBLE : View.GONE);
			buttonPeriod.setEnabled(valueType == DataGrid.DATA_TYPE_DOUBLE);			
				
					
			View.OnClickListener digitClickListener = new View.OnClickListener() 
			{				
				@Override public void onClick(View v) 
				{
					String buttonText = (String)((Button)v).getText();
					
					 try
			            {
			                Object strCellText = (inputValue.getText().length() > 0 ? inputValue.getText().toString() : "0");
			                
			                if (buttonText.equals("="))
	                		{
		                        if (cOperPrev == CalcOperation.None)
		                        {
		                            cOperPrev = cOper;
		                            dSecond = Convert.toDouble(strCellText, 0d);
		                        }
		                        else
		                        {
		                            dSecond = Convert.toDouble(strCellText, 0d);
		                        }
		                        inputValue.setText(CalculateResult().toString());
		                        cOper = CalcOperation.None;
	                		}				                        
			                else if (buttonText.equals("*"))
			                {
		                        if (cOperPrev == CalcOperation.None)
		                        {
		                            dFirst = Convert.toDouble(strCellText, 0d);
		                        }
		                        else
		                        {
		                            dSecond = Convert.toDouble(strCellText, 0d);
		                            inputValue.setText(CalculateResult().toString());
		                        }
		                        cOper = CalcOperation.Multiply;
			                }   
			                else if (buttonText.equals("+"))
			                {
			                        if (cOperPrev == CalcOperation.None)
			                        {
			                            dFirst = Convert.toDouble(strCellText, 0d);
			                        }
			                        else
			                        {
			                            dSecond = Convert.toDouble(strCellText, 0d);
			                            inputValue.setText(CalculateResult().toString());
			                        }
			                        cOper = CalcOperation.Plus;
			                }       				                        
			                else if (buttonText.equals("-"))
			                {
			                        if (cOperPrev == CalcOperation.None)
			                        {
			                            dFirst = Convert.toDouble(strCellText, 0d);
			                            inputValue.setText((CharSequence) (strCellText.equals("0") ? buttonText : strCellText));
			                        }
			                        else
			                        {
			                            dSecond = Convert.toDouble(strCellText, 0d);
			                            inputValue.setText(CalculateResult().toString());
			                        }
			                        cOper = CalcOperation.Minus;
			                }				                        				                       
			                else if (buttonText.equals("/"))
			                {
			                        if (cOperPrev == CalcOperation.None)
			                        {
			                            dFirst = Convert.toDouble(strCellText, 0d);
			                        }
			                        else
			                        {
			                            dSecond = Convert.toDouble(strCellText, 0d);
			                            inputValue.setText(CalculateResult().toString());
			                        }
			                        cOper = CalcOperation.Divide;
			                }
			                else if (buttonText.equals("C"))
			                {
			                		inputValue.setText("0");
			                        cOper = CalcOperation.None;
			                        cOperPrev = CalcOperation.None;
			                }
			                else if (buttonText.equals("<-"))
			                {
				                        if (cOper == CalcOperation.None)
				                        {
				                        	int selStart = inputValue.getSelectionStart();
				                        	int selEnd = inputValue.getSelectionEnd();				                        	
				                        	if(selStart == selEnd)
				                        	{
				                        		CharSequence s = inputValue.getText();
				                        		if (s.length()> 0) inputValue.setSelection(s.length()-1, s.length());
				                        	}
				                        	replaceSelection(inputValue, "");
				                        }
			                }
			                else
			                {
		                        if (cOper == CalcOperation.None)
		                        {
		    						if(!replaceSelection(inputValue, buttonText))
		    						{
		    							strCellText = strCellText.equals("0") ? buttonText : strCellText + buttonText;
		    							inputValue.setText((CharSequence) strCellText);		    							
		    						}
		                        }
		                        else if (cOper == CalcOperation.Minus && dFirst == 0)
		                        {
		                        	cOper = CalcOperation.None;
		                        	cOperPrev = CalcOperation.None;
		                        	strCellText = strCellText.equals("0") ? "-" + buttonText : strCellText + buttonText;
	    							inputValue.setText((CharSequence) strCellText);	
		                        }
		                        else
		                        {
		                            cOperPrev = cOper;
		                            cOper = CalcOperation.None;
		                            inputValue.setText(buttonText);
		                        }
			                }
			                
			            }
			            catch(Exception ex)
			            {
			            	inputValue.setText("0");
			                cOper = CalcOperation.None;
			                cOperPrev = CalcOperation.None;
			                
			                ErrorHandler.CatchError("docSaleCalculatorDialog input", ex);
			            }	
			            
			            inputValue.setSelection(inputValue.getText().length());
				}
			};
			
			button1.setOnClickListener( digitClickListener);
			button2.setOnClickListener( digitClickListener);
			button3.setOnClickListener( digitClickListener);
			button4.setOnClickListener( digitClickListener);
			button5.setOnClickListener( digitClickListener);
			button6.setOnClickListener( digitClickListener);
			button7.setOnClickListener( digitClickListener);
			button8.setOnClickListener( digitClickListener);
			button9.setOnClickListener( digitClickListener);
			button0.setOnClickListener( digitClickListener);
			buttonPeriod.setOnClickListener(digitClickListener);
			
			buttonMinus.setOnClickListener (digitClickListener);
			buttonPlus.setOnClickListener (digitClickListener);
			buttonMultiply.setOnClickListener (digitClickListener);
			buttonDivide.setOnClickListener (digitClickListener);
			
			buttonEquals.setOnClickListener (digitClickListener);
			buttonBackspace.setOnClickListener (digitClickListener);			
			buttonC.setOnClickListener (digitClickListener);
			
			String okText = (res.getString(R.string.doc_sale_calc_ok));
			builder.setPositiveButton(okText, new DialogInterface.OnClickListener() 
			{					
				@Override public void onClick(DialogInterface dialog, int which) 
				{
					try
					{
						String value = inputValue.getText().toString();
						boolean applyToAll = chkApplyToAll.isChecked();
						
						Object calcValue = value;
						try
						{
							if(_valueType == DataGrid.DATA_TYPE_INTEGER)
							{								
								Integer tmpRes = (int)Double.parseDouble(value);
								if (tmpRes < 0) tmpRes = tmpRes * -1;
								calcValue = tmpRes;								
							}
							else if(_valueType == DataGrid.DATA_TYPE_DOUBLE)
								calcValue = Double.parseDouble(value);
						}
						catch(Exception ex)
						{
							if(value!=null)
								ErrorHandler.CatchError(String.format("docSaleCalculatorDialog.onClick: value=%s", value), ErrorHandler.LOG_DEBUG);							
							
							ErrorHandler.CatchError("docSaleCalculatorDialog.onClick: exception parsing input data", ex);
							MessageBox.show(contextActivity, "", contextActivity.getResources().getString(R.string.doc_sale_calc_inputError));
							
							try
							{
								if(cancelClickListener != null)
									cancelClickListener.onClick(dialog, which);
							}
							catch(Exception ex1)
							{								
							}
							
							return;
						}			
						
						if(calcResultListener!=null)
							calcResultListener.onCalcResult(calcValue, applyToAll);
					}
					catch(Exception ex)
					{			
						ErrorHandler.CatchError("Exception in docSaleCalculatorDialog.onCreate", ex);
						
						try
						{
							if(cancelClickListener != null)
								cancelClickListener.onClick(dialog, which);
						}
						catch(Exception ex1)
						{								
						}						
					}  	
				}
			});
			
			String cancelText = (res.getString(R.string.doc_sale_calc_cancel));
			builder.setNegativeButton(cancelText, new DialogInterface.OnClickListener() 
			{					
				@Override public void onClick(DialogInterface dialog, int which) 
				{
					if(cancelClickListener != null)
						cancelClickListener.onClick(dialog, which);
				}
			});		
			
			return builder.create();
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in docSaleCalculatorDialog.onCreate", ex);
		}  	
		
		return null;    	
    }
        
    private Double CalculateResult()
    {
        double dResult = 0;
        switch (cOperPrev)
        {
            case Divide:
                if (dSecond == 0)
                {
                    //MessageBox.Show("������� �� 0!!!", "������!");
                    dResult = 0;
                }
                else
                {
                    dResult = dFirst / dSecond;
                }
                break;
            case Minus:
                dResult = dFirst - dSecond;
                break;
            case Multiply:
                dResult = dFirst * dSecond;
                break;
            case Plus:
                dResult = dFirst + dSecond;
                break;
            default:
            	dResult = dSecond;
                break;
        }
        dFirst = dResult;
        dSecond = 0;

        cOperPrev = CalcOperation.None;
        
        return dResult;
    }
    //--------------------------------------------------------------
    //returns true if replace succeeded
    private boolean replaceSelection(EditText editView, String text)
    {
    	int selStart = editView.getSelectionStart();
    	int selEnd = editView.getSelectionEnd();
    	
    	if(selStart != -1 && selEnd != -1 && selStart != selEnd)
    	{
    		StringBuilder sb = new StringBuilder(editView.getText());
    		sb.delete(selStart, selEnd);
    		sb.insert(selStart, text);
    		String result = sb.toString();
    		editView.setText(result.length() == 0 ? "0" : result);
    		editView.setSelection(selStart + text.length());    		
    		return true;
    	}
    	
    	return false;
    }
}

