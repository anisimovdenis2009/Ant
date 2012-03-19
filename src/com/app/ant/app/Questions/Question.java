package com.app.ant.app.Questions;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 14.03.12
 * Time: 10:26
 * To change this template use File | Settings | File Templates.
 */
public class Question {
    private String id;
    private String text;
    private String description;
    private boolean visible;
    private String answer;

    public HashMap<String, String> answers;
    public String[] keys;
    public Question(String id, String text, String description, HashMap<String, String> answers, boolean visible) {
        this.id = id;
        this.answers = answers;
        this.visible = visible;
        this.description = description;
        this.text = text;

    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }


    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getAnswer() {
        return answer;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
