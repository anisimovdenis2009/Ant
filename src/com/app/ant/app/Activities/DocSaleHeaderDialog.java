package com.app.ant.app.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.app.ant.R;
import com.app.ant.app.BusinessLayer.Document;
import com.app.ant.app.BusinessLayer.DocSale.DocSaleHeader;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class DocSaleHeaderDialog extends DialogBase implements CompoundButton.OnCheckedChangeListener {
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (mDocHeader.welcomeDiscount) {
            mDocHeader.welcomeDiscount = false;
            mCheckBoxWelcom.setChecked(mDocHeader.welcomeDiscount);
            mDocHeader.updateDbHeaderField("IsNeedWelc", "NULL");
        } else {
            mDocHeader.welcomeDiscount = true;
            mCheckBoxWelcom.setChecked(mDocHeader.welcomeDiscount);
            mDocHeader.updateDbHeaderField("IsNeedWelc", "Y");
        }
    }

    /**
     * ����������� ������ ��� �����������
     */
    public enum DocSaleHeaderPopup {
        Date, Respite, Comments1, Comments2, Contact
    }

    ;

    private TextView textDialogTitle;
    private EditText mTextEditDocType;
    private EditText mTextEditDate;
    private CheckBox mCheckBoxWelcom;
    //private EditText mTextEditComments1;
    //private EditText mTextEditComments2;
    private Spinner spnPrices;
    private Spinner spnPaymentConditions;
    Spinner spnAddresses;

    private boolean isEditable = false;

    HashMap<String, String> paymentConditionsList;
    private Context context;
    private DocSaleHeader mDocHeader;

    private Cursor pricesCursor;
    private Cursor paymentConditionsCursor;

    boolean contactSpinnerAlreadySelected = false;

    //--------------------------------------------------------------

    /**
     * ��������� ��� ��������� ������ - ����������� ����� ��������������� ����������� ����
     */
    public interface OnDocSaleHeaderPopupListener {
        abstract void onDocSaleHeaderPopup(DocSaleHeaderPopup whichDialog);
    }

    private OnDocSaleHeaderPopupListener docSaleHeaderPopupListener = null;

    public void setDocSaleHeaderPopupListener(OnDocSaleHeaderPopupListener listener) {
        docSaleHeaderPopupListener = listener;
    }

    //--------------------------------------------------------------

    /**
     * �������� ���������� � ������
     */
    public static class Price implements Cloneable {
        public long id;
        public String name;

        public Price(long id, String name) {
            this.name = name;
            this.id = id;
        }

        public Price(long id) {
            this.id = id;
            String sql = "SELECT PriceName FROM PriceHeads WHERE PriceID=" + id;
            this.name = Db.getInstance().getDataStringValue(sql, "");
        }

        public Price clone() throws CloneNotSupportedException {
            Price cloned = (Price) super.clone();
            return cloned;
        }

    }

    //--------------------------------------------------------------	
    public Dialog onCreate(final Context context, boolean isEditable, DocSaleHeader docSaleHeader) {
        //try {
        this.context = context;
        this.isEditable = isEditable;
        this.mDocHeader = docSaleHeader;

        String title = context.getResources().getString(R.string.doc_sale_header_title);

        Dialog dlg = super.onCreate(context, R.layout.doc_sale_header, R.id.docSaleHeaderDialog,
                title, DIALOG_FLAG_HAVE_OK_BUTTON | DIALOG_FLAG_HAVE_CANCEL_BUTTON | DIALOG_FLAG_NO_TITLE);

        textDialogTitle = (TextView) findViewById(R.id.textDialogTitle);
        textDialogTitle.setText(title);

        mTextEditDocType = (EditText) findViewById(R.id.textEditDocType);
        mTextEditDate = (EditText) findViewById(R.id.textEditDate);
        mCheckBoxWelcom = (CheckBox) findViewById(R.id.checkBoxWelcome);
        //mTextEditComments1 = (EditText) findViewById(R.id.textEditComments1);
        //mTextEditComments2 = (EditText) findViewById(R.id.textEditComments2);
        spnPrices = (Spinner) findViewById(R.id.spnPrices);
        spnPrices.setEnabled(false);

        spnPaymentConditions = (Spinner) findViewById(R.id.spPaymentConditions);
        if (mDocHeader != null) {
            //
            // doc header - Date
            //

            View.OnClickListener dateClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!DocSaleHeaderDialog.this.isEditable)
                        return;

                    Context context = DocSaleHeaderDialog.this.context;

                    if (docSaleHeaderPopupListener != null)
                        docSaleHeaderPopupListener.onDocSaleHeaderPopup(DocSaleHeaderPopup.Date);
                }
            };
            mTextEditDate.setOnClickListener(dateClickListener);
            updateDateDisplay();

            //
            // doc header - type
            //

            View.OnClickListener docTypeClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!DocSaleHeaderDialog.this.isEditable)
                        return;
                    if (mDocHeader != null) {
                        if (mDocHeader.docType == Document.DOC_TYPE_CLAIM)
                            mDocHeader.docType = Document.DOC_TYPE_SALE;
                        else if (mDocHeader.docType == Document.DOC_TYPE_SALE)
                            mDocHeader.docType = Document.DOC_TYPE_CLAIM;

                        updateDocTypeDisplay();
                    }
                }
            };
            mTextEditDocType.setOnClickListener(docTypeClickListener);
            if (mDocHeader != null)
                updateDocTypeDisplay();

            //
            // doc header - address
            //
            //make address selector visible. fill it with values
            spnAddresses = (Spinner) findViewById(R.id.spnAddresses);
            if(verifyForWZ()){
                String sql = "SELECT a.AddrID AS _id, a.AddrName FROM Addresses a\n" +
                        "WHERE a.ClientID = %ClientID%\n" +
                        "AND EXISTS (SELECT 1 FROM AddressAttributes aa, Settings s\n" +
                        "            WHERE aa.AddrID = a.AddrID\n" +
                        "            AND aa.AttrID = s.DefaultValue\n" +
                        "            AND s.Property = 'attr_id_delivery')\n" +
                        "ORDER BY AddrName" ;
                sql = sql.replace("%ClientID%",String.valueOf(AntContext.getInstance().getClient().clientID));
                final Cursor cursor = Db.getInstance().selectSQL(sql);
                ((Activity)context).startManagingCursor(cursor);

                String[] from = new String[] { "AddrName" };
                int[] to = new int[] { android.R.id.text1 };
                SimpleCursorAdapter curAdapter = new SimpleCursorAdapter(context, R.layout.spinner_item, cursor, from, to);
                curAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spnAddresses.setAdapter(curAdapter);

                //select default address (this is address we started visit with)
                for(int i=0; i<spnAddresses.getCount();i++)
                {
                    if(spnAddresses.getItemIdAtPosition(i) == mDocHeader.addrID)
                    {
                        spnAddresses.setSelection(i);
                        break;
                    }
                }
                cursor.close();
            } else DocListForm.fillAddressSpinner(context, spnAddresses, AntContext.getInstance().getClientID(), mDocHeader.addrID, false, true);


            
            spnAddresses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        if (id != mDocHeader.addrID)
                            mDocHeader.addrID = id;
                    } catch (Exception ex) {
                        ErrorHandler.CatchError("Exception in DocSaleHeaderDialog::onItemSelected", ex);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
            //
            // doc header - respite
            //
            /*View.OnClickListener respiteClickListener = new View.OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    if(!DocSaleHeaderDialog.this.isEditable)
                        return;

                    if(docSaleHeaderPopupListener!=null)
                        docSaleHeaderPopupListener.onDocSaleHeaderPopup(DocSaleHeaderPopup.Respite);
                }
            };*/

            mCheckBoxWelcom.setOnCheckedChangeListener(this);
            mCheckBoxWelcom.setChecked(docSaleHeader.welcomeDiscount);
            //updateRespiteDisplay();

            //
            // doc header - price
            //
            String sql = "SELECT PriceID as _id, PriceName FROM PriceHeads";
            pricesCursor = Db.getInstance().selectSQL(sql);
            ((Activity) context).startManagingCursor(pricesCursor);

            String[] from = new String[]{"PriceName"};
            int[] to = new int[]{android.R.id.text1};
            SimpleCursorAdapter pricesAdapter = new SimpleCursorAdapter(context, R.layout.spinner_item, pricesCursor, from, to);
            pricesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            pricesAdapter.setViewResource(R.layout.simple_spinner_dropdown_item_ex_left);
            spnPrices.setAdapter(pricesAdapter);

            //select current price entry
            for (int i = 0; i < spnPrices.getCount(); i++) {
                if (spnPrices.getItemIdAtPosition(i) == mDocHeader.price.id) {
                    spnPrices.setSelection(i);
                    break;
                }
            }

            spnPrices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        if (id != mDocHeader.price.id)
                            mDocHeader.price = new Price(id);
                    } catch (Exception ex) {
                        ErrorHandler.CatchError("Exception in DocSaleHeaderDialog::onItemSelected", ex);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });

            /*//
               // doc header - comments
               //
            mTextEditComments1.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    if(!DocSaleHeaderDialog.this.isEditable)
                        return;

                    if(docSaleHeaderPopupListener!=null)
                        docSaleHeaderPopupListener.onDocSaleHeaderPopup(DocSaleHeaderPopup.Comments1);
                }
            });

            mTextEditComments2.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    if(!DocSaleHeaderDialog.this.isEditable)
                        return;

                    if(docSaleHeaderPopupListener!=null)
                        docSaleHeaderPopupListener.onDocSaleHeaderPopup(DocSaleHeaderPopup.Comments2);				}
            });
            updateCommentsDisplay();*/

            paymentConditionsList = new HashMap<String, String>();
            paymentConditionsCursor = Db.getInstance().selectSQL("select PayTermID, PaytermName from PayTerms order by 2 ");

            if (paymentConditionsCursor != null) {
                for (int i = 0; i < paymentConditionsCursor.getCount(); i++) {
                    if (paymentConditionsCursor.moveToPosition(i)) {
                        String key = paymentConditionsCursor.getString(1);
                        String value = String.valueOf(paymentConditionsCursor.getInt(0));
                        paymentConditionsList.put(key, value);
                    }
                }
                paymentConditionsCursor.close();
            }
            //spnPaymentConditions.setAdapter(new SimpleAdapter());
            final Object[] keySet = paymentConditionsList.keySet().toArray();
            BaseAdapter adapter = new BaseAdapter() {
                @Override
                public int getCount() {
                    return keySet.length;
                }

                @Override
                public Object getItem(int i) {
                    return keySet[i];
                }

                @Override
                public long getItemId(int i) {
                    return i;
                }

                @Override
                public View getView(int i, View view, ViewGroup viewGroup) {
                    TextView textView = new TextView(context);
                    textView.setText((String) getItem(i));
                    textView.setTextSize(20);
                    return textView;
                }
            };
            /*from = new String[]{"PriceName"};
            to = new int[]{android.R.id.text1};
            SimpleCursorAdapter paymentAdapter = new SimpleCursorAdapter(context, R.layout.spinner_item, pricesCursor, from, to);
            paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            paymentAdapter.setViewResource(R.layout.simple_spinner_dropdown_item_ex_left);*/
            spnPaymentConditions.setAdapter(adapter);
            //select default address (this is address we started visit with)
            for(int i=0; i<spnPaymentConditions.getCount();i++)
            {
                if(spnPaymentConditions.getItemIdAtPosition(i) == docSaleHeader.paymentID)
                {
                    spnPaymentConditions.setSelection(i);
                    break;
                }
            }
            spnPaymentConditions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String key = ((TextView) view).getText().toString();
                    String value = paymentConditionsList.get(key);
                    mDocHeader.updateDbHeaderField("PayTermID", value);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            /* SimpleCursorAdapter contactsAdapter = DocDebtNotificationForm.fillContactsSpinner(context, AntContext.getInstance().getAddrID(), spnPaymentConditions, true);
           contactCursor = contactsAdapter.getCursor();

           if (mDocHeader.contactID == DocDebtNotificationForm.DEFAULT_CONTACT_ID && contactCursor.getCount() > 1) {
               contactCursor.moveToPosition(1);
               mDocHeader.contactID = contactCursor.getLong(contactCursor.getColumnIndex("_id"));
           }

           DocDebtNotificationForm.selectContactInSpinner(spnPaymentConditions, mDocHeader.contactID);*/

            /*spnPaymentConditions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        if (!contactSpinnerAlreadySelected) {
                            contactSpinnerAlreadySelected = true;
                            return;
                        }

                        mDocHeader.prevContactID = mDocHeader.contactID;

                        if (id != mDocHeader.contactID)
                            mDocHeader.contactID = id;

                        if (id == DocDebtNotificationForm.DEFAULT_CONTACT_ID) {
                            if (!DocSaleHeaderDialog.this.isEditable)
                                return;

                            Context context = DocSaleHeaderDialog.this.context;

                            if (docSaleHeaderPopupListener != null)
                                docSaleHeaderPopupListener.onDocSaleHeaderPopup(DocSaleHeaderPopup.Contact);
                        }
                    } catch (Exception ex) {
                        ErrorHandler.CatchError("Exception in DocSaleHeaderDialog::onItemSelected", ex);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });*/

        }
        return dlg;
        /* } catch (Exception ex) {
            MessageBox.show(context, context.getResources().getString(R.string.form_title_document),
                    context.getResources().getString(R.string.doc_sale_header_exception_create));
            ErrorHandler.CatchError("Exception in DocSaleHeaderDialog.onCreate", ex);
        }*/


    }

    private boolean verifyForWZ() {
        boolean result = false;
        String select = "SELECT 1 AS IsOrderPt FROM AddressAttributes aa, Settings s\n" +
                "WHERE aa.AttrID = s.DefaultValue\n" +
                "AND s.Property = 'attr_id_order'\n" +
                "AND a.AddrID = " + mDocHeader.addrID;
        Cursor cursor = Db.getInstance().selectSQL(select);
        if(cursor!=null){
              if(cursor.moveToFirst())
              {
                int a =cursor.getInt(cursor.getColumnIndex("IsOrderPt"));
                  if(a==1)
                      result = true;
              }
            cursor.close();
        }
        return result;
    }

    //--------------------------------------------------------------
    private void updateDateDisplay() {
        if (mDocHeader != null) {
            Calendar docDate = mDocHeader.docDate;
            String dateText = "";
            if (docDate != null)
                dateText = Convert.dateToString(docDate);
            mTextEditDate.setText(dateText);
        }
    }

    //--------------------------------------------------------------
    private void updateDocTypeDisplay() {

        int longNameResourceId = Document.getDocReadableTypeResId(mDocHeader.docType);
        String docTypeText = context.getResources().getString(longNameResourceId);

        mTextEditDocType.setText(docTypeText);
    }

    ;

    /* //--------------------------------------------------------------
    private void updateRespiteDisplay() {
        String strRespite = Integer.toString(mDocHeader.respite);
        mCheckBoxWelcom.setText(strRespite);
    }*/

    /* //--------------------------------------------------------------
        private void updateCommentsDisplay()
        {
          mTextEditComments1.setText(mDocHeader.comments1 + " " + mDocHeader.specMarks1);
          mTextEditComments2.setText(mDocHeader.comments2 + " " + mDocHeader.specMarks2);
        }	*/

}    
