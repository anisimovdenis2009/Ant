package com.app.ant.app.AddressBook.pojos.delivery;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 11.11.11
 * Time: 14:05
 * To change this template use File | Settings | File Templates.
 */
public class Kiosk implements Serializable{
    private String street;
    private String house;
    private String building;
    private String orientation;

    public String getStreet() {
        return street;
    }

    public String getHouse() {
        return house;
    }

    public String getBuilding() {
        return building;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }
}
