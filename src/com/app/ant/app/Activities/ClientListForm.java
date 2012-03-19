package com.app.ant.app.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.*;
import com.app.ant.R;
import com.app.ant.app.AddressBook.util.GUIFactory;
import com.app.ant.app.BusinessLayer.Address;
import com.app.ant.app.BusinessLayer.Client;
import com.app.ant.app.BusinessLayer.Plans;
import com.app.ant.app.BusinessLayer.Route;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Settings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 24.02.12
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */
public class ClientListForm extends AntActivity implements View.OnClickListener {
    private static final int IDD_DATE_DIALOG = 0;
    private static final int IDD_ROUTE_ITEM_ADD_DIALOG = 1;
    private static final int IDD_FILTERS_DIALOG = 2;
    private static final int IDD_QUALIFICATION_PROGRESS_DIALOG = 3;

    private ListViewOverlayHelper mOverlayHelper = null;
    private List<Plans.PlanItem> selectedProgressValues;
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

    ArrayList<Address> data = new ArrayList<Address>();
    AddressAttributesDialog filtersDialog;
    AddressAttributesDialog.Attributes attributeFilters = new AddressAttributesDialog.Attributes();
    Context context;

    @Override
    public void onClick(View view) {
        if(view == mButtonAddRouteItem){
            showDialog(IDD_ROUTE_ITEM_ADD_DIALOG);
        }else if(view == buttonFilters){
            showDialog(IDD_FILTERS_DIALOG);
        }
    }

    public interface ChartDetailsListener {
        abstract void onChartSelected(long addrId, List<Plans.PlanItem> progressValues);
    }

    /**
     * Интерфейс для обратного вызова - возвращает выбранный адрес
     */
    public interface AddressSelectListener {
        abstract void onAddressSelected(long clientId, long addrId, int visitTypeID);
    }

    static public class ClientListData {
        public ArrayList<ExpandableAdapterForArray.IGroupItem> clients;
        public Map<Long, ArrayList<ExpandableAdapterForArray.IChildItem>> addressMap;

        public int totalAddressCount = 0;
        public int totalVisitedAddressCount = 0;
    }

    ListView listView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.client_list_1);
        initGUI();
        initActions();
        AntContext.getInstance().setContext(getApplicationContext()); //global accessible app context
        routeDate = Calendar.getInstance();
        mTextRouteDate.setText( Convert.dateToString(routeDate));
        //CheckAndCopyDatabase() ;
        listView = (ListView) findViewById(R.id.clientAddrExpandableList1);
        fill();
        int proposePlans = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_PROPOSE_DAY_PLAN_AT_START, 0);
        if (proposePlans > 0) {
            //
            //Display alert dialog proposing to display plans form

            MessageBox.MessageBoxButton[] buttons = new MessageBox.MessageBoxButton[]
                    {
                            new MessageBox.MessageBoxButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.dialog_base_yes),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startActivity(new Intent(ClientListForm.this, ReportDaySummariesForm.class));
                                        }
                                    }),
                            new MessageBox.MessageBoxButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.dialog_base_cancel),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                    };

            MessageBox.show(this, getResources().getString(R.string.client_list_showPlansHeader), getResources().getString(R.string.client_list_showPlans), buttons);
        }
    }

    private void initActions() {
        mButtonAddRouteItem.setOnClickListener(this);
        buttonFilters.setOnClickListener(this);
    }

    private void initGUI() {
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
        buttonFilters = (ImageButton) findViewById(R.id.buttonFilters);
    }

    private void fill() {
        String sql = Q.CLIENT_LIST_SELECT;
        sql = sql.replace("%Address.ATTR_GOLD_MAG%", String.valueOf(Address.ATTR_GOLD_MAG));
        data = new ArrayList<Address>();
        Cursor cursor = Db.getInstance().selectSQL(sql);
        if (cursor != null && cursor.getCount() > 0) {
            int i = 0;
            while (cursor.moveToPosition(i)) {
                int anInt = cursor.getInt(0);
                int anInt1 = cursor.getInt(1);
                Address client = new Address(anInt, anInt1);
                client.addrName = cursor.getString(2);
                if(cursor.getInt(3)==1)
                 client.goldMag = true;
                data.add(client);
                i++;
            }
        }
        if (cursor != null)
            cursor.close();
        //listView.destroyDrawingCache();

        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return data.size();  //To change boy of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Object getItem(int i) {
                return data.get(i);  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public long getItemId(int i) {
                return data.get(i).addrID;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public View getView(final int i, View view, ViewGroup viewGroup) {
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                //layout.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                TextView tw = new TextView(context);
                final Address address = data.get(i);
                tw.setText(address.addrName);
                tw.setTextSize(15);
                if (i % 2 == 0)
                    tw.setBackgroundColor(Color.LTGRAY);

                tw.setTextColor(Color.BLACK);
                tw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AntContext.getInstance().getTabController().removeAllTabs();
                        Address address1 = new Address(address.addrID);
                        Client client = new Client(address.clientID);
                        AntContext.getInstance().startVisit(client, address1, 0);
                        AntContext.getInstance().getTabController().addAddressTabs(context);
                        //startActivity(new Intent(ClientListForm.this, ReportDaySummariesForm.class));
                    }
                });

                ImageView view1 = GUIFactory.imageView(context, R.drawable.medal_gold_no);
                if (address.goldMag)
                    view1.setImageResource(R.drawable.medal_gold);
                layout.addView(view1);
                layout.addView(tw);
                return layout;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        //listView.refreshDrawableState();
    }


    @Override
    public void refreshActivity() {
        fill();
    }

    //--------------------------------------------------------------
    public static void fillClientAddrList(Context context, AddressAttributesDialog.Attributes attributeFilters,
                                          boolean displayRoute, Calendar routeDate,
                                          ClientListData clientListData, ExpandableListView mClientAddrExpandableList,
                                          AddressSelectListener addrSelectListener,
                                          AddressSelectListener addrDeleteListener,
                                          AddressSelectListener addrMoveListener,
                                          ChartDetailsListener chartDetailsListener,
                                          boolean excludeRoute) {
        //Log.d("ClientList ", "Start fillClientAddrList");

        boolean debtorsOnly = false;
        if (attributeFilters != null && attributeFilters.additionalFilters.contains(AddressAttributesDialog.AttributeType.additionalFilterDebt))
            debtorsOnly = true;

        //
        // take into account filters applied on address attributes
        //
        String filterByAttrAddr = "";
        String filterByAttrClient = "";


        //Log.d("ClientList ", "Finished fillClientAddrList");
    }

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
                            new RouteItemAddDialog.AddressAndDateSelectListener()
                            {
                                public void onAddressAndDateSelected(final long clientId, final long addrId, final Calendar selectedDate)
                                {
                                    int defaultVisitTypes = 0;

                                    SelectVisitTypeDialog dlg = new SelectVisitTypeDialog();
                                    dlg.show(ClientListForm.this, defaultVisitTypes, true,
                                            new SelectVisitTypeDialog.VisitTypeSelectListener()
                                            {
                                                public void onVisitTypeSelected(int visitType, boolean needDocCountCheck)
                                                {
                                                    //save added route address to db
                                                    Route.addRouteItem(addrId, selectedDate, visitType);

                                                    //remove dialog and refresh display
                                                    removeDialog(IDD_ROUTE_ITEM_ADD_DIALOG);

                                                    //display a toast to user saying that route item is copied
                                                    String message = String.format( getResources().getString(R.string.route_item_create_notify), Convert.dateToString(selectedDate));
                                                    Toast.makeText(ClientListForm.this, message, Toast.LENGTH_SHORT).show();

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
                        @Override public void onAttributesSelected(AddressAttributesDialog.Attributes attributes)
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
}