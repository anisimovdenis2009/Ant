package com.app.ant.app.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.*;
import com.app.ant.R;
import com.app.ant.app.Activities.MessageBox.MessageBoxButton;
import com.app.ant.app.Activities.QuestionairesDialog.OnProcessQuestionnaire;
import com.app.ant.app.BusinessLayer.*;
import com.app.ant.app.BusinessLayer.Common;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Settings;
import com.app.ant.app.ServiceLayer.StepController.StepPanelType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * форма содержащая информацию о клиенте и адресе
 */
public class ClientForm extends AntActivity implements View.OnClickListener{
    private final static int IDD_CONTACT_POPUP = 0;
    private final static int IDD_ADDR_TYPE_POPUP = 1;
    private final static int IDD_ADDR_EDIT_POPUP = 2;

    SimpleDateFormat sdf = new SimpleDateFormat(Common.DATE_FORMAT_NOW);
    SimpleDateFormat tdf = new SimpleDateFormat(Common.DATE_FORMAT_NOW_DATABASE);
    private boolean isNewContact = true;
    private Long currentContactId = null;
    private final Context context = this;

    private int ActCode = 123;

    private Address address;

    TableLayout classificationTable;

    //--------------------------------------------------------------

    /**
     * Инициализация формы
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        /*try
          {*/
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        overridePendingTransition(0, 0);
        setContentView(R.layout.client);

        Button btnAddContact = (Button) findViewById(R.id.btnContactAdd);
        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNewContact = true;
                showDialog(IDD_CONTACT_POPUP);
            }
        });

        Button buttonShowMap = (Button) findViewById(R.id.buttonShowMap);
        buttonShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(ClientForm.this, MapForm.class));

                /*try
                        {*/
                Address address = AntContext.getInstance().getAddress();
                /*
                            String uri = String.format("geo:%s,%s?z=20", Double.toString(address.latitude), Double.toString(address.longitude));
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            startActivity(intent);
                            */
                Intent intent = new Intent(ClientForm.this, ClientAddressMapView.class);
                intent.putExtra("latitude", address.latitude);
                intent.putExtra("longitude", address.longitude);
                intent.putExtra("address", address.addrName);

                startActivityForResult(intent, ActCode);
                /*}
                        catch(Exception ex)
                        {
                            MessageBox.show(ClientForm.this, getResources().getString(R.string.form_title_client), getResources().getString(R.string.client_addr_exceptionShowMap));
                            ErrorHandler.CatchError("Exception in clientForm.onCreate", ex);
                        }*/

            }
        });

        //edit address and registration data
        View.OnClickListener editAddrClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(IDD_ADDR_EDIT_POPUP);
            }
        };
        /*Button buttonEditAddr = (Button) findViewById(R.id.buttonEditAddr);
        buttonEditAddr.setOnClickListener(editAddrClick);
        Button buttonEditRegData = (Button) findViewById(R.id.buttonEditRegData);
        buttonEditRegData.setOnClickListener(editAddrClick);

        //edit address attributes
        Button buttonEditAddrType = (Button) findViewById(R.id.buttonEditAddrType);
        buttonEditAddrType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(IDD_ADDR_TYPE_POPUP);
            }
        });*/


        initStepBar();

        fill();
        /*}
          catch(Exception ex)
          {
              MessageBox.show(this, getResources().getString(R.string.form_title_client), getResources().getString(R.string.client_addr_exceptionCreateForm));
              ErrorHandler.CatchError("Exception in clientForm.onCreate", ex);
          }*/
    }

    //--------------------------------------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123 & resultCode == 123) {
            Bundle bndl = data.getExtras();
            address.latitude = bndl.getDouble("latitude");
            address.longitude = bndl.getDouble("longitude");

            TextView textAddrLat = (TextView) findViewById(R.id.textLat);
            TextView textAddrLong = (TextView) findViewById(R.id.textLong);
            textAddrLat.setText(Convert.toString(address.latitude, "0.00000"));
            textAddrLong.setText(Convert.toString(address.longitude, "0.00000"));

            address.updateGeoCoordinates();
        }
    }

    //--------------------------------------------------------------
    private void initStepBar() {
        //init steps
        ViewGroup stepButtonPlacement = (ViewGroup) findViewById(R.id.stepButtonPlacement);
        AntContext.getInstance().getStepController().CreateButtons(this, stepButtonPlacement, StepPanelType.HORIZONTAL);

        //init tabs
        ViewGroup tabsPlacement = (ViewGroup) findViewById(R.id.tabsPlacement);
        AntContext.getInstance().getTabController().createTabs(this, tabsPlacement);
    }

    //--------------------------------------------------------------

    /**
     * заполнение полей формы
     */
    private void fill() {
        Client client = AntContext.getInstance().getClient();
        address = AntContext.getInstance().getAddress();

        //
        //get controls
        //
        TextView textClientName = (TextView) findViewById(R.id.textClientName);
        TextView textLicense = (TextView) findViewById(R.id.textLicense);
        TextView textRegDate = (TextView) findViewById(R.id.textRegDate);
        TextView textRegCode = (TextView) findViewById(R.id.textRegCode);
        TextView textCodeVAT = (TextView) findViewById(R.id.textCodeVAT);
        TextView textAddrName = (TextView) findViewById(R.id.textAddrName);
        TextView textAddrDeliveryDays = (TextView) findViewById(R.id.textAddrDeliveryDays);
        TextView textAddrChannelType = (TextView) findViewById(R.id.textAddrChannelType);
        TextView textAddrNumbLicAlc = (TextView) findViewById(R.id.textAddrNumbLicAlc);
        TextView textAddrEndDate = (TextView) findViewById(R.id.textAddrEndDate);
        TextView textAddrLat = (TextView) findViewById(R.id.textLat);
        TextView textAddrLong = (TextView) findViewById(R.id.textLong);

        TextView textERPIDAddress = (TextView) findViewById(R.id.textERPIDAddress);
        TextView textERPIDClient = (TextView) findViewById(R.id.textERPIDClient);

        //
        //display info about client
        //

        textClientName.setText(client.nameScreen);
        textLicense.setText(client.regNo);
        if (client.regDate != null && client.regDate.length() > 0) {
            try {
                textRegDate.setText(Convert.dateToString(Convert.getDateFromString(client.regDate)));
            } catch (Exception ex) {
                ErrorHandler.CatchError("Exception in clientForm.fill", ex);
            }
        }
        textRegCode.setText(client.subjCode);
        textCodeVAT.setText(client.taxNo);

        textAddrLat.setText(Convert.toString(address.latitude, "0.00000"));
        textAddrLong.setText(Convert.toString(address.longitude, "0.00000"));

        textERPIDAddress.setText(address.erpId);
        textERPIDClient.setText(client.erpId);

        textAddrName.setText(address.addrName);
        textAddrDeliveryDays.setText(address.deliveryDays);
        textAddrChannelType.setText(address.channelName);

        //
        //Proxies
        //

        String sql = String.format(" SELECT coalesce(ProxySeria, '')||coalesce(ProxyNO,'') AS ProxyNO, ProxyEnd" +
                " FROM Proxies " +
                " WHERE ClientID = %d AND ProxyTypeID=2", client.clientID);

        String[] proxyInfo = Db.getInstance().selectRowValues(sql);

        if (proxyInfo != null) {
            textAddrNumbLicAlc.setText(proxyInfo[0]);
            String proxyEnd = proxyInfo[1];

            if (!Convert.isNullOrBlank(proxyEnd)) {
                try {
                    String proxyEndFixed = Convert.dateToString(Convert.getDateFromString(proxyEnd));
                    textAddrEndDate.setText(proxyEndFixed);
                } catch (Exception ex) {
                    ErrorHandler.CatchError("Exception in clientForm.fill.proxyInfo.proxyEnd", ex);
                }
            }
        }

        classificationTable = (TableLayout) findViewById(R.id.attributesTable);
        insertClassificationRows(classificationTable);

        fillContacts();
        fillGold();
    }

    TextView getTw(int id) {
        TextView viewById = (TextView) findViewById(id);
        return viewById;
    }

    private void fillGold() {
        String select = "SELECT * FROM CustGSInfos WHERE AddrID = " + address.addrID;
        Cursor cursor = Db.getInstance().selectSQL(select);

        if (cursor != null) {
            if (cursor.getCount() == 0) {
                LinearLayout h = (LinearLayout) findViewById(R.id.client_golden_data_header);
                TableLayout e = (TableLayout) findViewById(R.id.client_golden_data_entity);
                h.setVisibility(View.GONE);
                e.setVisibility(View.GONE);
            } else if (cursor.moveToFirst()) {
                int cskutp = cursor.getInt(cursor.getColumnIndex("Cskutp"));
                getTw(R.id.textOPDPlan).setText(String.valueOf(cskutp));
                Integer cskutf = cursor.getInt(cursor.getColumnIndex("Cskutf"));
                getTw(R.id.textOPDFact).setText(cskutf.toString());
                Integer cskuap = cursor.getInt(cursor.getColumnIndex("Cskuap"));
                getTw(R.id.textListAPlan).setText(cskuap.toString());
                Integer cskuac = cursor.getInt(cursor.getColumnIndex("Cskuac"));
                getTw(R.id.textListAFact).setText(cskuac.toString());
                Integer cskuab = cursor.getInt(cursor.getColumnIndex("Cskuab"));
                getTw(R.id.textListAdd).setText(cskuab.toString());

                String cdate = cursor.getString(cursor.getColumnIndex("Cdate"));
                String format = getShortDateFormat(cdate);
                getTw(R.id.textContractDate).setText(format);

                String wdate = cursor.getString(cursor.getColumnIndex("Wdate"));
                getTw(R.id.textWellcomeDate).setText(getShortDateFormat(wdate));

                String discbeg = cursor.getString(cursor.getColumnIndex("Discbeg"));
                getTw(R.id.textWellcomeBeginDate).setText(getShortDateFormat(discbeg));

                String discend = cursor.getString(cursor.getColumnIndex("Discend"));
                getTw(R.id.textWellcomeEndDate).setText(getShortDateFormat(discend));

                getTw(R.id.textWellcomeDiscount).setText(cursor.getString(cursor.getColumnIndex("Discval")));
            }
            cursor.close();
        }

    }

    private String getShortDateFormat(String cdate) {
        Date date = null;
        if (cdate != null) {
            try {
                date = tdf.parse(cdate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return sdf.format(date);
        } else return " ";


    }

    //--------------------------------------------------------------

    /**
     * вывод типа торговой точки
     */
    private void insertClassificationRows(TableLayout table) {
        if (table == null)
            return;

        table.removeAllViews();

        String sql = String.format(" select a.AttrDescription " +
                " from AddressAttributes aa " +
                "      inner join Attributes a on aa.AttrID = a.AttrID " +
                " where AddrID = %d AND aa.State != '%s' " +
                "  ORDER BY aa.AttrID ",
                AntContext.getInstance().getAddress().addrID, Q.RECORD_STATE_DELETED);

        Cursor cursor = Db.getInstance().selectSQL(sql);

        if (cursor != null && cursor.getCount() > 0) {
            int idx = cursor.getColumnIndex("AttrDescription");

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                if (!cursor.isNull(idx)) {
                    LayoutInflater inflater = getLayoutInflater();
                    LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.client_attribute_row, (ViewGroup) findViewById(R.id.attributeRow));
                    TableRow tr = (TableRow) layout.findViewById(R.id.attributeTableRow);

                    TextView tvAttrName = (TextView) tr.findViewById(R.id.textAttrName);
                    String attrName = cursor.getString(idx);

                    tvAttrName.setText(attrName);

                    layout.removeView(tr);
                    table.addView(tr);
                }
            }
        }

        if (cursor != null)
            cursor.close();
    }

    //--------------------------------------------------------------

    /**
     * заполнение списка контактов
     */
    private void fillContacts() {
        LinearLayout contactsLayout = (LinearLayout) findViewById(R.id.contactsLayout);
        contactsLayout.removeAllViews();

        //get data from Contacts
        String sqlContacts = String.format("SELECT ContactID, FIO, Position, Phone, Phone2, Email "
                + " FROM Contacts "
                + " WHERE AddrID = %d AND Coalesce(State,'%s')<>'%s' ORDER BY FIO ",
                AntContext.getInstance().getAddrID(), Document.DOC_STATE_NEW, Document.DOC_STATE_DELETED);

        Cursor cursor = Db.getInstance().selectSQL(sqlContacts);

        if (cursor != null && cursor.getCount() > 0) {
            int idxId = cursor.getColumnIndex("ContactID");
            int idxFio = cursor.getColumnIndex("FIO");
            int idxPosition = cursor.getColumnIndex("Position");
            int idxPhone = cursor.getColumnIndex("Phone");

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                if (!cursor.isNull(idxId)) {
                    LayoutInflater inflater = getLayoutInflater();
                    LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.contact_list_item, (ViewGroup) findViewById(R.id.contactLayout));
                    RelativeLayout rl = (RelativeLayout) layout.findViewById(R.id.columnContactLayout);

                    TextView textPerson = (TextView) rl.findViewById(R.id.textPerson);
                    TextView textPosition = (TextView) rl.findViewById(R.id.textPosition);
                    TextView textPhone1 = (TextView) rl.findViewById(R.id.textPhone1);
                    ImageButton buttonEditContact = (ImageButton) rl.findViewById(R.id.buttonEditContact);
                    ImageButton buttonDeleteContact = (ImageButton) rl.findViewById(R.id.buttonDeleteContact);

                    Long id = cursor.getLong(idxId);
                    textPerson.setText(cursor.getString(idxFio));
                    textPosition.setText(cursor.getString(idxPosition));
                    textPhone1.setText(cursor.getString(idxPhone));

                    buttonEditContact.setTag(id);
                    buttonDeleteContact.setTag(id);

                    buttonDeleteContact.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                currentContactId = (Long) v.getTag();
                                askDeleteContact(currentContactId);
                            } catch (Exception ex) {
                                MessageBox.show(ClientForm.this, context.getResources().getString(R.string.form_title_contacts), context.getResources().getString(R.string.addr_contact_exceptionOnDelete));
                                ErrorHandler.CatchError("Exception in ClientForm.buttonDeleteContact.onClick", ex);
                            }
                        }
                    });

                    buttonEditContact.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                currentContactId = (Long) v.getTag();
                                isNewContact = false;
                                showDialog(IDD_CONTACT_POPUP);
                            } catch (Exception ex) {
                                ErrorHandler.CatchError("Exception in ClientForm.buttonEditContact.onClick", ex);
                            }
                        }
                    });

                    layout.removeView(rl);
                    contactsLayout.addView(rl);

                    //add line
                    TextView line = new TextView(this);
                    line.setBackgroundColor(i < cursor.getCount() - 1 ? Color.BLACK : Color.WHITE);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 1);
                    params.setMargins(0, 5, 0, 0);
                    line.setLayoutParams(params);
                    contactsLayout.addView(line);

                }
            }

        }
    }

    //--------------------------------------------------------------

    /**
     * создание диалогов
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        try {
            switch (id) {
                case IDD_CONTACT_POPUP:
                    try {
                        ContactAddDialog contactDialog = new ContactAddDialog();

                        Contact contact = null;
                        if (!isNewContact) {
                            contact = new Contact(currentContactId);
                        }

                        Dialog dlg = contactDialog.onCreate(this, contact);

                        contactDialog.setContactSubmitListener(new ContactAddDialog.OnContactSubmitListener() {
                            @Override
                            public void onContactSubmit(Contact contact) {
                                try {
                                    //finish dialog
                                    removeDialog(IDD_CONTACT_POPUP);

                                    //refresh form
                                    fillContacts();
                                } catch (Exception ex) {
                                    MessageBox.show(ClientForm.this, getResources().getString(R.string.form_title_contacts), getResources().getString(R.string.addr_contact_exceptionOnEdit));
                                    ErrorHandler.CatchError("Exception in ContactsForm.saveContact.onClick", ex);
                                }
                            }
                        });

                        contactDialog.setCancelClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeDialog(IDD_CONTACT_POPUP);
                            }
                        });


                        return dlg;
                    } catch (Exception ex) {
                        MessageBox.show(ClientForm.this, getResources().getString(R.string.form_title_contacts), getResources().getString(R.string.addr_contact_exceptionOnEdit));
                        ErrorHandler.CatchError("Exception in ContactsForm.onCreateDialog", ex);
                    }
                    return null;

                case IDD_ADDR_TYPE_POPUP: {
                    // редактирование аттрибутов адреса (офис, точка доставки, точка взятия заказа, торговая точка и т.п., а также их комбинации)
                    DialogInterface.OnClickListener cancelClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeDialog(IDD_ADDR_TYPE_POPUP);
                        }
                    };

                    final String attrIDtoken = "AttrID=";

                    OnProcessQuestionnaire processQuestionnaireListener = new OnProcessQuestionnaire() {
                        @Override
                        public void onQuestionnaireLoaded(Questionnaire questionnaire) {
                            //
                            // Читаем массив актуальных аттрибутов адреса из таблицы
                            //
                            String sql = String.format(" SELECT aa.AttrID " +
                                    " FROM AddressAttributes aa " +
                                    " WHERE aa.AddrID = %d AND aa.State!='%s' ",
                                    AntContext.getInstance().getAddress().addrID, Q.RECORD_STATE_DELETED);

                            Collection<Object> attrIds = Db.getInstance().selectColumnValues(sql, Db.DataType.Long);

                            //
                            // Включаем соответствующие аттрибуты в опроснике - итерируем по вопросам и расставляем начальные значения
                            //
                            if (attrIds != null) {
                                Collection<Question> questions = questionnaire.getQuestions().values();
                                for (Question question : questions) {
                                    if (question.getSubQuestions() != null) {
                                        Collection<Question> subQuestions = question.getSubQuestions().values();
                                        for (Question subQuestion : subQuestions) {
                                            String questCode = subQuestion.getCode();

                                            if (questCode.startsWith(attrIDtoken)) {
                                                String attrStr = questCode.substring(attrIDtoken.length());
                                                Long attrID = Convert.toLong(attrStr, -1L);

                                                if (attrID != -1L && attrIds.contains(attrID))
                                                    subQuestion.setValue("1");
                                            }
                                            //Log.d("Questions", "Question code: "+ subQuestion.getCode());
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onQuestionnaireConfirmed(Questionnaire questionnaire) {
                            //
                            //помечаем существующие аттрибуты адреса как удаленные
                            //
                            {
                                String sql = String.format(" UPDATE AddressAttributes SET State='%s', Sent=%d " +
                                        " WHERE AddrID=%d AND State!='%s' ",
                                        Q.RECORD_STATE_DELETED, Q.RECORD_NOT_SENT, AntContext.getInstance().getAddrID(), Q.RECORD_STATE_DELETED);
                                Db.getInstance().execSQL(sql);
                            }

                            //
                            // Проходим по результатам опросника, сохраняем включенные аттрибуты
                            //

                            Collection<Question> questions = questionnaire.getQuestions().values();
                            for (Question question : questions) {
                                if (question.getSubQuestions() != null) {
                                    Collection<Question> subQuestions = question.getSubQuestions().values();
                                    for (Question subQuestion : subQuestions) {
                                        String questCode = subQuestion.getCode();

                                        if (questCode.startsWith(attrIDtoken)) {
                                            String attrStr = questCode.substring(attrIDtoken.length());
                                            Long attrID = Convert.toLong(attrStr, -1L);

                                            if (attrID != -1L) {
                                                QuestionResult qr = subQuestion.getResult(-1L);
                                                if (qr.getResultValue() != null && qr.getResultValue().equals("1")) {
                                                    //enable attribute (insert new record to database or replace existing)
                                                    String sqlInsert = " INSERT OR REPLACE into AddressAttributes (AttrID, AddrID, State, Sent )"
                                                            + " VALUES ( ?, ?, ?, ? )";
                                                    Object[] bindArgs = new Object[]{attrID, AntContext.getInstance().getAddrID(), Q.RECORD_STATE_ACTIVE, Q.RECORD_NOT_SENT};
                                                    Db.getInstance().execSQL(sqlInsert, bindArgs);
                                                }
                                            }
                                        }
                                        //Log.d("Questions", "Question code: "+ subQuestion.getCode());
                                    }
                                }
                            }

                            removeDialog(IDD_ADDR_TYPE_POPUP);

                            //
                            // обновляем отображение аттрибутов адреса на форме
                            //
                            insertClassificationRows(classificationTable);

                        }
                    };

                    int questID = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_ADDR_ATTRS_QUESTIONNAIRE, 8);
                    QuestionairesDialog attrDialog = new QuestionairesDialog();
                    String title = "<b>" + AntContext.getInstance().getClient().nameScreen + "</b><br>" + AntContext.getInstance().getAddress().addrName;
                    //getResources().getString(R.string.edit_addr_attribs);
                    Dialog dlg = attrDialog.onCreate(this, questID, title, processQuestionnaireListener, cancelClickListener);

                    return dlg;
                }
                case IDD_ADDR_EDIT_POPUP:
                    //редактирование реквизитов адреса
                    Long addrID = AntContext.getInstance().getAddress().addrID;
                    Long clientID = AntContext.getInstance().getClient().clientID;

                    final Map<String, String> queryParams = new HashMap<String, String>();
                    queryParams.put("AddrID", addrID.toString());
                    queryParams.put("ClientID", clientID.toString());

                    DialogInterface.OnClickListener cancelClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeDialog(IDD_ADDR_EDIT_POPUP);
                        }
                    };

                    OnProcessQuestionnaire processQuestionnaireListener = new OnProcessQuestionnaire() {
                        @Override
                        public void onQuestionnaireLoaded(Questionnaire questionnaire) {
                            //
                            // Включаем соответствующие аттрибуты в опроснике - итерируем по вопросам и расставляем начальные значения
                            //
                            /*Address address = AntContext.getInstance().getAddress();

                                   Collection<Question> questions = questionnaire.getQuestions().values();
                                   for(Question question:questions)
                                   {
                                       if(question.getCode().equals("AddrName"))
                                       {
                                           question.setValue(address.addrName);
                                       }
                                       else if(question.getCode().equals("DeliveryDays"))
                                       {
                                           question.setValue(address.deliveryDays);
                                       }
                                   }*/

                            questionnaire.readValuesFromDb(queryParams);
                        }

                        @Override
                        public void onQuestionnaireConfirmed(Questionnaire questionnaire) {
                            //
                            // Проходим по результатам опросника, сохраняем включенные аттрибуты
                            //
                            questionnaire.saveValuesToDb(queryParams);

                            /*Collection<Question> questions = questionnaire.getQuestions().values();
                                   for(Question question:questions)
                                   {
                                       QuestionResult qr = question.getResult(-1L);
                                       if(qr.getResultValue()!=null)
                                       {
                                           if(question.getCode().equals("AddrName"))
                                           {
                                               address.addrName = qr.getResultValue();
                                           }
                                           else if(question.getCode().equals("DeliveryDays"))
                                           {
                                               address.deliveryDays = qr.getResultValue();
                                           }
                                       }
                                   }*/

                            removeDialog(IDD_ADDR_TYPE_POPUP);

                            //
                            // обновляем форму
                            //
                            AntContext.getInstance().getAddress().refreshProps();
                            AntContext.getInstance().getClient().refreshProps();
                            fill();

                        }
                    };

                    int questID = Settings.getInstance().getIntPropertyFromSettings(Settings.PROPNAME_ADDR_PROPS_QUESTIONNAIRE, 7);

                    String title = "<b>" + AntContext.getInstance().getClient().nameScreen + "</b>";
                    QuestionairesDialog attrDialog = new QuestionairesDialog();
                    Dialog dlg = attrDialog.onCreate(this, questID, title, processQuestionnaireListener, cancelClickListener);

                    return dlg;

                default:
                    return null;
            }
        } catch (Exception ex) {
            ErrorHandler.CatchError("Exception in contactsForm.onCreateDialog", ex);
        }

        return null;
    }

    //--------------------------------------------------------------

    /**
     * вывод диалогового окна для подтверждения пользователем удаления записи контактных данных
     *
     * @param contactID - id контакта
     */
    private void askDeleteContact(final Long contactID) {
        //
        //Display alert dialog proposing to confirm delete contact
        //
        MessageBoxButton[] buttons = new MessageBoxButton[]
                {
                        new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.contact_delete_yes),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteContact(contactID);
                                    }
                                }),
                        new MessageBoxButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.contact_delete_no),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                };

        MessageBox.show(this, getResources().getString(R.string.contact_delete_confirm_title),
                getResources().getString(R.string.contact_delete_confirm), buttons);

    }
    //--------------------------------------------------------------

    /**
     * удаление контактных данных из таблицы
     *
     * @param contactID - id контакта
     */
    private void deleteContact(Long contactID) {
        String sql = String.format("UPDATE Contacts SET State='%s', Sent=0 WHERE ContactID=%d AND AddrID=%d",
                Document.DOC_STATE_DELETED, contactID, AntContext.getInstance().getAddrID());
        Db.getInstance().execSQL(sql);
        fillContacts();
    }

    //--------------------------------------------------------------
    @Override
    public void onBackPressed() {
        try {
            AntContext.getInstance().getTabController().onBackPressed(this);
        } catch (Exception ex) {
            ErrorHandler.CatchError("Exception in clientForm.onBackPressed", ex);
        }
    }

    @Override
    public void onClick(View view) {

    }
}
