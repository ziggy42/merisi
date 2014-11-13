package com.andreapivetta.changemywall;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;


public class ZoomedActivity extends Activity {

    private PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoomed);

        ImageView zoomedImageView = (ImageView) findViewById(R.id.zoomedImageView);

        Picasso.with(this)
                .load(getIntent().getStringExtra("WALLPAPER"))
                .into(zoomedImageView);

        mAttacher = new PhotoViewAttacher(zoomedImageView);
        //mAttacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAttacher.cleanup();
    }
}
