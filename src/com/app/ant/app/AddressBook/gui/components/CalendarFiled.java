package com.app.ant.app.AddressBook.gui.components;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 06.10.11
 * Time: 11:26
 * To change this template use File | Settings | File Templates.
 */
public class CalendarFiled extends LinearLayout {
    private String resultValue;
    private TextView data;
    private Button minus;
    private Button plus;
    private int val;

    public CalendarFiled(Context context, int value, final boolean isDay) {
        super(context);
        this.val = value;
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        data = new TextView(context);
        data.setTextSize(50);

        minus = new Button(context);
        plus = new Button(context);
        minus.setWidth(120);
        plus.setWidth(120);
        minus.setText("-");
        plus.setText("+");

        data.setText(String.valueOf(value));
        addView(minus);
        addView(data);
        addView(plus);

        minus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //int newValue = Integer.parseInt(data.getText().toString());
                if (isDay) {
                    if (val >= 0)
                        val--;
                } else {
                    if (val >= 1990)
                        val--;
                }
                data.setText(String.valueOf(val));

            }
        });

        plus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //int newValue = Integer.parseInt(data.getText().toString());
                if (isDay) {
                    if (val <= 31)
                        val++;
                } else {
                    if (val <= 2090)
                        val++;
                }
                data.setText(String.valueOf(val));
            }

        });
    }

    public int getResultValue() {
        return val;
    }
}
