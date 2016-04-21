package net.wtako.thoughts.fragments;

import net.wtako.thoughts.utils.Database;

public class MyThoughtListFragment extends ThoughtListFragment {

    public static ThoughtListFragment newInstance() {
        return new MyThoughtListFragment();
    }

    @Override
    public void loadMore(int page) {
        if (!isAdded()) {
            return;
        }
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.getDataset().addAll(Database.getMyThoughts(getContext()).getSavedData());
                mAdapter.setHasMore(false);
                mAdapter.notifyDataSetChanged();
                mAdapter.onLoadMoreSuccess();
            }
        });
    }
}
