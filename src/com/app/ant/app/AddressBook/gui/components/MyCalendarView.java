package com.app.ant.app.AddressBook.gui.components;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.*;
import com.app.ant.R;
import com.app.ant.app.AddressBook.util.CalendarUtil;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 05.10.11
 * Time: 16:32
 * To change this template use File | Settings | File Templates.
 */
public class MyCalendarView extends DialogPreference {
    private int number;
    private ItemSaveListener saver;

    Context context;
    private ScrollView scrollView_;
    private LinearLayout linearLayout_;

    private TextView yearText;
    private TextView monthText;
    private TextView dayText;

    private CalendarFiled year;
    private Spinner month;
    private CalendarFiled day;

    public MyCalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        setLayoutResource(R.layout.preference);
        setPositiveButtonText("Ок");
        setNegativeButtonText("Отмена");
    }

    public MyCalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyCalendarView(Context context) {
        this(context, null);
    }

    protected View onCreateDialogView() {
        yearText = new TextView(getContext());
        monthText = new TextView(getContext());
        dayText = new TextView(getContext());

        yearText.setText("Год: ");
        monthText.setText("Месяц: ");
        dayText.setText("День: ");


        year = new CalendarFiled(getContext(), 2001, false);
        month = new Spinner(getContext());
        day = new CalendarFiled(getContext(), 20, true);

        month.setAdapter(ArrayAdapter.createFromResource(context, R.array.Months, android.R.layout.simple_spinner_dropdown_item));


        linearLayout_ = new LinearLayout(getContext());
        linearLayout_.setOrientation(LinearLayout.VERTICAL);
        linearLayout_.setPadding(10, 0, 10, 0);

        linearLayout_.addView(yearText);
        linearLayout_.addView(year);
        linearLayout_.addView(monthText);
        linearLayout_.addView(month);
        linearLayout_.addView(dayText);
        linearLayout_.addView(day);

        scrollView_ = new ScrollView(getContext());
        scrollView_.addView(linearLayout_);

        return scrollView_;
    }

    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            GregorianCalendar calendar = new GregorianCalendar(year.getResultValue(),
                    month.getSelectedItemPosition(), day.getResultValue());
            setSummary(calendar.get(Calendar.YEAR)+" "+ CalendarUtil.getSringMonth(calendar.get(Calendar.MONTH))+" "+calendar.get(Calendar.DAY_OF_MONTH));
            saver.onSaveItem(number, calendar);
        }
    }

    public void setOnItemSaveListener(ItemSaveListener listener) {
        this.saver = listener;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
};
