package com.app.ant.app.AddressBook.gui.clientcard.lawcontacts;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.*;
import com.app.ant.app.AddressBook.gui.components.Message;
import com.app.ant.app.AddressBook.pojos.Contact;
import com.app.ant.app.AddressBook.util.GUIFactory;

import java.util.ArrayList;


public class LawContactsView extends LinearLayout {
    private LawContactModel m;
    private Context context;
    private LinearLayout contacts;
    private Button add;
    //private BaseAdapter adapter;

    public LawContactsView(final Context context, final LawContactModel model) {
        super(context);
        this.m = model;
        this.context = context;
        setOrientation(VERTICAL);
        add = new Button(context);
        add.setText("Добавить контакт");

        contacts = new LinearLayout(context);
        contacts.setOrientation(VERTICAL);
        //lw = new ListView(context);
        //initAdapter(context);
        //lw.setAdapter(adapter);
        populateContacts(m.getContacts());
        setScrollContainer(true);

        addView(add);
        addView(contacts);

        //lw.setBackgroundColor(Color.WHITE);
        //setBackgroundColor(Color.WHITE);
        /*lw.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                lw.setBackgroundColor(Color.WHITE);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lw.setBackgroundColor(Color.WHITE);
            }
        });*/
    }

    public void populateContacts(ArrayList<Contact> data) {
        contacts.removeAllViews();
        if (data != null)
            for (final Contact c : data) {
                LinearLayout conLayout = GUIFactory.linearLayout(context);
                LayoutParams p = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                conLayout.setOrientation(VERTICAL);
                conLayout.setPadding(10, 5, 10, 5);


                LinearLayout la = GUIFactory.linearLayout(context);
                la.setBackgroundColor(Color.WHITE);
                TextView atw1 = GUIFactory.textView(context, "ФИО : ");
                int size = 25;
                atw1.setTextSize(size);
                atw1.setBackgroundColor(Color.WHITE);
                atw1.setTextColor(Color.BLUE);

                TextView atw = GUIFactory.textView(context, c.getFirstLastName());
                atw.setTextSize(size);
                atw.setBackgroundColor(Color.WHITE);
                atw.setTextColor(Color.BLUE);

                la.addView(atw1);
                la.addView(atw);

                LinearLayout lc = GUIFactory.linearLayout(context);
                lc.setBackgroundColor(Color.WHITE);
                TextView ctw1 = GUIFactory.textView(context, "Телефон : ");
                ctw1.setTextSize(size);
                ctw1.setBackgroundColor(Color.WHITE);
                ctw1.setTextColor(Color.BLUE);

                TextView ctw = GUIFactory.textView(context, c.getTelephone());
                ctw.setTextSize(size);
                ctw.setBackgroundColor(Color.WHITE);
                ctw.setTextColor(Color.BLUE);

                lc.addView(ctw1);
                lc.addView(ctw);


                LinearLayout lm = GUIFactory.linearLayout(context);
                lm.setBackgroundColor(Color.WHITE);
                TextView mtw1 = GUIFactory.textView(context, "Мобильный \n телефон : ");
                mtw1.setTextSize(size);
                mtw1.setBackgroundColor(Color.WHITE);
                mtw1.setTextColor(Color.BLUE);

                TextView mtw = GUIFactory.textView(context, c.getMobileTelephone());
                mtw.setTextSize(size);
                mtw.setBackgroundColor(Color.WHITE);
                mtw.setTextColor(Color.BLUE);

                lm.addView(mtw1);
                lm.addView(mtw);


                LinearLayout le = GUIFactory.linearLayout(context);
                le.setBackgroundColor(Color.WHITE);
                TextView etw1 = GUIFactory.textView(context, "E-mail : ");
                etw1.setTextSize(size);
                etw1.setBackgroundColor(Color.WHITE);
                etw1.setTextColor(Color.BLUE);

                TextView etw = GUIFactory.textView(context, c.getEmail());
                etw.setTextSize(size);
                etw.setBackgroundColor(Color.WHITE);
                etw.setTextColor(Color.BLUE);
                le.addView(etw1);
                le.addView(etw);

                Button delete = GUIFactory.button("Удалить", context);
                delete.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Message.confirmationYesNo(context, "Удаление контакта", "Вы уверены, что вы хотите удалить контакт?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LinearLayout p = (LinearLayout) v.getParent();
                                LinearLayout contact = (LinearLayout) p.getChildAt(0);
                                TextView contactRight = (TextView) contact.getChildAt(1);

                                String key = (String) contactRight.getText();
                                for (Contact c : m.getContacts()) {
                                    if (c.getFirstLastName().equals(key)) {
                                        m.getContacts().remove(c);
                                        contacts.removeView(p);
                                    }
                                    break;
                                }
                                populateContacts(m.getContacts());
                                ((LawContactController) context).setContentView(((LawContactController) context).getV());
                            }
                        }, true).show();
                    }
                });
                //ctw.setPadding(10, 0, 0, 0);
                //ctw.setWidth(200);
                conLayout.addView(la);
                if (c.getTelephone() != null)
                    conLayout.addView(lc);
                if (c.getMobileTelephone() != null)
                    conLayout.addView(lm);
                if (c.getEmail() != null)
                    conLayout.addView(le);
                conLayout.addView(delete);

                contacts.addView(conLayout);
            }
    }

    public Button getAdd() {
        return add;
    }

    /* public void initAdapter(final Context context) {
adapter = new BaseAdapter() {
@Override
public int getCount() {
return m.getContacts().size();
}

@Override
public Object getItem(int position) {
return m.getContacts().get(position);
}

@Override
public long getItemId(int position) {
return position;
}



@Override
public View getView(int position, View convertView, ViewGroup parent) {
Contact contact = m.getContacts().get(position);
Button delete = GUIFactory.button("Удалить контакт", context);
final int pos = position;
delete.setOnClickListener(new OnClickListener() {
@Override
public void onClick(View v) {
    //get linaer layout named rigth(it is item of list view)
    LinearLayout p = (LinearLayout) v.getParent();

    //get  list view
    ListView p1 = (ListView) p.getParent();

    m.getContacts().remove(pos);
    p1.removeViewAt(pos);
    //removeViewAt(pos);
    TextView name = (TextView) p.getChildAt(0);
    String key = name.getText().toString();

    *//* if (key != null) {
                          for(Contact contact:m.get)
                        } else {
                            TextView telephone = (TextView) p.getChildAt(1);
                            key = telephone.getText().toString();
                        }
                        if (key != null) {

                        } else {
                            TextView mobphone = (TextView) p.getChildAt(2);
                            key = mobphone.getText().toString();
                            if (key != null) {


                            } else {
                                TextView mail = (TextView) p.getChildAt(3);
                                key = mail.getText().toString();
                                if (key != null) {

                                }
                            }
                        }*//*
                    }
                }
                );
                //delete.setBackgroundColor(Color.TRANSPARENT);
                *//*LinearLayout l = new LinearLayout(context);
                l.setOrientation(HORIZONTAL);*//*
                LinearLayout right = new LinearLayout(context);
                right.setOrientation(VERTICAL);

                TextView telLabel = new TextView(context);
                String s = "Телефон:" + contact.getTelephone();
                telLabel.setText(s);
                telLabel.setTextSize(30);


                TextView mobLabel = new TextView(context);
                s = "Мобильный телефон:" + contact.getMobileTelephone();
                mobLabel.setText(s);
                mobLabel.setTextSize(30);


                TextView mailLabel = new TextView(context);
                s = "E-mail:" + contact.getEmail();
                mailLabel.setText(s);
                mailLabel.setTextSize(30);


                TextView v = new TextView(context);
                v.setText(contact.getFirstLastName());
                v.setTextSize(30);
*//*
                telLabel.setTextColor(Color.rgb(2, 0, 255));
                v.setTextColor(Color.rgb(2, 0, 255));
                mailLabel.setTextColor(Color.rgb(2, 0, 255));
                mobLabel.setTextColor(Color.rgb(2, 0, 255));
*//*
                if (contact.getFirstLastName() != null)
                    right.addView(v);
                if (contact.getTelephone() != null)
                    right.addView(telLabel);
                if (contact.getMobileTelephone() != null)
                    right.addView(mobLabel);
                if (contact.getEmail() != null)
                    right.addView(mailLabel);

                right.addView(delete);
                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                //l.addView(delete, params);
                return right;
            }
        }
        ;
    }*/


/*    public BaseAdapter getAdapter() {
        return adapter;
    }*/
}
