package net.wtako.thoughts.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import net.wtako.thoughts.R;
import net.wtako.thoughts.Thoughts;
import net.wtako.thoughts.adapters.ThoughtsAdapter;
import net.wtako.thoughts.data.SearchQuery;
import net.wtako.thoughts.data.Thought;
import net.wtako.thoughts.interfaces.ILoadMore;
import net.wtako.thoughts.utils.ItemClickSupport;
import net.wtako.thoughts.utils.MiscUtils;
import net.wtako.thoughts.utils.RequestSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

public class SearchThoughtsFragment extends BaseFragment implements ILoadMore {


    public static final String ARG_PREFILL = "prefill";
    protected LinearLayoutManager mLayoutManager;
    protected ThoughtsAdapter mAdapter;
    protected Handler mHandler = new Handler();

    @Bind(R.id.thought_list)
    RecyclerView mRecyclerView;
    @Bind(R.id.search_keywords)
    AutoCompleteTextView mSearchKeywords;

    @State
    String mSearchKeywordsString;


    public static Fragment newInstance(String prefill) {
        SearchThoughtsFragment fragment = new SearchThoughtsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PREFILL, prefill);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        mSearchKeywordsString = mSearchKeywords.toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_thought_search, container, false);
        ButterKnife.bind(this, rootView);
        if (getArguments() != null && mSearchKeywordsString == null) {
            mSearchKeywordsString = getArguments().getString(ARG_PREFILL);
        }
        mSearchKeywords.setText(mSearchKeywordsString);
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

        mSearchKeywords.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mHandler.removeCallbacksAndMessages(null);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.reset();
                    }
                }, 700L);
            }
        });
        return rootView;
    }

    @Override
    public void loadMore(final int page) {
        if (!isAdded()) {
            return;
        }
        final String query = mSearchKeywords.getText().toString();
        if (query.isEmpty()) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            return;
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
        }

        String url = Thoughts.AUTHORITY + "/search";

        JsonObjectRequest request = null;
        try {
            request = new JsonObjectRequest(Request.Method.POST, url,
                    new JSONObject(Thoughts.sGson.toJson(new SearchQuery(query, page))),
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                final List<Thought> list = new ArrayList<>();
                                JSONArray jArray = response.getJSONArray("thoughts");
                                if (jArray != null) {
                                    for (int i = 0; i < jArray.length(); i++) {
                                        list.add(Thoughts.sGson.fromJson(jArray.getString(i), Thought.class));
                                    }
                                }
                                final boolean hasMore = response.getBoolean("hasMore");
                                mRecyclerView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.getDataset().addAll(list);
                                        mAdapter.setHasMore(hasMore);
                                        mAdapter.notifyDataSetChanged();
                                        mAdapter.onLoadMoreSuccess();
                                    }
                                });
                            } catch (JSONException e) {
                                Snackbar.make(ButterKnife.findById(getActivity(), android.R.id.content),
                                        R.string.network_problem_message, Snackbar.LENGTH_SHORT).show();
                                mAdapter.onLoadMoreFail();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Snackbar.make(ButterKnife.findById(getActivity(), android.R.id.content),
                            MiscUtils.msgFromVolleyError(getActivity(), error), Snackbar.LENGTH_SHORT).show();
                    mAdapter.onLoadMoreFail();
                }
            });
        } catch (JSONException e) {
            Snackbar.make(ButterKnife.findById(getActivity(), android.R.id.content),
                    R.string.network_problem_message, Snackbar.LENGTH_SHORT).show();
            mAdapter.onLoadMoreFail();
        }
        RequestSingleton.add(getContext(), request);
    }
}
