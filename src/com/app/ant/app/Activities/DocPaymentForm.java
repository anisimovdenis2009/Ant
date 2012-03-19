package com.app.ant.app.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.Activities.MessageBox.MessageBoxButton;
import com.app.ant.app.BusinessLayer.Document;
import com.app.ant.app.Controls.DataGrid;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.*;
import com.app.ant.app.ServiceLayer.StepController.ICacheableStep;
import com.app.ant.app.ServiceLayer.StepController.IStep;

import java.util.Calendar;


public class DocPaymentForm extends AntActivity implements IStep, ICacheableStep
{
	private final static int IDD_CALCULATOR = 0;
	private static final int IDD_DATE_DIALOG = 1; 
	
	private static final int ERROR_BLANKNO_EMPTY = 1;
	private static final int ERROR_SUM_EXCEEDS_UNPAID = 2;
	private static final int ERROR_FULL_BLACK_DOC_PAYMENTS = 4;
	private static final int WARNING_BLANKNO_EMPTY = 1;
	
	public class ErrorsAndWarnings { public int errors; public int warnings; ErrorsAndWarnings(int e, int w) {errors=e; warnings=w;} };	
	
	private TextView mTextDocNum;
	private TextView mTextDocDate;
	private TextView mTextBlankNo;
	private TextView mTextDocSum;
	private TextView mTextComments;
	
	private DocPayment mDocPayment;	
	private DocSaleCalculatorDialog calculatorDlg;
	
	private boolean closeFormOnSuccessFinish = false;
	private boolean returnToDebtForm = false;
	
	//---------------------------------------------------- DocPayment --------------------------------------------------------------
	/** �������� ���������� � ��������� "������", ��������� ��������, ���������� ��������� � ���� ������*/ 
	public class DocPayment
	{		
		public char docState;
		long docId;		
		boolean haveBaseDoc;
		long baseDocId;
		
		public long addrID;
		public String docNumber;
		public Calendar createDate;
		public Calendar docDate;		
		public double docSum;
		public String comments;
		public String blankNo;
		
		double baseDocSum = 0;
		double sumLinked = 0;
		double sumUnpaid = 0;
		char baseDocBWG = Document.DOC_COLOR_UNKNOWN;
		char baseDocState = Document.DOC_STATE_UNKNOWN;
		double prevDocSum = 0;
		
		public boolean isEditable = true;
		
		public DocPayment(Bundle params)
		{		
	        //
	        // Parse params
	        //
        
			haveBaseDoc = params.containsKey(Document.PARAM_NAME_BASEDOCID);
	        if(haveBaseDoc)
	        	baseDocId = params.getLong(Document.PARAM_NAME_BASEDOCID);	
	        
			if(params.containsKey(Document.PARAM_NAME_DOCID))
	        {
	        	docId = params.getLong(Document.PARAM_NAME_DOCID);
	        	loadDocument(docId);
	        }
			else
			{
				newDocument();
			}

			//
			//retrieve info from base document
			//
			isEditable = (docState!=Document.DOC_STATE_CLOSED && docState!=Document.DOC_STATE_SENT);
			
			if(haveBaseDoc && isEditable)
			{
				String sql = "SELECT AddrID, SumAll, Comments, SumLinked, BWG, State FROM documents  " 
							 + " WHERE DocID = " + baseDocId;
				
		    	Cursor cursor = Db.getInstance().selectSQL(sql);
		    	if(cursor!=null && cursor.getCount()!=0)
		    	{
		    		cursor.moveToPosition(0);

		    		addrID = cursor.getLong(cursor.getColumnIndex("AddrID"));
		    		baseDocSum = Convert.roundUpMoney(cursor.getDouble(cursor.getColumnIndex("SumAll")));
		    		sumLinked = Convert.roundUpMoney(cursor.getDouble(cursor.getColumnIndex("SumLinked")));
		    		sumUnpaid = Convert.roundUpMoney((baseDocSum - sumLinked > 0)? (baseDocSum - sumLinked):0);
		    		
		    		String strBWG = cursor.getString(cursor.getColumnIndex("BWG"));
		    		baseDocBWG = (strBWG!=null && strBWG.length()>0)? strBWG.charAt(0): Document.DOC_COLOR_UNKNOWN;
		    		
		    		String strState = cursor.getString(cursor.getColumnIndex("State"));
		    		baseDocState = (strState!=null && strState.length()>0)? strState.charAt(0): Document.DOC_STATE_UNKNOWN;		    		
		    		
		    		if(docState==Document.DOC_STATE_NEW)
		    		{
		    			docSum = sumUnpaid;
		    			comments = cursor.getString(cursor.getColumnIndex("Comments"));
		    		}
		    	}
		    	
		    	if(cursor!=null)
		    		cursor.close();
			}	
		}		
		
		public void newDocument()
		{
			addrID = AntContext.getInstance().getAddrID();
			docState = Document.DOC_STATE_NEW;
			createDate = Calendar.getInstance();
			docDate = Calendar.getInstance();			
			comments = "";
			blankNo = "";
			docNumber = Document.getDocNumber(Document.DOC_TYPE_PAYMENT);
			docSum = 0;
			
			prevDocSum = 0;
		}
		
		public void loadDocument(long docId)
		{
			baseDocId = Document.getBaseDocId(docId);
			haveBaseDoc = (baseDocId!=0);

			String sql = "SELECT t1.AddrID, t1.DocType, t1.DocNumber, t1.DocDate, t1.CreateDate, t1.State, t1.SumAll, t1.Comments, t1.SpecMarks " 
								+ "FROM documents t1 " 
								+ "WHERE t1.DocID = " + docId;			

	    	Cursor cursor = Db.getInstance().selectSQL(sql);
	    	if(cursor!=null && cursor.getCount()!=0)
	    	{
	    		cursor.moveToPosition(0);
	    		addrID = cursor.getLong(cursor.getColumnIndex("AddrID"));
	    		docState = Convert.getDocStateFromString(cursor.getString(cursor.getColumnIndex("State")));	    		
	    		docDate = Convert.getDateFromString(cursor.getString(cursor.getColumnIndex("DocDate")));
	    		createDate = Convert.getDateFromString(cursor.getString(cursor.getColumnIndex("CreateDate")));	    		

	    		docNumber = cursor.getString(cursor.getColumnIndex("DocNumber"));
	    		comments = cursor.getString(cursor.getColumnIndex("Comments"));
	    		blankNo = cursor.getString(cursor.getColumnIndex("SpecMarks"));
	    		docSum =  Convert.roundUpMoney(cursor.getDouble(cursor.getColumnIndex("SumAll")));	    		
	    		prevDocSum = docSum;
	    		
	       	}
	    	
	    	if(cursor!=null)
	    		cursor.close();
	    	
		}
		
	    public void finishDocument()
	    {
	    	
			try
			{
				Db.getInstance().beginTransaction();
	    	
		    	//
		    	// Insert document to db
		    	//
		    	
		    	long newDocId = Document.getMinDocId() - 1;
		    	{
			    	char docType = Document.DOC_TYPE_PAYMENT;
			    	char newDocState = Document.DOC_STATE_FINISHED;
			    	
					long clientID = AntContext.getInstance().getClientID();
					long salerId = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_SALER_ID);
					long visitID = AntContext.getInstance().getVisit().getVisitID();	    	
			    	
			    	String sqlDate = Convert.getSqlDateTimeFromCalendar(mDocPayment.docDate);
			    	String sqlCreateDate = Convert.getSqlDateTimeFromCalendar(mDocPayment.createDate);
			    	String sqlCloseDate = Convert.getSqlDateTimeFromCalendar(Calendar.getInstance());
			    	
					String sql = "INSERT into Documents (DocID, ClientID, AddrID, DocType, DocNumber, CreateDate, DocDate, CloseDate, " +
											"State, SumAll, Comments, SpecMarks, SalerID, VisitID )" 
										+ " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";		
					Object[] bindArgs = new Object[] { newDocId, clientID, addrID, docType, docNumber, sqlCreateDate, sqlDate, sqlCloseDate, 
													   newDocState, docSum, comments, blankNo, salerId, visitID }; 
					Db.getInstance().execSQL(sql, bindArgs);
		    	}
				
				//
				// For edited documents, mark previous document with 'O'
				// and restore unpaid sum
				//
		    	
		    	if( docState!=Document.DOC_STATE_NEW )
				{
					String sql = "UPDATE Documents SET State = ? WHERE DocID = ?";
					Object[] bindArgs = new Object[] { Document.DOC_STATE_PREVIOUS, docId };
					Db.getInstance().execSQL(sql, bindArgs);			
				}
							
				if(haveBaseDoc)
				{
					//
					//increase paid sum in linked doc sale, minus sum of previous payment sum
					//				
					double sumLinkedNew = sumLinked + docSum - prevDocSum;
	
					if(sumLinkedNew > baseDocSum)
						sumLinkedNew = baseDocSum;
	
					Document.setHeaderValue(baseDocId, "SumLinked", sumLinkedNew);
	
					//
					//Insert new record into DocLinks, there is no difference if document is new or edited
					//baseDocSum-SumLinkedPrev - unpaid sum.
					//
					double linkSum = Convert.roundUpMoney(Math.min(docSum,(baseDocSum-sumLinked+prevDocSum)));
				
					String sql = "INSERT INTO DocLinks (DocID, LinkDocID, LinkSum, State) VALUES (?,?,?,?)";
					Object[] bindArgs = new Object[] { baseDocId, newDocId, linkSum, baseDocState };
					Db.getInstance().execSQL(sql, bindArgs);
				}			
				
				isEditable = false;
				
		        Db.getInstance().commitTransaction();
			}
			catch(Exception ex)
			{
				ErrorHandler.CatchError("Exception in DocPayment.finishDocument", ex);
				throw new RuntimeException(ex);
			}						
			finally
			{
				Db.getInstance().endTransaction();
			}			
				
	    }
	    
		public ErrorsAndWarnings checkCanFinish()
		{
			int errors = 0;
			int warnings = 0;

			long denyUnlinkedPayments = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_DENY_UNLINKED_PAYMENTS);
			long fullBlackDocPayments = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_FULL_BLACK_DOC_PAYMENTS);
			long checkPaymentNumber = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_CHECK_PAYMENT_NUMBER);
			
			if(blankNo.length()==0)
			{
				if(checkPaymentNumber == -1)
					errors |= ERROR_BLANKNO_EMPTY;
				else if(checkPaymentNumber == 1)
					warnings |= WARNING_BLANKNO_EMPTY;
			}
			
			if(haveBaseDoc && denyUnlinkedPayments==-1)
			{
				double actualSumUnpaid = sumUnpaid + prevDocSum;
				if((docSum - actualSumUnpaid) >= 0.01)
					errors |= ERROR_SUM_EXCEEDS_UNPAID;
				
				if( fullBlackDocPayments ==-1 && baseDocBWG == Document.DOC_COLOR_BLACK && (actualSumUnpaid - docSum) >= 0.01)
				{
					errors |= ERROR_FULL_BLACK_DOC_PAYMENTS;
				}
			}		
			
			return new ErrorsAndWarnings(errors, warnings);
		}
	}

    //--------------------------------------------------------- UI ---------------------------------------------------------    
    @Override public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        overridePendingTransition(0,0);
        onCreateEx(true);
    }
    
    @Override public void onConfigurationChanged(Configuration newConfig) 
    {
    	super.onConfigurationChanged(newConfig);
    	onCreateEx(false);
    }
    	        
    private void onCreateEx(boolean firstRun)
    {
    	try
    	{
    		if(!firstRun)
    			saveHeaderFields();
    		
	        setContentView(R.layout.doc_payment);
	        
	        if(firstRun)
	        {
	        	AntContext.getInstance().getStepController().registerActivityOnLaunch(this);

	        	//check form params	        	
	        	Bundle params = getIntent().getExtras();
				if(params!=null && params.containsKey(Document.PARAM_NAME_RETURN_TO_DEBTS))
		        {
		        	returnToDebtForm = params.getBoolean(Document.PARAM_NAME_RETURN_TO_DEBTS); 
		        }			
	        	
	        	mDocPayment = new DocPayment(params);
	        }
	        
	        mTextDocNum = (TextView) findViewById(R.id.textDocNum);
	        mTextDocDate = (TextView) findViewById(R.id.textDocDate);
	        mTextBlankNo = (TextView) findViewById(R.id.textBlankNo);
	       	mTextDocSum = (TextView) findViewById(R.id.textDocSum);
	        mTextComments = (TextView) findViewById(R.id.textComments);
	        
			TextView textClient = (TextView) findViewById(R.id.textClient);
			textClient.setText(AntContext.getInstance().getClient().nameScreen);
			
	        //document number
	        mTextDocNum.setText( mDocPayment.docNumber );
	
	    	//date      
	        updateDateDisplay();
	
	    	mTextDocDate.setOnClickListener(new View.OnClickListener() 
			{				
				@Override public void onClick(View v) 
				{
					if(mDocPayment.isEditable)
						showDialog(IDD_DATE_DIALOG); 			
				}
			} );
	    	
	        //blankNo
	    	mTextBlankNo.setText(mDocPayment.blankNo);
	    	
	    	//docSum
	    	mTextDocSum.setText(Double.toString(mDocPayment.docSum));
	    	mTextDocSum.setOnClickListener(new View.OnClickListener() 
			{				
				@Override public void onClick(View v) 
				{ 
					if(mDocPayment.isEditable)
					{
						showDialog(IDD_CALCULATOR);
					}
				}
			} );
	    	
	    	//comments
	    	mTextComments.setText(mDocPayment.comments);
	    	
	    	//
	    	//cash register related buttons
	    	//
	    	Button buttonPrintCheck = (Button) findViewById(R.id.buttonPrintCheck);
	    	buttonPrintCheck.setOnClickListener(new View.OnClickListener() 
			{				
				@Override public void onClick(View v) 
				{
					CashRegisterForm.setupDeviceAddress(DocPaymentForm.this);
					CashRegisterDatecs.printDocument(DocPaymentForm.this, mDocPayment.baseDocId, mDocPayment.docSum, mDocPayment.baseDocSum);
				}
			} );
	    	
	    	enableControls();
	    	initStepBar();
		}
		catch(Exception ex)
		{
			MessageBox.show(this, getResources().getString(R.string.form_title_docPayment), getResources().getString(R.string.doc_payment_exceptionOnCreate));
			ErrorHandler.CatchError("Exception in DocPaymentForm.onCreate", ex);
		}	
    }
    //--------------------------------------------------------------
    private void saveHeaderFields()
    {
		mDocPayment.blankNo = mTextBlankNo==null? "" : mTextBlankNo.getText().toString();
		//mDocPayment.docSum = mTextDocSum==null? 0:Double.valueOf(mTextDocSum.getText().toString());
		mDocPayment.comments = mTextComments==null? "":mTextComments.getText().toString();
    }
    //--------------------------------------------------------------    
    private void checkErrorsAndFinishDocument(boolean closeFormOnSuccess)
    {
    	closeFormOnSuccessFinish = closeFormOnSuccess;
    	
    	saveHeaderFields();
		
		ErrorsAndWarnings errWarn = mDocPayment.checkCanFinish();
		
		//errWarn.errors = ERROR_BLANKNO_EMPTY|ERROR_SUM_EXCEEDS_UNPAID|ERROR_FULL_BLACK_DOC_PAYMENTS; //test
		//errWarn.warnings = WARNING_BLANKNO_EMPTY; //test
		
		if(errWarn.errors!=0)
		{
			//display error message
			String errMsg = "";
			if( (errWarn.errors & ERROR_BLANKNO_EMPTY)>0)
				errMsg = errMsg + getResources().getString(R.string.doc_payment_errorBlankNoEmpty) + "\n";
			if( (errWarn.errors & ERROR_SUM_EXCEEDS_UNPAID)>0)
				errMsg = errMsg + getResources().getString(R.string.doc_payment_errorSumExceedsUnpaid) + "\n";
			if( (errWarn.errors & ERROR_FULL_BLACK_DOC_PAYMENTS)>0)
				errMsg = errMsg + getResources().getString(R.string.doc_payment_errorFullBlackDocPayments) + "\n";
			
			AlertDialog alertDialog;
			alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle(getResources().getString(R.string.doc_payment_alertCannotFinish));
			
			alertDialog.setMessage(errMsg);
			
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.doc_payment_alertChoiceOk),
			new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which) { }
			});

			alertDialog.show();			
		}
		else if(errWarn.warnings!=0)
		{
			//display warning message
			String errMsg = "";
			if( (errWarn.warnings & WARNING_BLANKNO_EMPTY)>0)
				errMsg = errMsg + getResources().getString(R.string.doc_payment_warningBlankNoEmpty) + "\n";

			AlertDialog alertDialog;
			alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle(getResources().getString(R.string.doc_payment_alertWarning));
			
			alertDialog.setMessage(errMsg);
			
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.document_alertChoiceSave),
			new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which) 
				{ 
					finishDocument();
				}
			});

			alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.document_alertChoiceCancel),
			new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which) { }
			});
			
			alertDialog.show();
		}
		else
		{
			finishDocument();
		}		
    }
    //--------------------------------------------------------------
    private void closeForm()
    {
		AntContext.getInstance().getTabController().onBackPressed(this, TabController.BackEventFlags.DO_NOT_LAUNCH_NEW_ACTIVITY);
		if(returnToDebtForm)
			AntContext.getInstance().getTabController().startTab(this, TabControllerVisit.TAB_VISIT, StepControllerVisit.VISIT_STEP_DEBTS, null); //go to docList tab
		else
			AntContext.getInstance().getTabController().startTab(this, TabControllerVisit.TAB_DOC_LIST, 0, null); //go to docList tab
    }
    
    //--------------------------------------------------------------
    private void finishDocument()
    {
    	try
    	{
			mDocPayment.finishDocument();
			enableControls();
			
			if(closeFormOnSuccessFinish)
			{
				closeForm();
			}
		}
		catch(Exception ex)
		{
			MessageBox.show(this, getResources().getString(R.string.form_title_docPayment), getResources().getString(R.string.doc_payment_exceptionFinish));
			ErrorHandler.CatchError("Exception in DocPaymentForm.finishDocument", ex);
		}		
    }
    
    //--------------------------------------------------------------
    private void enableControls()
    {
    	mTextDocDate.setEnabled(mDocPayment.isEditable);
    	mTextBlankNo.setEnabled(mDocPayment.isEditable);
    	mTextDocSum.setEnabled(mDocPayment.isEditable);
    	mTextComments.setEnabled(mDocPayment.isEditable);   	
    }   
    //--------------------------------------------------------------    
    private void initStepBar()
    {
    	//init tabs
    	ViewGroup tabsPlacement = (ViewGroup) findViewById(R.id.tabsPlacement);
    	AntContext.getInstance().getTabController().createTabs(this, tabsPlacement);
    }
    
    //--------------------------------------------------------------    
    private void updateDateDisplay() 
    {
        mTextDocDate.setText( Convert.dateToString(mDocPayment.docDate));
    }
    
    //--------------------------------------------------------------
    private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() 
    {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                    int dayOfMonth) 
            {
            	mDocPayment.docDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            	mDocPayment.docDate.set(Calendar.MONTH, monthOfYear);
            	mDocPayment.docDate.set(Calendar.YEAR, year);
                updateDateDisplay();
            }
    };     
    
    //--------------------------------------------------------------    
    @Override protected Dialog onCreateDialog(int id) 
    {
    	try
    	{
	        switch (id) 
	        {
	            case IDD_DATE_DIALOG:
	                return new DatePickerDialog(this, mDateSetListener, mDocPayment.docDate.get(Calendar.YEAR), 
	                													mDocPayment.docDate.get(Calendar.MONTH), 
	                													mDocPayment.docDate.get(Calendar.DAY_OF_MONTH));
	    		case IDD_CALCULATOR:
	    		{
	    			calculatorDlg = new DocSaleCalculatorDialog();    			
	    			int calcFlags = DocSaleCalculatorDialog.FLAGS_NONE;
	    			
	    			int titleResId;
	    			String defValue = "";
	    			
    				defValue =  mTextDocSum.getText().toString();
    				titleResId = R.string.doc_sale_calc_titleDocSum;
	    				
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
    						mTextDocSum.setText( sum.toString() );
    						mDocPayment.docSum = sum;
	    				}
	    			});
	    			return dlg;	
	    		}                
	        }
		}
		catch(Exception ex)
		{
			MessageBox.show(this, getResources().getString(R.string.form_title_docPayment), getResources().getString(R.string.doc_payment_exceptionOnCreateDialog));
			ErrorHandler.CatchError("Exception in DocPaymentForm.onCreateDialog", ex);
		}	
	        
        return null;
    }

    //--------------------------------------------------------------    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) 
    {
        switch (id) 
        {
            case IDD_DATE_DIALOG:
                ((DatePickerDialog) dialog).updateDate(mDocPayment.docDate.get(Calendar.YEAR), 
                									   mDocPayment.docDate.get(Calendar.MONTH), 
                									   mDocPayment.docDate.get(Calendar.DAY_OF_MONTH));
                break;
        }
    }
    //--------------------------------------------------------------
    @Override public void onBackPressed() 
    {
    	try
    	{
    		checkAndClose();
    	}
		catch(Exception ex)
		{
			MessageBox.show(this, getResources().getString(R.string.form_title_docPayment), getResources().getString(R.string.doc_payment_exceptionFinish));
			ErrorHandler.CatchError("Exception in DocPaymentForm.onBackPressed", ex);
		}
    }
    
    //--------------------------------------------------------------
    private void closeDocument()
    {
    	try
    	{
    		//AntContext.getInstance().getTabController().onBackPressed(this);
    		closeForm();
		}
		catch(Exception ex)
		{
			MessageBox.show(this, getResources().getString(R.string.form_title_docPayment), getResources().getString(R.string.doc_payment_exceptionFinish));
			ErrorHandler.CatchError("Exception in DocPaymentForm.closeDocument", ex);
		}    	
    }

    //--------------------------------------------------------------
    private void checkAndClose()
    {
    	if(haveUnsavedChanges()==false)
    	{
    		closeDocument();
    		return;
    	}

    	//
    	//Display alert dialog proposing to save a document
    	//
		MessageBoxButton[] buttons = new MessageBoxButton[]
        {
				new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.document_alertChoiceYes),
							new DialogInterface.OnClickListener()
							{
								@Override public void onClick(DialogInterface dialog, int which) 
								{
									try
									{
										checkErrorsAndFinishDocument(true);
									}
									catch(Exception ex)
									{
										MessageBox.show(DocPaymentForm.this, getResources().getString(R.string.form_title_docPayment), getResources().getString(R.string.doc_payment_exceptionFinish));
										ErrorHandler.CatchError("Exception in checkErrorsAndFinishDocument", ex);					
									}
								}
							}),
				new MessageBoxButton(DialogInterface.BUTTON_NEUTRAL, getResources().getString(R.string.document_alertChoiceNo),
						new DialogInterface.OnClickListener()
						{
							@Override public void onClick(DialogInterface dialog, int which) 
							{ 
								closeDocument();
							}
						}),						
							
				new MessageBoxButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.document_alertChoiceCancel),
							new DialogInterface.OnClickListener()
							{
								@Override public void onClick(DialogInterface dialog, int which) { }
							})								
	    };
		                                          		
  		MessageBox.show(this, getResources().getString(R.string.doc_payment_alertFinishTitle), 
  									getResources().getString(R.string.doc_payment_alertFinish), buttons);
    }
    
    //---------------------- IStep implementation ----------------------------------------
    public boolean haveUnsavedChanges()
    {
    	return mDocPayment.isEditable; 
    }
    
    //--------------------------------------------------------------    
    @Override public void onResume()
    {
    	try
    	{
	    	super.onResume(); 
	    	
	    	ViewGroup tabsPlacement = (ViewGroup) findViewById(R.id.tabsPlacement);
	    	AntContext.getInstance().getTabController().refreshTabs(this, tabsPlacement);
    	}
		catch(Exception ex)
		{
	    	MessageBox.show(this, getResources().getString(R.string.form_title_docPayment), getResources().getString(R.string.doc_sale_exceptionDisplay));
			ErrorHandler.CatchError("Exception in DocPaymentForm.onResume", ex);
		}								
    }
    
}

