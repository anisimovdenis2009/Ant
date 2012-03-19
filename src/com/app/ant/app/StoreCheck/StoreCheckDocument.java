package com.app.ant.app.StoreCheck;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 15.03.12
 * Time: 15:17
 * To change this template use File | Settings | File Templates.
 */
public class StoreCheckDocument {
    Integer id;
    Date date;
    String showDate;
    String code;

    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Date getDate() {
        return date;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public StoreCheckDocument(Integer id, Date date, String showDate, String code) {
        this.id = id;
        this.date = date;
        this.showDate = showDate;
        this.code = code;
    }
}
