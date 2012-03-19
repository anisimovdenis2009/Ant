package com.app.ant.app.AddressBook.gui.report;

import android.app.ListActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.util.DOMUtil;
import org.jdom.Element;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 08.12.11
 * Time: 9:39
 * To change this template use File | Settings | File Templates.
 */
public class ReadedActivity extends ListActivity {
    File[] realMessages;
    private BaseAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realMessages = new File(Common.ALIDI_MESSAGES_SENDED_PATH).listFiles();
        initAdapter();
        setListAdapter(adapter);
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
                final TextView textView = new TextView(ReadedActivity.this);
                textView.setBackgroundColor(Color.WHITE);
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(20);

                final int q = position;
                final File file = realMessages[q];
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
}