package com.andreapivetta.changemywall;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Utilities {
    public static final String MY_PREF = "MyPref";
    public static final String NOTIFICATIONS_ENABLED = "NOTIFICATIONS_ENABLED";
    public static final String RUN_IN_BACKGROUND = "RUN_IN_BACKGROUND";
    public static final String DONT_SHOW_AGAIN = "DONT_SHOW_AGAIN";
    public static final String DONT_SHOW_AGAIN_DOWNLOAD = "DONT_SHOW_AGAIN_WALLPAPER";
    public static final String IMAGE_NAME = "NAME";
    public static final String MINUTES = "MINUTES";
    public static final String NOT_FIRST = "Whatever";
    public static final String URLS = "urls";
    private static final String NOT_ID = "NOT_ID";

    public static final String KIND = "KIND";
    public static final int RANDOM = 0;
    public static final int TOP = 1;

    public static void pushNotification(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(MY_PREF, 0);
        int myID = preferences.getInt(NOT_ID, 0);
        SharedPreferences.Editor editor = preferences.edit();

        if (myID > 100)
            editor.putInt(NOT_ID, 0).apply();
        else
            editor.putInt(NOT_ID, (myID + 1)).apply();

        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class)
                .addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context);
        mBuilder.setSmallIcon(R.drawable.ic_stat_icon_notification)
                .setContentTitle(context.getResources().getString(R.string.wallpaper_set))
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(myID, mBuilder.build());
    }

    public static void saveImageFromUrl(String source, Context context, String name) {
        try {
            URL url = new URL(source);
            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            saveImageToExternalStorage(context, name, image);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();
        }
    }

    public static void saveImageToExternalStorage(Context context, String name, Bitmap finalBitmap) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/Wallpapers");
        myDir.mkdirs();
        String fileName = name + ".jpg";
        File file = new File(myDir, fileName);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaScannerConnection.scanFile(context, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                }
        );
    }

    public static boolean exists(String URLName) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con =
                    (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
