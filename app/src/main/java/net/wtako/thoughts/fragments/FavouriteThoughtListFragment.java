package net.wtako.thoughts.fragments;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.SparseBooleanArray;
import android.view.View;

import net.wtako.thoughts.R;
import net.wtako.thoughts.data.Thought;
import net.wtako.thoughts.utils.Database;

public class FavouriteThoughtListFragment extends ThoughtListFragment {

    private final SparseBooleanArray mDontRemove = new SparseBooleanArray();

    public static ThoughtListFragment newInstance() {
        return new FavouriteThoughtListFragment();
    }

    @Override
    public void loadMore(int page) {
        if (!isAdded()) {
            return;
        }
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        if (viewHolder.getAdapterPosition() >= mAdapter.getDataset().size()) {
                            mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                            return;
                        }
                        FavouriteThoughtListFragment.this.onSwiped(mAdapter.getDataset().get(viewHolder.getAdapterPosition()));
                    }
                }).attachToRecyclerView(mRecyclerView);
                mAdapter.getDataset().addAll(Database.getFavouriteThoughts(getContext()).getSavedData());
                mAdapter.setHasMore(false);
                mAdapter.notifyDataSetChanged();
                mAdapter.onLoadMoreSuccess();
            }
        });

    }

    private void onSwiped(final Thought thought) {
        Snackbar.make(mRecyclerView, R.string.item_removed, Snackbar.LENGTH_LONG).setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                int index = mAdapter.getDataset().indexOf(thought);
                if (index == -1) {
                    return;
                }
                synchronized (mDontRemove) {
                    try {
                        if (mDontRemove.get(thought.getID())) {
                            mAdapter.notifyItemChanged(index);
                        } else {
                            mAdapter.getDataset().remove(index);
                            mAdapter.notifyItemRemoved(index);
                            Database.getFavouriteThoughts(getContext()).getSavedData().remove(thought);
                            Database.getFavouriteThoughts(getContext()).save(true);
                        }
                        mDontRemove.clear();
                    } catch (Exception e) {
                        mAdapter.reset();
                    }
                }
            }
        }).setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                synchronized (mDontRemove) {
                    mDontRemove.put(thought.getID(), true);
                }
            }
        }).show();
    }
}
