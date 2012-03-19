package com.app.ant.app.AddressBook.util;

/**
 * Created by IntelliJ IDEA.
 * User: vianisimov
 * Date: 19.10.11
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
public class CalendarUtil {
    public static String getSringMonth(int a) {
        String month = "";
        switch (a) {
            case 0:
                month = "Январь";
                break;
            case 1:
                month = "Февраль";
                break;
            case 2:
                month = "Март";
                break;
            case 3:
                month = "Апрель";
                break;
            case 4:
                month = "Май";
                break;
            case 5:
                month = "Июнь";
                break;
            case 6:
                month = "Июль";
                break;
            case 7:
                month = "Август";
                break;
            case 8:
                month = "Сентябрь";
                break;
            case 9:
                month = "Октябрь";
                break;
            case 10:
                month = "Ноябрь";
                break;
            case 11:
                month = "Декабрь";
                break;
            default:
                break;

        }
        return month;
    }
}
