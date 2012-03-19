package com.app.ant.app.AddressBook.gui.clientcard.lawcontacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.clientcard.lawcontacts.addcontact.AddContactsController;
import com.app.ant.app.AddressBook.pojos.Contact;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 24.10.11
 * Time: 11:30
 * To change this template use File | Settings | File Templates.
 */
public class LawContactController extends Activity implements View.OnClickListener {
    private LawContactModel m;
    private LawContactsView v;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra(Common.CONTACT_EXTRA))
            m = new LawContactModel(intent.getParcelableArrayListExtra(Common.CONTACT_EXTRA));
        else
            m = new LawContactModel();
        v = new LawContactsView(this, m);
        setActivityContentView(v);
        v.getAdd().setOnClickListener(this);
    }

    public void setActivityContentView(View v) {
        if (v.getParent() != null && v.getParent() instanceof ViewGroup) {
            ((ViewGroup) (v.getParent())).removeView(v);
        }
        setContentView(v);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, AddContactsController.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Contact a = (Contact) data.getParcelableExtra(Common.CONTACT_EXTRA_RESULT);
            m.addContact(a);
            //v = new LawContactsView(this,m);
        }
        v.populateContacts(m.getContacts());
        setActivityContentView(v);
    }

    public LawContactsView getV() {
        return v;
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Common.CONTACT_EXTRA_RESULT, m.getContacts());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}