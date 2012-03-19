package com.app.ant.app.AddressBook.options;

import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.util.FileUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;

/**
 * @author DA
 * @since 2009-07-28
 */
public class OptionsUtil {
    public static Properties properties;

    public static HashMap<String, Field> fields = new HashMap<String, Field>();

    private static final String EMPTY = " ";

    static {
        for (Field field : Options.class.getDeclaredFields()) fields.put(field.getName(), field);
    }

    public static void load() {
        synchronized (WRITE_LOCKER) {
            if (new File(Common.FILE_NAME_PROPERTIES).exists()) { //property file not found, use defaults
                properties = FileUtil.readProperties(Common.FILE_NAME_PROPERTIES);
                fromProperties();
            }
        }
    }

    static public void save() {
        synchronized (WRITE_LOCKER) {
            toProperties();
            FileUtil.writeProperties(Common.FILE_NAME_PROPERTIES, properties);
        }
    }

    private static final Object WRITE_LOCKER = new Object();

    /*
     * Start options saving process in thread in order to speed up ui thread. 
     */
    static public void saveAsync() {
        new Thread(new Runnable() {
            public void run() {
                save();
            }
        }).start();
    }

    public static void loadAsync() {
        new Thread(new Runnable() {
            public void run() {
                load();
            }
        }).start();
    }

    private static void fromProperties() {
        for (Object key : properties.keySet()) {
            String keyStr = key.toString();
            Field field = fields.get(keyStr);
            if (field == null)
                continue;
            String value = properties.get(keyStr).toString();
            set(field, value);
        }
    }

    private static void toProperties() {
        properties = new Properties();
        for (Field field : Options.class.getDeclaredFields()) {
            String name = field.getName();
            if (name != null) {
                String value = get(field);
                if (value != null)
                    properties.put(name, value);
            }
        }
    }

    public static String get(Field field) {
        String result = EMPTY;
        try {
            Class type = field.getType();
            if (type == String.class) {
                result = (String) field.get(Options.class);
            } else if (type == Boolean.TYPE) {
                result = Boolean.toString(field.getBoolean(Options.class));
            } else if (type == Integer.TYPE) {
                result = Integer.toString(field.getInt(Options.class));
            } else if (type == Float.TYPE) {
                result = Float.toString(field.getFloat(Options.class));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void set(Field field, String value) {
        try {
            if (value == null)
                value = "";
            Class type = field.getType();
            if (type == Boolean.TYPE)
                field.setBoolean(Options.class, Boolean.parseBoolean(value));
            else if (type == Integer.TYPE)
                field.setInt(Options.class, Integer.parseInt(value));
            else if (type == Float.TYPE)
                field.setFloat(Options.class, Float.parseFloat(value));
            else
                field.set(Options.class, value);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


}
