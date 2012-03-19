package com.app.ant.app.DataLayer;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Api;
import com.app.ant.app.ServiceLayer.Convert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class Db extends SQLiteOpenHelper 
{
	public enum DataType { Long, Double, String };
	
    private static final String DATABASE_NAME = "ant.db";    
    private static final int DATABASE_VERSION = 3;
    
	private static Db _instance = null;	
	
	private boolean isOtherDbAttached = false;
	private boolean haveOpenTransaction = false; 
    
	public static Db getInstance()
	{
		if (_instance == null)
		{			
			_instance = new Db(AntContext.getInstance().getContext());
		}		
		return _instance;
	}
    
    
	//TODO must be private
    public Db(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
    
    public Db(Context context, String dbName)
    {
        super(context, dbName, null, DATABASE_VERSION);
	}
    
    public void vacuum()
    {
    	SQLiteDatabase db = this.getWritableDatabase();
    	try
    	{
	    	if (!db.inTransaction())
	    	{
	    		db.execSQL("vacuum");
	    	}
    	}
    	catch (Exception ex) 
    	{
    		Log.e("vacuum", ex.toString());
    	}
    }
    /**
     * The method is called when a database is created for the first time
     */
    @Override
    public void onCreate(SQLiteDatabase db) 
    {
    	try
    	{    		
    	}
    	catch (SQLException e)
    	{
    		Log.e("Db.onCreate", e.toString());
    	}

    }
    /**
     * The method is called when it is necessary to upgrade the database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    	
    }    
    
    //SELECT============================================================
    private Cursor selectSQL(SQLiteDatabase db, String sql)
    {
		
		Cursor curs = null;
		try
		{
			Log.v("DEN",sql);
            curs = db.rawQuery(sql, null);
			curs.moveToFirst();
		}
		catch (SQLException ex)
		{
			Log.e("Db.selectSQL", ex.toString());
			//throw e;
		}
		finally
		{
		}   	
    	return curs;
    }
    /**
     * Sampling from a database as a cursor
     * @ Param sql - the string SELECT
     * @ Return android.database.Cursor
     */
    public Cursor selectSQL(String sql)
    {
    	Cursor result = null; 
    	try
    	{
    		SQLiteDatabase db = this.getWritableDatabase();
    		result = selectSQL(db, sql); 
    	}
    	catch(Exception ex) 
    	{
    		Log.e("Db.selectSQL", ex.toString());	
    	}
    	return result;
    }
    
    private String selectValue(SQLiteDatabase db, String sql){
		Cursor c = null;
		String ret = null;
		
		try
		{
			c = selectSQL(db, sql);
			if (0 >= c.getCount()) { return ret; }
			c.moveToFirst();
			ret = c.getString(0);
		}
		catch (SQLException ex)
		{
			Log.e("Db.selectValue", ex.toString());
			//ErrorHandler.CatchError("SQL Error:", e);
		}
		finally
		{
			if (c != null)
			{
				try
				{
					c.close();
				}
				catch (Exception ex)
				{
					Log.e("Db.selectValue", ex.toString());
					//ErrorHandler.CatchError("SQL Error:", e);
				}
			}
		}
		return ret;
    }
    
	private String[] selectRowValues(SQLiteDatabase db, String sql)
	{
		Cursor c = null;
		String[] ret = null;

		try
		{
			c = selectSQL(db, sql);
			if (0 >= c.getCount()) { return ret; }
			c.moveToFirst();
			int count = c.getColumnCount();
			ret = new String[count];
			for (int i = 0; i < count; i++)
				ret[i] = c.getString(i);
		}
		catch (SQLException ex)
		{
			Log.e("Db.selectRowValues", ex.toString());
		}
		finally
		{
			if (c != null)
			{
				try
				{
					c.close();
				}
				catch (Exception ex)
				{
					Log.e("Db.selectRowValues", ex.toString());
				}
			}
		}
		return ret;
    }
	
	private Collection<Object> selectColumnValues(SQLiteDatabase db, String sql, DataType dataType)
	{
		Cursor c = null;
		Collection<Object> ret = null;

		try
		{
			c = selectSQL(db, sql);
			if (0 >= c.getCount()) { return ret; }
			c.moveToFirst();
			int count = c.getCount();

			ret = new ArrayList<Object>();
			for (int i = 0; i < count; i++)
			{
				if(dataType == DataType.Long)
					ret.add(c.getLong(0));
				else if(dataType == DataType.String)
					ret.add(c.getString(0));
				else if(dataType == DataType.Double)
					ret.add(c.getDouble(0));
				else 
					ret.add(c.getString(0));

				c.moveToNext();
			}
		}
		catch (SQLException ex)
		{
			Log.e("Db.selectColumnValues", ex.toString());
		}
		finally
		{
			if (c != null)
			{
				try
				{
					c.close();
				}
				catch (Exception ex)
				{
					Log.e("Db.selectColumnValues", ex.toString());
				}
			}
		}
		return ret;
	}
	
	private Map <String, String> selectColumnValuesInMap(SQLiteDatabase db, String sql, String keyColumn, String valueColumn)
	{
		Cursor c = null;
		Map<String, String> ret = null;

		try
		{
			c = selectSQL(db, sql);
			if (0 >= c.getCount()) { return ret; }			
			
			int keyIdx = c.getColumnIndex(keyColumn);
			int valueIdx = c.getColumnIndex(valueColumn);
			if (keyIdx < 0 || valueIdx < 0) { return ret; }
			
			c.moveToFirst();
			int count = c.getCount();
			ret = new HashMap<String, String>();
			for (int i = 0; i < count; i++)
			{
				if (!c.isNull(keyIdx)) ret.put(c.getString(keyIdx), c.getString(valueIdx));
				c.moveToNext();
			}
		}
		catch (SQLException ex)
		{
			Log.e("Db.selectColumnValuesInMap", ex.toString());
		}
		finally
		{
			if (c != null)
			{
				try
				{
					c.close();
				}
				catch (Exception ex)
				{
					Log.e("Db.selectColumnValuesInMap", ex.toString());
				}
			}
		}
		return ret;
	}
	
	private Map<String, String> selectRowValuesInMap(SQLiteDatabase db, String sql)
	{
		Cursor c = null;
		Map<String, String> ret = null;

		try
		{
			c = selectSQL(db, sql);
			if (0 >= c.getCount()) { return ret; }
			c.moveToFirst();
			int count = c.getColumnCount();
			ret = new HashMap<String, String>();
			for (int i = 0; i < count; i++)
				ret.put(c.getColumnName(i), c.getString(i)) ;
		}
		catch (SQLException ex)
		{
			Log.e("Db.selectRowValuesInMap", ex.toString());
		}
		finally
		{
			if (c != null)
			{
				try
				{
					c.close();
				}
				catch (Exception ex)
				{
					Log.e("Db.selectRowValuesInMap", ex.toString());
				}
			}
		}
		return ret;
    }
    
    /**
     * Selection of the first value of c the first column of the cursor
     * @ Param sql - the string SELECT
     * @ Return - String
     */
    public String selectValue(String sql)
    {
    	SQLiteDatabase db = this.getWritableDatabase();
    	return selectValue(db, sql);
    }
    
    /**
     * Selection first row of cursor
     * @ Param sql - the string SELECT
     * @ Return - String[]
     */
    public String[] selectRowValues(String sql)
    {
    	SQLiteDatabase db = this.getWritableDatabase();
    	return selectRowValues(db, sql);
    } 
    
    /**
     * Selection first column of cursor
     * @ Param sql - the string SELECT
     * @ Return - String[]
     */
    public Collection<Object> selectColumnValues(String sql, DataType dataType)
    {
    	SQLiteDatabase db = this.getWritableDatabase();
    	return selectColumnValues(db, sql, dataType);
    } 
    
    /**
     * Selection first row of cursor in map
     * @ Param sql - the string SELECT
     * @ Return - Map<String - column name, String - column value>
     */
    public Map<String, String> selectRowValuesInMap(String sql)
    {
    	SQLiteDatabase db = this.getWritableDatabase();
    	return selectRowValuesInMap(db, sql);
    } 
    /**
     * Selection columns values in HashMap   
     * @param sql
     * @param keyColumn - values of this columns will be hashmap keys (must be not null)
     * @param valueColumn - values of this columns will be hashmap valuess
     * @return map
     */
    public Map<String, String> selectColumnValuesInMap(String sql, String keyColumn, String valueColumn)
    {
    	SQLiteDatabase db = this.getWritableDatabase();
    	return selectColumnValuesInMap(db, sql, keyColumn, valueColumn);
    }
    
    //END SELECT========================================================

    //TRANSACTIONS======================================================
    public void beginTransaction()
    {
    	if(haveOpenTransaction == false)
    	{
    		SQLiteDatabase db = getWritableDatabase();
    		db.beginTransaction();
    		haveOpenTransaction = true;
    	}
    }
    
    public void commitTransaction()
    {
    	if(haveOpenTransaction == true)
    	{
    		SQLiteDatabase db = getWritableDatabase();
    		db.setTransactionSuccessful();
    	}
    }
    
    public void endTransaction()
    {
    	if(haveOpenTransaction == true)
    	{
    		SQLiteDatabase db = getWritableDatabase();
        	db.endTransaction();
        	haveOpenTransaction = false;
    	}
    }
    
    //EXEC==============================================================
    /**
     * Execute all of the SQL statements in the String[] array
     * @param db The database on which to execute the statements
     * @param sql An array of SQL statements to execute
     */
    
    private void execMultipleSQL(SQLiteDatabase db, String[] sql)
    {
		db.beginTransaction();
		try
		{
			for (String s : sql)
			{
				if (s.trim().length() > 0) 
					db.execSQL(s);
			}
			db.setTransactionSuccessful();
		}
		catch (SQLException ex)
		{
			Log.e("Db.execMultipleSQL", ex.toString());
		}
		finally
		{
			db.endTransaction();
			db.close();
		}
	}
    
    public void execMultipleSQL(String[] sql)
    {
    	SQLiteDatabase db = this.getWritableDatabase();	
    	execMultipleSQL(db, sql);
    }
    
    private void execSQL(SQLiteDatabase db, String sql, Object[] bindArgs)
    {	
    	
    	if(!haveOpenTransaction)
    		db.beginTransaction();
    	
		try 
		{
			if(bindArgs == null)
				db.execSQL(sql);
			else
				db.execSQL(sql, bindArgs);
			
			if(!haveOpenTransaction)
				db.setTransactionSuccessful();
		} 
		catch (SQLException ex) 
		{
			Log.e("Db.execSQL", ex.toString());
			//throw e;
        } 
		finally 
		{
			if(!haveOpenTransaction)
				db.endTransaction();
        }
	}
    
    public void execSQL(String sql) throws SQLException
    {
    	SQLiteDatabase db = getWritableDatabase();
    	execSQL(db, sql, null);    	
    }

    public void execSQL(String sql, Object[] bindArgs) throws SQLException
    {
    	SQLiteDatabase db = getWritableDatabase();
    	execSQL(db, sql, bindArgs);    	
    }
    
    //END EXEC==========================================================  
    
    //ATTACH============================================================
    public void attathDb(String dbPath, String dbAlias) throws SQLException
    {
    	SQLiteDatabase db = getWritableDatabase();
    	db.execSQL("attach '" + dbPath + "' as " + dbAlias);
    	isOtherDbAttached = true;
    }  
    
    public void detachDb(String dbAlias) throws SQLException
    {
    	SQLiteDatabase db = getWritableDatabase();
    	db.execSQL("detach database " + dbAlias);
    	isOtherDbAttached = false;
    } 
    
    public boolean isOtherDbAttached()
    {
    	return isOtherDbAttached;
    }
    //END ATTACH========================================================
    
	public long getDataLongValue(String sql, long defaultValue)
	{
		Cursor cursor = selectSQL(sql);

		if (cursor != null && cursor.getCount() != 0 && cursor.moveToPosition(0))
		{
			long value = cursor.getInt(0);
			cursor.close();
			return value;
		}

		if (cursor != null) cursor.close();

		return defaultValue;
	}

    public String getDataStringValue(String sql, String defaultValue)
    {
    	Cursor cursor = selectSQL(sql);
    	
    	if( cursor!=null && cursor.getCount()!=0 && cursor.moveToPosition(0))
    	{
    		String value = cursor.getString(0);
    		cursor.close();    	
       		return value;
    	}

    	if(cursor!=null)
    		cursor.close();
    	
    	return defaultValue;
    }
    
    public double getDataDoubleValue(String sql, double defaultValue)
    {
    	Cursor cursor = selectSQL(sql);
    	
    	if( cursor!=null && cursor.getCount()!=0 && cursor.moveToPosition(0))
    	{
    		double value = cursor.getDouble(0);
    		cursor.close();
       		return value;
    	}
   	
    	if(cursor!=null)
    		cursor.close();
    	
    	return defaultValue;
    }
    
    public String getParamValue(String paramName, String defaultValue)
    {
    	String result = defaultValue;
    	if (Convert.isNullOrBlank(paramName)) return result;
    	Cursor cursor = null;
    	try
    	{
	    	cursor = selectSQL("select ParamValue from Params where ParamName = '" + paramName + "'");
	    	
	    	if( cursor != null && cursor.getCount() != 0 && cursor.moveToPosition(0))
	    	{
	    		String value = cursor.getString(0);
	    		cursor.close();    	
	       		result = value;
	    	}	    	
    	}
    	catch(Exception ex)
    	{
    		Log.e("Db.getParamValue", ex.toString());
    	}
    	finally
    	{
    		if(cursor != null)
	    		cursor.close();
    	}
    	return result;
    }

    //-----------------------------------------------------------------------
    public String getDbVersion()
    {
    	return getParamValue("db_version", Api.getVersionName());
    }

    //-----------------------------------------------------------------------    
    public String[] getTableColumnNamesArray(String table)
    {
    	String[] columns = null;
    	
    	String sql = " SELECT * FROM " + table + " WHERE 0 = 1 ";
    	Cursor cursor = selectSQL(sql);
    	
    	if(cursor!=null)
    	{
    		columns = cursor.getColumnNames();    	
    		cursor.close();
    	}
    	
    	return columns;
    }
    
    public String getTableColumnNames(String table, String alias, Collection<String> excludeColumns)
    {
    	String result = "";
    	String prefix = (alias==null || alias.length()==0) ? "" : alias + ".";  
    	String [] columns = getTableColumnNamesArray(table);
    	for(int i=0; i<columns.length; i++)
    	{
    		String column = columns[i];
    		if(!excludeColumns.contains(column))
    			result = result + prefix + column + ",";
    	}
    	
    	//delete last comma sign
    	if(result.length()>0)
    	{
    		result = result.substring(0, result.length()-1);
    	}
    	
    	return result;
    }
        
    
}
