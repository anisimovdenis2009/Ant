package com.app.ant.app.AddressBook.pojos;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 25.10.11
 * Time: 13:12
 * To change this template use File | Settings | File Templates.
 */
public class LawAddress implements Serializable {
    //public static LawAddress instance;
    private String indeks;
    private String area;
    private String department;
    private String city;

    private String street;
    private String house;
    private String building;
    private String appartment;
    private String office;

    public LawAddress() {

    }

    /* public static LawAddress getInstance() {
        if (instance == null)
            instance = new LawAddress();
        return instance;
    }*/
    public void refresh() {
        indeks = null;
        area = null;
        department = null;
        city = null;
        street = null;
        house = null;
        building = null;
        appartment = null;
        office = null;
    }

    public String getIndeks() {
        return indeks;
    }

    public String getArea() {
        return area;
    }

    public String getDepartment() {
        return department;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public String getHouse() {
        return house;
    }

    public String getBuilding() {
        return building;
    }

    public String getAppartment() {
        return appartment;
    }

    public String getOffice() {
        return office;
    }

    public void setIndeks(String indeks) {
        this.indeks = indeks;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setCity(String city) {
        this.city = city;
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

    public void setAppartment(String appartment) {
        this.appartment = appartment;
    }

    public void setOffice(String office) {
        this.office = office;
    }
}
