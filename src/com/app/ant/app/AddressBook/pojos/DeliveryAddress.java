package com.app.ant.app.AddressBook.pojos;

import com.app.ant.app.AddressBook.pojos.delivery.Kiosk;
import com.app.ant.app.AddressBook.pojos.delivery.Market;
import com.app.ant.app.AddressBook.pojos.delivery.Shop;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 11.11.11
 * Time: 13:46
 * To change this template use File | Settings | File Templates.
 */
public class DeliveryAddress implements Serializable {
    public static final int SHOP = 0;
    public static final int KIOSK = 1;
    public static final int MARKET = 2;

    private String indeks = null;
    private String area = null;
    private String department = null;
    private String city = null;

    private int type;
    public Object deliveryAddress;

    public DeliveryAddress(int type) {
        this.type = type;
        switch (type) {
            case SHOP:
                deliveryAddress = new Shop();
                break;
            case KIOSK:
                deliveryAddress = new Kiosk();
                break;
            case MARKET:
                deliveryAddress = new Market();
                break;
        }
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
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
}
