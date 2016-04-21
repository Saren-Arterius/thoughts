package net.wtako.thoughts.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import net.wtako.thoughts.R;
import net.wtako.thoughts.fragments.SearchThoughtsFragment;

public class SearchActivity extends BaseThoughtActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Fragment myFragment = SearchThoughtsFragment
                    .newInstance(getIntent().getStringExtra(SearchThoughtsFragment.ARG_PREFILL));
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, myFragment).commit();
        }
    }

    @Override
    public int getThoughtActionMenuID() {
        return R.menu.thought_action;
    }
}
