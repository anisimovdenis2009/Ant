package com.app.ant.app.Questions;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import com.app.ant.app.Activities.AntActivity;
import com.app.ant.app.AddressBook.gui.components.Message;
import com.app.ant.app.BusinessLayer.Common;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.ErrorHandler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 14.03.12
 * Time: 10:03
 * To change this template use File | Settings | File Templates.
 */
public class QuestionsController extends AntActivity {
    QuestionsModel m;
    QuestionsView v;
    Context context;
    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

    private int prevPosition = 0;
    private Long visitId = null;
    private Long questId = null;

    private boolean isMandatory = false;
    private boolean byAddress = false;
    private boolean isFromSteps = false;
    private boolean startNextStep = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        m = new QuestionsModel();
        v = new QuestionsView(this, m);
        setContentView(v);
    }

    @Override
    public void onBackPressed() {
        Message.confirmationYesNo(this, "Возврат к визитам", "Сохранить вопросы?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        save();
                        finishActivity();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishActivity();
                    }
                }, true
        ).show();
    }

    private void save() {
        for (Question question : m.questions) {
            String[] args = new String[7];
            args[0] = m.docId;
            args[1] = "" + AntContext.getInstance().getAddress().addrID;
            args[2] = question.getId();
            args[3] = "SP" + m.docId;
            Date date = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat(Common.DATE_FORMAT_NOW_DATABASE);
            args[4] = sdf.format(date);
            args[5] = question.getAnswer();
            args[6] = "0";
            Db.getInstance().execSQL("INSERT INTO GSShelfParts (SpDocID, SpAddrID, SpQstID, SpDocCode, SpDate, SpAnswer, Sent) VALUES ( ?, ?, ?, ?, ?, ?, ?)", args);
        }
    }

    private void finishActivity() {
        try {
            AntContext.getInstance().getTabController().onBackPressed(context);
        } catch (Exception ex) {
            ErrorHandler.CatchError("Exception in clientForm.onBackPressed", ex);
        }
    }
}
