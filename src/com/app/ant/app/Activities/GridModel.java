package com.app.ant.app.Activities;

import android.database.Cursor;
import com.app.ant.app.BusinessLayer.DocSale;
import com.app.ant.app.Controls.DataGrid;

public class GridModel {
    CursorData cursorData;
    DocSale mDocSale;
    DataGrid mGrid;
    Cursor mCursor;


    public GridModel() {
    }

    public CursorData getCursorData() {
        return cursorData;
    }

    public DocSale getmDocSale() {
        return mDocSale;
    }

    public DataGrid getmGrid() {
        return mGrid;
    }

    public Cursor getmCursor() {
        return mCursor;
    }



    public void setCursorData(CursorData cursorData) {
        this.cursorData = cursorData;
    }

    public void setmDocSale(DocSale mDocSale) {
        this.mDocSale = mDocSale;
    }

    public void setmGrid(DataGrid mGrid) {
        this.mGrid = mGrid;
    }

    public void setmCursor(Cursor mCursor) {
        this.mCursor = mCursor;
    }


}