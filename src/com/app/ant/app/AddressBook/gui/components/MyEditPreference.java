package com.app.ant.app.AddressBook.gui.components;


import android.content.Context;
import android.preference.EditTextPreference;
import com.app.ant.R;
import com.app.ant.app.AddressBook.util.IOUtil;


/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 28.09.11
 * Time: 15:19
 * To change this template use File | settings | File Templates.
 */
public class MyEditPreference extends EditTextPreference {
    private ItemSaveListener saver;
    private int number;
    private boolean validation = false;
    private boolean allCapitals = false;

    public MyEditPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference);
        getEditText().setSingleLine();
    }

    public MyEditPreference(Context context, boolean allCapitals) {
        super(context);
        validation = true;
        this.allCapitals = allCapitals;
        setLayoutResource(R.layout.preference);
        getEditText().setSingleLine();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            String title = getEditText().getText().toString();
            if (validation && allCapitals) {
                title = title.toUpperCase();
            } else if (validation) {
                title = IOUtil.bigFrist(title);
            }
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

    public void setValidation(boolean validation) {
        this.validation = validation;
    }

    public void setAllCapitals(boolean allCapitals) {
        this.allCapitals = allCapitals;
    }
}
