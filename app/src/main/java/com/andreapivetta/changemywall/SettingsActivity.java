package com.andreapivetta.changemywall;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.File;


public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    public static class PlaceholderFragment extends PreferenceFragment {

        private CheckBoxPreference notificationsPreference;
        private Preference restorePreference, clearLibraryPreference;

        public PlaceholderFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            notificationsPreference = (CheckBoxPreference) findPreference("pref_key_notifications");
            restorePreference = findPreference("pref_key_restore");
            clearLibraryPreference = findPreference("pref_key_clear_library");

            final SharedPreferences mypref = getActivity().getSharedPreferences(Utilities.MY_PREF, 0);
            notificationsPreference.setChecked(mypref.getBoolean(Utilities.NOTIFICATIONS_ENABLED, true));

            notificationsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (notificationsPreference.isChecked()) {
                        mypref.edit().putBoolean(Utilities.NOTIFICATIONS_ENABLED, true).apply();
                    } else {
                        mypref.edit().putBoolean(Utilities.NOTIFICATIONS_ENABLED, false).apply();
                    }

                    return true;
                }
            });

            restorePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.restore_preferences_dialog_title)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    mypref.edit().putBoolean(Utilities.NOTIFICATIONS_ENABLED, true).apply();
                                    mypref.edit().putBoolean(Utilities.DONT_SHOW_AGAIN, false).apply();
                                    mypref.edit().putBoolean(Utilities.DONT_SHOW_AGAIN_DOWNLOAD, false).apply();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                }
                            }).create().show();

                    return true;
                }
            });

            clearLibraryPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.delete_library_dialog_title)
                            .setMessage(R.string.cant_undo)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                                    File myDir = new File(root + "/Wallpapers");

                                    if (myDir.list() != null) {
                                        for (String list : myDir.list()) {
                                            Uri actualUri = Uri.parse("file://" + myDir + "/" + list);
                                            deleteImage(actualUri);
                                        }
                                    }
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            }).create().show();


                    return true;
                }
            });

        }

        private void deleteImage(Uri uri) {
            Uri content_uri = getImageContentUri(getActivity(), new File(uri.getPath()));
            getActivity().getContentResolver().delete(content_uri, null, null);
        }

        private static Uri getImageContentUri(Context context, File imageFile) {
            String filePath = imageFile.getAbsolutePath();
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID},
                    MediaStore.Images.Media.DATA + "=? ",
                    new String[]{filePath}, null);

            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.MediaColumns._ID));
                return Uri.withAppendedPath(Uri.parse("content://media/external/images/media"), "" + id);
            } else {
                if (imageFile.exists()) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, filePath);
                    return context.getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                } else {
                    return null;
                }
            }
        }
    }
}
