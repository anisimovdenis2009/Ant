package com.app.ant.app.AddressBook.gui.components;

import android.content.Context;
import android.preference.ListPreference;
import com.app.ant.R;


/**
 * Created by IntelliJ IDEA.
 * User: vianisimov
 * Date: 19.10.11
 * Time: 16:24
 * To change this template use File | Settings | File Templates.
 */
public class MyListPreference extends ListPreference {
    private ItemSaveListener saver;
    private int number;

    public MyListPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            String title = getEntry().toString();
            setSummary(title);
            saver.onSaveItem(this, number, title);
        }
    }

    public void setOnItemSaveListener(ItemSaveListener listener) {
        this.saver = listener;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
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
