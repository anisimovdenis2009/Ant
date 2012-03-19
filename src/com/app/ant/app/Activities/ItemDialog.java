package com.app.ant.app.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Synchronizer;

import java.io.File;


public class ItemDialog
{
	private DialogInterface.OnClickListener	okClickListener	= null;

	public void setOkClickListener(DialogInterface.OnClickListener listener)
	{
		okClickListener = listener;
	}

	private Uri[]		urls;
	
	private Context context = null;

	// --------------------------------------------------------------
	public Dialog onCreate(Context context)
	{
		try
		{
			this.context = context;
			// get data from Items table
			String itemID = Integer.toString(AntContext.getInstance().curItemId);

			String sql = " SELECT t1.ItemExt, t1.License, t1.CashName, t1.ScreenName, t1.ItemName, t1.PerPall, t1.PerCase, " +
						 "		 t1.Volume, t1.UnitWeight, t1.ItemTax, t1.EAN, t1.ExtraCode " + 
						 " FROM Items t1 " + 
						 " WHERE t1.ItemID = " + itemID;

			Cursor cursor = Db.getInstance().selectSQL(sql);
			cursor.moveToPosition(0);

			// create dialog
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			View layout = inflater.inflate(R.layout.item, (ViewGroup) ((Activity) context).findViewById(R.id.item));

			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setView(layout);
			builder.setMessage(cursor.getString(cursor.getColumnIndex("ItemName")));

			String okText = ("Ok");
			builder.setPositiveButton(okText, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					if (okClickListener != null) okClickListener.onClick(dialog, which);
				}
			});

			// get controls
			TextView textExt = (TextView) layout.findViewById(R.id.textItemExt);
			TextView textID = (TextView) layout.findViewById(R.id.textInfoItemID);
			TextView textLicense = (TextView) layout.findViewById(R.id.textItemLicense);
			TextView textCashName = (TextView) layout.findViewById(R.id.textItemCashName);
			TextView textPerPall = (TextView) layout.findViewById(R.id.textItemPerPall);
			TextView textPerCase = (TextView) layout.findViewById(R.id.textItemPerCase);
			TextView textVolume = (TextView) layout.findViewById(R.id.textItemVolume);
			TextView textUnitWeight = (TextView) layout.findViewById(R.id.textItemUnitWeight);
			TextView textEAN = (TextView) layout.findViewById(R.id.textItemEAN);
			TextView textExtraCode = (TextView) layout.findViewById(R.id.textItemExtraCode);
			// TextView textQuantity = (TextView)
			// layout.findViewById(R.id.quantity);
			// TextView textUnitName = (TextView)
			// layout.findViewById(R.id.unitName);
			// TextView textKeepTime = (TextView)
			// layout.findViewById(R.id.keepTime);
			TextView textVATID = (TextView) layout.findViewById(R.id.textItemVATID);
            textID.setText(itemID);
			textExt.setText(cursor.getString(cursor.getColumnIndex("ItemExt")));
			textLicense.setText(cursor.getString(cursor.getColumnIndex("License")));
			textCashName.setText(cursor.getString(cursor.getColumnIndex("CashName")));
			textPerPall.setText(cursor.getString(cursor.getColumnIndex("PerPall")));
			textPerCase.setText(cursor.getString(cursor.getColumnIndex("PerCase")));
			textVolume.setText(cursor.getString(cursor.getColumnIndex("Volume")));
			textUnitWeight.setText(cursor.getString(cursor.getColumnIndex("UnitWeight")));
			textEAN.setText(cursor.getString(cursor.getColumnIndex("EAN")));
			textExtraCode.setText(cursor.getString(cursor.getColumnIndex("ExtraCode")));
			// textUnitName.setText(
			// ((Activity)context).getResources().getString(R.string.item_unitNameValue)
			// );
			textVATID.setText(cursor.getString(cursor.getColumnIndex("ItemTax")));

			cursor.close();
			
			initFileCollection();
			
			GridView grid = (GridView) layout.findViewById(R.id.images); 
			ImageAdapter adapter = new ImageAdapter(context); 
			//grid.setNumColumns(adapter.getCount() >= 4 ? 4 : adapter.getCount());
			grid.setNumColumns(5);
			grid.setAdapter(adapter);			
			grid.setFadingEdgeLength(40);

			return builder.create();
		}
		catch (Exception ex)
		{
			ErrorHandler.CatchError("Exception in ItemDialog.onCreate", ex);
		}

		return null;
	}

	public void initFileCollection()
	{
		//Synchronizer.SD_FILES_PATH;
		String sql = "select f.FilePath" +
					" from Files f " +
					"		inner join ItemFiles i on f.ID = i.FileID " +
					" where i.ItemID = " + AntContext.getInstance().curItemId;
		
		Cursor cursor = Db.getInstance().selectSQL(sql);
		if (cursor != null)
		{
			urls = new Uri[cursor.getCount()];
			int idx = cursor.getColumnIndex("FilePath");
			
			if (idx >= 0)
			{
				for (int i = 0; i < cursor.getCount(); i++)
				{
					cursor.moveToPosition(i);
					String filePath = cursor.getString(idx);
					if (filePath != null && filePath.length() > 4) //like *.ext
					{
						File file = new File(Synchronizer.SD_FILES_PATH + filePath);
						if (file.exists()) urls[i] = Uri.parse(file.getAbsolutePath());
					}
				}
			}
		}
		else
		{
			urls = new Uri[0];
		}
		
		if(cursor!=null)
			cursor.close();
		
		//global accessible context
		AntContext.getInstance().setForGalleryItemImagesUrls(urls);
	}

	public class ImageAdapter extends BaseAdapter
	{
		private Context	mContext;
		int				mGalleryItemBackground;

		public ImageAdapter(Context c)
		{
			mContext = c;
		}

		public int getCount()
		{
			return urls.length;
		}

		public Object getItem(int position)
		{
			return position;
		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			ImageView i = new ImageView(mContext);

			final int pos = position;
			
			i.setImageURI(urls[pos]);
			i.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			i.setPadding(2, 2, 2, 2);
			i.setLayoutParams(new GridView.LayoutParams(90, 90));
						
			i.setOnClickListener(new View.OnClickListener()
			{				
				@Override
				public void onClick(View v)
				{
					context.startActivity(new Intent(context, GalleryForm.class));					
				}
			});
			
			return i;
		}
	}
}
