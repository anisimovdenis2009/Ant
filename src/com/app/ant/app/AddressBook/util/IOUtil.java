package com.app.ant.app.AddressBook.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.Preference;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.Strings;
import com.app.ant.app.AddressBook.gui.StartActivity;
import com.app.ant.app.AddressBook.gui.components.Message;
import com.app.ant.app.AddressBook.options.Options;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 20.05.11
 * Time: 16:57
 * To change this template use File | settings | File Templates.
 */
public class IOUtil {
    private static final Object WRITE_LOCKER = new Object();
    public static final int UR_INN = 10;
    public static final int FIZ_INN = 12;


    public static void sendToFTPAsync(final File bufferPath, final Activity activity, final int type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean b;
                b = sendToFTP(bufferPath, type);
                if (b)
                    activity.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Message.info(activity, Strings.FILE_SENDED).show();

                                }
                            }
                    );
                else if (b)
                    activity.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Message.error(activity, Strings.FILE_NOT_SENDED).show();
                                }
                            }
                    );
            }
        }).start();
    }

    public static boolean loadFromFTP(String mespath) {
        return loadFromFTP(mespath, Common.AK_EXCHANGE_OUTBOUND, true);
    }

    public static boolean loadFromFTP(String androidPath, String ftpPath, boolean isMessage) {
        // synchronized (WRITE_LOCKER) {
        boolean b = false;
        FTPClient ftpClient = new FTPClient();
        FileInputStream fileInputStream = null;
        try {
            ftpClient.connect(Options.firstAddress, Options.port);
            ftpClient.login(Common.FTPUSER, Common.FTP_ADMIN_PASS);
            boolean ft = ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            if (!ft)
                throw new Exception("Error");
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(ftpPath);
            for (String path : ftpClient.listNames()) {
                String filename = androidPath + "/" + path;
                String path1 = ftpPath + path;
                if (!isMessage) {
                    if (!path.equals("delivery"))
                        b = ftpClient.retrieveFile(path1, new FileOutputStream(filename));
                } else if (path.startsWith(StartActivity.getId()) && !FileUtil.isMessageExist(path)) {
                    b = ftpClient.retrieveFile(path, new FileOutputStream(filename));
                }
            }
            ftpClient.logout();
        } catch (Exception
                e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //  }
        return b;
    }

    public static boolean loadFromFTP(File filename, String ftpPath) {
        // synchronized (WRITE_LOCKER) {
        boolean b = false;
        FTPClient ftpClient = new FTPClient();
        FileInputStream fileInputStream = null;
        try {
            ftpClient.connect(Options.firstAddress, Options.port);
            ftpClient.login(Common.FTPUSER, Common.FTP_ADMIN_PASS);
            boolean ft = ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            if (!ft)
                throw new Exception("Error");
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(ftpPath);
            b = ftpClient.retrieveFile(ftpPath, new FileOutputStream(filename));
            ftpClient.logout();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //  }
        return b;
    }

    public static boolean sendToFTP(final File bufferPath1, int type) {
        // synchronized (WRITE_LOCKER) {
        boolean b = false;
        FTPClient ftpClient = new FTPClient();
        FileInputStream fileInputStream = null;
        try {
            ftpClient.connect(Options.firstAddress, Options.port);
            ftpClient.login(Common.FTPUSER, Common.FTP_ADMIN_PASS);
            fileInputStream = new FileInputStream(bufferPath1);
            boolean ft = ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            if (!ft)
                throw new Exception("Error");
            ftpClient.enterLocalPassiveMode();
            String remote = null;
            switch (type) {
                case 0:
                    remote = Common.AK_EXCHANGE_INBOUND_CUSTOMER_CARD + bufferPath1.getName();
                    break;
                case 1:
                    remote = Common.AK_EXCHANGE_INBOUND_CREDIT_REQUEST + bufferPath1.getName();
                    break;
            }
            b = ftpClient.storeFile(remote, fileInputStream);
            ftpClient.logout();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //  }
        if (b) {
            File file = new File(bufferPath1.getParent() + Common.SENDED);
            IOUtil.checkAndCreatePath(file);
            String n = bufferPath1.getName();
            File dst = new File(file.getAbsolutePath() + "/" + n);
            try {
                FileUtil.copy(bufferPath1, dst, true, true);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            FileUtil.removeFile(bufferPath1.getAbsolutePath());
        }
        return b;
    }

    public static boolean getConnectionState(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return (networkInfo != null) && networkInfo.isConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static void checkAndCreatePath(String pathName) {
        File path = new File(pathName);
        checkAndCreatePath(path);
    }

    public static void checkAndCreatePath(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    public static boolean validateIndeks(Activity activity, Preference parent, String title) {
        boolean b = false;
        boolean b1 = false;
        String errorMessage = "Неправильно ввдённый индекс!";
        b1 = title.length() == 6;
        errorMessage += "Должно быть 6 символов!";
        if (b1) {
            b = tryToParse(activity, parent, title);
        } else {
            Message.error(activity, errorMessage).show();
            parent.setSummary(Strings.ERROR_TITLE);
        }
        return b;
    }

    public static boolean validateInn(boolean isUr, Activity activity, Preference parent, String title) {
        boolean b = false;
        boolean b1 = false;
        String errorMessage = Strings.WRONG_INN;
        if (isUr) {
            b1 = title.length() == UR_INN;
            errorMessage += "Должно быть 10 символов!";
        } else {
            b1 = title.length() == FIZ_INN;
            errorMessage += "Должно быть 12 символов!";
        }
        if (b1) {
            b = tryToParse(activity, parent, title);
        } else {
            Message.error(activity, errorMessage).show();
            parent.setSummary(Strings.ERROR_TITLE);
        }
        return b;
    }

    public static boolean validateBELInn(boolean isUr, Activity activity, Preference parent, String title) {
        boolean b = false;
        boolean b1 = false;
        String errorMessage = Strings.WRONG_INN;
        b1 = title.length() == 9;
        errorMessage += "Должно быть 9 символов!";
        if (b1) {
            if (!isUr) {
                b = tryToParse(activity, parent, title);
                if (b) {
                    return true;
                } else {
                    Message.error(activity, errorMessage).show();
                    parent.setSummary(Strings.ERROR_TITLE);
                    return false;
                }
            } else
                return true;
        } else {
            Message.error(activity, errorMessage).show();
            parent.setSummary(Strings.ERROR_TITLE);
            return false;
        }
    }

    public static boolean validateOgrn(boolean isUr, Activity activity, Preference parent, String title) {
        boolean b = false;
        boolean b1 = false;
        String errorMessage = "Неправильно введённое ОГРН!";
        if (isUr) {
            b1 = title.length() == 13;
            errorMessage = errorMessage + "Должно быть 13 символов";
        } else {
            b1 = title.length() == 15;
            errorMessage = errorMessage + "Должно быть 15 символов";
        }
        if (b1) {
            b = tryToParse(activity, parent, title);
        } else {
            Message.error(activity, errorMessage).show();
            parent.setSummary(Strings.ERROR_TITLE);
        }
        return b;
    }

    public static boolean validateKpp(boolean isUr, Activity activity, Preference parent, String title) {
        boolean b = false;
        String errorMessage = "Неправильно введённое KPP!";
        boolean b1 = false;
        if (isUr) {
            b1 = title.length() == 13;
            errorMessage = errorMessage + "Должно быть 13 символов";
        } else {
            b1 = title.length() == 15;
            errorMessage = errorMessage + "Должно быть 15 символов";
        }
        if (b1) {
            b = tryToParse(activity, parent, title);
        } else {
            Message.error(activity, errorMessage).show();
            parent.setSummary(Strings.ERROR_TITLE);
        }
        return b;
    }

    public static boolean tryToParse(Activity activity, Preference parent, String title) {
        boolean b = false;
        try {
            Long.parseLong(title);
            b = true;
        } catch (NumberFormatException e) {
            Message.error(activity, "Введён неправильный символ! Должны быть только цифры!").show();
            parent.setSummary(Strings.ERROR_TITLE);
        }
        return b;
    }

    public static boolean tryToParse(String title) {
        boolean b = false;
        try {
            Long.parseLong(title);
            b = true;
        } catch (NumberFormatException e) {

        }
        return b;
    }


    public static String bigFrist(String b) {
        char a = b.charAt(0);
        String s = String.valueOf(a);
        s = s.toUpperCase();
        b = b.substring(1, b.length());
        String res = s.concat(b);
        return res;
    }

    public static String getWiFiName(Context context) {
        try {
            // Setup WiFi
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            // Get WiFi status
            WifiInfo info = wifi.getConnectionInfo();
            return info.getSSID();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean getWiFiConnectionState(Context context) {
        boolean hasConnection = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo
                    ();
            if ((networkInfo != null) && networkInfo.isConnected()
                    && (networkInfo.getType() == ConnectivityManager.TYPE_WIFI)) {

                if (
                        !(StringUtil.isEmpty(Options.whiteWiFiList)
                                && StringUtil.isEmpty(Options.blackWiFiList))
                        ) {
                    String ssid = getWiFiName(context);
                    if (Options.blackWiFiList != null && Options.blackWiFiList.indexOf(ssid) != -1) {
                        hasConnection = false;
                    } else if (StringUtil.isEmpty(Options.whiteWiFiList)) {
                        hasConnection = true;
                    } else {
                        hasConnection = Options.whiteWiFiList.indexOf(ssid) != -1;
                    }
                } else {
                    hasConnection = true;
                }
            } else {
                hasConnection = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasConnection;
    }

    public static void convert() {
        File[] bufferMessages = new File(Common.ALIDI_MESSAGES_BUFFER_PATH).listFiles();
        for (File file : bufferMessages) {
            if (!file.isDirectory()) {
                final String f = file.getAbsolutePath();
                final String n = f.split("\\.")[0];
                String termp = n.split("buffer")[1];
                File out = new File(Common.ALIDI_MESSAGES_PATH + "/" + termp + ".xml");
                Converter.convert(file, out);
                FileUtil.removeFile(f);
            }
        }
    }
}
