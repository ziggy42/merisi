package com.andreapivetta.changemywall.wallpapers;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

class PreferenceHelper {
    public static final int SKETCHY = 10;
    public static final int NSFW = 1;
    //TIMESPAN
    public static final String ALLTIME = "1";
    public static final String THREE_MONTHS = "3m";
    public static final String TWO_MONTHS = "2m";
    public static final String ONE_MONTH = "1m";
    public static final String TWO_WEEKS = "2w";
    public static final String THREE_DAYS = "3d";
    public static final String ONE_DAY = "1d";
    public static final String RANDOM = "random";
    public static final int MIN_FREQ_MILLIS = (int) (3 * DateUtils.HOUR_IN_MILLIS); // rotate every 3 hours
    //PURITY
    private static final int SAFE = 100;
    //BOARD
    private static final String MANGA = "1";
    private static final String GENERAL = "2";
    private static final String HIRES = "3";
    private static final String ONE_WEEK = "1w";
    //SEARCHMODE
    private static final String TOPLIST = "toplist";
    private static final int DEFAULT_FREQ_MILLIS = (int) (24 * DateUtils.HOUR_IN_MILLIS); // rotate every 24 hours

    public static boolean getConfigWifiOnly(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getBoolean("conf_wifi", true);
    }

    public static void setConfigWifiOnly(Context context, boolean wifiOnly) {
        SharedPreferences preferences = getPreferences(context);
        preferences.edit().putBoolean("conf_wifi", wifiOnly).apply();
    }

    public static void setConfigFreq(Context context, int durationMillis) {
        SharedPreferences preferences = getPreferences(context);
        preferences.edit().putInt("conf_freq", durationMillis).apply();
    }

    public static int getConfigFreq(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getInt("conf_freq", DEFAULT_FREQ_MILLIS);
    }

    public static int getConfigPurity(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getInt("conf_purity", SAFE);
    }

    public static void setConfigPurity(Context context, int purity) {
        SharedPreferences preferences = getPreferences(context);
        preferences.edit().putInt("conf_purity", purity).apply();
    }

    public static String getConfigBoard(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getString("conf_board", MANGA + GENERAL + HIRES);
    }

    public static void setConfigBoard(Context context, String board) {
        SharedPreferences preferences = getPreferences(context);
        preferences.edit().putString("conf_board", board).apply();
    }

    public static String getConfigTimeSpan(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getString("conf_timespan", ONE_WEEK);
    }

    public static void setConfigTimeSpan(Context context, String timespan) {
        SharedPreferences preferences = getPreferences(context);
        preferences.edit().putString("conf_timespan", timespan).apply();
    }

    public static String getConfigSearchMode(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getString("conf_searchmode", TOPLIST);
    }

    public static void setConfigSearchMode(Context context, String mode) {
        SharedPreferences preferences = getPreferences(context);
        preferences.edit().putString("conf_searchmode", mode).apply();
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences("WallbaseArtSource", Context.MODE_PRIVATE);
    }
}