package com.app.ant.app.Activities;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.AddressBook.util.IOUtil;
import com.app.ant.app.AddressBook.util.Monitor;
import com.app.ant.app.BusinessLayer.Contact;
import com.app.ant.app.Activities.DocSaleHeaderDialog.DocSaleHeaderPopup;
import com.app.ant.app.Activities.DocSaleSelectGroupDialog.ItemGroup;
import com.app.ant.app.Activities.DocSaleSelectGroupDialog.ItemGroupSelector;
import com.app.ant.app.BusinessLayer.DocSale;
import com.app.ant.app.BusinessLayer.Document;
import com.app.ant.app.BusinessLayer.Filter;
import com.app.ant.app.BusinessLayer.Plans.PlanItem;
import com.app.ant.app.Controls.DataGrid;
import com.app.ant.app.Controls.DataGrid.CellStyle;
import com.app.ant.app.Controls.DataGrid.CellStyleCollection;
import com.app.ant.app.Controls.DataGrid.ColumnInfo;
import com.app.ant.app.Controls.DataGrid.GridColumns;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.MLog;
import com.app.ant.app.ServiceLayer.Settings;
import com.app.ant.app.ServiceLayer.StepControllerVisit;
import com.app.ant.app.ServiceLayer.TabController;
import com.app.ant.app.ServiceLayer.TabControllerVisit;
import com.app.ant.app.ServiceLayer.StepController.IStep;
import com.app.ant.app.ServiceLayer.StepController.ICacheableStep;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class DocSaleForm extends AntActivity implements IStep, ICacheableStep, View.OnClickListener, View.OnTouchListener, FilerGroupDialog.FilterGroupSelectListener {
    private final static int IDD_CALCULATOR = 0;
    private final static int IDD_STATS = 1;
    private final static int IDD_ACTIONS = 2;
    private final static int IDD_ITEM = 3;
    private final static int IDD_SELECT_COLUMNS = 4;
    private final static int IDD_SELECT_ITEM_GROUP = 5;
    private final static int IDD_DATE_DIALOG = 6;
    private final static int IDD_RESPITE_DIALOG = 7;
    private final static int IDD_COMMENTS_DIALOG = 8;
    private final static int IDD_PROGRESS_SPINNER_DIALOG = 9;
    private final static int IDD_DOC_SALE_HEADER_DIALOG = 10;
    private final static int IDD_EDIT_DATE_DIALOG = 11;
    private final static int IDD_CONTACT_POPUP = 12;


    /**
     * ��������� ������� ������� �� ������ � ������
     */
    public enum QuantityFilterState {
        NoFilter, FilterQuantity, FilterStock
    }

    ;

    private DocSale.DocSaleHeader mTempDocSaleHeader;    //temporary header to store unconfirmed data being edited

    private char activeDocColor = Document.DOC_COLOR_WHITE;
    //
    //Variables for popup number input dialog 
    //
    public DocSaleTableModel docSaleTableModel;
    private GridModel gridModelUp;
    private GridModel gridModelDown;

    boolean denyMoreThanRest;
    boolean bookingOn = false;
    boolean adjasmentOn = false;
    int commentIdx;

    private ViewGroup filterQuantityAndStock;
    private TextView mSum;
    private TextView mOrders;
    private TextView mTextQuantityFilterLabel;
    private TextView mTextStockFilterLabel;
    private TextView mTextItemGroup;
    private TextView mTextItemGroupParent;
    private ViewGroup mLayoutItemGroupTexts;
    private Button buttonActiveDocColor;
    private ImageButton buttonActions;
    private ViewGroup headerPanel;
    private ImageButton buttonEdit;
    private ImageButton buttonLineUp;
    private ImageButton buttonLineDown;
    private TextView filterList;
    private TextView filterType;


    private TextView booking;
    private TextView adjasment;
    //header items
    private TextView mTextDocType;
    private TextView mTextDate;
    private ImageButton mButtonNextStep;
    private ItemGroup mCurrentGroup;

    DocSaleSelectGroupDialog.ItemGroupSelector itemGroupSelector;
    private DocSaleCalculatorDialog calculatorDlg;
    private DocSaleSelectGroupDialog selectGroupDlg;
    private DocSaleSelectColumnsDialog selectColumnsDlg;
    private DocSaleCommentsDialog commentsDlg;
    private ItemDialog itemDlg;
    private DocSaleStatsDialog statsDlg;

    //Filters
    private String filterCondition = "";
    private String filterBooking = "";

    private String listFilterCondition = "";
    private String listFilterBooking = "";
    private String typeFilterCondition = "";
    private String typeFilterBooking = "";


    QuantityFilterState quantityFilter = QuantityFilterState.NoFilter;

    //need for buttonUp and buttonDown
    private boolean mButtonUpPressed = false;
    private boolean mButtonDownPressed = false;

    private Handler mUpHandler = new Handler();
    private Handler mDownHandler = new Handler();

    private final int SELECTION_FIRST_MOVEMENT_INTERVAL = 500;
    private final int SELECTION_MOVEMENT_INTERVAL = 100;

    private boolean returnToDebtForm = false;
    private boolean applyPredictOrders = false;

    private boolean mRowChanged = true;
    private boolean useQuickKeyboard = true;

    //���������� ��������� ����� ��� �������������� ��� �������� �� ����� ������ ������� � ������
    private HashMap<Long, DataGrid.GridState> gridStates = new HashMap<Long, DataGrid.GridState>();
    public HashMap<Integer, Filter> listsChecked;
    public HashMap<Integer, Filter> typeChecked;

    private Handler mPlanTotalsCalculationHandler = new Handler();
    private final int PLAN_TOTALS_CALCULATION_INTERVAL = 1000;

    //--------------------------------------------------------------	
    private DocSaleSelectGroupDialog.OnSelectGroupListener selectGroupListener = new DocSaleSelectGroupDialog.OnSelectGroupListener() {

        @Override
        public void onGroupSelected(ItemGroup itemGroup, boolean fromDialog) {
            try {
                if (fromDialog)
                    removeDialog(IDD_SELECT_ITEM_GROUP);
                final String filter = (bookingOn) ? filterBooking : filterCondition;
                applyNewItemGroupUp(itemGroup);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        refreshGridAsync(gridModelUp, filter, bookingOn, true, null);
                    }
                }).start();

                applyNewItemGroupDown(itemGroup);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        refreshGridAsync(gridModelDown, filter, bookingOn, false, null);
                    }
                });
                gridModelUp.mGrid.setSelectedRow(0, 1);
            } catch (Exception ex) {
                MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionDisplay) + "1");
                ErrorHandler.CatchError("Exception in DocSaleForm.onGroupSelected", ex);
            }
        }
    };

    //--------------------------------------------------------------

    /**
     * �������������� ������ ��������� "�������"
     */
    public class InfoPanelRemnants extends InfoPanelBase {
        TextView textRemnantsEntered;
        TextView textAllItems;

        protected void getFields() {
            textRemnantsEntered = (TextView) infoPanelLayout.findViewById(R.id.textRemnantsEntered);
            textAllItems = (TextView) infoPanelLayout.findViewById(R.id.textAllItems);
        }

        public void displayTotals() {
            if (textRemnantsEntered != null)
                textRemnantsEntered.setText(String.format("%d", gridModelUp.mDocSale.totalRemnantEntered));
            if (textAllItems != null) textAllItems.setText(String.format("%d", gridModelUp.mCursor.getCount()));
        }

        /*public void loadInfoPanel(Context context, InfoPanelBase prevPanel)
          {
              int rowHeight = Convert.dipToPixels(20);
              int minHeight = Convert.dipToPixels(30);
              int maxHeight = minHeight;			
  
              int infoPanelResId = R.layout.doc_sale_remnants_statistics_panel;
              int infoPanelLayoutMainViewResId = R.id.docSaleStats;
              
              //
              //create information panel
              //
              
              loadInfoPanel( context, prevPanel, R.id.infoPanelPlacement, infoPanelResId, infoPanelLayoutMainViewResId,
                                  rowHeight, maxHeight, minHeight);
              
              getFields();					   
          }*/

    }

    //--------------------

    /**
     * �������������� ������ ��������� "�������"
     */
    public class InfoPanelDocument extends InfoPanelBase {
        TextView textItemName;
        TextView textItemExt;
        TextView textItemPerCase;
        TextView textItemUnitWeight;

        //totals
        TextView textSum;
        TextView textOrderItems;
        TextView textSumI;
        TextView textSumII;
        TextView textCases;
        TextView textPalettes;
        //		TextView textSumVAT;
//		TextView textSumWoVAT;
        TextView textUnits;
        TextView textMSU;
        TextView textAllItems;

        //plan totals
        TextView textPlanDistr;
        TextView textPlanPowerSKU;

        protected void getFields() {
            textItemName = (TextView) infoPanelLayout.findViewById(R.id.textItemName);
            textItemExt = (TextView) infoPanelLayout.findViewById(R.id.textItemExt);
            textItemPerCase = (TextView) infoPanelLayout.findViewById(R.id.textItemPerCase);
            textItemUnitWeight = (TextView) infoPanelLayout.findViewById(R.id.textItemUnitWeight);

            textSum = (TextView) infoPanelLayout.findViewById(R.id.textSum);
            textOrderItems = (TextView) infoPanelLayout.findViewById(R.id.textOrderItems);
//			textSumI = (TextView) infoPanelLayout.findViewById(R.id.textSumI);
//			textSumII = (TextView) infoPanelLayout.findViewById(R.id.textSumII);
            textCases = (TextView) infoPanelLayout.findViewById(R.id.textCases);
            textPalettes = (TextView) infoPanelLayout.findViewById(R.id.textPalettes);
//			textSumVAT = (TextView) infoPanelLayout.findViewById(R.id.textSumVAT);
//			textSumWoVAT = (TextView) infoPanelLayout.findViewById(R.id.textSumWoVAT);
            textUnits = (TextView) infoPanelLayout.findViewById(R.id.textUnits);
            textMSU = (TextView) infoPanelLayout.findViewById(R.id.textSumMSU);
//			textAllItems = (TextView) infoPanelLayout.findViewById(R.id.textAllItems);

            textPlanDistr = (TextView) infoPanelLayout.findViewById(R.id.textPlanDistr);
            textPlanPowerSKU = (TextView) infoPanelLayout.findViewById(R.id.textPlanPowerSKU);
        }

        public void displayTotals() {

            int itemID = AntContext.getInstance().curItemId;

            if (itemID == 0) {
                if (textItemName != null) textItemName.setText("");
                if (textItemExt != null) textItemExt.setText("");
                if (textItemPerCase != null) textItemPerCase.setText("");
                if (textItemUnitWeight != null) textItemUnitWeight.setText("");
            } else {
                String sql =
                        " SELECT i.ItemExt, i.License, i.CashName, i.ScreenName, i.ItemName, i.PerPall, i.PerCase, i.Volume, " +
                                " round(i.UnitWeight,3) AS UnitWeight, i.ItemTax, i.ShelfLife " +
                                " FROM Items i " +
                                " WHERE i.ItemID = " + itemID;

                Cursor cursor = Db.getInstance().selectSQL(sql);

                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToPosition(0);

                    if (textItemName != null) textItemName.setText(cursor.getString(cursor.getColumnIndex("ItemName")));
                    if (textItemExt != null) textItemExt.setText(cursor.getString(cursor.getColumnIndex("ItemExt")));
                    if (textItemPerCase != null)
                        textItemPerCase.setText(cursor.getString(cursor.getColumnIndex("PerCase")));
                    if (textItemUnitWeight != null)
                        textItemUnitWeight.setText(cursor.getString(cursor.getColumnIndex("UnitWeight")));
                }
            }

            if (textSum != null) textSum.setText(Convert.moneyToString(gridModelUp.mDocSale.totalAll));
            if (textOrderItems != null)
                textOrderItems.setText(String.format("%d", gridModelUp.mDocSale.totalItems) + " (" + String.format("%d", gridModelUp.mCursor.getCount()) + ")");
//			if(textSumI!=null) textSumI.setText(Convert.moneyToString(mDocSale.total1));
//			if(textSumII!=null) textSumII.setText(Convert.moneyToString(mDocSale.total2));
            if (textCases != null) textCases.setText(String.format("%.2f", gridModelUp.mDocSale.totalCases));
            if (textPalettes != null) textPalettes.setText(String.format("%.2f", gridModelUp.mDocSale.totalPalettes));
//			if(textSumVAT != null) textSumVAT.setText(Convert.moneyToString(mDocSale.totalVat));
//			if(textSumWoVAT != null) textSumWoVAT.setText(Convert.moneyToString(mDocSale.totalNoVatAll));
            if (textUnits != null) textUnits.setText(String.format("%d", gridModelUp.mDocSale.totalOrders));
            if (textMSU != null) textMSU.setText(String.format("%.4f", gridModelUp.mDocSale.totalMSU));
//			if(textAllItems != null) textAllItems.setText(String.format("%d", mCursor.getCount()));

            if (textPlanDistr != null) {
                PlanItem plan = gridModelUp.mDocSale.tolalPlanDistr;
                if (plan != null) {
                    double fact = plan.fact + gridModelUp.mDocSale.tolalFactDistr;
                    textPlanDistr.setText(String.format("%s(%s)", PlanItem.getValueString(fact, plan.unitID), plan.getPlanString()));
                }
            }

            if (textPlanPowerSKU != null) {
                PlanItem plan = gridModelUp.mDocSale.tolalPlanPowerSKU;
                if (plan != null) {
                    double fact = plan.fact + gridModelUp.mDocSale.tolalFactPowerSKU;
                    textPlanPowerSKU.setText(String.format("%s(%s)", PlanItem.getValueString(fact, plan.unitID), plan.getPlanString()));
                }
            }
        }

        /*public void loadInfoPanel(Context context, InfoPanelBase prevPanel)
          {
              int rowHeight = Convert.dipToPixels(20);
              int minHeight = Convert.dipToPixels(30);
              int maxHeight = minHeight + rowHeight*4;			
  
              int infoPanelResId = R.layout.doc_sale_statistics_panel;
              int infoPanelLayoutMainViewResId = R.id.docSaleStats;
              
              //
              //create information panel
              //
              
              loadInfoPanel( context, prevPanel, R.id.infoPanelPlacement, infoPanelResId, infoPanelLayoutMainViewResId,
                                  rowHeight, maxHeight, minHeight);
              
              getFields();
          }	*/

    }

    //--------------------

    /**
     * ��� �������������� ������
     */
    public enum InfoPanelType {
        Empty, DocumentSummary, ItemSummary, RemnantSummary
    }

    ;
    //private InfoPanelBase infoPanel;
    InfoPanelType currentPanelType = InfoPanelType.Empty;

    /*public void loadInfoPanel(boolean displayItemInfo)
     {
         InfoPanelType newPanelType;
 
         if(displayItemInfo)
             newPanelType = InfoPanelType.ItemSummary;
         else
         {
             if(mDocSale.mDocHeader.docType == Document.DOC_TYPE_REMNANTS)
                 newPanelType = InfoPanelType.RemnantSummary;
             else
                 newPanelType = InfoPanelType.DocumentSummary;
         }
 
         if(newPanelType == currentPanelType)
             return;
 
         currentPanelType = newPanelType;
 
         InfoPanelBase prevPanel = infoPanel;
 
         if(newPanelType == InfoPanelType.ItemSummary)
             infoPanel = new InfoPanelSKU();
         else if(newPanelType == InfoPanelType.RemnantSummary)
             infoPanel = new InfoPanelRemnants();
         else if(newPanelType == InfoPanelType.DocumentSummary)
             infoPanel = new InfoPanelDocument();
 
         if(infoPanel != null)
             infoPanel.loadInfoPanel(this, prevPanel);
     }*/
    //--------------------------------------------------------------
    public class GridProps {
        String sortField;
        DataGrid.SortOrder sortOrder;
        Map<Integer, Object[]> editedValues;

        void getFromGrid(DataGrid dataGrid) {
            if (dataGrid == null)
                return;

            sortField = dataGrid.getSortColumnField();
            sortOrder = dataGrid.getSortOrder();
            editedValues = dataGrid.getEditedValues();
        }
    }

    public class InstanceProps {
        GridProps gridProps = new GridProps();
    }

    //--------------------------------------------------------------	

    /**
     * ������������� �����
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        overridePendingTransition(0, 0);

        try {
            String colorFromPrefs = Settings.getInstance().getStringActivityPreference("doc_sale_form_preferences", "activeDocColor", new Character(Document.DOC_COLOR_WHITE).toString());
            activeDocColor = colorFromPrefs.charAt(0);
        } catch (Exception ex) {
            ErrorHandler.CatchError("DocSaleForm.onCreate", ex);
        }

        onCreateEx(true);
        try {
            if(gridModelUp!=null&&gridModelUp.mDocSale!=null&&gridModelUp.mDocSale.mDocHeader!=null)
            mTempDocSaleHeader = gridModelUp.mDocSale.mDocHeader.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        showDialog(IDD_DOC_SALE_HEADER_DIALOG);
    }

    @Override
    public void refreshActivity() {
        //TODO REFRESH FORM AFTER SYNC
        //if (mCurrentGroup != null)
        //	selectGroupListener.onGroupSelected(mCurrentGroup);
        gridModelUp.mCursor = fillGridWithData(true, true, false, false, gridModelUp, true);
        gridModelDown.mCursor = fillGridWithData(true, true, false, false, gridModelDown, false);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onCreateEx(false);
    }

    private void onCreateEx(boolean firstRun) {
        try {
            setContentView(R.layout.doc_sale);

            if (firstRun)
                AntContext.getInstance().getStepController().registerActivityOnLaunch(this);

            InstanceProps instanceProps = null;

            if (!firstRun) {
                //screen is rotated, store some values before rotation
                //recreate views
                //later, stored props will be applied to new views
                instanceProps = new InstanceProps();

                instanceProps.gridProps.getFromGrid(gridModelUp.mGrid);
            }

            //
            //create new docSale using parameters
            //
            if (firstRun) {

                gridModelUp = new GridModel();
                gridModelDown = new GridModel();

                gridModelUp.setmDocSale(new DocSale(getIntent().getExtras()));
                //gridModelDown.setmDocSale(new DocSale(getIntent().getExtras()));
                gridModelDown.setmDocSale(gridModelUp.getmDocSale());

                gridModelUp.setCursorData(new CursorData());
                gridModelDown.setCursorData(new CursorData());

                Bundle params = getIntent().getExtras();
                if (params != null && params.containsKey(Document.PARAM_NAME_RETURN_TO_DEBTS))
                    returnToDebtForm = params.getBoolean(Document.PARAM_NAME_RETURN_TO_DEBTS);
            }

            initGUI();
            initActions();
            //
            // init item groups
            //

            if (firstRun) {
                itemGroupSelector = new ItemGroupSelector(this, selectGroupListener,
                        gridModelUp.mDocSale.isEditable ? DocSaleSelectGroupDialog.SelectorType.order :
                                DocSaleSelectGroupDialog.SelectorType.closedOrder);
                mCurrentGroup = itemGroupSelector.getCurrentDictionary().getDefaultGroup();
            }
            onItemGroupApplied();

            InitStepBar();
            gridModelUp.mGrid = (DataGrid) findViewById(R.id.dataGridItems);
            gridModelDown.mGrid = (DataGrid) findViewById(R.id.dataGridItems1);

            gridModelUp.mCursor = InitGrid(instanceProps == null ? null : instanceProps.gridProps, gridModelUp, true);
            gridModelDown.mCursor = InitGrid(instanceProps == null ? null : instanceProps.gridProps, gridModelDown, false);
            gridModelUp.mGrid.setSelectedRow(0);

            updateFilterLabels();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            useQuickKeyboard = prefs.getBoolean(getString(R.string.preference_key_document_quick_keyboard_key), true);
            denyMoreThanRest = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_DENY_ORDER_QNT_MORE_THAN_REST, 0) == 1;


            /*//
               // Create info panel
               //
               if(!firstRun)
                   currentPanelType = InfoPanelType.Empty;
               
               loadInfoPanel(false);
               
               if(infoPanel!=null)
                   infoPanel.displayTotals();*/

            //
            //Configure document appearance
            //
            configDocAppearanceByType();
            //ItemGroup itemGroup = createAllItemsItemGroup();
            //applyNewItemGroup(itemGroup,false,gridModelDown);
            //setCursorToGrid(gridModelDown.mGrid, gridModelDown.mCursor, docSaleTableModel, gridModelDown.mDocSale);
            /*if(infoPanel!=null)
            infoPanel.displayTotals();*/

        } catch (Exception ex) {
            MessageBox.show(this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionOnCreate));
            ErrorHandler.CatchError("Exception in DocSaleForm.onCreate", ex);
        }

    }

    /*   private ItemGroup createAllItemsItemGroup() {
        String sql = "SELECT * FROM ItemGroups WHERE ItemGroupID = 2";
        Cursor cursor = Db.getInstance().selectSQL(sql);
        long id = (long) cursor.getDouble(0);
        String name = cursor.getString(2);
        boolean haveDocGridCondition = false;
        String docGridCondition = "";
        boolean enabled = true;
        int groupType = cursor.getInt(4);
        long parentId = (long) cursor.getDouble(1);
        long flags = (long) 0.0;
        if (cursor != null)
            cursor.close();
        ItemGroup itemGroup = new ItemGroup(id, name, haveDocGridCondition, docGridCondition, enabled, groupType, false, false, parentId, flags, (long) 0.0, 0.0, 0.0, false);
        return itemGroup;
    }*/

    private void initActions() {
        mLayoutItemGroupTexts.setOnClickListener(this);
        buttonActions.setOnClickListener(this);
        filterList.setOnClickListener(this);
        filterType.setOnClickListener(this);
        headerPanel.setOnClickListener(this);
        buttonActiveDocColor.setOnClickListener(this);
        buttonEdit.setOnClickListener(this);
        buttonLineUp.setOnTouchListener(this);
        buttonLineDown.setOnTouchListener(this);
        booking.setOnClickListener(this);
        adjasment.setOnClickListener(this);
        filterQuantityAndStock.setOnClickListener(this);
    }

    private void initGUI() {
        mTextQuantityFilterLabel = (TextView) findViewById(R.id.textFilterQuantity);
        mTextStockFilterLabel = (TextView) findViewById(R.id.textFilterStock);
        mTextItemGroup = (TextView) findViewById(R.id.textItemGroup);
        mTextItemGroupParent = (TextView) findViewById(R.id.textItemGroupParent);
        mLayoutItemGroupTexts = (ViewGroup) findViewById(R.id.itemGroupTexts);
        mSum = (TextView) findViewById(R.id.sumClient);
        mOrders = (TextView) findViewById(R.id.textOrders);
        //
        //doc header items
        //
        mTextDocType = (TextView) findViewById(R.id.textDocType);
        mTextDate = (TextView) findViewById(R.id.textDate);
        mButtonNextStep = (ImageButton) findViewById(R.id.buttonNextStep);
        filterList = (TextView) findViewById(R.id.doc_sale_filter_list);
       filterType = (TextView) findViewById(R.id.doc_sale_filter_type);
        filterQuantityAndStock = (ViewGroup) findViewById(R.id.filterQuantityAndStock);
        booking = (TextView) findViewById(R.id.textSaleBooking);
        adjasment = (TextView) findViewById(R.id.textSaleAdjacent);
        headerPanel = (ViewGroup) findViewById(R.id.headerPanel);

        NextStepClickListener l = new NextStepClickListener();
        mButtonNextStep.setOnClickListener(l);
        TextView textClient = (TextView) findViewById(R.id.textClient);
        textClient.setText(AntContext.getInstance().getClient().nameScreen.trim());
        //
        // doc header
        //
        updateDateDisplay();
        updateDocTypeDisplay();
        updateRespiteDisplay();
        updatePriceDisplay();
        updateCommentsDisplay();
        //
        //init action button
        //
        buttonActions = (ImageButton) findViewById(R.id.buttonActions);
        buttonActiveDocColor = (Button) findViewById(R.id.buttonActiveDocColor);
        //updateActiveColorButton();
        //
        // Up/down movement, edit
        //
        buttonLineUp = (ImageButton) findViewById(R.id.buttonLineUp);
        buttonLineDown = (ImageButton) findViewById(R.id.buttonLineDown);
        buttonEdit = (ImageButton) findViewById(R.id.buttonEdit);
    }

    //--------------------------------------------------------------
    private void selectColumnBeingEdited(String editField, DataGrid mGrid, CursorData cursorData) {
        ColumnInfo column = mGrid.getColumns().getColumnByDbField(editField);

        if (column.isVisible())
            mGrid.setSelectedColumn(cursorData.mEditedColumn);
    }

    private Cursor InitGrid(GridProps gridProps, GridModel gridModel, boolean visibility) {
        Cursor mCursor;
        docSaleTableModel = new DocSaleTableModel();
        //load grid columns
        DocSale mDocSale = gridModel.mDocSale;
        DataGrid grid = gridModel.mGrid;
        int columnsSetId = mDocSale.mDocHeader.docType == Document.DOC_TYPE_REMNANTS
                ? Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_COLUMNS_SET_ID_REMNANTS, 2)
                : Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_COLUMNS_SET_ID_CLAIM_SALE, 1);

        //set grid styles
        CellStyleCollection styles = AntContext.getInstance().getStyles();

        grid.setCellStyles(styles);

        GridColumns gridColumns = new GridColumns(columnsSetId);
        grid.setColumns(gridColumns);

        grid.setDefaultSortColumnByDbField("SortID");

        if (gridProps == null) {
            grid.setSortOrder(DataGrid.SortOrder.ASCENDING);
        } else {
            grid.setSortColumnByDbField(gridProps.sortField);
            grid.setSortOrder(gridProps.sortOrder);
            grid.setEditedValues(gridProps.editedValues);
        }

        //get grid row height from settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int gridRowHeight = Convert.toInt(prefs.getString(getString(R.string.preferences_grid_row_height_key), "40"), 40);
        int gridTextSize = Convert.toInt(prefs.getString(getString(R.string.preferences_grid_text_size_key), "16"), 16);
        int gridFlingVelocity = Convert.toInt(prefs.getString(getString(R.string.preferences_grid_fling_velocity_key), "15"), 15);
        useQuickKeyboard = prefs.getBoolean(getString(R.string.preference_key_document_quick_keyboard_key), true);

        grid.setRowHeight(gridRowHeight);
        grid.setTextSize(gridTextSize);
        grid.setHeaderTextSize(gridTextSize);
        grid.setFlingVelocity(gridFlingVelocity);
        grid.setAdditionalBottomPadding(useQuickKeyboard ? 120 : 0);

        mCursor = fillGridWithData(false, true, false, false, gridModel, visibility);

        gridModel.mCursor = mCursor;
        gridModel.mGrid = grid;
        gridModel.mDocSale = mDocSale;

        gridModel.mGrid.setCellListener(new CellListener(gridModel));
        return mCursor;
    }

    //--------------------------------------------------------------
    private Cursor fillCursor(DataGrid grid, DocSale mDocSale, boolean upper, String docGridCondition) {
        Cursor mCursor = null;
        String sortColumn = grid.getDbFieldForColumn(grid.getSortColumn());
        String sortOrder = (grid.getSortOrder() == DataGrid.SortOrder.ASCENDING) ? "ASC" : "DESC";

        boolean filterQuantity = (quantityFilter == QuantityFilterState.FilterQuantity);
        boolean filterStock = (quantityFilter == QuantityFilterState.FilterStock);
        try {
            mCursor = mDocSale.mDocDetails.getItemsCursor(sortColumn, sortOrder, mCurrentGroup, filterQuantity, filterStock, upper, docGridCondition);
        } catch (Exception ex) {
            MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionDisplay) + "4.21");
            ErrorHandler.CatchError("Exception in DocSaleForm.fillGridWithData", ex);
        }
        // try {
        docSaleTableModel = getDocSaleTableDataModel(mCursor);


        return mCursor;
        //startManagingCursor(mCursor); //do not use startManagingCursor because it could be emptied when application comes to background
    }


    private DocSaleTableModel getDocSaleTableDataModel(Cursor mCursor) {
        DocSaleTableModel docSaleTableModel = new DocSaleTableModel();
        //docSaleTableModel.setChannelCountColumnIdx(mCursor.getColumnIndex("ChannelCount"));
        docSaleTableModel.setListStID(mCursor.getColumnIndex("ListStID"));
        docSaleTableModel.setListingStID(mCursor.getColumnIndex("ListingStID"));
        docSaleTableModel.setStyleIDColumnIdx(mCursor.getColumnIndex("StyleID"));
        docSaleTableModel.setItemTypeStyleIDColumnIdx(mCursor.getColumnIndex("ItemTypeStyleID"));

        docSaleTableModel.setItemIdIdx(mCursor.getColumnIndex("ItemID"));
        docSaleTableModel.setParentItemIdIdx(mCursor.getColumnIndex("ParentID"));
        docSaleTableModel.setCountSubIdx(mCursor.getColumnIndex("CountSub"));

        docSaleTableModel.setSaledQuantityColumnIdx(mCursor.getColumnIndex("SaledQuantity")); //TODO settings
        docSaleTableModel.setPrevMonthQntColumnIdx(mCursor.getColumnIndex("PrevMonthQnt")); //TODO settings
        docSaleTableModel.setPrevMonth2QntColumnIdx(mCursor.getColumnIndex("PrevMonth2Qnt")); //TODO settings

        docSaleTableModel.setProductSaledQuantityColumnIdx(mCursor.getColumnIndex("ProductSaledQuantity")); //TODO settings;
        docSaleTableModel.setProductPrevMonthQntColumnIdx(mCursor.getColumnIndex("ProductPrevMonthQnt")); //TODO settings;
        docSaleTableModel.setProductPrevMonth2QntColumnIdx(mCursor.getColumnIndex("ProductPrevMonth2Qnt")); //TODO settings;

        return docSaleTableModel;
    }

    private void setCursorToGrid(DataGrid grid, Cursor mCursor, DocSaleTableModel docSaleTableModel, DocSale mDocSale, boolean visibility) {
        //
        //for predicted order step, need to make corresponding column always visible
        //
        //int predictedOrderGroupID = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_DOC_STEP_PREV_ORDER, -1);
        int columnIdx = grid.getColumns().getColumnIndexByDbField("PredictedOrder");
        //grid.setColumnVisibility(columnIdx, mCurrentGroup.id == predictedOrderGroupID );
        grid.setColumnVisibility(columnIdx, visibility);


        int itemIdIdx = docSaleTableModel.getItemIdIdx();
        grid.setIdentityColumn(itemIdIdx);
        grid.setCursor(mCursor);

        //apply predict orders values if needed
        if (applyPredictOrders) {
            applyPredictOrders = false;
            mDocSale.mDocDetails.fillOrdersFromPredictedOrder(grid, mCursor);

            mDocSale.mDocDetails.calculateTotals(true);
            mDocSale.mDocHeader.onSumChanged();
        }

        //set proper value of curItemId
        if (mCursor != null && mCursor.getCount() > 0 && mCursor.moveToPosition(0))
            AntContext.getInstance().curItemId = mCursor.getInt(docSaleTableModel.getItemIdIdx());
        else
            AntContext.getInstance().curItemId = 0;
    }

    //--------------------------------------------------------------
    private class UpdateGridTask extends AsyncTask<Void, Void, Boolean> {
        private boolean preserveGridPosition = false;
        private boolean restoreGridState = false;
        int selectedRow = 0;
        int offsetFromTop = 0;
        int selectedItemId = 0;
        private GridModel gridModel;
        private DataGrid mGrid;
        private Cursor mCursor;


        public UpdateGridTask(boolean preserveGridPosition, boolean restoreGridState, GridModel gridModel) {
            this.gridModel = gridModel;
            this.preserveGridPosition = preserveGridPosition;
            this.restoreGridState = restoreGridState;
            this.mGrid = gridModel.mGrid;
            this.mCursor = gridModel.mCursor;
            saveGridPos(mGrid);
        }

        public void saveGridPos(DataGrid grid) {
            if (preserveGridPosition && grid != null) {
                selectedRow = grid.getSelectedRow();
                int scrollPos = grid.getScrollPos();
                offsetFromTop = selectedRow - scrollPos;
                if (mCursor.moveToPosition(selectedRow))
                    selectedItemId = mCursor.getInt(docSaleTableModel.getItemIdIdx());
            }
        }

        private boolean checkItemAtPosition(int pos, int selectedItemId, DataGrid grid, DocSaleTableModel docSaleTableModel) {
            if (mCursor.moveToPosition(pos)) {
                int itemId = mCursor.getInt(docSaleTableModel.getItemIdIdx());
                if (itemId == selectedItemId) {
                    grid.setSelectedRow(pos);
                    grid.scrollToPos(pos - offsetFromTop);
                    return true;
                }
            }

            return false;
        }


        public void restoreGridPos(Cursor mCursor, DataGrid mGrid) {
            if (!preserveGridPosition || mCursor == null || mGrid == null)
                return;

            int i = selectedRow;
            int j = selectedRow + 1;

            boolean iOk = true, jOk = true;
            do {
                iOk = (i >= 0 && i < mCursor.getCount());
                if (iOk) {
                    if (checkItemAtPosition(i, selectedItemId, mGrid, docSaleTableModel))
                        break;
                    i--;
                }

                jOk = (j >= 0 && j < mCursor.getCount());
                if (jOk) {
                    if (checkItemAtPosition(j, selectedItemId, mGrid, docSaleTableModel))
                        break;
                    j++;
                }
            }
            while (iOk || jOk);
        }

        public void restoreGridState(DataGrid grid) {
            if (!restoreGridState)
                return;

            //??????????????? ????????? ?????, ???? ??? ???? ????????? ?????
            if (grid != null && gridStates.containsKey(mCurrentGroup.id))
                grid.restoreGridState(gridStates.get(mCurrentGroup.id));
        }

        @Override
        protected void onPreExecute() {
            if (mCursor != null)    //close previous cursor to free data
            {
                mCursor.close();
                mCursor = null;
            }

            showDialog(IDD_PROGRESS_SPINNER_DIALOG);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean result = false;
            try {
                gridModel.mCursor = fillCursor(gridModel.mGrid, gridModel.mDocSale, true, null);
            } catch (Exception ex) {
                result = true; //true means error
                ErrorHandler.CatchError("Exception in UpdateGridTask.doInBackground", ex);
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result == true) //error
            {
                MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionDisplay) + "2");
                removeDialog(IDD_PROGRESS_SPINNER_DIALOG);
            } else {
                try {
                    setCursorToGrid(gridModel.mGrid, gridModel.mCursor, docSaleTableModel, gridModel.mDocSale, true);
                    /*if(infoPanel!=null)
                             infoPanel.displayTotals();*/

                    restoreGridPos(gridModel.mCursor, gridModel.mGrid);
                    restoreGridState(gridModel.mGrid);
                } catch (Exception ex) {
                    MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionDisplay) + "3");
                    ErrorHandler.CatchError("Exception in UpdateGridTask.doInBackground", ex);
                } finally {
                    removeDialog(IDD_PROGRESS_SPINNER_DIALOG);
                }
            }
        }
    }

    //--------------------------------------------------------------
    private Cursor fillGridWithData(boolean displayProgress, boolean resetExpandedItem, boolean preserveGridPosition, boolean restoreGridState, GridModel gridModel, boolean visibility) {
        Cursor mCursor = null;
        DocSale mDocSale = gridModel.mDocSale;
        DataGrid grid = gridModel.mGrid;
        if (resetExpandedItem)


            if (restoreGridState == false)
                gridStates.clear();

        int gridPos = grid.getSelectedRow();

        UpdateGridTask updateGridTask = new UpdateGridTask(preserveGridPosition, restoreGridState, gridModel);

        if (displayProgress == false) {
            updateGridTask.saveGridPos(grid);
            mCursor = fillCursor(grid, mDocSale, visibility, filterCondition);
            setCursorToGrid(grid, mCursor, docSaleTableModel, mDocSale, visibility);
            updateGridTask.restoreGridPos(mCursor, grid);
            updateGridTask.restoreGridState(grid);


        } else {
            updateGridTask.execute();
        }
        return mCursor;
    }

    //--------------------------------------------------------------
    private class CellListener extends DataGrid.BaseCellListener {
        private GridModel gridModel;
        private DataGrid mGrid;
        private Cursor mCursor;
        private DocSale mDocSale;
        private CursorData cursorData;

        private CellListener(GridModel gridModel) {

            this.gridModel = gridModel;
            mGrid = gridModel.mGrid;
            mCursor = gridModel.mCursor;
            mDocSale = gridModel.mDocSale;
            cursorData = gridModel.cursorData;

        }

        @Override
        public void onCellSelected(int row, int column) {
            setupAction();
            refreshModel();
           // try {
                mRowChanged = true;
                String columnName = mGrid.getDbFieldForColumn(column);
                if (columnName.equals("ScreenName")) {
                    if (gridModel == DocSaleForm.this.gridModelUp)
                        if (adjasmentOn) {
                            int itemIDidx = mCursor.getColumnIndex("ItemID");
                            if (mCursor.moveToPosition(row)) {
                                final Integer item = mCursor.getInt(itemIDidx);
                                final String filter = (bookingOn) ? filterBooking : filterCondition;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshGridAsync(gridModelDown, filter, bookingOn, false, item.toString());
                                    }
                                }).start();

                            }
                        } else {
                            if (mCursor.moveToPosition(row)) {
                                int brandID = mCursor.getColumnIndex("BrandID");
                                int brand = mCursor.getInt(brandID);
                                for (int i = 0; i < gridModelDown.mCursor.getCount(); i++) {
                                    if (gridModelDown.mCursor.moveToPosition(i))
                                        if (brand == gridModelDown.mCursor.getInt(brandID)) {
                                            DocSaleForm.this.gridModelDown.mGrid.scrollToPos(i);
                                            break;
                                        }
                                }
                            }
                        }
                }
                if (columnName.equals("PredictedOrder")) {
                    if (gridModel.mCursor.moveToPosition(row)) {
                        String cellValue = mGrid.getCellValue(row, column);
                        if (IOUtil.tryToParse(cellValue)) {
                            Integer newCellValue = new Integer(cellValue);
                            mGrid.setCellValue(row, 6, newCellValue);
                            int itemID = gridModel.mCursor.getInt(docSaleTableModel.getItemIdIdx());
                            mDocSale.mDocDetails.updateRowInDb(mGrid, itemID);
                        }
                        mDocSale.mDocHeader.onSumChanged();
                        mSum.setText(" Итого :" + Convert.moneyToString(gridModelUp.mDocSale.totalAll + gridModelDown.mDocSale.totalAll));
                        mOrders.setText(" Позиций :" +String.valueOf(gridModelUp.mDocSale.totalItems + gridModelDown.mDocSale.totalItems));
                    }


                }
                /*if (columnName.equals("SubItems")) {
                    if (mCursor.moveToPosition(row)) {
                        int countSub = mCursor.getInt(docSaleTableModel.getCountSubIdx());

                        if (countSub > 0) {
                            int itemId = mCursor.getInt(docSaleTableModel.getItemIdIdx());
                            AntContext.getInstance().curItemId = itemId;

                            int parentItemId = mCursor.getInt(docSaleTableModel.getParentItemIdIdx());
                            if (parentItemId == itemId)    //filter out childe items
                            {

                                fillGridWithData(true, false, true, false, gridModel, false);
                            }
                        }
                    }
                } else {
                    if (columnName.equals("ScreenName") && mCursor.moveToPosition(row)) {
                        AntContext.getInstance().curItemId = mCursor.getInt(mCursor.getColumnIndex("ItemID"));

                        loadInfoPanel(true);

                        if (infoPanel != null)
                            infoPanel.displayTotals();
                    } else {
                        if (gridModel.mCursor.moveToPosition(row))
                            AntContext.getInstance().curItemId = gridModel.mCursor.getInt(docSaleTableModel.getItemIdIdx());

                        loadInfoPanel(false);

                        if (infoPanel != null)
                            infoPanel.displayTotals();
                    }
                }*/
                saveChanges();


        }

        private void setupAction() {
            Button button1 = (Button) findViewById(R.id.buttonOne);
            Button button2 = (Button) findViewById(R.id.buttonTwo);
            Button button3 = (Button) findViewById(R.id.buttonThree);
            Button button4 = (Button) findViewById(R.id.buttonFour);
            Button button5 = (Button) findViewById(R.id.buttonFive);
            Button button6 = (Button) findViewById(R.id.buttonSix);
            Button button7 = (Button) findViewById(R.id.buttonSeven);
            Button button8 = (Button) findViewById(R.id.buttonEight);
            Button button9 = (Button) findViewById(R.id.buttonNine);
            Button button0 = (Button) findViewById(R.id.buttonZero);
            Button buttonC = (Button) findViewById(R.id.buttonClear);

            if (useQuickKeyboard) {
                DigitClickListener digitClickListener = new DigitClickListener(denyMoreThanRest, mCursor, mDocSale, mGrid, cursorData);

                button1.setOnClickListener(digitClickListener);
                button2.setOnClickListener(digitClickListener);
                button3.setOnClickListener(digitClickListener);
                button4.setOnClickListener(digitClickListener);
                button5.setOnClickListener(digitClickListener);
                button6.setOnClickListener(digitClickListener);
                button7.setOnClickListener(digitClickListener);
                button8.setOnClickListener(digitClickListener);
                button9.setOnClickListener(digitClickListener);
                button0.setOnClickListener(digitClickListener);
                buttonC.setOnClickListener(digitClickListener);
            } else {
                button1.setVisibility(View.GONE);
                button2.setVisibility(View.GONE);
                button3.setVisibility(View.GONE);
                button4.setVisibility(View.GONE);
                button5.setVisibility(View.GONE);
                button6.setVisibility(View.GONE);
                button7.setVisibility(View.GONE);
                button8.setVisibility(View.GONE);
                button9.setVisibility(View.GONE);
                button0.setVisibility(View.GONE);
                buttonC.setVisibility(View.GONE);
            }
        }

        @Override
        public void onCellLongPress(int row, int column) {
            refreshModel();
            try {
                String columnName = mGrid.getDbFieldForColumn(column);

                if (columnName.equals("ScreenName") && mCursor.moveToPosition(row)) {
                    AntContext.getInstance().curItemId = mCursor.getInt(docSaleTableModel.getItemIdIdx());
                    showDialog(IDD_ITEM);
                }
            } catch (Exception ex) {
                MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionOnCreateDialog));
                ErrorHandler.CatchError("Exception in onCellLongPress", ex);
            }
        }

        @Override
        public void onCellEdit(int row, int column, int dataType) {
            if (mDocSale.isEditable) {
                cursorData.mEditedRow = row;
                cursorData.mEditedColumn = column;
                cursorData.mEditedDataType = dataType;

                //check if discount could be edited
                ColumnInfo columnInfo = mGrid.getColumns().getColumn(cursorData.mEditedColumn);

                String dbField = columnInfo.getDbField(); //mGrid.getDbFieldForColumn(mEditedColumn);			
                if (dbField.equals("DiscountI") || dbField.equals("DiscountII")) {
                    if (Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_DENY_EDIT_DISCOUNT) != 0) {
                        MessageBox.show(DocSaleForm.this, getResources().getString(R.string.doc_sale_limitation), getResources().getString(R.string.doc_sale_denyEditDiscount));
                        return;
                    }
                } else if (dbField.equals("OrdersI") || dbField.equals("OrdersII")) {
                    if (useQuickKeyboard) return;
                }

                if (columnInfo.getDataType() == DataGrid.DATA_TYPE_DATE)
                    showDialog(IDD_EDIT_DATE_DIALOG);
                else
                    showDialog(IDD_CALCULATOR);
            }
        }

        @Override
        public void onHeaderClicked(int column, boolean sortOrderChanged) {
            //header click implies changes in sort order. we need to re-read Cursor and set it to DataGrid
            if (sortOrderChanged)
                fillGridWithData(true, true, false, false, gridModel, false);
        }

        double calculatePrice(String discountField, Object[] values) {
            //calculate prices "Price*((100-ISNULL(DiscountI, 0))/100)*(1 + VATID)"
            refreshModel();
            int price1 = mCursor.getColumnIndex("Price");
            double price = mCursor.getDouble(price1);
            double vatId = mCursor.getDouble(mCursor.getColumnIndex("VATID"));

            double discount = 0;
            if (values != null) {
                ColumnInfo discountColumn = mGrid.getColumns().getColumnByDbField(discountField);
                if (discountColumn != null && discountColumn.isEditable()) {
                    int discountIdx = discountColumn.getEditableIndex();
                    Object discountObj = values[discountIdx];

                    if (discountObj != null)
                        discount = (Double) discountObj;
                    else
                        discount = mCursor.getDouble(mCursor.getColumnIndex(discountField));
                } else
                    discount = mCursor.getDouble(mCursor.getColumnIndex(discountField));
            } else
                discount = mCursor.getDouble(mCursor.getColumnIndex(discountField));

            double priceCalculated = price * ((100 - discount) / 100) * (1 + vatId);
            saveChanges();
            return priceCalculated;
        }

        @Override
        public Object onCellCalculate(int row, int column, Object[] values) {
            Object retValue = null;
            refreshModel();
            try {
                String columnName = mGrid.getDbFieldForColumn(column);

                if (columnName.equals("RowSumI") || columnName.equals("RowSumII")) {
                    if (values != null) {
                        //calculate orders
                        String ordersField = (columnName.equals("RowSumI")) ? "OrdersI" : "OrdersII";
                        ColumnInfo ordersColumn = mGrid.getColumns().getColumnByDbField(ordersField);
                        int orders = Convert.toInt(mGrid.getCellValue(row, ordersColumn), 0);

                        //calculate price
                        String discountField = (columnName.equals("RowSumI")) ? "DiscountI" : "DiscountII";
                        double priceCalculated = calculatePrice(discountField, values);
                        retValue = Convert.roundUpMoney(priceCalculated * orders);
                    } else
                        retValue = null;
                } else if (columnName.equals("PriceI") || columnName.equals("PriceII")) {
                    String discountField = (columnName.equals("PriceI")) ? "DiscountI" : "DiscountII";
                    double priceCalculated = calculatePrice(discountField, values);
                    retValue = Convert.roundUpMoney(priceCalculated);
                } else if (columnName.equals("Quantity")) {
                    //if((quantity+oldOrders)<(orders+moreOrders))
                    int quantity = mCursor.getInt(mCursor.getColumnIndex("Quantity"));

                    if (mDocSale.mDocHeader.docType == Document.DOC_TYPE_SALE) {
                        ColumnInfo ordersIColumn = mGrid.getColumns().getColumnByDbField("OrdersI");
                        ColumnInfo ordersIIColumn = mGrid.getColumns().getColumnByDbField("OrdersII");

                        int ordersI = Convert.toInt(mGrid.getCellValue(row, ordersIColumn), 0);
                        int ordersII = Convert.toInt(mGrid.getCellValue(row, ordersIIColumn), 0);

                        int oldOrders = 0;

                        if (mDocSale.mDocHeader.docTypePrev == Document.DOC_TYPE_SALE)
                            oldOrders = mCursor.getInt(mCursor.getColumnIndex("Orders"));

                        retValue = quantity + oldOrders - (ordersI + ordersII);
                    } else
                        retValue = quantity;
                }
                saveChanges();
            } catch (Exception ex) {
                ErrorHandler.CatchError("Exception in onCellCalculate", ex);
            }

            return retValue;
        }

        private void saveChanges() {
            gridModel.mGrid = mGrid;
            gridModel.mCursor = mCursor;
            gridModel.mDocSale = mDocSale;
        }

        private void refreshModel() {
            mGrid = gridModel.mGrid;
            mCursor = gridModel.mCursor;
            mDocSale = gridModel.mDocSale;
            cursorData = gridModel.cursorData;
        }

        @Override
        public CellStyle onCalculateRowStyle() {
            // Styles are applied using following rule:
            // 1. Style for unsaled items have the outmost priority
            // 2. Then goes PowerSCU style
            // 3. Style taken from StyleID column of CurDocDetails is set when above styles were not applied

            try {
                ArrayList<Integer> appliedStyles = new ArrayList<Integer>();

                /*  //collect style IDs to array
                int unsaledPowerSKUStyle = (int) Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_STYLE_NOT_SOLD_POWER_SKU, -1);
                int powerSKUStyle = (int) Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_STYLE_POWER_SKU, -1);
                int notSoldStyle = (int) Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_STYLE_NOT_SOLD, 13);

                int channelCount = mCursor.isNull(docSaleTableModel.getChannelCountColumnIdx()) ? 0 : mCursor.getInt(docSaleTableModel.getChannelCountColumnIdx());

                int productSaledQuantity = mCursor.isNull(docSaleTableModel.getProductSaledQuantityColumnIdx()) ? 0 : mCursor.getInt(docSaleTableModel.getProductSaledQuantityColumnIdx());
                int productPrevMonthQntColumnQnt = mCursor.isNull(docSaleTableModel.getProductPrevMonthQntColumnIdx()) ? 0 : mCursor.getInt(docSaleTableModel.getProductPrevMonthQntColumnIdx());
                int productPrevMonth2QntColumnQnt = mCursor.isNull(docSaleTableModel.getProductPrevMonth2QntColumnIdx()) ? 0 : mCursor.getInt(docSaleTableModel.getProductPrevMonth2QntColumnIdx());

                if (unsaledPowerSKUStyle != -1) {
                    if ((productSaledQuantity + productPrevMonthQntColumnQnt + productPrevMonth2QntColumnQnt) == 0 && channelCount != 0)
                        appliedStyles.add(unsaledPowerSKUStyle);
                }

                if (powerSKUStyle != -1) {
                    if (channelCount != 0)
                        appliedStyles.add(powerSKUStyle);
                }

                int itemTypeStyle = mCursor.isNull(docSaleTableModel.getItemTypeStyleIDColumnIdx()) ? -1 : mCursor.getInt(docSaleTableModel.getItemTypeStyleIDColumnIdx());
                if (itemTypeStyle != -1)
                    appliedStyles.add(itemTypeStyle);

                int itemStyle = mCursor.isNull(docSaleTableModel.getStyleIDColumnIdx()) ? -1 : mCursor.getInt(docSaleTableModel.getStyleIDColumnIdx());
                if (itemStyle != -1)
                    appliedStyles.add(itemStyle);

                //add NOT SOLD style if applicable
                if (productSaledQuantity == 0 && (productPrevMonthQntColumnQnt + productPrevMonth2QntColumnQnt) > 0) {
                    appliedStyles.add(notSoldStyle);
                }*/
                docSaleTableModel = getDocSaleTableDataModel(mCursor);
                int listingStID = docSaleTableModel.getListingStID();
                if (mCursor.isNull(listingStID)) {
                    int anInt = mCursor.getInt(listingStID);
                    appliedStyles.add(anInt);
                }
                int listStID = docSaleTableModel.getListStID();
                int anInt1 = 0;
                if (!mCursor.isNull(listStID)) {
                    anInt1 = mCursor.getInt(listStID);
                    appliedStyles.add(anInt1);
                }
                //create composite style for all IDs in array
                CellStyleCollection styles = AntContext.getInstance().getStyles();
                CellStyle style = styles.getCompositeStyle(appliedStyles);

                style.setExpandedImageIndex(-1);

                return style;

            } catch (Exception ex) {
                ErrorHandler.CatchError("Exception in DocSaleForm.onCalculateRowStyle", ex);
            }

            return CellStyleCollection.getDefault();
        }

        @Override
        public CellStyle onCalculateColumnStyle(CellStyle currentStyle, ColumnInfo column) {
            if (column.getDbField().equals("OrdersI") || column.getDbField().equals("OrdersII")) {
                CellStyle columnStyle = (CellStyle) currentStyle.clone();
                columnStyle.fontStyle |= CellStyle.TEXT_STYLE_BOLD;
                return columnStyle;
            }

            return currentStyle;
        }

        @Override
        public Bitmap onGetImage(int imageIndex) {
            Bitmap img = AntContext.getInstance().getCachedImage(DocSaleForm.this, imageIndex);
            return img;
        }
    }

    //--------------------------------------------------------------
    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        super.onPrepareDialog(id, dialog);

        switch (id) {
            case IDD_DATE_DIALOG:
                ((DatePickerDialog) dialog).updateDate(mTempDocSaleHeader.docDate.get(Calendar.YEAR),
                        mTempDocSaleHeader.docDate.get(Calendar.MONTH),
                        mTempDocSaleHeader.docDate.get(Calendar.DAY_OF_MONTH));
                break;
            default:
                return;
        }

    }

    //--------------------------------------------------------------    
    private void moveBetweenForms(boolean moveItoII) {
        final boolean moveItoII_copy = moveItoII;

        try {
            int messageResId = moveItoII ? R.string.doc_sale_moveFormsMessageItoII : R.string.doc_sale_moveFormsMessageIItoI;

            //
            //Display alert dialog proposing to save a document
            //
            AlertDialog alertDialog;
            alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getResources().getString(R.string.doc_sale_moveFormsHeader));
            alertDialog.setMessage(getResources().getString(messageResId));

            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.doc_sale_moveFormsChoiceOk),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                gridModelUp.mDocSale.mDocDetails.moveBetweenForms(moveItoII_copy, gridModelUp.mGrid);
                                gridModelUp.mGrid.invalidate();

                                gridModelUp.mDocSale.mDocDetails.calculateTotals(true);
                                gridModelUp.mDocSale.mDocHeader.onSumChanged();
                                /*if(infoPanel!=null)
                                    infoPanel.displayTotals();*/
                            } catch (Exception ex) {
                                MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionAction));
                                ErrorHandler.CatchError("Exception in DocSaleForm.moveBetweenForms", ex);
                            }
                        }
                    });

            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.doc_sale_moveFormsChoiceCancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

            alertDialog.show();
        } catch (Exception ex) {
            MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionAction));
            ErrorHandler.CatchError("Exception in DocSaleForm.moveBetweenForms", ex);
        }
    }

    //--------------------------------------------------------------
    private double getMaxDiscount(int priceColumn, int minPriceColumn, Cursor mCursor) {
        double price = mCursor.getDouble(priceColumn);
        double minPrice = mCursor.getDouble(minPriceColumn);
        double maxDiscount = (((price > minPrice) ? (price - minPrice) : 0) / price) * 100;

        return maxDiscount;
    }

    //--------------------------------------------------------------    
    @Override
    protected Dialog onCreateDialog(int id) {
       // try {
            switch (id) {
                case IDD_COMMENTS_DIALOG: {
                    return createCommentDialog();
                }
                case IDD_RESPITE_DIALOG: {
                    return createRespiteDialog();
                }
                case IDD_CALCULATOR: {
                    return createCalculatorDialog();
                }
                /*case IDD_STATS:
                    {
                        statsDlg = new DocSaleStatsDialog();
                        Dialog dlg = statsDlg.onCreate(this);
        
                        statsDlg.setOkClickListener(new DialogInterface.OnClickListener() 
                        {					
                            @Override public void onClick(DialogInterface dialog, int which) { removeDialog(IDD_STATS); }
                        });    			
                        
                        return dlg;    			
                    }*/
                case IDD_ACTIONS: {
                    return createActionsDialog();

                }
                case IDD_ITEM: {
                    itemDlg = new ItemDialog();
                    Dialog dlg = itemDlg.onCreate(this);

                    itemDlg.setOkClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeDialog(IDD_ITEM);
                        }
                    });

                    return dlg;
                }

                case IDD_SELECT_COLUMNS: {
                    selectColumnsDlg = new DocSaleSelectColumnsDialog();

                    Dialog dlg = null;

                    GridColumns columns = null;
                    try {
                        columns = gridModelUp.mGrid.getColumns().clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();  
                    }

                    if (columns != null) {
                        dlg = selectColumnsDlg.onCreate(this, columns);
                        final GridColumns columns1 = columns;
                        selectColumnsDlg.setOkClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    columns1.save();
                                    gridModelUp.mGrid.replaceColumns(columns1);
                                    removeDialog(IDD_SELECT_COLUMNS);
                                } catch (Exception ex) {
                                    MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionEditColumns));
                                    ErrorHandler.CatchError("Exception in DocSaleForm.onSelectColumnsOk", ex);
                                }
                            }
                        });

                        selectColumnsDlg.setCancelClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeDialog(IDD_SELECT_COLUMNS);
                            }
                        });
                    }
                    return dlg;
                }
                case IDD_SELECT_ITEM_GROUP: {
                    DialogInterface.OnClickListener cancelClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeDialog(IDD_SELECT_ITEM_GROUP);
                        }
                    };

                    selectGroupDlg = new DocSaleSelectGroupDialog();
                    Dialog dlg = selectGroupDlg.onCreate(this, cancelClickListener, true, gridModelUp.mDocSale.docId, itemGroupSelector, mCurrentGroup);

                    return dlg;
                }
                case IDD_DATE_DIALOG:
                    DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                        public void onDateSet(DatePicker view, int year, int monthOfYear,
                                              int dayOfMonth) {
                            try {
                                removeDialog(IDD_DATE_DIALOG);

                                mTempDocSaleHeader.docDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                mTempDocSaleHeader.docDate.set(Calendar.MONTH, monthOfYear);
                                mTempDocSaleHeader.docDate.set(Calendar.YEAR, year);
                                showDialog(IDD_DOC_SALE_HEADER_DIALOG);

                                //updateDateDisplay();		                            
                                //mDocSale.mDocHeader.onDateChanged();
                            } catch (Exception ex) {
                                MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionDisplay) + "7");
                                ErrorHandler.CatchError("Exception in DocSaleForm.onDateSet", ex);
                            }
                        }
                    };

                    DatePickerDialog dateDlg = new DatePickerDialog(this, mDateSetListener, mTempDocSaleHeader.docDate.get(Calendar.YEAR),
                            mTempDocSaleHeader.docDate.get(Calendar.MONTH),
                            mTempDocSaleHeader.docDate.get(Calendar.DAY_OF_MONTH));

                    dateDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_NEGATIVE) {
                                removeDialog(IDD_DATE_DIALOG);
                                showDialog(IDD_DOC_SALE_HEADER_DIALOG);
                            }
                        }
                    });

                    return dateDlg;

                case IDD_EDIT_DATE_DIALOG:
                    return createEditDateDialog();

                case IDD_PROGRESS_SPINNER_DIALOG:
                    ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setMessage(getResources().getString(R.string.doc_sale_progressMessage));
                    dialog.setIndeterminate(true);
                    dialog.setCancelable(false);
                    dialog.show();
                    return dialog;

                case IDD_CONTACT_POPUP: {
                    ContactAddDialog contactDialog = new ContactAddDialog();

                    Contact contact = null;
                    Dialog dlg = contactDialog.onCreate(this, contact);

                    contactDialog.setContactSubmitListener(new ContactAddDialog.OnContactSubmitListener() {
                        @Override
                        public void onContactSubmit(Contact contact) {
                            try {
                                //finish dialog
                                removeDialog(IDD_CONTACT_POPUP);
                                mTempDocSaleHeader.contactID = contact.contactID;
                                showDialog(IDD_DOC_SALE_HEADER_DIALOG);
                            } catch (Exception ex) {
                                MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_contacts), getResources().getString(R.string.addr_contact_exceptionOnEdit));
                                ErrorHandler.CatchError("Exception in DocSaleForm.onContactSubmit", ex);
                            }
                        }
                    });

                    contactDialog.setCancelClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeDialog(IDD_CONTACT_POPUP);
                            mTempDocSaleHeader.contactID = mTempDocSaleHeader.prevContactID;
                            showDialog(IDD_DOC_SALE_HEADER_DIALOG);
                        }
                    });

                    return dlg;
                }

                case IDD_DOC_SALE_HEADER_DIALOG: {
                    return createDocSaleHeaderDialog();

                }

                default:
                    return null;
            }
      /*  } catch (Exception ex) {
            MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionOnCreateDialog));
            ErrorHandler.CatchError("Exception in onCreateDialog", ex);
        }*/


    }

    private Dialog createEditDateDialog() {
        DatePickerDialog.OnDateSetListener mEditDateSetListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                try {
                    removeDialog(IDD_EDIT_DATE_DIALOG);

                    Calendar date = Calendar.getInstance();
                    date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    date.set(Calendar.MONTH, monthOfYear);
                    date.set(Calendar.YEAR, year);
                    String strDate = Convert.getSqlDateTimeFromCalendar(date);

                    gridModelUp.mGrid.setCellValue(gridModelUp.cursorData.mEditedRow, gridModelUp.cursorData.mEditedColumn, strDate);
                    if (gridModelUp.mCursor.moveToPosition(gridModelUp.cursorData.mEditedRow)) {
                        int itemId = gridModelUp.mCursor.getInt(docSaleTableModel.getItemIdIdx());
                        gridModelUp.mDocSale.mDocDetails.updateRowInDb(gridModelUp.mGrid, itemId);
                    }
                } catch (Exception ex) {
                    MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionDisplay) + "8");
                    ErrorHandler.CatchError("Exception in DocSaleForm.onDateSet", ex);
                }
            }
        };

        String oldValue = "";
        Calendar date = Calendar.getInstance();

        if (gridModelUp.mCursor.moveToPosition(gridModelUp.cursorData.mEditedRow)) {
            oldValue = gridModelUp.mGrid.getCellValue(gridModelUp.cursorData.mEditedRow, gridModelUp.cursorData.mEditedColumn);
            if (oldValue != null && oldValue.length() > 0) {
                try {
                    date = Convert.getDateFromString(oldValue);
                } catch (Exception ex) {
                }
            }
        }

        DatePickerDialog editDateDlg = new DatePickerDialog(this, mEditDateSetListener, date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH));

        editDateDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(IDD_EDIT_DATE_DIALOG);
            }
        });

        return editDateDlg;
    }

    private Dialog createDocSaleHeaderDialog() {
        DocSaleHeaderDialog headerDialog = new DocSaleHeaderDialog();
        Dialog dlg = headerDialog.onCreate(this, gridModelUp.mDocSale.isEditable, mTempDocSaleHeader);

        headerDialog.setDocSaleHeaderPopupListener(new DocSaleHeaderDialog.OnDocSaleHeaderPopupListener() {
            @Override
            public void onDocSaleHeaderPopup(DocSaleHeaderPopup whichDialog) {
                overridePendingTransition(0, 0);
                removeDialog(IDD_DOC_SALE_HEADER_DIALOG);

                switch (whichDialog) {
                    case Date:
                        showDialog(DocSaleForm.IDD_DATE_DIALOG);
                        break;
                    case Respite:
                        showDialog(DocSaleForm.IDD_RESPITE_DIALOG);
                        break;
                    case Comments1:
                        commentIdx = 1;
                        showDialog(DocSaleForm.IDD_COMMENTS_DIALOG);
                        break;
                    case Comments2:
                        commentIdx = 2;
                        showDialog(DocSaleForm.IDD_COMMENTS_DIALOG);
                        break;
                    case Contact:
                        showDialog(DocSaleForm.IDD_CONTACT_POPUP);
                        break;
                }
            }
        });

        headerDialog.setOkClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(IDD_DOC_SALE_HEADER_DIALOG);

                try {
                    boolean priceChanged = (gridModelUp.mDocSale.mDocHeader.price.id != mTempDocSaleHeader.price.id);
                    boolean addressChanged = (gridModelUp.mDocSale.mDocHeader.addrID != mTempDocSaleHeader.addrID);

                    //save edited data
                    gridModelUp.mDocSale.mDocHeader.docType = mTempDocSaleHeader.docType;
                    gridModelUp.mDocSale.mDocHeader.docDate = mTempDocSaleHeader.docDate;
                    gridModelUp.mDocSale.mDocHeader.respite = mTempDocSaleHeader.respite;
                    gridModelUp.mDocSale.mDocHeader.price = mTempDocSaleHeader.price;
                    gridModelUp.mDocSale.mDocHeader.comments1 = mTempDocSaleHeader.comments1;
                    gridModelUp.mDocSale.mDocHeader.comments2 = mTempDocSaleHeader.comments2;
                    gridModelUp.mDocSale.mDocHeader.specMarks1 = mTempDocSaleHeader.specMarks1;
                    gridModelUp.mDocSale.mDocHeader.specMarks2 = mTempDocSaleHeader.specMarks2;
                    gridModelUp.mDocSale.mDocHeader.contactID = mTempDocSaleHeader.contactID;
                    gridModelUp.mDocSale.mDocHeader.addrID = mTempDocSaleHeader.addrID;

                    gridModelUp.mDocSale.mDocHeader.onDocTypeChanged();
                    updateDocTypeDisplay();

                    gridModelUp.mDocSale.mDocHeader.onDateChanged();
                    updateDateDisplay();

                    gridModelUp.mDocSale.mDocHeader.onRespiteChanged();
                    updateRespiteDisplay();

                    if (priceChanged) {
                        gridModelUp.mDocSale.onPriceIdChanged();
                        updatePriceDisplay();

                        gridModelUp.mDocSale.mDocDetails.calculateTotals(true);
                    }

                    gridModelUp.mDocSale.mDocHeader.onComments1Changed();
                    gridModelUp.mDocSale.mDocHeader.onComments2Changed();
                    updateCommentsDisplay();

                    gridModelUp.mDocSale.mDocHeader.onContactIDChanged();

                    gridModelUp.mDocSale.mDocHeader.onAddrIDChanged();

                    if (priceChanged || addressChanged)
                        gridModelUp.mCursor = fillGridWithData(true, true, false, false, gridModelUp, true);
                    else
                        gridModelUp.mGrid.invalidate();
                } catch (Exception ex) {
                    ErrorHandler.CatchError("Exception in DocSaleHeaderDialog.OkClickListener", ex);
                }
            }
        });

        headerDialog.setCancelClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(IDD_DOC_SALE_HEADER_DIALOG);
            }
        });

        return dlg;
    }

    private Dialog createActionsDialog() {
        //create dialog
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.doc_sale_actions, (ViewGroup) findViewById(R.id.docSaleActions));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        //builder.setMessage(getResources().getString(R.string.form_title_actions));

        //update discounts
        Button buttonUpdateDocDiscounts = (Button) layout.findViewById(R.id.buttonUpdateDocDiscounts);
        buttonUpdateDocDiscounts.setEnabled(gridModelUp.mDocSale.isEditable);

        //calculate predicted orders
        Button buttonCalcPredictOrders = (Button) layout.findViewById(R.id.buttonCalcPredictOrders);
        buttonCalcPredictOrders.setEnabled(gridModelUp.mDocSale.isEditable);

        //start column editor
        Button buttonSelectColumns = (Button) layout.findViewById(R.id.buttonSelectColumns);
        buttonSelectColumns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDialog(IDD_ACTIONS);
                showDialog(IDD_SELECT_COLUMNS);
            }
        });

        //move between forms
        Button buttonCopyItoII = (Button) layout.findViewById(R.id.buttonCopyItoII);
        buttonCopyItoII.setEnabled((gridModelUp.mDocSale.mDocHeader.docType == Document.DOC_TYPE_REMNANTS) ? false : gridModelUp.mDocSale.isEditable);
        buttonCopyItoII.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDialog(IDD_ACTIONS);
                moveBetweenForms(true);
            }
        });

        Button buttonCopyIItoI = (Button) layout.findViewById(R.id.buttonCopyIItoI);
        buttonCopyIItoI.setEnabled((gridModelUp.mDocSale.mDocHeader.docType == Document.DOC_TYPE_REMNANTS) ? false : gridModelUp.mDocSale.isEditable);
        buttonCopyIItoI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDialog(IDD_ACTIONS);
                moveBetweenForms(false);
            }
        });

        //finish document
        Button buttonFinishDocument = (Button) layout.findViewById(R.id.buttonDocComplete);
        buttonFinishDocument.setEnabled(gridModelUp.mDocSale.isEditable);
        buttonFinishDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDialog(IDD_ACTIONS);
                checkErrorsAndFinishDocument(false, false, false, gridModelUp.mDocSale, gridModelUp.mCursor);
            }
        });

        //print document
        Button buttonPrintDocument = (Button) layout.findViewById(R.id.buttonDocPrint);
        buttonPrintDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDialog(IDD_ACTIONS);
                checkErrorsAndFinishDocument(false, false, true, gridModelUp.mDocSale, gridModelUp.mCursor);
            }
        });


        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(IDD_ACTIONS);
            }
        });

        return builder.create();
    }

    private Dialog createCalculatorDialog() {
        calculatorDlg = new DocSaleCalculatorDialog();

        int calcFlags = DocSaleCalculatorDialog.FLAGS_NONE;
        String dbField = gridModelUp.mGrid.getDbFieldForColumn(gridModelUp.cursorData.mEditedColumn);
        if (dbField.equals("DiscountI") || dbField.equals("DiscountII"))
            calcFlags = DocSaleCalculatorDialog.FLAGS_SHOW_APPLY_TO_ALL_CHECKBOX;

        String oldValue = "";
        if (gridModelUp.mCursor.moveToPosition(gridModelUp.cursorData.mEditedRow))
            oldValue = gridModelUp.mGrid.getCellValue(gridModelUp.cursorData.mEditedRow, gridModelUp.cursorData.mEditedColumn);

        //oldValue = mCursor.getString(mCursor.getColumnIndex(dbField));

        //calc dialog title
        int titleResID = R.string.doc_sale_calc_title;
        if (dbField.equals("DiscountI"))
            titleResID = R.string.doc_sale_calc_titleDiscountI;
        else if (dbField.equals("DiscountII"))
            titleResID = R.string.doc_sale_calc_titleDiscountII;
        else if (dbField.equals("OrdersI"))
            titleResID = R.string.doc_sale_calc_titleOrdersI;
        else if (dbField.equals("OrdersII"))
            titleResID = R.string.doc_sale_calc_titleOrdersII;
        else if (dbField.equals("CurRemnant"))
            titleResID = R.string.doc_sale_calc_titleRemnant;

        String title = getResources().getString(titleResID);

        Dialog dlg = calculatorDlg.onCreate(this, title, calcFlags, oldValue, gridModelUp.cursorData.mEditedDataType);

        calculatorDlg.setCancelClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(IDD_CALCULATOR);
            }
        });

        calculatorDlg.setCalcResultListener(new DocSaleCalculatorDialog.OnCalcResultListener() {
            @Override
            public void onCalcResult(Object calcValue, boolean applyToAll) {
                //Log.d("DocSale.onCalcResult", "entering");
                try {
                    removeDialog(IDD_CALCULATOR);

                    Object calcValueObj = calcValue;
                    double doubleValue = 0;

                    if (gridModelUp.cursorData.mEditedDataType == DataGrid.DATA_TYPE_DOUBLE)
                        doubleValue = (Double) calcValue;

                    String dbField = gridModelUp.mGrid.getDbFieldForColumn(gridModelUp.cursorData.mEditedColumn);

                    if (dbField.equals("DiscountI") || dbField.equals("DiscountII")) {
                        //check that discount is under 100%
                        if (doubleValue >= 99.99) {
                            MessageBox.show(DocSaleForm.this, getResources().getString(R.string.doc_sale_limitation),
                                    getResources().getString(R.string.doc_sale_discountExceeds100));
                            return;
                        }

                        boolean checkMaxDiscount = (Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_CHECK_MAX_DISCOUNT) == 1);

                        int priceColumn = gridModelUp.mCursor.getColumnIndex("Price");
                        int minPriceColumn = gridModelUp.mCursor.getColumnIndex("MinPrice");

                        if (applyToAll) {
                            int countExceeds = 0;

                            for (int i = 0; i < gridModelUp.mCursor.getCount(); i++) {
                                //Log.d("DocSale.onCalcResult", "mCursor.moveToPosition"+i);
                                if (gridModelUp.mCursor.moveToPosition(i)) {
                                    if (checkMaxDiscount) {
                                        double discount = doubleValue;
                                        double maxDiscount = getMaxDiscount(priceColumn, minPriceColumn, gridModelUp.mCursor);
                                        if (discount > maxDiscount) {
                                            discount = maxDiscount;
                                            countExceeds++;
                                        }

                                        calcValueObj = discount;
                                    }
                                    gridModelUp.mGrid.setCellValue(i, gridModelUp.cursorData.mEditedColumn, calcValueObj);

                                    int itemId = gridModelUp.mCursor.getInt(docSaleTableModel.getItemIdIdx());
                                    gridModelUp.mDocSale.mDocDetails.updateRowInDb(gridModelUp.mGrid, itemId);
                                }
                            }

                            if (countExceeds > 0) {
                                String message = String.format(getResources().getString(R.string.doc_sale_maxDiscountNPositions), countExceeds);
                                MessageBox.show(DocSaleForm.this, getResources().getString(R.string.doc_sale_warning), message);
                            }

                            //Log.d("DocSale.onCalcResult", "move to pos 0");
                            //mCursor.moveToPosition(0);
                        } else {
                            if (checkMaxDiscount) {
                                if (gridModelUp.mCursor.moveToPosition(gridModelUp.cursorData.mEditedRow)) {
                                    double discount = doubleValue;
                                    double maxDiscount = getMaxDiscount(priceColumn, minPriceColumn, gridModelUp.mCursor);
                                    if (discount > maxDiscount) {
                                        discount = maxDiscount;
                                        String message = String.format(getResources().getString(R.string.doc_sale_maxDiscount), discount);
                                        MessageBox.show(DocSaleForm.this, getResources().getString(R.string.doc_sale_limitation), message);
                                    }

                                    calcValueObj = discount;
                                }
                            }
                        }
                    } else if (dbField.equals("OrdersI") || dbField.equals("OrdersII")) {
                        boolean denyMoreThanRest = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_DENY_ORDER_QNT_MORE_THAN_REST, 0) > 0;
                        if (gridModelUp.mDocSale.mDocHeader.docType == Document.DOC_TYPE_SALE || denyMoreThanRest) {
                            if (gridModelUp.mCursor.moveToPosition(gridModelUp.cursorData.mEditedRow)) {
                                int orders = (Integer) calcValue;
                                long oldOrders = gridModelUp.mCursor.getInt(gridModelUp.mCursor.getColumnIndex("Orders"));

                                String moreOrdersField = dbField.equals("OrdersI") ? "OrdersII" : "OrdersI";
                                ColumnInfo moreOrdersColumn = gridModelUp.mGrid.getColumns().getColumnByDbField(moreOrdersField);
                                int moreOrders = Convert.toInt(gridModelUp.mGrid.getCellValue(gridModelUp.cursorData.mEditedRow, moreOrdersColumn), 0);

                                int quantity = gridModelUp.mCursor.getInt(gridModelUp.mCursor.getColumnIndex("Quantity"));

                                if ((quantity + oldOrders) < (orders + moreOrders)) {
                                    MessageBox.show(DocSaleForm.this, getResources().getString(R.string.doc_sale_limitation),
                                            getResources().getString(R.string.doc_sale_ordersExceedsQuantity));
                                    return;
                                }
                            }
                        }
                    }


                    if (!applyToAll) {
                        gridModelUp.mGrid.setCellValue(gridModelUp.cursorData.mEditedRow, gridModelUp.cursorData.mEditedColumn, calcValueObj);
                        if (gridModelUp.mCursor.moveToPosition(gridModelUp.cursorData.mEditedRow)) {
                            int itemId = gridModelUp.mCursor.getInt(docSaleTableModel.getItemIdIdx());
                            gridModelUp.mDocSale.mDocDetails.updateRowInDb(gridModelUp.mGrid, itemId);

                            //mDocSale.mDocDetails.onFieldChanged(itemId, dbField, calcValueObj );
                        }
                    }

                    gridModelUp.mDocSale.mDocDetails.calculateTotals(true);
                    gridModelUp.mDocSale.mDocHeader.onSumChanged();
                    /*if(infoPanel!=null)
                                infoPanel.displayTotals();*/
                } catch (Exception ex) {
                    MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionInput));
                    ErrorHandler.CatchError("Exception in DocSaleForm.onCalcResult", ex);
                } finally {
                    //Log.d("DocSale.onCalcResult", "exiting");
                }
            }
        });
        return dlg;
    }

    private Dialog createRespiteDialog() {
        calculatorDlg = new DocSaleCalculatorDialog();
        String title = getResources().getString(R.string.doc_sale_calc_titleRespite);
        Dialog dlg = calculatorDlg.onCreate(this, title, 0, "", DataGrid.DATA_TYPE_INTEGER);

        calculatorDlg.setCancelClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(IDD_RESPITE_DIALOG);
                showDialog(IDD_DOC_SALE_HEADER_DIALOG);
            }
        });

        calculatorDlg.setCalcResultListener(new DocSaleCalculatorDialog.OnCalcResultListener() {
            @Override
            public void onCalcResult(Object calcValue, boolean applyToAll) {
                try {
                    removeDialog(IDD_RESPITE_DIALOG);

                    mTempDocSaleHeader.respite = (Integer) calcValue;
                    showDialog(IDD_DOC_SALE_HEADER_DIALOG);
                    //mDocSale.mDocHeader.onRespiteChanged();
                    //updateRespiteDisplay();
                } catch (Exception ex) {
                    MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionDisplay) + "6");
                    ErrorHandler.CatchError("Exception in DocSaleForm.onRespiteDialogCalcResult", ex);
                }
            }
        });

        return dlg;
    }

    private Dialog createCommentDialog() {
        commentsDlg = new DocSaleCommentsDialog();
        String comment = (commentIdx == 1) ? mTempDocSaleHeader.comments1 : mTempDocSaleHeader.comments2;
        String specMarks = (commentIdx == 1) ? mTempDocSaleHeader.specMarks1 : mTempDocSaleHeader.specMarks2;

        Dialog dlg = commentsDlg.onCreate(this, commentIdx, comment, specMarks);

        commentsDlg.setCancelClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(IDD_COMMENTS_DIALOG);
                showDialog(IDD_DOC_SALE_HEADER_DIALOG);
            }
        });

        commentsDlg.setInputCommentListener(new DocSaleCommentsDialog.OnInputCommentListener() {
            @Override
            public void onCommentChanged(int commentIdx, String comments, String specMarks) {
                try {
                    removeDialog(IDD_COMMENTS_DIALOG);

                    if (commentIdx == 1) {
                        mTempDocSaleHeader.comments1 = comments;
                        mTempDocSaleHeader.specMarks1 = specMarks;

                        showDialog(IDD_DOC_SALE_HEADER_DIALOG);
                        //mDocSale.mDocHeader.onComments1Changed();
                    } else if (commentIdx == 2) {
                        mTempDocSaleHeader.comments2 = comments;
                        mTempDocSaleHeader.specMarks2 = specMarks;

                        showDialog(IDD_DOC_SALE_HEADER_DIALOG);

                        //mDocSale.mDocHeader.onComments2Changed();
                    }

                    //updateCommentsDisplay();
                } catch (Exception ex) {
                    MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionDisplay) + "5");
                    ErrorHandler.CatchError("Exception in DocSaleForm.onCommentChanged", ex);
                }
            }
        });

        return dlg;
    }

    //--------------------------------------------------------------
    private void updateDateDisplay() {
        String dateText = Convert.dateToString(gridModelUp.mDocSale.mDocHeader.docDate);
        mTextDate.setText(dateText);
    }

    //--------------------------------------------------------------
    private void updateDocTypeDisplay() {
        int longNameResourceId = Document.getDocReadableTypeResId(gridModelUp.mDocSale.mDocHeader.docType);
        String docTypeText = getResources().getString(longNameResourceId);

        mTextDocType.setText(docTypeText);
    }

    ;

    //--------------------------------------------------------------
    private void updateRespiteDisplay() {
        //String strRespite = Integer.toString(mDocSale.mDocHeader.respite);   		
        //mTextRespite.setText(strRespite);
    }

    //--------------------------------------------------------------
    private void updatePriceDisplay() {
        //String strPrice = mDocSale.mDocHeader.price.name;
        //mTextPrice.setText(strPrice);
    }

    //--------------------------------------------------------------
    private void updateCommentsDisplay() {
    }

    //--------------------------------------------------------------
    private void InitStepBar() {
        //init steps
        //commented out because full-screen step bar is used
        //ViewGroup stepButtonPlacement = (ViewGroup) findViewById(R.id.stepButtonPlacement);
        //AntContext.getInstance().getStepController().CreateButtons(this, stepButtonPlacement, StepPanelType.HORIZONTAL);

        //init tabs
        ViewGroup tabsPlacement = (ViewGroup) findViewById(R.id.tabsPlacement);
        AntContext.getInstance().getTabController().createTabs(this, tabsPlacement);
    }

    //--------------------------------------------------------------    
    @Override
    public void onResume() {
        try {
            super.onResume();

            ViewGroup tabsPlacement = (ViewGroup) findViewById(R.id.tabsPlacement);
            AntContext.getInstance().getTabController().refreshTabs(this, tabsPlacement);
        } catch (Exception ex) {
            MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionDisplay) + "10");
            ErrorHandler.CatchError("Exception in DocSaleForm.onResume", ex);
        }
    }

    //-------------------------------------------------------------
    private void configDocAppearanceByType() {
        if (gridModelUp.mDocSale.mDocHeader.docType == Document.DOC_TYPE_REMNANTS) {
            mTextDate.setVisibility(View.GONE);
           /* TextView mTextDatePrefix = (TextView) findViewById(R.id.textDatePrefix);
            mTextDatePrefix.setVisibility(View.GONE);*/

            //View layoutAdditionalHeaderInfo = (View) findViewById(R.id.layoutAdditionalHeaderInfo);
            //layoutAdditionalHeaderInfo.setVisibility(View.GONE);

            //mCheckFilterQuantity.setVisibility(View.GONE);
            //mCheckFilterStock.setVisibility(View.GONE);
            filterQuantityAndStock.setVisibility(View.GONE);
            buttonActiveDocColor.setVisibility(View.GONE);
        }
    }

    //--------------------------------------------------------------    
    private void checkErrorsAndFinishDocument(boolean closeFormOnSuccess, boolean startNextStep, boolean printDocument, DocSale mDocSale, Cursor mCursor) {
        try {
            boolean result = mDocSale.finishDocument();

            if (result == false) {
                MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_saveWarning));
                ErrorHandler.CatchError("Error saving document in mDocSale.finishDocument", ErrorHandler.LOG_ERROR);
                return;
            }

            if (printDocument && mDocSale.mDocHeader.savedDocID != 0) {
                PrintDocForm printForm = new PrintDocForm();
                Dialog dialog = printForm.onCreate(this, mDocSale.mDocHeader.savedDocID);
                dialog.show();
            }

            if (closeFormOnSuccess) {
                //DO_NOT_LAUNCH_NEW_ACTIVITY flag is used to avoid starting activity when calling onBackPressed, because it will be started
                //in startTab. otherwise, two activities will be started, last one will be on top but other will produce side effects 
                //like bug when need to press back button several times to exit from Visit window
                mCursor.close();
                mCursor = null;
                if (startNextStep) {
                    AntContext.getInstance().getTabController().onNextStepPressed(this);
                } else {
                    AntContext.getInstance().getTabController().onBackPressed(this, TabController.BackEventFlags.DO_NOT_LAUNCH_NEW_ACTIVITY);

                    if (returnToDebtForm)
                        AntContext.getInstance().getTabController().startTab(this, TabControllerVisit.TAB_VISIT, StepControllerVisit.VISIT_STEP_DEBTS, null); //go to visit/debts tab
                    else
                        AntContext.getInstance().getTabController().startTab(this, TabControllerVisit.TAB_DOC_LIST, 0, null); //go to docList tab
                }
            }
        } catch (Exception ex) {
            MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionFinish));
            ErrorHandler.CatchError("Exception in DocSaleForm.checkErrorsAndFinishDocument", ex);
        }
    }

    //--------------------------------------------------------------
    @Override
    public void onBackPressed() {
        try {
            checkAndClose(false, gridModelUp);
            checkAndClose(false, gridModelDown);

        } catch (Exception ex) {
            MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionFinish));
            ErrorHandler.CatchError("Exception in DocSaleForm.onBackPressed", ex);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == filterQuantityAndStock)
            try {
                if (quantityFilter == QuantityFilterState.NoFilter)
                    quantityFilter = QuantityFilterState.FilterQuantity;
                else if (quantityFilter == QuantityFilterState.FilterQuantity)
                    quantityFilter = QuantityFilterState.FilterStock;
                else
                    quantityFilter = QuantityFilterState.NoFilter;

                updateFilterLabels();
                refreshForFilter();

            } catch (Exception ex) {
                ErrorHandler.CatchError("Exception in filterQuantityAndStock.onClick", ex);
            }
        else if (view == filterList) {
            if (listsChecked == null) {
                listsChecked = new HashMap<Integer, Filter>();
                String sql = "select ItemGroupID ListID, ItemGroupName ListName, DocGridCondition ListFilter from ItemGroups where GroupType = (select DefaultValue from Settings where Property = 'item_group_lists') order by SortID";
                Cursor visitTypesCursor = Db.getInstance().selectSQL(sql);
                if (visitTypesCursor == null || visitTypesCursor.getCount() == 0)
                    return;

                int count = visitTypesCursor.getCount();

                int visitNameColumnIdx = visitTypesCursor.getColumnIndex("ListName");
                int docGridConditionIdx = visitTypesCursor.getColumnIndex("ListFilter");
                int groupIDIdx = visitTypesCursor.getColumnIndex("ListID");
                for (int i = 0; i < visitTypesCursor.getCount(); i++) {
                    visitTypesCursor.moveToPosition(i);
                    int id = visitTypesCursor.getInt(groupIDIdx);
                    String name = visitTypesCursor.getString(visitNameColumnIdx);
                    String docGridCondition = visitTypesCursor.getString(docGridConditionIdx);
                    Filter filter = new Filter(id, i, name, docGridCondition);
                    listsChecked.put(i, filter);
                }
                visitTypesCursor.close();
            }
            FilerGroupDialog dialog = new FilerGroupDialog(true, bookingOn);

            dialog.show(this, this, listsChecked);
        } else if (view == filterType) {
            if (typeChecked == null) {
                typeChecked = new HashMap<Integer, Filter>();
                String sql = "SELECT ItemTypeID, ItemTypeName FROM ItemTypes\n" +
                        "ORDER BY 1 ";
                Cursor visitTypesCursor = Db.getInstance().selectSQL(sql);
                if (visitTypesCursor == null || visitTypesCursor.getCount() == 0)
                    return;

                int count = visitTypesCursor.getCount();
                int visitNameColumnIdx = visitTypesCursor.getColumnIndex("ItemTypeName");
                int groupIDIdx = visitTypesCursor.getColumnIndex("ItemTypeID");

                for (int i = 0; i < visitTypesCursor.getCount(); i++) {
                    visitTypesCursor.moveToPosition(i);
                    int id = visitTypesCursor.getInt(groupIDIdx);
                    String name = visitTypesCursor.getString(visitNameColumnIdx);
                    String docGridCondition = String.valueOf(visitTypesCursor.getInt(groupIDIdx));
                    Filter filter = new Filter(id, i, name, docGridCondition);
                    typeChecked.put(i, filter);
                }
                visitTypesCursor.close();
            }
            FilerGroupDialog dialog = new FilerGroupDialog(bookingOn);
            dialog.show(this, this, typeChecked);

        } else if (view == adjasment)
            if (adjasmentOn) {
                adjasmentOn = false;
                adjasment.setTextColor(Color.WHITE);
                final String filter = (bookingOn) ? filterBooking : filterCondition;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        refreshGridAsync(gridModelDown, filter, bookingOn, false, null);
                    }
                }).start();

            } else {
                adjasmentOn = true;
                adjasment.setTextColor(Color.GREEN);
                int row = gridModelUp.mGrid.getSelectedRow();
                int itemIDidx = gridModelUp.mCursor.getColumnIndex("ItemID");
                if (gridModelUp.mCursor.moveToPosition(row)) {
                    final Integer item = gridModelUp.mCursor.getInt(itemIDidx);
                    final String filter = (bookingOn) ? filterBooking : filterCondition;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            refreshGridAsync(gridModelDown, filter, bookingOn, false, item.toString());
                        }
                    }).start();

                }
            }
        else if (view == booking) {
            if (bookingOn) {
                bookingOn = false;
                booking.setTextColor(Color.WHITE);
                setBookingCursor(bookingOn);

            } else {
                bookingOn = true;
                booking.setTextColor(Color.GREEN);
                setBookingCursor(bookingOn);
            }
        } else if (view == buttonActions) {
            showDialog(IDD_ACTIONS);
        } else if (view == mLayoutItemGroupTexts) {
            showDialog(IDD_SELECT_ITEM_GROUP);
        } else if (view == headerPanel) {
            try {
                if (gridModelUp.mDocSale.mDocHeader.docType != Document.DOC_TYPE_REMNANTS) {
                    mTempDocSaleHeader = gridModelUp.mDocSale.mDocHeader.clone();
                    showDialog(IDD_DOC_SALE_HEADER_DIALOG);
                }
            } catch (Exception ex) {
                ErrorHandler.CatchError("Exception in headerPanel.onClick", ex);
            }
        } else if (view == buttonActiveDocColor) {
            short first_form_linked = gridModelUp.mGrid.getColumns().getColumnByDbField("OrdersI").getLinked();
            short second_form_linked = gridModelUp.mGrid.getColumns().getColumnByDbField("OrdersII").getLinked();

            if (activeDocColor == Document.DOC_COLOR_BLACK)
                activeDocColor = Document.DOC_COLOR_WHITE;
            else
                activeDocColor = Document.DOC_COLOR_BLACK;

            gridModelUp.mGrid.getColumns().setColumnVisibilityByLink(activeDocColor == Document.DOC_COLOR_WHITE, first_form_linked);
            gridModelUp.mGrid.getColumns().setColumnVisibilityByLink(activeDocColor == Document.DOC_COLOR_BLACK, second_form_linked);
            gridModelUp.mGrid.getColumns().save();
            gridModelUp.mGrid.replaceColumns(gridModelUp.mGrid.getColumns());
            updateActiveColorButton();
        } else if (view == buttonEdit) {
            if (gridModelUp.mDocSale.isEditable && gridModelUp.mGrid != null && gridModelUp.mGrid.getSelectedRow() != -1) {
                String editField;

                if (gridModelUp.mDocSale.mDocHeader.docType == Document.DOC_TYPE_REMNANTS)
                    editField = "CurRemnant";
                else
                    editField = (activeDocColor == Document.DOC_COLOR_WHITE || activeDocColor == Document.DOC_COLOR_UNKNOWN)
                            ? "OrdersI" : "OrdersII";
                gridModelUp.cursorData.mEditedColumn = gridModelUp.mGrid.getColumns().getColumnIndexByDbField(editField);
                if (gridModelUp.cursorData.mEditedColumn != -1) {
                    selectColumnBeingEdited(editField, gridModelUp.mGrid, gridModelUp.cursorData);
                    gridModelUp.cursorData.mEditedRow = gridModelUp.mGrid.getSelectedRow();
                    gridModelUp.cursorData.mEditedDataType = DataGrid.DATA_TYPE_INTEGER;
                    showDialog(IDD_CALCULATOR);
                }
            }
        }
    }

    private void setBookingCursor(boolean isbooking) {
/*        String sortColumn = gridModelUp.mGrid.getDbFieldForColumn(gridModelUp.mGrid.getSortColumn());
        String sortOrder = (gridModelUp.mGrid.getSortOrder() == DataGrid.SortOrder.ASCENDING) ? "ASC" : "DESC";
        boolean filterQuantity = (quantityFilter == QuantityFilterState.FilterQuantity);
        boolean filterStock = (quantityFilter == QuantityFilterState.FilterStock);
        //first grid
        gridModelUp.mCursor.close();
        gridModelUp.mCursor = gridModelUp.mDocSale.mDocDetails.getItemsCursor(sortColumn, sortOrder, mCurrentGroup, filterQuantity, filterStock, true, filterBooking, isbooking);
        gridModelUp.mGrid.setCursor(gridModelUp.mCursor);
        //second grid
        gridModelDown.mCursor.close();
        gridModelDown.mCursor = gridModelDown.mDocSale.mDocDetails.getItemsCursor(sortColumn, sortOrder, mCurrentGroup, filterQuantity, filterStock, false, filterBooking, isbooking);
        gridModelDown.mGrid.setCursor(gridModelDown.mCursor);*/
        String filter = (isbooking) ? filterBooking : filterCondition;

        refreshGridAsync(gridModelUp, filter, isbooking, true, null);
        refreshGridAsync(gridModelDown, filter, isbooking, false, null);
    }

    @Override
    public boolean onTouch(View view, MotionEvent ev) {
        if (view == buttonLineUp) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN && mButtonUpPressed == false) {
                if (gridModelUp.mGrid != null)
                    gridModelUp.mGrid.moveSelectionUp();

                mButtonUpPressed = true;
                mDownHandler.removeCallbacks(mMoveSelectionUpTask);
                mDownHandler.postDelayed(mMoveSelectionUpTask, SELECTION_FIRST_MOVEMENT_INTERVAL);
            } else if (ev.getAction() == MotionEvent.ACTION_UP) {
                mButtonUpPressed = false;
            }
        } else if (view == buttonLineDown) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN && mButtonDownPressed == false) {
                if (gridModelUp.mGrid != null)
                    gridModelUp.mGrid.moveSelectionDown();

                mButtonDownPressed = true;
                mDownHandler.removeCallbacks(mMoveSelectionDownTask);
                mDownHandler.postDelayed(mMoveSelectionDownTask, SELECTION_FIRST_MOVEMENT_INTERVAL);
            } else if (ev.getAction() == MotionEvent.ACTION_UP) {
                mButtonDownPressed = false;
            }
        }
        return false;
    }

    //--------------------------------------------------------------
    private void closeDocument(boolean startNextStep, DocSale mDocSale, Cursor mCursor) {
        try {
            mDocSale.cancelDocument();

            if (mCursor != null)
                mCursor.close();
            mCursor = null;


            if (startNextStep) {
                AntContext.getInstance().getTabController().onNextStepPressed(this);
            } else {
                AntContext.getInstance().getTabController().onBackPressed(this);
            }
        } catch (Exception ex) {
            MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionFinish));
            ErrorHandler.CatchError("Exception in DocSaleForm.closeDocument", ex);
        }
    }

    //--------------------------------------------------------------
    private void checkAndClose(boolean startNextStep, final GridModel gridModel) {
        final boolean _startNextStep = startNextStep;
        final Cursor mCursor = gridModel.mCursor;
        final DocSale mDocSale = gridModel.mDocSale;
        gridModel.mGrid.getColumns().save();

        if (haveUnsavedChanges() == false) {
            closeDocument(_startNextStep, gridModel.mDocSale, gridModel.mCursor);
            return;
        }

        //
        //Display alert dialog proposing to save a document
        //
        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getResources().getString(R.string.doc_payment_alertFinishTitle));
        alertDialog.setMessage(getResources().getString(R.string.doc_payment_alertFinish));

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.document_alertChoiceYes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        checkErrorsAndFinishDocument(true, _startNextStep, false, gridModel.mDocSale, mCursor);
                    }
                });

        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getResources().getString(R.string.document_alertChoiceNo),
                new DialogInterface.OnClickListener() {


                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeDocument(_startNextStep, mDocSale, mCursor);
                    }
                });

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.document_alertChoiceCancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        alertDialog.show();
    }

    //---------------------- IStep implementation ----------------------------------------
    public boolean haveUnsavedChanges() {
        return gridModelUp.mDocSale.isEditable;
    }

    //------------------------------------------------------------------------------------
    public void updateActiveColorButton() {
        if (activeDocColor == Document.DOC_COLOR_WHITE)
            buttonActiveDocColor.setText(getResources().getString(R.string.doc_sale_button_formI));
        else if (activeDocColor == Document.DOC_COLOR_BLACK)
            buttonActiveDocColor.setText(getResources().getString(R.string.doc_sale_button_formII));

        Settings.getInstance().setStringActivityPreference("doc_sale_form_preferences", "activeDocColor", new Character(activeDocColor).toString());
    }

    //------------------------------------------------------------------------------------
    public void updateFilterLabels() {
        if (quantityFilter == QuantityFilterState.FilterQuantity)
            mTextQuantityFilterLabel.setTextColor(0xFF00FF00);
        else
            mTextQuantityFilterLabel.setTextColor(0xFFFFFFFF);

        if (quantityFilter == QuantityFilterState.FilterStock)
            mTextStockFilterLabel.setTextColor(0xFF00FF00);
        else
            mTextStockFilterLabel.setTextColor(0xFFFFFFFF);
    }

    //------------------------------------------------------------------------------------
    public String displayItemGroupParent(ItemGroup itemGroup) {
        mTextItemGroupParent.setVisibility(itemGroup.isChild() ? View.VISIBLE : View.GONE);
        if (itemGroup.isChild()) {
            ItemGroup parentGroup = itemGroupSelector.getCurrentDictionary().getGroupParent(mCurrentGroup);
            if (parentGroup != null) {
                mTextItemGroupParent.setText(parentGroup.name);
                return parentGroup.name;
            }
        }

        return "";
    }

    //------------------------------------------------------------------------------------
    public void onItemGroupApplied() {
        boolean b = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_NEED_FILL_ORDER_FROM_PREDICTED, -1) > 0;
        if (mCurrentGroup.visited == false && mCurrentGroup.isPredictOrder && b && mCurrentGroup.parentId != 2) //do not apply for "all"
        {
            //this predict order group was not visited earlier, so need to copy values from predicted order column to quantity
            applyPredictOrders = true;
            //mDocSale.mDocDetails.fillOrdersFromPredictedOrder(mGrid, mCursor);  		
        } else
            applyPredictOrders = false;

        mCurrentGroup.visited = true;
        String parentName = displayItemGroupParent(mCurrentGroup);
        mTextItemGroup.setText(mCurrentGroup.name);

        String message = String.format("ItemGroup applied: %s, parent %s ", mCurrentGroup.name, parentName);
        MLog.WriteLog(MLog.LOG_TYPE_DOC_STEP, message);
    }

    //------------------------------------------------------------------------------------
    public void applyNewItemGroupUp(ItemGroup itemGroup) {
        if (itemGroup.id != mCurrentGroup.id) {
            //��������� ��������� �����
            if (gridModelUp.mGrid != null)
                gridStates.put(mCurrentGroup.id, gridModelUp.mGrid.getGridState());

            mCurrentGroup = itemGroup;
            onItemGroupApplied();
            //gridModelUp.mCursor = fillGridWithData(true, true, false, true, gridModelUp, false);

        }
    }//------------------------------------------------------------------------------------

    public void applyNewItemGroupDown(ItemGroup itemGroup) {
        if (itemGroup.id != mCurrentGroup.id) {
            //��������� ��������� �����
            if (gridModelDown.mGrid != null)
                gridStates.put(mCurrentGroup.id, gridModelUp.mGrid.getGridState());

            ItemGroup mCurrentGroup = itemGroup;
            boolean b = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_NEED_FILL_ORDER_FROM_PREDICTED, -1) > 0;
            applyPredictOrders = false;
            mCurrentGroup.visited = true;
            String parentName = displayItemGroupParent(mCurrentGroup);
            mTextItemGroup.setText(mCurrentGroup.name);

            //gridModelDown.mCursor = fillGridWithData(true, true, false, true, gridModelUp, true);

        }
    }

    //------------------------------------------------------------------------------------    
    private Runnable mMoveSelectionUpTask = new Runnable() {
        public void run() {
            try {
                if (mButtonUpPressed) {
                    if (gridModelUp.mGrid != null)
                        gridModelUp.mGrid.moveSelectionUp();

                    mUpHandler.removeCallbacks(mMoveSelectionUpTask);
                    mUpHandler.postDelayed(mMoveSelectionUpTask, SELECTION_MOVEMENT_INTERVAL);
                }
            } catch (Exception ex) {
                ErrorHandler.CatchError("Exception in DocSaleForm.mMoveSelectionUpTask", ex);
            }
        }
    };

    //------------------------------------------------------------------------------------    
    private Runnable mMoveSelectionDownTask = new Runnable() {
        public void run() {
            try {
                if (mButtonDownPressed) {
                    if (gridModelUp.mGrid != null)
                        gridModelUp.mGrid.moveSelectionDown();

                    mUpHandler.removeCallbacks(mMoveSelectionDownTask);
                    mUpHandler.postDelayed(mMoveSelectionDownTask, SELECTION_MOVEMENT_INTERVAL);
                }
            } catch (Exception ex) {
                ErrorHandler.CatchError("Exception in DocSaleForm.mMoveSelectionDownTask", ex);
            }
        }
    };

    //------------------------------------------------------------------------------------    
    private Runnable mCalculatePlanTotalsTask = new Runnable() {
        public void run() {
            try {
                //mPlanTotalsCalculationHandler.removeCallbacks(mCalculatePlanTotalsTask);          		
                //Log.d("DocSaleForm ", "CalcTotals end");

                gridModelUp.mDocSale.mDocDetails.calculatePlanTotals(); //just calculate plans
                /*if(infoPanel!=null)
                            infoPanel.displayTotals();*/

            } catch (Exception ex) {
                ErrorHandler.CatchError("Exception in DocSaleForm.mCalculatePlanTotalsTask", ex);
            }
        }
    };

    public class NextStepClickListener implements View.OnClickListener {


        public NextStepClickListener() {

        }

        @Override
        public void onClick(View v) {
            try {
                checkAndClose(true, gridModelUp);
                checkAndClose(true, gridModelDown);
            } catch (Exception ex) {
                MessageBox.show(DocSaleForm.this, getResources().getString(R.string.form_title_docSale), getResources().getString(R.string.doc_sale_exceptionFinish));
                ErrorHandler.CatchError("Exception in DocSaleForm.mButtonNextStep.onClick", ex);
            }

        }
    }

    public class DigitClickListener implements View.OnClickListener {

        private boolean denyMoreThanRest;
        private Cursor mCursor;
        private DocSale mDocSale;
        private DataGrid mGrid;
        private CursorData cursorData;

        public DigitClickListener(boolean denyMoreThanRest, Cursor mCursor, DocSale mDocSale, DataGrid mGrid, CursorData cursorData) {
            this.denyMoreThanRest = denyMoreThanRest;
            this.mCursor = mCursor;
            this.mDocSale = mDocSale;
            this.mGrid = mGrid;
            this.cursorData = cursorData;
        }

        @Override
        public void onClick(View v) {
            //Log.d("DocSaleForm ", "-------OnClick start");

            String buttonText = (String) ((Button) v).getText();


            if (mDocSale.isEditable && mGrid != null && mGrid.getSelectedRow() != -1) {
                String editField;

                if (mDocSale.mDocHeader.docType == Document.DOC_TYPE_REMNANTS)
                    editField = "CurRemnant";
                else
                    editField = (activeDocColor == Document.DOC_COLOR_WHITE || activeDocColor == Document.DOC_COLOR_UNKNOWN)
                            ? "OrdersI" : "OrdersII";

                cursorData.mEditedColumn = mGrid.getColumns().getColumnIndexByDbField(editField);

                if (cursorData.mEditedColumn != -1) {
                    //select cell being edited
                    selectColumnBeingEdited(editField, mGrid, cursorData);
                    /*ColumnInfo column = mGrid.getColumns().getColumnByDbField(editField);

                if(column.isVisible())
                    mGrid.setSelectedColumn(mEditedColumn);*/

                    //change value of the cell
                    cursorData.mEditedRow = mGrid.getSelectedRow();

                    if (mCursor.moveToPosition(cursorData.mEditedRow)) {
                        int ItemId = mCursor.getInt(docSaleTableModel.getItemIdIdx());

                        if (buttonText.equals("C")) {
                            mGrid.setCellValue(cursorData.mEditedRow, cursorData.mEditedColumn, null);
                        } else {
                            String cellValue = mGrid.getCellValue(cursorData.mEditedRow, cursorData.mEditedColumn);
                            String newCellValue = "0";

                            if (mRowChanged)
                                newCellValue = buttonText;
                            else
                                newCellValue = cellValue != null ? cellValue + buttonText : buttonText;

                            if (mDocSale.mDocHeader.docType == Document.DOC_TYPE_SALE || (denyMoreThanRest && !editField.equals("CurRemnant"))) {
                                int orders = Convert.toInt(newCellValue, 0);
                                int oldOrders = mCursor.getInt(mCursor.getColumnIndex("Orders"));

                                String secondOrdersField = editField.equals("OrdersI") ? "OrdersII" : "OrdersI";
                                ColumnInfo secondOrdersColumn = mGrid.getColumns().getColumnByDbField(secondOrdersField);
                                int secondOrders = Convert.toInt(mGrid.getCellValue(cursorData.mEditedRow, secondOrdersColumn), 0);

                                int quantity = mCursor.getInt(mCursor.getColumnIndex("Quantity"));

                                if ((quantity + oldOrders) < (orders + secondOrders)) {
                                    MessageBox.show(DocSaleForm.this, getResources().getString(R.string.doc_sale_limitation), getResources().getString(R.string.doc_sale_ordersExceedsQuantity));
                                    return;
                                }
                            }

                            mGrid.setCellValue(cursorData.mEditedRow, cursorData.mEditedColumn, newCellValue);

                            if (mRowChanged)
                                mRowChanged = false;
                        }


                        mDocSale.mDocDetails.updateRowInDb(mGrid, ItemId);
                        //Log.d("DocSaleForm ", "CalcTotals start");
                        mDocSale.mDocDetails.calculateTotals(false); //do not include plans to calculation

                        //launch delayed plans calculation
                        mPlanTotalsCalculationHandler.removeCallbacks(mCalculatePlanTotalsTask);
                        mPlanTotalsCalculationHandler.postDelayed(mCalculatePlanTotalsTask, PLAN_TOTALS_CALCULATION_INTERVAL);

                        //Log.d("DocSaleForm ", "CalcTotals end");
                        mDocSale.mDocHeader.onSumChanged();
                        mSum.setText(" Итого :" + Convert.moneyToString(gridModelUp.mDocSale.totalAll + gridModelDown.mDocSale.totalAll));
                        mOrders.setText(" Позиций :" +String.valueOf(gridModelUp.mDocSale.totalItems + gridModelDown.mDocSale.totalItems));
                        /*if(infoPanel!=null)
                        infoPanel.displayTotals();*/
                    }
                }
            }
            //Log.d("DocSaleForm ", "-------OnClick end");
        }
    }

    ;

    @Override
    public void onDocSaleSelected(final String docGridCondition, final boolean booking, HashMap<Integer, Filter> groupsChecked, boolean listFilter) {
        if (listFilter) {
            this.listsChecked = groupsChecked;
            if (booking) {
                listFilterBooking = docGridCondition;
                filterBooking = listFilterBooking + typeFilterBooking;
            } else {
                listFilterCondition = docGridCondition;
                filterCondition = listFilterCondition + typeFilterCondition;
            }
        } else {
            this.typeChecked = groupsChecked;
            if (booking) {
                typeFilterBooking = docGridCondition;
                filterBooking = listFilterBooking + typeFilterBooking;
            } else {
                typeFilterCondition = docGridCondition;
                filterCondition = listFilterCondition + typeFilterCondition;
            }
        }
        refreshForFilter();
    }

    private void refreshForFilter() {
        final String filter = (bookingOn) ? filterBooking : filterCondition;
        new Thread(new Runnable() {
            @Override
            public void run() {
                refreshGridAsync(gridModelDown, filter, bookingOn, false, null);
                refreshGridAsync(gridModelUp, filter, bookingOn, true, null);
            }
        }).start();
        //gridModelUp.mGrid.setSelectedRow(0,1);
    }


    private void refreshGridAsync(final GridModel gridModel, final String docGridCondition, final boolean booking, final boolean upper, final String adjasment) {
        final String sortColumn = gridModel.mGrid.getDbFieldForColumn(gridModel.mGrid.getSortColumn());
        final String sortOrder = (gridModel.mGrid.getSortOrder() == DataGrid.SortOrder.ASCENDING) ? "ASC" : "DESC";
        final boolean filterQuantity = (quantityFilter == QuantityFilterState.FilterQuantity);
        final boolean filterStock = (quantityFilter == QuantityFilterState.FilterStock);
        final Monitor serviceStartupMonitor = new Monitor().once();
        startWaiting();
        new Thread(new Runnable() {
            @Override
            public void run() {
                gridModel.mCursor.close();
                if (adjasment != null)
                    gridModel.mCursor = gridModel.mDocSale.mDocDetails.getItemsCursor(sortColumn, sortOrder, mCurrentGroup, filterQuantity, filterStock, upper, docGridCondition, booking, adjasment);
                else
                    gridModel.mCursor = gridModel.mDocSale.mDocDetails.getItemsCursor(sortColumn, sortOrder, mCurrentGroup, filterQuantity, filterStock, upper, docGridCondition, booking);
                serviceStartupMonitor.doNotify();
            }
        }).start();
        serviceStartupMonitor.doWait();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gridModel.mGrid.setCursor(gridModel.mCursor);
                if (upper) gridModel.mGrid.setSelectedRow(0, 1);
            }
        });
        cancelWaiting();
    }

}
