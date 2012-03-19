package com.app.ant.app.AddressBook.gui.report;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.components.Message;
import com.app.ant.app.AddressBook.util.*;
import org.jdom.Element;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 01.12.11
 * Time: 14:38
 * To change this template use File | Settings | File Templates.
 */
public class ReportActivity extends ListActivity {
    public static final String EXTRA = ReportActivity.class.getName() + "UPDATE";
    private File[] realMessages;
    private File[] bufferMessages;
    private BaseAdapter adapter;
    private boolean isReaded = false;
    private boolean extra = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extra = getIntent().hasExtra(EXTRA);
        if (!extra) {
            refreshAndSet();
        } else {
            realMessages = new File(Common.ALIDI_MESSAGES_PATH).listFiles();
            initAdapter();
            setListAdapter(adapter);
        }

    }

    private void refreshAndSet() {
        startWaiting();
        final Monitor serviceStartupMonitor = new Monitor().once();
        loadReports(serviceStartupMonitor);
        new Thread(new Runnable() {
            @Override
            public void run() {
                serviceStartupMonitor.doWait();
                realMessages = new File(Common.ALIDI_MESSAGES_PATH).listFiles();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initAdapter();
                        ReportActivity.this.setListAdapter(adapter);
                    }
                });
            }
        }).start();
    }

    private void initAdapter() {
        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return realMessages.length;
            }

            @Override
            public Object getItem(int position) {
                return realMessages[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                final TextView textView = new TextView(ReportActivity.this);
                textView.setBackgroundColor(Color.WHITE);
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(20);
                textView.setHeight(40);
                final int q = position;
                final File file = realMessages[q];
                if (!file.isDirectory())
                    new Thread(new Runnable() {
                        public void run() {
                            Element rootElement = DOMUtil.getRootElement(file.getAbsolutePath());
                            if (rootElement != null) {
                                final String test = rootElement.getChild("Text").getText();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView.setText(test);
                                    }
                                });
                            }
                        }
                    }).start();
                return textView;
            }
        };
    }


    private void loadReports(final Monitor serviceStartupMonitor) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (IOUtil.getConnectionState(ReportActivity.this)) {
                    String filename = Common.ALIDI_MESSAGES_BUFFER_PATH;
                    IOUtil.checkAndCreatePath(filename);
                    IOUtil.loadFromFTP(filename);
                    IOUtil.convert();
                    cancelWaiting();
                    serviceStartupMonitor.doNotify();
                } else {
                    Message.hintInLooper("No internet connection", ReportActivity.this);
                    IOUtil.convert();
                    cancelWaiting();
                    serviceStartupMonitor.doNotify();
                }
            }
        }).start();

    }


    private static final Object WAIT_SYNCHRONIZER = new Object();

    private void checkAndResumeWaiting() {
        synchronized (WAIT_SYNCHRONIZER) {
            if (isWaitStillNeeded) {
                showWaitDialog();
            }
        }
    }

    private Dialog waitDialog;
    public static boolean isWaitStillNeeded = false;

    public void startWaiting() {
        runOnUiThread(
                new Runnable() {
                    public void run() {
                        synchronized (WAIT_SYNCHRONIZER) {
                            Log.v(Common.TAG, "WAIT_DIALOG starting  ");
                            isWaitStillNeeded = true;
                            showWaitDialog();
                            Log.v(Common.TAG, "WAIT_DIALOG started ");
                        }
                    }
                }
        );
    }

    private void showWaitDialog() {
        if (waitDialog != null) {
            if (waitDialog.isShowing()) return;
            dissmissWaitDialog();
        }
        waitDialog = GUIUtil.getWaitDialog(this);
        waitDialog.show();
        Log.v(Common.TAG, "WAIT_DIALOG shown");
        if (!isWaitStillNeeded)
            dissmissWaitDialog();
    }

    public void cancelWaiting() {
        runOnUiThread(
                new Runnable() {
                    public void run() {
                        synchronized (WAIT_SYNCHRONIZER) {
                            isWaitStillNeeded = false;
                            dissmissWaitDialog();

                        }
                    }
                }
        );
    }

    private void hideWaitDialogOnPause() {
        synchronized (WAIT_SYNCHRONIZER) {
            dissmissWaitDialog();
        }
    }

    private void dissmissWaitDialog() {
        if (waitDialog != null) {
            Dialog killer = waitDialog;
            waitDialog = null;
            if (killer.isShowing())
                killer.dismiss();

        }
    }

    @Override
    protected void onListItemClick(ListView l, final View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);
        String text = (String) ((TextView) v).getText();
        Message.confirmationYesNo(this, "Переместит сообщение в прочетённые?", text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File realMessage = realMessages[position];
                File dst = new File(Common.ALIDI_MESSAGES_SENDED_PATH + "/" + realMessage.getName());
                try {
                    FileUtil.copy(realMessage, dst, true, true);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                FileUtil.removeFile(realMessage.getAbsolutePath());
                v.setBackgroundColor(Color.DKGRAY);
                v.setEnabled(false);
            }
        }, true).show();


    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(0, 0, 0, "Прочтённые").setIcon(R.drawable.ic_menu_archive);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0: {
                Intent intent = new Intent(this, ReadedActivity.class);
                startActivity(intent);
            }
            break;
        }
        return true;
    }
}