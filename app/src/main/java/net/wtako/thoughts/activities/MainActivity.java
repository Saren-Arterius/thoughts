package net.wtako.thoughts.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import net.wtako.thoughts.R;
import net.wtako.thoughts.Thoughts;
import net.wtako.thoughts.data.FeaturedType;
import net.wtako.thoughts.data.TimeSpan;
import net.wtako.thoughts.fragments.FeaturedThoughtListFragment;
import net.wtako.thoughts.interfaces.IHasTimeSpan;
import net.wtako.thoughts.interfaces.IRefreshable;
import net.wtako.thoughts.interfaces.IUpdateThought;
import net.wtako.thoughts.utils.StringUtils;
import net.wtako.thoughts.widgets.RegisteredPagerAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

public class MainActivity extends BaseThoughtActivity implements IHasTimeSpan, IRefreshable,
        NavigationView.OnNavigationItemSelectedListener, IUpdateThought {

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @Bind(R.id.container)
    ViewPager mViewPager;
    @Bind(R.id.fab)
    FloatingActionButton mFab;
    @Bind(R.id.ctl)
    CollapsingToolbarLayout mCtl;
    @Bind(R.id.tabs)
    TabLayout mTabLayout;
    @State
    TimeSpan mTimeSpan = TimeSpan.W;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mCtl.setTitleEnabled(false);
        mTabLayout = ButterKnife.findById(this, R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        if (getIntent().getBooleanExtra("show_latest", false)) {
            mViewPager.setCurrentItem(FeaturedType.LATEST.ordinal(), false);
        }
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SubmitThoughtActivity.class));
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mFab.show();
            }
        });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = ButterKnife.findById(this, R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Thoughts.tryStartGCM(this);
    }


    @Override
    public int getThoughtActionMenuID() {
        return R.menu.thought_action;
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_set_time_span:
                setTimeSpan();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTimeSpan() {
        new MaterialDialog.Builder(this).title(R.string.set_time_span)
                .items(StringUtils.getStringArray(getApplicationContext(), TimeSpan.values()))
                .positiveText(R.string.ok)
                .itemsCallbackSingleChoice(getTimeSpan().ordinal(), new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        if (TimeSpan.values()[i] != mTimeSpan) {
                            mTimeSpan = TimeSpan.values()[i];
                            refresh();
                        }
                        return false;
                    }
                }).show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_favourite:
                startActivity(new Intent(getApplicationContext(), FavouriteActivity.class));
                break;
            case R.id.nav_my_thoughts:
                startActivity(new Intent(getApplicationContext(), MyThoughtsActivity.class));
                break;
            case R.id.nav_about:
                showAbout();
                break;
            case R.id.nav_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;
            case R.id.nav_search:
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                break;
        }

        // TODO: GCM
        // TODO: Push notification

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showAbout() {
        try {
            new MaterialDialog.Builder(this).title(R.string.about)
                    .content(getString(R.string.about_format, getString(R.string.app_version_format,
                            getString(R.string.internal_app_name),
                            getPackageManager().getPackageInfo(getPackageName(), 0).versionName)))
                    .positiveText(R.string.ok)
                    .show();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public TimeSpan getTimeSpan() {
        return mTimeSpan;
    }

    @Override
    public void refresh() {
        for (int pos = 0; pos < mSectionsPagerAdapter.getCount(); pos++) {
            Fragment fragment = mSectionsPagerAdapter.getRegisteredFragment(pos);
            if (fragment != null && fragment instanceof IRefreshable) {
                ((IRefreshable) fragment).refresh();
            }
        }
    }

    @Override
    public void updateScore(int id, int newScore) {
        for (int pos = 0; pos < mSectionsPagerAdapter.getCount(); pos++) {
            Fragment fragment = mSectionsPagerAdapter.getRegisteredFragment(pos);
            if (fragment != null && fragment instanceof IUpdateThought) {
                ((IUpdateThought) fragment).updateScore(id, newScore);
            }
        }
    }

    @Override
    public void deleteThought(int id) {

    }

    public class SectionsPagerAdapter extends RegisteredPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return FeaturedThoughtListFragment.newInstance(FeaturedType.values()[position]);
        }

        @Override
        public int getCount() {
            return FeaturedType.values().length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return FeaturedType.values()[position].name();
        }

    }
}
