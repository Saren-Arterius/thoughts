package net.wtako.thoughts.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;

import net.wtako.thoughts.R;
import net.wtako.thoughts.fragments.MyThoughtListFragment;
import net.wtako.thoughts.fragments.ThoughtListFragment;
import net.wtako.thoughts.interfaces.IUpdateThought;

import butterknife.ButterKnife;

public class MyThoughtsActivity extends BaseThoughtActivity implements IUpdateThought {

    private ThoughtListFragment mMyFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mMyFragment = MyThoughtListFragment.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, mMyFragment).commit();
        }

        int newID = getIntent().getIntExtra("new", -1);
        if (newID != -1) {
            Snackbar.make(ButterKnife.findById(this, android.R.id.content),
                    getString(R.string.submit_success, newID), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public int getThoughtActionMenuID() {
        return R.menu.thought_owner_action;
    }

    @Override
    public void updateScore(int id, int newScore) {
        mMyFragment.updateScore(id, newScore);
    }

    @Override
    public void deleteThought(int id) {
        mMyFragment.deleteThought(id);
    }
}
