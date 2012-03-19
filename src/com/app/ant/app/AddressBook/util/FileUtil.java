package com.app.ant.app.AddressBook.util;

import android.content.Context;
import android.util.Log;
import com.app.ant.app.AddressBook.Common;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Comparator;
import java.util.Properties;


/**
 * @author DA
 * @since 2009-07-28
 */
public class FileUtil {
    public static final int COPY_OK = 0;
    public static final int COPY_PERMISSION_SRC_DENIED = 1;
    public static final int COPY_PERMISSION_DEST_DENIED = 2;
    public static final int COPY_IO_EXCEPTION = 3;
    public static final int COPY_CANT_REPLACE_DEST_FILE = 4;

    public static final Comparator<File> FILENAME_IGNORED_ORDER = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    };


    public static Properties readProperties(String fileName) {
        Properties result = new Properties();
        FileInputStream fis = null;

        try {
            File file = new File(fileName);
            if (file.exists()) {
                fis = new FileInputStream(file);
                result.load(fis);
            } else {
                Log.e(Common.TAG, "Can't find file: " + fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static void writeProperties(String fileName, Properties properties) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            properties.store(fos, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Copy file with out file access permission test
     *
     * @param src     source file
     * @param dst     destination file
     * @param testSrc text access permission to source file
     * @param testDst text access permission to destination file
     * @return result code
     */
    public static int copy(File src, File dst, boolean testSrc, boolean testDst) throws IOException {
        Runtime.getRuntime().exec("chmod 777 " + src.getAbsolutePath());
        if (testSrc && !src.canRead())
            return COPY_PERMISSION_SRC_DENIED;
        if (testDst && !dst.getParentFile().canWrite())
            return COPY_PERMISSION_DEST_DENIED;
        if (testDst && dst.exists())
            if (!dst.delete())
                return COPY_CANT_REPLACE_DEST_FILE;
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
            return COPY_OK;
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    public static void checkAndReplaceFile(String origFilePath, String bakFilePath) {
        File f2 = new File(bakFilePath);
        File f1 = new File(origFilePath);
        if (f2.exists()) {
            if (f1.exists()) {
                f1.delete();
            }
            f2.renameTo(f1);
        } else {
            if (!f1.exists()) {
                try {
                    f1.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void removeFile(String tempPath) {
        File f = new File(tempPath);
        if (f.exists())
            f.delete();
    }

    public static void copyRawToFile(File file, int rId, Context context) throws IOException {
        final InputStream is = context.getApplicationContext().getResources()
                .openRawResource(rId);
        byte[] data = new byte[is.available()];
        is.read(data);
        is.close();

        final FileOutputStream os = new FileOutputStream(file);
        os.write(data);
        os.flush();
        os.close();
    }

    public static boolean isMessageExist(String file) {
        String[] messages = new File(Common.ALIDI_MESSAGES_PATH).list();
        String a = file.split("\\.")[0];
        for (String message : messages) {
            String b = message.split("\\.")[0];
            if (b.equals(a))
                return true;
        }
        messages = new File(Common.ALIDI_MESSAGES_SENDED_PATH).list();
        for (String message : messages) {
            String b = message.split("\\.")[0];
            if (b.equals(a))
                return true;
        }
        return false;
    }
}
