package com.andreapivetta.changemywall.fragments;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.os.Process;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.andreapivetta.changemywall.R;
import com.andreapivetta.changemywall.Utilities;
import com.andreapivetta.changemywall.background.StartupService;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment  extends Fragment {

    private static final int SELECT_PICTURE = 1;
    private Button scheduleButton, chooseImageButton;
    private SharedPreferences mSharedPreferences;
    private ArrayList<Uri> imagesUri = new ArrayList<Uri>();
    private GridView gridView;
    public GridViewAdapter gridViewAdapter;
    private AlertDialog mDialog, mDeleteDialog;

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mSharedPreferences = getActivity().getSharedPreferences(Utilities.MY_PREF, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        this.scheduleButton = (Button) rootView.findViewById(R.id.setRandomBackgroundButton);
        this.chooseImageButton = (Button) rootView.findViewById(R.id.chooseImageButton);
        this.gridView = (GridView) rootView.findViewById(R.id.imagesGridView);
        this.gridViewAdapter = new GridViewAdapter(getActivity(), imagesUri);
        this.gridView.setAdapter(gridViewAdapter);
        setUpListeners();
        loadImages();

        return rootView;
    }

    void setUpListeners() {
        this.scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View dialogView = View.inflate(getActivity(), R.layout.dialog_program, null);

                final RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.radioGroup);
                final Switch changeWallpaperSwitch = (Switch) dialogView.findViewById(R.id.changeWallpaperSwitch);
                final Button doneButton = (Button) dialogView.findViewById(R.id.doneButton);

                radioGroup.check(getCheckedId());
                changeWallpaperSwitch.setChecked(mSharedPreferences.getBoolean(Utilities.RUN_IN_BACKGROUND, false));

                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (radioGroup.getCheckedRadioButtonId()) {
                            case R.id.radio_one:
                                mSharedPreferences.edit().putInt(Utilities.MINUTES, 60).apply();
                                break;
                            case R.id.radio_two:
                                mSharedPreferences.edit().putInt(Utilities.MINUTES, 120).apply();
                                break;
                            case R.id.radio_eight:
                                mSharedPreferences.edit().putInt(Utilities.MINUTES, 480).apply();
                                break;
                            case R.id.radio_day:
                                mSharedPreferences.edit().putInt(Utilities.MINUTES, 1440).apply();
                                break;
                            case R.id.radio_two_days:
                                mSharedPreferences.edit().putInt(Utilities.MINUTES, 2880).apply();
                                break;
                            case R.id.radio_three_days:
                                mSharedPreferences.edit().putInt(Utilities.MINUTES, 4320).apply();
                                break;
                        }

                        mSharedPreferences.edit().putBoolean(Utilities.RUN_IN_BACKGROUND, changeWallpaperSwitch.isChecked()).apply();

                        if (changeWallpaperSwitch.isChecked()) {
                            stopService(); // TODO In teoria non dovrebbe servire
                            startService();
                        } else {
                            stopService();
                        }

                        mDialog.dismiss();
                    }
                });

                mDialog = builder.setView(dialogView).create();
                mDialog.show();
            }
        });

        this.chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent()
                        .setType("image/*")
                        .setAction(Intent.ACTION_GET_CONTENT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
            }
        });

        this.gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View dialogView = View.inflate(getActivity() ,R.layout.dialog_delete, null);

                final Button deleteButton = (Button) dialogView.findViewById(R.id.deleteButton);
                final Button cancelButton = (Button) dialogView.findViewById(R.id.cancelButton);
                final ImageView deleteWallpaperImageView = (ImageView) dialogView.findViewById(R.id.deleteWallpaperImageView);

                Picasso.with(getActivity())
                        .load(imagesUri.get(position))
                        .into(deleteWallpaperImageView);

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteImage(imagesUri.get(position));
                        loadImages();
                        mDeleteDialog.dismiss();
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDeleteDialog.dismiss();
                    }
                });

                mDeleteDialog = builder.setView(dialogView).create();
                mDeleteDialog.show();

                return true;
            }
        });

        this.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent setAs = new Intent(Intent.ACTION_ATTACH_DATA);
                setAs.setDataAndType(imagesUri.get(position), "image/*");
                startActivityForResult(Intent.createChooser(setAs, getString(R.string.set_as)), 0);
            }
        });
    }

    private int getCheckedId() {
        switch (mSharedPreferences.getInt(Utilities.MINUTES, 120)) {
            case 60:
                return R.id.radio_one;
            case 120:
                return R.id.radio_two;
            case 480:
                return R.id.radio_eight;
            case 1440:
                return R.id.radio_day;
            case 2880:
                return R.id.radio_two_days;
            case 4320:
                return R.id.radio_three_days;
            default:
                return R.id.radio_two;
        }
    }

    private void deleteImage(Uri uri) {
        getActivity().getContentResolver().delete(getImageContentUri(getActivity(), new File(uri.getPath())), null, null);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == SELECT_PICTURE)
                if (data.getClipData() != null) {
                    ClipData clip = data.getClipData();
                    for (int i = 0; i < clip.getItemCount(); i++) {
                        try {
                            Utilities.saveImageToExternalStorage(getActivity(), clip.getItemAt(i).getUri().getLastPathSegment(), MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), clip.getItemAt(i).getUri()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        Utilities.saveImageToExternalStorage(getActivity(), data.getData().getLastPathSegment(), MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NullPointerException ne) {
                        ne.printStackTrace();
                    }
                }
        loadImages();
    }

    public void loadImages() {
        imagesUri.clear();
        File myDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Wallpapers");

        if (myDir.list() != null) {
            for (String list : myDir.list()) {
                Uri actualUri = Uri.parse("file://" + myDir + "/" + list);
                if (!(imagesUri.indexOf(actualUri) >= 0))
                    imagesUri.add(actualUri);
            }
            gridViewAdapter.notifyDataSetChanged();
        }
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

    void stopService() {
        ActivityManager am = (ActivityManager) getActivity().getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo next : runningAppProcesses) {
            String processName = getActivity().getPackageName() + ":service";
            String processName2 = getActivity().getPackageName() + ":wallService";

            if (next.processName.equals(processName) || next.processName.equals(processName2))
                Process.killProcess(next.pid);
        }
    }

    void startService() {
        getActivity().startService(new Intent(getActivity(), StartupService.class));
        mSharedPreferences.edit().putBoolean(Utilities.NOT_FIRST, false).apply();
    }

    public class GridViewAdapter extends ArrayAdapter<Uri> {
        private final Context context;
        private ArrayList<Uri> data = new ArrayList<Uri>();

        public GridViewAdapter(Context context,
                               ArrayList<Uri> data) {
            super(context, 0, data);
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SquaredImageView view = (SquaredImageView) convertView;
            if (view == null) {
                view = new SquaredImageView(context);
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }

            Picasso.with(context)
                    .load(data.get(position))
                    .fit()
                    .into(view);

            return view;
        }
    }
}
