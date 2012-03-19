package com.app.ant.app.Activities;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 23.01.12
 * Time: 13:50
 * To change this template use File | Settings | File Templates.
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.app.ant.R;
import com.app.ant.app.ServiceLayer.ErrorHandler;

/** Базовый класс для диалоговых окон */
public class DialogBase
{
	//------------------------- Dialog creation flags ----------------------------------------
	/** флаги диалога - по умолчанию */
	public static final long DIALOG_FLAG_DEFAULT = 0;
	/** флаг диалога - показывать кнопку "Отмена"*/
	public static final long DIALOG_FLAG_HAVE_CANCEL_BUTTON = 1;
	/** флаг диалога - показывать кнопку "Ок"*/
	public static final long DIALOG_FLAG_HAVE_OK_BUTTON = 2;
	/** флаг диалога - скрыть заголовок*/
	public static final long DIALOG_FLAG_NO_TITLE = 4;

	//-------------------------------------------------------------
	private DialogInterface.OnClickListener cancelClickListener = null;
	private DialogInterface.OnClickListener okClickListener = null;
	private OnSelectItemListener selectItemListener = null;

	/** Интерфейс для обратного вызова - выбор значения из списка*/
    public interface OnSelectItemListener
    {
        abstract void onItemSelected(Object item);
    }

	/** Установить callback для кнопки Отмена*/
	public void setCancelClickListener(DialogInterface.OnClickListener listener) { cancelClickListener = listener; }
	/** Установить callback для кнопки Ok*/
	public void setOkClickListener(DialogInterface.OnClickListener listener) { okClickListener = listener; }
    /** Установить callback для получения результата (выбранное значение из списка)*/
	public void setSelectItemListener(OnSelectItemListener listener) { selectItemListener = listener; }

	//--------------------------------------------------------------
	private View layout = null;

	//--------------------------------------------------------------
	/** поиск View по идентификатору */
	public View findViewById(int id)
	{
		if(layout!=null)
			return layout.findViewById(id);

		return null;
	}

	//--------------------------------------------------------------
	/** Инициализация диалога в соответствии с флагами
	 * @param context контекст
	 * @param dialogResID идентификатор ресурсов диалога
	 * @param rootViewID идентификатор базового View в ресурсе диалога
	 * @param title заголовок диалога
	 * @param flags флаги настройки отображения диалога
	 */
    public Dialog onCreate(Context context, int dialogResID, int rootViewID, String title,  long flags)
    {
    	try
    	{
			//create dialog
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			layout = inflater.inflate(dialogResID, (ViewGroup) ((Activity)context).findViewById(rootViewID));

			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setView(layout);
			if( !((flags & DIALOG_FLAG_NO_TITLE)>0) )
				builder.setMessage(title);

			//create buttons
			boolean haveCancelButton = (flags & DIALOG_FLAG_HAVE_CANCEL_BUTTON) >0;
			boolean haveOkButton = (flags & DIALOG_FLAG_HAVE_OK_BUTTON) >0;

			DialogInterface.OnClickListener cancelClick = new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					if(cancelClickListener != null)
						cancelClickListener.onClick(dialog, which);
				}
			};

			DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					if(okClickListener != null)
						okClickListener.onClick(dialog, which);
				}
			};

			String cancelText = ((Activity)context).getResources().getString(R.string.dialog_base_cancel);
			String okText = ((Activity)context).getResources().getString(R.string.dialog_base_ok);

			if( haveCancelButton && haveOkButton )
			{
				builder.setPositiveButton(okText, okClick);
				builder.setNegativeButton(cancelText, cancelClick);
			}
			else
				if( haveCancelButton && !haveOkButton )
					builder.setPositiveButton(cancelText, cancelClick);
				else if( !haveCancelButton && haveOkButton )
					builder.setPositiveButton(okText, okClick);

			return builder.create();
    	}
		catch(Exception ex)
		{
			ErrorHandler.CatchError("Exception in DialogBase.onCreate", ex);
		}

		return null;
    }

}