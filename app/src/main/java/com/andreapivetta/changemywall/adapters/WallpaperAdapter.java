package com.andreapivetta.changemywall.adapters;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.andreapivetta.changemywall.FullWallpaperActivity;
import com.andreapivetta.changemywall.R;
import com.andreapivetta.changemywall.wallpapers.Wallpaper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.ViewHolder> {

    private ArrayList<Wallpaper> mDataset;
    private Context context;

    public WallpaperAdapter(ArrayList<Wallpaper> wallpaperList, Context context) {
        this.mDataset = wallpaperList;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView wallpaperImageView;
        public FrameLayout wallpaperFrameLayout;

        public ViewHolder(View container) {
            super(container);

            this.wallpaperImageView = (ImageView) container.findViewById(R.id.wallpaperImageView);
            this.wallpaperFrameLayout = (FrameLayout) container.findViewById(R.id.wallpaperFrameLayout);
        }
    }

    @Override
    public WallpaperAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wallpaper, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Picasso.with(context)
                .load(mDataset.get(position).thumbURL)
                .placeholder(R.drawable.placeholder)
                .into(holder.wallpaperImageView);

        holder.wallpaperFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, FullWallpaperActivity.class);
                i.putExtra("WALL", mDataset.get(position))
                        .putExtra("THUMB", ((BitmapDrawable) holder.wallpaperImageView.getDrawable()).getBitmap());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
