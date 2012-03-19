package com.app.ant.app.AddressBook.gui.components;

import android.content.Context;
import android.preference.CheckBoxPreference;
import com.app.ant.R;


/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 29.09.11
 * Time: 15:40
 * To change this template use File | settings | File Templates.
 */
public class MyCheckBoxPreference extends CheckBoxPreference {
    private int number;
    private ItemSaveListener saver;


    public MyCheckBoxPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference);
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        String title = String.valueOf(checked);
        saver.onSaveItem(this, number, title);
    }

    public void setChecked(boolean checked, boolean save) {
        super.setChecked(checked);
        String title = String.valueOf(checked);
        if (save)
            saver.onSaveItem(this, number, title);
    }

    public void setOnItemSaveListener(ItemSaveListener listener) {
        this.saver = listener;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled == true)
            setLayoutResource(R.layout.preference);
        else
            setLayoutResource(R.layout.preference_disabled);
    }
}
