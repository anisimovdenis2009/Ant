package com.app.ant.app.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.app.ant.R;
import com.app.ant.app.Controls.DataGrid;
import com.app.ant.app.Controls.DataGrid.ColumnInfo;
import com.app.ant.app.ServiceLayer.ErrorHandler;


public class DocSaleSelectColumnsDialog  
{
	//ok
	private DialogInterface.OnClickListener okClickListener = null;
	public void setOkClickListener(DialogInterface.OnClickListener listener) { okClickListener = listener; }
	
	//cancel
	private DialogInterface.OnClickListener cancelClickListener = null;
	public void setCancelClickListener(DialogInterface.OnClickListener listener) { cancelClickListener = listener; }

	ListView columnList;
	
	//--------------------------------------------------------------	
    public Dialog onCreate(Context context, DataGrid.GridColumns columns) 
    {
    	try
    	{
			//create dialog
	    	Activity contextActivity = ((Activity)context);
	    	Resources res = contextActivity.getResources();    	
	    	
	    	LayoutInflater inflater = contextActivity.getLayoutInflater();
			View layout = inflater.inflate(R.layout.doc_sale_select_columns, (ViewGroup)contextActivity.findViewById(R.id.docSaleSelectColumns));
			
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setView(layout);
			builder.setMessage(res.getString(R.string.form_title_selectColumns));
	
			String okText = (res.getString(R.string.doc_sale_selectColumns_ok));
			builder.setPositiveButton(okText, new DialogInterface.OnClickListener() 
			{					
				@Override public void onClick(DialogInterface dialog, int which) 
				{
					if(okClickListener != null)
						okClickListener.onClick(dialog, which);
				}
			});
			
			String cancelText = (res.getString(R.string.doc_sale_selectColumns_cancel));
			builder.setNegativeButton(cancelText, new DialogInterface.OnClickListener() 
			{					
				@Override public void onClick(DialogInterface dialog, int which) 
				{
					if(cancelClickListener != null)
						cancelClickListener.onClick(dialog, which);
				}
			});		
			
			//initialize column list
			columnList = (ListView) layout.findViewById(R.id.columnList);    			
			columnList.setAdapter(new SelectColumnAdapter(context, columns));
				
			return builder.create();
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in docSaleSelectColumnsDialog.onCreate", ex);
		}
		
		return null;
    }
    
    //--------------------------------------------------------------
    static class ColumnItemViewHolder 
    {
        TextView textColumn;
        TextView textColumnDescription;
        CheckBox chkBoxVisible;
        ImageButton buttonUp;
        ImageButton buttonDown;
    }      
    //--------------------------------------------------------------
    private static class SelectColumnAdapter extends BaseAdapter 
    {
        private LayoutInflater mInflater;
        //private Context mContext;
        private DataGrid.GridColumns columns;

        public SelectColumnAdapter(Context context, DataGrid.GridColumns columns) 
        {
			//mContext = context;        	            
            mInflater = LayoutInflater.from(context); // Cache the LayoutInflate to avoid asking for a new one each time.           
            this.columns = columns;
        }

        public int getCount() { return columns.getLength(); }

		public Object getItem(int position) { return columns.getColumn(position); } // position;  

		public long getItemId(int position)	{ return position; }

        public View getView(int position, View convertView, ViewGroup parent) 
        {
        	try
        	{
	        	final ColumnItemViewHolder holder;
	        	final int pos = position;
	        	final ColumnInfo col = columns.getColumn(position);
	        	
	            if (convertView == null) 
	            {
	        		convertView = mInflater.inflate(R.layout.doc_sale_select_columns_list_item, null);
	
	                holder = new ColumnItemViewHolder();
	                holder.textColumn = (TextView) convertView.findViewById(R.id.textColumnName);
	                holder.textColumnDescription = (TextView) convertView.findViewById(R.id.textColumnDescription);
	                holder.chkBoxVisible = (CheckBox) convertView.findViewById(R.id.chkBoxColumnVisible);
	                holder.buttonUp = (ImageButton) convertView.findViewById(R.id.buttonColumnUp);
	                holder.buttonDown = (ImageButton) convertView.findViewById(R.id.buttonColumnDown);
	                
	                convertView.setTag(holder);                
	            } 
	            else 
	            {
	                holder = (ColumnItemViewHolder) convertView.getTag();
	            }
	            
	            holder.buttonUp.setEnabled(!col.isFixed());
	            holder.buttonDown.setEnabled(!col.isFixed());
	            
	            holder.textColumn.setText(col.getHeader()); // Bind the data with the holder.	            
	            holder.textColumnDescription.setText(col.getDescription());
	            
	            holder.chkBoxVisible.setOnCheckedChangeListener(null);            
	            holder.chkBoxVisible.setChecked(col.isVisible());                       
	            holder.chkBoxVisible.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{				
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
					{
						if (col.getLinked() > 0)
						{
							columns.setColumnVisibilityByLink(isChecked, col.getLinked());
							refresh();
						}
						else
						{
							col.setVisible(isChecked);
						}
					}
				});
	            
	            holder.buttonUp.setOnClickListener(new View.OnClickListener() 
	    		{				
	    			@Override public void onClick(View v) 
	    			{
	    				columns.moveColumnUp(pos);
	    				refresh();
	    			}
	    		});	
	            
	            holder.buttonDown.setOnClickListener(new View.OnClickListener() 
	    		{				
	    			@Override public void onClick(View v) 
	    			{
	    				columns.moveColumnDown(pos);
	    				refresh();
	    			}
	    		});
        	}
    		catch(Exception ex)
    		{			
    			ErrorHandler.CatchError("Exception in docSaleSelectColumnsDialog.getView", ex);
    		}
            
            return convertView;
        }
        
        void refresh()
        {
        	this.notifyDataSetChanged();
        }
    }
    
}

