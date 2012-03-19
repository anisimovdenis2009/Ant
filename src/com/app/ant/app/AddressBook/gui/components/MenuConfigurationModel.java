package com.app.ant.app.AddressBook.gui.components;

import android.view.Menu;
import com.app.ant.R;
import com.app.ant.app.AddressBook.Strings;
import com.app.ant.app.AddressBook.gui.StartActivity;

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 03.10.11
 * Time: 10:09
 * To change this template use File | Settings | File Templates.
 */
public class MenuConfigurationModel {
    public static LinkedList<PositionItem> options = new LinkedList<PositionItem>();

    public static Menu createMenu(Menu menu) {
        options = new LinkedList<PositionItem>();
        options.add(new PositionItem(Strings.MENU_SETTINGS, StartActivity.MENU_SETTINGS, true, android.R.drawable.ic_menu_preferences, StartActivity.MENU_SETTINGS));
        options.add(new PositionItem(Strings.MENU_UPDATE_DATABASE, StartActivity.MENU_UPDATE_DATABASE, true, R.drawable.ic_menu_archive, 1));
        options.add(new PositionItem(Strings.MENU_REPLICATION, StartActivity.MENU_REPLICATION, true, R.drawable.ic_menu_refresh, 2));
        options.add(new PositionItem(Strings.MENU_INFO, StartActivity.MENU_INFO, true, android.R.drawable.ic_menu_info_details, StartActivity.MENU_INFO));
        options.add(new PositionItem(Strings.MENU_UPDATE, StartActivity.MENU_UPDATE, true, R.drawable.ic_menu_refresh, StartActivity.MENU_UPDATE));

        for (PositionItem i : options) {
            menu.add(0, i.code, 0, i.name).setIcon(i.icon);
        }
        return menu;
    }

    public static Menu createMenuClientCard(Menu menu) {
        options = new LinkedList<PositionItem>();
        options.add(new PositionItem(Strings.MENU_SETTINGS, 0, true, android.R.drawable.ic_menu_preferences, 0));
        options.add(new PositionItem(Strings.MENU_REPLICATION, 1, true, R.drawable.ic_menu_refresh, 1));
        options.add(new PositionItem(Strings.MENU_SAVE, 2, true, R.drawable.ic_menu_archive, 2));
        options.add(new PositionItem(Strings.MENU_SAVE_NEW, 3, true, R.drawable.ic_menu_archive, 3));

        for (PositionItem i : options) {
            menu.add(0, i.code, 0, i.name).setIcon(i.icon);
        }
        return menu;
    }

    static public class PositionItem {

        public String name;
        public int code;
        public boolean active;
        public int icon;
        public int position;


        public PositionItem(String name, int code, boolean active, int icon, int position) {
            this.name = name;
            this.code = code;
            this.active = active;
            this.icon = icon;
            this.position = position;
        }
    }
}
