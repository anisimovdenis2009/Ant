package com.app.ant.app.BusinessLayer;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 23.01.12
 * Time: 14:37
 * To change this template use File | Settings | File Templates.
 */
import android.database.Cursor;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.Convert;

import java.util.Map;

/** Информация о торговой точке*/
public class Address
{
	public final static int		ADDR_TYPE_DELIVERY_POINT	= 1;
	public final static int		ADDR_TYPE_SALE_POINT		= 2;
	public final static int		ADDR_TYPE_OFFICE			= 3;
	public final static int		ADDR_TYPE_ORDER_POINT		= 4;

	public final static int		ATTR_GOLD_POINT				= 5;	// золотая торговая точка
	public final static int		ATTR_ARGENTUM_POINT			= 6;	// серебрянная торговая точка
	public final static int		ATTR_PLATIN_POINT			= 7;	// платиновая торговая точка

	public final static int		ATTR_TRADE_TYPE_S			= 8;
	public final static int		ATTR_TRADE_TYPE_C			= 9;
	public final static int		ATTR_TRADE_TYPE_P			= 10;
	public final static int		ATTR_TRADE_TYPE_B			= 11;

	public final static int		ATTR_GOLD_MAG				= 12;
	public final static int		ATTR_GOLD_M_PRETENDER		= 13;
	public final static int		ATTR_GOLD_M_PRETENDER_ND	= 14;
	public final static int		ATTR_GOLD_M_DISCVALIF		= 15;

	public final static int		ATTR_GOLD_CHECK				= 16;
	public final static int		ATTR_GOLD_CH_PRETENDER		= 17;
	public final static int		ATTR_GOLD_CH_PRETENDER_ND	= 18;
	public final static int		ATTR_GOLD_CH_DISCVALIF		= 19;

    public long clientID;
	public long addrID;
   	public String addrName;
	public String deliveryDays;
	public int channelID = -1;
	public String channelName;
	public double latitude;
	public double longitude;
	public String erpId;
    public boolean goldMag = false;

	private Map<String, String> attributes = null;
	//private String[] attributesCollection = null;


    public Address(long addrID,long clientID) {
        this.clientID = clientID;
        this.addrID = addrID;
    }

    public Address(long addrID)
	{
		this.addrID = addrID;

		refreshProps();


	}

	public void refreshProps()
	{
    	//
    	//get data from address table
    	//
    	{

	    	String sqlAddr =
	    		"SELECT a.AddrName, a.DeliveryDays, a.PointX, a.PointY, a.ERPID " +
				"FROM Addresses a " +
				"WHERE a.AddrID = " + addrID;

	    	Cursor addrCursor = Db.getInstance().selectSQL(sqlAddr);
	    	if(addrCursor!=null)
	    	{
		    	addrCursor.moveToPosition(0);

		    	addrName = addrCursor.getString(addrCursor.getColumnIndex("AddrName"));
		    	deliveryDays = addrCursor.getString(addrCursor.getColumnIndex("DeliveryDays"));
		    	latitude = addrCursor.getDouble(addrCursor.getColumnIndex("PointX"));
		    	longitude = addrCursor.getDouble(addrCursor.getColumnIndex("PointY"));
		    	erpId = addrCursor.getString(addrCursor.getColumnIndex("ERPID"));

		    	addrCursor.close();
	    	}
    	}

		//
		// channel
		//
    	{
	    	String sql = String.format(" SELECT c.ChannelID, c.ChannelName " +
									   " FROM AddrChannels ac " +
									   " 		INNER JOIN Channels c ON ac.ChannelID = c.ChannelID" +
									   " WHERE ac.AddrID = %d AND c.ChannelTypeID = 1", addrID); //TODO ChannelTypeID = 1 to settings

			Cursor cursor = Db.getInstance().selectSQL(sql);

			if(cursor!=null && cursor.getCount()>0)
			{
				cursor.moveToPosition(0);

				int channelIDcolumn = cursor.getColumnIndex("ChannelID");
				channelID = cursor.isNull(channelIDcolumn) ? -1: cursor.getInt(channelIDcolumn);
				channelName = cursor.getString(cursor.getColumnIndex("ChannelName"));
			}

			if(cursor!=null)
				cursor.close();
    	}
	}

	public void updateGeoCoordinates()
	{

		String sql = "update Addresses set PointX = " + Double.toString(latitude) + ", PointY = " + Double.toString(longitude) +
				                        ", State = 'E', Sent = 0" +
				     " where AddrID = " + Long.toString(addrID);

		Db.getInstance().execSQL(sql);
	}

	/*public String[] getAttributesCollection()
	{
		if (attributesCollection == null)
		{
			String sql = String.format("select AttrID from AddressAttributes where AddrID = %s", this.addrID);
			attributesCollection = Db.getInstance().selectColumnValues(sql);
		}
		return attributesCollection;
	}*/

 	public boolean isAddressAttributeSet(int attrId)
	{
		boolean result = false;
		String sql = "";
		if (attributes == null)
		{
			sql =  String.format("select AttrID, 1 as Cnt from AddressAttributes where AddrID = %s", this.addrID);
			attributes = Db.getInstance().selectColumnValuesInMap(sql, "AttrID", "Cnt");
		}

		if (attributes != null)
		{
			String attr = Convert.toString(attrId, "-1");
			result = !Convert.isNullOrBlank(attributes.get(attr));
		}
		return result;
	}

	public boolean isDeliveryPoint()
	{
		return isAddressAttributeSet(Address.ADDR_TYPE_DELIVERY_POINT);
	}

	public boolean isSalePoint()
	{
		return isAddressAttributeSet(Address.ADDR_TYPE_SALE_POINT);
	}

	public boolean isOrderPoint()
	{
		return isAddressAttributeSet(Address.ADDR_TYPE_ORDER_POINT);
	}

	public boolean isOffice()
	{
		return isAddressAttributeSet(Address.ADDR_TYPE_OFFICE);
	}

	public boolean isSomeGoldCheckout()
	{
		return isAddressAttributeSet(Address.ATTR_GOLD_CHECK)
				|| isAddressAttributeSet(Address.ATTR_GOLD_CH_PRETENDER)
				|| isAddressAttributeSet(Address.ATTR_GOLD_CH_PRETENDER_ND);
	}

	public boolean isSomeGoldMag()
	{
		return isAddressAttributeSet(Address.ATTR_GOLD_MAG);
/*				|| isAddressAttributeSet(Address.ATTR_GOLD_M_PRETENDER)
				|| isAddressAttributeSet(Address.ATTR_GOLD_M_PRETENDER_ND);*/
	}

	public int getTradeTypeAttribute()
	{
		int attrTradeType = 0;

		if (this.isAddressAttributeSet(Address.ATTR_TRADE_TYPE_B))
		{
			attrTradeType= Address.ATTR_TRADE_TYPE_B;
		}
		else if (this.isAddressAttributeSet(Address.ATTR_TRADE_TYPE_C))
		{
			attrTradeType = Address.ATTR_TRADE_TYPE_C;
		}
		else if (this.isAddressAttributeSet(Address.ATTR_TRADE_TYPE_P))
		{
			attrTradeType= Address.ATTR_TRADE_TYPE_P;
		}
		else if (this.isAddressAttributeSet(Address.ATTR_TRADE_TYPE_S))
		{
			attrTradeType = Address.ATTR_TRADE_TYPE_S;
		}
		return attrTradeType;
	}
}
