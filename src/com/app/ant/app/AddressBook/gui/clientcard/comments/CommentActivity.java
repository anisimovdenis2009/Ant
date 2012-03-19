package com.app.ant.app.AddressBook.gui.clientcard.comments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import com.app.ant.R;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.components.Message;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 09.11.11
 * Time: 15:02
 * To change this template use File | Settings | File Templates.
 */
public class CommentActivity extends Activity {
    private String comment = "";
    EditText textView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment);
        textView = (EditText) findViewById(R.id.comment_area);
        Intent intent = getIntent();
        if (intent.hasExtra(Common.COMMENT_EXTRA)) {
            comment = intent.getStringExtra(Common.COMMENT_EXTRA);
            textView.setText(comment);
        }
    }

    @Override
    public void onBackPressed() {

        Message.confirmationYesNo(this, "Соxранение комментария", "Сохранять ли комментарий?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                comment = textView.getText().toString();
                Intent resultIntent = new Intent();
                resultIntent.putExtra(Common.COMMENT_EXTRA, comment);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }, true).show();

    }
}