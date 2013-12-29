package no.dega.couchpotatoremote;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.Locale;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create settings for and initialise nostra13's ImageLoader
        //See: https://github.com/nostra13/Android-Universal-Image-Loader
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().
                cacheInMemory(true).cacheOnDisc(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config);

        //Build preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //Add listener for swiping between sections
        mViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

        //For each section, create + add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Settings
            case R.id.action_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;
            //Add movie
            case R.id.action_add_movie:
                Intent addMovie = new Intent(this, AddMovieActivity.class);
                startActivity(addMovie);
                return true;
            //Refresh movie list
            case R.id.action_refresh:
                //Refresh the Wanted list
                ((MovieListFragment) mSectionsPagerAdapter.getRegisteredFragment(0)).refresh();
                //Refresh the Manage list
                ((MovieListFragment) mSectionsPagerAdapter.getRegisteredFragment(1)).refresh();
                Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
                return true;
            //View the log
            case R.id.action_viewlog:
                Intent log = new Intent(this, LogActivity.class);
                startActivity(log);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //When selected, switch to appropriate view
    @Override
    public void onTabSelected(Tab tab,
                              FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }
    @Override
    public void onTabReselected(Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        //Fragments are registered so we can retrieve them based on their position
        final SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>(2);

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        //Instantiate fragment for the given page
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new MovieListFragment();
            Bundle args = new Bundle();
            //Pos 0 = wanted. Pos 1 = manage
            if (position == 0) {
                args.putBoolean("isWanted", true);
            } else {
                args.putBoolean("isWanted", false);
            }
            fragment.setArguments(args);
            return fragment;
        }

        //Registration/deregistration of fragments
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }
        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_wanted).toUpperCase(l);
                case 1:
                    return getString(R.string.title_manage).toUpperCase(l);
            }
            return null;
        }
    }

}
