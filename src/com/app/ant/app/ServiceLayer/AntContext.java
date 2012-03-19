package com.app.ant.app.ServiceLayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.app.ant.R;
import com.app.ant.app.BusinessLayer.Address;
import com.app.ant.app.BusinessLayer.Client;
import com.app.ant.app.BusinessLayer.Visit;
import com.app.ant.app.Controls.DataGrid.CellStyleCollection;


public class AntContext 
{
	private static AntContext antContext = null;
	private Context _context = null;
	
	//salerId
	private Long salerId = null; 
	
	//device identification
	private String deviceId = null;
	private String deviceKey = null;

	//item
	public int curItemId = 0;
	
	//client
	public Client getClient() { return visit == null ? null : visit.getClient();} 
	public long getClientID() { return getClient() != null ? getClient().clientID : 0; }
	
	//address
	public Address getAddress() { return visit == null ? null : visit.getAddress();} 
	public long getAddrID() { return getAddress() != null ? getAddress().addrID : 0; }	
	
	//visit
	private Visit visit = null;
	public void startVisit(Client client, Address address, int visitType) 
	{ 
		visit = new Visit(client, address, visitType); 
	}
	public void endVisit()
	{
		visit = null;
	}
	
	public Visit getVisit() { return visit; }
	
	//tab controller
	private TabControllerVisit tabController = null;
	public TabControllerVisit getTabController() { return tabController; }
	public StepController getStepController() {	return getTabController().getStepController(getTabController().currentTab);	}
	
	//images
	private Uri[] forGalleryItemImagesUrls;
	private byte[] lastCameraImage = null;
	
	Bitmap[] cachedImages = null; //cache images so don't need to decode next time
	public static final int DEF_PLUS_IMG_IDX = 7;
	public static final int DEF_MINUS_IMG_IDX = 8;
	public static final int DEF_CHILD_IMG_IDX = 9;
	public Bitmap getCachedImage(Context context, int imageIndex)
	{
		final int[] imageResIds = new int[] { R.drawable.mark_black, R.drawable.mark_blue, R.drawable.mark_green, R.drawable.mark_red,
											R.drawable.power, R.drawable.gold, R.drawable.initiatives,
											R.drawable.plus, R.drawable.minus, R.drawable.child};
		
		if(imageIndex >= imageResIds.length || imageIndex<0)
			return null;
		
		if(cachedImages ==null)
		{
			cachedImages = new Bitmap[imageResIds.length];
			for(int i=0; i<cachedImages.length; i++)
				cachedImages[i] = null;
		}
		
		cachedImages[imageIndex] = BitmapFactory.decodeResource(context.getResources(), imageResIds[imageIndex]);
		
		return cachedImages[imageIndex];
	}
	
	//---------------------------------------- styles ----------------------------------------
	CellStyleCollection cellStyles = null;
	
	private AntContext()
	{
		tabController = new TabControllerVisit();		
	}
	
	public String getDeviceId()
	{
		if (deviceId == null)
		{
			deviceId = Api.getDeviceID();
		}		
		return deviceId;
	}
	
	
	public String getDeviceKey()
	{
		if (deviceKey == null)
		{			
			//deviceKey = Db.getInstance().getParamValue("app_key", "D26E7FAE-AE12-4500-8330-49C262221902");
			deviceKey = "D26E7FAE-AE12-4500-8330-49C262221902";
		}		
		return deviceKey;
	}
	
	public static AntContext getInstance()
	{
		if( antContext == null )
		{
			antContext = new AntContext();
		}
		return antContext;
	}

	public static Settings getSettings()
	{
		return Settings.getInstance();
	}
	
	public CellStyleCollection getStyles()
	{
		//Styles are loaded one time per app launch and cached for further use		
		if(cellStyles == null)
		{
			cellStyles = new CellStyleCollection();
			cellStyles.loadFromDatabase();
		}
		
		return cellStyles;
	}
	
	/**
	 * @return Global accessible context - current AntActivity 
	 */
	public Context getContext()
	{
		return _context;
	}
	/**
	 * @param _context the _context to set
	 */
	public void setContext(Context cntxt)
	{
		this._context = cntxt;
	}

	public void setNeedRefreshFull()
	{
		Settings.getInstance().setNeedReinit(true);
	}
		
	public void setNeedRefreshPartial()
	{
		Settings.getInstance().setNeedReinit(true);
	}
	
	public void setNeedRefreshSaldo()
	{
		Settings.getInstance().setNeedReinit(true);
	}
	
	public void setNeedRefreshRests()
	{
		Settings.getInstance().setNeedReinit(true);
	}

	public Long getSalerId()
	{		
		if (salerId == null)
		{
			salerId = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_SALER_ID); 
		}		
		return salerId;
	}
	public Uri[] getForGalleryItemImagesUrls()
	{
		return forGalleryItemImagesUrls;
	}
	public void setForGalleryItemImagesUrls(Uri[] forGalleryItemImagesUrls)
	{
		this.forGalleryItemImagesUrls = forGalleryItemImagesUrls;
	}
	public byte[] getLastCameraImage()
	{
		return lastCameraImage;
	}
	public void setLastCameraImage(byte[] lastCameraImage)
	{
		this.lastCameraImage = lastCameraImage;
	}
}
