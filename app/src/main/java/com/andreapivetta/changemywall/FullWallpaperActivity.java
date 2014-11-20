package com.andreapivetta.changemywall;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreapivetta.changemywall.wallpapers.Wallpaper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;


public class FullWallpaperActivity extends ActionBarActivity {

    private Wallpaper mWallpaper;
    private ImageView fullImageView;
    private ImageButton addToLibraryButton, zoomImageButton;
    private Button setAsBackgroundButton;
    private SharedPreferences mSharedPreferences;
    private AlertDialog mResDialog, mDownloadDialog;

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyManager;

    private boolean download = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_wallpaper);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        mSharedPreferences = getSharedPreferences("MyPref", 0);

        mWallpaper = getIntent().getParcelableExtra("WALL");
        fullImageView = (ImageView) findViewById(R.id.fullImageView);

        if (mWallpaper.isResTooHigh()) {
            Picasso.with(this)
                    .load(mWallpaper.thumbURL)
                    .into(fullImageView);

            if (!mSharedPreferences.getBoolean(Utilities.DONT_SHOW_AGAIN, false)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View dialogView = View.inflate(FullWallpaperActivity.this, R.layout.dialog_res_toohigh, null);

                final Button gotItButton = (Button) dialogView.findViewById(R.id.gotItButton);
                final CheckBox checkBox = (CheckBox) dialogView.findViewById(R.id.checkBox);

                checkBox.setChecked(mSharedPreferences.getBoolean(Utilities.DONT_SHOW_AGAIN, false));
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mSharedPreferences.edit().putBoolean(Utilities.DONT_SHOW_AGAIN, !isChecked).apply();
                    }
                });

                gotItButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mResDialog.dismiss();
                    }
                });

                mResDialog = builder.setView(dialogView).create();
                mResDialog.show();
            }

        } else {
            Picasso.with(this)
                    .load(mWallpaper.fullURL)
                    .placeholder(new BitmapDrawable(getResources(), (Bitmap) getIntent().getParcelableExtra("THUMB")))
                    .into(fullImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            download = false;
                        }

                        @Override
                        public void onError() {

                        }
                    });
        }

        addToLibraryButton = (ImageButton) findViewById(R.id.addToLibraryButton);
        zoomImageButton = (ImageButton) findViewById(R.id.zoomImageButton);
        setAsBackgroundButton = (Button) findViewById(R.id.setAsBackgroundButton);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            ((TextView) findViewById(R.id.resTextView)).setText(mWallpaper.getResolutionString());
        setUpListeners();

    }

    void setUpListeners() {
        this.addToLibraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mSharedPreferences.getBoolean(Utilities.DONT_SHOW_AGAIN_DOWNLOAD, false)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FullWallpaperActivity.this);
                    View dialogView = View.inflate(FullWallpaperActivity.this, R.layout.dialog_download, null);

                    final CheckBox showAgainCheckBox = (CheckBox) dialogView.findViewById(R.id.askMeAgainCheckBox);
                    final Button downloadButton = (Button) dialogView.findViewById(R.id.downloadButton);
                    final Button cancelButton = (Button) dialogView.findViewById(R.id.cancelButton);

                    showAgainCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            mSharedPreferences.edit().putBoolean(Utilities.DONT_SHOW_AGAIN_DOWNLOAD, true).apply();
                        }
                    });

                    downloadButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pushNotification();
                            int i = mSharedPreferences.getInt(Utilities.IMAGE_NAME, 1);
                            if (download) {
                                new DownloadAsyncTask().execute("merisi" + i);
                            } else {
                                Utilities.saveImageToExternalStorage(getApplicationContext(), "merisi" + i, ((BitmapDrawable) fullImageView.getDrawable()).getBitmap());

                                mBuilder.setContentText("Download complete")
                                        .setProgress(0, 0, false);
                                mNotifyManager.notify(i, mBuilder.build());
                                mSharedPreferences.edit().putInt(Utilities.IMAGE_NAME, i + 1).apply();
                            }


                            mDownloadDialog.dismiss();
                        }
                    });

                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDownloadDialog.dismiss();
                        }
                    });

                    mDownloadDialog = builder.setView(dialogView).create();
                    mDownloadDialog.show();
                } else {
                    pushNotification();
                    int i = mSharedPreferences.getInt(Utilities.IMAGE_NAME, 1);
                    if (download)
                        new DownloadAsyncTask().execute("merisi" + i);
                    else
                        Utilities.saveImageToExternalStorage(getApplicationContext(), "merisi" + i, ((BitmapDrawable) fullImageView.getDrawable()).getBitmap());

                    mBuilder.setContentText("Download complete")
                            .setProgress(0, 0, false);
                    mNotifyManager.notify(i, mBuilder.build());
                    mSharedPreferences.edit().putInt(Utilities.IMAGE_NAME, i + 1).apply();
                }
            }
        });

        this.setAsBackgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setAs = new Intent(Intent.ACTION_ATTACH_DATA);
                setAs.setDataAndType(getImageUri(getApplicationContext(), ((BitmapDrawable) fullImageView.getDrawable()).getBitmap()), "image/*");
                startActivityForResult(Intent.createChooser(setAs, getString(R.string.set_as)), 0);
            }
        });

        this.zoomImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FullWallpaperActivity.this, ZoomedActivity.class);
                i.putExtra("WALLPAPER", mWallpaper.fullURL);

                startActivity(i);
            }
        });
    }

    void pushNotification() {
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Picture Download")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_stat_icon_notification)
                .setProgress(0, 0, true);

        mNotifyManager.notify(mSharedPreferences.getInt(Utilities.IMAGE_NAME, 1), mBuilder.build());
    }

    Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_full_wallpaper, menu);

        ShareActionProvider mShareActionProvider = (ShareActionProvider)
                MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share));
        mShareActionProvider.setShareIntent(getDefaultIntent());

        return true;
    }

    private Intent getDefaultIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT,
                "Look! " + mWallpaper.fullURL + " I've found it with this amazing app: http://www.amazon.com/Andrea-Pivetta-Merisi/dp/B00PPP5JYC/");
        intent.setType("text/plain");
        return intent;
    }

    private class DownloadAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... name) {
            Utilities.saveImageFromUrl(mWallpaper.fullURL, FullWallpaperActivity.this, name[0]);
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                int i = mSharedPreferences.getInt(Utilities.IMAGE_NAME, 1);
                mBuilder.setContentText("Download complete")
                        .setProgress(0, 0, false);
                mNotifyManager.notify(i, mBuilder.build());
                mSharedPreferences.edit().putInt(Utilities.IMAGE_NAME, i + 1).apply();
            }
        }
    }
}
