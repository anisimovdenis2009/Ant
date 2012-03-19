package com.app.ant.app.AddressBook.gui.components;

import android.content.Context;
import android.preference.DialogPreference;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.AddressBook.util.GUIFactory;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 21.12.11
 * Time: 13:57
 * To change this template use File | Settings | File Templates.
 */
public class MyContactView extends DialogPreference {
    Context context;
    private TextView firstNameText;
    private TextView lastNameText;
    private TextView telephoneText;
    private TextView mobileTelephoneText;
    private TextView emailText;

    private EditText firstNameEdit;
    private EditText lastNameEdit;
    private EditText telephoneEdit;
    private EditText mobileTelephoneEdit;
    private EditText emailEdit;

    public MyContactView(Context context) {
        super(context, null);
        this.context = context;
        setLayoutResource(R.layout.preference);
        setPositiveButtonText("Ок");
        setNegativeButtonText("Отмена");
    }


    protected View onCreateDialogView() {
        firstNameText = GUIFactory.textView(context, "Имя и Отчество");
        lastNameText = GUIFactory.textView(context, "Фамилия");
        telephoneText = GUIFactory.textView(context, "Телефон");
        firstNameText = GUIFactory.textView(context, "Мобильный Телефон");
        firstNameText = GUIFactory.textView(context, "E-Mail");



        LinearLayout linearLayout_ = new LinearLayout(getContext());
        linearLayout_.setOrientation(LinearLayout.VERTICAL);
        linearLayout_.setPadding(10, 0, 10, 0);

        linearLayout_.addView(firstNameText);

        ScrollView scrollView_ = new ScrollView(getContext());
        scrollView_.addView(linearLayout_);
        return linearLayout_;
    }
}
