package com.hp.myidea.guidedroid.app;

import java.util.Locale;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.hp.myidea.guidedroid.R;
import com.hp.myidea.guidedroid.base.Communicator;
import com.hp.myidea.guidedroid.service.BluetoothReceiver;

public class GuideDroidActivity extends FragmentActivity {
    private static final String TAG = GuideDroidActivity.class.getSimpleName();

    public static final String GUIDE_DROID_PREFS = "GuideDroidSharedPrefs";

    private Communicator communicator;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_droid);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent == null) {
            Log.d(TAG, "NAO RECEBEU NADA!!!");
            return;
        }
        String scheme = intent.getScheme();
        Bundle bundle = intent.getExtras();
        Uri uri = intent.getData();
        if (uri == null) {
            Log.d(TAG, "NAO RECEBEU URI!!!");
            return;
        }
        String building = uri.getQueryParameter("building");
        String point = uri.getQueryParameter("point");
        String lat = uri.getQueryParameter("lat");
        String lng = uri.getQueryParameter("lng");
        
        Log.d(TAG, "scheme: " + scheme + ", building: " + building + ", point: " + point + ", lat: " + lat + ", lng: " + lng);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.guide_droid, menu);
        return true;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
            case 0:
                fragment = new UltrasonicSectionFragment();
                break;
            case 1:
                fragment = new NavigationSectionFragment();
                break;
            default:
                break;
            }
            return fragment;

        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
            case 0:
                return getString(R.string.title_section1).toUpperCase(l);
            case 1:
                return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }
}
