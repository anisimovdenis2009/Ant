package com.app.ant.app.ServiceLayer;

import java.util.ArrayList;

import com.app.ant.R;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class CustomItemizedOverlay extends ItemizedOverlay {
	 public ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	 Context mContext;
	 boolean MoveMap;
	 public boolean IsPointChanged = false;

	 public CustomItemizedOverlay(Drawable defaultMarker) {
	   super(boundCenterBottom(defaultMarker));
	 }

	 public CustomItemizedOverlay(Drawable defaultMarker, Context context) {
	  super(boundCenterBottom(defaultMarker));
	  mContext = context;
	 }

	 public void addOverlay(OverlayItem overlay) {
	     mOverlays.add(overlay);
	     populate();
	 }

	 @Override
	 protected OverlayItem createItem(int i) {
	   return mOverlays.get(i);
	 }

	 @Override
	 public int size() {
	   return mOverlays.size();
	 }

	 @Override
	 protected boolean onTap(int index) {
	   OverlayItem item = mOverlays.get(index);
	   AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	   dialog.setTitle(item.getTitle());
	   dialog.setMessage(item.getSnippet());
	   dialog.show();
	   return true;
	 }
	 
	 @Override
	 public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		 
		 if (event.getAction() == MotionEvent.ACTION_UP/*1*/) {                
             if (!MoveMap) {
				 GeoPoint p = mapView.getProjection().fromPixels((int)event.getX(), (int)event.getY());
	             
	             OverlayItem fff = new OverlayItem(p, mContext.getString(R.string.itemizedoverlay_move_notification),
	            		 						      mContext.getString(R.string.itemizedoverlay_move_notification_text));
	             addOverlay(fff);
	             
	             mOverlays.remove(0);
	             populate();
	             
	             IsPointChanged = true;
	             
	             Toast.makeText(mContext, 
	                            p.getLatitudeE6() / 1E6 + "," + 
	                            p.getLongitudeE6() /1E6 , 
	                            Toast.LENGTH_SHORT).show();
             }
         }
		 else
			 if (event.getAction() == MotionEvent.ACTION_DOWN) {
				 MoveMap = false;
			 }
			 else
				 if (event.getAction() == MotionEvent.ACTION_MOVE) {
				 	 MoveMap = true;
				 }
		 
		 return false;
	 }
}
