package com.app.ant.app.AddressBook.gui.components;

import android.content.Context;
import android.preference.Preference;
import com.app.ant.R;


/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 21.10.11
 * Time: 15:27
 * To change this template use File | Settings | File Templates.
 */
public class MyPreference extends Preference {
    public MyPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference);
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
