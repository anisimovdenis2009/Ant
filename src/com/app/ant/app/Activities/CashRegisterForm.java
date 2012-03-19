package com.app.ant.app.Activities;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 23.01.12
 * Time: 14:29
 * To change this template use File | Settings | File Templates.
 */
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import com.app.ant.R;
import com.app.ant.app.Activities.MessageBox.MessageBoxButton;
import com.app.ant.app.Controls.DataGrid;
import com.app.ant.app.ServiceLayer.CashRegister.CheckItem;
import com.app.ant.app.ServiceLayer.CashRegisterDatecs;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** Окно работы с кассовым аппаратом*/
public class CashRegisterForm extends Activity
{
	private final static int IDD_CALCULATOR = 0;
	private final static int IDD_DATE_DIALOG = 1;

	private DocSaleCalculatorDialog calculatorDlg;

    private static enum InputMode
    {
        none, moneyIn, moneyOut, periodicStartDate, periodicEndDate
    }

    private InputMode currentInputMode = InputMode.none;

    Calendar startDate = Calendar.getInstance();
    Calendar endDate = Calendar.getInstance();

    /** сообщает классу работы с устройством адрес устройства Bluetooth */
	public static void setupDeviceAddress(Context context)
	{
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String deviceAddr = prefs.getString( context.getString(R.string.preferences_cash_register_addr_key), "");
        CashRegisterDatecs.setDeviceAddress(deviceAddr);
	}

	/** Инициализация формы	 */
    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cash_register_test);

        try
        {
        	setupDeviceAddress(this);

        	Button buttonGetVersion = (Button) findViewById(R.id.buttonVersion);
			buttonGetVersion.setOnClickListener( new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					CashRegisterDatecs.getVersion(CashRegisterForm.this);
				}
			});

        	Button buttonXReport = (Button) findViewById(R.id.buttonXReport);
			buttonXReport.setOnClickListener( new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					CashRegisterDatecs.printXReport(CashRegisterForm.this);
				}
			});

        	Button buttonZReport = (Button) findViewById(R.id.buttonZReport);
			buttonZReport.setOnClickListener( new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					CashRegisterDatecs.printZReport(CashRegisterForm.this);
				}
			});

        	Button buttonPeriodicReport = (Button) findViewById(R.id.buttonPeriodicReport);
			buttonPeriodicReport.setOnClickListener( new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
				    startDate = Calendar.getInstance();
				    endDate = Calendar.getInstance();
					currentInputMode = InputMode.periodicStartDate;
					showDialog(IDD_DATE_DIALOG);
					//CashRegisterDatecs.printPeriodicReport(CashRegisterForm.this);
				}
			});

        	Button buttonMoneyIn = (Button) findViewById(R.id.buttonMoneyIn);
			buttonMoneyIn.setOnClickListener( new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					currentInputMode = InputMode.moneyIn;
					showDialog(IDD_CALCULATOR);
					//CashRegisterDatecs.printMoneyIn(100.);
				}
			});

        	Button buttonMoneyOut = (Button) findViewById(R.id.buttonMoneyOut);
			buttonMoneyOut.setOnClickListener( new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					currentInputMode = InputMode.moneyOut;
					showDialog(IDD_CALCULATOR);
					//CashRegisterDatecs.printMoneyOut(100.);
				}
			});

        	Button buttonPrintNonFiscal = (Button) findViewById(R.id.buttonPrintNonFiscal);
			buttonPrintNonFiscal.setOnClickListener( new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					List<String> lines = new ArrayList<String> ();
					lines.add("TEST LINE1");
					lines.add("TEST LINE2");
					lines.add("TEST LINE3");
					CashRegisterDatecs.printNonFiscal(CashRegisterForm.this, lines);
				}
			});

        	Button buttonDepartmentsReport = (Button) findViewById(R.id.buttonDepartmentsReport);
        	buttonDepartmentsReport.setOnClickListener( new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					CashRegisterDatecs.printDepartmentsReport(CashRegisterForm.this);
				}
			});

        	Button buttonOperatorsReport = (Button) findViewById(R.id.buttonOperatorsReport);
        	buttonOperatorsReport.setOnClickListener( new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					CashRegisterDatecs.printOperatorsReport(CashRegisterForm.this);
				}
			});

        	Button buttonCheckoutTape = (Button) findViewById(R.id.buttonCheckoutTape);
        	Button buttonCheckoutTape1 = (Button) findViewById(R.id.buttonCheckoutTape1);
        	View.OnClickListener listener = new View.OnClickListener()

			{
				@Override public void onClick(View v)
				{
					CashRegisterDatecs.printCheckoutTape(CashRegisterForm.this);
				}
			};
			buttonCheckoutTape.setOnClickListener(listener);
			buttonCheckoutTape1.setOnClickListener(listener);



        	Button buttonAbortFiscal = (Button) findViewById(R.id.buttonAbortFiscal);
        	buttonAbortFiscal.setOnClickListener( new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					CashRegisterDatecs.abortFiscal(CashRegisterForm.this);
				}
			});


        	Button buttonCloseCheckoutTape = (Button) findViewById(R.id.buttonCloseCheckoutTape);
        	buttonCloseCheckoutTape.setOnClickListener( new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					CashRegisterDatecs.closeCheckoutTape(CashRegisterForm.this);
				}
			});


        	Button buttonRestartDevice = (Button) findViewById(R.id.buttonRestartDevice);
        	buttonRestartDevice.setOnClickListener( new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					CashRegisterDatecs.restartDevice(CashRegisterForm.this);
				}
			});

        	Button buttonPrintFiscalCheck = (Button) findViewById(R.id.buttonPrintFiscalCheck);
        	buttonPrintFiscalCheck.setOnClickListener( new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					ArrayList<CheckItem> checkItems = new ArrayList<CheckItem>();

					checkItems.add( new CheckItem(5, 0., 10.0, 0, "Test Item 1", 1) );
					checkItems.add( new CheckItem(1, 0., 20.0, 0, "Test Item 2", 1) );
					checkItems.add( new CheckItem(1, 0., 30.0, 0, "Test Item 3", 1) );

					//checkItems.add( new CheckItem(5, 0., 10.0, 0, "Товар 1", 1) );
					//checkItems.add( new CheckItem(1, 0., 20.0, 0, "Товар 2", 2) );
					//checkItems.add( new CheckItem(1, 0., 30.0, 0, "Товар 3", 3) );

					CashRegisterDatecs.printFiscalCheck(CashRegisterForm.this, checkItems, 100.0, false);

					//CashRegisterDatecs.printDocument(-24271, 463.25, 463.25);
				}
			});

           	Button buttonSelectDevice = (Button) findViewById(R.id.buttonSelectDevice);
        	buttonSelectDevice.setOnClickListener( new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					Bundle bundle = new Bundle();
		    		bundle.putInt(BluetoothSelectDeviceForm.PARAM_NAME_BLUETOOTH_DEVICE_TYPE, R.string.preferences_cash_register_addr_key);
		    		Intent selectDeviceIntent = new Intent(CashRegisterForm.this, BluetoothSelectDeviceForm.class);
		    		selectDeviceIntent.putExtras(bundle);
					startActivity(selectDeviceIntent);
				}
			});

           	Button buttonReadDate = (Button) findViewById(R.id.buttonReadDate);
        	buttonReadDate.setOnClickListener( new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					CashRegisterDatecs.getDate(CashRegisterForm.this);
				}
			});


        }
        catch(Exception ex)
        {

        }
    }

    //--------------------------------------------------------------
    /** Вызывается когда окно получает фокус
     * @param hasFocus наличие/отсутствие фокуса
     */
    @Override public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
        {
        	try
        	{
        		setupDeviceAddress(this);
        	}
    		catch(Exception ex)
    		{
    			ErrorHandler.CatchError("Exception in CashRegisterForm.onWindowFocusChanged", ex);
    		}
        }
    }

    //--------------------------------------------------------------
    /** Создание диалоговых окон
     * @param id идентификатор диалогового окна
     */
    @Override protected Dialog onCreateDialog(int id)
    {
    	try
    	{
	        switch (id)
	        {
            	case IDD_DATE_DIALOG:
            	{
            		Calendar date = (currentInputMode == InputMode.periodicStartDate) ? startDate : endDate;
            		Dialog dlg = new DatePickerDialog(this,
                		new DatePickerDialog.OnDateSetListener()
                		{
		                    public void onDateSet(DatePicker view, int year, int monthOfYear,
		                            int dayOfMonth)
		                    {
		                    	Calendar date = (currentInputMode == InputMode.periodicStartDate) ? startDate : endDate;
		                    	date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		                    	date.set(Calendar.MONTH, monthOfYear);
		                    	date.set(Calendar.YEAR, year);

		                    	if(currentInputMode == InputMode.periodicStartDate)
		                    	{
		                    		removeDialog(IDD_DATE_DIALOG);

		                    		if(year<1997)
		                    		{
		                    			MessageBox.show(CashRegisterForm.this, getResources().getString(R.string.message_box_error),
		                    													getResources().getString(R.string.cash_register_dateErrorTooLow));
		                    		}
		                    		else
		                    		{
			        					currentInputMode = InputMode.periodicEndDate;
			        					showDialog(IDD_DATE_DIALOG);
		                    		}
		                    	}
		                    	else if(currentInputMode == InputMode.periodicEndDate)
		                    	{
		                    		removeDialog(IDD_DATE_DIALOG);

		                    		if( year >2020)
		                    		{
		                    			MessageBox.show(CashRegisterForm.this, getResources().getString(R.string.message_box_error),
																		getResources().getString(R.string.cash_register_dateErrorTooHigh));
		                    		}
		                    		else if( startDate.after(endDate) )
		                    		{
		                    			MessageBox.show(CashRegisterForm.this, getResources().getString(R.string.message_box_error),
												getResources().getString(R.string.cash_register_dateErrorEndIsLess));
		                    		}
		                    		else
		                    		{

			                        	//
			                        	// Выбор типа отчета - сокращенный или полный
			                        	//
			                    		MessageBoxButton[] buttons = new MessageBoxButton[]
			                            {
			                    				new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.cash_register_shortReport),
			                    							new DialogInterface.OnClickListener()
			                    							{
			                    								@Override public void onClick(DialogInterface dialog, int which)
			                    								{
			                    									CashRegisterDatecs.printPeriodicReport(CashRegisterForm.this, startDate, endDate, true);
			                    								}
			                    							}),
			                    				new MessageBoxButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.cash_register_fullReport),
			                    							new DialogInterface.OnClickListener()
			                    							{
			                    								@Override public void onClick(DialogInterface dialog, int which)
			                    								{
			                    									CashRegisterDatecs.printPeriodicReport(CashRegisterForm.this, startDate, endDate, false);
			                    								}
			                    							})
			                            };

			                    		MessageBox.show(CashRegisterForm.this, "", getResources().getString(R.string.cash_register_askShortReport), buttons);
		                    		}

		                    	}
		                    }
                		},
                		date.get(Calendar.YEAR),
                		date.get(Calendar.MONTH),
                		date.get(Calendar.DAY_OF_MONTH));

            		dlg.setTitle( (currentInputMode == InputMode.periodicStartDate) ? getResources().getString(R.string.cash_register_reportStartDate):
            																		  getResources().getString(R.string.cash_register_reportEndDate));
            		return dlg;
            	}
	    		case IDD_CALCULATOR:
	    		{
	    			calculatorDlg = new DocSaleCalculatorDialog();
	    			int calcFlags = DocSaleCalculatorDialog.FLAGS_NONE;

	    			int titleResId;
	    			String defValue = "";

	    			if(currentInputMode == InputMode.moneyIn)
	    				titleResId = R.string.doc_payment_calc_title_moneyIn;
	    			else
	    				titleResId = R.string.doc_payment_calc_title_moneyOut;

	    			String title = getResources().getString(titleResId);
	    			Dialog dlg = calculatorDlg.onCreate(this, title, calcFlags, defValue, DataGrid.DATA_TYPE_DOUBLE);

	    			calculatorDlg.setCancelClickListener(new DialogInterface.OnClickListener()
	    			{
	    				@Override public void onClick(DialogInterface dialog, int which)
	    				{
	    					removeDialog(IDD_CALCULATOR);
	    				}
	    			});

	    			calculatorDlg.setCalcResultListener(new DocSaleCalculatorDialog.OnCalcResultListener()
	    			{
	    				@Override public void onCalcResult(Object calcValue, boolean applyToAll)
	    				{
	    					removeDialog(IDD_CALCULATOR);
	    					Double sum = (Double)Convert.roundUpMoney((Double)calcValue);
	    					if(currentInputMode == InputMode.moneyIn)
	    					{
	    						CashRegisterForm.setupDeviceAddress(CashRegisterForm.this);
	    						CashRegisterDatecs.printMoneyIn(CashRegisterForm.this, sum);
	    					}
	    					else if(currentInputMode == InputMode.moneyOut)
	    					{
	    						CashRegisterForm.setupDeviceAddress(CashRegisterForm.this);
	    						CashRegisterDatecs.printMoneyOut(CashRegisterForm.this, sum);
	    					}
	    				}
	    			});
	    			return dlg;
	    		}
	        }
		}
		catch(Exception ex)
		{
			MessageBox.show(this, getResources().getString(R.string.form_title_cash_register), getResources().getString(R.string.doc_exceptionOnCreateDialog));
			ErrorHandler.CatchError("Exception in CashRegisterTestForm.onCreateDialog", ex);
		}

        return null;
    }

    //--------------------------------------------------------------
    @Override protected void onPrepareDialog(int id, Dialog dialog)
    {
        switch (id)
        {
            case IDD_DATE_DIALOG:
            	Calendar date = (currentInputMode == InputMode.periodicStartDate) ? startDate : endDate;
                ((DatePickerDialog) dialog).updateDate(date.get(Calendar.YEAR),
                										date.get(Calendar.MONTH),
                										date.get(Calendar.DAY_OF_MONTH));
                break;
        }
    }

}
