package net.wtako.thoughts.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.wtako.thoughts.R;
import net.wtako.thoughts.adapters.ThoughtsAdapter;
import net.wtako.thoughts.interfaces.ILoadMore;
import net.wtako.thoughts.interfaces.IUpdateThought;
import net.wtako.thoughts.utils.ItemClickSupport;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public abstract class ThoughtListFragment extends BaseFragment implements ILoadMore, IUpdateThought {


    protected LinearLayoutManager mLayoutManager;
    protected ThoughtsAdapter mAdapter;
    @Bind(R.id.thought_list)
    RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_thought_list, container, false);
        ButterKnife.bind(this, rootView);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new ThoughtsAdapter(getActivity(), this);
        mRecyclerView.setAdapter(mAdapter);

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                mAdapter.expandThought(position);
            }
        });
        return rootView;
    }

    @Override
    public void updateScore(int id, int newScore) {
        for (int pos = 0; pos < mAdapter.getDataset().size(); pos++) {
            if (mAdapter.getDataset().get(pos).getID() == id) {
                mAdapter.getDataset().get(pos).setRating(newScore);
                mAdapter.notifyItemChanged(pos);
                break;
            }
        }
    }

    @Override
    public void deleteThought(int id) {
        for (int pos = 0; pos < mAdapter.getDataset().size(); pos++) {
            if (mAdapter.getDataset().get(pos).getID() == id) {
                mAdapter.getDataset().remove(pos);
                mAdapter.notifyItemRemoved(pos);
                break;
            }
        }
    }
}