package net.wtako.thoughts.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import net.wtako.thoughts.R;
import net.wtako.thoughts.fragments.FavouriteThoughtListFragment;
import net.wtako.thoughts.fragments.ThoughtListFragment;
import net.wtako.thoughts.interfaces.IUpdateThought;

public class FavouriteActivity extends BaseThoughtActivity implements IUpdateThought {

    private ThoughtListFragment mMyFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mMyFragment = FavouriteThoughtListFragment.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, mMyFragment).commit();
        }
    }

    @Override
    public int getThoughtActionMenuID() {
        return R.menu.thought_favourite_action;
    }

    @Override
    public void updateScore(int id, int newScore) {
        mMyFragment.updateScore(id, newScore);
    }

    @Override
    public void deleteThought(int id) {

    }
}
