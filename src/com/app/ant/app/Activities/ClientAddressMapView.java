package com.app.ant.app.Activities;

import java.util.List;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.app.ant.R;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.CustomItemizedOverlay;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * @author shraiber.k
 *
 */
public class ClientAddressMapView extends MapActivity {
	
	private double lat, lon;
	private String address;  
	private MapController mapController;
	MapView mapView;
	CustomItemizedOverlay itemizedoverlay;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_address_map_view);
        
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(true);
        
        mapController = mapView.getController();
    
        Bundle bndl = this.getIntent().getExtras();
        lat = bndl.getDouble("latitude");
        lon = bndl.getDouble("longitude");
        address = bndl.getString("address");
        
        List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.marker_rounded_blue);
        itemizedoverlay = new CustomItemizedOverlay(drawable, this);
        
        GeoPoint point = new GeoPoint((int)(lat * 1e6), (int)(lon * 1e6)); 
        OverlayItem ovrlItem = new OverlayItem(point, address, Convert.toString(lat, "_") + " " + Convert.toString(lon, "|"));
        
        itemizedoverlay.addOverlay(ovrlItem);
        mapOverlays.add(itemizedoverlay);
        
        CenterLocation(point);
        
        Button btnSave = (Button) findViewById(R.id.buttonSavePoint);
        btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent in = new Intent();
				in.putExtra("latitude", itemizedoverlay.mOverlays.get(0).getPoint().getLatitudeE6()/1e6);
				in.putExtra("longitude", itemizedoverlay.mOverlays.get(0).getPoint().getLongitudeE6()/1e6);
				
				if (itemizedoverlay.IsPointChanged)
					setResult(123, in);
				else
					setResult(122, in);
												
				finish();				
			}
		});
	};
 
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    private void CenterLocation(GeoPoint centerGeoPoint)
    {
    	mapController.animateTo(centerGeoPoint);
    	mapController.setZoom(15);
    };
}
