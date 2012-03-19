package com.app.ant.app.Activities;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.app.ant.R;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.app.ant.app.BusinessLayer.Contact;
import com.app.ant.app.Activities.MessageBox.MessageBoxButton;
import com.app.ant.app.BusinessLayer.Document;
import com.app.ant.app.Controls.DataGrid;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Settings;
import com.app.ant.app.ServiceLayer.StepControllerVisit;
import com.app.ant.app.ServiceLayer.TabController;
import com.app.ant.app.ServiceLayer.TabControllerVisit;
import com.app.ant.app.ServiceLayer.StepController.ICacheableStep;
import com.app.ant.app.ServiceLayer.StepController.IStep;


public class DocDebtNotificationForm extends AntActivity implements IStep, ICacheableStep
{
	public static final int DEFAULT_CONTACT_ID = -100;
	
	private static final int IDD_CALCULATOR = 0;
	private static final int IDD_DATE_DIALOG = 1; 
	private static final int IDD_TIME_DIALOG = 2;
	private static final int IDD_CONTACT_POPUP = 3;
	
	private Spinner spnContactPerson;
	private TextView mTextDocDate;
	private TextView mTextDebtSum;
	private TextView mTextPromisedSum;
	private TextView mTextComments;
	
	private DocDebtNotification mDocDebtNotification;	
	private DocSaleCalculatorDialog calculatorDlg;
	
	private Cursor contactCursor;
	private SimpleCursorAdapter contactsAdapter;
	
	private boolean closeFormOnSuccessFinish = false;
	private boolean editDebtSum = false;
	private boolean returnToDebtForm = false;
	
	private boolean contactSpinnerAlreadySelected = false;
	
	//---------------------------------------------------- DocDebtNotification --------------------------------------------------------------
	
	/** �������� ���������� � ��������� "����������� � �����"*/ 
	public class DocDebtNotification
	{		
		/** ��������� ���������*/
		public char docState;
		/** ������������� ���������*/
		long docId;		
		
		/** ������������� ������*/
		public long addrID;
		/** ����� ���������*/
		public String docNumber;
		/** ���� ��������*/		
		public Calendar createDate;
		/** ���� ���������*/
		public Calendar docDate;
		/** ����� �����*/
		public double debtSum;
		/** �����, ��������� � �������*/
		public double promisedSum;
		/** ���������� ����*/
		public long contactID;		
		/** �����������*/
		public String comments;
		
		/** ������ �� �������� ��� ��������������*/
		public boolean isEditable = true;
		
		public long prevContactID;
		
		//------------------------------------------
		/** �����������
		 * @param params ��������� ������������� ��������� (�������� ������������� ��������� ��� �������� ���������� ���������) 
		 */
		public DocDebtNotification(Bundle params)
		{		
	        //
	        // Parse params
	        //
        
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
		}		

		//------------------------------------------
		/** ������������� ������ ���������*/
		public void newDocument()
		{
			addrID = AntContext.getInstance().getAddrID();
			docState = Document.DOC_STATE_NEW;
			createDate = Calendar.getInstance();
			docDate = Calendar.getInstance();			
			docNumber = Document.getDocNumber(Document.DOC_TYPE_DEBT_NOTIFICATION);
			debtSum = 0;
			promisedSum = 0;
			contactID = DEFAULT_CONTACT_ID;
			comments = "";
		}
		
		//------------------------------------------
		/** �������� ����� ���������� ���������
		 * @param docId ������������� ��������� ��� ��������
		 */
		public void loadDocument(long docId)
		{
			String sql = "SELECT t1.AddrID, t1.DocType, t1.DocNumber, t1.DocDate, t1.CreateDate, t1.State, t1.SumAll, t1.SumLinked, t1.ContactID, t1.Comments " 
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
	    		contactID = cursor.getLong(cursor.getColumnIndex("ContactID"));

	    		docNumber = cursor.getString(cursor.getColumnIndex("DocNumber"));
	    		debtSum =  Convert.roundUpMoney(cursor.getDouble(cursor.getColumnIndex("SumAll")));
	    		promisedSum =  Convert.roundUpMoney(cursor.getDouble(cursor.getColumnIndex("SumLinked")));
	    		comments = cursor.getString(cursor.getColumnIndex("Comments"));
	       	}
	    	
	    	if(cursor!=null)
	    		cursor.close();
	    	
		}
		
		//------------------------------------------
		/** ���������� ��������� � ��*/
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
			    	char docType = Document.DOC_TYPE_DEBT_NOTIFICATION;
			    	char newDocState = Document.DOC_STATE_FINISHED;
			    	
					long clientID = AntContext.getInstance().getClientID();
					long salerId = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_SALER_ID);
					long visitID = AntContext.getInstance().getVisit().getVisitID();	    	
			    	
			    	String sqlDate = Convert.getSqlDateTimeFromCalendar(docDate);
			    	String sqlCreateDate = Convert.getSqlDateTimeFromCalendar(createDate);
			    	String sqlCloseDate = Convert.getSqlDateTimeFromCalendar(Calendar.getInstance());
			    	
					String sql = "INSERT into Documents (DocID, ClientID, AddrID, DocType, DocNumber, CreateDate, DocDate, CloseDate, " +
											" State, SumAll, SumLinked, SalerID, VisitID, ContactID, Comments )" 
										+ " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";		
					Object[] bindArgs = new Object[] { newDocId, clientID, addrID, docType, docNumber, sqlCreateDate, sqlDate, sqlCloseDate, 
													   newDocState, debtSum, promisedSum, salerId, visitID, contactID, comments }; 
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

		    	isEditable = false;
				
		        Db.getInstance().commitTransaction();
			}
			catch(Exception ex)
			{
				ErrorHandler.CatchError("Exception in DocDebtNotification.finishDocument", ex);
				throw new RuntimeException(ex);
			}						
			finally
			{
				Db.getInstance().endTransaction();
			}			
				
	    }	    
	}

    //--------------------------------------------------------- UI ---------------------------------------------------------
	/** ������������� �����*/
    @Override public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        overridePendingTransition(0,0);
        onCreateEx(true);
    }
    
    /** ����������������� ����� ��� ���������� ������������ ������������ (���������� ������)*/
    @Override public void onConfigurationChanged(Configuration newConfig) 
    {
    	super.onConfigurationChanged(newConfig);
    	onCreateEx(false);
    }
    	        
    /** ������������� ����� 
     * @param firstRun �������� ����� ��� �����������������
     */
    private void onCreateEx(boolean firstRun)
    {
    	try
    	{
	        setContentView(R.layout.doc_debt_notification);
	        
	        if(firstRun)
	        {
	        	AntContext.getInstance().getStepController().registerActivityOnLaunch(this);

	        	//check form params	        	
	        	Bundle params = getIntent().getExtras();
				if(params!=null && params.containsKey(Document.PARAM_NAME_RETURN_TO_DEBTS))
		        {
		        	returnToDebtForm = params.getBoolean(Document.PARAM_NAME_RETURN_TO_DEBTS); 
		        }			
	        	
	        	mDocDebtNotification = new DocDebtNotification(params);
	        }
	        
	        mTextDocDate = (TextView) findViewById(R.id.textDocDate);
	       	mTextDebtSum = (TextView) findViewById(R.id.textDebtSum);
	       	mTextPromisedSum = (TextView) findViewById(R.id.textPromisedSum);
	       	mTextComments = (TextView) findViewById(R.id.textComments);
	       	spnContactPerson = (Spinner) findViewById(R.id.spPaymentConditions);

			TextView textClient = (TextView) findViewById(R.id.textClient);
			textClient.setText(AntContext.getInstance().getClient().nameScreen);

			//
			//contact person
			//
			
			contactsAdapter = fillContactsSpinner(this, mDocDebtNotification.addrID, spnContactPerson, false);
			contactCursor = contactsAdapter.getCursor();
			
			
			if( mDocDebtNotification.docState == Document.DOC_STATE_NEW && contactCursor.getCount()>1) 
			{
				contactCursor.moveToPosition(1);
				mDocDebtNotification.contactID = contactCursor.getLong(contactCursor.getColumnIndex("_id"));
			}			
						
			//select default contact
			selectContactInSpinner(spnContactPerson, mDocDebtNotification.contactID);			
			
	        spnContactPerson.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
			{
				@Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
				{				
					try
					{
						if(!contactSpinnerAlreadySelected)
						{
							contactSpinnerAlreadySelected = true;
							return;
						}
						
						mDocDebtNotification.prevContactID = mDocDebtNotification.contactID; 
							
						if(id!=mDocDebtNotification.contactID)
							mDocDebtNotification.contactID = id;
						
						if(id == DEFAULT_CONTACT_ID)
							showDialog(IDD_CONTACT_POPUP);
					}
					catch(Exception ex)
					{
						ErrorHandler.CatchError("Exception in DocDebtNotificetionForm::onItemSelected", ex);
					}
				}
				
				@Override public void onNothingSelected(AdapterView<?> arg0){ }
			});			
			
			
	    	//date      
	        updateDateDisplay();
	
	    	mTextDocDate.setOnClickListener(new View.OnClickListener() 
			{				
				@Override public void onClick(View v) 
				{
					if(mDocDebtNotification.isEditable)
						showDialog(IDD_DATE_DIALOG); 			
				}
			} );
	    	
	    	//docSum
	    	mTextDebtSum.setText(Double.toString(mDocDebtNotification.debtSum));
	    	mTextDebtSum.setOnClickListener(new View.OnClickListener() 
			{				
				@Override public void onClick(View v) 
				{ 
					if(mDocDebtNotification.isEditable)
					{
						editDebtSum = true;
						showDialog(IDD_CALCULATOR);
					}
				}
			} );
	    	
	    	mTextPromisedSum.setText(Double.toString(mDocDebtNotification.promisedSum));
	    	mTextPromisedSum.setOnClickListener(new View.OnClickListener() 
			{				
				@Override public void onClick(View v) 
				{ 
					if(mDocDebtNotification.isEditable)
					{
						editDebtSum = false;
						showDialog(IDD_CALCULATOR);
					}
				}
			} );
	    	
	    	//comments
	    	mTextComments.setText(mDocDebtNotification.comments);   	
	    	
	    	enableControls();
	    	initStepBar();
		}
		catch(Exception ex)
		{
			MessageBox.show(this, getResources().getString(R.string.form_title_debtNotification), getResources().getString(R.string.doc_debt_notification_exceptionOnCreate));
			ErrorHandler.CatchError("Exception in DocDebtNotificationForm.onCreate", ex);
		}	
    }
    //--------------------------------------------------------------
    /** ������� ��� ����������� ������ ��������� � �������� Spinner
     * @param context ��������
     * @param addrId �������������� ������
     * @param spnContactPerson ������� Spinner ��� ����������
     * @param alignLeft ������������ ������ � ������
     */
    public static SimpleCursorAdapter fillContactsSpinner(Context context, long addrID, Spinner spnContactPerson, boolean alignLeft )
    {
		String sql = String.format( " SELECT c.ContactID AS _id, c.FIO as FIO, c.FIO as SortString " +
				" FROM Contacts c " +
				" WHERE c.AddrID = %d AND Coalesce(State,'%s')<>'%s' " + 										
				" UNION ALL " +
				" SELECT %d AS _id, '%s' as FIO, '000000' as SortString " +
				" ORDER BY SortString ", 
				addrID, Document.DOC_STATE_NEW, Document.DOC_STATE_DELETED,
				DEFAULT_CONTACT_ID, context.getResources().getString(R.string.doc_list_newContact));

		Cursor contactCursor = Db.getInstance().selectSQL(sql);
		((Activity)context).startManagingCursor(contactCursor);

		String[] from = new String[] { "FIO" };
		int[] to = new int[] { android.R.id.text1 };    
		SimpleCursorAdapter contactsAdapter = new SimpleCursorAdapter(context, R.layout.spinner_item, contactCursor, from, to);
		contactsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		contactsAdapter.setViewResource( alignLeft ? R.layout.simple_spinner_dropdown_item_ex_left : R.layout.simple_spinner_dropdown_item_ex );
		spnContactPerson.setAdapter(contactsAdapter);
		
		return contactsAdapter;
    }    
    
    //--------------------------------------------------------------
    /** ������������� ��������� �������� � ������ ��������� 
     * @param spnContactPerson ������� Spinner �� ������� ���������
     * @param contactID ������������� ��������
     */
    public static void selectContactInSpinner(Spinner spnContactPerson, long contactID)
    {
		for(int i=0; i<spnContactPerson.getCount();i++)
		{
			if(spnContactPerson.getItemIdAtPosition(i) == contactID)
			{
				spnContactPerson.setSelection(i);
				break;
			}						
		}
    }

    //--------------------------------------------------------------    
    /** �������� � ���������� ���������
     * @param closeFormOnSuceess ��������� �� ����� ����� ����������
     */    
    private void checkErrorsAndFinishDocument(boolean closeFormOnSuccess)
    {
    	closeFormOnSuccessFinish = closeFormOnSuccess;    	
    	mDocDebtNotification.comments = mTextComments==null? "":mTextComments.getText().toString();    	
		finishDocument();
    }

    //--------------------------------------------------------------
    /** ������� �����*/
    private void closeForm()
    {
		AntContext.getInstance().getTabController().onBackPressed(this, TabController.BackEventFlags.DO_NOT_LAUNCH_NEW_ACTIVITY);
		if(returnToDebtForm)
			AntContext.getInstance().getTabController().startTab(this, TabControllerVisit.TAB_VISIT, StepControllerVisit.VISIT_STEP_DEBTS, null); //go to docList tab
		else
			AntContext.getInstance().getTabController().startTab(this, TabControllerVisit.TAB_DOC_LIST, 0, null); //go to docList tab
    }
    
    //--------------------------------------------------------------
    /** ��������� ��������*/
    private void finishDocument()
    {
    	try
    	{
			mDocDebtNotification.finishDocument();
			enableControls();
			
			if(closeFormOnSuccessFinish)
			{
				closeForm();
			}
		}
		catch(Exception ex)
		{
			MessageBox.show(this, getResources().getString(R.string.form_title_debtNotification), getResources().getString(R.string.doc_debt_notification_exceptionFinish));
			ErrorHandler.CatchError("Exception in DocDebtNotificationForm.finishDocument", ex);
		}		
    }
    
    //--------------------------------------------------------------
    /** ������������/�������������� �������� ����� � ����������� �� ��������� ���������*/
    private void enableControls()
    {
    	mTextDocDate.setEnabled(mDocDebtNotification.isEditable);
    	mTextDebtSum.setEnabled(mDocDebtNotification.isEditable);
    	mTextPromisedSum.setEnabled(mDocDebtNotification.isEditable);
    	mTextComments.setEnabled(mDocDebtNotification.isEditable);
    }   
    //--------------------------------------------------------------    
    private void initStepBar()
    {
    	//init tabs
    	ViewGroup tabsPlacement = (ViewGroup) findViewById(R.id.tabsPlacement);
    	AntContext.getInstance().getTabController().createTabs(this, tabsPlacement);
    }
    
    //--------------------------------------------------------------
    /** ���������� ���� ���������*/
    private void updateDateDisplay() 
    {
        mTextDocDate.setText( Convert.dateTimeToString(mDocDebtNotification.docDate));
    }
    
    //--------------------------------------------------------------
    /** �������� ���������� ������ ��������� � �������� Spinner*/
    private void refreshSpinner()
    {
		contactCursor.requery();
		contactsAdapter.notifyDataSetChanged();
		selectContactInSpinner(spnContactPerson, mDocDebtNotification.contactID);    	
    }   
    
    //--------------------------------------------------------------
    /** ����� ���������� ���� ��� �������������� ������ ��������� (����, �����, �����, ���������� ����)*/
    @Override protected Dialog onCreateDialog(int id) 
    {
    	try
    	{
	        switch (id) 
	        {
	            case IDD_DATE_DIALOG:
	                return new DatePickerDialog(this, 
						                		new DatePickerDialog.OnDateSetListener() 
						                		{
								                    public void onDateSet(DatePicker view, int year, int monthOfYear,
								                            int dayOfMonth) 
								                    {
								                    	mDocDebtNotification.docDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
								                    	mDocDebtNotification.docDate.set(Calendar.MONTH, monthOfYear);
								                    	mDocDebtNotification.docDate.set(Calendar.YEAR, year);
								                        updateDateDisplay();
								                        showDialog(IDD_TIME_DIALOG);
								                    }
						                		},
						                		mDocDebtNotification.docDate.get(Calendar.YEAR), 
						                		mDocDebtNotification.docDate.get(Calendar.MONTH), 
												mDocDebtNotification.docDate.get(Calendar.DAY_OF_MONTH));
	            case IDD_TIME_DIALOG:    
	                return new TimePickerDialog(this, 
						                        new TimePickerDialog.OnTimeSetListener() 
								                {
								                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) 
								                        {
								                        	mDocDebtNotification.docDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
								                        	mDocDebtNotification.docDate.set(Calendar.MINUTE, minute);
								                            updateDateDisplay();
								                        }
								                }, 
	                							mDocDebtNotification.docDate.get(Calendar.HOUR_OF_DAY), 
	                							mDocDebtNotification.docDate.get(Calendar.MINUTE),
	                							true);
	                
	    		case IDD_CALCULATOR:
	    		{
	    			calculatorDlg = new DocSaleCalculatorDialog();    			
	    			int calcFlags = DocSaleCalculatorDialog.FLAGS_NONE;
	    			
	    			String title = editDebtSum ? getResources().getString(R.string.doc_debt_notification_calc_titleDebtSum)
	    													:getResources().getString(R.string.doc_debt_notification_calc_titlePromisedSum);
	    			String value = editDebtSum ? mTextDebtSum.getText().toString() : mTextPromisedSum.getText().toString();
	    			Dialog dlg = calculatorDlg.onCreate(this, title, calcFlags, value, DataGrid.DATA_TYPE_DOUBLE);
	    			
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
	    					if(editDebtSum)
	    					{
		    					mTextDebtSum.setText( sum.toString() );
		    					mDocDebtNotification.debtSum = sum;
	    					}
	    					else
	    					{
		    					mTextPromisedSum.setText( sum.toString() );
		    					mDocDebtNotification.promisedSum = sum;	    						
	    					}
	    				}
	    			});
	    			return dlg;
	    		}	    			
	    		case IDD_CONTACT_POPUP:
					ContactAddDialog contactDialog = new ContactAddDialog();
					
					Contact contact = null;					
					Dialog dlg = contactDialog.onCreate(this, contact);
					
					contactDialog.setContactSubmitListener(new ContactAddDialog.OnContactSubmitListener() 
	    			{					
	    				@Override public void onContactSubmit(Contact contact) 
	    				{ 
	    					try
	    					{
								//finish dialog
								removeDialog(IDD_CONTACT_POPUP);								
								mDocDebtNotification.contactID = contact.contactID;
								refreshSpinner();
	    					}
							catch(Exception ex)
							{
								MessageBox.show(DocDebtNotificationForm.this, getResources().getString(R.string.form_title_contacts), getResources().getString(R.string.addr_contact_exceptionOnEdit));
								ErrorHandler.CatchError("Exception in DocDebtNotificationForm.onContactSubmit", ex);					
							}		    					
	    				}
	    			});
					
					contactDialog.setCancelClickListener(new DialogInterface.OnClickListener() 
	    			{					
	    				@Override public void onClick(DialogInterface dialog, int which) 
	    				{ 
	    					removeDialog(IDD_CONTACT_POPUP);
	    					mDocDebtNotification.contactID = mDocDebtNotification.prevContactID;
	    					refreshSpinner();
	    				}
	    			});
	    			
	    			return dlg;
    		}                
		}
		catch(Exception ex)
		{
			MessageBox.show(this, getResources().getString(R.string.form_title_debtNotification), getResources().getString(R.string.doc_debt_notification_exceptionOnCreateDialog));
			ErrorHandler.CatchError("Exception in DocDebtNotificationForm.onCreateDialog", ex);
		}	
	        
        return null;
    }

    //--------------------------------------------------------------
    /** ������������� ������� ����� ������������*/
    @Override protected void onPrepareDialog(int id, Dialog dialog) 
    {
        switch (id) 
        {
            case IDD_DATE_DIALOG:
                ((DatePickerDialog) dialog).updateDate(mDocDebtNotification.docDate.get(Calendar.YEAR), 
                										mDocDebtNotification.docDate.get(Calendar.MONTH), 
                										mDocDebtNotification.docDate.get(Calendar.DAY_OF_MONTH));
                break;
            case IDD_TIME_DIALOG:
                ((TimePickerDialog) dialog).updateTime(mDocDebtNotification.docDate.get(Calendar.HOUR_OF_DAY), 
                										mDocDebtNotification.docDate.get(Calendar.MINUTE));
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
			MessageBox.show(this, getResources().getString(R.string.form_title_debtNotification), getResources().getString(R.string.doc_debt_notification_exceptionFinish));
			ErrorHandler.CatchError("Exception in DocDebtNotificationForm.onBackPressed", ex);
		}
    }
    
    //--------------------------------------------------------------
    /** ������� �����*/
    private void closeDocument()
    {
    	try
    	{
    		closeForm();
		}
		catch(Exception ex)
		{
			MessageBox.show(this, getResources().getString(R.string.form_title_debtNotification), getResources().getString(R.string.doc_debt_notification_exceptionFinish));
			ErrorHandler.CatchError("Exception in DocDebtNotificationForm.closeDocument", ex);
		}    	
    }

    //--------------------------------------------------------------
    /** ������� �����. ���� �������� ��������������, ��������� ��������� � ������������ ��������� ��������*/
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
										MessageBox.show(DocDebtNotificationForm.this, getResources().getString(R.string.form_title_debtNotification), getResources().getString(R.string.doc_debt_notification_exceptionFinish));
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
		                                          		
  		MessageBox.show(this, getResources().getString(R.string.doc_debt_notification_alertFinishTitle), 
  									getResources().getString(R.string.doc_debt_notification_alertFinish), buttons);
    }
    
    //---------------------- IStep implementation ----------------------------------------
    /** ������� �� ������������� ��������� � ���������
     * @return ������� ���������
     */
    public boolean haveUnsavedChanges()
    {
    	return mDocDebtNotification.isEditable; 
    }
    
    //--------------------------------------------------------------
    /** ������� � ����� �� ������� ���� */
    @Override public void onResume()
    {
    	try
    	{
	    	super.onResume();	    	
			refreshSpinner();	    	
	    	ViewGroup tabsPlacement = (ViewGroup) findViewById(R.id.tabsPlacement);
	    	AntContext.getInstance().getTabController().refreshTabs(this, tabsPlacement);
    	}
		catch(Exception ex)
		{
	    	MessageBox.show(this, getResources().getString(R.string.form_title_debtNotification), getResources().getString(R.string.doc_sale_exceptionDisplay));
			ErrorHandler.CatchError("Exception in DocDebtNotificationForm.onResume", ex);
		}								
    }
    
}

