package com.app.ant.app.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import com.app.ant.R;
import com.app.ant.app.Activities.AddressAttributesDialog.AttributeType;
import com.app.ant.app.Activities.AddressAttributesDialog.Attributes;
import com.app.ant.app.Activities.ExpandableAdapterForArray.IChildItem;
import com.app.ant.app.Activities.ExpandableAdapterForArray.IGroupItem;
import com.app.ant.app.Activities.HierarchicalListAdapter.TreeItem;
import com.app.ant.app.Activities.MessageBox.MessageBoxButton;
import com.app.ant.app.Activities.RouteItemAddDialog.AddressAndDateSelectListener;
import com.app.ant.app.Activities.SelectVisitTypeDialog.VisitTypeSelectListener;
import com.app.ant.app.BusinessLayer.Address;
import com.app.ant.app.BusinessLayer.Client;
import com.app.ant.app.BusinessLayer.Plans;
import com.app.ant.app.BusinessLayer.Plans.PlanItem;
import com.app.ant.app.BusinessLayer.Route;
import com.app.ant.app.Controls.ChartView;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Settings;

import java.util.*;

/** Форма списка клиентов/адресов. Содержит иерархический список адресов, при выборе адреса начинает визит
 * по данному адресу. Возможна фильтрация адресов по долгу и маршруту, а также быстрый переход по
 * алфавитному указателю.
 */
public class ClientListForm1 extends AntActivity implements ListView.OnScrollListener
{
	/**	Интерфейс для обратного вызова - возвращает выбранный адрес*/
    public interface AddressSelectListener
    {
        abstract void onAddressSelected(long clientId, long addrId, int visitTypeID);
    }

    public interface ChartDetailsListener
    {
        abstract void onChartSelected(long addrId, List<PlanItem> progressValues);
    }


	static public class ClientListData
	{
		public ArrayList<IGroupItem> clients;
		public Map<Long, ArrayList<IChildItem> > addressMap;

		public int totalAddressCount = 0;
		public int totalVisitedAddressCount = 0;
	}

	private static final int IDD_DATE_DIALOG = 0;
	private static final int IDD_ROUTE_ITEM_ADD_DIALOG = 1;
	private static final int IDD_FILTERS_DIALOG = 2;
	private static final int IDD_QUALIFICATION_PROGRESS_DIALOG = 3;

	private ListViewOverlayHelper mOverlayHelper = null;

	private ExpandableListView mClientAddrExpandableList;
	private ToggleButton mCheckRoute;
	//private ToggleButton mCheckDebt;
	private ViewGroup mAlphabetButtonsPanel;
	//private ImageButton mButtonSearch;
	private ImageButton buttonFilters;
	private ImageButton mButtonAddRouteItem;
	private TextView mTextTotalAddressCount;
	private TextView mTextVisitedAddressCount;
	private TextView mTextRouteDate;
	private ViewGroup mRouteDatePanel;

	private boolean mShowAlphabetButtonsPanel = false;

	private Calendar routeDate;

	private long selectedAddrId = -1;
	private List<PlanItem> selectedProgressValues;

	private ClientListData clientListData = new ClientListData();
	AddressAttributesDialog filtersDialog;
	AddressAttributesDialog.Attributes attributeFilters = new AddressAttributesDialog.Attributes();

    //--------------------------------------------------------------
	/** Класс, содержащий информацию о клиенте. Содержит методы отображения своего содержимого на форме*/
    public static class ListItemClient implements IGroupItem
    {
    	private Long id;
        private String name;
        ClientListData clientListData;

        /** Конструктор
         *
         * @param id идентификатор клиента
         * @param name название клиента
         */
        public ListItemClient(Long id, String name, ClientListData clientListData)
        {
            this.name = name;
            this.id = id;
            this.clientListData = clientListData;
        }

        /** Возвращает ID клиента*/
        public Long getID() { return id; }

        public boolean isEnabled() { return true; }

        /** Выводит содержимое класса на View c соответствующим viewId
        * @param view экранный элемент для отображения
        * @param viewId идентификатор экранного элемента
        * @param isExpanded флаг - раскрыт элемент или нет (актуален для верхнего уровня в древовидном списке)
        */

    	public void fillView(View view, int viewId, boolean isExpanded)
    	{
    		ArrayList<IChildItem> addresses = clientListData.addressMap.get(id);

    		view.setTag(this);

    		if(viewId == R.id.text_client && view instanceof TextView)
    		{
    			((TextView) view).setText(name);

	        	//determine if this client has visited addresses
	        	boolean haveVisitsToday = false;
	        	for(int i=0; i<addresses.size(); i++)
	        	{
	        		ListItemAddress addr = (ListItemAddress)addresses.get(i);
	        		if(addr.haveVisitsToday)
	        		{
	        			haveVisitsToday = true;
	        			break;
	        		}
	        	}
            	((TextView)view).setTextColor(haveVisitsToday?0xFF0000FF:0xFF000000);
    		}
    		else if(viewId == R.id.imgGold)
    		{
    			boolean haveGold = false;
	        	for(int i=0; i<addresses.size(); i++)
	        	{
	        		ListItemAddress addr = (ListItemAddress)addresses.get(i);
	        		if(addr.isGold)
	        		{
	        			haveGold = true;
	        			break;
	        		}
	        	}
	        	view.setVisibility(haveGold?View.VISIBLE:View.GONE);
    		}
    		else if(viewId == R.id.imgSilver)
    		{
    			boolean haveSilver = false;
	        	for(int i=0; i<addresses.size(); i++)
	        	{
	        		ListItemAddress addr = (ListItemAddress)addresses.get(i);
	        		if(addr.isSilver)
	        		{
	        			haveSilver = true;
	        			break;
	        		}
	        	}
	        	view.setVisibility(haveSilver?View.VISIBLE:View.GONE);
    		}
    	}
    }

    //--------------------------------------------------------------
    /** Класс, содержащий информацию об адресе. Содержит методы отображения своего содержимого на форме*/
    public static class ListItemAddress implements IChildItem
    {
    	private Long id;
    	private Long clientID;
    	private String name;
    	private boolean haveVisitsToday;
    	private boolean isGold;
    	private boolean isSilver;
    	private boolean isDeliveryPoint;
    	private boolean isSalePoint;
    	private boolean isOffice;
    	private boolean isOrderPoint;
    	private char state;
    	private int visitTypeID;
    	private List<PlanItem> progressValues;

    	private AddressSelectListener addressSelectListener;
    	private AddressSelectListener addressDeleteListener;
    	private AddressSelectListener addressMoveListener;

    	ChartDetailsListener chartDetailsListener;

    	/** конструктор
    	 *
    	 * @param id идентификатор адреса
    	 * @param clientID идентификатор клиента, к которому относится адрес
    	 * @param name название адресса
    	 * @param haveVisitsToday наличие визитов по адресу за сегодняшний день
    	 * @param isGold является ли точка "золотой"
    	 * @param isSilver является ли точка "серебряной"
    	 */
        public ListItemAddress(Long id, Long clientID, String name, boolean haveVisitsToday,
        							boolean isGold, boolean isSilver,
        							boolean isDeliveryPoint, boolean isSalePoint, boolean isOffice, boolean isOrderPoint,
        							AddressSelectListener addressSelectListener, AddressSelectListener addressDeleteListener,
        							AddressSelectListener addressMoveListener, char state, int visitTypeID,
        							List<PlanItem> progressValues, ChartDetailsListener chartDetailsListener)
        {
            this.name = name;
            this.clientID = clientID;
            this.id = id;
            this.haveVisitsToday = haveVisitsToday;
            this.isGold = isGold;
            this.isSilver = isSilver;
            this.isDeliveryPoint = isDeliveryPoint;
            this.isSalePoint = isSalePoint;
            this.isOffice = isOffice;
            this.isOrderPoint = isOrderPoint;
            this.state = state;
            this.visitTypeID = visitTypeID;
            this.progressValues = progressValues;

            this.addressSelectListener = addressSelectListener;
            this.addressDeleteListener = addressDeleteListener;
            this.addressMoveListener = addressMoveListener;

            this.chartDetailsListener = chartDetailsListener;
        }

        /** возвращает ID адреса*/
        public Long getID() { return id; }

        public boolean isEnabled() { return true; }

        /** Выводит содержимое класса на экранном элементе c соответствующим viewId
         * @param view экранный элемент для отображения
         * @param viewId идентификатор экранного элемента
         * @param isExpanded флаг - раскрыт элемент или нет (актуален для верхнего уровня в древовидном списке)
         */
    	public void fillView(View view, int viewId, boolean isExpanded)
    	{
    		if(viewId == R.id.text_address && view instanceof TextView)
    		{
    			((TextView) view).setText(name);

                if(haveVisitsToday)
                	((TextView) view).setTextColor(0xFF0000FF);
                else
                	((TextView) view).setTextColor(0xFF000000);
    		}
    		else if(viewId == R.id.imgGold)
    		{
    			view.setVisibility(isGold?View.VISIBLE:View.GONE);
    		}
    		else if(viewId == R.id.imgSilver)
    		{
    			view.setVisibility(isSilver?View.VISIBLE:View.GONE);
    		}
    		else if(viewId == R.id.imgAddress)
    		{
    			ImageView imgAddress = (ImageView) view;

    			if(isOffice)
    				imgAddress.setImageResource(isDeliveryPoint?R.drawable.office_car:R.drawable.office);
    			else
    				imgAddress.setImageResource(isDeliveryPoint?R.drawable.address_icon_car:R.drawable.address_icon);
    		}
    		else if(viewId == R.id.imgSalePoint)
    		{
    			view.setVisibility(isSalePoint? View.VISIBLE : View.INVISIBLE);
    		}
			else if(viewId == R.id.imgOrderPoint)
			{
				view.setVisibility(isOrderPoint? View.VISIBLE : View.INVISIBLE);
			}
			else if(viewId == R.id.imgDeleteItem)
			{
				view.setVisibility(state==Q.RECORD_STATE_ACTIVE? View.VISIBLE : View.GONE);
			}
    		else if(viewId == R.id.chartProgress)
    		{
    			if(isGold)
    			{
	    			float[] floatValues = Plans.getPlanPercents(progressValues);
	    			((ChartView)view).setValues(floatValues, 100.f);
    			}
    		}
    		else if(viewId == R.id.chartProgressBkg)
    		{
    			view.setVisibility(isGold?View.VISIBLE:View.GONE);
    		}

			view.setTag(this);

			if(viewId == R.id.imgDeleteItem)
			{
				view.setOnClickListener(
			            new View.OnClickListener()
			    		{
			    			@Override public void onClick(View v)
			    			{
			    		    	try
			    		    	{
		    		    			ListItemAddress addr = (ListItemAddress)v.getTag();
		    		    			if(addressDeleteListener!=null)
		    		    				addressDeleteListener.onAddressSelected(addr.clientID, addr.id, addr.visitTypeID);
			    		    	}
			    		    	catch(Exception ex)
			    		    	{
			    		    		ErrorHandler.CatchError("Exception in ClientListForm1.onClick", ex);
			    		    	}
			    			}
			    		});

			}
			else if(viewId == R.id.chartProgress || viewId == R.id.chartProgressBkg)
			{
				if(isGold)
				{
					view.setOnClickListener(
				            new View.OnClickListener()
				    		{
				    			@Override public void onClick(View v)
				    			{
				    		    	try
				    		    	{
			    		    			ListItemAddress addr = (ListItemAddress)v.getTag();
			    		    			if(chartDetailsListener!=null)
			    		    				chartDetailsListener.onChartSelected(addr.id, progressValues);
				    		    	}
				    		    	catch(Exception ex)
				    		    	{
				    		    		ErrorHandler.CatchError("Exception in ClientListForm1.onClick", ex);
				    		    	}
				    			}
				    		});
				}
			}
			else
			{
	            view.setOnClickListener(
			            new View.OnClickListener()
			    		{
			    			@Override public void onClick(View v)
			    			{
			    		    	try
			    		    	{
		    		    			ListItemAddress addr = (ListItemAddress)v.getTag();
		    		    			if(addressSelectListener!=null)
		    		    				addressSelectListener.onAddressSelected(addr.clientID, addr.id, addr.visitTypeID);
		    		    			//startVisit(addr.clientID, addr.id);
			    		    	}
			    		    	catch(Exception ex)
			    		    	{
			    		    		ErrorHandler.CatchError("Exception in ClientListForm1.onClick", ex);
			    		    	}
			    			}
			    		});

	            if(addressMoveListener!=null)
	            {
		            view.setOnLongClickListener(
				            new View.OnLongClickListener()
				    		{
				    			@Override public boolean onLongClick(View v)
				    			{
				    		    	try
				    		    	{
			    		    			ListItemAddress addr = (ListItemAddress)v.getTag();

			    		    			if(addressMoveListener!=null)
			    		    				addressMoveListener.onAddressSelected(addr.clientID, addr.id, addr.visitTypeID);
				    		    	}
				    		    	catch(Exception ex)
				    		    	{
				    		    		ErrorHandler.CatchError("Exception in ClientListForm1.onLongClick", ex);
				    		    	}

				    		    	return true;
				    			}
				    		});
	            }
			}
    	}
    }

    //--------------------------------------------------------------
    /** Инициализация формы	 */
    @Override public void onCreate(Bundle savedInstanceState)
    {
    	try
    	{
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        overridePendingTransition(0,0);
	        setContentView(R.layout.client_list);

	        mClientAddrExpandableList = ((ExpandableListView) findViewById(R.id.clientAddrExpandableList));
	    	mCheckRoute = ((ToggleButton) findViewById(R.id.chkBoxRoute));
	    	//mCheckDebt  = ((ToggleButton) findViewById(R.id.chkBoxDebt));
	    	mAlphabetButtonsPanel = (ViewGroup) findViewById(R.id.buttonFilterPlacement);
	    	//mButtonSearch = (ImageButton) findViewById(R.id.buttonSearch);
	    	mButtonAddRouteItem = (ImageButton) findViewById(R.id.buttonAddRouteItem);
	    	mTextTotalAddressCount = (TextView) findViewById(R.id.textTotalCount);
	    	mTextVisitedAddressCount = (TextView) findViewById(R.id.textVisitedCount);
	    	mTextRouteDate = (TextView) findViewById(R.id.textRouteDate);
	    	mRouteDatePanel = (ViewGroup) findViewById(R.id.datePanel);

	    	routeDate = Calendar.getInstance();

	    	//
	    	// Обработка добавления точки на маршрут
	    	//
	    	mButtonAddRouteItem.setOnClickListener(new ImageButton.OnClickListener()
	    	{
	        	@Override public void onClick(View v)
	        	{
	        		showDialog(IDD_ROUTE_ITEM_ADD_DIALOG);
	        	}
	        });

	    	buttonFilters = (ImageButton) findViewById(R.id.buttonFilters);
	    	buttonFilters.setOnClickListener(new ImageButton.OnClickListener()
	    	{
	        	@Override public void onClick(View v)
	        	{
	        		showDialog(IDD_FILTERS_DIALOG);
	        	}
	        });
	    	updateFilterIcon();


	    	//
	    	// when search button is pressed, hide/unhide alphabet panel
	    	//

	    	/*
	    	mButtonSearch.setOnClickListener(new ImageButton.OnClickListener()
	    	{
	        	@Override public void onClick(View v)
	        	{
	        		mShowAlphabetButtonsPanel = !mShowAlphabetButtonsPanel;

	        		if(mShowAlphabetButtonsPanel)
	        			mAlphabetButtonsPanel.setVisibility(View.VISIBLE);
	        		else
	        			mAlphabetButtonsPanel.setVisibility(View.GONE);
	        	}
	        });*/

	    	//
	    	// process buttons with letters to jump across list quickly
	    	//
	    	View.OnClickListener buttonFilterListener = new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					if( clientListData.clients==null || mClientAddrExpandableList==null )
						return;

					int letterIndex = (Integer)v.getTag();

					if(letterIndex == -1)
						initAlphabetButtons( null ); //reset all button tags, do not set click listener as it set already

					//advance to next letter when clicked twice
					letterIndex++;
					if( letterIndex >= ((Button) v).getText().length() )
						letterIndex = 0;

					v.setTag(letterIndex);

					Character firstLetter = Character.toUpperCase( ((Button) v).getText().charAt(letterIndex) );

					for(int i=0; i<clientListData.clients.size(); i++)
					{
						Character curFirstLetter = Character.toUpperCase( ((ListItemClient)clientListData.clients.get(i)).name.charAt(0));

						if( curFirstLetter.compareTo(firstLetter) >=0 )
						{
							//mClientAddrExpandableList.smoothScrollToPosition(mClientAddrExpandableList.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(i)));
							//mClientAddrExpandableList.setSelection(i);
							mClientAddrExpandableList.setSelectedGroup(i);

							break;
						}
					}
				}
			};

			initAlphabetButtons( buttonFilterListener);

	    	//init checkbox event listener
			ToggleButton.OnClickListener checkBoxClickListener = new ToggleButton.OnClickListener()
	        {
	        	@Override public void onClick(View v)
	        	{
	        		boolean route = mCheckRoute.isChecked();
	        		mRouteDatePanel.setVisibility(route ? View.VISIBLE: View.INVISIBLE);
	        		mButtonAddRouteItem.setVisibility(route ? View.VISIBLE: View.INVISIBLE);
	        		if(route)
	        		{
	        			routeDate = Calendar.getInstance();
	        			updateDateDisplay();
	        		}

	        		fill();
	        	}
	        };

	        mCheckRoute.setOnClickListener(checkBoxClickListener);
	        //mCheckDebt.setOnClickListener(checkBoxClickListener);

	        //init date click event listener
	        mRouteDatePanel.setOnClickListener(new ViewGroup.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					try
					{
						showDialog(IDD_DATE_DIALOG);
					}
					catch(Exception ex)
					{
						ErrorHandler.CatchError("Exception in headerPanel.onClick", ex);
					}
				}
			});

	        fill();

	        int proposePlans = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_PROPOSE_DAY_PLAN_AT_START, 0);
	        if(proposePlans>0)
	        {
		    	//
		    	//Display alert dialog proposing to display plans form

	    		MessageBoxButton[] buttons = new MessageBoxButton[]
	            {
						new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.dialog_base_yes),
											new DialogInterface.OnClickListener()
											{
												@Override public void onClick(DialogInterface dialog, int which)
												{
													startActivity(new Intent(ClientListForm1.this, ReportDaySummariesForm.class));
												}
											}),
						new MessageBoxButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.dialog_base_cancel),
											new DialogInterface.OnClickListener()
											{
												@Override public void onClick(DialogInterface dialog, int which) { }
											})
	            };

	    		MessageBox.show(this, getResources().getString(R.string.client_list_showPlansHeader),	getResources().getString(R.string.client_list_showPlans), buttons);
	        }
    	}
		catch(Exception ex)
		{
			ErrorHandler.CatchError("Exception in ClientListForm1.onCreate", ex);
		}

    }

    //--------------------------------------------------------------
    /** обновление формы если данные изменились*/
    @Override public void refreshActivity()
    {
    	fill();
    }

    //--------------------------------------------------------------
    private void initAlphabetButtons( View.OnClickListener buttonFilterListener)
    {
		for(int i = 0; i<mAlphabetButtonsPanel.getChildCount();i++)
		{
			View childGroupView =  mAlphabetButtonsPanel.getChildAt(i);

			if( childGroupView instanceof ViewGroup)
			{
				for(int j = 0; j< ((ViewGroup)childGroupView).getChildCount();j++)
				{
					View childView = ((ViewGroup)childGroupView).getChildAt(j);
					if(childView instanceof Button)
					{
						if(buttonFilterListener!=null)
							childView.setOnClickListener( buttonFilterListener );
						childView.setTag(-1);
					}
				}
			}
		}
    }

    //--------------------------------------------------------------
    /** Инициализация элементов формы. Заполняет дерево клиентов/адресов значениями из базы данных.*/
    private void fill()
    {
    	boolean route = mCheckRoute.isChecked();
    	boolean debt  = false; //mCheckDebt.isChecked();

		AddressSelectListener addrSelectListener = new AddressSelectListener()
		{
			public void onAddressSelected(long clientId, long addrId, int visitTypeID)
			{
				startVisit(clientId, addrId, visitTypeID);
			}
		};

		AddressSelectListener addrDeleteListener = new AddressSelectListener()
		{
			public void onAddressSelected(final long clientId, final long addrId, int visitTypeID)
			{
		    	//Display alert dialog proposing to confirm delete route item
				MessageBoxButton[] buttons = new MessageBoxButton[]
		        {
						new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.contact_delete_yes),
									new DialogInterface.OnClickListener()
									{
										@Override public void onClick(DialogInterface dialog, int which)
										{
											Route.deleteRouteItem(addrId, routeDate);

		                    				//refresh form
		                    				refreshActivity();

											//display a toast to user saying that route item is deleted
											Toast.makeText(ClientListForm1.this, getResources().getString(R.string.route_item_delete_notify), Toast.LENGTH_SHORT).show();

										}
									}),
						new MessageBoxButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.contact_delete_no),
									new DialogInterface.OnClickListener()
									{
										@Override public void onClick(DialogInterface dialog, int which) { }
									})
		        };

				MessageBox.show(ClientListForm1.this, getResources().getString(R.string.route_item_delete_confirm_title),
											getResources().getString(R.string.route_item_delete_confirm), buttons);

			}
		};

		AddressSelectListener addrMoveListener = new AddressSelectListener()
		{
			public void onAddressSelected(final long clientId, final long addrId, final int visitTypeID)
			{
				/*boolean route = mCheckRoute.isChecked();
				if(!route)
					return;*/

		    	//Display alert dialog proposing to confirm move route item
				MessageBoxButton[] buttons = new MessageBoxButton[]
		        {
						new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.contact_delete_yes),
									new DialogInterface.OnClickListener()
									{
										@Override public void onClick(DialogInterface dialog, int which)
										{
											//display dialog asking date move to
											Calendar date = Calendar.getInstance();
											Dialog dlg = new DatePickerDialog(ClientListForm1.this,
									        		new DatePickerDialog.OnDateSetListener()
									        		{
									                    public void onDateSet(DatePicker view, int year, int monthOfYear,
									                            int dayOfMonth)
									                    {
									                    	Calendar selectedDate = Calendar.getInstance();
									                    	selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
									                    	selectedDate.set(Calendar.MONTH, monthOfYear);
									                    	selectedDate.set(Calendar.YEAR, year);

									                    	//copy route item to selected date
															Route.addRouteItem(addrId, selectedDate, visitTypeID);

															//display a toast to user saying that route item is copied
															String message = String.format( getResources().getString(R.string.route_item_move_notify), Convert.dateToString(selectedDate));
															Toast.makeText(ClientListForm1.this, message, Toast.LENGTH_SHORT).show();
									                    }
									        		},
									        		date.get(Calendar.YEAR),
									        		date.get(Calendar.MONTH),
									        		date.get(Calendar.DAY_OF_MONTH));

											dlg.show();
										}
									}),
						new MessageBoxButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.contact_delete_no),
									new DialogInterface.OnClickListener()
									{
										@Override public void onClick(DialogInterface dialog, int which) { }
									})
		        };

				MessageBox.show(ClientListForm1.this, getResources().getString(R.string.route_item_move_confirm_title),
											getResources().getString(R.string.route_item_move_confirm), buttons);

			}
		};

		ChartDetailsListener chartDetailsListener = new ChartDetailsListener()
		{
			public void onChartSelected(long addrId, List<PlanItem> progressValues)
			{
				selectedAddrId = addrId;
				selectedProgressValues = progressValues;
				showDialog(IDD_QUALIFICATION_PROGRESS_DIALOG);
			}
		};

    	fillClientAddrList(this, attributeFilters, route, routeDate, clientListData, mClientAddrExpandableList, addrSelectListener, addrDeleteListener, addrMoveListener, chartDetailsListener, false);

    	//mButtonSearch.setEnabled(true);
		mAlphabetButtonsPanel.setVisibility(mShowAlphabetButtonsPanel ? View.VISIBLE : View.GONE);

    	mOverlayHelper = new ListViewOverlayHelper(this);
    	mClientAddrExpandableList.setOnScrollListener(this);

    	mTextTotalAddressCount.setText(String.format("%d",clientListData.totalAddressCount));
    	mTextVisitedAddressCount.setText(String.format("%d",clientListData.totalVisitedAddressCount));
    }

    //--------------------------------------------------------------
    public static void fillClientAddrList( Context context, AddressAttributesDialog.Attributes attributeFilters,
    										boolean displayRoute, Calendar routeDate,
    										ClientListData clientListData, ExpandableListView mClientAddrExpandableList,
    										AddressSelectListener addrSelectListener,
    										AddressSelectListener addrDeleteListener,
    										AddressSelectListener addrMoveListener,
    										ChartDetailsListener chartDetailsListener,
    										boolean excludeRoute)
    {
        //Log.d("ClientList ", "Start fillClientAddrList");

    	boolean debtorsOnly = false;
    	if( attributeFilters!=null && attributeFilters.additionalFilters.contains(AttributeType.additionalFilterDebt))
    		debtorsOnly = true;

		//
		// take into account filters applied on address attributes
		//
		String filterByAttrAddr = "";
		String filterByAttrClient = "";

		//аттрибуты сгруппированы в иерархию
		//внутри групп фильтры должны объединяться по "или"
		//между группами - по "и"
		if( attributeFilters!=null && !attributeFilters.addressAttributeIds.isEmpty())
		{
			//get top-level items
			ArrayList<TreeItem> items = attributeFilters.addressAttributeIds.getItems(0, false);

			//iterate through top-level items
			for(int i=0; i<items.size(); i++)
			{
				TreeItem item = items.get(i);

				//in case if item have children, add entry to sql filter
				if(item.getChildCount()>0)
				{
					//create comma-separated string of ids of children
					String filterAttrIds = "";

					Iterator<TreeItem> it = item.children.iterator();
			        while (it.hasNext())
			        {
			        	TreeItem child = it.next();

			        	if(filterAttrIds.length() == 0)
			        		filterAttrIds += child.id;
			        	else
			        		filterAttrIds = filterAttrIds + "," + child.id;
			    	}

			        filterByAttrAddr +=
			        			" AND EXISTS ( SELECT aaFilter.AddrID \n" +
								"	FROM AddressAttributes aaFilter \n" +
								"   WHERE aaFilter.AddrID = a.AddrID AND aaFilter.AttrID in ( " + filterAttrIds + ") ) \n";

			        filterByAttrClient +=
			        			" AND EXISTS ( SELECT aaFilter.AddrID \n" +
								"	FROM AddressAttributes aaFilter \n" +
								"   INNER JOIN Addresses a ON a.ClientID = c.ClientID \n" +
								"   WHERE aaFilter.AddrID = a.AddrID AND aaFilter.AttrID in ( " + filterAttrIds + ") ) \n";
				}
			}
		}


		//
		//create array of clients
		//

		//Log.d("ClientList ", "Start loading clients");
		clientListData.clients = new ArrayList<IGroupItem>();
		clientListData.addressMap = new HashMap< Long, ArrayList<IChildItem> > ();

		String saldoSelect = "";
		String saldoFilter = "";
		if(debtorsOnly)
		{
			long defaultDirectionID = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_DEFAULT_DIRECTION_ID, 0);
			saldoSelect = String.format(", coalesce((select round(max(Saldo),2) from ClientSaldo where ClientID = c.ClientID and SaldoTypeID=2 and DirectionID=%d ), 0) AS Saldo ", defaultDirectionID);
			saldoFilter = " AND Saldo > 0 ";
		}

		String routeFilterClients = (displayRoute|excludeRoute) ? Q.getRouteFilter(routeDate, excludeRoute): "";

		String sql = " SELECT c.ClientID, c.NameScreen " + saldoSelect +
					 " FROM Clients c " +
					 " WHERE 1 = 1 " + saldoFilter + routeFilterClients + filterByAttrClient +
					 " GROUP BY c.ClientID " +
					 " ORDER BY c.NameScreen";

		Cursor clientsCursor = Db.getInstance().selectSQL(sql);

		//Log.d("ClientList ", "Start processing clients");

		if (clientsCursor != null && clientsCursor.getCount() > 0)
		{
			int clientIDColumnIdx = clientsCursor.getColumnIndex("ClientID");
			int nameScreenColumnIdx = clientsCursor.getColumnIndex("NameScreen");

			for(int i = 0; i < clientsCursor.getCount(); i++)
			{
				clientsCursor.moveToPosition(i);
				long clientID = clientsCursor.getInt(clientIDColumnIdx);

				clientListData.clients.add(new ListItemClient(clientID, clientsCursor.getString(nameScreenColumnIdx), clientListData));

				ArrayList<IChildItem> addresses = new ArrayList<IChildItem>();
				clientListData.addressMap.put(clientID, addresses);
			}
		}

		if (clientsCursor != null)
			clientsCursor.close();

		//
		//fill addresses array
		//
		//Log.d("ClientList ", "Start loading addresses");

    	//
    	//needed to mark addresses as gold or silver
    	//
    	String goldMagAttributes = "(" + Address.ATTR_GOLD_MAG + "," + Address.ATTR_GOLD_M_PRETENDER + "," + Address.ATTR_GOLD_M_PRETENDER_ND + ")"; //TODO move to settings
    	String goldCheckoutAttributes = "(" + Address.ATTR_GOLD_CHECK + "," + Address.ATTR_GOLD_CH_PRETENDER + "," + Address.ATTR_GOLD_CH_PRETENDER_ND + ")"; //TODO move to settings

		String where = " WHERE 1 = 1 ";
		String joinRoute = (displayRoute | excludeRoute) ? Q.getRouteJoin(routeDate, excludeRoute) : "";
		String selectRoute = displayRoute ? ",max(r.State) AS State, max(r.VisitTypeID) AS VisitTypeID ":"";
		if(excludeRoute)
			where += " AND r.AddrID IS NULL ";

		String attributeExists = " (case when exists (select * from AddressAttributes aa1 where aa1.AddrID = a.AddrID AND aa1.AttrID %s %s) then 1 else 0 end)";

		String channel = " (SELECT max(c.ChannelID) as ChannelID " +
				   		 " FROM AddrChannels ac " +
			   		 	 " 		INNER JOIN Channels c ON ac.ChannelID = c.ChannelID" +
			   		 	 " WHERE ac.AddrID = a.AddrID AND c.ChannelTypeID = 1) as ChannelID";

		String sqlAddr = " SELECT DISTINCT a.AddrID, a.ClientID, a.AddrName, max(v.VisitStartDate) as VisitStartDate, " +
								String.format(attributeExists, "=", Address.ADDR_TYPE_DELIVERY_POINT) + " AS IsDeliveryPoint, " +
								String.format(attributeExists, "=", Address.ADDR_TYPE_SALE_POINT) + " AS IsSalePoint, " +
								String.format(attributeExists, "=", Address.ADDR_TYPE_OFFICE) + " AS IsOffice, " +
								String.format(attributeExists, "=", Address.ADDR_TYPE_ORDER_POINT) + " AS IsOrderPoint, " +
								String.format(attributeExists, "in", goldMagAttributes) + " AS gold, " +
								String.format(attributeExists, "in", goldCheckoutAttributes) + " AS silver, " +
								channel +
								selectRoute +
						" FROM Addresses a " +
								" LEFT JOIN Visits v ON a.AddrID = v.AddrID " +
								joinRoute +
								where + filterByAttrAddr +
						" GROUP BY a.AddrID, a.ClientID, a.AddrName " +
						" ORDER BY a.ClientID, a.AddrName";

		Cursor addrCursor = Db.getInstance().selectSQL(sqlAddr);

		//Log.d("ClientList ", "Start processing addresses");
		if (addrCursor != null && addrCursor.getCount() > 0)
		{
			int clientIDColumnIdx = addrCursor.getColumnIndex("ClientID");
			int addrIDColumnIdx = addrCursor.getColumnIndex("AddrID");
			int addrNameColumnIdx = addrCursor.getColumnIndex("AddrName");
			int goldColumnIdx = addrCursor.getColumnIndex("gold");
			int silverColumnIdx = addrCursor.getColumnIndex("silver");
			int visitStartColumnIdx = addrCursor.getColumnIndex("VisitStartDate");

			int addrTypeDeliveryIdx = addrCursor.getColumnIndex("IsDeliveryPoint");
			int addrTypeSaleIdx = addrCursor.getColumnIndex("IsSalePoint");
			int addrTypeOfficeIdx = addrCursor.getColumnIndex("IsOffice");
			int addrTypeOrderIdx = addrCursor.getColumnIndex("IsOrderPoint");

			int recordStateIdx = displayRoute ? addrCursor.getColumnIndex("State") : 0;
			int visitTypeIdx = displayRoute ? addrCursor.getColumnIndex("VisitTypeID") : 0;

			int channelIdIdx = addrCursor.getColumnIndex("ChannelID");

			clientListData.totalAddressCount = addrCursor.getCount();

			boolean showClientQualification = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_SHOW_CLIENT_QUALIFICATION_IN_LIST, 0) > 0;

			for(int i = 0; i < addrCursor.getCount(); i++)
			{
				addrCursor.moveToPosition(i);

				boolean haveVisits = !addrCursor.isNull(visitStartColumnIdx);
				boolean haveVisitsToday = false;
				if (haveVisits)
				{
					Calendar lastVisitStartDate = Convert.getDateFromString(addrCursor.getString(visitStartColumnIdx));
					haveVisitsToday = Convert.getDateDiffInDays(Calendar.getInstance(), lastVisitStartDate ) == 0;
				}

				long clientID = addrCursor.getInt(clientIDColumnIdx);
				long addrID = addrCursor.getInt(addrIDColumnIdx);
				String addrName = addrCursor.getString(addrNameColumnIdx);
				int isGold = addrCursor.getInt(goldColumnIdx);
				int isSilver = addrCursor.getInt(silverColumnIdx);
				boolean isDeliveryPoint = (addrCursor.getInt(addrTypeDeliveryIdx) > 0);
				boolean isSalePoint = (addrCursor.getInt(addrTypeSaleIdx) > 0);
				boolean isOffice = (addrCursor.getInt(addrTypeOfficeIdx) > 0);
				boolean isOrderPoint = (addrCursor.getInt(addrTypeOrderIdx) > 0);
				char routeItemState = displayRoute ? Convert.getRecordStateFromString(addrCursor.getString(recordStateIdx)) : Q.RECORD_STATE_CLOSED;
				int visitTypeID = displayRoute ? addrCursor.getInt(visitTypeIdx) : 0;

				long channelId = addrCursor.isNull(channelIdIdx) ? -1 : addrCursor.getLong(channelIdIdx);

				List<PlanItem> progressValues = new ArrayList<PlanItem>();

				if(isGold > 0 && showClientQualification)
				{
					//powerSKU
					PlanItem planPowerSKU = Plans.getPlanValues(addrID, Plans.PLAN_TYPE_MONTH_POWER_SKU, false, Plans.SKU_UNIT_ID, true);
					String  sqlFactPowSKU = Q.getPlanFact(Plans.PLAN_TYPE_MONTH_POWER_SKU, true, false, 0, addrID, channelId);
					Long factPSKU = Db.getInstance().getDataLongValue(sqlFactPowSKU, 0);
					planPowerSKU.fact += factPSKU;

					//планы по ОПД
					PlanItem planGeneralDistr = Plans.getPlanValues(addrID, Plans.PLAN_TYPE_MONTH_DISTRIBUTION, false, Plans.SKU_UNIT_ID, true);
					String  sqlGeneralDistr = Q.getPlanFact(Plans.PLAN_TYPE_DAY_DISTRIBUTION, true, false, 0, addrID, channelId);
					Long factGeneralDistr = Db.getInstance().getDataLongValue(sqlGeneralDistr, 0);
					planGeneralDistr.fact += factGeneralDistr;

					progressValues.add(planGeneralDistr);
					progressValues.add(planPowerSKU);

					sql = "   SELECT count(*) as factCnt, " +
							" 	   (select count(*) from QuestionTargets t where t.addrid = " + addrID + " AND t.TargetValue is not null) as planCnt " +
							" FROM QuestionTargets qt " +
							"      LEFT JOIN Questions q ON qt.QuestionID = q.QuestionID " +
							" WHERE coalesce(qt.PrevResult, -1) >= coalesce(qt.TargetValue,0) " +
							"		AND qt.AddrID = " + addrID +
							" 		AND qt.TargetValue is not null ";

					Map<String, String> row = Db.getInstance().selectRowValuesInMap(sql);

					long plan = Convert.toLong(row.get("planCnt"), 0);
					long fact = Convert.toLong(row.get("factCnt"), 0);
					progressValues.add(new PlanItem(plan, fact, Plans.PCS_UNIT_ID));
					row = null;
				}

				ArrayList<IChildItem> addrList = clientListData.addressMap.get(clientID);
				if( addrList != null)
					addrList.add( new ListItemAddress(addrID, clientID, addrName, haveVisitsToday,
														isGold > 0, isSilver > 0, isDeliveryPoint, isSalePoint, isOffice, isOrderPoint,
														addrSelectListener, addrDeleteListener, addrMoveListener,
														routeItemState, visitTypeID, progressValues, chartDetailsListener));
				if(haveVisitsToday)
					clientListData.totalVisitedAddressCount++;
			}
		}

		//Log.d("ClientList ", "Finished processing addresses");

		if(addrCursor != null)
			addrCursor.close();

		//
		//create adapter
		//
		int[] clientViewIds = new int[] { R.id.text_client, R.id.imgGold, R.id.imgSilver };

		int[] addrViewIds = new int[] { R.id.text_address, R.id.imgGold, R.id.imgSilver, R.id.imgAddress, R.id.imgSalePoint,
										R.id.imgOrderPoint, R.id.imgDeleteItem, R.id.chartProgress, R.id.chartProgressBkg };

		ExpandableAdapterForArray clientAdapter = new ExpandableAdapterForArray(context, clientListData.clients, clientListData.addressMap,
												R.layout.client_list_item, R.layout.addr_list_item, clientViewIds, addrViewIds);

		mClientAddrExpandableList.setAdapter(clientAdapter);

		//Log.d("ClientList ", "Finished fillClientAddrList");
    }

    //--------------------------------------------------------------
    /** Обновляет информацию о посещенных точках (пометки в списке адресов и количество посещенных точек)
     * @param addressMap ассоциативный массив адресов (ключ-идентификатор клиента)
     * */
    private void updateVisitInfo(Map<Long, ArrayList<IChildItem> > addressMap)
    {
    	if(addressMap == null)
    		return;

    	int totalVisitedAddressCount = 0;

    	Date useRouteDate = routeDate.getTime();
    	boolean route = mCheckRoute.isChecked();
		String routeFilterAddresses = route ? " AND exists ( SELECT r.AddrID " +
															" FROM Routes r " +
															" WHERE r.AddrID = a.AddrID AND r.Date " + Q.getSqlBetweenDayStartAndEnd(useRouteDate) + ")" : "";

		String sql = "SELECT a.ClientID, v.AddrID, max(v.VisitStartDate) as VisitStartDate " +
					 "FROM Visits v " +
					 	"  INNER JOIN Addresses a ON v.AddrID = a.AddrID " +
					 "WHERE 1 = 1 " +
					 		routeFilterAddresses +
					 "GROUP BY v.AddrID ";

		Cursor cursor = Db.getInstance().selectSQL(sql);

		if (cursor != null)
		{
			totalVisitedAddressCount = cursor.getCount();

			int clientIDColumnIdx = cursor.getColumnIndex("ClientID");
			int addrIDColumnIdx = cursor.getColumnIndex("AddrID");
			int visitStartColumnIdx = cursor.getColumnIndex("VisitStartDate");

			for(int i = 0; i < cursor.getCount(); i++)
			{
				cursor.moveToPosition(i);
				long clientID = cursor.getInt( clientIDColumnIdx );
				long addrID = cursor.getInt(addrIDColumnIdx);

				boolean haveVisits = !cursor.isNull(visitStartColumnIdx);
				boolean haveVisitsToday = false;
				if(haveVisits)
				{
					Calendar lastVisitStartDate = Convert.getDateFromString(cursor.getString(visitStartColumnIdx));
					if( Convert.getDateDiffInDays(lastVisitStartDate, Calendar.getInstance()) == 0)
						haveVisitsToday = true;

					ArrayList<IChildItem> list = addressMap.get(clientID);

					if(list!=null)
					{
						for(int j = 0; j < list.size(); j++)
						{
							ListItemAddress addrItem = (ListItemAddress)list.get(j);
							if(addrItem != null && addrItem.id == addrID)
							{
								addrItem.haveVisitsToday = haveVisitsToday;
								break;
							}
						}
					}
				}
			}

			cursor.close();
		}
		mTextVisitedAddressCount.setText(String.format("%d",totalVisitedAddressCount));
    }

    //--------------------------------------------------------------
    /** Начать визит
     * @param clientID идентификатор клиента
     * @param addrId идентификатор адреса
     * */
    private void startVisit(long clientID, long addrID, int visitType)
    {
		AntContext.getInstance().getTabController().removeAllTabs();
		AntContext.getInstance().startVisit(new Client(clientID), new Address(addrID), visitType);
		AntContext.getInstance().getTabController().addAddressTabs(this);
    }

    //--------------------------------------------------------------
    // Methods required for display overlay window with letter. ListView.OnScrollListener implementation
    /** Callback метод, вызывается при скроллинге списка. Показывает во всплывающем окне заглавную букву алфавита, на которую переместился список*/
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
    	try
    	{
			if(mOverlayHelper != null && clientListData.clients!=null && view instanceof ExpandableListView)
			{
				ExpandableListView expandableList = (ExpandableListView) view;
				int groupIndex = expandableList.getPackedPositionGroup(expandableList.getExpandableListPosition(firstVisibleItem));
				if(groupIndex < clientListData.clients.size())
				{
					char firstLetter = ((ListItemClient)clientListData.clients.get(groupIndex)).name.charAt(0);
					mOverlayHelper.onScroll(firstLetter);
				}
			}
    	}
    	catch(Exception ex)
    	{
    		ErrorHandler.CatchError("Exception in ClientListForm1.onScroll", ex);
    	}
    }

    /** Имплементация метода AbsListView.OnScrollListener (заглушка)*/
    public void onScrollStateChanged(AbsListView view, int scrollState) {}
    /** Обработка возобновление работы Activity*/
    @Override protected void onResume() { super.onResume(); if( mOverlayHelper != null ) mOverlayHelper.onResume();	}
    /** Обработка паузы работы Activity*/
    @Override protected void onPause() {  super.onPause();  if( mOverlayHelper != null ) mOverlayHelper.onPause();  }
    /** Обработка очистки ресурсов при завершении Activity*/
    @Override protected void onDestroy() { super.onDestroy(); if( mOverlayHelper != null ) mOverlayHelper.onDestroy(); }

    //--------------------------------------------------------------
    /** Обработка нажатия клавиши "Назад". Выводит диалоговое окно подтверждения выхода из программы.*/
    @Override public void onBackPressed()
    {
    	try
    	{
	    	//
	    	//Display alert dialog proposing to confirm exit from application
	    	//

    		MessageBoxButton[] buttons = new MessageBoxButton[]
            {
					new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.client_list_exitAppOk),
										new DialogInterface.OnClickListener()
										{
											@Override public void onClick(DialogInterface dialog, int which)
											{
												finish();
												System.runFinalizersOnExit(true);
												System.exit(0);
											}
										}),
					new MessageBoxButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.client_list_exitAppCancel),
										new DialogInterface.OnClickListener()
										{
											@Override public void onClick(DialogInterface dialog, int which) { }
										})
            };

    		MessageBox.show(this, getResources().getString(R.string.client_list_exitAppHeader),	getResources().getString(R.string.client_list_exitAppMessage), buttons);

    	}
		catch(Exception ex)
		{
			ErrorHandler.CatchError("Exception in ClientListForm1.onBackPressed", ex);
		}
    }

    //--------------------------------------------------------------
    /** Вызывается при приобретении или потери окном фокуса
     * @param hasFocus наличие фокуса у окна
     */
    @Override public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
        {
        	try
        	{
        		if(mClientAddrExpandableList != null)
        		{
        			updateVisitInfo(clientListData.addressMap);

        			//refresh view
        			BaseExpandableListAdapter adapter = (BaseExpandableListAdapter) mClientAddrExpandableList.getExpandableListAdapter();
        			if(adapter!=null) adapter.notifyDataSetChanged();

        			mClientAddrExpandableList.invalidate();
        		}
        	}
    		catch(Exception ex)
    		{
    			ErrorHandler.CatchError("Exception in ClientListForm1.onWindowFocusChanged", ex);
    		}
        }
    }

    //--------------------------------------------------------------
    /** Отображение даты маршрута на окне*/
    private void updateDateDisplay()
    {
        mTextRouteDate.setText( Convert.dateToString(routeDate));
    }

    //--------------------------------------------------------------
    /** Изменение цвета иконки фильтра в зависимости от наличия включенных фильтров */
    private void updateFilterIcon()
    {
    	int drawableId = ( attributeFilters!=null && (!attributeFilters.addressAttributeIds.isEmpty() ||attributeFilters.additionalFilters.size()>0)) ?
    			          R.drawable.filter_green :
    			          R.drawable.filter;

    	buttonFilters.setImageResource(drawableId);
    }

    //--------------------------------------------------------------
    /** Создает всплывающее диалоговое окно
     * @param id идентификатор диалогового окна
     * @return созданный диалог
     */
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
			                    	routeDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			                    	routeDate.set(Calendar.MONTH, monthOfYear);
			                    	routeDate.set(Calendar.YEAR, year);
			                        updateDateDisplay();

			                        fill();
			                    }
	                		},
	                		routeDate.get(Calendar.YEAR),
	                		routeDate.get(Calendar.MONTH),
	                		routeDate.get(Calendar.DAY_OF_MONTH));

	        	case IDD_ROUTE_ITEM_ADD_DIALOG:
	        	{
	        		RouteItemAddDialog dialog = new RouteItemAddDialog();
	        		Dialog dlg = dialog.onCreate(this,
	        						(Calendar)routeDate.clone(),
			        				new DialogInterface.OnClickListener()
			        				{
			            				public void onClick(DialogInterface dialog, int result)
			            				{
			            					removeDialog(IDD_ROUTE_ITEM_ADD_DIALOG);
			            				}
	        						},
	        						new AddressAndDateSelectListener()
					    			{
					    				public void onAddressAndDateSelected(final long clientId, final long addrId, final Calendar selectedDate)
					    				{
					    					int defaultVisitTypes = 0;

					    			        SelectVisitTypeDialog dlg = new SelectVisitTypeDialog();
					    			        dlg.show(ClientListForm1.this, defaultVisitTypes, true,
					    			        		new VisitTypeSelectListener()
							    			        {
							    						public void onVisitTypeSelected(int visitType, boolean needDocCountCheck)
							    			            {
							    							//save added route address to db
							    							Route.addRouteItem(addrId, selectedDate, visitType);

						            						//remove dialog and refresh display
							    			            	removeDialog(IDD_ROUTE_ITEM_ADD_DIALOG);

															//display a toast to user saying that route item is copied
															String message = String.format( getResources().getString(R.string.route_item_create_notify), Convert.dateToString(selectedDate));
															Toast.makeText(ClientListForm1.this, message, Toast.LENGTH_SHORT).show();

							    			            	if(mCheckRoute.isChecked())	//refresh only when route is displayed, because full list is not changed
							    			            		refreshActivity();

							    			            	removeDialog(IDD_ROUTE_ITEM_ADD_DIALOG);
							    			            }
							    			        },
					    			        		new DialogInterface.OnClickListener()
							    			        {
							    			            public void onClick(DialogInterface dialog, int result)
							    			            {
							    			            	removeDialog(IDD_ROUTE_ITEM_ADD_DIALOG);
							    			            }
							    			        }
					    			       		);
					    				}
					    			}
	        					);

	        		return dlg;
	        	}
	    		case IDD_FILTERS_DIALOG:
	    		{
	    			DialogInterface.OnClickListener cancelClickListener = new DialogInterface.OnClickListener()
	    			{
	    				@Override public void onClick(DialogInterface dialog, int which)
	    				{
	    					removeDialog(IDD_FILTERS_DIALOG);
	    				}
	    			};

	    			AddressAttributesDialog.OnSelectAttributes selectAttributesListener = new AddressAttributesDialog.OnSelectAttributes()
	    			{
	    				@Override public void onAttributesSelected(Attributes attributes)
	    				{
	    					attributeFilters = attributes;
	    					removeDialog(IDD_FILTERS_DIALOG);
	    					updateFilterIcon();

	    					fill();
	    				}
	    			};

	    			filtersDialog = new AddressAttributesDialog();
	    			Dialog dlg = filtersDialog.onCreate(this, attributeFilters, selectAttributesListener, cancelClickListener, false);

	    			return dlg;
	    		}
				case IDD_QUALIFICATION_PROGRESS_DIALOG:
				{
					QualificationProgressDialog qualificationProgressDialog = new QualificationProgressDialog();
					Dialog dlg = qualificationProgressDialog.onCreate(this, selectedAddrId, selectedProgressValues );
					qualificationProgressDialog.setOkClickListener(new DialogInterface.OnClickListener()
	    			{
	    				@Override public void onClick(DialogInterface dialog, int which)
	    				{
	    					removeDialog(IDD_QUALIFICATION_PROGRESS_DIALOG);
	    				}
	    			});
					return dlg;
				}
	        }
		}
		catch(Exception ex)
		{
			MessageBox.show(this, getResources().getString(R.string.form_title_docPayment), getResources().getString(R.string.doc_payment_exceptionOnCreateDialog));
			ErrorHandler.CatchError("Exception in ClientListForm1.onCreateDialog", ex);
		}

        return null;
    }

    //--------------------------------------------------------------
    /** Установка параметров диалогового окна перед отображением
     * @param id идентификатор диалогового окна
     * @param dialog класс диалогового окна
     */
    @Override protected void onPrepareDialog(int id, Dialog dialog)
    {
        switch (id)
        {
            case IDD_DATE_DIALOG:
                ((DatePickerDialog) dialog).updateDate(routeDate.get(Calendar.YEAR),
                										routeDate.get(Calendar.MONTH),
                										routeDate.get(Calendar.DAY_OF_MONTH));
                break;
        }
    }

    //--------------------------------------------------------------
    /**
     * Реакция на нажатие клавиши "Поиск" - показать/спрятать алфавитную панель
     */
    @Override public boolean onKeyDown (int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_SEARCH )
        {
    		mShowAlphabetButtonsPanel = !mShowAlphabetButtonsPanel;

    		if(mShowAlphabetButtonsPanel)
    			mAlphabetButtonsPanel.setVisibility(View.VISIBLE);
    		else
    			mAlphabetButtonsPanel.setVisibility(View.GONE);

            return true;
        }

        // Передаем управление родительскому классу
        return super.onKeyDown(keyCode, event);
    }
}

