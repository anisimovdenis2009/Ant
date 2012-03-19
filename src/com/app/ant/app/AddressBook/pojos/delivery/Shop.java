package com.app.ant.app.AddressBook.pojos.delivery;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 11.11.11
 * Time: 14:05
 * To change this template use File | Settings | File Templates.
 */
public class Shop implements Serializable {
    private String street;
    private String house;
    private String building;
    private String name;
    private String floor;
    private String number;

    public String getStreet() {
        return street;
    }

    public String getHouse() {
        return house;
    }

    public String getBuilding() {
        return building;
    }

    public String getName() {
        return name;
    }

    public String getFloor() {
        return floor;
    }

    public String getNumber() {
        return number;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public void setBuilding(String bilding) {
        this.building = bilding;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
