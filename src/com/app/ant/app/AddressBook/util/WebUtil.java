package com.app.ant.app.AddressBook.util;

import android.util.Log;
import com.app.ant.app.AddressBook.Common;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by IntelliJ IDEA.
 *
 * @author DA
 *         Date: 16.05.11
 *         Time: 15:49
 *         To change this template use File | settings | File Templates.
 */
public class WebUtil {


    private static InputStream getCoverInput(String imUrl) {
        InputStream input = null;
        try {
            if (imUrl != null) {
                URLConnection covercon = new URL(imUrl).openConnection();
                input = covercon.getInputStream();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return input;
    }

    public static boolean downloadImageToFile(String imUrl, String fileName) {
        boolean success = true;
        InputStream input = null;
        BufferedOutputStream output = null;
        try {
            byte[] buffer = new byte[2048];
            int size;
            input = getCoverInput(imUrl);
            if (input != null) {
                output = new BufferedOutputStream(new FileOutputStream(fileName));
                while ((size = input.read(buffer)) > 0) {
                    output.write(buffer, 0, size);
                }
                output.flush();
            } else
                success = false;

        } catch (IOException ex) {
            ex.printStackTrace();
            success = false;
        } finally {
            try {
                if (input != null)
                    input.close();
                if (output != null)
                    output.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return success;
    }

    public static boolean downloadImageToFile(String imUrl, File fileName) {
        boolean success = true;
        InputStream input = null;
        BufferedOutputStream output = null;
        try {
            byte[] buffer = new byte[2048];
            int size;
            input = getCoverInput(imUrl);
            if (input != null) {
                output = new BufferedOutputStream(new FileOutputStream(fileName));
                while ((size = input.read(buffer)) > 0) {
                    output.write(buffer, 0, size);
                }
                output.flush();
            } else
                success = false;

        } catch (IOException ex) {
            ex.printStackTrace();
            success = false;
        } finally {
            try {
                if (input != null)
                    input.close();
                if (output != null)
                    output.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return success;
    }

    public static File downloadImageToFile2(String imUrl, File success) {
        //boolean success = true;
        InputStream input = null;
        BufferedOutputStream output = null;
        if (success.exists())
            try {
                byte[] buffer = new byte[2048];
                int size;
                input = getCoverInput(imUrl);
                if (input != null) {
                    output = new BufferedOutputStream(new FileOutputStream(success));
                    while ((size = input.read(buffer)) > 0) {
                        output.write(buffer, 0, size);
                    }
                    output.flush();
                } else
                    Log.v(Common.TAG, "file didn,t downloaded");

            } catch (IOException ex) {
                ex.printStackTrace();
                
                //success = false;
            } finally {
                try {
                    if (input != null)
                        input.close();
                    if (output != null)
                        output.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        return success;
    }
}
