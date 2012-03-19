package com.app.ant.app.Questions;

import com.app.ant.R;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.app.ant.app.AddressBook.util.GUIFactory;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 14.03.12
 * Time: 10:07
 * To change this template use File | Settings | File Templates.
 */
public class QuestionsView extends LinearLayout {
    Context context;
    TextView bottom;

    public QuestionsView(final Context context, final QuestionsModel m) {
        super(context);
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.questions_pg, this);
        final LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.LEFT;
        layoutParams.weight = 1;
        final LayoutParams spinnerParam = layoutParams;
        String description = m.questions.get(0).getDescription();
        bottom = (TextView) findViewById(R.id.questions_pg_description);
        bottom.setText(description);
        bottom.setTextSize(25);
        ListView view = (ListView) findViewById(R.id.questions_pg_list_view);
        view.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return m.questions.size();
            }

            @Override
            public Object getItem(int i) {
                return m.questions.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(HORIZONTAL);
                final Question question = m.questions.get(i);
                final HashMap<String, String> answers = question.answers;
                
                TextView textView = GUIFactory.textView(context, question.getText());
                layout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottom.setText(question.getDescription());
                    }
                });

                Spinner spinner = new Spinner(context);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, question.keys);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setSelection(0);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        question.setAnswer(question.answers.get(question.keys[i]));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        question.setAnswer(question.answers.get(question.keys[0]));
                    }
                });
                /*for(int k=0; k<spinner.getCount();k++)
                {
                    if(spinner.getItemIdAtPosition(k) == docSaleHeader.paymentID)
                    {
                        spinner.setSelection(k);
                        break;
                    }
                }*/
                layout.addView(textView, spinnerParam);
                spinnerParam.gravity = Gravity.RIGHT;
                spinnerParam.weight = 0;
                layout.addView(spinner, spinnerParam);
                if (question.isVisible())
                    return layout;
                else {
                    TextView textView1 = GUIFactory.textView(context, " ");
                    textView1.setTextSize(0);
                    return textView1;
                }
            }
        });


       
    }
}
