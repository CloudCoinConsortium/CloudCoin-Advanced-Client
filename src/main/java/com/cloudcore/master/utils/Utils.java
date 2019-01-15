package com.cloudcore.master.utils;

import com.cloudcore.master.core.Config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;

public class Utils {


    /* Fields */

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss.SSSSSSSXXX");

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");


    /* Methods */

    /**
     * Creates a Gson object, a JSON parser for converting JSON Strings and objects.
     *
     * @return a Gson object.
     */
    public static Gson createGson() {
        return new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
    }

    public static int charCount(String pown, char character) {
        return pown.length() - pown.replace(Character.toString(character), "").length();
    }

    public static String ensureFilenameUnique(String filename, String extension, String folder) {
        if (!Files.exists(Paths.get(folder + filename + extension)))
            return filename + extension;

        filename = filename + '.';
        String newFilename;
        int loopCount = 0;
        do {
            newFilename = filename + Integer.toString(++loopCount);
        }
        while (Files.exists(Paths.get(folder + newFilename + extension)));
        return newFilename + extension;
    }

    public static String getDate() {
        return dateFormat.format(new Date());
    }

    public static String getSimpleDate() {
        return simpleDateFormat.format(new Date());
    }

    public static String getHtmlFromURL(String urlAddress) {
        String data = "";

        SimpleLogger.QuickLog("connecting to " + urlAddress);
        try {
            URL url = new URL(urlAddress);
            HttpURLConnection connect = (HttpURLConnection) url.openConnection();
            connect.setConnectTimeout(Config.milliSecondsToTimeOutDetect);
            if (200 != connect.getResponseCode()) {
                SimpleLogger.QuickLog("200 response code from " + urlAddress);
                return data;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));

            StringBuilder builder = new StringBuilder();
            while ((data = in.readLine()) != null)
                builder.append(data);
            in.close();
            data = builder.toString();
            SimpleLogger.QuickLog("received: " + data);
        } catch (IOException e) {
            e.printStackTrace();
            data = "";
        }

        return data;
    }

    public static int indexOf(String[] array, String value) {
        for (int i = 0, j = array.length; i < j; i++)
            if (value.equals(array[i])) return i;
        return -1;
    }

    /**
     * Converts a byte array to a hexadecimal String.
     *
     * @param data the byte array.
     * @return a hexadecimal String.
     */
    public static String bytesToHexString(byte[] data) {
        final String HexChart = "0123456789ABCDEF";
        final StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data)
            hex.append(HexChart.charAt((b & 0xF0) >> 4)).append(HexChart.charAt((b & 0x0F)));
        return hex.toString();
    }

    /**
     * Pads a String with characters appended in the beginning.
     * This is primarily used to pad 0's to hexadecimal Strings.
     *
     * @param string  the String to pad.
     * @param length  the length of the output String.
     * @param padding the character to pad the String with.
     * @return a padded String with the specified length.
     */
    public static String padString(String string, int length, char padding) {
        return String.format("%" + length + "s", string).replace(' ', padding);
    }

    public static int parseInt(String value) {
        if (value == null || value.length() == 0) return -1;

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void saveData(String key, String value) {
        Preferences pref = Preferences.userRoot().node("com/cloudcore/master");
        pref.put(key, value);
    }

    public static String loadData(String key) {
        Preferences pref = Preferences.userRoot().node("com/cloudcore/master");
        return pref.get(key, "");
    }
}
