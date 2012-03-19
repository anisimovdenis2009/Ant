package com.app.ant.app.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.app.ant.R;
import com.app.ant.app.Activities.DocSaleSelectGroupDialog.ItemGroup;
import com.app.ant.app.Controls.DataGrid;
import com.app.ant.app.Controls.DataGrid.GridColumns;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;


public class ItemListForm extends AntActivity  
{
	private final static int IDD_SELECT_ITEM_GROUP = 0;
	private final static int IDD_ITEM = 1;
	
	private DataGrid mGrid;
	private Cursor mCursor;
	
	private ToggleButton mCheckFilterStock;
	private TextView mTextItemGroup;

	private InfoPanelBase infoPanel;
	private ItemDialog itemDlg;

	private DocSaleSelectGroupDialog selectGroupDlg;
	private ItemGroup mCurrentGroup;
	DocSaleSelectGroupDialog.ItemGroupSelector itemGroupSelector;
	
	DocSaleSelectGroupDialog.OnSelectGroupListener selectGroupListener = new DocSaleSelectGroupDialog.OnSelectGroupListener() 
	{					
		@Override public void onGroupSelected(ItemGroup itemGroup, boolean fromDialog) 
		{
			try
			{
				removeDialog(IDD_SELECT_ITEM_GROUP);		    					
				applyNewItemGroup(itemGroup);
			}
			catch(Exception ex)
			{
		    	MessageBox.show(ItemListForm.this, getResources().getString(R.string.form_title_itemList), getResources().getString(R.string.document_exceptionDisplay));
				ErrorHandler.CatchError("Exception in ItemListForm.onGroupSelected", ex);
			}								
		}
	};
	
    //--------------------------------------------------------------	
    @Override public void onCreate(Bundle savedInstanceState) 
    {
    	try
    	{
	        super.onCreate(savedInstanceState);
	    	requestWindowFeature(Window.FEATURE_NO_TITLE);
	    	overridePendingTransition(0,0);
	        setContentView(R.layout.item_list);

	        mCheckFilterStock = (ToggleButton) findViewById(R.id.chkBoxFilterStock);
	        mTextItemGroup = (TextView) findViewById(R.id.textItemGroup);

	        //
	        //init item group selector
	        //
			itemGroupSelector = new DocSaleSelectGroupDialog.ItemGroupSelector(this, selectGroupListener, 
																					DocSaleSelectGroupDialog.SelectorType.itemList);				
			mCurrentGroup = itemGroupSelector.getCurrentDictionary().getDefaultGroup();
	        
	        mTextItemGroup.setText(mCurrentGroup.name);
	        
	        mTextItemGroup.setOnClickListener( new View.OnClickListener()
			{				
				@Override public void onClick(View v) 
				{
					showDialog(IDD_SELECT_ITEM_GROUP);	
				}
			});

	        /*//info panel
	        InfoPanelBase prevPanel = infoPanel;
	        infoPanel = new InfoPanelSKU();
	        infoPanel.loadInfoPanel(this, prevPanel);*/
	        
	        InitGrid();        
	  
	        //
	        //init stock filter checkbox
	        //
	        CheckBox.OnClickListener checkBoxClickListener = new CheckBox.OnClickListener() 
	        {
	        	@Override public void onClick(View v)
	        	{
	        		fillGridWithData();
	        	}
	        };
	        
	        mCheckFilterStock.setOnClickListener(checkBoxClickListener);
	        
    	}
		catch(Exception ex)
		{			
			MessageBox.show(this, getResources().getString(R.string.form_title_itemList), getResources().getString(R.string.item_list_exceptionOnCreate));
			ErrorHandler.CatchError("Exception in ItemListForm.onCreate", ex);
		}
    	
    }
    
    //--------------------------------------------------------------
    private void InitGrid()
    {
        mGrid = (DataGrid) findViewById(R.id.dataGridItems);  
        
        DataGrid.ColumnInfo[] columns = new DataGrid.ColumnInfo[]
        {
        		new DataGrid.ColumnInfo(1, getResources().getString(R.string.item_list_column_header_id), DataGrid.DATA_TYPE_INTEGER, 60, 
        									DataGrid.GRID_COLUMN_DEFAULT|DataGrid.GRID_COLUMN_FIXED, "ItemID", 1, (short)-1, ""),
        		new DataGrid.ColumnInfo(2, getResources().getString(R.string.item_list_column_header_name), DataGrid.DATA_TYPE_VARCHAR, 230,
        									DataGrid.GRID_COLUMN_DEFAULT|DataGrid.GRID_COLUMN_FIXED, "ScreenName", 2, (short)-1, ""),
        		new DataGrid.ColumnInfo(3, getResources().getString(R.string.item_list_column_header_stock), DataGrid.DATA_TYPE_VARCHAR, 50, DataGrid.GRID_COLUMN_DEFAULT, "Rest", 3, (short)-1, ""),
        		new DataGrid.ColumnInfo(4, getResources().getString(R.string.item_list_column_header_price), DataGrid.DATA_TYPE_VARCHAR, 50, DataGrid.GRID_COLUMN_DEFAULT, "Price", 4, (short)-1, ""),
        		new DataGrid.ColumnInfo(5, getResources().getString(R.string.item_list_column_header_minPrice), DataGrid.DATA_TYPE_VARCHAR, 80, DataGrid.GRID_COLUMN_DEFAULT, "MinPrice", 5, (short)-1, ""),
        		new DataGrid.ColumnInfo(6, getResources().getString(R.string.item_list_column_header_EAN), DataGrid.DATA_TYPE_VARCHAR, 125, DataGrid.GRID_COLUMN_DEFAULT, "EAN", 6, (short) -1, ""),
        		new DataGrid.ColumnInfo(7, getResources().getString(R.string.item_list_column_header_sortID), DataGrid.DATA_TYPE_VARCHAR, 80, DataGrid.GRID_COLUMN_DEFAULT, "SortID", 7, (short) -1, "")
        };
        
        mGrid.setColumns(new GridColumns(columns));
        mGrid.setCellListener(new CellListener());
        mGrid.setDefaultSortColumnByDbField("SortID");
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
    private void fillGridWithData()
    {
    	String sortColumn = mGrid.getDbFieldForColumn(mGrid.getSortColumn());
    	String sortOrder = (mGrid.getSortOrder() == DataGrid.SortOrder.ASCENDING)? "ASC": "DESC";
    	
    	String where = "";
    	
    	if(mCheckFilterStock.isChecked())
    		where = " WHERE Rest > 0 ";

    	String itemGroupFilter = Q.getItemGroupFilter("i",mCurrentGroup);
		if(itemGroupFilter.length()>0)
			where = (where.length()>0) ? where + " AND " + itemGroupFilter : " WHERE " + itemGroupFilter;

        String sql = " SELECT i.ItemID, i.ScreenName, i.EAN, i.Price*(1+i.ItemTax) as Price, i.MinPrice*(1+i.ItemTax) as MinPrice, i.ImageIndex, i.SortID," +
        					" coalesce(r.Rest,0)-coalesce(r.SaledQnt,0) AS Rest " +
		  			 " FROM Items i " +
		  			 " LEFT JOIN Rest r ON r.ItemID = i.ItemID " +
		  			 where +
		  			 " ORDER BY " + sortColumn + " " + sortOrder; 
    	Log.v("DEN1",sql);
    	mCursor = Db.getInstance().selectSQL(sql);    	
    	
    	mGrid.setCursor(mCursor);
    	
    	if(mCursor.getCount()>0)
    	{
    		mGrid.setSelectedRow(0);
    		
    	}
    }
    
    //--------------------------------------------------------------
    private class CellListener extends DataGrid.BaseCellListener 
    {
        @Override public void onCellSelected(int row, int column) 
        { 
        	try
        	{
		    	if(mCursor.moveToPosition(row))
		    	{
		    		AntContext.getInstance().curItemId = mCursor.getInt(mCursor.getColumnIndex("ItemID"));
		    		
			    	if(infoPanel!=null)
				    	infoPanel.displayTotals();
		    	}
        	}
    		catch(Exception ex)
    		{
   	    		MessageBox.show(ItemListForm.this, getResources().getString(R.string.form_title_itemList), getResources().getString(R.string.item_list_exceptionItemInfo));
    			ErrorHandler.CatchError("Exception in onCellSelected", ex);
    		}
        } 
        @Override public void onCellLongPress(int row, int column) 
        { 
        	try
        	{
		    	if(mCursor.moveToPosition(row))
		    	{
		    		AntContext.getInstance().curItemId = mCursor.getInt(mCursor.getColumnIndex("ItemID"));
		    		showDialog(IDD_ITEM);
		    	}
        	}
    		catch(Exception ex)
    		{
   	    		MessageBox.show(ItemListForm.this, getResources().getString(R.string.form_title_itemList), getResources().getString(R.string.item_list_exceptionItemInfo));
    			ErrorHandler.CatchError("Exception in onCellLongPress", ex);
    		}
        	
        } 
        
        @Override public void onHeaderClicked(int column, boolean sortOrderChanged)
        {
        	//header click implies changes in sort order. we need to re-read Cursor and set it to DataGrid
        	fillGridWithData();
        }        
    } 
    
    //------------------------------------------------------------------------------------
    public void applyNewItemGroup(ItemGroup itemGroup)
    {
	    if( itemGroup.id != mCurrentGroup.id)
		{
	    	mCurrentGroup = itemGroup;
	        mTextItemGroup.setText(mCurrentGroup.name);
	        fillGridWithData();		    	    	    		
		}
    }
    
    //--------------------------------------------------------------    
    @Override protected Dialog onCreateDialog(int id)
    {
    	try
    	{
	    	switch(id)
	    	{
	    		case IDD_SELECT_ITEM_GROUP:
	    		{    			
	    			DialogInterface.OnClickListener cancelClickListener = new DialogInterface.OnClickListener() 
	    			{					
	    				@Override public void onClick(DialogInterface dialog, int which) 
	    				{
	    					removeDialog(IDD_SELECT_ITEM_GROUP);
	    				}
	    			};
	    			
	    			selectGroupDlg = new DocSaleSelectGroupDialog();    			
	    			Dialog dlg = selectGroupDlg.onCreate(this, cancelClickListener, false, 0, itemGroupSelector, mCurrentGroup);
	    			
	    			return dlg;
	    		}

	    		case IDD_ITEM:
	    		{
	    			itemDlg = new ItemDialog();
	    			Dialog dlg = itemDlg.onCreate(this);
	
	    			itemDlg.setOkClickListener(new DialogInterface.OnClickListener() 
	    			{					
	    				@Override public void onClick(DialogInterface dialog, int which) { removeDialog(IDD_ITEM); }
	    			});    			
	    			
	    			return dlg;    			
	    		}   		
	    		
        	
	    		default:
	    			return null;
	    	}
    	}
		catch(Exception ex)
		{
	    	MessageBox.show(this, getResources().getString(R.string.form_title_itemList), getResources().getString(R.string.document_exceptionOnCreateDialog));
			ErrorHandler.CatchError("Exception in onCreateDialog", ex);
		}

		return null;
    }
    
}

