package com.andreapivetta.changemywall.wallpapers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

public class Util {
    private static final String TAG = "Wallbase.Util";

    public static boolean isWifiConnected(Context c) {
        if (c != null) {
            ConnectivityManager connManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] connections = connManager.getAllNetworkInfo();
            for (NetworkInfo connection : connections)
                if (connection != null && connection.getType() == ConnectivityManager.TYPE_WIFI && connection.isConnectedOrConnecting() ||
                        connection != null && connection.getType() == ConnectivityManager.TYPE_ETHERNET && connection.isConnectedOrConnecting())
                    return true;
        }
        return false;
    }

    public static boolean exists(String URLName) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            // HttpURLConnection.setInstanceFollowRedirects(false);
            HttpURLConnection con =
                    (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }
}