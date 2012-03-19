package com.app.ant.app.ServiceLayer;

import java.util.HashMap;

import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;

import android.content.SharedPreferences;
import android.database.Cursor;


public class Settings
{		

	public static final String PROPNAME_SALER_DEFAULT_PRICE_ID="saler_default_price_id";  
	public static final String PROPNAME_SPEC_MARKS="spec_marks";
	public static final String PROPNAME_SALER_ID="saler_id";
	public static final String PROPNAME_SALER_NAME="saler_name";
	public static final String PROPNAME_SALER_SALARY="saler_salary";
	public static final String PROPNAME_SALER_SALARY_RATE="saler_salary_rate";
	
	public static final String PROPNAME_SALER_DEFAULT_DOC_TYPE = "saler_default_doc_type";
	
	public static final String PROPNAME_CHECK_PAYMENT_NUMBER="check_payment_number";
	public static final String PROPNAME_DENY_UNLINKED_PAYMENTS="deny_unlinked_payments";
	public static final String PROPNAME_FULL_BLACK_DOC_PAYMENTS="full_black_doc_payments";

	public static final String PROPNAME_DENY_EDIT_DISCOUNT="deny_edit_discount"; 
	public static final String PROPNAME_CHECK_MAX_DISCOUNT="use_check_max_discount";
	
	public static final String PROPNAME_STYLE_NOT_SOLD_POWER_SKU = "style_not_sold_power_sku";
	public static final String PROPNAME_STYLE_POWER_SKU="style_power_sku";
	
	public static final String PROPNAME_GPRS_ROOT_URL = "gprs_root_url";	
	//public static final String PROPNAME_GPRS_DEF_GET_URL = "gprs_def_get_url";
	//public static final String PROPNAME_GPRS_DEF_SET_URL = "gprs_def_set_url";

	public static final String PROPNAME_LOCAL_ROOT_URL = "local_root_url";
	public static final String PROPNAME_CUSTOM_ROOT_URL = "custom_root_url"; //�������������� URL �������������
	//public static final String PROPNAME_LOCAL_DEF_GET_URL = "local_def_get_url";
	//public static final String PROPNAME_LOCAL_DEF_SET_URL = "local_def_set_url";	
	
	//columns sets
	public static final String PROPNAME_COLUMNS_SET_ID_REMNANTS = "remnants_columns_set_id";
	public static final String PROPNAME_COLUMNS_SET_ID_CLAIM_SALE = "claim_sale_columns_set_id";
	public static final String PROPNAME_COLUMNS_SET_ID_DOC_LIST = "doc_list_columns_set_id";
	
	public static final String PROPNAME_ATTR_ID_ADDRESS_GOLD = "attr_id_address_gold";
	public static final String PROPNAME_ATTR_ID_ADDRESS_SILVER = "attr_id_address_argentum";

	public static final String PROPNAME_GPS_ENABLE = "gps_log_enable_module";
	public static final String PROPNAME_GPS_LOG_DISTANCE = "gps_log_distance";
	public static final String PROPNAME_GPS_LOG_INTERVAL = "gps_log_interval";
	public static final String PROPNAME_GPS_ATTEMPTS = "gps_attempts";
	//public static final String PROPNAME_DOC_STEP_PREV_ORDER = "doc_step_pred_order";
	
	public static final String PROPNAME_LOG_LEVEL = "log_level";
	public static final String PROPNAME_ITEM_GROUP_PLAN_TOTAL = "item_group_plan_total";
	
	public static final String PROPNAME_PROPOSE_DAY_PLAN_AT_START = "propose_day_plan_at_start";
	public static final String PROPNAME_CHECK_VISIT_EFFICIENCY = "check_visit_efficiency";
	public static final String PROPNAME_NEED_FILL_ORDER_FROM_PREDICTED = "need_fill_order_from_predicted";
	
	public static final String PROPNAME_CHECK_STORECHECK_PG_ON_VISIT_END = "check_storecheck_pg_on_visit_end";
	
	public static final String PROPNAME_SHOW_DIALOG_SYNC_REST_EVERY_N_MINUTES = "show_dialog_sync_rest_every_n_minutes";		
	
	public static final String PROPNAME_VAN = "van";
	public static final String PROPNAME_COMPANY_ID = "company_id";
	public static final String PROPNAME_COMPANY_ADDR_ID = "company_addr_id";
	
	public static final String PROPNAME_ALLOWED_NOT_SENT_ORDERS_QNT = "allowed_not_sent_orders_qtn";
	
	public static final String PROPNAME_ADDR_TYPE_CLASSIFIER_ID = "addr_type_classifier_id";
	public static final String PROPNAME_ADDR_PLAN_TOTAL = "address_plan_total";
	public static final String PROPNAME_DOC_ADD_DAYS_TO_DELIVERY = "doc_add_days_to_delivery";
	
	public static final String PROPNAME_STYLE_NOT_SOLD="style_not_sold";
	public static final String PROPNAME_DEFAULT_DIRECTION_ID="default_saldo_direction_id";
	
	public static final String PROPNAME_ITEM_GROUP_NOVELTY = "item_group_novelty";
	
	public static final String PROPNAME_SHOW_CLIENT_QUALIFICATION_IN_LIST = "show_client_qualification_in_list";
	
	public static final String PROPNAME_DENY_ORDER_QNT_MORE_THAN_REST = "deny_order_qnt_more_than_rest";
	
	public static final String PROPNAME_ADDR_PROPS_QUESTIONNAIRE = "addr_properties_questionnaire";	//TODO
	public static final String PROPNAME_ADDR_ATTRS_QUESTIONNAIRE = "addr_attributes_questionnaire";	//TODO
	
	/** 
	 * ������������ ��������� ������
	 */
	private static Settings _instance = null;
	/**
	 * ���� ������������� ����������������� ����
	 */
	private static boolean _isNeedReinit = true;
	
	/**
	 * ��������� ����
	 */
	private HashMap<String, Setting> _settings;
	/**
	 * ��������� ���������� ������ ��������. 
	 * ���� �� �� ��������������� ��� ���������� ����������������� ���������� ����������� 
	 * @return _instance
	 * 
	 * @see setNeedReinit
	 */	
	public static Settings getInstance()	
	{
		if (_instance == null || _isNeedReinit) 
		{
			_instance = new Settings();
			_isNeedReinit = false;	
		}
		return _instance;
	}
	/**
	 * �������� ����������� ������
	 */
	private Settings()
	{
		_settings = new HashMap<String, Setting>();		
		String sql = Q.settings_get();		
		Cursor cursor = Db.getInstance().selectSQL(sql);
		
		if(cursor != null)
		{
			try
			{
				int propColIdx = cursor.getColumnIndex("Property");
				int defValColIdx = cursor.getColumnIndex("DefaultValue");
				int valColIdx = cursor.getColumnIndex("Value");

				for (int i = 0; i < cursor.getCount(); i++)
				{
					try
					{
						cursor.moveToPosition(i);

						String prop = cursor.getString(propColIdx);

						Setting setting = new Setting();
						setting.value = cursor.getString(valColIdx);
						setting.defValue = cursor.getString(defValColIdx);

						_settings.put(prop, setting);
					}
					catch (Exception ex)
					{
						//ErrorHandler �� ������������ ����������
						ex.printStackTrace();
					}
				}
			}
			catch (Exception ex)
			{
				//ErrorHandler �� ������������ ����������
				ex.printStackTrace();
			}
			finally
			{
				if (cursor != null)
				{
					cursor.close();
					cursor = null;
				}
			}			
		}
	}
	//----------------------------------------------------------------------------------------------------------------
	/**
	 * �������� String �������� ��������� 
	 * 
	 * @param property ��� ��������� 
	 * @param def �������� �� ��������� 
	 * @return ���� �������� ��������� �� �������� �������� ��� ��� ����� ������ ������ ("") ��� ����� null ������������ �������� �� ��������� def
	 */
	public String getPropertyFromSettings(String property, String def)
	{
		Setting setting = _settings.get(property);
		String result = null;
		if (setting != null)
		{
			result = Convert.toString(setting.value, setting.defValue);
		}
		return 	Convert.isNullOrBlank(result) ? def : result;
	}
	/**
	 * �������� String �������� ��������� 
	 * 
	 * @param property ��� ��������� 
	 * @return ���� �������� ��������� �� �������� �������� ��� ����� null ������������ �������� �� ��������� ""
	 */
	public String getPropertyFromSettings(String property)
	{
		return getPropertyFromSettings(property, "");	
	}
	
	/**
	 * �������� long �������� ��������� 
	 * 
	 * @param property ��� ���������
	 * @param def �������� �� ���������  
	 * @return ���� �������� ��������� �� �������� �������� ��� ��� ����� null ��� �� �������� �������� � ���� (Convert.toLong) ������������ �������� �� ��������� def
	 * 
	 * @see Convert.toLong
	 */
	public long getLongPropertyFromSettings(String property, long def)
	{		
		return Convert.toLong(getPropertyFromSettings(property), def);
	}	
	/**
	 * �������� long �������� ��������� 
	 * 
	 * @param property ��� ���������  
	 * @return ���� �������� ��������� �� �������� �������� ��� ��� ����� null ��� �� �������� �������� � ���� (Convert.toLong) ������������ �������� �� ��������� 0
	 * 
	 * @see Convert.toLong
	 */
	public long getLongPropertyFromSettings(String property)
	{		
		return Convert.toLong(getPropertyFromSettings(property), 0);
	}
	/**
	 * �������� int �������� ��������� 
	 * 
	 * @param property ��� ���������  
	 * @return ���� �������� ��������� �� �������� �������� ��� ��� ����� null ��� �� �������� �������� � ���� (Convert.toInt) ������������ �������� �� ��������� 0
	 * 
	 * @see Convert.toInt
	 */
	public int getIntPropertyFromSettings(String property)
	{		
		return Convert.toInt(getPropertyFromSettings(property), 0);
	}
	/**
	 * �������� int �������� ��������� 
	 * 
	 * @param property ��� ���������
	 * @param def �������� �� ���������   
	 * @return ���� �������� ��������� �� �������� �������� ��� ��� ����� null ��� �� �������� �������� � ���� ������������ �������� �� ��������� def
	 * 
	 * @see Convert.toInt
	 */
	public int getIntPropertyFromSettings(String property, int def)
	{		
		return Convert.toInt(getPropertyFromSettings(property), def);
	}
	
	/**
	 * �����-��������� ���������, ������ ������ ���� �������� ���������: ������������� �������� � ����������� ��������
	 * ������������ ��� �����������: HashMap<String, Setting> _settings
	 * 
	 * @see _settings
	 * 
	 */
	private class Setting
	{
		//public String name;		
		public String value;
		public String defValue;
	}

	/**
	 * ��������� ����� ������������� ����������������� ���� ��������
	 * ���� ���� ���������� ��� ����� ��������� ��� ��������� ��������� � Settings.getInstance()
	 *  
	 * @param isNeed 
	 */
	public void setNeedReinit(boolean isNeed)
	{
		Settings._isNeedReinit = isNeed;
	}

	/**
	 * ��������� �������� SharedPreferences ����� �������������, ���� �������� sync_preferences
	 * @param key ��� ���������
	 * @param value �������� ���������
	 */
	public void setStringSyncPreference(String key, String value)
	{               				
		setStringActivityPreference("sync_preferences", key, value);
	}
	/**
	 * ������ �������� SharedPreferences ����� �������������, ���� �������� sync_preferences
	 * @param key ��� ���������
	 * @param defValue �������� �� ��������� ���������
	 */
	public String getStringSyncPreference(String key, String defValue)
	{               			
		return getStringActivityPreference("sync_preferences", key, defValue);
	}
	
	/**
	 * ��������� �������� SharedPreferences ����� �������������
	 * @param fileName ��� ����� ��������
	 * @param key ��� ���������
	 * @param value �������� ���������
	 */
	public void setStringActivityPreference(String fileName, String key, String value)
	{               				
		SharedPreferences prefs = AntContext.getInstance().getContext().getSharedPreferences("com.app.ant_" + fileName, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
	}
	/**
	 * ������ �������� SharedPreferences ����� �������������, ���� �������� sync_preferences
	 * @param fileName ��� ����� ��������
	 * @param key ��� ���������
	 * @param defValue �������� �� ��������� ���������
	 */
	public String getStringActivityPreference(String fileName, String key, String defValue)
	{               	
		SharedPreferences prefs = AntContext.getInstance().getContext().getSharedPreferences("com.app.ant_" + fileName, 0);
		return prefs.getString(key, defValue);
	}
	
}
