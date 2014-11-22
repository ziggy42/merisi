package com.andreapivetta.changemywall;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andreapivetta.changemywall.adapters.WallpaperAdapter;
import com.andreapivetta.changemywall.wallpapers.WallpapersService;
import com.andreapivetta.changemywall.wallpapers.Wallpaper;

import java.util.ArrayList;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;


public class SearchActivity extends ActionBarActivity {

    private ArrayList<Wallpaper> wallpaperList = new ArrayList<Wallpaper>();
    private WallpaperAdapter mWallpaperAdapter;
    private GridLayoutManager mSearchGridLayoutManager;
    private EditText searchEditText;
    private ProgressBar loadingSearchProgressBar;
    private WallpapersService service;
    private int currentPage;
    private SmoothProgressBar mSmoothProgressbar;

    private boolean loading = true;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

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

        service = new WallpapersService();

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.searchedListRecyclerView);
        mWallpaperAdapter = new WallpaperAdapter(wallpaperList, this);
        mSearchGridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.nCol));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mSearchGridLayoutManager);
        mRecyclerView.setAdapter(mWallpaperAdapter);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = mSearchGridLayoutManager.getChildCount();
                totalItemCount = mSearchGridLayoutManager.getItemCount();
                pastVisiblesItems = mSearchGridLayoutManager.findFirstVisibleItemPosition() + 1;

                if (loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = false;
                        mSmoothProgressbar.setVisibility(View.VISIBLE);
                        new searchWallpapers().execute(null, null, null);
                    }
                }
            }
        });

        ImageButton searchImageButton = (ImageButton) findViewById(R.id.searchImageButton);
        searchImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });

        this.loadingSearchProgressBar = (ProgressBar) findViewById(R.id.loadingSearchProgressBar);
        this.searchEditText = (EditText) findViewById(R.id.searchEditText);
        this.searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                search();
                return true;
            }
        });

        mSmoothProgressbar = (SmoothProgressBar) findViewById(R.id.searchSmoothProgressbar);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void search() {
        if(searchEditText.getText().length() > 0) {
            loadingSearchProgressBar.setVisibility(View.VISIBLE);

            currentPage = 1;
            wallpaperList.clear();
            mWallpaperAdapter.notifyDataSetChanged();

            new searchWallpapers().execute(null, null, null);

            InputMethodManager imm = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        }
    }

    public class searchWallpapers extends AsyncTask<Void, Void, Boolean> {
        private ArrayList<Wallpaper> newWalllist = new ArrayList<Wallpaper>();

        @Override
        protected Boolean doInBackground(Void... uris) {
            newWalllist = service.getWallpapersSearched(currentPage, searchEditText.getText().toString());
            currentPage++;

            if (newWalllist != null && newWalllist.size() > 0) {
                wallpaperList.addAll(newWalllist);
                return true;
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                loadingSearchProgressBar.setVisibility(View.GONE);
                mWallpaperAdapter.notifyDataSetChanged();
                mSmoothProgressbar.setVisibility(View.GONE);
                loading = true;
            } else {
                mSmoothProgressbar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), getString(R.string.no_more_wallpapers), Toast.LENGTH_SHORT).show();
                loadingSearchProgressBar.setVisibility(View.GONE);
            }
        }
    }
}
