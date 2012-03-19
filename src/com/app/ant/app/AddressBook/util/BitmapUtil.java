package com.app.ant.app.AddressBook.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.app.ant.app.AddressBook.Common;

import java.io.*;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 13.05.11
 * Time: 17:06
 * To change this template use File | settings | File Templates.
 */
public class BitmapUtil {


    public static Bitmap getScaledBitmapFromFile(File file, int width, int height) {
        if (file != null) {
            FileInputStream input = null;
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                //read only size of image from file
                input = new FileInputStream(file);
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(input, null, options);
                if (input != null)
                    input.close();
                options.inJustDecodeBounds = false;
                options.inSampleSize = getImageScale(options.outWidth, options.outHeight, width, height);
                input = new FileInputStream(file);
/*                Log.v(Common.TAG, file.getName());
                Log.v(Common.TAG, String.valueOf(options.outWidth));
                Log.v(Common.TAG, String.valueOf(options.outHeight));*/
                //read scaled bitmap
                return BitmapFactory.decodeStream(input, null, options);
            } catch (IOException e) {
                
                e.printStackTrace();
            } catch (OutOfMemoryError e) {
                Log.w(Common.TAG, "Unable to get bitmap from file - out of memory");
                e.printStackTrace();
            } finally {
                try {
                    if (input != null)
                        input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static int getImageScale(int originalWidth, int originalHeight, int newWidth, int newHeight) {
        if ((newWidth > originalWidth) && (newHeight > originalHeight))
            return 0;
        double scaleWidth = 0;
        double scaleHeight = 0;
        if (newWidth - 1 >= 0)
            scaleWidth = (double) originalWidth / (double) newWidth - 1;
        if (newHeight - 1 >= 0)
            scaleHeight = (double) originalHeight / (double) newHeight - 1;
        if (scaleWidth < 0)
            scaleWidth = 0;
        if (scaleHeight < 0)
            scaleHeight = 0;
        if (scaleWidth > scaleHeight) {
            return (int) scaleWidth;
        } else {
            return (int) scaleHeight;
        }
    }

    public static File[] getFolderCovers(File imFile) {
        if (imFile != null) {
            File[] images = imFile.listFiles(
                    new FilenameFilter() {
                        public boolean accept(File dir, String path) {
                            int lastSlash = path.lastIndexOf('/');
                            if (lastSlash >= 0 && lastSlash + 2 < path.length()) {
                                // ignore those ._* files created by MacOS
                                if (path.regionMatches(lastSlash + 1, "._", 0, 2)) {
                                    return false;
                                }
                            }
                            String temp = path.toUpperCase();
                            if (temp.endsWith(Common.JPG_EXTENSION)
                                    || temp.endsWith(Common.JPEG_EXTENSION)
                                    || temp.endsWith(Common.PNG_EXTENSION)
                                    || temp.endsWith(Common.GIF_EXTENSION)
                                    ) {

                                return true;
                            } else
                                return false;
                        }
                    }
            );
            if (images == null || images.length == 0)
                return new File[0];
            if (images.length > 1)
                Arrays.sort(images);
            return images;
        }
        return new File[0];
    }

    public static File getCacheCover(final String filename) {
        File imFile = new File(Common.CASH_PATH);
        IOUtil.checkAndCreatePath(imFile);
        File[] imagesCache = null;
        imagesCache = imFile.listFiles(
                new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isFile() && pathname.getName().startsWith(filename);
                    }
                }
        );
        if (imagesCache == null || imagesCache.length < 1)
            imagesCache = new File[1];
        return imagesCache[0];
    }
}
