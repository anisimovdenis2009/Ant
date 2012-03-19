package com.app.ant.app.Activities;

public class DocSaleTableModel {
    public int listStID;
    public int listingStID;
    private int channelCountColumnIdx = 0;
    private int styleIDColumnIdx;
    private int itemTypeStyleIDColumnIdx;
    private int itemIdIdx;
    private int parentItemIdIdx;
    private int countSubIdx;
    private int saledQuantityColumnIdx;
    private int prevMonthQntColumnIdx;
    private int prevMonth2QntColumnIdx;
    private int productSaledQuantityColumnIdx;
    private int productPrevMonthQntColumnIdx;
    private int productPrevMonth2QntColumnIdx;

    public DocSaleTableModel() {
    }

    public int getChannelCountColumnIdx() {
        return channelCountColumnIdx;
    }

    public int getStyleIDColumnIdx() {
        return styleIDColumnIdx;
    }

    public int getItemTypeStyleIDColumnIdx() {
        return itemTypeStyleIDColumnIdx;
    }

    public int getItemIdIdx() {
        return itemIdIdx;
    }

    public int getParentItemIdIdx() {
        return parentItemIdIdx;
    }

    public int getCountSubIdx() {
        return countSubIdx;
    }

    public int getSaledQuantityColumnIdx() {
        return saledQuantityColumnIdx;
    }

    public int getPrevMonthQntColumnIdx() {
        return prevMonthQntColumnIdx;
    }

    public int getPrevMonth2QntColumnIdx() {
        return prevMonth2QntColumnIdx;
    }

    public int getProductSaledQuantityColumnIdx() {
        return productSaledQuantityColumnIdx;
    }

    public int getProductPrevMonthQntColumnIdx() {
        return productPrevMonthQntColumnIdx;
    }

    public int getProductPrevMonth2QntColumnIdx() {
        return productPrevMonth2QntColumnIdx;
    }

   /* public void setChannelCountColumnIdx(int channelCountColumnIdx) {
        this.channelCountColumnIdx = channelCountColumnIdx;
    }*/

    public void setStyleIDColumnIdx(int styleIDColumnIdx) {
        this.styleIDColumnIdx = styleIDColumnIdx;
    }

    public void setItemTypeStyleIDColumnIdx(int itemTypeStyleIDColumnIdx) {
        this.itemTypeStyleIDColumnIdx = itemTypeStyleIDColumnIdx;
    }

    public void setItemIdIdx(int itemIdIdx) {
        this.itemIdIdx = itemIdIdx;
    }

    public void setParentItemIdIdx(int parentItemIdIdx) {
        this.parentItemIdIdx = parentItemIdIdx;
    }

    public void setCountSubIdx(int countSubIdx) {
        this.countSubIdx = countSubIdx;
    }

    public void setSaledQuantityColumnIdx(int saledQuantityColumnIdx) {
        this.saledQuantityColumnIdx = saledQuantityColumnIdx;
    }

    public void setPrevMonthQntColumnIdx(int prevMonthQntColumnIdx) {
        this.prevMonthQntColumnIdx = prevMonthQntColumnIdx;
    }

    public void setPrevMonth2QntColumnIdx(int prevMonth2QntColumnIdx) {
        this.prevMonth2QntColumnIdx = prevMonth2QntColumnIdx;
    }

    public void setProductSaledQuantityColumnIdx(int productSaledQuantityColumnIdx) {
        this.productSaledQuantityColumnIdx = productSaledQuantityColumnIdx;
    }

    public void setProductPrevMonthQntColumnIdx(int productPrevMonthQntColumnIdx) {
        this.productPrevMonthQntColumnIdx = productPrevMonthQntColumnIdx;
    }

    public void setProductPrevMonth2QntColumnIdx(int productPrevMonth2QntColumnIdx) {
        this.productPrevMonth2QntColumnIdx = productPrevMonth2QntColumnIdx;
    }

    public void setListStID(int listStID) {
        this.listStID = listStID;
    }

    public void setListingStID(int listingStID) {
        this.listingStID = listingStID;
    }

    public int getListStID() {
        return listStID;
    }

    public int getListingStID() {
        return listingStID;
    }
}