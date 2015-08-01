package com.tixon.portlab.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.purplebrain.adbuddiz.sdk.AdBuddiz;

public class ActivityMain extends ActionBarActivity {
    Toolbar toolbar;
    ViewPager pager;
    PagerAdapter pagerAdapter;

    private static final int NUM_PAGES = 2;
    private static final int APP_VERSION = 2;
    private static final String LOG_TAG = "myLogs";
    private static final String KEY_POSITION = "key_position";
    private static final String KEY_USE_COUNTER = "useCounter";
    private static final String KEY_VERSION = "key_version";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //AdBuddiz.setPublisherKey("8d15e4c0-70c5-40ed-a2c4-df4567e62a48");
        //AdBuddiz.cacheAds(this); // this = current Activity

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setLogo(R.drawable.ic_launcher);

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePageFragment(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setPageTransformer(true, new ZoomOutPageTransformer());

        if(Build.VERSION.SDK_INT < 21) {
            pager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        } else {
            pager.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);

            //Changing color of the system bar
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.primary700));
        }

        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        int intUseCounter = sPref.getInt(KEY_USE_COUNTER, 0);
        int version = sPref.getInt(KEY_VERSION, APP_VERSION-1);

        if(APP_VERSION != version) {
            Intent intent_greetings = new Intent(getApplicationContext(), ActivityGreetings.class);
            startActivity(intent_greetings);
            finish();
            ed.putInt(KEY_VERSION, APP_VERSION);
            ed.apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //AdBuddiz.showAd(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                this.startActivity(new Intent(this, ActivitySettings.class));
                break;
            case R.id.action_help:
                this.startActivity(new Intent(this, ActivityHelp.class));
                break;
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }

    //Pager adapter class
    private class ScreenSlidePageFragment extends FragmentStatePagerAdapter {
        public ScreenSlidePageFragment(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return FragmentCalculator.newInstance();
                case 1: return FragmentMatrix.newInstance();
                default: return FragmentCalculator.newInstance();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            CharSequence title;
            String[] viewsArray = getResources().getStringArray(R.array.views_array);
            switch (position) {
                case 0: title = viewsArray[0]; break;
                case 1: title = viewsArray[1]; break;
                default: title = viewsArray[0]; break;
            }
            return title;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
