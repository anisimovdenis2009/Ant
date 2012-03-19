package com.app.ant.app.Controls;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Scroller;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.lang.Math;

import com.app.ant.app.Activities.MessageBox;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;


public class DataGrid extends View 
{
	public static final int DATA_TYPE_INTEGER 	= 4;
	public static final int DATA_TYPE_DATE 		= 91;
	public static final int DATA_TYPE_DOUBLE 	= 8;
	public static final int DATA_TYPE_VARCHAR 	= 12;
	public static final int DATA_TYPE_MSU 		= 998;
	public static final int DATA_TYPE_MONEY 	= 999;	
	
	/** ������� ���������� � ������� �����*/
	public enum SortOrder { /** ���������� �� �����������*/ ASCENDING, 
							/** ���������� �� ��������*/ DESCENDING };
	
	private GestureDetector mGestureDetector;
	
	private Paint mTextPaint;
	private Paint mHeaderTextPaint;
	private Paint mHeaderPaint;	
	private Path    mAscendingMarkPath = new Path();
	private Path    mDescendingMarkPath = new Path();
	
	private String mText;
	private int mAscent;
	int mMarkHeight;
	int mMarkWidth;

	private int mHeaderHeight = 50;
	private int mRowHeight = 50;
	private int mHeaderTextSize = 16;
	private int mTextSize = 16;
	
	int selectedRowColor = Color.rgb(241, 240, 156); //Color.BLUE;
	int selectedCellColor = Color.rgb(255, 186, 0); //Color.GREEN;
	
	private int mRows;
	private int mTopRow;
	private int mSelectedRow;
	private int mSelectedColumn;
	private int mPartialRows;
	private int mExtraBottomPadding = 0;
	private boolean mCellSelectEnabled = true;
	
	//scroll and column rlesize
	private final int mColumnBorderSelectionAccuracy = 5;
	private final int mColumnMinSize = 20;
	private int mResizeColumn = -1;
	private int mResizeColumnCurX = 0;
	
	private int mRecordCount;
	private int mHeaderTextIndent = 2;
	
	//database
	private Cursor mCursor = null;
	private int mIdentityColumn = -1;
	
	//sorting
	int mSortColumn = -1;
	SortOrder mSortOrder;
	int mDefaultSortColumn;
	
	//stores edited values. Paint procedure should look here first
	Map<Integer, Object[]> mEditedHash;
	
	private FlingRunnable mFlingRunnable;
	private boolean mFlingInProgress = false;
	private boolean mFlingJustStopped = false;
	
	private int mFlingVelocity = 15;
	
	public class GridState
	{
    	int selectedRow;
    	int scrollPos;
		String sortField;
		DataGrid.SortOrder sortOrder;
	}
	
	//-------------------------------------------------------------------------------------------------------
	/** Callback - ��������� ��� ��������� ������� Grid. ����������� ������, ���������� Grid.*/ 
    public interface ICellListener 
    {    	
    	/** ������� �� ������*/
        abstract void onCellSelected(int row, int column);
        /** ���������� ������� �� ������*/
        abstract void onCellLongPress(int row, int column);
        /** �������������� ������*/
        abstract void onCellEdit(int row, int column, int dataType);
        /**������� �� ��������� ������� (��� ���� �������� ������� ����������*/
        abstract void onHeaderClicked(int column, boolean sortOrderChanged);
        /** ����������� � ��������� ������ �������*/
        abstract void onColumnWidthChanged(int column);
        /** ���������� �������� ������
         * @param row ������ 
         * @param column �������
         * @param values �������� ���������� �������� � ��������� ������
         * @return �������� ������
         */
        abstract Object onCellCalculate(int row, int column, Object[] values);
        /** ���������� ����� ������ 
         * @return ����� ��� ������
         */
        abstract CellStyle onCalculateRowStyle();
        
        /** ����������� ����� ��� �������*/
        abstract CellStyle onCalculateColumnStyle(CellStyle currentStyle, ColumnInfo column);
        
        /** ������������ �������� ��� �����������*/ 
        abstract Bitmap onGetImage(int imageIndex);
    } 
    private ICellListener mCellListener; 
    
    /** ������������ ������ ������� ���������� ������� Grid. ����� ������������� ������������ ������� Grid.*/ 
    public static class BaseCellListener implements ICellListener
    {
        @Override public void onCellSelected(int row, int column) { } 
        @Override public void onCellLongPress(int row, int column){ } 
        @Override public void onCellEdit(int row, int column, int dataType) { }        
        @Override public void onHeaderClicked(int column, boolean sortOrderChanged) { }
        @Override public void onColumnWidthChanged(int column){ }
        @Override public Object onCellCalculate(int row, int column, Object[] values) { return null; }        
        @Override public CellStyle onCalculateRowStyle() { return CellStyleCollection.getDefault(); }
        @Override public CellStyle onCalculateColumnStyle(CellStyle currentStyle, ColumnInfo column) { return currentStyle; }
        @Override public Bitmap onGetImage(int imageIndex) { return null; }
    }
    
	//-------------------------------------------------------------------------------------------------------    
    /** ������ ����� ������ �����*/
    public static class CellStyle implements Cloneable
    {
    	public static final int TEXT_STYLE_BOLD				=1;
    	public static final int TEXT_STYLE_ITALIC			=2;
    	public static final int TEXT_STYLE_UNDERLINE		=4;
    	public static final int TEXT_STYLE_STRIKE_THROUGH	=8;
    	
    	public String fontName;
    	public int fontStyle;
    	public boolean haveTextColor;
    	public int textColor;
    	public boolean haveBgColor;
    	public int bgColor;    	
    	public int imageIndex;
    	public int priority;
    	public int expandedImageIndex;
    	
    	public void setExpandedImageIndex(int imgIndex)	{ expandedImageIndex = imgIndex; }    	
    	public void setImageIndex(int imgIndex) { imageIndex = imgIndex; }
    	
    	public CellStyle(String fontName, int fontStyle, boolean haveTextColor, int textColor, boolean haveBgColor, 
    							int bgColor, int imageIndex, int priority, int expandedImageIndex)
    	{
    		this.fontName = fontName;
    		this.fontStyle = fontStyle;
    		this.haveTextColor = haveTextColor;
    		this.textColor = textColor;
    		this.haveBgColor = haveBgColor;
    		this.bgColor = bgColor;
    		this.imageIndex = imageIndex;
    		this.priority = priority;
    		this.expandedImageIndex = expandedImageIndex; 
    	}
    	
        public Object clone()
        {
            try
            {
                return super.clone();
            }
	        catch( CloneNotSupportedException e )
	        {
                return null;
            }
        }     	
    	
		//anthill values: 1=Bold; 2=Italic; 4=Underline; 8=Strikeout; 0=Regular;
		//allowable text styles in Android are:  NORMAL, BOLD, ITALIC, MONOSPACE et al. (see Typeface documentation)

    	public int getTextFlags()
    	{
			//allowable text flags are: STRIKE_THRU_TEXT_FLAG, UNDERLINE_TEXT_FLAG and more (see Paint class documentation)
			int textFlags = 0;
			if( (fontStyle & TEXT_STYLE_UNDERLINE)>0 )
				textFlags |= Paint.UNDERLINE_TEXT_FLAG;
			if( (fontStyle & TEXT_STYLE_STRIKE_THROUGH)>0 )
				textFlags |= Paint.STRIKE_THRU_TEXT_FLAG;
			
			return textFlags;
    	}
    	
    	public boolean isItalic()
    	{
    		return (fontStyle & TEXT_STYLE_ITALIC)>0? true:false;
    	}
    	
    	public Typeface getTypeface()
    	{			
			int textStyle = 0;
			if( (fontStyle & TEXT_STYLE_BOLD)>0 )
				textStyle |= Typeface.BOLD;
			if( (fontStyle & TEXT_STYLE_ITALIC)>0 )
				textStyle |= Typeface.ITALIC;
			
			return Typeface.create(fontName, textStyle);	
    	}
    	
    }

    /** ��������� ��������� ������ ����� �����. ��������� �������� ������ �� ���� ������, ���������� ��������������� ������*/
    public static class CellStyleCollection
    {
    	private Map<Integer, CellStyle> mCellStyles = new HashMap<Integer, CellStyle>();    	
    	private static CellStyle defaultStyle = new CellStyle("Arial", 0, true, 0xFF000000, true, 0xFFFFFFFF, -1, 100000, -1);
    	
    	public void add(int styleId, CellStyle style)
    	{
    		mCellStyles.put(styleId, style);
    	}
    	
    	public CellStyle get(int styleId)
    	{
    		CellStyle style = mCellStyles.get(styleId);
    	
    		if(style!=null)
    			return style;
    		else
    			return getDefault();
    	}
    	
    	public static CellStyle getDefault()
    	{
    		return defaultStyle;
    	}   	
    	
    	public void loadFromDatabase()
    	{   		
			String sql = "SELECT StyleID, Font, FontStyle, FontColor, BackgroundColor, ImageIndex, PaintPriority FROM Styles";
			
			Cursor cursor = Db.getInstance().selectSQL(sql);
			
			for(int i=0; i<cursor.getCount(); i++)
			{
				cursor.moveToPosition(i);
				int fontStyleColumn = cursor.getColumnIndex("FontStyle");
				int textColorColumn = cursor.getColumnIndex("FontColor");
				int bgColorColumn = cursor.getColumnIndex("BackgroundColor");
				int imgIndexColumn = cursor.getColumnIndex("ImageIndex");
				int priorityColumn = cursor.getColumnIndex("PaintPriority");
				
				boolean haveTextColor = !cursor.isNull(textColorColumn);
				boolean haveBgColor = !cursor.isNull(bgColorColumn);
				
				int styleID = cursor.getInt(cursor.getColumnIndex("StyleID"));
				String font = cursor.getString(cursor.getColumnIndex("Font"));
				int fontStyle = cursor.isNull(fontStyleColumn)? -1:cursor.getInt(fontStyleColumn);
				int textColor = haveTextColor? cursor.getInt(textColorColumn):0xFF000000;
				int bgColor = haveBgColor ? cursor.getInt(bgColorColumn):0xFFFFFFFF; 
				int imgIndex = cursor.isNull(imgIndexColumn)? -1:cursor.getInt(imgIndexColumn);
				int priority = cursor.isNull(priorityColumn)? -1:cursor.getInt(priorityColumn);				
				
				
				CellStyle style = new CellStyle( font, fontStyle, haveTextColor, textColor, haveBgColor, bgColor, imgIndex, priority, -1);
				add(styleID, style );
			}
			cursor.close();
			
    	}
    	
    	public CellStyle getCompositeStyle(ArrayList<Integer> styleIDs)
    	{
    		//no styles to unite, return default
    		if(styleIDs.size() == 0)
    			return getDefault();
    		
    		if(styleIDs.size() > 1)
    		{
    			int i=0;    			
    		}
    		
    		//create map to sort by priority
    		SortedMap<Integer, CellStyle> stylesMap = new TreeMap<Integer, CellStyle>(); 
    		
    		int maxPriority = 0;
    		for(int i=0; i<styleIDs.size(); i++)
    		{
    			CellStyle cellStyle = get(styleIDs.get(i));
    			stylesMap.put(cellStyle.priority, cellStyle);
    			
    			maxPriority = Math.max(maxPriority, cellStyle.priority);
    		}
    		
    		//add default style
    		stylesMap.put(maxPriority+1, getDefault());
    		
    		//create composite style
    		CellStyle compositeStyle = new CellStyle("", -1, false, -1, false, -1, -1, -1, -1);    		
			Collection<CellStyle> collection = stylesMap.values();
			
			Iterator<CellStyle> it = collection.iterator();			
	        while (it.hasNext()) 
	        {
	        	CellStyle style = it.next();
	        	
	        	if(Convert.isNullOrBlank(compositeStyle.fontName))
	        		compositeStyle.fontName = style.fontName;
	        	if(compositeStyle.fontStyle == -1)
	        		compositeStyle.fontStyle = style.fontStyle;	        	
	        	if(compositeStyle.haveTextColor == false)
	        	{
	        		compositeStyle.haveTextColor = style.haveTextColor;
	        		compositeStyle.textColor = style.textColor;
	        	}
	        	if(compositeStyle.haveBgColor == false)
	        	{
	        		compositeStyle.haveBgColor = style.haveBgColor;
	        		compositeStyle.bgColor = style.bgColor;
	        	}
	        	if(compositeStyle.imageIndex == -1)
	        		compositeStyle.imageIndex = style.imageIndex;
	        } 
    		
    		return compositeStyle;
    	}
    }
    
    private CellStyleCollection mCellStyles = new CellStyleCollection();
   
	//-------------------------------------------------------------------------------------------------------
    public static final int GRID_COLUMN_DEFAULT = 0;
    public static final int GRID_COLUMN_HIDDEN = 1;
    public static final int GRID_COLUMN_EDITABLE = 2;
    public static final int GRID_COLUMN_CALCULABLE = 4;    
    public static final int GRID_COLUMN_FIXED = 8;
    public static final int GRID_COLUMN_IMAGE = 16;
    public static final int GRID_COLUMN_NO_VALUE = 32;
    public static final int GRID_COLUMN_DENY_SORTING = 64;
    
    /** ���������� � ������� �����*/
    public static class ColumnInfo
    {
		private int     id;
    	private String	header;
    	private String	descr;
		private int		dataType;
		private int		width;
		private int flags;
		private String	dbField;    	
		private int 	ordinalIndex; 
		private short   linked;
		
		private int 	editableIndex = -1;

		//additional auxiliary variables needed for display
		private int     left;
		private int     right;
		private int		scrolledLeft;
		private int		scrolledRight;
		private int		clipLeft;
		private int		clipRight;	
		
    	public ColumnInfo(int id, String header, int dataType, int width, int flags, String dbField, int ordinal, short linked, String descr)
    	{
			this.ordinalIndex = ordinal;
			this.id = id;
			this.header = header;
			this.descr = descr;
			this.width = width;
			this.dataType = dataType;

			this.flags = flags;
			this.dbField = dbField;
			this.linked = linked;
    	}
    	
    	public void setCalculable(boolean calculable) 
		{
    		this.flags = calculable ? this.flags | GRID_COLUMN_CALCULABLE : ((this.flags | GRID_COLUMN_CALCULABLE) ^ GRID_COLUMN_CALCULABLE);
		} 
    	
    	public boolean isCalculable() { return ((flags & GRID_COLUMN_CALCULABLE) > 0); }
		/**
		 * @return the header
		 */
		public String getHeader(){ return header; }
		/**
		 * @return the dataType
		 */
		public int getDataType(){ return dataType; }
		/**
		 * @param width the width to set
		 */
		public void setWidth(int width){ this.width = width; }
		/**
		 * @return the width
		 */
		public int getWidth() { return width; }
		/**
		 * @param editable the editable to set
		 */
		public void setEditable(boolean editable) 
		{ 
			this.flags = editable ? this.flags | GRID_COLUMN_EDITABLE : ((this.flags | GRID_COLUMN_EDITABLE) ^ GRID_COLUMN_EDITABLE); 
		}
		/**
		 * @return the editable
		 */
		public boolean isEditable() { return ((flags&GRID_COLUMN_EDITABLE) > 0); }
		/**
		 * @param visible the visible to set
		 */
		public void setVisible(boolean visible) 
		{ 
			this.flags = visible ? ((this.flags | GRID_COLUMN_HIDDEN) ^ GRID_COLUMN_HIDDEN): this.flags | GRID_COLUMN_HIDDEN; 
		}
		/**
		 * @return the visible
		 */
		public boolean isVisible() { return !((flags & GRID_COLUMN_HIDDEN) > 0); }
		/**
		 * @return the dbField
		 */
		public String getDbField() { return dbField; }
		/**
		 * @return the editableIndex
		 */
		public int getEditableIndex() { return editableIndex; }
		/**
		 * @param editableIndex the editableIndex to set
		 */
		public void setEditableIndex(int editableIndex) { this.editableIndex = editableIndex; }
		/**
		 * @param ordinalIndex the ordinalIndex to set
		 */
		public void setOrdinalIndex(int ordinalIndex) { this.ordinalIndex = ordinalIndex; }
		/**
		 * @return the ordinalIndex
		 */
		public int getOrdinalIndex() { return ordinalIndex; }
		/**
		 * @return the flags
		 */
		public int getFlags(){ return flags;}
		/**
		 * @return the fixed
		 */
		public boolean isFixed() { return ((flags & GRID_COLUMN_FIXED) > 0); }
		/**
		 * @param fixed the fixed to set
		 */
		public void setFixed(boolean fixed) { this.flags = fixed ? this.flags | GRID_COLUMN_FIXED : ((this.flags | GRID_COLUMN_FIXED) ^ GRID_COLUMN_FIXED); }
		/**
		 * @return the id
		 */
		public int getId() { return id; }
	
		public boolean hasImage() { return ((flags & GRID_COLUMN_IMAGE) > 0); }
		public boolean hasNoValue() { return ((flags & GRID_COLUMN_NO_VALUE) > 0); }
		public boolean isSortingDenied() { return ((flags & GRID_COLUMN_DENY_SORTING) > 0); }
		/**
		 * @return the linked
		 */
		public short getLinked() { return linked; }

		public String getDescription()
		{
			return descr;
		}
		public void setDescription(String descr)
		{
			this.descr = descr;
		}
    };

	//-------------------------------------------------------------------------------------------------------
    /** ��������� ������� �����. �������� ������ �� ����������� �������/���������� ����������� �������*/ 
    public static class GridColumns implements Cloneable
    {
    	private ColumnInfo[] mColumnArray;    	
    	
    	public ColumnInfo getColumn(int column) { return mColumnArray[column]; }
    	public int getLength() { return mColumnArray.length; };
    	public int getNumEditableColumns() { return mNumEditableColumns; };

		private int mScrollPosX;
		    
    	public int mNumEditableColumns;
    	private int mSumColumnWidth;
		public int mFirstVisibleColumnWidth;
    	
    	
    	public GridColumns(ColumnInfo[] columnArray) 
    	{
    		mColumnArray = columnArray;
    	}
    	
    	public GridColumns(int columnsSetId) 
    	{
           	String sql = "select Header, DbField, DataType, Ordinal, Width, Flags, Linked, ID, Description from GridColumns where SetID = " + columnsSetId + " order by Ordinal ";
           	
    		Cursor cursor = Db.getInstance().selectSQL(sql);
    		
    		mColumnArray = new DataGrid.ColumnInfo[cursor.getCount()];
    		
    		for(int i = 0; i < cursor.getCount(); i++)
    		{
    			cursor.moveToPosition(i);
    			
    			String header = cursor.getString(cursor.getColumnIndex("Header"));
    			String descr = cursor.getString(cursor.getColumnIndex("Description"));
    			String dbField = cursor.getString(cursor.getColumnIndex("DbField"));
    			int dataType = cursor.getInt(cursor.getColumnIndex("DataType"));
    			int width = cursor.getInt(cursor.getColumnIndex("Width"));
    			int flags = cursor.getInt(cursor.getColumnIndex("Flags"));
    			int id = cursor.getInt(cursor.getColumnIndex("ID"));
    			int ordinal = cursor.getInt(cursor.getColumnIndex("Ordinal"));    			
    			short linked = cursor.getShort(cursor.getColumnIndex("Linked"));
    			
    			mColumnArray[i] = new DataGrid.ColumnInfo(id, header, dataType, width, flags, dbField, ordinal, linked, descr);
    		}
    		cursor.close();
    		cursor = null; 
    	}
    	
    	public GridColumns clone() throws CloneNotSupportedException
    	{
    		GridColumns cloned = (GridColumns)super.clone();    		
    		cloned.mColumnArray = mColumnArray.clone();
    		
    		return cloned;
    	}    	

    	public void init()
    	{
    		mScrollPosX = 0;
    		calculatePaintParams();    		
    					
			//
			//calculate number of editable columns. store editable index in column info
			//
			mNumEditableColumns = 0;
			int editableIndex = 0;			
			for(ColumnInfo column: mColumnArray)
			{
				if(column.isEditable())
				{
					column.setEditableIndex(editableIndex);
					editableIndex++;
				}
			}				
			mNumEditableColumns = editableIndex;			
    	}
    	
    	public ColumnInfo getColumnByDbField(String name)
    	{
    		for(int i=0; i<mColumnArray.length; i++ )
    			if(mColumnArray[i].getDbField().equals(name))
    				return mColumnArray[i];
    		
    		return null;
    	}

    	public int getColumnIndexByDbField(String name)
    	{
    		for(int i = 0; i<mColumnArray.length; i++ )
    			if(mColumnArray[i].getDbField().equals(name))
    				return i;
    		
    		return -1;
    	}

		private void calculatePaintParams()
		{
//			try
//			{
				mSumColumnWidth = 0;
				mFirstVisibleColumnWidth = 0;
			
				for(ColumnInfo column: mColumnArray)
	    		{
	    			if(column.isVisible())
	    			{
				   		if(column.isFixed())
				   			mFirstVisibleColumnWidth += column.getWidth();

	    				column.left = mSumColumnWidth;
	    				column.right = column.left + column.getWidth();
	    				column.scrolledLeft = column.isFixed() ? column.left : column.left - mScrollPosX; //for scrollable columns, scroll is taken to effect. substract scroll value from x
	    				column.scrolledRight = column.scrolledLeft + column.getWidth();      				
						column.clipLeft = column.isFixed() ? column.scrolledLeft : Math.max(column.scrolledLeft, mFirstVisibleColumnWidth); //clip scrolled column differently
						column.clipRight = column.scrolledRight;
	    				
	    				mSumColumnWidth += column.getWidth();
	    			}
	    		}
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//			}
		}     	

    	public void setColumnVisibility(int columnIdx, boolean visible)
    	{
    		if( columnIdx < mColumnArray.length)
    		{    		
    			mColumnArray[columnIdx].setVisible(visible);    			
    			calculatePaintParams();
    		}
    	}
    	
    	public void setColumnWidth(int columnIdx, int width)
    	{
    		if( columnIdx < mColumnArray.length)
    		{
    			mColumnArray[columnIdx].setWidth(width);
    			calculatePaintParams();		
    		}
    	}

    	public void moveColumnUp(int columnIdx)
    	{
    		if (columnIdx < 1 || columnIdx > mColumnArray.length) return; //not first col
    		   			    		
			ColumnInfo col = mColumnArray[columnIdx];    			
			ColumnInfo col2 = mColumnArray[columnIdx - 1];
			
			if (col2.isFixed()) return;
			
			col.setOrdinalIndex(col.getOrdinalIndex() - 1);
			col2.setOrdinalIndex(col.getOrdinalIndex() + 1);
			
			mColumnArray[columnIdx] = col2;
			mColumnArray[columnIdx - 1] = col;
    	}
    	
    	public void moveColumnDown(int columnIdx)
    	{
    		if (columnIdx >= mColumnArray.length) return; //not last col
    		
			ColumnInfo col = mColumnArray[columnIdx];    			    					
			col.setOrdinalIndex(col.getOrdinalIndex() + 1);
			
			ColumnInfo col2 = mColumnArray[columnIdx + 1];
			col2.setOrdinalIndex(col.getOrdinalIndex() - 1);
			
			mColumnArray[columnIdx] = col2;
			mColumnArray[columnIdx + 1] = col;
    	}
    	
    	public void setColumnVisibilityByLink(boolean visible, short linked)
    	{    		
			for (int i = 0; i < mColumnArray.length; i++)
			{
				ColumnInfo col = mColumnArray[i];
				if (col.getLinked() == linked) col.setVisible(visible);				
			}    		
    		calculatePaintParams();
    	}
    	
    	public void save()
    	{
    		try
			{
    			Db db = Db.getInstance();
    			String [] strArr = new String[mColumnArray.length];
				for (int i = 0; i < mColumnArray.length; i++)
				{
					ColumnInfo col = mColumnArray[i];
					strArr[i] = Q.columnInfo_getUpdateCommandText(col.getOrdinalIndex(), col.getWidth(), col.getFlags(), col.getId());
				}
				db.execMultipleSQL(strArr);
			}
			catch (Exception ex)
			{
				MessageBox.show(AntContext.getInstance().getContext(), "error", ex.getLocalizedMessage());
			}
    	}
    }
    
    private GridColumns mColumns;
    
	//-------------------------------------------------------------------------------------------------------
	// Sets the text to display in this label.  @param text The text to display. This will be drawn as one line.
	public void setText(String text) {  mText = text; requestLayout(); invalidate(); } 
	//Sets the text color for this label. @param color ARGB value for the text
	public void setTextColor(int color) {  mTextPaint.setColor(color);  invalidate(); }
	//Sets the text size for this label. @param size Font size
	//public void setTextSize(int size) { mTextPaint.setTextSize(size); requestLayout(); invalidate(); }	
	public void setCursor(Cursor cursor) 
	{ 
		mCursor = cursor; 
		mRecordCount = mCursor.getCount(); 
		ResetGrid(); 
		invalidate(); 
	}
    public void setCellListener(ICellListener cellListener) { mCellListener = cellListener; }    
    public void setSortColumn(int column) { mSortColumn = column; }
    public void setDefaultSortColumn(int column) { mDefaultSortColumn = column; setSortColumn(mDefaultSortColumn); }
    public void setSortColumnByDbField(String dbField) 
    {
        int sortColumnIndex = mColumns.getColumnIndexByDbField(dbField);
        sortColumnIndex = sortColumnIndex == -1 ? 0 : sortColumnIndex;
        setSortColumn(sortColumnIndex);
    }    
    public void setDefaultSortColumnByDbField(String dbField) 
    {
        int sortColumnIndex = mColumns.getColumnIndexByDbField(dbField);
        sortColumnIndex = sortColumnIndex == -1 ? 0 : sortColumnIndex;
        setDefaultSortColumn(sortColumnIndex);
    }   
    
    public void 		setSortOrder(SortOrder order) { mSortOrder = order; }     
    public int 			getSortColumn() { return mSortColumn; }
    public String		getSortColumnField() {  return getDbFieldForColumn(getSortColumn()); }
    public SortOrder	getSortOrder() { return mSortOrder; }
    public void setIdentityColumn(int column) { mIdentityColumn = column; }
    public void setRowHeight(int rowHeight) { mHeaderHeight = rowHeight; mRowHeight = rowHeight; }
    public void setTextSize(int textSize) { mTextSize = textSize; mTextPaint.setTextSize(mTextSize); }    
    public void setHeaderTextSize(int textSize) { mHeaderTextSize = textSize; mHeaderTextPaint.setTextSize(mTextSize); }
    public void setFlingVelocity(int velocity) { mFlingVelocity = velocity; }    
    public GridColumns getColumns() { return mColumns; }
	public void setColumns(GridColumns columns) { mColumns = columns; mColumns.init(); }
	public void setCellStyles(CellStyleCollection styles) { mCellStyles = styles; }
	public void setAdditionalBottomPadding(int padding) { mExtraBottomPadding = padding; } 
	
	public void setSelectionEnabled(boolean enableSelection ) { mCellSelectEnabled = enableSelection; } 
	public int getSelectedRow() { return mSelectedRow;}
	public void setSelectedRow(int selectedRow)
	{ 
		mSelectedRow=selectedRow;
		if(mCellListener!=null)
			mCellListener.onCellSelected(mSelectedRow, mSelectedColumn);
	}


    public void setSelectedRow(int selectedRow,int mSelectedColumn)
	{
		mSelectedRow=selectedRow;
		if(mCellListener!=null)
			mCellListener.onCellSelected(mSelectedRow, mSelectedColumn);
	}
	public void setSelectedColumn(int selectedColumn)
	{
		if(mSelectedColumn != selectedColumn)
		{
			mSelectedColumn = selectedColumn;
			invalidate();
		}
	}
	public Map<Integer, Object[]> getEditedValues() { return mEditedHash; }
	public void setEditedValues(Map<Integer, Object[]> editedValues) { mEditedHash = editedValues; }

	public int getScrollPos() { return mTopRow; }
	public void scrollToPos(int row)
	{
		mTopRow = Math.max(0, Math.min(computeVerticalScrollRange(), row));
        invalidate();
	}
	
	//-------------------------------------------------------------------------------------------------------
	// Constructor.  This version is only needed if you will be instantiating the object manually (not from a layout XML file). @param context
	public DataGrid(Context context) 
	{
	   super(context);
	   initDataGrid();
	   
	   //my
	   setText("Red");
	   setTextColor(0xFFFF0000);
	}

	//-------------------------------------------------------------------------------------------------------
	// Construct object, initializing with any attributes we understand from a layout file. These attributes are defined in SDK/assets/res/any/classes.xml.
	// @see android.view.View#View(android.content.Context, android.util.AttributeSet)
	public DataGrid(Context context, AttributeSet attrs) 
	{
	   super(context, attrs);
	   initDataGrid();
	
	   /*TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LabelView);
	   
	   CharSequence s = a.getString(R.styleable.LabelView_text);
	   if (s != null) 
	   {
	       setText(s.toString());
	   }
	
	   // Retrieve the color(s) to be used for this view and apply them.
	   // Note, if you only care about supporting a single color, that you
	   // can instead call a.getColor() and pass that to setTextColor().
	   setTextColor(a.getColor(R.styleable.LabelView_textColor, 0xFF000000));
	
	   int textSize = a.getDimensionPixelOffset(R.styleable.LabelView_textSize, 0);
	   if (textSize > 0) 
	   {
	       setTextSize(textSize);
	   }     
	
	   a.recycle();*/
	   
	   setText("Red");
	   setTextColor(0xFF000000);	   
	}
	
	public void replaceColumns(GridColumns columns) 
	{ 
		mColumns = columns; 
		mColumns.calculatePaintParams();
		mSelectedColumn = -1;
		invalidate();	
	}

	//-------------------------------------------------------------------------------------------------------
	public void setColumnVisibility(int columnIdx, boolean visible)
	{
		if( columnIdx < mColumns.getLength())
		{
			mColumns.setColumnVisibility(columnIdx, visible);			
			mSelectedColumn = -1;
			invalidate();			
		}
	}
	
	//-------------------------------------------------------------------------------------------------------
	public void setColumnWidth(int columnIdx, int width)
	{
		if( columnIdx < mColumns.getLength())
		{
			mColumns.setColumnWidth(columnIdx, width);
			mSelectedColumn = -1;
			invalidate();			
		}
	}
	
	//-------------------------------------------------------------------------------------------------------
	private final void initDataGrid() 
	{	
		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(mTextSize);
		mTextPaint.setColor(0xFF000000);
		mAscent = (int) mTextPaint.ascent();
		
		mHeaderTextPaint = new Paint();
		mHeaderTextPaint.setAntiAlias(true);
		mHeaderTextPaint.setTextSize(mHeaderTextSize);
		mHeaderTextPaint.setColor(0xFF000000);		
		
		// Construct paths
		/*mAscendingMarkPath.moveTo(4, 0);
		mAscendingMarkPath.lineTo(8, 12);
		mAscendingMarkPath.lineTo(0, 12);*/
		mMarkHeight = -mAscent;
		mMarkWidth = mMarkHeight/2;
		mAscendingMarkPath.moveTo(mMarkWidth/2, 0);
		mAscendingMarkPath.lineTo(mMarkWidth, mMarkHeight);
		mAscendingMarkPath.lineTo(0, mMarkHeight);		
		mAscendingMarkPath.close(); 
		
		mDescendingMarkPath.moveTo(0, 0);
		mDescendingMarkPath.lineTo(mMarkWidth, 0);
		mDescendingMarkPath.lineTo(mMarkWidth/2, mMarkHeight);		
		mDescendingMarkPath.close();
	   
		mHeaderPaint = new Paint();
		mHeaderPaint.setColor(Color.GRAY);
		mHeaderPaint.setStrokeWidth(1);
		mHeaderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
	   
		setPadding(3, 3, 3, 3);  
	   
		mRecordCount = 0;
		mGestureDetector = new GestureDetector(new GestureListener());	   
       
		setHorizontalScrollBarEnabled(true);
		setVerticalScrollBarEnabled(true);
		mEditedHash = new HashMap<Integer, Object[]>();

		ResetGrid();
	}

	//-------------------------------------------------------------------------------------------------------	
	private final void ResetGrid()
	{
		mTopRow = 0;
		if(mColumns!=null)
			mColumns.mScrollPosX = 0;
		mSelectedRow = -1;
		mSelectedColumn = -1;
	}

	
	//-------------------------------------------------------------------------------------------------------

	// @see android.view.View#measure(int, int)
	@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
	   setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}

	//-------------------------------------------------------------------------------------------------------
	/**
	* Determines the width of this view
	* @param measureSpec A measureSpec packed into an int
	* @return The width of the view, honoring constraints from measureSpec
	*/
	private int measureWidth(int measureSpec) 
	{
	   int result = 0;
	   int specMode = MeasureSpec.getMode(measureSpec);
	   int specSize = MeasureSpec.getSize(measureSpec);
	
	   if (specMode == MeasureSpec.EXACTLY) {
	       // We were told how big to be
	       result = specSize;
	   } 
	   else 
	   {
	       // Measure the text
	       result = (int) mTextPaint.measureText(mText) + getPaddingLeft()+ getPaddingRight();
	       if (specMode == MeasureSpec.AT_MOST) 
	       {
	           // Respect AT_MOST value if that was what is called for by measureSpec
	           result = Math.min(result, specSize);
	       }
	   }
	
	   return result;
	}

	//-------------------------------------------------------------------------------------------------------
	/**
	* Determines the height of this view
	* @param measureSpec A measureSpec packed into an int
	* @return The height of the view, honoring constraints from measureSpec
	*/
	private int measureHeight(int measureSpec) 
	{
	   int result = 0;
	   int specMode = MeasureSpec.getMode(measureSpec);
	   int specSize = MeasureSpec.getSize(measureSpec);
	
	   //mAscent = (int) mTextPaint.ascent();
	   if (specMode == MeasureSpec.EXACTLY) 
	   {
	       // We were told how big to be
	       result = specSize;
	   } 
	   else 
	   {
	       // Measure the text (beware: ascent is a negative number)
	       //result = (int) (-mAscent + mTextPaint.descent()) + getPaddingTop() + getPaddingBottom();
	       
	       if (specMode == MeasureSpec.AT_MOST) 
	       {
	           // Respect AT_MOST value if that was what is called for by measureSpec
	           //result = Math.min(result, specSize);
	    	   result = specSize;
	       }
	   }
	   return result;
	}

	//-------------------------------------------------------------------------------------------------------
	private CellStyle getStyleForCurrentRow()
	{
	    //get row style settings 
	    if(mCellListener!=null)
	    {
	    	CellStyle style = mCellListener.onCalculateRowStyle();
	    	if(style!=null)
	    		return style;
	    }

	    return CellStyleCollection.getDefault();
	}

	//-------------------------------------------------------------------------------------------------------
	private CellStyle getStyleForCurrentColumn(CellStyle currentStyle, ColumnInfo column )
	{
	    //apply column style 
	    if(mCellListener!=null)
	    {
	    	CellStyle style = mCellListener.onCalculateColumnStyle(currentStyle, column);
	    	if(style!=null)
	    		return style;
	    }

	    return currentStyle;
	}
	
	
	//-------------------------------------------------------------------------------------------------------
	// Render the text. @see android.view.View#onDraw(android.graphics.Canvas)
	
	@Override protected void onDraw(Canvas canvas) 
	{
		//Log.d("DataGrid.onDraw", "entering");
		try
		{
		   super.onDraw(canvas);
		   
		   //reset paint params
		   mHeaderPaint.setStrokeWidth(1);
		   
		   int width = getMeasuredWidth();
		   int height = getMeasuredHeight();	   
		   int tableWidth = Math.min(width, mColumns.mSumColumnWidth - mColumns.mScrollPosX);	   
		   int maxRowsToDisplay = (height - mHeaderHeight + (mRowHeight-1) )/mRowHeight; //when at least one pixel of the row is visible, draw it	   
		   int rowsToDisplay = Math.min(maxRowsToDisplay, mRecordCount - mTopRow); 
		   int entityFieldHeight = Math.min(rowsToDisplay*mRowHeight, height-mHeaderHeight);
		   
		   //
		   // Update paint parameters for columns
		   //
		   
		   mColumns.calculatePaintParams();
	     
		   //
		   //Draw background
		   //
		   canvas.drawColor(Color.DKGRAY); //common background
		   mHeaderPaint.setColor(Color.GRAY);
		   canvas.drawRect(0, 0, tableWidth, mHeaderHeight, mHeaderPaint); //draw background of header
		   /*if(mFlingInProgress)	//required for fling testing
			   mHeaderPaint.setColor(Color.LTGRAY);
		   else
			   mHeaderPaint.setColor(Color.WHITE);*/
		   mHeaderPaint.setColor(Color.WHITE);
		   canvas.drawRect(0, mHeaderHeight,tableWidth, mHeaderHeight + entityFieldHeight, mHeaderPaint); //draw background of entity
		   
		   //
		   //special row background as set in style
		   //
		   if(mCursor!=null)
		   {		   
			   int yPos = 0;		   
			   
			   for(int i=mTopRow; i<rowsToDisplay+mTopRow; i++)
			   { 
				    //get row style settings
				    if(mCursor.moveToPosition(i))
				    {
					    CellStyle rowStyle = getStyleForCurrentRow();
					    
					    if(rowStyle.bgColor != Color.WHITE)
					    {
							mHeaderPaint.setColor(rowStyle.bgColor);
					    	
							int rowYCoord = mHeaderHeight + (i-mTopRow)*mRowHeight;
							canvas.drawRect(0, rowYCoord, tableWidth, rowYCoord+mRowHeight, mHeaderPaint); //draw background of styled Row			    	
					    }
				    }
			   }
		   }	   
		   
		   //
		   // Selection background
		   //
		   if(mSelectedRow!=-1 && isSelectedRowOnScreen())
		   {
			   mHeaderPaint.setColor(selectedRowColor);
			   int rowYCoord = mHeaderHeight + (mSelectedRow-mTopRow)*mRowHeight;
			   canvas.drawRect(0, rowYCoord, tableWidth, rowYCoord+mRowHeight, mHeaderPaint); //draw background of selected Row
			   
			   if(mSelectedColumn!=-1 && mSelectedColumn<mColumns.getLength() )
			   {
				   	ColumnInfo selectedColumn = mColumns.getColumn(mSelectedColumn);
				   	
				   	if(selectedColumn.isFixed() || selectedColumn.scrolledRight > mColumns.mFirstVisibleColumnWidth)
				   	{
					   	mHeaderPaint.setColor(selectedCellColor);   	
					   	canvas.drawRect(selectedColumn.clipLeft, rowYCoord, 
					   					selectedColumn.clipRight, rowYCoord+mRowHeight, mHeaderPaint); //draw background of selected Cell
					}
			   }
		   }
		   
		   //
		   //Draw lines
		   //
		   
		   //header horizontal lines
		   mHeaderPaint.setColor(Color.BLACK);
		   canvas.drawLine(0, 0, tableWidth, 0, mHeaderPaint);
		   canvas.drawLine(0, mHeaderHeight, tableWidth, mHeaderHeight, mHeaderPaint);
		   
		   //entity horizontal lines
		   mHeaderPaint.setColor(Color.GRAY);
		   for(int i=0; i<rowsToDisplay;i++)
		   {
			   int y = mHeaderHeight + (i+1)*mRowHeight;
			   canvas.drawLine(0, y, tableWidth, y, mHeaderPaint);
		   }
		   
		   //header and entity vertical lines   
		   int x0 = 0;
		   mHeaderPaint.setColor(Color.BLACK);
		   canvas.drawLine(x0, 0, x0, mHeaderHeight, mHeaderPaint);
		   mHeaderPaint.setColor(Color.GRAY);
		   canvas.drawLine(x0, mHeaderHeight, x0, height, mHeaderPaint);
		   
		   for(int i=0; i<mColumns.getLength(); i++)
		   {
			   ColumnInfo column = mColumns.getColumn(i);		   
			   if(column.isVisible())		   
			   {	   
				   if( column.isFixed() || column.scrolledRight > mColumns.mFirstVisibleColumnWidth ) //do not draw border if its coordinates less than width of first column
				   {
				   	   //draw bold line in case if current column is fixed and next column is NOT fixed 
					   if( column.isFixed() && (i<mColumns.getLength()-1) && !mColumns.getColumn(i+1).isFixed())					   
						   mHeaderPaint.setStrokeWidth(2);
					   else
						   mHeaderPaint.setStrokeWidth(1);
					   
					   mHeaderPaint.setColor(Color.BLACK);
					   canvas.drawLine(column.scrolledRight, 0, column.scrolledRight, mHeaderHeight, mHeaderPaint);
					   mHeaderPaint.setColor(Color.GRAY);
					   canvas.drawLine(column.scrolledRight, mHeaderHeight, column.scrolledRight, mHeaderHeight + entityFieldHeight, mHeaderPaint);
				   }
			   }
		   }
	  
		   //
		   // Draw text
		   //
	
		   //header text
		   for(int i = 0; i<mColumns.getLength(); i++)   
		   {
		   	   ColumnInfo column = mColumns.getColumn(i);
		   
			   if(column.isVisible())
			   {
				   //header text
				   int textX = getPaddingLeft() + column.scrolledLeft;
				   int textClipLeft =  getPaddingLeft() + column.clipLeft;
				   int textY = getPaddingTop() + (mHeaderHeight - mAscent)/2;				   
				   int textClipRight =  column.clipRight - mHeaderTextIndent;				   
	
				   //take into account X scroll pos			   
				   if(column.isFixed() || textClipRight > mColumns.mFirstVisibleColumnWidth)
				   {
					   canvas.save();
					   canvas.clipRect(textClipLeft, 0, textClipRight, mHeaderHeight);
					   
					   if(i == mSortColumn)
					   {
					   	   int markRight = textX + mMarkWidth;
					   	   if(column.isFixed() || markRight > mColumns.mFirstVisibleColumnWidth)
					   	   {
							   //draw mark denoting sort column
							   canvas.save();
							   canvas.translate(textX, getPaddingTop() + (mHeaderHeight - mMarkHeight)/2);
							   if(mSortOrder == SortOrder.ASCENDING)
								   canvas.drawPath(mAscendingMarkPath, mHeaderTextPaint);
							   else 
								   canvas.drawPath(mDescendingMarkPath, mHeaderTextPaint);					   
							   canvas.restore();
							   
							   textX = textX + mMarkWidth + mHeaderTextIndent;
						   }
					   }				
					   					   
					   canvas.drawText(column.getHeader(), textX, textY, mHeaderTextPaint);
					   canvas.restore();
				   }
			   }
		   }	   
		   
		   //entity text
		   if(mCursor!=null)
		   {		   
			   int yPos = 0;		   
			   
			   for(int i=mTopRow; i<rowsToDisplay+mTopRow; i++)
			   { 
				   //Log.d("DataGrid.onDraw", "mCursor.moveToPosition"+i);
		        	if(!mCursor.moveToPosition(i))
		        		continue;
		        	
	        		//get row style settings
	        		CellStyle rowStyle = getStyleForCurrentRow();
	        		
	        		int mapIndex = (mIdentityColumn == -1)? i : mCursor.getInt(mIdentityColumn);	        		
	        		Object[] values = mEditedHash.get(mapIndex);
	        		
	 			   	for(int j=0; j<mColumns.getLength(); j++)
	 			   	{
	 			   		ColumnInfo column = mColumns.getColumn(j);
	 			   		if(!column.isVisible())
	 			   			continue;
	 			   		
	 			   		CellStyle columnStyle = getStyleForCurrentColumn(rowStyle, column);

 			   			int cellX = getPaddingLeft()+ column.scrolledLeft;
 			   			int clipLeft = getPaddingLeft() + column.clipLeft;
 			   			int cellY = getPaddingTop() + mHeaderHeight + yPos;
 			   			int clipRight = column.clipRight - mHeaderTextIndent;
	 			   			
	 			   		canvas.save();				 			   		
	 			   		canvas.clipRect(clipLeft, cellY, clipRight, cellY + mRowHeight);   		
	 			   			
		        		//----- draw image if column flag is set -------				        			
		        		int imgWidth = 0;
		        		
		        		if(column.hasImage())
		        		{
			        		Bitmap cellImage = null;
			        		if(mCellListener!=null)
			        	    {
			        			if(j==0)
			        				cellImage = mCellListener.onGetImage(columnStyle.imageIndex);
			        			else
			        				cellImage = mCellListener.onGetImage(columnStyle.expandedImageIndex);

			        			if(cellImage!=null)
			        			{
			        				int imgY = (cellImage.getHeight()>=mRowHeight)? cellY: cellY + (mRowHeight-cellImage.getHeight())/2; 
			        				imgWidth = cellImage.getWidth();
				        			canvas.drawBitmap( cellImage, cellX, imgY, null);
			        			}
			        	    }
		        		}
		        		//----------	
 			   			if(!column.hasNoValue()) //skip columns having this flag
 			   			{				        		
		 			   		String value = "";
		 			   		
		 			   		int cursorIdx = mCursor.getColumnIndex(column.getDbField());

		 			   		if(column.isEditable() && values != null)
		 			   		{
		 			   			//read value from HashMap
		 			   			//value = mCursor.getString(cursorIdx);
		 			   			int editableIndex = column.getEditableIndex();
		 			   			value = (values[editableIndex]!=null) ? values[editableIndex].toString() : "";
		 			   			
		 			   			if(column.getDataType() == DATA_TYPE_DATE)
		 			   			{
									if( value!=null && value.length()>0)
										value = Convert.dateToString(Convert.getDateFromString(value));
		 			   			}
		 			   			else if(column.getDataType() == DATA_TYPE_MONEY)
		 			   			{
		 			   				value = (values[editableIndex]!=null) ? Convert.moneyToString((Double)values[editableIndex]) : "";
		 			   			}
		 			   			else if(column.getDataType() == DATA_TYPE_MSU)
		 			   			{
		 			   				value = (values[editableIndex]!=null) ? Convert.msuToString((Double)values[editableIndex]) : "";
		 			   			}		 			   			
		 			   		}
		 			   		else if(column.isCalculable())
		 			   		{
		 			   			if( mCellListener != null)
		 			   			{
		 			   				Object calculated = mCellListener.onCellCalculate(i, j, values);  
		 			   				value = (calculated != null)? calculated.toString():"";
		 			   			}
		 			   		}
		 			   		else
		 			   		{				 			   			
		 			   			//read value from Cursor
		 			   			if(cursorIdx!=-1)
		 			   			{
									if(column.getDataType() == DATA_TYPE_DATE)  //handle date differently (need to reformat it)
									{
										String strDate = mCursor.getString(cursorIdx);
										if(!mCursor.isNull(cursorIdx) && strDate!=null && strDate.length()>0)
											value = Convert.dateToString(Convert.getDateFromString(strDate));
									}
									
									if(column.getDataType() == DATA_TYPE_MONEY)
										value = mCursor.isNull(cursorIdx)? "" : Convert.moneyToString(mCursor.getDouble(cursorIdx));
									else if(column.getDataType() == DATA_TYPE_MSU)
										value = mCursor.isNull(cursorIdx)? "" : Convert.msuToString(mCursor.getDouble(cursorIdx));
									else
										value = mCursor.getString(cursorIdx);
		 			   			}
		 			   		}
		 			   		
		 			   		if(value!=null)
		 			   		{ 			   		
			 			   		int textY = cellY + (mRowHeight - mAscent)/2;
			 			   		int textX = (imgWidth==0)? cellX: cellX + imgWidth + getPaddingLeft(); 
			 			   		
			 			   		mTextPaint.setColor(columnStyle.textColor);
			 			   		int oldFlags = mTextPaint.getFlags();
			 			   		mTextPaint.setFlags( oldFlags|columnStyle.getTextFlags() );			 			   		
			 			   		mTextPaint.setTypeface(columnStyle.getTypeface());
		 			   			mTextPaint.setTextSkewX(columnStyle.isItalic()? -0.25f : 0f); //handle italic
			 			   		
							    if(column.isFixed() || clipRight > mColumns.mFirstVisibleColumnWidth)
							    {
				 			   		canvas.drawText(value, textX, textY, mTextPaint);
				 			   	}
			 			   		
			 			   		//mTextPaint.setColor(0xFF000000);
			 			   		mTextPaint.setFlags(oldFlags);
		 			   		}
 			   			}	 			   			
 			   			canvas.restore();
	 			   	}//for		   
	 			   	yPos+=mRowHeight;
			   }//for
		   }//if(mCursor!=null)
		}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in DataGrid.onDraw", ex);
		}
	
		//Log.d("DataGrid.onDraw", "exiting");
	}
	
	//-------------------------------------------------------------------------------------------------------
	private int calculateColumnByPos(int xPos, boolean previous)
	{
		int columnIdx = -1;
		int prevColumnIdx = -1;
		
		mColumns.calculatePaintParams();
		
	   	for(int j=0; j<mColumns.getLength(); j++)
	   	{
	   		ColumnInfo column = mColumns.getColumn(j);
	   		if(column.isVisible())
	   		{
		   		if( xPos>=column.scrolledLeft && xPos<column.scrolledRight)
		   		{
		   			columnIdx = j;
		   			break;
		   		}
		   				   		
		   		prevColumnIdx = j;
	   		}
	   	}
		
	   	return previous ? prevColumnIdx:columnIdx;
	}
	
	//-------------------------------------------------------------------------------------------------------
	private int calculateRowByPos(int yPos)
	{
		int row = -1;
		
		if( yPos>mHeaderHeight )
		{
			row = (yPos-mHeaderHeight)/mRowHeight + mTopRow;
			
			if(row>=mRecordCount)
				row = -1;
		}
		
		return row;
	}
	
	//-------------------------------------------------------------------------------------------------------
	public String getCellValue(int row, int column)
	{
		ColumnInfo columnInfo = mColumns.getColumn(column);
		
		return getCellValue(row, columnInfo);
	}
	
	public String getCellValue(int row, ColumnInfo columnInfo)
	{
		String value = "";
		
    	if(mCursor.moveToPosition(row))
    	{    		
	   		boolean haveValue = false;
	   		if(mIdentityColumn!=-1)
	   		{
	   			int mapIndex =  mCursor.getInt(mIdentityColumn);
	    		Object[] values = mEditedHash.get(mapIndex);
		   		if(columnInfo.isEditable() && values != null)
				{
		   			//read value from HashMap
		   			int editableIndex = columnInfo.getEditableIndex();
		   			value = (values[editableIndex]!=null) ? values[editableIndex].toString() : "";
		   			haveValue = true;
		   		}
	   		}
		   		
	   		if(haveValue == false)
	   		{
	   			int cursorIdx = mCursor.getColumnIndex(columnInfo.getDbField());
	   			value = mCursor.getString(cursorIdx);
	   		}	   		
    	}
    	
    	return value;
	}
	//-------------------------------------------------------------------------------------------------------
	public void setCellValue(int row, int column, Object value)
	{
		if(mCursor == null || mCursor.moveToPosition(row) == false)
			return;
		
		int mapIndex = (mIdentityColumn == -1)? row : mCursor.getInt(mIdentityColumn);
		
		Object[] values = mEditedHash.get(mapIndex);
		if(values != null)
		{
			//
			//record exists in HashMap, update one field
			//
			values[mColumns.getColumn(column).getEditableIndex()] = value;
			
			invalidate();
		}
		else
		{			
			//
			// no record in HashMap, copy all editable values to value array and add to HashMap
			//
			
			Object[] newValues = new Object[mColumns.mNumEditableColumns];
			for(int i=0; i<mColumns.getLength(); i++)
			{
				//copy editable items to hashMap
				if(mColumns.getColumn(i).isEditable())
				{
					int editableIndex = mColumns.getColumn(i).getEditableIndex();
					
					if( editableIndex >= 0 && editableIndex < mColumns.mNumEditableColumns)
					{
						if(i == column)
							newValues[editableIndex] = value;
						else
						{
							int cursorIdx = mCursor.getColumnIndex(mColumns.getColumn(i).getDbField());						
							
							if(mColumns.getColumn(i).getDataType() == DATA_TYPE_INTEGER)							
								newValues[editableIndex] = mCursor.isNull(cursorIdx)? null:mCursor.getInt(cursorIdx);
							else if(mColumns.getColumn(i).getDataType() == DATA_TYPE_DOUBLE || 
									mColumns.getColumn(i).getDataType() == DATA_TYPE_MONEY ||
									mColumns.getColumn(i).getDataType() == DATA_TYPE_MSU)
										newValues[editableIndex] = mCursor.isNull(cursorIdx)? null:mCursor.getDouble(cursorIdx);
							else //VARCHAR et al.
								newValues[editableIndex] = mCursor.isNull(cursorIdx)? null:mCursor.getString(cursorIdx);
						}
					}
					else
					{
						Log.e("DataGrid.setCellValue", "ELSE: if( editableIndex >= 0 && editableIndex < mColumns.mNumEditableColumns) !");
					}
				}			
			}
			mEditedHash.put(mapIndex, newValues);
			
			invalidate();
		}
		
	}
	//-------------------------------------------------------------------------------------------------------
	public String getDbFieldForColumn(int column)
	{
		if(column<0 || column >= mColumns.getLength() )
			return "";
		
		return mColumns.getColumn(column).getDbField();		
	}
	
	//-------------------------------------------------------------------------------------------------------
	// Scroll support
	@Override protected int  computeVerticalScrollExtent  ()
	{
		return mRows;
	}
	
	@Override protected int  computeVerticalScrollOffset  ()
	{
		//return mRecordCount + mTopRow - mRows;
		return  mTopRow;
	}
	
	@Override protected int  computeVerticalScrollRange  ()
	{
		int paddingRows = (int)Math.round( (double)mExtraBottomPadding/(double)mRowHeight); 
		
		return Math.max(mRecordCount+paddingRows-mRows,0);
	}

	@Override  protected void onLayout(boolean changed, int left, int top, int right, int bottom) 
	{
		if (changed) 
		{
			mRows = Math.max(0, (getHeight() - mHeaderHeight)/mRowHeight);
			mPartialRows = ((getHeight() - mHeaderHeight)-mRows*mRowHeight)>0 ? 1:0;
			//mTopRow = 0;
		}
	}
		
	//-------------------------------------------------------------------------------------------------------
	//
	// GestureListener implementation
	//
	
	public class GestureListener implements OnGestureListener 
	{
		//private boolean mScrolled;
		private float mScrollRemainderY;
		private boolean hScrollEnabled = false;
		private boolean vScrollEnabled = false;
		private long prevDownTime = 0;
		private float sumDistX = 0;
		private float sumDistY = 0;

		@Override public void onLongPress(MotionEvent e) 
		{
			try
			{
				if(mFlingJustStopped)
				{
					mFlingJustStopped = false;
					return;
				}
				
				if(!mCellSelectEnabled)
					return;

				mSelectedColumn = calculateColumnByPos( (int) e.getX(), false );
				mSelectedRow = calculateRowByPos( (int) e.getY() );
				invalidate();
				
				if ( mSelectedColumn!=-1 && mSelectedRow!=-1 && mCellListener != null)
				{		
					mCellListener.onCellLongPress(mSelectedRow, mSelectedColumn);	
				}
			}
			catch(Exception ex)
			{			
				ErrorHandler.CatchError("Exception in DataGrid.onLongPress", ex);
			}
		}
		
		@Override public void onShowPress(MotionEvent e) {}
		
		@Override public boolean onSingleTapUp(MotionEvent e) 
		{
			try
			{
				/*if(mFlingJustStopped)
				{
					mFlingJustStopped = false;
					return false;
				}*/
				
				if(!mCellSelectEnabled)
					return false;		

				mSelectedColumn = calculateColumnByPos( (int) e.getX(), false );
				mSelectedRow = calculateRowByPos( (int) e.getY() );
				
				if ( mSelectedColumn!=-1 && mCellListener != null)
				{		
					if( e.getY() <= mHeaderHeight)
					{
						boolean sortOrderChanged = false;
						
						if(! mColumns.getColumn(mSelectedColumn).isSortingDenied()) //ignore event if sorting is denied for column
						{
							sortOrderChanged = true;
							
							if(mSortColumn == mSelectedColumn) //just change sort order
							{
								if(mSortColumn != mDefaultSortColumn && mSortOrder == SortOrder.DESCENDING)
								{
									mSortColumn = mDefaultSortColumn;
									mSortOrder = SortOrder.ASCENDING;									
								}
								else
									mSortOrder = (mSortOrder == SortOrder.ASCENDING) ? SortOrder.DESCENDING:SortOrder.ASCENDING;
							}
							else
							{
								//change sort column, set ascending sort order
								mSortColumn = mSelectedColumn;
								mSortOrder = SortOrder.ASCENDING;
							}
						}
						
						mCellListener.onHeaderClicked(mSelectedColumn, sortOrderChanged);
					}
					else if ( mSelectedRow!=-1 )
			        {
				            mCellListener.onCellSelected(mSelectedRow, mSelectedColumn);
				            
				            if(mColumns.getColumn(mSelectedColumn).isEditable())
				            	mCellListener.onCellEdit(mSelectedRow, mSelectedColumn, mColumns.getColumn(mSelectedColumn).getDataType());
			        }
				}
		    	
		    	invalidate();
			}
			catch(Exception ex)
			{			
				ErrorHandler.CatchError("Exception in DataGrid.onSingleTapUp", ex);
			}		
		    return true;
		}
	
		@Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) 
		{
			try
			{
				boolean needInvalidate = false;
			
				float startX = e1.getX();
				float startY = e1.getY();
				
				long curTime = e2.getEventTime();
				long downTime = e2.getDownTime();

				//
				//down time is changed comparing to previous, that means that user released the pointer from screen and pressed again
				//we reset all variables here
				//
				if(prevDownTime<downTime)
				{
					prevDownTime = downTime;
					sumDistX=0;
					sumDistY=0;
					
					hScrollEnabled = false;
					vScrollEnabled = false;
				}
				
				//calculate summary distances after downTime
				sumDistX+=distanceX;
				sumDistY+=distanceY;				

				//determine horizontal and vertical velocity of scrolling
				long timePassed = curTime-downTime;
				
				//depending on movement distance, determine scroll direction
				if( timePassed>200 )
				{
					if(!hScrollEnabled && !vScrollEnabled)
					{
						if(Math.abs(sumDistY)>Math.abs(sumDistX) )
							vScrollEnabled = true;
						else //if(timePassed>1000)
							hScrollEnabled = true;
					}
				}

				//Log.d("GestureListener.onScroll", "hScroll=" + hScrollEnabled);
				//Log.d("GestureListener.onScroll", "vScroll=" + vScrollEnabled);					
				
				
				//
				// Vertical scroll
				//	
			
				//if( startX<mColumns.mFirstVisibleColumnWidth) //scroll vertical only on left part of the screen				
				if(!hScrollEnabled)
				{
					if( startY > mHeaderHeight) //do not scroll vertical if column is resized
					{
						//
						//process vertical scroll of table part
						//
						//mScrolled = true;
						
					    distanceY += mScrollRemainderY;
					    int deltaRows = (int) (distanceY / mRowHeight);
					    mScrollRemainderY = distanceY - deltaRows * mRowHeight;
					    
					    //mTopRow changing between 0 and (mRecordCount-mRows)
					    mTopRow = Math.max(0, Math.min(computeVerticalScrollRange(), mTopRow + deltaRows));	
					    		
						awakenScrollBars();
						needInvalidate = true;
					}
				}
				
				//
				// Horizontal scroll
				//
				
				if(hScrollEnabled)
				{
					if( startX>mColumns.mFirstVisibleColumnWidth) //scroll horizontally only on right part of the screen
					{
						//
						// process horizontal scroll
						//
				
						if(distanceX!=0 && startY>mHeaderHeight)
						{
							int width = getMeasuredWidth();
							int maxScrollX = Math.max(0, mColumns.mSumColumnWidth-width);		
							mColumns.mScrollPosX = Math.max(0, Math.min(maxScrollX, mColumns.mScrollPosX + (int)distanceX));
							needInvalidate = true;
						}
						
						if(needInvalidate)
							invalidate();
					}
				}
			}
			catch(Exception ex)
			{			
				ErrorHandler.CatchError("Exception in DataGrid.onScroll", ex);
			}
			
		    return true;
		}
	
		@Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
		{
			mScrollRemainderY = 0.0f;
			//onScroll(e1, e2, -2 * velocityX, -2 * velocityY);
			
			//if(velocityX>velocityY)
			//	onScroll(e1, e2, -2 * velocityX, 0);		

			if (mFlingRunnable == null) 
	             mFlingRunnable = new FlingRunnable();

			float velocityKoef = (float)mFlingVelocity/10;			
	        mFlingRunnable.start((int)(velocityY*velocityKoef));			
			
		    return true;
		}
	
		@Override public boolean onDown(MotionEvent e) 
		{
			mScrollRemainderY = 0.0f;
			return true;
		}
	}

	//-------------------------------------------------------------------------------------------------------
	@Override public boolean onTouchEvent(MotionEvent ev) 
	{
		try
		{
			if(mFlingInProgress)
			{
				mFlingJustStopped = mFlingInProgress;  
				mFlingInProgress = false;
			}
			
	        int x = (int) ev.getX();
	        int y = (int) ev.getY();
	
	        int action = ev.getAction();
	
	        //process resize column actions
	        switch (action) 
	        {
	            case MotionEvent.ACTION_DOWN:
	                if(y <= mHeaderHeight)
	                {
	                	mResizeColumnCurX = x;
	                	mResizeColumn = calculateColumnByPos(x, true);
	                }
	                break;
	            case MotionEvent.ACTION_MOVE:
	            	if(mResizeColumn != -1)
	            	{
		    			if( mColumns.getLength() > mResizeColumn )
		    			{
		    				int distanceX = mResizeColumnCurX - x; 
		    				
		    				mColumns.getColumn(mResizeColumn).setWidth((mColumns.getColumn(mResizeColumn).getWidth() - distanceX >mColumnMinSize) ? (mColumns.getColumn(mResizeColumn).getWidth() - distanceX) : mColumnMinSize);
		    				mResizeColumnCurX = x;
	
		    				mColumns.calculatePaintParams();
		    				invalidate();
		    			}
	            	}	            	
	                break;
	            case MotionEvent.ACTION_UP:
	            	if(mResizeColumn!=-1)
	            	{
	            		if ( mCellListener != null)
	            			mCellListener.onColumnWidthChanged(mResizeColumn);	
	            		
	            		mResizeColumn = -1;
	            	}
	                break;
	        }
			
			
	        //process the rest actions
			if(mGestureDetector.onTouchEvent(ev))
				return true;
			
		}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in DataGrid.onTouchEvent", ex);
		}
		
        return true;
	}

	//-------------------------------------------------------------------------------------------------------
	public void adjustViewPortOnSelectionMove()
	{
		if(mRows>0 && mSelectedRow>(mTopRow+mRows-1))
			mTopRow = mSelectedRow-mRows+1;
		
		if(mSelectedRow<mTopRow)
			mTopRow = mSelectedRow;
	}
	
	public boolean isSelectedRowOnScreen()
	{
		return (mSelectedRow>=mTopRow && mRows>0 && mSelectedRow<(mTopRow+mRows+mPartialRows));
	}
	
	public void moveSelectionUp() 
	{
		if(mSelectedRow == -1)
		{
			mSelectedRow = mTopRow;
		} 
		else if(mSelectedRow>0) 
		{ 
			if(isSelectedRowOnScreen())
				mSelectedRow--;
			else
				mSelectedRow = mTopRow;
			
			adjustViewPortOnSelectionMove();

			if(mCellListener!=null)
				mCellListener.onCellSelected(mSelectedRow, mSelectedColumn);

			invalidate();
		} 
	}
	
	public void moveSelectionDown() 
	{
		if(mRecordCount==0)
			return;
		
		if(mSelectedRow == -1)
		{
			mSelectedRow = mTopRow;
		}
		else if(mSelectedRow<(mRecordCount-1)) 
		{ 
			if(isSelectedRowOnScreen())
				mSelectedRow++;
			else
				mSelectedRow = mTopRow;
			
			adjustViewPortOnSelectionMove();			
		}
		
		if(mCellListener!=null)
			mCellListener.onCellSelected(mSelectedRow, mSelectedColumn);
		
		invalidate();
	}
	
	//-------------------------------------------------------------------------------------------------------
	//Some code is taken from android.widget.AbsListView
	//Responsible for fling behavior. Use start(int) to initiate a fling. Each frame of the fling is handled in run().
	//A FlingRunnable will keep re-posting itself until the fling is done.
    private class FlingRunnable implements Runnable 
    {
    	//	Tracks the decay of a fling scroll
    	private Scroller mScroller;

    	//Y value reported by mScroller on the previous fling
        private int mLastFlingY;

        public FlingRunnable() 
        {
            mScroller = new Scroller(getContext());
        }

        public void start(int initialVelocity) 
        {
            int initialY = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
            mLastFlingY = initialY;
            mScroller.fling(0, initialY, 0, initialVelocity, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        	mFlingInProgress = true;

        	post(this);	            
        }
	        
        private void  endFling() 
	    {
        	mFlingInProgress = false;
        	//invalidate();
	    }
	        
        public void run() 
        {
        	if(!mFlingInProgress)
        		return;
        	
        	if(mRecordCount == 0)
        	{
        		endFling();
        		return;
        	}
	            
	        final Scroller scroller = mScroller;
	        boolean more = scroller.computeScrollOffset();
	        final int y = scroller.getCurrY();

            // Flip sign to convert finger direction to list items direction
            // (e.g. finger moving down means list is moving towards the top)
            int delta = mLastFlingY - y;

            // Pretend that each frame of a fling scroll is a touch scroll
	        if (delta > 0) 
	        {
                // List is moving towards the top
                delta = Math.min(getHeight() - getPaddingBottom() - getPaddingTop() - 1, delta);
            } 
	        else 
	        {
                // List is moving towards the bottom
                delta = Math.max(-(getHeight() - getPaddingBottom() - getPaddingTop() - 1), delta);
            }

		    int deltaRows = (int) (delta / mRowHeight);
		    
		    if(deltaRows>0)
		    {
		    	if(mTopRow == mRecordCount-mRows)
		    		more = false;
		    }
		    else
		    {
		    	if(mTopRow == 0)
		    		more = false;
		    }	    
		    
	        if (more) 
	        {
			    //mTopRow changing between 0 and (mRecordCount-mRows)
			    mTopRow = Math.max(0, Math.min(computeVerticalScrollRange(), mTopRow + deltaRows)); //Math.max(mRecordCount-mRows,0)
	        	
				invalidate();
				mLastFlingY = y;
				post(this);
            } 
	        else 
	        {
	            endFling();
	        }

	    }
    }
   	//--------------------------------------------------------
    public GridState getGridState()
    {
    	GridState state = new GridState();
    	state.selectedRow = getSelectedRow();
    	state.scrollPos = getScrollPos();
		//state.sortField = getSortColumnField();
		//state.sortOrder = getSortOrder();		
    	
    	return state;
	}
    
    //--------------------------------------------------------
    public void restoreGridState(GridState state)
    {
    	//setSortColumnByDbField(state.sortField);
    	//setSortOrder(state.sortOrder);	
		setSelectedRow(state.selectedRow);
		scrollToPos(state.scrollPos);
		invalidate();
    }

    public Cursor getmCursor() {
        return mCursor;
    }
}

