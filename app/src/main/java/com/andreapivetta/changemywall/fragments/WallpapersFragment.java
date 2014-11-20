package com.andreapivetta.changemywall.fragments;


import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;

import com.andreapivetta.changemywall.MainActivity;
import com.andreapivetta.changemywall.R;
import com.andreapivetta.changemywall.Utilities;
import com.andreapivetta.changemywall.adapters.WallpaperAdapter;
import com.andreapivetta.changemywall.wallpapers.Wallpaper;
import com.andreapivetta.changemywall.wallpapers.WallpapersService;

import java.util.ArrayList;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class WallpapersFragment extends Fragment {

    public ViewStub mViewStub;
    private WallpapersService service;
    private ArrayList<Wallpaper> wallpaperList = new ArrayList<Wallpaper>();
    private WallpaperAdapter mWallpaperAdapter;
    private GridLayoutManager mGridLayoutManager;
    private boolean loading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount, currentPage = 1;
    private SmoothProgressBar mSmoothProgressbar;
    private ProgressBar loadingProgressBar;
    private View rootView;
    private RecyclerView mRecyclerView;
    private AlertDialog mDialog;

    private int kind;

    public WallpapersFragment() {
    }

    public static WallpapersFragment newInstance(int kind) {
        WallpapersFragment f = new WallpapersFragment();
        Bundle args = new Bundle();
        args.putInt("KIND", kind);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        kind = getArguments().getInt("KIND");
        service = new WallpapersService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_wallpapers, container, false);

        int nColumns;
        if (getResources().getBoolean(R.bool.isTablet)) {
            nColumns = 3;
        } else {
            nColumns = 2;
        }

        mWallpaperAdapter = new WallpaperAdapter(wallpaperList, getActivity());
        mGridLayoutManager = new GridLayoutManager(getActivity(), nColumns);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.topListRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setAdapter(mWallpaperAdapter);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = mGridLayoutManager.getChildCount();
                totalItemCount = mGridLayoutManager.getItemCount();
                pastVisibleItems = mGridLayoutManager.findFirstVisibleItemPosition() + 1;

                if (loading) {
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        loading = false;
                        mSmoothProgressbar.setVisibility(View.VISIBLE);
                        new setUpWallpapersArray().execute(null, null, null);
                    }
                }

                if(dy > 0) {
                    if(((MainActivity) getActivity()).isUp) {
                        ((MainActivity) getActivity()).searchDown();
                    }
                } else {
                    if(!((MainActivity) getActivity()).isUp) {
                        ((MainActivity) getActivity()).searchUp();
                    }
                }

            }
        });

        mSmoothProgressbar = (SmoothProgressBar) rootView.findViewById(R.id.randomSmoothProgressbar);
        loadingProgressBar = (ProgressBar) rootView.findViewById(R.id.loadingProgressBar);

        if (wallpaperList.size() > 0)
            loadingProgressBar.setVisibility(View.GONE);

        mViewStub = ((ViewStub) rootView.findViewById(R.id.noWifiViewStub));
        new setUpWallpapersArray().execute(null, null, null);
        return rootView;
    }

    public void noConnectionHandler() {
        if (wallpaperList.size() <= 0) {
            currentPage = 0;
            loadingProgressBar.setVisibility(View.GONE);
            mSmoothProgressbar.setVisibility(View.GONE);

            mViewStub.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);

            rootView.findViewById(R.id.retryButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    new setUpWallpapersArray().execute(null, null, null);
                    mViewStub.setVisibility(View.GONE);
                }
            });
        } else {
            mSmoothProgressbar.setVisibility(View.GONE);
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View dialogView = View.inflate(getActivity(), R.layout.noconnection, null);

            dialogView.findViewById(R.id.retryButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSmoothProgressbar.setVisibility(View.VISIBLE);
                    new setUpWallpapersArray().execute(null, null, null);
                    mDialog.dismiss();
                }
            });

            builder.setCancelable(false);

            mDialog = builder.setView(dialogView).create();
            mDialog.show();
        }
    }

    public class setUpWallpapersArray extends AsyncTask<Void, Void, Boolean> {
        private ArrayList<Wallpaper> newWallpapersList = new ArrayList<Wallpaper>();

        @Override
        protected Boolean doInBackground(Void... uris) {
            switch (kind) {
                case Utilities.RANDOM:
                    newWallpapersList = service.getRandomWallpapers(currentPage);
                    break;
                case Utilities.TOP:
                    newWallpapersList = service.getWallpapersTopList(currentPage);
                    break;
                default:
                    newWallpapersList = service.getRandomWallpapers(currentPage);
                    break;
            }

            currentPage++;

            if (newWallpapersList != null && newWallpapersList.size() > 0) {
                wallpaperList.addAll(newWallpapersList);
                return true;
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                mWallpaperAdapter.notifyDataSetChanged();
                loadingProgressBar.setVisibility(View.GONE);
                mSmoothProgressbar.setVisibility(View.GONE);
                loading = true;
            } else {
                noConnectionHandler();
            }
        }
    }
}
