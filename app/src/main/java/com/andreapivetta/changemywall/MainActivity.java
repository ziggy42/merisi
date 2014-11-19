package com.andreapivetta.changemywall;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andreapivetta.changemywall.fragments.HomeFragment;
import com.andreapivetta.changemywall.fragments.WallpapersFragment;


public class MainActivity extends ActionBarActivity {

    private ImageButton searchImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new MyFragmentPagerAdapter());

        searchImageButton = (ImageButton) findViewById(R.id.searchButton);
        searchImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                overridePendingTransition(R.anim.push_left_in, 0);
            }
        });
    }

    void searchDown() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) searchImageButton.getLayoutParams();
        ValueAnimator downAnimator = ValueAnimator.ofInt(params.bottomMargin, -120);
        downAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                searchImageButton.requestLayout();
            }
        });
        downAnimator.setDuration(300);
        downAnimator.start();
    }

    void searchUp() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) searchImageButton.getLayoutParams();
        ValueAnimator upAnimator = ValueAnimator.ofInt(params.bottomMargin, 20);
        upAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                searchImageButton.requestLayout();
            }
        });
        upAnimator.setDuration(300);
        upAnimator.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem shareItem = menu.findItem(R.id.action_share);
        ShareActionProvider mShareActionProvider = (ShareActionProvider)
                MenuItemCompat.getActionProvider(shareItem);
        mShareActionProvider.setShareIntent(getDefaultIntent());

        return super.onCreateOptionsMenu(menu);
    }

    private Intent getDefaultIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT,
                "Take a look at this amazing app " +
                        "https://play.google.com/store/apps/details?id=com.andreapivetta.changemywall");
        intent.setType("text/plain");
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_about) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = View.inflate(MainActivity.this, R.layout.dialog_about, null);

            final TextView egTwitterTextView = (TextView) dialogView.findViewById(R.id.egzoTwitter);
            egTwitterTextView.setMovementMethod(LinkMovementMethod.getInstance());

            final TextView mTwitterTextView = (TextView) dialogView.findViewById(R.id.textView3);
            mTwitterTextView.setMovementMethod(LinkMovementMethod.getInstance());

            final ImageButton heartImageButton = (ImageButton) dialogView.findViewById(R.id.heartImageButton);
            heartImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://alpha.wallhaven.cc/"));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            });

            builder.setView(dialogView).create().show();
        } else if (item.getItemId() == R.id.action_rate) {
            Intent i = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://www.amazon.com/Andrea-Pivetta-Merisi/dp/B00PPP5JYC/ref=sr_1_11?ie=UTF8&qid=1416429180&sr=8-11&keywords=merisi"));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 3;
        private Fragment mCurrentFragment;

        public MyFragmentPagerAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    searchDown();
                    return HomeFragment.newInstance();
                case 1:
                    return WallpapersFragment.newInstance(Utilities.TOP);
                case 2:
                    searchUp();
                    return WallpapersFragment.newInstance(Utilities.RANDOM);
                default:
                    return HomeFragment.newInstance();
            }
        }

        public CharSequence getPageTitle(int position) {
            return getResources().getStringArray(R.array.tabs_names)[position];
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (mCurrentFragment != object) {
                mCurrentFragment = ((Fragment) object);
            }

            if (position == 0) {
                searchDown();

                if(((HomeFragment) mCurrentFragment).gridViewAdapter != null)
                    ((HomeFragment) mCurrentFragment).loadImages();

            }
            if (position == 1) searchUp();

            super.setPrimaryItem(container, position, object);
        }
    }
}
