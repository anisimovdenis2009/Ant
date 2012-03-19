package com.app.ant.app.AddressBook.gui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.AddressBook.util.ApplicationUtil;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 22.12.11
 * Time: 9:34
 * To change this template use File | Settings | File Templates.
 */
public class InfoActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);
        TextView v = (TextView) findViewById(R.id.info_text_version);
        v.setText("Версия " + ApplicationUtil.getVersionName(this));
    }
}