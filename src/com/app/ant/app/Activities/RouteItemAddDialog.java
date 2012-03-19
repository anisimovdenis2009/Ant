package com.app.ant.app.Activities;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 23.01.12
 * Time: 14:51
 * To change this template use File | Settings | File Templates.
 */
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.TextView;
import com.app.ant.R;

import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

/** Всплывающий диалог добавления точки на маршрут. Содержит иерархический список клиентов/адресов и поле для редактирования
 * даты посещения
 */
public class RouteItemAddDialog extends DialogBase
{
	/**	Интерфейс для обратного вызова - возвращает выбранный адрес и дату*/
    public interface AddressAndDateSelectListener
    {
        abstract void onAddressAndDateSelected(long clientId, long addrId, Calendar date);
    }

	private ClientListForm.ClientListData clientListData = new ClientListForm.ClientListData();
	private Calendar selectedDate;

    private ExpandableListView mClientAddrExpandableList;
    private ViewGroup mDatePanel;
    private TextView mTextDate;

    private AddressAndDateSelectListener addrAndDateSelectListener;
    private Context context;

	//--------------------------------------------------------------
	public Dialog onCreate(final Context context,
							Calendar defaultDate,
							DialogInterface.OnClickListener cancelClickListener,
							final AddressAndDateSelectListener addrAndDateSelectListener)
    {
    	try
    	{
    		if (isToday(defaultDate) || isEarlierThenToday(defaultDate))
    		{
    			selectedDate = Calendar.getInstance();
    			selectedDate.add(Calendar.DAY_OF_MONTH , 1);
    		}
    		else
    			selectedDate = defaultDate;

    		this.addrAndDateSelectListener = addrAndDateSelectListener;
    		this.context = context;

    		String title = context.getResources().getString(R.string.route_item_add);
			Dialog dlg = super.onCreate(context, R.layout.route_item_add_dialog, R.id.routeItemAdd,
 								title, DIALOG_FLAG_HAVE_OK_BUTTON | DIALOG_FLAG_HAVE_CANCEL_BUTTON | DIALOG_FLAG_NO_TITLE );
			super.setCancelClickListener(cancelClickListener);

			mClientAddrExpandableList = ((ExpandableListView) findViewById(R.id.clientAddrExpandableList));
			mDatePanel = (ViewGroup) findViewById(R.id.datePanel);
			mTextDate = (TextView) findViewById(R.id.textDate);

	        mDatePanel.setOnClickListener(new ViewGroup.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					try
					{
						displayDateDialog(context, selectedDate);
					}
					catch(Exception ex)
					{
						ErrorHandler.CatchError("Exception in datePanel.onClick", ex);
					}
				}
			});
	        updateDateDisplay();

			//
			//fill client/address list
			//
	        fillClientList();


			return dlg;
    	}
		catch(Exception ex)
		{
			ErrorHandler.CatchError("Exception in RouteItemAddDialog.onCreate", ex);
		}

    	return null;
    }
	//--------------------------------------------------------------
	/** Заполнение списка клиентов/адресов. Адреса, которые уже есть в маршруте на заданную дату, не включаются в список*/
	private void fillClientList()
	{
    	ClientListForm.fillClientAddrList(context, null, false, selectedDate, clientListData, mClientAddrExpandableList,
				new ClientListForm.AddressSelectListener()
				{
					public void onAddressSelected(final long clientId, final long addrId, int visitTypeID)
					{
						if(addrAndDateSelectListener!=null)
							addrAndDateSelectListener.onAddressAndDateSelected(clientId, addrId, selectedDate);
					}
				},
				null, /*deleteListener = null because we do not delete items from this dialog*/
				null,  /*moveListener = null because we do not move items using this dialog*/
				null,
				true
			);
	}

    //--------------------------------------------------------------
    /** Отображение выбранной даты на окне*/
    private void updateDateDisplay()
    {
        mTextDate.setText( Convert.dateToString(selectedDate));
    }

    private boolean isToday(Calendar calendar)
    {
    	boolean result = false;

    	if (calendar != null)
    	{
    		result = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
            		&& Calendar.getInstance().get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
            		&& Calendar.getInstance().get(Calendar.YEAR) == calendar.get(Calendar.YEAR);
    	}
    	return result;
    }

    private boolean isEarlierThenToday(Calendar calendar)
    {
    	boolean result = true;

    	if (calendar != null)
    	{
    		if (Calendar.getInstance().get(Calendar.YEAR) > calendar.get(Calendar.YEAR)) return result;
    		if (Calendar.getInstance().get(Calendar.MONTH) > calendar.get(Calendar.MONTH)) return result;
    		if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) > calendar.get(Calendar.DAY_OF_MONTH)) return result;
    		result = false;
    	}
    	return result;
    }

	//--------------------------------------------------------------
	private void displayDateDialog(Context context, Calendar date)
	{
		Dialog dlg = new DatePickerDialog(context,
        		new DatePickerDialog.OnDateSetListener()
        		{
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                    {
                        Calendar date = Calendar.getInstance();
                        date.set(year, monthOfYear, dayOfMonth);

                    	if (isToday(date) || isEarlierThenToday(date))
                    	{
                			selectedDate = Calendar.getInstance();
                			selectedDate.add(Calendar.DAY_OF_MONTH , 1);
                    	}
                    	else
                    	{
                    		selectedDate.set(year, monthOfYear, dayOfMonth);
                    	}
                    	updateDateDisplay();
                        fillClientList();
                    }
        		},
        		date.get(Calendar.YEAR),
        		date.get(Calendar.MONTH),
        		date.get(Calendar.DAY_OF_MONTH));

		dlg.show();
	}

}

