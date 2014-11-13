package com.andreapivetta.changemywall.background;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.andreapivetta.changemywall.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.Random;


public class ChangeWallpaperService extends IntentService {
    private Context context;
    private SharedPreferences mSharedPreferences;

    public ChangeWallpaperService() {
        super("ChangeWallpaperService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.context = getApplicationContext();
        this.mSharedPreferences = context.getSharedPreferences(Utilities.MY_PREF, 0);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (mSharedPreferences.getBoolean(Utilities.RUN_IN_BACKGROUND, false) && mSharedPreferences.getBoolean(Utilities.NOT_FIRST, false)) {
            File myDir =
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Wallpapers");
            String[] images = myDir.list();
            Uri actualUri = Uri.parse("file://" + myDir + "/" + images[(new Random()).nextInt(images.length)]);
            WallpaperManager myWallpaperManager = WallpaperManager.getInstance(context);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), actualUri);
                myWallpaperManager.setBitmap(bitmap);

                if (mSharedPreferences.getBoolean(Utilities.NOTIFICATIONS_ENABLED, false))
                    Utilities.pushNotification(context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mSharedPreferences.edit().putBoolean(Utilities.NOT_FIRST, true).apply();
        }
    }
}
