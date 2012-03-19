package com.app.ant.app.AddressBook.gui.clientcard.kladraddress;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 17.01.12
 * Time: 13:13
 * To change this template use File | Settings | File Templates.
 */
public class KladrModel {
    public final String NORTHWEST = "Северо-Западный";
    public final String VOLGANEAR = "Приволжский";
    public final String[] VOLGA_AREAS = new String[]{"Республика Башкортостан",
            "Кировская область",
            "Республика Марий Эл",
            "Республика Мордовия",
            "Нижегородская область",
            "Оренбургская область",
            "Пензенская область",
            "Пермский край",
            "Самарская область",
            "Саратовская область",
            "Республика Татарстан",
            "Удмуртская Республика",
            "Ульяновская область",
            "Чувашская Республика"};

    public final String[] VOLGA_AREAS_ID = new String[]{"02",
            "43", "12", "13", "52", "56", "58", "59",
            "63",
            "64",
            "16",
            "18",
            "73",
            "21"};

    /*public final String[] VOLGA_AREAS_ID = new String[]{"02",
            "43", "12", "13", "52", "56", "58", "59",
            "63",
            "64",
            "16",
            "18",
            "73",
            "21"};*/

    public final String[] NORTHWEST_AREAS = new String[]{"Архангельская область",
            "Вологодская область",
            "Калининградская область",
            "Республика Карелия",
            "Республика Коми",
            "Ленинградская область",
            "Мурманская область",
            "Ненецкий автономный округ",
            "Новгородская область",
            "Псковская область",
            "Санкт-Петербург"};
    public final String[] NORTHWEST_AREAS_ID = new String[]{"29", "35", "39", "10", "11", "47", "51", "83", "53", "60", "78"};



    //Pojos
    private String area = null;
    private String department = null;
    private String city = null;
    private String vilage = null;
    private String street = null;

    public String getArea() {
        return area;
    }

    public String getDepartment() {
        return department;
    }

    public String getCity() {
        return city;
    }

    public String getVilage() {
        return vilage;
    }

    public String getStreet() {
        return street;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setVilage(String vilage) {
        this.vilage = vilage;
    }
}
