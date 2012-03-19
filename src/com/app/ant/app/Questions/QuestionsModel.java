package com.app.ant.app.Questions;

import android.database.Cursor;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Settings;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 14.03.12
 * Time: 10:16
 * To change this template use File | Settings | File Templates.
 */
public class QuestionsModel {

    ArrayList<Question> questions;
    String docId;


    public QuestionsModel() {
        questions = new ArrayList<Question>();
        HashMap<String, String> answer = new HashMap<String, String>();
        String[] objects = new String[0];
        /*answer.put("ДА", "Y");
        answer.put("НЕТ", "N");
        answer.put("КАТЕГОРИЯ НЕ ПРЕДСТАВЛЕНА", "NULL");*/

        String answerSelect = Settings.getInstance().getPropertyFromSettings("gs_shelfpart_answers");
        Cursor cursor = Db.getInstance().selectSQL(answerSelect);
        if (cursor != null) {
            int count = cursor.getCount();
            objects = new String[count];
            for (int k = 0; k < count; k++) {
                if (cursor.moveToPosition(k)) {
                    answer.put(cursor.getString(2), cursor.getString(1));
                    objects[cursor.getInt(0)] = cursor.getString(2);
                }
            }

            cursor.close();
        }

        String select = "SELECT QstID, QstQuestion, QstDescr, case when (QstGsPt = '00' or QstGsPt = Dpgspt) then 1 else 0 end QstVisible FROM GSQuestionnaires Q, CustGSInfos I " +
                "WHERE AddrID = " + AntContext.getInstance().getAddress().addrID + " ORDER BY 4 DESC, QstSeq ASC";

        cursor = Db.getInstance().selectSQL(select);
        if (cursor != null) {
            int size = cursor.getCount();
            for (int i = 0; i < size; i++) {
                if (cursor.moveToPosition(i)) {
                    String description = cursor.getString(2);
                    int visibility = cursor.getInt(3);
                    String text = cursor.getString(1);
                    String id = String.valueOf(cursor.getInt(0));
                    boolean vis = false;
                    if (visibility == 1)
                        vis = true;
                    Question question = new Question(id, text, description, answer, vis);
                    question.keys = objects;
                    questions.add(question);
                }
            }
            cursor.close();
        }

        cursor = Db.getInstance().selectSQL("select coalesce(MIN (SpDocID),0)-1 from GSShelfParts");
        if (cursor != null) {
            docId = String.valueOf(cursor.getInt(0));
            cursor.close();
        }
    }


}
