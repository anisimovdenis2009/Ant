package com.app.ant.app.AddressBook.gui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.Strings;
import com.app.ant.app.AddressBook.gui.components.MenuConfigurationModel;
import com.app.ant.app.AddressBook.gui.components.Message;
import com.app.ant.app.AddressBook.gui.settings.SettingsController;
import com.app.ant.app.AddressBook.util.ApplicationUtil;
import com.app.ant.app.AddressBook.util.DOMUtil;
import com.app.ant.app.AddressBook.util.IOUtil;
import com.app.ant.app.AddressBook.xmlfeatures.BaseXMLCreator;
import com.app.ant.app.AddressBook.xmlfeatures.ClientCardXMLCreator;
import com.app.ant.app.AddressBook.xmlfeatures.CreditAskXMLCreator;

import java.io.File;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 08.12.11
 * Time: 14:33
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseGUIActivity extends PreferenceActivity {
    private BaseModel m;
    private String current;
    private int type = 0;

    protected abstract void init();

    protected abstract void populate();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void checkAndCreaterUuid() {
        if (m.getUuid() == null)
            m.setUuid(UUID.randomUUID().toString());
    }

    @Override
    public void onBackPressed() {
        Message.confirmationYesNo(this, Strings.MAIN_MENU_ESCAPE, "Сохранить заявку?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String s1 = null;
                        if (save(type, false))
                            finish();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }, true
        ).show();

    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu = MenuConfigurationModel.createMenuClientCard(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                startActivityForResult(new Intent(this, SettingsController.class), 1);
                break;
            case 1:
                replication(type);
                break;
            case 2:
                if (save(type, false))
                    Message.info(this, Strings.FILE_SAVED).show();
                break;
            case 3:
                if (save(type, true))
                    Message.info(this, Strings.FILE_SAVED).show();
                break;
        }
        return true;
    }

    protected void replication(final int type) {
        String s1 = null;
        if (IOUtil.getConnectionState(this))
            if (save(type, false))
                IOUtil.sendToFTPAsync(new File(current), this, type);
            else
                Message.error(this, Strings.VERIFICATION_FALSE).show();
        else
            Message.confirmationYesNo(this, "Не обнаружено подключения к интеренету. Репликация не может быть выполнена", "Может просто сохранить заявку на sd-карту?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String s1 = null;
                    if (save(type, false))
                        Message.info(BaseGUIActivity.this, Strings.FILE_SAVED).show();
                }
            }, true).show();
    }

    protected boolean save(int screen, boolean isNew) {
        boolean b = false;
        if (m.verification()) {
            BaseXMLCreator akmlCreator = null;
            if (isNew)
                m.setUuid(UUID.randomUUID().toString());
            switch (screen) {
                case 0:
                    akmlCreator = new ClientCardXMLCreator(m);
                    String s1 = m.getUuid() + ".akml";
                    String s = "/" + s1;
                    current = Common.CLIENT_CARD_OUTBOUND_PATH + s;
                    break;
                case 1:
                    akmlCreator = new CreditAskXMLCreator(m);
                    s1 = m.getUuid() + ".akml";
                    s = "/" + s1;
                    current = Common.CREDIT_ASK_OUTBOUND_PATH + s;
                    break;
            }

            DOMUtil.output(akmlCreator.getDocument(), current);
            b = true;
        } else Message.error(this, Strings.VERIFICATION_FALSE).show();
        return b;
    }

    public void setM(BaseModel m) {
        this.m = m;
        m.setMobVer(ApplicationUtil.getVersionName(this));
    }

    public void setType(int type) {
        this.type = type;
    }

}