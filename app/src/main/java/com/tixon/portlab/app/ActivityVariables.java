package com.tixon.portlab.app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ActivityVariables extends ActionBarActivity {

    static final String TAG = "myLogs";
    static final String KEY_SAVED_PAGE_NUMBER = "savedPageNumber";
    static final int PAGE_COUNT = 3;

    ViewPager pager;
    PagerAdapter pagerAdapter;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_variables);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.help_menu_title);
        toolbar.setLogo(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pager = (ViewPager) findViewById(R.id.pager_variables);
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setPageTransformer(true, new ZoomOutPageTransformer());

        if(Build.VERSION.SDK_INT < 21) {
            pager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        } else {
            pager.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);

            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.primary700));
        }

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected, position = " + position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        if(savedInstanceState != null) {
            Log.d(TAG, "ActivityVariables: onCreate: savedInstanceState.getInt(saved page): " + savedInstanceState.getInt(KEY_SAVED_PAGE_NUMBER));
            pager.setCurrentItem(savedInstanceState.getInt(KEY_SAVED_PAGE_NUMBER), true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                int SUCCESS_RESULT = 1;
                setResult(SUCCESS_RESULT, new Intent());
                finish();
                //overridePendingTransition(R.anim.right_slow, R.anim.right_fast);
                //Intent intent = new Intent(this, ActivityMain.class);
                //startActivity(intent);
                break;
            case R.id.home:
                onBackPressed();
                break;
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_MENU:
                Intent intent = new Intent(this, ActivityVariables.class);
                startActivityForResult(intent, 1);
                //overridePendingTransition(R.anim.left_fast, R.anim.left_slow);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            CharSequence title;
            String[] viewsArray = getResources().getStringArray(R.array.views_array_variables);
            switch (position) {
                case 0: title = viewsArray[0]; break;
                case 1: title = viewsArray[1]; break;
                case 2: title = viewsArray[2]; break;
                default: title = viewsArray[0]; break;
            }
            return title;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.right_slow, R.anim.right_fast);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "ActivityVariables: onSaveInstanceState: pager.getCurrentItem(): " + pager.getCurrentItem());
        outState.putInt(KEY_SAVED_PAGE_NUMBER, pager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }
}
