package com.app.ant.app.BusinessLayer;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 05.03.12
 * Time: 15:00
 * To change this template use File | Settings | File Templates.
 */
public class Filter {
    private int id;
    private int position;
    private String name;
    private String docGridCondition;
    private boolean checked;
    private boolean booking;

    public int getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getDocGridCondition() {
        return docGridCondition;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setDocGridCondition(String docGridCondition) {
        this.docGridCondition = docGridCondition;
    }

    public boolean isBooking() {
        return booking;
    }

    public void setBooking(boolean booking) {
        this.booking = booking;
    }

    public Filter(int id, int position, String name, String docGridCondition) {
        this.id = id;
        this.position = position;
        this.name = name;
        this.docGridCondition = docGridCondition;
    }
}
