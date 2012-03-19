package com.app.ant.app.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.app.ant.R;
import com.app.ant.app.Activities.MessageBox.MessageBoxButton;
import com.app.ant.app.BusinessLayer.Client;
import com.app.ant.app.BusinessLayer.Document;
import com.app.ant.app.Controls.DataGrid;
import com.app.ant.app.Controls.DataGrid.CellStyle;
import com.app.ant.app.Controls.DataGrid.CellStyleCollection;
import com.app.ant.app.Controls.DataGrid.GridColumns;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Settings;
import com.app.ant.app.ServiceLayer.StepControllerVisit;
import com.app.ant.app.ServiceLayer.TabControllerVisit;
import com.app.ant.app.ServiceLayer.StepController.StepPanelType;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Calendar;


public class DocListForm extends AntActivity 
{
	private static final long DEF_ALL_ADDRESSES = 0; 
	
	private ToggleButton mCheckUnpaid;
	private DataGrid mGrid;
	private Cursor mCursor;
	
	private boolean isDebtForm;
	private boolean fromVisit = true;
	public static final String PARAM_NAME_IS_DEBT_FORM="isDebtForm";
	private Spinner spnAddresses;
	private long clientID;
	private long addrID;
	

    @Override  public void onCreate(Bundle savedInstanceState) 
    {
    	try
    	{
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        overridePendingTransition(0,0);
	        setContentView(R.layout.doc_list);
	        
	        //check form params
	        Bundle params = getIntent().getExtras();	        
			if(params!=null)
	        {
				if(params.containsKey(PARAM_NAME_IS_DEBT_FORM))
					isDebtForm = params.getBoolean(PARAM_NAME_IS_DEBT_FORM);
	        	if(params.containsKey(Document.PARAM_NAME_FROM_VISIT))
	        		fromVisit = params.getBoolean(Document.PARAM_NAME_FROM_VISIT);
	        	if(params.containsKey(Document.PARAM_NAME_CLIENT_ID))
	        		clientID = params.getLong(Document.PARAM_NAME_CLIENT_ID);        	
	        }			
			
			TextView textClient = (TextView) findViewById(R.id.textClient);
			
			Client client; 
			
			if(fromVisit)
			{
				clientID = AntContext.getInstance().getClientID();
				addrID = AntContext.getInstance().getAddrID();
				client = AntContext.getInstance().getClient();				
			}
			else
			{
				client = new Client(clientID);
				addrID = DEF_ALL_ADDRESSES;
			}
			
			textClient.setText(client.nameScreen);
			
			mCheckUnpaid = ((ToggleButton) findViewById(R.id.chkBoxUnpaid));
			//mCheckUnpaid.setChecked(isDebtForm);
		
			if(fromVisit)
				initStepBar();
			
	        initGrid();	        
	        	        
	        mCheckUnpaid.setOnClickListener( new ToggleButton.OnClickListener()			
	        {
	        	@Override public void onClick(View v)
	        	{
					try
					{
						fillGridWithData();
					}
					catch(Exception ex)
					{
						ErrorHandler.CatchError("Exception in DocListForm::onClick", ex);
					}	        		
	        	}
	        } );
	        
	        //
	        // Initialize address spinner
	        {
				//make address selector visible. fill it with values
				spnAddresses = (Spinner) findViewById(R.id.spnAddresses);
				fillAddressSpinner(this, spnAddresses, clientID, addrID, true, false);
				
		        spnAddresses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
				{
					@Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
					{				
						try
						{
							if(id!=addrID)
							{
								addrID = id;
								fillGridWithData();
							}						
						}
						catch(Exception ex)
						{
							ErrorHandler.CatchError("Exception in DocListForm::onItemSelected", ex);
						}
					}
					
					@Override public void onNothingSelected(AdapterView<?> arg0){ }					
				});  

	        }
	        
	        
			if(isDebtForm)
			{
				//change title
				TextView textTitle = (TextView) findViewById(R.id.textTitle);
				textTitle.setText(R.string.form_title_debts);

				if(fromVisit)
				{

					ImageButton buttonNextStep = (ImageButton) findViewById(R.id.buttonNextStep);
					buttonNextStep.setVisibility(View.VISIBLE);
			        buttonNextStep.setOnClickListener( new View.OnClickListener() 
					{			
						@Override public void onClick(View v) 
						{	 
					    	try
					    	{
					    		mGrid.getColumns().save();
					    		AntContext.getInstance().getTabController().onNextStepPressed(DocListForm.this);
					    	}
							catch(Exception ex)
							{
								ErrorHandler.CatchError("Exception in DocListForm.buttonNextStep.onClick", ex);
							}					
						}
					});			

			        //make the debt button panel visible
			        View buttonPlacement = (View) findViewById(R.id.stepButtonPlacementDebts);
			        buttonPlacement.setVisibility(View.VISIBLE);
		        
			        //Process events from payment button
			        ImageButton paymentButton = (ImageButton) findViewById(R.id.buttonPayment);
			        paymentButton.setOnClickListener( new View.OnClickListener() 
					{			
						@Override public void onClick(View v) 
						{
							try
							{
								onCreateDoc(Document.DOC_TYPE_PAYMENT);
							}
							catch(Exception ex)
							{
								ErrorHandler.CatchError("Exception in paymentButton.onClick", ex);			
							}
						}
					});
		        
			        //Process events from debt notification button
			        ImageButton debtNotificationButton = (ImageButton) findViewById(R.id.buttonDebtNotification);
			        debtNotificationButton.setOnClickListener( new View.OnClickListener() 
					{			
						@Override public void onClick(View v) 
						{
							try
							{
								onCreateDoc(Document.DOC_TYPE_DEBT_NOTIFICATION);
							}
							catch(Exception ex)
							{
								ErrorHandler.CatchError("Exception in debtNotificationButton.onClick", ex);			
							}
						}
					});
		        
			        //Process events from edit button
			        ImageButton editDocButton = (ImageButton) findViewById(R.id.buttonEditDocument);
			        editDocButton.setOnClickListener( new View.OnClickListener() 
					{			
						@Override public void onClick(View v) 
						{
							try
							{
								onEditDoc();
							}
							catch(Exception ex)
							{
								ErrorHandler.CatchError("Exception in editDocButton.onClick", ex);			
							}
						}
					});
			        
			        //Process events from delete button
			        ImageButton deleteButton = (ImageButton) findViewById(R.id.buttonDeleteDocument);
			        deleteButton.setOnClickListener( new View.OnClickListener() 
					{			
						@Override public void onClick(View v) 
						{
							try
							{
								onDeleteDoc();
							}
							catch(Exception ex)
							{
								ErrorHandler.CatchError("Exception in deleteButton.onClick", ex);			
							}
						}
					});
				}		        
		        
		        //display saldo info
		        View infoPanelPlacement = (View) findViewById(R.id.infoPanelPlacement);
		        infoPanelPlacement.setVisibility(View.VISIBLE);
//		        ClientForm.fillSaldo(this, AntContext.getInstance().getAddrID(),  R.id.saldoTable, 0xFFFFFFFF, false);		        
			}
	        
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in docListForm.onCreate", ex);
		}
    }

    public static void fillAddressSpinner(Context context, Spinner spnAddresses, long clientID, long currentAddrID, boolean addAllAddressesEntry, boolean blackTextColor)
    {
		spnAddresses.setVisibility(View.VISIBLE);
		
		String sqlAllAddr = addAllAddressesEntry ? String.format(" SELECT %d AS _id, '%s' as AddrName UNION ALL ", 
											DEF_ALL_ADDRESSES, context.getResources().getString(R.string.doc_list_allAddresses)):"";
		
		String sql = String.format( " %s SELECT a.AddrID AS _id, a.AddrName FROM Addresses a WHERE a.ClientID=%d " +  
							" ORDER BY AddrName",
							 sqlAllAddr, clientID);
		
		final Cursor cursor = Db.getInstance().selectSQL(sql);
		((Activity)context).startManagingCursor(cursor);

		String[] from = new String[] { "AddrName" };
        int[] to = new int[] { android.R.id.text1 };    
		SimpleCursorAdapter curAdapter = new SimpleCursorAdapter(context, R.layout.spinner_item, cursor, from, to);
		curAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		if(blackTextColor)
			curAdapter.setViewResource(R.layout.simple_spinner_dropdown_item_ex_left);
		
		spnAddresses.setAdapter(curAdapter);
		
		//select default address (this is address we started visit with)
		for(int i=0; i<spnAddresses.getCount();i++)
		{
			if(spnAddresses.getItemIdAtPosition(i) == currentAddrID)
			{
				spnAddresses.setSelection(i);
				break;
			}						
		}
    	
    }    

    //--------------------------------------------------------------
    /** Îáđŕáîň÷čę đĺäŕęňčđîâŕíč˙ äîęóěĺíňŕ*/
    public void onEditDoc()
    {
    	try
    	{
	    	if(AntContext.getInstance().getTabController().getStepControllerByType(TabControllerVisit.TAB_VISIT).haveUnsavedChanges())
	    	{
	    		//display warning that there is unsaved changes
	    		MessageBox.show(this, getResources().getString(R.string.doc_list_warning), getResources().getString(R.string.doc_list_warningSaveBeforeEdit));
	    		return;
	    	}
	    	
	    	int row = mGrid.getSelectedRow();
	    	if(row!=-1)
	    	{
	    		mCursor.moveToPosition(row);
	    		
	    		int docSource = mCursor.getInt(mCursor.getColumnIndex("DocSource"));
	    		
	    		char docType = Convert.getDocTypeFromString(mCursor.getString(mCursor.getColumnIndex("DocType")));
	    		char docState = Convert.getDocStateFromString(mCursor.getString(mCursor.getColumnIndex("State")));
	    		long docID = mCursor.getLong(mCursor.getColumnIndex("DocID"));
	    		final char BWG = Convert.getDocColorFromString(mCursor.getString(mCursor.getColumnIndex("BWG")));
	    		
	    		long editDocID = (docType==Document.DOC_TYPE_PAYMENT || docType==Document.DOC_TYPE_DEBT_NOTIFICATION || docSource == Document.DOC_SOURCE_DOC_DETAILS) 
	    							? docID : mCursor.getLong(mCursor.getColumnIndex("ParentDocID"));
	    		
	    		//ĺńëč äîęóěĺíň íĺ çŕęđűň č íĺ îňďđŕâëĺí, ňî îí îňęđîĺňń˙ íŕ đĺäŕęňčđîâŕíčĺ. â ýňîě ńëó÷ŕĺ äîëćĺí áűňü îňęđűň âčçčň
	    		if(docState!=Document.DOC_STATE_CLOSED && docState!=Document.DOC_STATE_SENT)
	    		{
	    			if(StepControllerVisit.checkVisitStarted(this) == false)
	    				return;	    			
	    		}	    			
	    						
	    		//for closed documents, copy a document from Documents to CurDocuments and pass newly generated docID to form
	    		if( docSource == Document.DOC_SOURCE_DOCUMENTS &&
	    				(docType==Document.DOC_TYPE_SALE || docType==Document.DOC_TYPE_CLAIM || docType==Document.DOC_TYPE_REMNANTS) )
	    		{
    				try
    				{
    					Db.getInstance().beginTransaction();	    				
	    			
		    			if(/*docState == Document.DOC_STATE_CLOSED &&*/ editDocID==0) //doc is closed and is not copied before
		    			{
		    				//generate new doc id and assign it to document as parent
		    				editDocID = Document.getNewCurDocID();
		    				String sql = String.format("UPDATE Documents SET ParentDocID=%d WHERE DocID=%d", editDocID, docID);
		    		        Db.getInstance().execSQL(sql);
		    			}

		    			//overwrite parent doc (delete it an then create new copy)
		    			Document.deleteFromCurDocuments(editDocID);
		    			Document.copyDocFromDocuments2CurDocuments(editDocID, docID);
		    			
			        	Db.getInstance().commitTransaction();
    				}
    				catch(Exception ex)
    				{
    					ErrorHandler.CatchError("Exception in DocListForm.onEditDoc", ex);
    					throw new RuntimeException(ex);
    				}						
    				finally
    				{
    					Db.getInstance().endTransaction();
    				}	    			
	    		}
	    			
	    		
	    		Bundle params = new Bundle();
	    		params.putLong(Document.PARAM_NAME_DOCID, editDocID);
	    		if(isDebtForm) params.putBoolean(Document.PARAM_NAME_RETURN_TO_DEBTS, true);
	
	    		
	    		/*
	    		if(docType == Document.DOC_TYPE_CLAIM)
	    			AntContext.getInstance().getTabController().startTab(DocListForm.this, TabControllerVisit.TAB_VISIT,
	    													StepControllerVisit.VISIT_STEP_DOC_ORDER, params);
	    		if(docType == Document.DOC_TYPE_SALE)
	    			AntContext.getInstance().getTabController().startTab(DocListForm.this, TabControllerVisit.TAB_VISIT,
							StepControllerVisit.VISIT_STEP_DOC_SALE, params);
	    		else if(docType == Document.DOC_TYPE_REMNANTS)
	    			AntContext.getInstance().getTabController().startTab(DocListForm.this, TabControllerVisit.TAB_VISIT,
	    													StepControllerVisit.VISIT_STEP_REMNANTS, params);
	    		*/
	    		if(docType == Document.DOC_TYPE_CLAIM || docType == Document.DOC_TYPE_SALE || docType == Document.DOC_TYPE_REMNANTS)
	    			AntContext.getInstance().getTabController().startTab(DocListForm.this, TabControllerVisit.TAB_VISIT,
															StepControllerVisit.VISIT_STEP_OPEN_DOC_SALE_FAMILY, params);
	    		else if(docType == Document.DOC_TYPE_PAYMENT)
	    			AntContext.getInstance().getTabController().startTab(DocListForm.this, TabControllerVisit.TAB_VISIT, 
	    													StepControllerVisit.VISIT_STEP_DOC_PAYMENT, params);
	    		else if(docType == Document.DOC_TYPE_DEBT_NOTIFICATION)
	    			AntContext.getInstance().getTabController().startTab(DocListForm.this, TabControllerVisit.TAB_VISIT, 
	    													StepControllerVisit.VISIT_STEP_DEBT_NOTIFICATION, params);	    		
	    	}
    	}
		catch(Exception ex)
		{	
			MessageBox.show(this, getResources().getString(R.string.form_title_docList), getResources().getString(R.string.doc_list_exceptionOnEdit));
			ErrorHandler.CatchError("Exception in docListForm.onEditDoc", ex);
		}
    }
    //--------------------------------------------------------------
    /** Îáđŕáîň÷čę óäŕëĺíč˙ äîęóěĺíňŕ*/
    public void onDeleteDoc()
    {
    	try
    	{
			if(StepControllerVisit.checkVisitStarted(this) == false)
				return;
    		
    		int row = mGrid.getSelectedRow();
    		if(row==-1)
    			return;

    		mCursor.moveToPosition(row);
    		
    		final int docSource = mCursor.getInt(mCursor.getColumnIndex("DocSource"));    		
			final char docType = Convert.getDocTypeFromString(mCursor.getString(mCursor.getColumnIndex("DocType")));
			char docState = Convert.getDocStateFromString(mCursor.getString(mCursor.getColumnIndex("State")));
			final long docID = mCursor.getLong(mCursor.getColumnIndex("DocID"));
			final long parentDocID = mCursor.getLong(mCursor.getColumnIndex("ParentDocID"));
			final char BWG = Convert.getDocColorFromString(mCursor.getString(mCursor.getColumnIndex("BWG")));
			
			if(docState == Document.DOC_STATE_CLOSED || docState == Document.DOC_STATE_SENT)
			{
				MessageBox.show(this, getResources().getString(R.string.doc_list_limitation), getResources().getString(R.string.doc_list_errorDeleteClosedDoc));
				return;
			}
			
	    	//
	    	//Display alert dialog proposing to confirm document deletion
	    	//
			
			MessageBoxButton[] buttons = new MessageBoxButton[]
	        {
	  				new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.doc_list_alertChoiceDelete),
	  							new DialogInterface.OnClickListener()
	  							{
	  								@Override public void onClick(DialogInterface dialog, int which) 
	  								{
	  									try
	  									{
	  										Document.deleteDocument(docID, docType, parentDocID, BWG, docSource);
	  										fillGridWithData(); //refresh grid
	  									}
	  									catch(Exception ex)
	  									{
	  										MessageBox.show(DocListForm.this, getResources().getString(R.string.form_title_docList), getResources().getString(R.string.doc_list_exceptionOnDelete));
	  										ErrorHandler.CatchError("Exception in docListForm.onDeleteDoc", ex);
	  									}
	  								}
	  							}),
	  				new MessageBoxButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.document_alertChoiceCancel),
	  							new DialogInterface.OnClickListener()
	  							{
	  								@Override public void onClick(DialogInterface dialog, int which) { }
	  							})								
	        };
			                                          		
      		MessageBox.show(this, getResources().getString(R.string.doc_list_warning), 
      									getResources().getString(R.string.doc_list_warningDeleteDocument), buttons);			
    	}
		catch(Exception ex)
		{			
			MessageBox.show(this, getResources().getString(R.string.form_title_docList), getResources().getString(R.string.doc_list_exceptionOnDelete));
			ErrorHandler.CatchError("Exception in docListForm.onDeleteDoc", ex);
		}    	
    }    

    //--------------------------------------------------------------
    /** Ńîçäŕíčĺ íîâîăî äîęóěĺíňŕ (îďëŕňŕ čëč óâĺäîěëĺíčĺ î äîëăĺ*/
    public void onCreateDoc(char docToCreate)
    {
    	try
    	{
			if(StepControllerVisit.checkVisitStarted(this) == false)
				return;
    		
	    	if(AntContext.getInstance().getTabController().getStepControllerByType(TabControllerVisit.TAB_VISIT).haveUnsavedChanges())
	    	{
	    		//display warning that there is unsaved changes
	    		MessageBox.show(this, getResources().getString(R.string.doc_list_warning), getResources().getString(R.string.doc_list_warningSaveBeforeCreate));
	    		return;
	    	}
	    	
	    	char docType = Document.DOC_TYPE_UNKNOWN;
	    	int row = mGrid.getSelectedRow();
	    	
	    	if(row!=-1)
	    	{
	    		mCursor.moveToPosition(row);
	    		
	    		int docSource = mCursor.getInt(mCursor.getColumnIndex("DocSource"));
	    		if(docSource == Document.DOC_SOURCE_DOC_DETAILS)
	    			return;
	    		
	    		long docId = mCursor.getLong(mCursor.getColumnIndex("DocID"));
	    		docType = Convert.getDocTypeFromString(mCursor.getString(mCursor.getColumnIndex("DocType")));

	    		if(docToCreate == Document.DOC_TYPE_PAYMENT)
	    		{
		    		if(docType == Document.DOC_TYPE_CLAIM || docType == Document.DOC_TYPE_SALE)
		    		{
			    		Bundle params = new Bundle();
			    		params.putLong(Document.PARAM_NAME_BASEDOCID, docId);
			    		if(isDebtForm)	params.putBoolean(Document.PARAM_NAME_RETURN_TO_DEBTS, true);			    		
		    			AntContext.getInstance().getTabController().startTab(DocListForm.this, TabControllerVisit.TAB_VISIT, 
		    																	StepControllerVisit.VISIT_STEP_DOC_PAYMENT, params);
		    		}
		    		else if(docType == Document.DOC_TYPE_PAYMENT)
		    		{
		    			onEditDoc();	
		    		}
	    		}
	    	}
	    	
	    	if(docToCreate == Document.DOC_TYPE_DEBT_NOTIFICATION)
    		{
	    		//if(docType!=Document.DOC_TYPE_DEBT_NOTIFICATION)
	    		{
		    		Bundle params = new Bundle();
		    		params.putBoolean(Document.PARAM_NAME_CREATE_NEW, true);
		    		if(isDebtForm)	params.putBoolean(Document.PARAM_NAME_RETURN_TO_DEBTS, true);
	    			
	    			AntContext.getInstance().getTabController().startTab(DocListForm.this, TabControllerVisit.TAB_VISIT, 
	    																	StepControllerVisit.VISIT_STEP_DEBT_NOTIFICATION, params);
	    		}
	    		/*else 
	    		{
	    			onEditDoc();
	    		}*/
    		}	    	
    	}
		catch(Exception ex)
		{
			MessageBox.show(this, getResources().getString(R.string.form_title_docList), getResources().getString(R.string.doc_list_exceptionOnDebtNotification));
			ErrorHandler.CatchError("Exception in docListForm.onCreateDocDebtNotification", ex);
		}    	
    } 
    //--------------------------------------------------------------
    /** Ďĺ÷ŕňü äîęóěĺííňŕ (ďĺ÷ŕňŕţňń˙ çŕ˙âęč č ďđîäŕćč)*/
    public void onPrintDoc()
    {
    	int row = mGrid.getSelectedRow();
    	char docType = Document.DOC_TYPE_UNKNOWN;

    	if(row!=-1)
    	{
    		mCursor.moveToPosition(row);
    		
    		long docId = mCursor.getLong(mCursor.getColumnIndex("DocID"));
    		docType = Convert.getDocTypeFromString(mCursor.getString(mCursor.getColumnIndex("DocType")));

    		if(docType == Document.DOC_TYPE_CLAIM || docType == Document.DOC_TYPE_SALE)
    		{
	        	PrintDocForm printForm = new PrintDocForm();
	        	Dialog dialog = printForm.onCreate(this, docId);
	        	dialog.show();
    		}
    	}
    }    
    
    //--------------------------------------------------------------
    /** Číčöčŕëčçŕöč˙ ýëĺěĺíňŕ grid*/ 
    private void initGrid()
    {
        mGrid = (DataGrid) findViewById(R.id.dataGridDocList);
        
        /*DataGrid.ColumnInfo[] columns = new DataGrid.ColumnInfo[]
        {
        		new DataGrid.ColumnInfo(1, "DocID", Types.INTEGER, 50, DataGrid.GRID_COLUMN_HIDDEN, "DocID", 0),
        		new DataGrid.ColumnInfo(2, getResources().getString(R.string.doc_list_column_header_date), Types.DATE, 90, DataGrid.GRID_COLUMN_DEFAULT, "DocDate", 1),
        		new DataGrid.ColumnInfo(3, getResources().getString(R.string.doc_list_column_header_respite), Types.VARCHAR, 40, DataGrid.GRID_COLUMN_DEFAULT, "Respite", 2),
        		new DataGrid.ColumnInfo(4, getResources().getString(R.string.doc_list_column_header_respiteLeft), Types.VARCHAR, 40, DataGrid.GRID_COLUMN_CALCULABLE|DataGrid.GRID_COLUMN_DENY_SORTING, "RespiteLeft", 3),
        		new DataGrid.ColumnInfo(5, getResources().getString(R.string.doc_list_column_header_sumAll), Types.VARCHAR, 60, DataGrid.GRID_COLUMN_DEFAULT, "SumAll", 4),
        		new DataGrid.ColumnInfo(6, getResources().getString(R.string.doc_list_column_header_sumFree), Types.VARCHAR, 60, DataGrid.GRID_COLUMN_DEFAULT, "SumFree", 5),        		
        		new DataGrid.ColumnInfo(7, getResources().getString(R.string.doc_list_column_header_salerInfo), Types.VARCHAR, 100, DataGrid.GRID_COLUMN_DEFAULT, "SalerName", 6),
        		new DataGrid.ColumnInfo(8, getResources().getString(R.string.doc_list_column_header_docType), Types.VARCHAR, 100, DataGrid.GRID_COLUMN_DEFAULT, "ReadableDocType", 7),
        		new DataGrid.ColumnInfo(9, getResources().getString(R.string.doc_list_column_header_docState), Types.VARCHAR, 100, DataGrid.GRID_COLUMN_DEFAULT, "ReadableDocState", 8)
        };
        GridColumns gridColumns = new GridColumns(columns);*/ 
        
        int columnsSetId = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_COLUMNS_SET_ID_DOC_LIST, 3);        
        GridColumns gridColumns = new GridColumns(columnsSetId);        
        mGrid.setColumns(gridColumns);

        mGrid.setCellListener(new CellListener());
        mGrid.setDefaultSortColumnByDbField("DocDate");
        
        mGrid.setSortOrder(DataGrid.SortOrder.ASCENDING);
        mGrid.setIdentityColumn(0); // TODO maybe move to settings       
        
        //get grid row height from settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int gridRowHeight = Convert.toInt(prefs.getString(getString(R.string.preferences_grid_row_height_key), "40"), 40);        
        int gridTextSize = Convert.toInt(prefs.getString(getString(R.string.preferences_grid_text_size_key), "16"), 16);
        int gridFlingVelocity = Convert.toInt(prefs.getString(getString(R.string.preferences_grid_fling_velocity_key), "15"), 15);
        
        mGrid.setRowHeight(gridRowHeight);
        mGrid.setTextSize(gridTextSize);
        mGrid.setHeaderTextSize(gridTextSize);
        mGrid.setFlingVelocity(gridFlingVelocity);
                
        fillGridWithData();
    }

    //--------------------------------------------------------------------
    /** Çŕďîëíĺíčĺ ýëĺěĺíňŕ grid ńďčńęîě äîęóěĺíňîâ*/
    private void fillGridWithData()
    {
    	String docTypeField = Document.getReadableDocTypes(this, "d");    	
    	String docStateField = Document.getReadableDocStates(this, "d");
    	String selectSumFree = 	String.format(" CASE d.DocType when '%s' then d.SumLinked" +
				 				" else round(d.SumAll - d.SumLinked,2) end " +
				 				"AS SumFree ", Document.DOC_TYPE_DEBT_NOTIFICATION);
    	
    	//where
    	String docStateFilter = Q.getDocStateFilter();

    	String where = String.format(" WHERE d.ClientID=%d %s ", clientID, addrID != DEF_ALL_ADDRESSES ? String.format(" AND d.AddrID=%d ", addrID) : "" );   	    	
    	
    	//display only unpaid sale documents
    	if(mCheckUnpaid.isChecked())
    		where = where + String.format(" AND ( (d.DocType <> '%s' AND d.DocType <> '%s') OR SumFree > 0) AND %s ",
    									Document.DOC_TYPE_CLAIM, Document.DOC_TYPE_SALE, Q.getRespiteFilter());    	
    	
    	String where1 = String.format(" %s AND %s", where, docStateFilter);
    	
    	// sorting
    	String sortColumn = mGrid.getDbFieldForColumn(mGrid.getSortColumn());
    	String sortOrder = (mGrid.getSortOrder() == DataGrid.SortOrder.ASCENDING)? "ASC": "DESC";
    	String orderBy = " ORDER BY " + sortColumn + " " + sortOrder;
    	
    	
    	String select = " SELECT " + docTypeField + ", " + docStateField + ", d.DocType AS DocType, d.State AS State, d.DocID AS DocID, d.SumAll AS SumAll," +
						" d.SumWOVAT AS SumWOVAT, d.DocDate AS DocDate, d.DocNumber AS DocNumber, d.CreateDate AS CreateDate, d.Comments AS Comments, d.Respite AS Respite, " +
						"s.SalerName AS SalerName, a.AddrName as AddrName, c.ContactID AS ContactID, c.FIO AS FIO, doctypes.StyleID AS StyleID, d.ExtID as ExtID "; 
						
		String joins = " LEFT JOIN Salers s ON d.SalerID = s.SalerID " +
	 	 			  " LEFT JOIN Addresses a ON d.AddrID = a.AddrID " +
	 	 			  " LEFT JOIN Contacts c ON d.ContactID=c.ContactID " +
	 	 			  " LEFT JOIN DocTypes doctypes ON d.DocType=doctypes.DocTypeChar ";
		
    	// document drafts in curDocDetails
    	String unionDrafts = ""; 
    	
    	if(!isDebtForm)
    	{
    		String where2 = String.format(" %s AND d.State='%s'", where, Document.DOC_STATE_NEW);
    		    		
    		unionDrafts = 
    				" UNION ALL " +
					 String.format("%s, 0 AS SumFree, s.SalerName AS SalerInfo, 0 AS ParentDocID, '%s' AS BWG, %d AS DocSource",
							 	select, Document.DOC_COLOR_UNKNOWN, Document.DOC_SOURCE_DOC_DETAILS) + 
					 " FROM CurDocuments d " + 
					 	joins +
				 	where2;
    	}
    	
    	//sql
    	String sql = String.format( "%s, %s , d.SalerInfo AS SalerInfo, d.ParentDocID AS ParentDocID, d.BWG AS BWG, %d AS DocSource ",
    							select, selectSumFree, Document.DOC_SOURCE_DOCUMENTS) +   
    				 " FROM Documents d " +
    				 	joins +
    				  where1 +  
    				  unionDrafts + 
    			      orderBy;
    	
    	mCursor = Db.getInstance().selectSQL(sql);
    	startManagingCursor(mCursor);
    	mGrid.setCursor(mCursor); 	
    }
    
    //--------------------------------------------------------------
    /** Îáđŕáîňęŕ ńîáűňčé ýëĺěĺíňŕ grid*/
    private class CellListener extends DataGrid.BaseCellListener 
    {    	
        @Override public void onHeaderClicked(int column, boolean sortOrderChanged)
        {
        	try
        	{
        		//header click implies changes in sort order. we need to re-read Cursor and set it to DataGrid
        		fillGridWithData();
        	}
    		catch(Exception ex)
    		{			
    			ErrorHandler.CatchError("Exception in docListForm.onHeaderClicked", ex);
    		}    	        	
        }        
        
        @Override public Object onCellCalculate(int row, int column, Object[] values)
        {
        	try
        	{
	        	String columnName = mGrid.getDbFieldForColumn(column);        	
	        	Object retValue = null;
	        	
	        	if(columnName.equals("RespiteLeft"))
	        	{   
	        		int respite = mCursor.getInt(mCursor.getColumnIndex("Respite"));
	        		String strDocDate = mCursor.getString(mCursor.getColumnIndex("DocDate"));
	        		double sumFree = mCursor.getDouble(mCursor.getColumnIndex("SumFree"));
	        		if(sumFree>0.0001)
	        		{
		        		Calendar dueDate =  Convert.getDateFromString(strDocDate);
		        		dueDate.add(Calendar.DAY_OF_MONTH, respite);
		        		Calendar nowDate = Calendar.getInstance();    	
		        		
		        		long diff = Convert.getDateDiffInDays(nowDate, dueDate);
		        		retValue = diff;
		        		return retValue;
	        		}
	        		else
	        		{
	        			//do not display RespiteLeft for paid documents
	        			return "";
	        		}
	        	}
        	}
    		catch(Exception ex)
    		{			
    			ErrorHandler.CatchError("Exception in docListForm.onCellCalculate", ex);
    		}   	        	
        	
        	return null;
        }
        
        @Override public CellStyle onCalculateRowStyle() 
        { 
        	try
        	{
	        	int styleColumn = mCursor.getColumnIndex("StyleID");        	
	        	if(!mCursor.isNull(styleColumn))
	        	{
	        		ArrayList<Integer> appliedStyles = new ArrayList<Integer>();
	        		int styleID = mCursor.getInt(styleColumn);
	        		CellStyleCollection styles = AntContext.getInstance().getStyles();
	        		appliedStyles.add(styleID);
		        	CellStyle style = styles.getCompositeStyle(appliedStyles);	        	
		        	return style;        		
	        		//return styles.get(styleID);
	        	}
	        	
        	}
        	catch(Exception ex)
        	{
        		ErrorHandler.CatchError("Exception in DocListForm.onCalculateRowStyle", ex);	
        	}
	        	
        	return CellStyleCollection.getDefault();
        }        
    } 
     
    //--------------------------------------------------------------
    private void initStepBar()
    {
    	//init steps
    	if(isDebtForm == false)
    	{
	    	ViewGroup stepButtonPlacement = (ViewGroup) findViewById(R.id.stepButtonPlacement);
	    	AntContext.getInstance().getStepController().CreateButtons(this, stepButtonPlacement, StepPanelType.HORIZONTAL);
    	}
    	
    	//init tabs
    	ViewGroup tabsPlacement = (ViewGroup) findViewById(R.id.tabsPlacement);
    	AntContext.getInstance().getTabController().createTabs(this, tabsPlacement);
    }

    //--------------------------------------------------------------
    @Override public void onBackPressed() 
    {
    	try
    	{
    		if(mGrid!=null)
    			mGrid.getColumns().save();
    		
    		if(fromVisit)
    			AntContext.getInstance().getTabController().onBackPressed(this);
    		else
    			this.finish();
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in docListForm.onBackPressed", ex);
		}    	        	
    }

}

