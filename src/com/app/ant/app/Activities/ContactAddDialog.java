package com.app.ant.app.Activities;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 23.01.12
 * Time: 13:56
 * To change this template use File | Settings | File Templates.
 */
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

import com.app.ant.R;
import com.app.ant.app.BusinessLayer.Contact;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.ErrorHandler;

/** Всплывающий диалог для редактирования контактных данных*/
public class ContactAddDialog extends DialogBase
{
	EditText textPerson;
	EditText textPosition;
	EditText textPhone1;
	EditText textPhone2;
	EditText textEmail;

	private Context context;
	private Contact contact;
	//---------------------------------------------------
	/** Интерфейс для обратного вызова - отправка результата*/
    public interface OnContactSubmitListener
    {
    	/** Сообщает вызывающему методу результат редактирования
    	 *
    	 * @param contact контактные данные после редактирования
    	 */
        abstract void onContactSubmit(Contact contact);
    }
    private OnContactSubmitListener contactSubmitListener = null;

    /** Установить callback для получения результата*/
	public void setContactSubmitListener(OnContactSubmitListener listener) { contactSubmitListener = listener; }

    //--------------------------------------------------------------
	/** Инициализация диалога. Заполняет элементы формы соотв. значениями, включает обработку нажатия на кнопку "Ок"
	 * @param contact контактные данные для редактирования
	 */
	public Dialog onCreate(Context context, Contact contact)
	{
		try
		{
			this.context = context;
			this.contact = contact;

			String title = context.getResources().getString(R.string.addr_contact_header);

			Dialog dlg = super.onCreate(context, R.layout.contact, R.id.addrContact,
    				 					title, DIALOG_FLAG_HAVE_OK_BUTTON | DIALOG_FLAG_HAVE_CANCEL_BUTTON | DIALOG_FLAG_NO_TITLE );

			textPerson = (EditText) super.findViewById(R.id.textPerson);
			textPosition = (EditText) super.findViewById(R.id.textPosition);
			textPhone1 = (EditText) super.findViewById(R.id.textPhone1);
			textPhone2 = (EditText) super.findViewById(R.id.textPhone2);
			textEmail = (EditText) super.findViewById(R.id.textEmail);

			if(contact!=null)
			{
        		textPerson.setText(contact.person);
        		textPosition.setText(contact.position);
        		textPhone1.setText(contact.phone);
        		textPhone2.setText(contact.phone2);
        		textEmail.setText(contact.email);
			}

			super.setOkClickListener(new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					try
					{
						Contact contact = ContactAddDialog.this.contact;

						if(contact == null)
						{
							contact = new Contact();

							//get free ID for new contact
							String sql = "SELECT min(ContactID) AS ContactID FROM Contacts ";
							long res = Db.getInstance().getDataLongValue(sql, 0);
							contact.contactID = res > 0 ? (res*-1) -1: res - 1;
						}

						contact.person = textPerson.getText().toString();
						contact.position = textPosition.getText().toString();
						contact.phone = textPhone1.getText().toString();
						contact.phone2 = textPhone2.getText().toString();
						contact.email = textEmail.getText().toString();

						//insert new record to database or replace existing
						String sqlInsert = "INSERT OR REPLACE into Contacts (ContactID, AddrID, FIO, Position, Phone, Phone2, Email, State, Sent )"
													+ " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?)";
						Object[] bindArgs = new Object[] { contact.contactID, AntContext.getInstance().getAddrID(), contact.person,
																				contact.position, contact.phone, contact.phone2, contact.email, 'N', 0 };
						Db.getInstance().execSQL(sqlInsert, bindArgs);

						if(contactSubmitListener!=null)
							contactSubmitListener.onContactSubmit(contact);
					}
					catch(Exception ex)
					{
						Context context = ContactAddDialog.this.context;
						MessageBox.show(context, context.getResources().getString(R.string.form_title_contacts), context.getResources().getString(R.string.addr_contact_exceptionOnEdit));
						ErrorHandler.CatchError("Exception in ContactAddDialog.saveContact.onClick", ex);
					}

				}
			});

			return dlg;
		}
		catch(Exception ex)
		{
			MessageBox.show(context, context.getResources().getString(R.string.form_title_contacts), context.getResources().getString(R.string.addr_contact_exceptionOnEdit));
			ErrorHandler.CatchError("Exception in ContactsForm.onCreateDialog", ex);
		}

		return null;
	}
}
