package com.app.ant.app.Activities;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 23.01.12
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */
import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.app.ant.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.content.Context;
import android.database.Cursor;
import com.app.ant.app.BusinessLayer.Address;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Settings;

/** Форма вывода аналитической информации по клиенту/адресу. Выводит текущее сальдо*/
public class AnalyticsForm extends AntActivity
{
   //--------------------------------------------------------------
	/** Инициализация формы */
    @Override  public void onCreate(Bundle savedInstanceState)
    {
    	try
    	{
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        overridePendingTransition(0,0);
	        setContentView(R.layout.analytics);

			TextView textClient = (TextView) findViewById(R.id.textClient);
			textClient.setText(AntContext.getInstance().getClient().nameScreen);

	        InitStepBar();

	    	//
	    	//display saldo info
	    	//
	        Address address = AntContext.getInstance().getAddress();
	    	fillSaldo(this, address.addrID,  R.id.saldoTable, 0xFF000000, true);

    	}
		catch(Exception ex)
		{
			ErrorHandler.CatchError("Exception in contactsForm.onCreate", ex);
		}
    }

    //--------------------------------------------------------------
    private void InitStepBar()
    {
    	//init tabs
    	ViewGroup tabsPlacement = (ViewGroup) findViewById(R.id.tabsPlacement);
    	AntContext.getInstance().getTabController().createTabs(this, tabsPlacement);
    }

    //--------------------------------------------------------------
    @Override public void onBackPressed()
    {
    	try
    	{
    		AntContext.getInstance().getTabController().onBackPressed(this);
    	}
		catch(Exception ex)
		{
			ErrorHandler.CatchError("Exception in contactsForm.onBackPressed", ex);
		}
    }

    //--------------------------------------------------------------
    /** Выводит информацию о текущем сальдо
     * @param context контекст
     * @param addrId идентификатор адреса
     * @param saldoTableViewId родительский элемент пользовательского интерфейса для вывода данных
     * @param textColor цвет текста для отображения
     * @param displayBullet показывать или нет маркер перед текстом
     */
    public static void fillSaldo( Context context, long addrID, int saldoTableViewId, int textColor, boolean displayBullet )
    {
    	long defaultDirectionID = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_DEFAULT_DIRECTION_ID, 0);

    	String sql = String.format(" SELECT st.SaldoTypeName, coalesce(s.Saldo,0) as Saldo " +
    							   " FROM ClientSaldoTypes st " +
 								   " 		LEFT JOIN ClientSaldo s ON st.SaldoTypeID=s.SaldoTypeID AND s.AddrID= %d AND s.DirectionID=%d", addrID, defaultDirectionID);
    	Cursor saldoCursor = Db.getInstance().selectSQL(sql);

		if(saldoCursor!=null)
		{
			TableLayout table = (TableLayout) ((Activity)context).findViewById(saldoTableViewId);
			int nameColumnIdx = saldoCursor.getColumnIndex("SaldoTypeName");
			int saldoColumnIdx = saldoCursor.getColumnIndex("Saldo");

			for(int i = 0; i < saldoCursor.getCount(); i++)
			{
				saldoCursor.moveToPosition(i);

				//inflate table row from resource file and add it to table
				LayoutInflater inflater = ((Activity)context).getLayoutInflater();
				LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.client_saldo_row,
														(ViewGroup) ((Activity)context).findViewById(R.id.saldoRow));
				TableRow row = (TableRow) layout.findViewById(R.id.saldoTableRow);
				TextView textName = (TextView) row.findViewById(R.id.saldoName);
				TextView textValue = (TextView) row.findViewById(R.id.saldoValue);
				textName.setText(saldoCursor.getString(nameColumnIdx));
				textValue.setText(saldoCursor.getString(saldoColumnIdx));
				textName.setTextColor(textColor);
				textValue.setTextColor(textColor);
				if(displayBullet==false)
				{
					ImageView bulletImage = (ImageView) row.findViewById(R.id.bulletImage);
					if(bulletImage!=null)
						bulletImage.setVisibility(View.GONE);
				}

				layout.removeView(row);
				table.addView(row);
			}

			saldoCursor.close();
		}

    }

}
