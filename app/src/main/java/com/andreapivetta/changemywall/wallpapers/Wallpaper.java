package com.andreapivetta.changemywall.wallpapers;

import android.opengl.GLES10;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.andreapivetta.changemywall.Utilities;

import javax.microedition.khronos.opengles.GL10;

public class Wallpaper implements Parcelable{
    public String thumbURL;
    public String fullURL;
    public int height;
    public int width;

    public Wallpaper(String thumbURL) {
        this.thumbURL = thumbURL;

        String url = "http://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-" + thumbURL.substring(thumbURL.indexOf("-") + 1);
        if (!Utilities.exists(url)) {
            url = url.replace(".jpg", ".png");
        }

        this.fullURL = url;
    }

    public Wallpaper(String thumbURL, String res) {
        this(thumbURL);

        int index = res.indexOf('x');
        width = Integer.parseInt(res.substring(0, index).trim());
        height = Integer.parseInt(res.substring(index + 1, res.length()).trim());
    }

    public String getResolutionString() {
        return width + " x " + height;
    }

    public boolean isResTooHigh() {
        if(Build.VERSION.SDK_INT < 21){
            int[] maxSize = new int[1];
            GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxSize, 0);

            return (maxSize[0] < width) || (maxSize[0] < height);
        }

        /*
            Let's assume all devices that runs Lollipop can display bitmaps with size 4096 or less
            I'm too lazy to actually resolve this problem
         */

        return (4096 < width) || (4096 < height);
    }

    public Wallpaper(Parcel in) {
        String[] data = new String[4];

        in.readStringArray(data);
        this.thumbURL = data[0];
        this.fullURL = data[1];
        this.width = Integer.parseInt(data[2]);
        this.height = Integer.parseInt(data[3]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{this.thumbURL, this.fullURL, this.width + "", this.height + ""});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Wallpaper createFromParcel(Parcel in) {
            return new Wallpaper(in);
        }

        public Wallpaper[] newArray(int size) {
            return new Wallpaper[size];
        }
    };
}
