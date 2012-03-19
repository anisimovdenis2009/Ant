package com.app.ant.app.AddressBook.util;

import android.content.Context;
import android.preference.Preference;
import android.view.View;
import android.webkit.WebView;
import android.widget.*;
import com.app.ant.app.AddressBook.gui.components.*;

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 29.04.11
 * Time: 12:12
 * To change this template use File | settings | File Templates.
 */
public class GUIFactory {
    public static View setup(View component) {
        component.setId(component.hashCode());
        return component;
    }

    public static ImageView setup(int resId, ImageView component) {
        setup(component);
        component.setImageResource(resId);
        return component;
    }


    public static MyPreference myPreference(Context context, String title) {
        MyPreference c = new MyPreference(context);
        c.setTitle(title);
        c.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) context);
        return c;
    }

    public static MyListPreference createList(Context context, int number, String title) {
        MyListPreference a;
        a = new MyListPreference(context);
        a.setTitle(title);
        a.setNumber(number);
        a.setOnItemSaveListener((ItemSaveListener) context);
        return a;
    }

    public static MyEditPreference myEditPreference(Context context, String title, Integer n) {
        MyEditPreference name = new MyEditPreference(context);
        name.setTitle(title);
        name.setNumber(n);
        name.setOnItemSaveListener((ItemSaveListener) context);
        return name;
    }

    public static MyCheckBoxPreference myCheckBoxPreference(Context context, CharSequence title, int number, CharSequence summaryOn, CharSequence summaryOff) {
        MyCheckBoxPreference checkBox = new MyCheckBoxPreference(context);
        checkBox.setTitle(title);
        checkBox.setNumber(number);
        checkBox.setSummaryOn(summaryOn);
        checkBox.setSummaryOff(summaryOff);
        checkBox.setOnItemSaveListener((ItemSaveListener) context);
        return checkBox;
    }

    public static EditText editText(Context context) {
        return (EditText) GUIFactory.setup(new EditText(context));
    }

    public static TextView textView(Context context) {
        return (TextView) GUIFactory.setup(new TextView(context));
    }


    public static CheckBox checkBox(Context context) {
        return (CheckBox) GUIFactory.setup(new CheckBox(context));
    }

    public static RadioButton radioButton(Context context) {
        return (RadioButton) GUIFactory.setup(new RadioButton(context));
    }

    public static ImageView imageView(Context context) {
        return (ImageView) GUIFactory.setup(new ImageView(context));
    }

    public static ImageView imageView(Context context, int resId) {
        return GUIFactory.setup(resId, new ImageView(context));
    }

    public static ImageButton imageButton(Context context) {
        return (ImageButton) GUIFactory.setup(new ImageButton(context));
    }

    public static ImageButton imageButton(Context context, int resId) {
        return (ImageButton) GUIFactory.setup(resId, new ImageButton(context));
    }

    public static RatingBar ratingBar(Context context) {
        return (RatingBar) GUIFactory.setup(new RatingBar(context));
    }

    public static WebView webView(Context context) {
        return (WebView) GUIFactory.setup(new WebView(context));
    }

    public static ScrollView scrollView(Context context) {
        return (ScrollView) GUIFactory.setup(new ScrollView(context));
    }

    public static ProgressBar progressBar(Context context) {
        return (ProgressBar) GUIFactory.setup(new ProgressBar(context));
    }

    public static SeekBar seekBar(Context context) {
        return (SeekBar) GUIFactory.setup(new SeekBar(context));
    }

    public static ListView listView(Context context) {
        return (ListView) GUIFactory.setup(new ListView(context));
    }

    public static Button button(Context context) {
        return (Button) GUIFactory.setup(new Button(context));
    }

    public static TableLayout tableLayout(Context context) {
        return (TableLayout) GUIFactory.setup(new TableLayout(context));
    }

    public static TableRow tableRow(Context context) {
        return (TableRow) GUIFactory.setup(new TableRow(context));
    }

    public static LinearLayout linearLayout(Context context) {
        return (LinearLayout) GUIFactory.setup(new LinearLayout(context));
    }

    public static RelativeLayout relativeLayout(Context context) {
        return (RelativeLayout) GUIFactory.setup(new RelativeLayout(context));
    }

    public static FrameLayout frameLayout(Context context) {
        return (FrameLayout) GUIFactory.setup(new FrameLayout(context));
    }

    public static TextView textView(Context context, String text) {
        TextView component = new TextView(context);
        component.setText(text);
        return (TextView) GUIFactory.setup(component);
    }

    public static Button button(String text, Context context) {
        Button component = new Button(context);
        component.setText(text);
        return (Button) GUIFactory.setup(component);
    }

    public static CheckBox checkBox(Context context, String text) {
        CheckBox component = new CheckBox(context);
        component.setText(text);
        return (CheckBox) GUIFactory.setup(component);
    }

    public static RadioButton radioButton(Context context, String text) {
        RadioButton component = new RadioButton(context);
        component.setText(text);
        return (RadioButton) GUIFactory.setup(component);
    }
}
