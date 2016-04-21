package net.wtako.thoughts.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import net.wtako.thoughts.R;
import net.wtako.thoughts.Thoughts;
import net.wtako.thoughts.data.MyThought;
import net.wtako.thoughts.fragments.EditThoughtFragment;
import net.wtako.thoughts.fragments.PreviewThoughtFragment;
import net.wtako.thoughts.utils.RequestSingleton;
import net.wtako.thoughts.widgets.RegisteredPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SubmitThoughtActivity extends BaseThoughtActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    @Bind(R.id.container)
    ViewPager mViewPager;
    @Bind(R.id.tabs)
    TabLayout mTabLayout;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private MyThought mMyThought;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_thought);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    updatePreview();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabLayout.setupWithViewPager(mViewPager);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String myThoughtJson = extras.getString("my_thought");
            mMyThought = Thoughts.sGson.fromJson(myThoughtJson, MyThought.class);
            String url = Thoughts.AUTHORITY + String.format("/thought/%s", mMyThought.getID());
            final ProgressDialog pd = ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait));
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        MyThought dbThought = Thoughts.sGson.fromJson(response.getString("thought"), MyThought.class);
                        dbThought.setAdminToken(mMyThought.getAdminToken());
                        mMyThought = dbThought;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    pd.dismiss();
                    fillToEditor();
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    pd.dismiss();
                    fillToEditor();
                }
            });
            RequestSingleton.add(this, request);
        }
    }

    private void fillToEditor() {
        Fragment editFrag = mSectionsPagerAdapter.getRegisteredFragment(0);
        if (editFrag != null && editFrag instanceof EditThoughtFragment) {
            ((EditThoughtFragment) editFrag).feedThought(mMyThought);
        }
    }

    private MyThought getThoughtFromEditor() {
        Fragment editFrag = mSectionsPagerAdapter.getRegisteredFragment(0);
        if (editFrag != null && editFrag instanceof EditThoughtFragment) {
            return ((EditThoughtFragment) editFrag).getThought();
        }
        return null;
    }

    private void updatePreview() {
        Fragment previewFrag = mSectionsPagerAdapter.getRegisteredFragment(1);
        MyThought thought = getThoughtFromEditor();
        if (thought == null) {
            return;
        }
        if (mMyThought != null) {
            thought.setID(mMyThought.getID());
            thought.setAdminToken(mMyThought.getAdminToken());
        }
        if (previewFrag != null && previewFrag instanceof PreviewThoughtFragment) {
            ((PreviewThoughtFragment) previewFrag).feedThought(thought);
        }
    }

    @Override
    public int getThoughtActionMenuID() {
        return R.menu.thought_null_action;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends RegisteredPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return EditThoughtFragment.newInstance();
                case 1:
                    return PreviewThoughtFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.edit);
                case 1:
                    return getString(R.string.preview);
            }
            return null;
        }
    }
}
