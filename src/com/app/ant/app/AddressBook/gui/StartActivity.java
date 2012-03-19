package com.app.ant.app.AddressBook.gui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import com.app.ant.app.AddressBook.BaseActivity;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.clientcard.ClientCardController;
import com.app.ant.app.AddressBook.gui.clientcard.kladraddress.KladrActivity;
import com.app.ant.app.AddressBook.gui.components.MenuConfigurationModel;
import com.app.ant.app.AddressBook.gui.components.Message;
import com.app.ant.app.AddressBook.gui.creditask.CreditAskController;
import com.app.ant.app.AddressBook.gui.report.ReportActivity;
import com.app.ant.app.AddressBook.gui.settings.SettingsController;
import com.app.ant.app.AddressBook.gui.viewcards.OpenFileActivity;
import com.app.ant.app.AddressBook.options.OptionsUtil;
import com.app.ant.app.AddressBook.util.IOUtil;
import com.app.ant.app.AddressBook.util.Monitor;
import com.app.ant.app.AddressBook.xmlfeatures.JDEOptions;

import java.io.File;
import java.util.UUID;


/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 26.09.11
 * Time: 12:47
 * To change this template use File | settings | File Templates.
 */
public class StartActivity extends BaseActivity implements View.OnClickListener {
    StartActivityView v;
    public static final int MENU_SETTINGS = 0;
    public static final int MENU_UPDATE_DATABASE = MENU_SETTINGS + 1;
    public static final int MENU_REPLICATION = MENU_UPDATE_DATABASE + 1;
    public static final int MENU_INFO = MENU_REPLICATION + 1;
    public static final int MENU_UPDATE = MENU_INFO + 1;

    public static final String СС = "Карточка клиента";
    public static final String CZ = "Кредитная заявка";
    public static final String INK = "Инкассация";
    public static final String[] items = {СС, CZ, INK};
    private boolean isOptionsLoaded = false;
    private static String id;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        check();
        final Monitor serviceStartupMonitor = new Monitor().once();
        new Thread(new Runnable() {
            @Override
            public void run() {
                startWaiting();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean b;
                        if (!checkMyXML()) {
                            {
                               b = IOUtil.loadFromFTP(Common.ALIDI_GUI_PATH, Common.AK_EXCHANGE_ANDROID, false);
                               b = IOUtil.loadFromFTP(Common.DELIVERY_PATH, Common.AK_EXCHANGE_ANDROID_DELIVERY, false);
                            }
                        }
                        if (!new File(Common.KK).exists())
                            if (IOUtil.getConnectionState(StartActivity.this))
                                b = IOUtil.loadFromFTP(new File(Common.KK), Common.AK_EXCHANGE_ANDROID_KK);
                        String filename = Common.ALIDI_MESSAGES_BUFFER_PATH;
                        IOUtil.checkAndCreatePath(filename);
                        if (IOUtil.getConnectionState(StartActivity.this))
                            IOUtil.loadFromFTP(filename);
                        IOUtil.convert();
                        final int length = new File(Common.ALIDI_MESSAGES_PATH).listFiles().length - 1;
                        if (length > 0)
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Message.confirmationYesNo(StartActivity.this, "У вас есть непрочитанные сообщения!! " + length + " штук!", "Хотите прочитать?", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(StartActivity.this, ReportActivity.class);
                                            intent.putExtra(ReportActivity.EXTRA, true);
                                            startActivity(intent);
                                        }
                                    }, true).show();
                                }
                            });
                        serviceStartupMonitor.doNotify();
                    }
                }).start();
                serviceStartupMonitor.doWait();


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        id = getDeviceID();
                        if (!isOptionsLoaded) {
                            OptionsUtil.load();
                            isOptionsLoaded = true;
                        }
                        v = new StartActivityView(StartActivity.this);
                        setContentView(v);
                        cancelWaiting();
                        initActions();
                        JDEOptions.getInstance(StartActivity.this);
                    }
                });
            }
        }).start();

    }

    private void downloadGUI(final Monitor serviceStartupMonitor) {

    }


    private boolean checkMyXML() {
        File kk = new File(Common.KK);
        File a = new File(Common.ADD_CONTACT);
        File l = new File(Common.LAW_ADDRESS_OPTIONS);
        File m = new File(Common.MARKET);
        File k = new File(Common.KIOSK);
        File s = new File(Common.SHOP);
        if (kk.exists() && a.exists() && l.exists() && m.exists() && k.exists() && s.exists())
            return true;
        else
            return false;
    }

    private void check() {
        IOUtil.checkAndCreatePath(Common.ALIDI_DATA_PATH);
        IOUtil.checkAndCreatePath(Common.ALIDI_GUI_PATH);
        IOUtil.checkAndCreatePath(Common.DELIVERY_PATH);
        IOUtil.checkAndCreatePath(Common.ALIDI_OUTBOUND_PATH);
        IOUtil.checkAndCreatePath(Common.ALIDI_INBOUND_PATH);
        IOUtil.checkAndCreatePath(Common.ALIDI_MESSAGES_PATH);
        IOUtil.checkAndCreatePath(Common.ALIDI_MESSAGES_BUFFER_PATH);
        IOUtil.checkAndCreatePath(Common.ALIDI_MESSAGES_SENDED_PATH);
        IOUtil.checkAndCreatePath(Common.CLIENT_CARD_OUTBOUND_PATH);
        IOUtil.checkAndCreatePath(Common.CREDIT_ASK_OUTBOUND_PATH);
    }

    private String getDeviceID() {
        final String android_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();

        UUID deviceUuid = new UUID(android_id.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid.toString();
    }

    private void initActions() {
        v.getClientCard().setOnClickListener(this);
        v.getCreditAsk().setOnClickListener(this);
        v.getInkassat().setOnClickListener(this);
        v.getRequestReview().setOnClickListener(this);
/*        v.getMobileSystem().setOnClickListener(this);
        v.getQuit().setOnClickListener(this);
        v.getRefreshDatabaze().setOnClickListener(this);
        v.getReplikat().setOnClickListener(this);

        v.getSearch().setOnClickListener(this);
        v.getSettings().setOnClickListener(this);
        v.getSinchronizationOptimum().setOnClickListener(this);*/
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu = MenuConfigurationModel.createMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SETTINGS: {
                Intent intent = new Intent(this, SettingsController.class);
                startActivity(intent);
            }
            break;
            case MENU_UPDATE_DATABASE:
                Intent intent = new Intent(this, KladrActivity.class);
                startActivity(intent);
                break;
            case MENU_REPLICATION:
                Dialog clickMenu = Message.createContextMenu(this, "", items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String extra = null;
                        switch (which) {
                            case 0:
                                extra = Common.CLIENT_CARD_OUTBOUND_PATH;
                                break;
                            case 1:
                                extra = Common.CREDIT_ASK_OUTBOUND_PATH;
                                break;
                            case 2:
                                extra = Common.CLIENT_CARD_OUTBOUND_PATH;
                                break;
                        }
                        replication(extra);
                    }
                });
                clickMenu.show();
                break;
            case MENU_INFO:
                startActivity(new Intent(this,InfoActivity.class));
                break;
            case MENU_UPDATE:
                update();
                break;
        }
        return true;
    }

    private void update() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startWaiting();
                    }
                });
                boolean b = IOUtil.loadFromFTP(new File(Common.ALIDI_UPDATE), Common.AK_EXCHANGE_UPDATE);
                if (b) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cancelWaiting();
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri uri = Uri.parse("file:///mnt/sdcard/Alidi" + Common.ADRESS_BOOK_APK);
                            intent.setDataAndType(uri, "application/vnd.android.package-archive");
                            startActivity(intent);
                        }
                    });
                }
            }
        }).start();
    }

    private void replication(String extra) {
        File out = new File(extra);
        final File[] list = out.listFiles();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (File a : list) {
                    String s1 = null;
                    if (!IOUtil.sendToFTP(a, 0))
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Message.error(StartActivity.this, "Файл не оправлен").show();
                            }
                        });

                }
            }
        }).start();

    }


    public void onClick(View view) {
        if (view == v.getClientCard()) {
            startActivity(new Intent(this, ClientCardController.class));
        } else if (view == v.getCreditAsk()) {
            startActivity(new Intent(this, CreditAskController.class));
        } else if (view == v.getInkassat()) {
            startActivity(new Intent(this, ReportActivity.class));
        } else if (view == v.getRequestReview()) {

            Dialog clickMenu = Message.createContextMenu(this, "", items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(StartActivity.this, OpenFileActivity.class);
                    intent.putExtra(OpenFileActivity.EXTRA, which);
                    startActivity(intent);
                }
            });
            clickMenu.show();

        }

    }

    public static String getId() {
        return id;
    }
}