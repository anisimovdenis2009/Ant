package com.app.ant.app.StoreCheck;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.Activities.AntActivity;
import com.app.ant.app.AddressBook.util.GUIFactory;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.StepController;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 14.03.12
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */
public class StoreCheckController extends AntActivity {
    Context context;
    StoreCheckModel m;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.app.ant.R.layout.store_check);
        this.context = this;
        initStepBar();
        ListView listView = (ListView) findViewById(R.id.store_check_doc_list_view);
        m = new StoreCheckModel();
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return m.result.size();
            }

            @Override
            public Object getItem(int i) {
                return m.result.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                StoreCheckDocument item = (StoreCheckDocument) getItem(i);
                TextView textView = GUIFactory.textView(context, item.showDate + " " + item.code);
                textView.setTextSize(18);
                return textView;
            }
        });

    }

    //--------------------------------------------------------------
    private void initStepBar() {
        //init steps
        ViewGroup stepButtonPlacement = (ViewGroup) findViewById(com.app.ant.R.id.stepButtonPlacement);
        AntContext.getInstance().getStepController().CreateButtons(this, stepButtonPlacement, StepController.StepPanelType.HORIZONTAL);

        //init tabs
        ViewGroup tabsPlacement = (ViewGroup) findViewById(com.app.ant.R.id.tabsPlacement);
        AntContext.getInstance().getTabController().createTabs(this, tabsPlacement);
    }

    @Override
    public void onBackPressed() {
        try {
            AntContext.getInstance().getTabController().onBackPressed(this);
        } catch (Exception ex) {
            ErrorHandler.CatchError("Exception in clientForm.onBackPressed", ex);
        }
    }
}
