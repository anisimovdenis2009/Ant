package com.app.ant.app.Activities;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 23.01.12
 * Time: 14:49
 * To change this template use File | Settings | File Templates.
 */
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import com.app.ant.R;
import com.app.ant.app.DataLayer.Db;

/** Всплывающий диалог выбора типа визита. Возможен выбор нескольких значений
 */

public class SelectVisitTypeDialog
{
	/**	Интерфейс для обратного вызова - возвращает результат выбора типа визита*/
    public interface VisitTypeSelectListener
    {
        abstract void onVisitTypeSelected(int visitType, boolean needDocCountCheck);
    }

	public void show(final Context context, int defaultVisitTypes, final boolean allowUncheck,
							final VisitTypeSelectListener resultListener, DialogInterface.OnClickListener cancelClickListener)
	{
		// редактирование типов визита (заказ, финансовые вопросы, работа с документами)
		String sql = "SELECT VisitTypeID, VisitTypeName, NeedCheckCountDocs, VisitTypeMS FROM VisitTypes";
		Cursor visitTypesCursor = Db.getInstance().selectSQL(sql);

		if(visitTypesCursor==null || visitTypesCursor.getCount()==0)
			return;

		int count = visitTypesCursor.getCount();

		CharSequence[] names = new CharSequence[count];
		final boolean[] checkedDefault = new boolean [count];
		final boolean[] checked = new boolean [count];
		final int[] masks = new int[count];
		final boolean[] needDocCountCheck = new boolean[count];

		int visitNameColumnIdx = visitTypesCursor.getColumnIndex("VisitTypeName");
		int needDocCountCheckIdx = visitTypesCursor.getColumnIndex("NeedCheckCountDocs");
		int visitTypeMaskIdx = visitTypesCursor.getColumnIndex("VisitTypeMS");

		for(int i=0; i<visitTypesCursor.getCount(); i++)
		{
			visitTypesCursor.moveToPosition(i);

			names[i] = visitTypesCursor.getString(visitNameColumnIdx);
			masks[i] = visitTypesCursor.getInt(visitTypeMaskIdx);
			checkedDefault[i] = checked[i] = (defaultVisitTypes & masks[i])>0;
			needDocCountCheck[i] = visitTypesCursor.getInt(needDocCountCheckIdx)>0;

		}

		visitTypesCursor.close();

		AlertDialog alertDialog = new AlertDialog.Builder(context)
        .setTitle(R.string.visit_select_visit_type_title)
        .setMultiChoiceItems(
        		names,
                checked,
                new DialogInterface.OnMultiChoiceClickListener()
        		{
                    public void onClick(DialogInterface dialog, int whichButton, boolean isChecked)
                    {
                    	if(!allowUncheck && checkedDefault[whichButton]==true && !isChecked)
                    	{
                    		//user have tried to uncheck item that is not allowed, restore it back
                    		((AlertDialog)dialog).getListView().setItemChecked(whichButton, true);
                    		checked[whichButton] = true;
                    	}
                    }
                })
        .setPositiveButton(R.string.dialog_base_ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
            	int result = 0;
            	boolean bHaveChecked = false;
            	boolean bNeedDocCountCheck = false;

            	//compose mask
            	for(int i=0; i<checked.length; i++)
            	{
            		if(checked[i])
            		{
            			bHaveChecked = true;
            			result |= masks[i];
            			if(needDocCountCheck[i])
            				bNeedDocCountCheck = true;
            		}
            	}

            	if(!bHaveChecked)
            	{
            		//Ни один из типов визита не выбран, выводим предупреждение пользователю что надо выбрать
            		MessageBox.show(context, context.getResources().getString(R.string.message_box_warning),
            								 context.getResources().getString(R.string.visit_select_visit_type));
            	}
            	else
            	{
	            	if(resultListener!=null)
	            		resultListener.onVisitTypeSelected(result, bNeedDocCountCheck);
            	}
            }
        })
        .setNegativeButton(R.string.dialog_base_cancel, cancelClickListener)
        .create();

		alertDialog.show();
	}

}


