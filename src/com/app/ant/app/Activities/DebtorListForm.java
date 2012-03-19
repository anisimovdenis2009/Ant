package com.app.ant.app.Activities;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 23.01.12
 * Time: 13:51
 * To change this template use File | Settings | File Templates.
 */
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.app.ant.R;
import com.app.ant.app.Activities.ItemSwitcher.ItemSwitcherElement;
import com.app.ant.app.BusinessLayer.Document;
import com.app.ant.app.Controls.DataGrid;
import com.app.ant.app.Controls.DataGrid.GridColumns;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import java.util.Calendar;
import java.util.Date;

/** Форма-справочник со списком дебиторов
 */
public class DebtorListForm extends AntActivity
{
	private static final int IDD_DATE_DIALOG = 0;

	private DataGrid mGrid;
	private Cursor mCursor;

	private ToggleButton mCheckFilterUnpaid;
	private ToggleButton mCheckRoute;
	//private ViewGroup mRouteDatePanel;
	private TextView mTextRouteDate;

	private InfoPanelSaldo infoPanel;

	private Calendar routeDate = Calendar.getInstance();
	private ItemSwitcher debtDirectionSwitcher;

    //--------------------------------------------------------------
    @Override public void onCreate(Bundle savedInstanceState)
    {
    	try
    	{
	        super.onCreate(savedInstanceState);
	    	requestWindowFeature(Window.FEATURE_NO_TITLE);
	    	overridePendingTransition(0,0);
	        setContentView(R.layout.debtor_list);

	        mCheckFilterUnpaid = (ToggleButton) findViewById(R.id.chkBoxUnpaid);
	        mCheckRoute = ((ToggleButton) findViewById(R.id.chkBoxRoute));
	        mTextRouteDate = (TextView) findViewById(R.id.textRouteDate);
	        //mRouteDatePanel = (ViewGroup) findViewById(R.id.datePanel);

	        //
	        //init direction switcher
	        //
	        //ImageButton buttonPrev = (ImageButton) findViewById(R.id.buttonPrevDebtDirection);
	        //ImageButton buttonNext = (ImageButton) findViewById(R.id.buttonNextDebtDirection);
	        TextView textDebtDirection = (TextView) findViewById(R.id.textDebtDirection);

	        ItemSwitcher.OnItemSwitch onItemSwitch = new ItemSwitcher.OnItemSwitch()
											        {
											        	@Override public void onItemSelected(ItemSwitcherElement item)
											        	{
											        		fillGridWithData();
											        	}
											        };

			long defaultDirectionID = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_DEFAULT_DIRECTION_ID, 0);
	        String sql = "SELECT dir.DirectionID AS ItemID, dir.DirectionName AS ItemName FROM Directions dir";
	        debtDirectionSwitcher = new ItemSwitcher(this, sql, defaultDirectionID, /*buttonPrev, buttonNext,*/ null, null,
	        											textDebtDirection, onItemSwitch );


	        //info panel
	        InfoPanelBase prevPanel = infoPanel;
	        infoPanel = new InfoPanelSaldo();
	        infoPanel.loadInfoPanel(this, prevPanel);

	        initGrid();

	        //
	        //init stock filter checkbox
	        //
	        CheckBox.OnClickListener checkBoxClickListener = new CheckBox.OnClickListener()
	        {
	        	@Override public void onClick(View v)
	        	{
	        		boolean route = mCheckRoute.isChecked();
	        		//mRouteDatePanel.setVisibility(route ? View.VISIBLE: View.INVISIBLE);
	        		if(route)
	        		{
	        			routeDate = Calendar.getInstance();
	        			//updateDateDisplay();
	        		}

	        		fillGridWithData();
	        	}
	        };

	        mCheckFilterUnpaid.setOnClickListener(checkBoxClickListener);
	        mCheckRoute.setOnClickListener(checkBoxClickListener);


	        //
	        //init date click event listener
	        //
	        /*mRouteDatePanel.setOnClickListener(new ViewGroup.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					try
					{
						showDialog(IDD_DATE_DIALOG);
					}
					catch(Exception ex)
					{
						ErrorHandler.CatchError("Exception in routeDatePanel.onClick", ex);
					}
				}
			});*/

	        Button buttonDetails = ((Button) findViewById(R.id.buttonDetails));
	        buttonDetails.setOnClickListener(new ViewGroup.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					try
					{
			        	try
			        	{
			        		if(mGrid!=null)
			        		{
			        			int row = mGrid.getSelectedRow();
			        			if(row!=-1)
			        				displaySaldoDetails(row);
			        		}
			        	}
			    		catch(Exception ex)
			    		{
			   	    		MessageBox.show(DebtorListForm.this, getResources().getString(R.string.form_title_debtorList), getResources().getString(R.string.item_list_exceptionItemInfo));
			    			ErrorHandler.CatchError("Exception in buttonDetails.onClick", ex);
			    		}

					}
					catch(Exception ex)
					{
						ErrorHandler.CatchError("Exception in buttonDetails.onClick", ex);
					}
				}
			});

    	}
		catch(Exception ex)
		{
			MessageBox.show(this, getResources().getString(R.string.form_title_debtorList), getResources().getString(R.string.item_list_exceptionOnCreate));
			ErrorHandler.CatchError("Exception in DebtorsListForm.onCreate", ex);
		}

    }
    //--------------------------------------------------------------
    /** Отображение даты маршрута на окне*/
//    private void updateDateDisplay()
//    {
//        mTextRouteDate.setText( Convert.dateToString(routeDate));
//    }

    //--------------------------------------------------------------
    private void initGrid()
    {
        mGrid = (DataGrid) findViewById(R.id.dataGridItems);

        DataGrid.ColumnInfo[] columns = new DataGrid.ColumnInfo[]
        {
        		new DataGrid.ColumnInfo(1, getResources().getString(R.string.debtor_list_column_header_client), DataGrid.DATA_TYPE_VARCHAR, 270,
        									DataGrid.GRID_COLUMN_DEFAULT|DataGrid.GRID_COLUMN_FIXED, "NameScreen", 1, (short)-1, ""),
        		new DataGrid.ColumnInfo(2, getResources().getString(R.string.debtor_list_column_header_saldo), DataGrid.DATA_TYPE_MONEY, 80, DataGrid.GRID_COLUMN_DEFAULT, "Saldo2", 2, (short)-1, ""),
        		new DataGrid.ColumnInfo(3, getResources().getString(R.string.debtor_list_column_header_overdue), DataGrid.DATA_TYPE_MONEY, 80, DataGrid.GRID_COLUMN_DEFAULT, "Saldo6", 3, (short)-1, ""),
        		new DataGrid.ColumnInfo(4, getResources().getString(R.string.debtor_list_column_header_today), DataGrid.DATA_TYPE_MONEY, 80, DataGrid.GRID_COLUMN_DEFAULT, "Saldo4", 4, (short)-1, ""),
        		new DataGrid.ColumnInfo(5, getResources().getString(R.string.debtor_list_column_header_paid), DataGrid.DATA_TYPE_MONEY, 80, DataGrid.GRID_COLUMN_DEFAULT, "PaidToday", 5, (short)-1, ""),
        };

        mGrid.setColumns(new GridColumns(columns));
        mGrid.setCellListener(new CellListener());
        mGrid.setDefaultSortColumnByDbField("ClientID");
        mGrid.setSortOrder(DataGrid.SortOrder.ASCENDING);
        mGrid.setIdentityColumn(0);

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
    //--------------------------------------------------------------
    public static String getSaldoQuery(boolean unpaidOnly, boolean routeOnly, Calendar routeDate, boolean summary, long directionID)
    {
    	String filter = " WHERE 1=1 ";

    	if(unpaidOnly)
    		filter += " AND Saldo2>0 ";

		if(routeOnly)
			filter +=  Q.getRouteFilter(routeDate, false);

    	Date todayDate = Calendar.getInstance().getTime();
		String selectPayments = String.format(
								" coalesce((select round(sum(d.SumAll),2) " +
								" from Documents d " +
								" where d.ClientID = c.ClientID AND d.DocType in ('%s') AND d.State in ('%s','%s') AND d.CreateDate %s ), 0) " +
								" AS PaidToday ",
								Document.DOC_TYPE_PAYMENT, Document.DOC_STATE_FINISHED, Document.DOC_STATE_SENT,
								Q.getSqlBetweenDayStartAndEnd(todayDate));

    	String sql =
			" SELECT " + (summary ? "" :" c.ClientID, c.NameScreen, ") +
				String.format(" coalesce((select round(max(Saldo),2) from ClientSaldo where ClientID = c.ClientID and SaldoTypeID=2 and DirectionID=%d ), 0) AS Saldo2, ", directionID) +
				String.format(" coalesce((select round(sum(Saldo),2) from ClientSaldo where ClientID = c.ClientID and SaldoTypeID=4 and DirectionID=%d ), 0) AS Saldo4, ", directionID) +
				String.format(" coalesce((select round(max(Saldo),2) from ClientSaldo where ClientID = c.ClientID and SaldoTypeID=6 and DirectionID=%d ), 0) AS Saldo6, ", directionID) +
				selectPayments +
			" FROM Clients c " +
			filter;

    	if(summary)
    		sql = String.format( " SELECT sum(Saldo2) AS Saldo2, sum(Saldo4) AS Saldo4, sum(Saldo6) AS Saldo6, sum(PaidToday) AS PaidToday FROM ( %s )", sql);

    	return sql;
    }

    //--------------------------------------------------------------
    private void fillGridWithData()
    {
    	String sortColumn = mGrid.getDbFieldForColumn(mGrid.getSortColumn());
    	String sortOrder = (mGrid.getSortOrder() == DataGrid.SortOrder.ASCENDING)? "ASC": "DESC";

    	boolean unpaidOnly = mCheckFilterUnpaid.isChecked();
    	boolean routeOnly = mCheckRoute.isChecked();
    	long directionID = debtDirectionSwitcher.getCurrentItemID();

    	String sql = getSaldoQuery(unpaidOnly, routeOnly, routeDate, false, directionID);
    	sql += " ORDER BY " + sortColumn + " " + sortOrder;

    	mCursor = Db.getInstance().selectSQL(sql);

    	mGrid.setCursor(mCursor);

    	if(mCursor.getCount()>0)
    	{
    		mGrid.setSelectedRow(0);
    	}

    	if(infoPanel!=null)
	    	infoPanel.displayTotals(unpaidOnly, routeOnly, routeDate, directionID);
    }

    //--------------------------------------------------------------
    private void displaySaldoDetails(int row)
    {
    	if(mCursor!=null && mCursor.moveToPosition(row))
    	{
    		int clientID = mCursor.getInt(mCursor.getColumnIndex("ClientID"));

    		//start activity with document list for selected client
    		Bundle bundleDebts = new Bundle();
    		bundleDebts.putBoolean(DocListForm.PARAM_NAME_IS_DEBT_FORM, true);
    		bundleDebts.putBoolean(Document.PARAM_NAME_FROM_VISIT, false);
    		bundleDebts.putLong(Document.PARAM_NAME_CLIENT_ID, clientID);

			Intent debtsIntent = new Intent(DebtorListForm.this, DocListForm.class);
			debtsIntent.putExtras(bundleDebts);

			startActivity(debtsIntent);
    	}

    }

    //--------------------------------------------------------------
    private class CellListener extends DataGrid.BaseCellListener
    {
        /*@Override public void onCellLongPress(int row, int column)
        {
        	try
        	{
        		displaySaldoDetails(row);
        	}
    		catch(Exception ex)
    		{
   	    		MessageBox.show(DebtorListForm.this, getResources().getString(R.string.form_title_debtorList), getResources().getString(R.string.item_list_exceptionItemInfo));
    			ErrorHandler.CatchError("Exception in onCellSelected", ex);
    		}
        }*/

        @Override public void onHeaderClicked(int column, boolean sortOrderChanged)
        {
        	//header click implies changes in sort order. we need to re-read Cursor and set it to DataGrid
        	fillGridWithData();
        }
    }

    //--------------------------------------------------------------
    @Override protected Dialog onCreateDialog(int id)
    {
    	try
    	{
	        switch (id)
	        {
	            /*case IDD_DATE_DIALOG:
	                return new DatePickerDialog(this,
	                		new DatePickerDialog.OnDateSetListener()
	                		{
			                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
			                    {
			                    	routeDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			                    	routeDate.set(Calendar.MONTH, monthOfYear);
			                    	routeDate.set(Calendar.YEAR, year);
			                        updateDateDisplay();

			                        fillGridWithData();
			                    }
	                		},
	                		routeDate.get(Calendar.YEAR),
	                		routeDate.get(Calendar.MONTH),
	                		routeDate.get(Calendar.DAY_OF_MONTH));*/
			}
    	}
		catch(Exception ex)
		{
			MessageBox.show(this, getResources().getString(R.string.form_title_docPayment), getResources().getString(R.string.doc_payment_exceptionOnCreateDialog));
			ErrorHandler.CatchError("Exception in DebtorListForm.onCreateDialog", ex);
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

}
