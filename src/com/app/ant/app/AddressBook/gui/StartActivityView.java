package com.app.ant.app.AddressBook.gui;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.app.ant.R;


/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 27.09.11
 * Time: 11:54
 * To change this template use File | settings | File Templates.
 */
public class StartActivityView extends LinearLayout{
    private Context context;

    private ImageButton clientCard;
    private ImageButton creditAsk;
    private ImageButton inkassat;
    private ImageButton requestReview;
    private Button replikat;
    private Button settings;
    private Button mobileSystem;
    private Button refreshDatabaze;
    private Button sinchronizationOptimum;
    private Button search;
    private Button quit;

    public StartActivityView(Context context) {
        super(context);
        this.context = context;
                LayoutInflater inflater;
        inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.start_activity, this);
        createComponents();
    }

    private void createComponents() {
        clientCard = (ImageButton) findViewById(R.id.start_view_client_card);
        creditAsk = (ImageButton) findViewById(R.id.start_view_credit_ask);
        inkassat = (ImageButton) findViewById(R.id.start_view_inkassat);
        requestReview = (ImageButton) findViewById(R.id.start_view_request_review);
    }
        //replikat = (Button) findViewById(R.id.start_view_);
/*
        settings = (Button) findViewById(R.id.start_view_settings);
        mobileSystem = (Button) findViewById(R.id.start_view_mobile_system);
        refreshDatabaze = (Button) findViewById(R.id.start_view_database_refresh);
        sinchronizationOptimum = (Button) findViewById(R.id.start_view_synchronization);
        search = (Button) findViewById(R.id.start_view_search);*//*

        //quit = (Button) findViewById(R.id.start_view_quit);

    }


/*    public Button getQuit() {
        return quit;
    }*/

    public ImageButton getClientCard() {
        return clientCard;
    }

    public ImageButton getCreditAsk() {
        return creditAsk;
    }

    public ImageButton getInkassat() {
        return inkassat;
    }

    public ImageButton getRequestReview() {
        return requestReview;
    }

    public Button getReplikat() {
        return replikat;
    }
/*

    public Button getSettings() {
        return settings;
    }

    public Button getMobileSystem() {
        return mobileSystem;
    }

    public Button getRefreshDatabaze() {
        return refreshDatabaze;
    }

    public Button getSinchronizationOptimum() {
        return sinchronizationOptimum;
    }

    public Button getSearch() {
        return search;
    }
*/
}
