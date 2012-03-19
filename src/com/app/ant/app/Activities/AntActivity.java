package com.app.ant.app.Activities;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 23.01.12
 * Time: 14:31
 * To change this template use File | Settings | File Templates.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import com.app.ant.R;
import com.app.ant.app.Activities.MessageBox.MessageBoxButton;
import com.app.ant.app.AddressBook.BaseActivity;
import com.app.ant.app.AddressBook.gui.StartActivity;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.ServiceLayer.*;

import java.util.Locale;

/**
 * Базовый класс для экранных форм
 */
public class AntActivity extends BaseActivity implements Synchronizer.ISynchronizationResult {
    private static final int IDM_SYNCHRONIZATION = Menu.FIRST;
    private static final int IDM_DAY_SUMMARIES = Menu.FIRST + 1;
    private static final int IDM_DEBTORS = Menu.FIRST + 2;
    private static final int IDM_MESSAGES = Menu.FIRST + 3;
    private static final int IDM_BACKUP_DB = Menu.FIRST + 4;
    private static final int IDM_DAY_PLANS = Menu.FIRST + 5;
    private static final int IDM_PREFERENCES = Menu.FIRST + 6;
    private static final int IDM_FEEDBACK = Menu.FIRST + 7;
    private static final int IDM_ABOUT = Menu.FIRST + 8;
    private static final int IDM_CASH_REGISTER = Menu.FIRST + 9;
    private static final int IDM_PRINTER = Menu.FIRST + 10;
    private static final int IDM_TEST = Menu.FIRST + 12;
    private static final int IDM_ITEM_LIST = Menu.FIRST + 13;
    private static final int IDM_ITEM_ADDRESS_BOOK = IDM_ITEM_LIST + 1;
    //public static final String DEN_SYNC_CONSTANT = "http://nnsrv30.alidi.ru:8080/anthillservice";
    public static final String DEN_SYNC_CONSTANT = "http://81.18.138.11:45535/anthillservice";

    private Locale locale;

    //synchronization dialog fields
    private static int last_sync_server_position = 0;
    private static final int CUSTOM_SERVER_INDEX = 2;

    private boolean bHaveChanges = false;
    private Integer sendType = Synchronizer.SYNC_TYPE_NONE;
    private Integer recieveType = Synchronizer.SYNC_TYPE_NONE;

    private OnClickListener syncTypeOnClickListener = null;
    private DialogInterface.OnDismissListener dismissListenerExternal = null;

    /**
     * Инициализация формы. Устанавливает локаль в зависимости от выбора в установках
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            AntContext.getInstance().setContext(this); //global accessible app context

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String lang = preferences.getString(getResources().getString(R.string.preference_key_lang), "ru");

            locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, null);

            sendType = Synchronizer.SYNC_TYPE_NONE;
            recieveType = Synchronizer.SYNC_TYPE_NONE;
        } catch (Exception ex) {
            ErrorHandler.CatchError(this.toString() + "onCreate", ex);
        }
    }

    /**
     * Переинициализация формы при изменении конфигурации устройства (поворот экрана)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);
    }

    //--------------------------------------------------------------

    /**
     * Создает контекстное меню формы
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem item = menu.add(0, IDM_SYNCHRONIZATION, IDM_SYNCHRONIZATION, R.string.menu_synchronization);
        item.setIcon(R.drawable.menu_synchronization);
        item = menu.add(0, IDM_PREFERENCES, IDM_PREFERENCES, R.string.menu_preferences);
        item.setIcon(R.drawable.menu_settings);
        item = menu.add(0, IDM_DAY_SUMMARIES, IDM_DAY_SUMMARIES, R.string.menu_day_summaries);
        item.setIcon(R.drawable.menu_plans);
        item = menu.add(0, IDM_DEBTORS, IDM_DEBTORS, R.string.menu_debtors);
        item.setIcon(R.drawable.menu_saldo);
        item = menu.add(0, IDM_BACKUP_DB, IDM_BACKUP_DB, R.string.menu_backup_db);
        item.setIcon(R.drawable.menu_backup);
        //item = menu.add(0, IDM_DAY_PLANS, IDM_DAY_PLANS, R.string.menu_day_plans);
        //item.setIcon(R.drawable.menu_plans);
        item = menu.add(0, IDM_MESSAGES, IDM_MESSAGES, R.string.menu_messages);
        item.setIcon(R.drawable.menu_messages);
        item = menu.add(0, IDM_FEEDBACK, IDM_FEEDBACK, R.string.menu_feedback);
        item = menu.add(0, IDM_ABOUT, IDM_ABOUT, R.string.menu_about);
        item = menu.add(0, IDM_CASH_REGISTER, IDM_CASH_REGISTER, R.string.menu_cash_register);
        item = menu.add(0, IDM_PRINTER, IDM_PRINTER, R.string.printer_select);
        //item = menu.add(0, IDM_PRINTER1, IDM_PRINTER1, R.string.printer_select);
        //item = menu.add(0, IDM_TEST, IDM_TEST, "Анкетирование"); //TODO resources
        item = menu.add(0, IDM_ITEM_LIST, IDM_ITEM_LIST, R.string.menu_items);
        item = menu.add(0, IDM_ITEM_ADDRESS_BOOK, IDM_ITEM_ADDRESS_BOOK, R.string.menu_address_book);
        item.setIcon(R.drawable.menu_items);

        menu.getItem(0).setEnabled((this instanceof ClientListForm)||(this instanceof LoginForm));

        return true;
    }

    //--------------------------------------------------------------

    /**
     * Обработка нажатий на кнопки в меню
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case IDM_SYNCHRONIZATION:
                CreateSynchronizationDialog(Synchronizer.SYNC_TYPE_NONE, false);
                return true;
            case IDM_PREFERENCES:
                startActivity(new Intent(this, PreferencesForm.class));
                return true;
            case IDM_DAY_SUMMARIES:
                /*int orientation = getWindowManager().getDefaultDisplay().getOrientation();

                    if( orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270) //landscape
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    else
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);*/

                startActivity(new Intent(this, ReportDaySummariesForm.class));
                return true;
            case IDM_ITEM_LIST:
                startActivity(new Intent(this, ItemListForm.class));
                return true;
            case IDM_ITEM_ADDRESS_BOOK:
                startActivity(new Intent(this, StartActivity.class));
                return true;
            case IDM_BACKUP_DB:
                try {
                    Synchronizer.backupDataBase();
                } catch (Exception ex) {
                    ErrorHandler.CatchError("Exception in AndActivity::onOptionsItemSelected", ex);
                }
                return true;
            case IDM_DAY_PLANS:
                startActivity(new Intent(this, PlansForm.class));
                return true;
            case IDM_MESSAGES:
                startActivity(new Intent(this, MessageProactiveForm.class));
                return true;
            case IDM_FEEDBACK:
                startActivity(new Intent(this, FeedbackForm.class));
                return true;
            case IDM_CASH_REGISTER:
                startActivity(new Intent(this, CashRegisterForm.class));
                return true;
            case IDM_ABOUT: {
                startActivity(new Intent(this, AboutForm.class));
                return true;
            }
            case IDM_PRINTER:
                Printer.selectPrinter(this);
                return true;
            case IDM_TEST:
                startActivity(new Intent(this, QuestionnairesForm.class));
                return true;

            /*
               case IDM_PRINTER1:
                   //String document = new PrintInvoice(PrintableDocumentType.Bill, 0.2).getPrintableDocument(this, -730815);

                   String document0 = new PrintInvoice(PrintableDocumentType.Bill, 0.2).getPrintableDocument(this, -730817);
                   String document1 = new PrintInvoice(PrintableDocumentType.Bill, 0).getPrintableDocument(this, -730817);
                   String document2 = new PrintInvoice(PrintableDocumentType.Bill, 0.2).getPrintableDocument(this, -730816);
                   String document3 = new PrintInvoice(PrintableDocumentType.Bill, 0).getPrintableDocument(this, -730816);

                   String money1 = Convert.moneyToStringInWords(getResources(), 100.0);
                   String money2 = Convert.moneyToStringInWords(getResources(), 101.50);
                   String money3 = Convert.moneyToStringInWords(getResources(), 20010100.15);
                   String money4 = Convert.moneyToStringInWords(getResources(), 15125154.01);

                   //Printer printer = new Printer(this);
                   //printer.printInvoice(this, PrintableDocumentType.Bill, -730815, 0.2);

                   PrintDocForm printForm = new PrintDocForm();
                   Dialog dialog = printForm.onCreate(this, -730817);
                   dialog.show();

                   return true;*/

            case IDM_DEBTORS:
                startActivity(new Intent(this, DebtorListForm.class));
                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Создание диалога управления синхронизацией
     *
     * @param syncType  флаги полноты приема/передачи данных (полная или частичная закачка)
     * @param hideOther прятать дополнительные элементы управления
     */
    public void CreateSynchronizationDialog(Integer syncType, boolean hideOther) {
        CreateSynchronizationDialog(syncType, hideOther, null, null, null);
    }

    /**
     * Создание диалога управления синхронизацией
     *
     * @param syncType        флаги полноты приема/передачи данных (полная или частичная закачка)
     * @param hideOther       прятать дополнительные элементы управления
     * @param dismissListener обработчик, вызывается при отмене выполнения диалога
     * @param title           заголовок диалога
     * @param message         дополнительное сообщение пользователю
     */
    public void CreateSynchronizationDialog(Integer sync_type, boolean hideOther, DialogInterface.OnDismissListener dismissListener, String title, String message) {
        this.dismissListenerExternal = dismissListener;

        // create dialog
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.synchronization_form, (ViewGroup) findViewById(R.id.synchronizationForm));

        final TextView lblTextViewSend = (TextView) layout.findViewById(R.id.sync_lbl_send_data);
        final TextView lblTextViewRecieve = (TextView) layout.findViewById(R.id.sync_lbl_recieve_data);
        //final TextView lblTextViewRecieveMedia = (TextView) layout.findViewById(R.id.sync_lbl_recieve_data_media);

        final RadioButton checkIsFullSend = (RadioButton) layout.findViewById(R.id.sync_send_full_option);
        final RadioButton checkIsIncrSend = (RadioButton) layout.findViewById(R.id.sync_send_incr_option);
        final RadioButton checkIsMediaSend = (RadioButton) layout.findViewById(R.id.sync_send_media_files);

        checkIsFullSend.setChecked((sync_type & Synchronizer.SYNC_TYPE_FULL_SEND) > 0);
        checkIsIncrSend.setChecked((sync_type & Synchronizer.SYNC_TYPE_INCR_SEND) > 0);
        checkIsMediaSend.setChecked((sync_type & Synchronizer.SYNC_TYPE_MEDIA_SEND) > 0);

        checkIsMediaSend.setVisibility(Api.isWiFiEnabled() ? View.VISIBLE : View.GONE);

        final RadioButton checkIsFullRecieve = (RadioButton) layout.findViewById(R.id.sync_receive_full_option);
        final RadioButton checkIsIncrRecieve = (RadioButton) layout.findViewById(R.id.sync_receive_incr_option);
        final RadioButton checkIsSelRecieve = (RadioButton) layout.findViewById(R.id.sync_receive_select_option);

        checkIsFullRecieve.setChecked((sync_type & Synchronizer.SYNC_TYPE_FULL) > 0);
        checkIsIncrRecieve.setChecked((sync_type & Synchronizer.SYNC_TYPE_INCR) > 0);
        checkIsSelRecieve.setChecked((sync_type & Synchronizer.SYNC_TYPE_REST) > 0);

        final RadioButton checkIsMediaRecieve = (RadioButton) layout.findViewById(R.id.sync_receive_media_files);
        checkIsMediaRecieve.setChecked((sync_type & Synchronizer.SYNC_TYPE_MEDIA) > 0);

        checkIsMediaRecieve.setVisibility(Api.isWiFiEnabled() ? View.VISIBLE : View.GONE);

        final Button btnSyncUpdateVersion = (Button) layout.findViewById(R.id.sync_btn_update_version);
        final Button btnShowStatistic = (Button) layout.findViewById(R.id.sync_btn_show_statistic);

        if (Convert.isNullOrBlank(title)) {
            title = getResources().getString(R.string.form_title_synchronization);
        }

        TextView tvMessage = (TextView) layout.findViewById(R.id.sync_message);
        if (!Convert.isNullOrBlank(message)) {
            tvMessage.setText(message);
        }
        tvMessage.setVisibility(Convert.isNullOrBlank(message) ? View.GONE : View.VISIBLE);

        syncTypeOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((RadioButton) v == checkIsFullRecieve) {
                    String sql = Q.getTodayNotSentOrdersCount();
                    int countDocs = Convert.toInt(Db.getInstance().selectValue(sql), 1);
                    if (countDocs > 0)
                        MessageBox.show(AntActivity.this, getResources().getString(R.string.message_box_warning), getResources().getString(R.string.sync_documents_not_sended));
                }
            }
        };

        checkIsFullSend.setOnClickListener(syncTypeOnClickListener);
        checkIsIncrSend.setOnClickListener(syncTypeOnClickListener);

        checkIsFullRecieve.setOnClickListener(syncTypeOnClickListener);
        checkIsIncrRecieve.setOnClickListener(syncTypeOnClickListener);
        checkIsSelRecieve.setOnClickListener(syncTypeOnClickListener);

        checkIsMediaRecieve.setOnClickListener(syncTypeOnClickListener);

        if (hideOther) {
            lblTextViewSend.setVisibility((sync_type & (Synchronizer.SYNC_TYPE_INCR_SEND | Synchronizer.SYNC_TYPE_FULL_SEND)) > 0 ? View.VISIBLE : View.GONE);

            checkIsFullSend.setVisibility((sync_type & Synchronizer.SYNC_TYPE_FULL_SEND) > 0 ? View.VISIBLE : View.GONE);
            checkIsIncrSend.setVisibility((sync_type & Synchronizer.SYNC_TYPE_INCR_SEND) > 0 ? View.VISIBLE : View.GONE);
            checkIsMediaSend.setVisibility((sync_type & Synchronizer.SYNC_TYPE_MEDIA_SEND) > 0 ? View.VISIBLE : View.GONE);

            lblTextViewRecieve.setVisibility((sync_type & (Synchronizer.SYNC_TYPE_FULL | Synchronizer.SYNC_TYPE_INCR | Synchronizer.SYNC_TYPE_REST)) > 0 ? View.VISIBLE : View.GONE);

            checkIsFullRecieve.setVisibility((sync_type & Synchronizer.SYNC_TYPE_FULL) > 0 ? View.VISIBLE : View.GONE);
            checkIsIncrRecieve.setVisibility((sync_type & Synchronizer.SYNC_TYPE_INCR) > 0 ? View.VISIBLE : View.GONE);
            checkIsSelRecieve.setVisibility((sync_type & Synchronizer.SYNC_TYPE_REST) > 0 ? View.VISIBLE : View.GONE);

            btnSyncUpdateVersion.setVisibility((sync_type & Synchronizer.SYNC_TYPE_UPDATE) > 0 ? View.VISIBLE : View.GONE);

            //lblTextViewRecieveMedia.setVisibility((sync_type & Synchronizer.SYNC_TYPE_MEDIA) > 0 ? View.VISIBLE : View.GONE);
            checkIsMediaRecieve.setVisibility((sync_type & Synchronizer.SYNC_TYPE_MEDIA) > 0 ? View.VISIBLE : View.GONE);

            if (sync_type == Synchronizer.SYNC_TYPE_REST) {
                lblTextViewRecieve.setVisibility(View.GONE);
                checkIsSelRecieve.setVisibility(View.GONE);
            }
        }

        final TextView tvDeviceId = (TextView) layout.findViewById(R.id.sync_tv_device_id);
        tvDeviceId.setText(getResources().getString(R.string.sync_device_id) + AntContext.getInstance().getDeviceId());

        final TextView tvSalerName = (TextView) layout.findViewById(R.id.saler_name);
        tvSalerName.setText(Settings.getInstance().getPropertyFromSettings(Settings.PROPNAME_SALER_NAME, getResources().getString(R.string.login_sample_saler)));

        final EditText etServer = (EditText) layout.findViewById(R.id.editTextServerToSync);
        etServer.setText(Settings.getInstance().getPropertyFromSettings(Settings.PROPNAME_GPRS_ROOT_URL));

        btnSyncUpdateVersion.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String serverUrl = etServer.getText().toString();
                    Synchronizer synchronizer = new Synchronizer(AntActivity.this);
                    synchronizer.StartAsyncSyncronizeTask(Synchronizer.SYNC_TYPE_UPDATE, DEN_SYNC_CONSTANT);
                } catch (Exception ex) {
                    ErrorHandler.CatchError("Exception in AndActivity::CreateSynchronizationDialog", ex);
                    MessageBox.show(AntActivity.this, getResources().getString(R.string.message_box_error), ex.getLocalizedMessage());
                }
            }
        });

        btnShowStatistic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(AntActivity.this, PreferencesSyncForm.class));
                } catch (Exception ex) {
                    ErrorHandler.CatchError("Exception in AndActivity::btnShowStatistic::Click", ex);
                }
            }
        });

        final Spinner spnServer = (Spinner) layout.findViewById(R.id.spnServerToSync);

        final Cursor cur = Db.getInstance().selectSQL(Q.settings_getSyncServersUrl());
        try {
            String[] from = new String[]{getResources().getString(R.string.sync_server_name_col_name)};
            int[] to = new int[]{android.R.id.text1};

            SimpleCursorAdapter curAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cur, from, to);

            curAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnServer.setAdapter(curAdapter);
        } catch (Exception ex) {
            ErrorHandler.CatchError("Exception in AndActivity::onOptionsItemSelected", ex);
        }

        spnServer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                try {
                    if (!cur.isClosed()) {
                        last_sync_server_position = spnServer.getSelectedItemPosition();
                        etServer.setVisibility(last_sync_server_position == CUSTOM_SERVER_INDEX ? View.VISIBLE : View.GONE);
                        etServer.setText(cur.getString(cur.getColumnIndex(getResources().getString(R.string.sync_server_url_col_name))));
                    }
                } catch (Exception ex) {
                    ErrorHandler.CatchError("Exception in AndActivity::spnServer::onItemSelected", ex);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                etServer.setText("");
            }
        });

        spnServer.setSelection(last_sync_server_position, true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(layout);
        builder.setMessage(title);

        builder.setPositiveButton(R.string.sync_syncronize, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Synchronizer synchronizer = new Synchronizer(AntActivity.this);

                    if (Api.isOnline()) {
                        if (checkIsFullRecieve.isChecked())
                            recieveType = Synchronizer.SYNC_TYPE_FULL;
                        else if (checkIsIncrRecieve.isChecked())
                            recieveType = Synchronizer.SYNC_TYPE_INCR;
                        else if (checkIsSelRecieve.isChecked())
                            recieveType = Synchronizer.SYNC_TYPE_SALDO | Synchronizer.SYNC_TYPE_REST;

                        if (checkIsMediaRecieve.isChecked())
                            recieveType = recieveType | Synchronizer.SYNC_TYPE_MEDIA;

                        if (checkIsMediaSend.isChecked())
                            recieveType = recieveType | Synchronizer.SYNC_TYPE_MEDIA_SEND;

                        if (checkIsFullSend.isChecked())
                            sendType = Synchronizer.SYNC_TYPE_FULL_SEND;
                        else if (checkIsIncrSend.isChecked())
                            sendType = Synchronizer.SYNC_TYPE_INCR_SEND;

                        if (sendType == Synchronizer.SYNC_TYPE_NONE && recieveType == Synchronizer.SYNC_TYPE_NONE) {
                            onSynchronizationFinished(false, getResources().getString(R.string.sync_no_selection), "");
                            return;
                        }

                        String serverUrl = etServer.getText().toString();
                        cur.close();

                        synchronizer.StartAsyncSyncronizeTask((sendType | recieveType), DEN_SYNC_CONSTANT);
                        Log.v("den",serverUrl);
                    } else {
                        onSynchronizationFinished(false, getResources().getString(R.string.sync_no_network), "");
                    }
                } catch (Exception ex) {
                    ErrorHandler.CatchError("Exception in AndActivity::CreateSynchronizationDialog", ex);
                    MessageBox.show(AntActivity.this, "Error", ex.getLocalizedMessage());
                }
            }
        });

        builder.setNegativeButton(R.string.sync_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cur.close();
                dialog.dismiss();

                if (dismissListenerExternal != null)
                    dismissListenerExternal.onDismiss(dialog);
            }
        });

        final Dialog dialog = builder.create();

        //return
        dialog.setOwnerActivity(this);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });

        bHaveChanges = false;
        dialog.show();

        if (!Db.getInstance().getDbVersion().equals(Api.getVersionName())) {
            String msg = getResources().getString(R.string.sync_db_version_is_falied);
            MessageBox.show(this, "Ant database", msg);
        }
    }

    public void refreshActivity() {
    }
    //--------------------------------------------------------------
    //Synchronizer.ISynchronizationResult implementation

    /**
     * Обработчик завершения синхронизации.
     *
     * @param bHaveDbChanges флаг наличия новых данных
     * @param msg            сообщение пользователю
     * @param apkUrl         путь к исполняемому файлу новой версии приложения
     */
    public void onSynchronizationFinished(boolean bHaveDbChanges, String msg, String apkUrl) {
        sendType = Synchronizer.SYNC_TYPE_NONE;
        recieveType = Synchronizer.SYNC_TYPE_NONE;

        this.bHaveChanges = bHaveDbChanges;

        String syncMsg = bHaveChanges ? getResources().getString(R.string.sync_success) : getResources().getString(R.string.sync_falied);
        String syncCaption = getResources().getString(R.string.sync_syncronize);
        syncMsg = syncMsg + " " + msg;

        if (Convert.isNullOrBlank(apkUrl)) {
            MessageBoxButton[] buttons = new MessageBoxButton[]
                    {
                            new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //startActivity(new Intent(AntActivity.this, ClientListForm.class));
                                    //AntActivity.this.finish();
                                    refreshActivity();

                                    if (dismissListenerExternal != null)
                                        dismissListenerExternal.onDismiss(dialog);

                                }
                            })
                    };
            MessageBox.show(this, syncCaption, syncMsg, buttons);
        } else {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(apkUrl);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                startActivity(intent);
            } catch (Exception ex) {
                ErrorHandler.CatchError("Exception in AndActivity::onSynchronizationFinished", ex);
            }
        }

        if (!Db.getInstance().getDbVersion().equals(Api.getVersionName())) {
            msg = getResources().getString(R.string.sync_db_version_is_falied);
            MessageBox.show(this, "Ant database", msg);
        }
    }

}