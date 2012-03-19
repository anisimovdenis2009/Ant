package com.app.ant.app.Activities;

import com.app.ant.R;
import com.app.ant.app.ServiceLayer.AntContext;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Gallery;


public class GalleryForm extends Activity
{
	private Uri[]		urls;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);

		Gallery gallery = (Gallery) findViewById(R.id.gallery);

		urls = AntContext.getInstance().getForGalleryItemImagesUrls();
		gallery.setAdapter(new ImageAdapter(this));
		
		gallery.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> arg0, View view, int arg2, long arg3)
			{
				ImageView i = (ImageView)view;
				i.setLayoutParams(new Gallery.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				i.setScaleType(ImageView.ScaleType.FIT_CENTER);
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{		
			}
		});
		
		gallery.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id)
			{
				ImageView i = (ImageView)view;
				String imageUrl = i.getTag().toString();
				Toast.makeText(GalleryForm.this, imageUrl, Toast.LENGTH_SHORT).show();
			}
		});
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
			i.setPadding(20, 0, 20, 0);
			i.setLayoutParams(new Gallery.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
			i.setTag(urls[pos].toString());
			
			return i;
		}
	}
}
