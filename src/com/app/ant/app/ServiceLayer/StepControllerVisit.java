package com.app.ant.app.ServiceLayer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import com.app.ant.R;
import com.app.ant.app.Activities.*;
import com.app.ant.app.Activities.MessageBox.MessageBoxButton;
import com.app.ant.app.BusinessLayer.Address;
import com.app.ant.app.BusinessLayer.Document;
import com.app.ant.app.BusinessLayer.Visit;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.Questions.QuestionsController;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class StepControllerVisit extends StepController
{
	public static final int		VISIT_STEP_MENU						= 0;
	public static final int		VISIT_STEP_START_VISIT				= 1;
	public static final int		VISIT_STEP_PLANS					= 2;
	public static final int		VISIT_STEP_AIMS						= 3;
	public static final int		VISIT_STEP_DEBTS					= 4;
	public static final int		VISIT_STEP_DOC_ORDER				= 5;
	public static final int		VISIT_STEP_VISIT_SUMMARIES			= 6;
	public static final int		VISIT_STEP_DAY_SUMMARIES			= 7;
	public static final int		VISIT_STEP_END_VISIT				= 8;
	public static final int		VISIT_STEP_ONE_PAGE					= 9;
	public static final int		VISIT_STEP_GPS_CHECK				= 10;
	public static final int		VISIT_STEP_QUESTIONAIRE_PG			= 11;
	public static final int		VISIT_STEP_QUESTIONAIRE_PG2			= 12;
	public static final int		VISIT_STEP_DOC_SALE					= 13;
	//TODO dynamic steps
	public static final int		VISIT_STEP_QUESTIONAIRE_SAV1		= 14;
	public static final int		VISIT_STEP_QUESTIONAIRE_SAV2		= 15;
	public static final int		VISIT_STEP_QUESTIONAIRE_SAV3		= 16;

	public static final int		VISIT_STEP_DEBT_NOTIFICATION		= 1000;
	public static final int		VISIT_STEP_DOC_PAYMENT				= 1001;
	public static final int		VISIT_STEP_REMNANTS					= 1002;
	public static final int		VISIT_STEP_DPSM						= 1003;
	public static final int		VISIT_STEP_OPEN_DOC_SALE_FAMILY		= 1004;
	
	/*private StepInfo[] steps = new StepInfo[]
    {
		new StepInfo(R.string.visit_step_visit, R.drawable.start_visit, STEP_INVISIBLE | STEP_MENU),
		new StepInfo(R.string.visit_step_startVisit, R.drawable.start_visit, STEP_ACTION),
		new StepInfo(R.string.form_title_plans, R.drawable.aim, STEP_DEFAULT ),
		new StepInfo(R.string.form_title_aims, R.drawable.aim, STEP_DEFAULT),
		new StepInfo(R.string.visit_step_debt, R.drawable.step_doc_payment, STEP_DEFAULT),
		new StepInfo(R.string.visit_step_debtNotification, R.drawable.step_doc_payment, STEP_DEFAULT | STEP_INVISIBLE),
		new StepInfo(R.string.visit_step_docPayment, R.drawable.step_doc_payment, STEP_INVISIBLE),
		new StepInfo(R.string.visit_step_remnants, R.drawable.step_remnants, STEP_DEFAULT | STEP_INVISIBLE),
		new StepInfo(R.string.visit_step_dpsm, R.drawable.dpsm, STEP_ACTION | STEP_INVISIBLE),
		new StepInfo(R.string.visit_step_docSale, R.drawable.step_doc_sale, STEP_DEFAULT),
		new StepInfo(R.string.visit_step_visit_summaries, R.drawable.step_doc_header, STEP_DEFAULT),
		new StepInfo(R.string.visit_step_day_summaries, R.drawable.step_doc_header, STEP_DEFAULT),
		new StepInfo(R.string.visit_step_endVisit, R.drawable.end_visit, STEP_ACTION)
    };*/
	
	long currentStepID = -1;
	int visitQuitReasonIdx = 0;
	Cursor visitQuitReasonsCursor;
	
	private ServiceConnection mGPSServiceConnection;
	
    //--------------------------------------------------------------
	public	StepControllerVisit()
	{	
		loadSteps(1);
	}

	//--------------------------------------------------------------
	protected void loadSteps(long visitTypeMask)
	{
		addStep( new StepInfo(VISIT_STEP_MENU, R.string.visit_step_visit, R.drawable.start_visit, STEP_INVISIBLE | STEP_MENU, false));
		addStep( new StepInfo(VISIT_STEP_DEBT_NOTIFICATION, R.string.visit_step_debtNotification, R.drawable.step_doc_payment, STEP_DEFAULT | STEP_INVISIBLE, false));
		addStep( new StepInfo(VISIT_STEP_DOC_PAYMENT, R.string.visit_step_docPayment, R.drawable.step_doc_payment, STEP_INVISIBLE, false));
		addStep( new StepInfo(VISIT_STEP_OPEN_DOC_SALE_FAMILY, R.string.visit_step_docSale, R.drawable.step_doc_sale, STEP_INVISIBLE, false));
		
		// read info from Steps table 
		
		String sql = " SELECT DISTINCT s.StepID, s.StepName, s.IsMandatory, s.SortID " + 
					" FROM Steps s " + 
					" 			INNER JOIN VisitTypeSteps vs ON s.StepID = vs.StepID " + 
					" 			INNER JOIN VisitTypes vt ON vs.VisitTypeID = vt.VisitTypeID AND vt.VisitTypeMS & " + visitTypeMask + 
					" ORDER BY s.SortID";
    	Cursor cursor = Db.getInstance().selectSQL(sql);
    	
    	if(cursor != null)
    	{
        	int stepIdColumnIdx = cursor.getColumnIndex("StepID");
    		int stepNameColumnIdx = cursor.getColumnIndex("StepName");
    		int mandatoryColumnIdx = cursor.getColumnIndex("IsMandatory");
    		
    		for(int i=0; i<cursor.getCount(); i++)
    		{   
    			cursor.moveToPosition(i);
    			
    			int stepId = cursor.getInt(stepIdColumnIdx);
    			String stepName = cursor.getString(stepNameColumnIdx);
    			boolean isMandatory = cursor.getInt(mandatoryColumnIdx) > 0;
    	
    			int picResourceId = R.drawable.start_visit; 
    			int flags = STEP_DEFAULT;    			
    			
    			switch(stepId)
    			{
    				case VISIT_STEP_START_VISIT:
    					picResourceId = R.drawable.start_visit;
    					flags = STEP_ACTION;
    					break;
    				case VISIT_STEP_PLANS:
    					picResourceId = R.drawable.plans;
    					flags = STEP_DEFAULT;
    					break;
    				case VISIT_STEP_GPS_CHECK:
    					picResourceId = R.drawable.gps;
    					flags = STEP_DEFAULT;
    					break;
    				case VISIT_STEP_ONE_PAGE:
    					picResourceId = R.drawable.step_doc_header;
    					flags = STEP_DEFAULT;    					
    					break;
    				case VISIT_STEP_AIMS:
    					picResourceId = R.drawable.aim;
    					flags = STEP_DEFAULT;
    					break;
    				case VISIT_STEP_DEBTS:
    					picResourceId = R.drawable.step_doc_payment;
    					flags = STEP_DEFAULT;
    					break;
    				case VISIT_STEP_DOC_ORDER:
    					picResourceId = R.drawable.step_doc_sale;
    					flags = STEP_DEFAULT;
    					break;
    				case VISIT_STEP_VISIT_SUMMARIES:
    					picResourceId = R.drawable.visit_summaries;
    					flags = STEP_DEFAULT;
    					break;
    				case VISIT_STEP_DAY_SUMMARIES:
    					picResourceId = R.drawable.day_summaries;
    					flags = STEP_DEFAULT;
    					break;
    				case VISIT_STEP_END_VISIT:
    					picResourceId = R.drawable.end_visit;
    					flags = STEP_ACTION;
    					break;
    				case VISIT_STEP_QUESTIONAIRE_PG:
    					picResourceId = R.drawable.step_doc_header;
    					Address adr = AntContext.getInstance().getVisit().getAddress();
    					boolean needThisStep = checkStepQuestionnairePG();
    					flags =  needThisStep ? STEP_DEFAULT : STEP_DEFAULT | STEP_INVISIBLE;
    					break;
    				case VISIT_STEP_QUESTIONAIRE_PG2:
    				case VISIT_STEP_QUESTIONAIRE_SAV1:
    				case VISIT_STEP_QUESTIONAIRE_SAV2:
    				case VISIT_STEP_QUESTIONAIRE_SAV3:    					
    					picResourceId = R.drawable.step_doc_header;
    					flags = STEP_ACTION;
    					break;
    			}
    			
    			addStep( new StepInfo(stepId, stepName, picResourceId, flags, isMandatory));
    		}
    		
    		cursor.close();
    	} 		
	}
	
	//--------------------------------------------------------------
	public static boolean checkVisitStarted(Context context)
	{
		if(!AntContext.getInstance().getVisit().isStarted())
		{
			MessageBoxButton[] buttons = new MessageBoxButton[]
            {
					new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, context.getResources().getString(R.string.visit_warning_ok),
									new DialogInterface.OnClickListener()
									{
										@Override public void onClick(DialogInterface dialog, int which) { }
									})
            };
    		MessageBox.show(context, context.getResources().getString(R.string.visit_warning),	context.getResources().getString(R.string.visit_start_visit_warning), buttons);

			return false;
		}
		
		return true;
	}

	//--------------------------------------------------------------
	public void startVisit(final Context context, final StepInfo step)
	{
		selectVisitType(context, step, true);
	}
	
	//-----------------------------------------------------------------------------------------
	public void selectVisitType(final Context context, final StepInfo step, final boolean startVisit)
	{
		int visitType = AntContext.getInstance().getVisit().getVisitType();
		
        SelectVisitTypeDialog dlg = new SelectVisitTypeDialog();
        dlg.show(context, visitType, startVisit, 
        		new SelectVisitTypeDialog.VisitTypeSelectListener()
		        {
					public void onVisitTypeSelected(int visitType, boolean needDocCountCheck)
		            {
						AntContext.getInstance().getVisit().setVisitType(visitType);
						if(startVisit)
							AntContext.getInstance().getVisit().start(context, visitType, needDocCountCheck);
						loadSteps(visitType);
				
						StepInfo si = findStepByID(VISIT_STEP_START_VISIT);
						if(si!=null)
						{
							si.labelResourceId = R.string.visit_step_change_visit_type;
							si.fulfilled = true;
						}							
						
						startStep(context, VISIT_STEP_MENU, false, null);									
		            }
		        },
        		new DialogInterface.OnClickListener() 
		        {
		            public void onClick(DialogInterface dialog, int result) { }
		        }
       		);
	}
	
	private boolean checkStepQuestionnairePG()
	{
		Address adr = AntContext.getInstance().getVisit().getAddress(); 
		boolean needStepQuestionnairePG = adr.isSomeGoldCheckout() || adr.isSomeGoldMag();
		boolean result = needStepQuestionnairePG;
				
		if (needStepQuestionnairePG)
		{
			/*String sql = "select case when exists (select * from QuestionTargets qt where qt.AddrID = " + adr.addrID + ") then count(*) else 1 end as cnt " +
					"	  from VisitQuestionResults r " +
					"			inner join Visits v on r.VisitID = v.VisitID " +
					"	  where v.AddrID = " + adr.addrID +
					"			and v.VisitStartDate " + Q.getSqlBetweenDayStartAndEnd(new Date());*/
            
            String sql = "SELECT 1 As ShowStorCheckBtn FROM AddressAttributes aa, Settings s\n" +
                    "WHERE aa.AttrID = s.DefaultValue\n" +
                    "AND s.Property = 'attr_id_delivery'\n" +
                    "AND EXISTS (SELECT 1 FROM CustGSInfos GS\n" +
                    "            WHERE GS.AddrID = aa.AddrID)\n" +
                    "AND aa.AddrID = " + adr.addrID;
			
			long count = Db.getInstance().getDataLongValue(sql, 0);		
			
			result = count > 0;
		}
		return result;
	}
	
    //--------------------------------------------------------------

	public boolean checkIsVisitEffective(final Context context, final StepInfo step )
	{
		Visit visit = AntContext.getInstance().getVisit(); 
		
		if(!visit.requiresDocCountCheck())
			return true;
		
		if(visit.isStarted() && !visit.isFinished())
		{
			{
				String sql = String.format(" SELECT count(DISTINCT d.DocID) AS DocCount " +
											" FROM Documents d " +
											" WHERE d.VisitID=%d", visit.getVisitID());
				long documentCount = Db.getInstance().getDataLongValue(sql, 0);
				
				if(documentCount>0)
					return true;
			}
			
			MessageBoxButton[] buttons = new MessageBoxButton[]
            {
      				new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, context.getResources().getString(R.string.visit_start_visit_select),
						new DialogInterface.OnClickListener()
						{
							@Override public void onClick(DialogInterface dialog, int which) 
							{
								int visitQuitReasonId = 0;
								
								if(visitQuitReasonsCursor!=null && visitQuitReasonIdx>=0 && visitQuitReasonIdx<visitQuitReasonsCursor.getCount())
								{
									visitQuitReasonsCursor.moveToPosition(visitQuitReasonIdx);
									visitQuitReasonId = visitQuitReasonsCursor.getInt(visitQuitReasonsCursor.getColumnIndex("ReasonID"));
								}
								
								AntContext.getInstance().getVisit().finish(context, visitQuitReasonId);
								step.fulfilled = true;
								FinishPrevActivity(context);
							}
						}),
      				new MessageBoxButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(R.string.visit_start_visit_cancel),
						new DialogInterface.OnClickListener()
						{
							@Override public void onClick(DialogInterface dialog, int which) { }
						})								
            };
			                                          		
      		CharSequence[] items = null;
      		
      		if(visitQuitReasonsCursor == null || visitQuitReasonsCursor.isClosed() )
      		{
      			String sql = "SELECT ReasonID, ReasonName FROM VisitEndReasons";
      			visitQuitReasonsCursor = Db.getInstance().selectSQL(sql);		
      			((Activity)context).startManagingCursor(visitQuitReasonsCursor);
      		}		
			                                              	
          	if(visitQuitReasonsCursor!=null)
          	{
          		int nameColumnIdx = visitQuitReasonsCursor.getColumnIndex("ReasonName");
          		items = new CharSequence[visitQuitReasonsCursor.getCount()];
          		
          		for(int i=0; i<visitQuitReasonsCursor.getCount(); i++)
          		{
          			visitQuitReasonsCursor.moveToPosition(i);
          			String visitName = visitQuitReasonsCursor.getString(nameColumnIdx);
          			items[i] = visitName; 
          		}
          	}
			                                          		                                          		
    		MessageBox.show(context, context.getResources().getString(R.string.visit_inefficient_visit_warning), 
    							context.getResources().getString(R.string.visit_start_visit_confirm), 
    							buttons,
    							null,
    							items,
    							0,
    							new DialogInterface.OnClickListener() 
    							{
    						    	public void onClick(DialogInterface dialog, int item) 
    						    	{
    						    		visitQuitReasonIdx = item;  						    
    						    	}
    							}
  						);
			
    		return false;
		}		
		
		return true;
	}
		
	//---------------------------------------------------	
	private static long getMaxVisitStepID()
    {
		String sql = "SELECT max(VisitStepID) FROM VisitSteps";
		long res = Db.getInstance().getDataLongValue(sql, 0);		
        return res;
    }	
	
	//--------------------------------------------------------------
	private void logVisitStep(int stepID)
	{
		long visitID = AntContext.getInstance().getVisit().getVisitID();
		String sqlDate = Convert.getSqlDateTimeFromCalendar(Calendar.getInstance());
		
		currentStepID = getMaxVisitStepID() + 1;
		
		String sql = "INSERT INTO VisitSteps(VisitStepID, VisitID, StepID, StartDate, EndDate) VALUES(?,?,?,?,?)";		
		Object[] bindArgs = new Object[] { currentStepID, visitID, stepID, sqlDate, sqlDate }; 
		Db.getInstance().execSQL(sql, bindArgs);
	}
	
	//--------------------------------------------------------------
	private void logVisitStepFinish()
	{
		if(currentStepID != -1)
		{
			String sqlDate = Convert.getSqlDateTimeFromCalendar(Calendar.getInstance());
	
			String sqlUpdate = "UPDATE VisitSteps SET EndDate=? WHERE VisitStepID=?"; 
			Object[] bindArgs = new Object[] { sqlDate, currentStepID};
			Db.getInstance().execSQL(sqlUpdate, bindArgs);
			
			currentStepID = -1;
		}
	}	
	
	// --------------------------------------------------------------
	public ProgressDialog displayProgressDialog(Context context, String message)
	{
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(message);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.show();
		
		return progressDialog;
	}
	// --------------------------------------------------------------
	public void dismissProgressDialog(ProgressDialog progressDialog)
	{
		progressDialog.dismiss();
	}	
	
    //--------------------------------------------------------------
	@Override public boolean startStep(final Context context, int stepId, boolean fromTabController, Bundle params)
	{		
		boolean cancelled = false;		

		final StepInfo step = findStepByID(stepId);
		
		if(step == null)
		{
			startStep(context, VISIT_STEP_MENU, false, null); //no cached activity, start visit step
			return false;
		}
		
		logVisitStepFinish();
		Intent qIntent = null;
		switch(stepId)
		{
			case VISIT_STEP_MENU:
				finishCachedActivities();
				
				context.startActivity(new Intent(context, VisitForm.class));
				FinishPrevActivity(context);
				break;
			case VISIT_STEP_QUESTIONAIRE_PG:
				finishCachedActivities();
				
				qIntent = new Intent(context, QuestionsController.class);
/*				qIntent.putExtra("visitId", AntContext.getInstance().getVisit().getVisitID());
				qIntent.putExtra("questId", 1L);
				qIntent.putExtra("isMandatory", step.isMandatory);
				qIntent.putExtra("byAddress", true);*/
				
				context.startActivity(qIntent);
				FinishPrevActivity(context);
				break;
			case VISIT_STEP_QUESTIONAIRE_PG2:
				finishCachedActivities();
				
				qIntent = new Intent(context, QuestionnairesForm.class);
				qIntent.putExtra("visitId", AntContext.getInstance().getVisit().getVisitID());
				qIntent.putExtra("questId", 2L);
				qIntent.putExtra("isMandatory", true);
				qIntent.putExtra("byAddress", false);
				
				context.startActivity(qIntent);
				FinishPrevActivity(context);
				break;
			case VISIT_STEP_QUESTIONAIRE_SAV1:
				finishCachedActivities();
				
				qIntent = new Intent(context, QuestionnairesForm.class);
				qIntent.putExtra("visitId", AntContext.getInstance().getVisit().getVisitID());
				qIntent.putExtra("questId", 4L);
				qIntent.putExtra("isMandatory", true);
				qIntent.putExtra("byAddress", false);
				
				context.startActivity(qIntent);
				FinishPrevActivity(context);
				break;
			case VISIT_STEP_QUESTIONAIRE_SAV2:
				finishCachedActivities();
				
				qIntent = new Intent(context, QuestionnairesForm.class);
				qIntent.putExtra("visitId", AntContext.getInstance().getVisit().getVisitID());
				qIntent.putExtra("questId", 5L);
				qIntent.putExtra("isMandatory", true);
				qIntent.putExtra("byAddress", false);
				
				context.startActivity(qIntent);
				FinishPrevActivity(context);
				break;
			case VISIT_STEP_QUESTIONAIRE_SAV3:
				finishCachedActivities();
				
				qIntent = new Intent(context, QuestionnairesForm.class);
				qIntent.putExtra("visitId", AntContext.getInstance().getVisit().getVisitID());
				qIntent.putExtra("questId", 6L);
				qIntent.putExtra("isMandatory", true);
				qIntent.putExtra("byAddress", false);
				
				context.startActivity(qIntent);
				FinishPrevActivity(context);
				break;
				
			case VISIT_STEP_START_VISIT:
				//start visit
				if(fromTabController == false)
				{
					if(!AntContext.getInstance().getVisit().isStarted())
					{
						String sql = Q.getTodayNotSentOrdersCount();
						Long docCount = Db.getInstance().getDataLongValue(sql, 0);
												
						Date date = new Date();
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
						
						String lastSyncDateFromPrefs = Settings.getInstance().getStringSyncPreference(context.getResources().getString(R.string.preference_key_last_sync_date), df.format(date));						
						Calendar lastSync = Convert.getDateFromString(lastSyncDateFromPrefs);
						
						int syncRestPeriod = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_SHOW_DIALOG_SYNC_REST_EVERY_N_MINUTES);
						
						if (docCount >= Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_ALLOWED_NOT_SENT_ORDERS_QNT, 3))
						{
							String title = context.getResources().getString(R.string.sync_need_sync);
							String msg = context.getResources().getString(R.string.sync_documents_not_sended);
														
							((AntActivity) context).CreateSynchronizationDialog(Synchronizer.SYNC_TYPE_FULL_SEND | Synchronizer.SYNC_TYPE_INCR_SEND, true,
									new DialogInterface.OnDismissListener()
									{
										@Override public void onDismiss(DialogInterface dialog)
									    {
											startVisit(context, step);
									    }			
									},
									title, msg);
						}
						else if (Convert.getDateDiffInMinutes(Calendar.getInstance(), lastSync) > syncRestPeriod)
						{											
							((AntActivity)context).CreateSynchronizationDialog(Synchronizer.SYNC_TYPE_REST, true, 
									new DialogInterface.OnDismissListener()
									{
										@Override public void onDismiss(DialogInterface dialog)
									    {
											startVisit(context, step);
									    }
									}, 
									null, null);
						}
						else
						{
							startVisit(context, step);
						}
						//AntContext.getInstance().getVisit().start(context);
					}
					else
					{
						//visit is started already, allow user to change visit type
						selectVisitType(context, step, false);		
					}

					cancelled = true;
					break;
				}
				
				startStep(context, VISIT_STEP_MENU, false, null);
				break;
				
			case VISIT_STEP_GPS_CHECK: 
				final ProgressDialog progressDialog = displayProgressDialog(context, context.getResources().getString(R.string.receive_data_from_GPS));
				
				//ComponentName comp = new ComponentName(context.getPackageName(),  GPSLoggingService.class.getName());				
				mGPSServiceConnection = new ServiceConnection() 
				{
					@Override	public void onServiceConnected(ComponentName name, IBinder service) 
					{
						final GPSLoggingService mGPSService = ((GPSLoggingService.GPSLoggingBinder)service).getService();
						mGPSService.unRegisterStatusListener();
						mGPSService.stopLocationUpdates();
						mGPSService.registerStatusListener( 
								new GPSLoggingService.IGPSStatusListener() 
								{
									public void onFinishLocationTracking(boolean bSuccess)
									{
										final boolean gpsSuccess = bSuccess; 
										((Activity)context).runOnUiThread(
												new Runnable() 
												{
													public void run() 
													{														
														mGPSService.unRegisterStatusListener();
														dismissProgressDialog(progressDialog);
														context.unbindService(mGPSServiceConnection);
				
														String strMessage = gpsSuccess?context.getResources().getString(R.string.GPS_success)
																						:context.getResources().getString(R.string.GPS_fail);
														
														if(gpsSuccess) //mark the step as fulfilled
															step.fulfilled = true;		
																
														MessageBoxButton[] buttons = new MessageBoxButton[] 
		                                          		{ 
															new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
		                                          			{
		                                          				@Override
		                                          				public void onClick(DialogInterface dialog, int which)
		                                          				{
		                                          					startStep(context, VISIT_STEP_MENU, false, null);					                                          					
		                                          				}
		                                          			})
		                                          		};		
														MessageBox.show(context, "", strMessage, buttons);
													}
												});
									}
								}
						);
						
						mGPSService.startLocationUpdates();
					}
					@Override	public void onServiceDisconnected(ComponentName name) { }
				};
				
				AntContext.getInstance().getVisit().increaseConnectAttempts();
	            context.bindService(new Intent(context, GPSLoggingService.class), mGPSServiceConnection, Context.BIND_AUTO_CREATE);

	            int gpsAttempts = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_GPS_ATTEMPTS, 3);
	            if(AntContext.getInstance().getVisit().getConnectAttempts() < gpsAttempts)
				{
					//haven't tried yet several times; so do not treat this step as fulfilled
					cancelled = true;
				}
	            
	            break;
			case VISIT_STEP_ONE_PAGE:
				if(checkVisitStarted(context) == false)
				{
					cancelled = true; 
					break;
				}
				
				logVisitStep(stepId);

				{
					Bundle bundle = new Bundle();
		    		bundle.putBoolean(Document.PARAM_NAME_FROM_VISIT, true);
		    		Intent onePageIntent = new Intent(context, MessageProactiveForm.class);
		    		onePageIntent.putExtras(bundle);				
					context.startActivity(onePageIntent);
				}
				FinishPrevActivity(context);
				break;
			case VISIT_STEP_PLANS:
			{
				if(checkVisitStarted(context) == false)
				{
					cancelled = true; 
					break;
				}
				
				logVisitStep(stepId);
				
				{
		    		Bundle bundle = new Bundle();
		    		bundle.putBoolean(Document.PARAM_NAME_FROM_VISIT, true);
		    		
					Intent plansIntent = new Intent(context, PlansForm.class);
					plansIntent.putExtras(bundle);
	
					context.startActivity(plansIntent);
				}
				FinishPrevActivity(context);			
				break;
			}
			case VISIT_STEP_AIMS:
				if(checkVisitStarted(context) == false)
				{
					cancelled = true; 
					break;
				}
				
				logVisitStep(stepId);
				
				{
					Bundle bundle = new Bundle();
		    		bundle.putBoolean(Document.PARAM_NAME_FROM_VISIT, true);
		    		bundle.putBoolean(Document.PARAM_NAME_VISIT_SUMMARIES, true);
		    		bundle.putBoolean(Document.PARAM_NAME_VISIT_TASKS, true);
					
					Intent reportDaySummaries = new Intent(context, ReportDaySummariesForm.class);
					reportDaySummaries.putExtras(bundle);				
					context.startActivity(reportDaySummaries);
				}
				
				FinishPrevActivity(context);	
				break;
				
			case VISIT_STEP_DEBTS:
				if(checkVisitStarted(context) == false)
				{
					cancelled = true; 
					break;
				}				
				
				logVisitStep(stepId);
				
	    		Bundle bundleDebts = new Bundle();
	    		bundleDebts.putBoolean(DocListForm.PARAM_NAME_IS_DEBT_FORM, true);
	    		
				Intent debtsIntent = new Intent(context, DocListForm.class);
				debtsIntent.putExtras(bundleDebts);			
				
				context.startActivity(debtsIntent);
				FinishPrevActivity(context);
				break;
				
			case VISIT_STEP_DEBT_NOTIFICATION:
				{
					if(checkVisitStarted(context) == false)
					{
						cancelled = true; 
						break;
					}
					
					logVisitStep(stepId);
					
					boolean haveDoc = (params!=null && params.containsKey(Document.PARAM_NAME_DOCID));
					boolean createNew = (params!=null && params.containsKey(Document.PARAM_NAME_CREATE_NEW));
					
					if(haveDoc || createNew) //start new document
					{
						finishCachedActivities();
						
						Intent intent = new Intent(context, DocDebtNotificationForm.class);
						if(params!=null)
							intent.putExtras(params);			
						context.startActivity(intent);
						FinishPrevActivity(context);
					}
					else	//just return to previous document
					{					
						boolean found = false;
					
						//check if we have payment form cached
						for(int i=0; i<cachedActivities.size(); i++)
						{
							Activity activity = cachedActivities.get(i);
							if(activity instanceof DocDebtNotificationForm)
							{
								found = true;
								break;
							}
						}
	
						if(found)
							FinishPrevActivity(context);		//cached activity exists, finish current activity and payment window will popup to foreground 
						else
						{						
							stepId = VISIT_STEP_MENU;
							startStep(context, stepId, false, null); //no cached activity, start visit step
						}
					}
				}
				break;				
				
			case VISIT_STEP_DOC_PAYMENT:
			{
				//payment
				if(checkVisitStarted(context) == false)
				{
					cancelled = true; 
					break;
				}
				
				logVisitStep(stepId);
				
				boolean haveDoc = (params!=null && params.containsKey(Document.PARAM_NAME_DOCID));
				boolean haveBaseDoc = (params!=null && params.containsKey(Document.PARAM_NAME_BASEDOCID));
				
				if(fromTabController)
				{
					if(haveDoc || haveBaseDoc) //start new document
					{
						finishCachedActivities();
						
						Intent intent = new Intent(context, DocPaymentForm.class);
						if(params!=null)
							intent.putExtras(params);			
						context.startActivity(intent);
						FinishPrevActivity(context);
					}
					else	//just return to previous document
					{					
						boolean found = false;
					
						//check if we have payment form cached
						for(int i=0; i<cachedActivities.size(); i++)
						{
							Activity activity = cachedActivities.get(i);
							if(activity instanceof DocPaymentForm)
							{
								found = true;
								break;
							}
						}

						if(found)
							FinishPrevActivity(context);		//cached activity exists, finish current activity and payment window will popup to foreground 
						else
						{						
							stepId = VISIT_STEP_MENU;
							startStep(context, stepId, false, null); //no cached activity, start visit step
						}
					}
				}
				/*else
				{
					AntContext.getInstance().getTabController().startTab(context, TabControllerVisit.TAB_DOC_LIST, 0, null);
				}*/
				break;
			}	
			case VISIT_STEP_DPSM:
				//dpsm
				if(checkVisitStarted(context) == false)
				{
					cancelled = true; 
					break;
				}
				
				logVisitStep(stepId);
				
				startStep(context, VISIT_STEP_MENU, false, null);
				break;

			case VISIT_STEP_REMNANTS:	
			case VISIT_STEP_DOC_ORDER:
			case VISIT_STEP_DOC_SALE:
			case VISIT_STEP_OPEN_DOC_SALE_FAMILY:				
			{	
				if(stepId!=VISIT_STEP_OPEN_DOC_SALE_FAMILY)
				{
					if(checkVisitStarted(context) == false)
					{
						cancelled = true; 
						break;
					}
				}
				
				logVisitStep(stepId);
				
				boolean haveDoc = (params!=null && params.containsKey(Document.PARAM_NAME_DOCID));
				
				if(fromTabController && !haveDoc )
				{
					//just switched tab, try to bring back cached docSale form
					FinishPrevActivity(context);
				}
				else
				{
					finishCachedActivities();

					//start new docSale with parameters						
					Intent intent = new Intent(context, DocSaleForm.class);
					if(params!=null)
						intent.putExtras(params);

					if(stepId == VISIT_STEP_DOC_ORDER || stepId == VISIT_STEP_DOC_SALE || stepId == VISIT_STEP_REMNANTS )
					{
						char docType = Document.DOC_TYPE_CLAIM;

						if(stepId == VISIT_STEP_DOC_SALE)
							docType = Document.DOC_TYPE_SALE;
						else if(stepId == VISIT_STEP_REMNANTS)
							docType = Document.DOC_TYPE_REMNANTS;
					
						intent.putExtra("docType", docType);						
					}

					context.startActivity(intent);			
					FinishPrevActivity(context);			
				}				
				
				break;
			}	
			case VISIT_STEP_VISIT_SUMMARIES:
			{
				//aims				
				if(checkVisitStarted(context) == false)
				{
					cancelled = true; 
					break;
				}
				
				logVisitStep(stepId);
				
	    		Bundle bundle = new Bundle();
	    		bundle.putBoolean(Document.PARAM_NAME_FROM_VISIT, true);
	    		bundle.putBoolean(Document.PARAM_NAME_VISIT_SUMMARIES, true);	    		
				
				Intent reportDaySummaries = new Intent(context, ReportDaySummariesForm.class);
				reportDaySummaries.putExtras(bundle);				
				context.startActivity(reportDaySummaries);
				
				FinishPrevActivity(context);				
				break;
			}	
			case VISIT_STEP_DAY_SUMMARIES:
			{
				//aims				
				if(checkVisitStarted(context) == false)
				{
					cancelled = true; 
					break;
				}
				
				logVisitStep(stepId);
				
	    		Bundle bundle = new Bundle();
	    		bundle.putBoolean(Document.PARAM_NAME_FROM_VISIT, true);
	    		
				Intent reportDaySummaries = new Intent(context, ReportDaySummariesForm.class);
				reportDaySummaries.putExtras(bundle);				
				context.startActivity(reportDaySummaries);
				FinishPrevActivity(context);				
				break;
			}	
				
			case VISIT_STEP_END_VISIT:
				if(checkVisitStarted(context) == false)
				{	
					cancelled = true; 
					break;
				}
				
				/*if(Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_CHECK_STORECHECK_PG_ON_VISIT_END, 0) > 0)
				{
					if(checkStepQuestionnairePG())
					{
						String title = context.getResources().getString(R.string.questionnaires_fail);
						String message = context.getResources().getString(R.string.visit_storecheck_pg_warning);
						MessageBox.show(context, title, message);
						
						cancelled = true;
						break;
					}
				}*/
				
				int checkEfficiency = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_CHECK_VISIT_EFFICIENCY, 0);				
				if(checkEfficiency > 0 && checkIsVisitEffective(context, step) == false)
				{
					cancelled = true;
					break;
				}

				AntContext.getInstance().getVisit().finish(context, -1);
				AntContext.getInstance().endVisit();
				//context.startActivity(new Intent(context, VisitForm.class));
				FinishPrevActivity(context);

				break;
		}

		if(!cancelled)
		{
			step.fulfilled = true;
			return super.startStep(context, stepId, fromTabController, params);
		}
		
		return false; //cancelled
	}
		
    //--------------------------------------------------------------	
	@Override public boolean checkCanFinish(final Context context, int stepId)
	{
		if(context instanceof VisitForm)
		{
			Visit visit = AntContext.getInstance().getVisit(); 
			if(visit.isStarted() && !visit.isFinished())
			{
				MessageBoxButton[] buttons = new MessageBoxButton[]
                {
  					new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, context.getResources().getString(R.string.visit_start_visit_yes),
						new DialogInterface.OnClickListener()
						{
							@Override public void onClick(DialogInterface dialog, int which) 
							{
								startStep(context, VISIT_STEP_END_VISIT, false, null);
							}
						}),
					new MessageBoxButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(R.string.visit_start_visit_cancel),
							new DialogInterface.OnClickListener()
							{
								@Override public void onClick(DialogInterface dialog, int which) 
								{
									
								}
							})
						
                };
					                                       
	    		MessageBox.show(context, context.getResources().getString(R.string.visit_warning),	
	    							context.getResources().getString(R.string.visit_end_visit_warning), buttons);
	    		
	    		return false;
			}
		}
		return true;
	}
	
	//----------------------------------------------------------------
	@Override public boolean checkNeedTheStep(int stepId)
	{
		if(stepId == VISIT_STEP_DEBTS)
		{
			//check if we have unpaid documents
	    	String docStateFilter = Q.getDocStateFilter();
	    	long addrID = AntContext.getInstance().getAddrID();
	    	String where = String.format(" WHERE d.ClientID=%d %s ", AntContext.getInstance().getClientID(), addrID != 0 ? String.format(" AND d.AddrID=%d ", addrID) : "" );   	    	
    		where = where + String.format(" AND (d.DocType = '%s' OR d.DocType = '%s') AND (round(d.SumAll - d.SumLinked,2) > 0) AND %s ",
    										Document.DOC_TYPE_CLAIM, Document.DOC_TYPE_SALE, Q.getRespiteFilter());    	
	    	
	    	String where1 = String.format(" %s AND %s", where, docStateFilter);
	    	
	    	String sql = String.format(" SELECT count(d.DocID) FROM Documents d %s ", where1);
	    	
	    	long count = Db.getInstance().getDataLongValue(sql, 0);
	    	
	    	if(count==0)
	    		return false;
		}
		
		return true;
	}
	
}