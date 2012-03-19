package com.app.ant.app.AddressBook.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Operations on java.lang.String
 *
 * @author AT
 * @since 2009-10-20
 */
public class StringUtil {

    public static SimpleDateFormat DATE_FORMAT_LOCALE = new SimpleDateFormat();

    /**
     * A method similar to the one in apache commons-lang-2.0 StringUtils
     * <p>Checks if a String is whitespace, empty ("") or null.</p>
     * <p/>
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param s the String to check, may be null
     * @return <code>true</code> if the String is null, empty or whitespace
     * @since 2.0
     */
    public static boolean isBlank(String s) {
        int length;
        if (s == null || (length = s.length()) == 0)
            return true;
        for (int i = 0; i < length; i++)
            if (!Character.isWhitespace(s.charAt(i)))
                return false;

        return true;
    }

    /**
     * A method similar to the one in apache commons-lang-2.0 StringUtils
     * <p>Checks if a String is empty ("") or null.</p>
     * <p/>
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     * <p/>
     * <p>NOTE: This method changed in Lang version 2.0.
     * It no longer trims the String.
     * That functionality is available in isBlank().</p>
     *
     * @param s the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }

    public static String longToDate(long date) {
        return longToDate(date, DATE_FORMAT_LOCALE);
    }

    public static String longToDate(long date, DateFormat format) {
        return format.format(new Date(date));
    }

/*    public static String intToTime(int millies){
        int decSec = millies / 100;

        StringBuffer sb = new StringBuffer();
        sb.append(decSec/36000).append(':').append((decSec%36000)/600)
                .append(':').append((decSec%600)/10)
                .append('.').append(decSec%10);
        return sb.toString();
    }*/

/*    public static String longToMb(long size) {
        StringBuffer sb = new StringBuffer();
        if (size < 1024){
            sb.append(String.valueOf(size)).append(" b");
        }else if (size < 1048576){
            sb.append(size/1024).append('.').append((size%1024)/102).append(" Kb");
        }else{
            sb.append(size/1048576).append('.').append((size%1048576)/104858).append(" Mb");
        }
        return sb.toString();
    }*/

    /**
     * Convert byte array to string.
     * Don't forget invoke mark() if buffer isn't fresh
     * @param buffer byte array with string
     * @param length length of decode string
     * @param charsetEncoding
     * @return string's been encoded with appropriate charset encoding
     * @throws UnsupportedEncodingException
     */
    public static String decodeString(ByteBuffer buffer, int length, TextEncodings charsetEncoding)
            throws UnsupportedEncodingException {
        if((buffer.limit() - buffer.position()) < length)
            return "";

        byte[] rawString = new byte[length];
        buffer.get(rawString);
        return new String(rawString, charsetEncoding.getCharsetName());
    }

    /**
     * Convert byte array with stop zero(s) to string.
     * Don't forget invoke mark() if buffer isn't fresh
     *
     * @param buffer          abyte array with string
     * @param charsetEncoding
     * @return string's been encoded with appropriate charset encoding
     */
    public static String decodeZeroEndString(ByteBuffer buffer, TextEncodings charsetEncoding)
            throws UnsupportedEncodingException {
        boolean oneZeroStop = charsetEncoding != TextEncodings.UTF16 && charsetEncoding != TextEncodings.UTF16BE;
        boolean isTerminatorFound = false;
        byte ch;

        while(!isTerminatorFound && buffer.hasRemaining()) {
            ch = buffer.get();
            if(ch == 0) {
                //if UTF-8 or ISO-8859-1 one stop zero
                if(oneZeroStop) {
                    isTerminatorFound = true;
                //if UTF-16 check second stop zero as well
                } else if(buffer.hasRemaining() && buffer.get() == 0)
                    isTerminatorFound = true;
            }
        }

        if(isTerminatorFound || !buffer.hasRemaining()) {
            //safe position and move to string begining
            int oldPosition = buffer.position();
            buffer.reset();

            int rawStringSize = oldPosition - buffer.position();
            //if stop-zerro wasn't found try to decode all bytes
            if(isTerminatorFound)
                rawStringSize = rawStringSize - charsetEncoding.getStopZeroCount();

            //count byte string size
            byte[] rawString = new byte[rawStringSize];
            //get string
            buffer.get(rawString);

            //restore position. just in case if buffer isn't fresh
            buffer.position(oldPosition);
            return new String(rawString, charsetEncoding.getCharsetName());
        } else
            return "";
    }

    /**
     * Return text encoding by index according with id3 specification 
     * @param index of support encoding in (id3)
     * @return encdoing
     */
    public static TextEncodings getTextEncodingByIndex(int index) {
        TextEncodings[] encodings = TextEncodings.values();
        if(index >= 0 && index < encodings.length)
            return encodings[index];
        else
            return encodings[0];
    }

    public static String uncapitalizeFirst(String ROOT) {
        String ROOTbegin = ROOT.substring(0, 1).toLowerCase();
        String ROOTend = ROOT.substring(1);
        ROOT = ROOTbegin + ROOTend;
        return ROOT;
    }

    public static String capitalizeFirst(String ROOT) {
        String ROOTbegin = ROOT.substring(0, 1).toUpperCase();
        String ROOTend = ROOT.substring(1);
        ROOT = ROOTbegin + ROOTend;
        return ROOT;
    }
    

    public static String toMb(long bytes) {
        long mb = (long)(bytes >> 20);
        bytes -= (mb << 20);
        long mbp = bytes * 100 / 1048576;
        StringBuilder sb = new StringBuilder();
        sb.append(mb).append('.');
        if (mbp < 10)
            sb.append(0);
        sb.append(mbp).append("Mb");
        return sb.toString();
    }

    public static Integer stringToInteger(String val){
        Integer res;
        try{
            res = Integer.parseInt(val);
        }catch(Exception e){
            return null;
        }
        return res;
    }

    public static String milliesToMin(long miliSec) {
        long min = miliSec/60000;
        int centiMin = (int)((miliSec % 60000) / 600);
        StringBuilder sb = new StringBuilder();
        sb.append(min);
        if (centiMin > 0)
            sb.append('.').append( centiMin);
        return sb.toString();  //To change body of created methods use File | Settings | File Templates.
    }

    public static String cutCanonicalName(String name) {
        int index;
        String tmp = null;
        for(;;) {
            if((index = name.lastIndexOf(File.separator)) != -1) {
                tmp = name.substring(index+1);
            }
            // If length of tile > 0, then go next
            if(tmp != null && tmp.length() > 0) {
                return tmp;
            }
            if(index > 0)
                name = name.substring(0, index);
            else
                return name;
        }
    }
}