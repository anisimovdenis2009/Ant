package com.app.ant.app.AddressBook.gui.viewcards;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.clientcard.ClientCardController;
import com.app.ant.app.AddressBook.gui.clientcard.ClientCardModel;
import com.app.ant.app.AddressBook.gui.components.Message;
import com.app.ant.app.AddressBook.gui.creditask.CreditAskController;
import com.app.ant.app.AddressBook.gui.creditask.CreditAskModel;
import com.app.ant.app.AddressBook.util.FileUtil;
import com.app.ant.app.AddressBook.util.GUIFactory;
import com.app.ant.app.AddressBook.util.IOUtil;
import com.app.ant.app.AddressBook.xmlfeatures.ClientCardXMLReader;
import com.app.ant.app.AddressBook.xmlfeatures.CreditAskXMLReader;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 30.11.11
 * Time: 16:22
 * To change this template use File | Settings | File Templates.
 */
public class OpenFileActivity extends ListActivity implements View.OnClickListener, View.OnLongClickListener {

    public static final String EXTRA = OpenFileActivity.class.toString() + "EXTRA";
    private String extra = null;
    private boolean isReaded = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA)) {
            int which = intent.getIntExtra(EXTRA, 0);
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
            String[] list;
            list = new File(extra).list();
            ListAdapter adapter;
            adapter = initAdapter(list);
            setListAdapter(adapter);

        }
    }

    private ListAdapter initAdapter(final String[] list) {
        ListAdapter adapter;
        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return list.length;
            }

            @Override
            public Object getItem(int position) {
                return list[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = GUIFactory.textView(OpenFileActivity.this, (String) getItem(position));
                textView.setTextSize(25);
                textView.setBackgroundColor(Color.WHITE);
                textView.setTextColor(Color.BLACK);
                textView.setOnClickListener(OpenFileActivity.this);
                textView.setOnLongClickListener(OpenFileActivity.this);
                return textView;
            }
        };
        return adapter;
    }

    @Override
    public void onClick(View v) {
        if (getIntent().hasExtra(EXTRA)) {
            int which = getIntent().getIntExtra(EXTRA, 0);
            String basePath = null;
            switch (which) {
                case 0:
                    basePath = Common.CLIENT_CARD_OUTBOUND_PATH;
                    String path = composeFileName((TextView) v, basePath);
                    boolean b = new File(path).isDirectory();
                    if (!b)
                        startClientCard(path);
                    else
                        sended1();
                    break;
                case 1:
                    basePath = Common.CREDIT_ASK_OUTBOUND_PATH;
                    path = composeFileName((TextView) v, basePath);
                    b = new File(path).isDirectory();
                    if (!b)
                        startCreditAsk(path);
                    else
                        sended1();
                    break;
            }
        }
    }

    private String composeFileName(TextView v, String extra) {
        String path;
        if (isReaded)
            path = extra + Common.SENDED + "/" + v.getText();
        else
            path = extra + "/" + v.getText();
        return path;
    }

    @Override
    public boolean onLongClick(final View v) {
        if (getIntent().hasExtra(EXTRA)) {
            int which = getIntent().getIntExtra(EXTRA, 0);
            String extra = null;
            switch (which) {
                case 0:
                    extra = Common.CLIENT_CARD_OUTBOUND_PATH;
                    final String path = extra + "/" + ((TextView) v).getText();
                    Message.confirmationYesNo(this, "Удаление заявки", "Вы уверены, что хотите удалить заявку?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FileUtil.removeFile(path);
                            v.setEnabled(false);
                            v.setBackgroundColor(Color.BLACK);
                        }
                    }, true).show();
                    break;
                case 1:
                    extra = Common.CREDIT_ASK_OUTBOUND_PATH;
                    final String path1 = extra + "/" + ((TextView) v).getText();
                    Message.confirmationYesNo(this, "Удаление заявки", "Вы уверены, что хотите удалить заявку?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FileUtil.removeFile(path1);
                            v.setEnabled(false);
                            v.setBackgroundColor(Color.BLACK);
                        }
                    }, true).show();
                    break;
                case 2:
                    extra = Common.CLIENT_CARD_OUTBOUND_PATH;
                    break;
            }
        }
        return true;
    }

    private void startClientCard(String path) {
        ClientCardXMLReader reader = new ClientCardXMLReader(path);
        Intent intent = new Intent(this, ClientCardController.class);
        ClientCardModel m = reader.getM();
        ArrayList<Parcelable> contacts = m.getContacts();
        if (contacts != null) {
            intent.putExtra(Common.CLIENT_CARD_EXTRA_CONTACTS, contacts);
            m.setContacts(null);
        }
        intent.putExtra(Common.CLIENT_CARD_EXTRA, m);
        startActivity(intent);
    }

    private void startCreditAsk(String path) {
        CreditAskXMLReader reader = new CreditAskXMLReader(path);
        Intent intent = new Intent(this, CreditAskController.class);
        CreditAskModel m = reader.getM();
        intent.putExtra(Common.CREDIT_ASK_EXTRA, m);
        startActivity(intent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (isReaded)
            menu.add(0, 0, 0, "Новые").setIcon(R.drawable.ic_menu_day);
        else
            menu.add(0, 0, 0, "Отправленные").setIcon(R.drawable.ic_menu_archive);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0: {
                sended(item);
            }
            break;

        }
        return true;
    }

    private void sended(MenuItem item) {
        ListAdapter adapter;
        String[] list = null;
        if (isReaded) {
            File file = new File(extra);
            IOUtil.checkAndCreatePath(file);
            list = file.list();
            isReaded = false;
            item.setTitle("Отправленные");
            item.setIcon(R.drawable.ic_menu_archive);

        } else {
            File file = new File(extra + Common.SENDED);
            IOUtil.checkAndCreatePath(file);
            list = file.list();
            isReaded = true;
            item.setTitle("Новые");
            item.setIcon(R.drawable.ic_menu_day);
        }
        if (list != null) {

            adapter = initAdapter(list);
            setListAdapter(adapter);
        }
    }

    private void sended1() {
        ListAdapter adapter;
        String[] list = null;
        if (isReaded) {
            File file = new File(extra);
            IOUtil.checkAndCreatePath(file);
            list = file.list();
            isReaded = false;
        } else {
            File file = new File(extra + Common.SENDED);
            IOUtil.checkAndCreatePath(file);
            list = file.list();
            isReaded = true;
        }
        if (list != null) {
            adapter = initAdapter(list);
            setListAdapter(adapter);
        }
    }


}