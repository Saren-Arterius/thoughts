package net.wtako.thoughts.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import net.wtako.thoughts.Thoughts;
import net.wtako.thoughts.data.FeaturedType;
import net.wtako.thoughts.data.Thought;
import net.wtako.thoughts.data.TimeSpan;
import net.wtako.thoughts.interfaces.IHasTimeSpan;
import net.wtako.thoughts.interfaces.IRefreshable;
import net.wtako.thoughts.utils.MiscUtils;
import net.wtako.thoughts.utils.RequestSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class FeaturedThoughtListFragment extends ThoughtListFragment implements IRefreshable {

    private static final String ARG_FEATURED_TYPE = "featured_type";
    private FeaturedType mFeaturedType;

    public static ThoughtListFragment newInstance(FeaturedType type) {
        ThoughtListFragment fragment = new FeaturedThoughtListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FEATURED_TYPE, type.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        mFeaturedType = FeaturedType.valueOf(getArguments().getString(ARG_FEATURED_TYPE));
        return rootView;
    }

    @Override
    public void loadMore(final int page) {
        if (!isAdded()) {
            return;
        }
        TimeSpan ts;
        if (getActivity() instanceof IHasTimeSpan) {
            ts = ((IHasTimeSpan) getActivity()).getTimeSpan();
        } else {
            ts = TimeSpan.W;
        }

        String url = Thoughts.AUTHORITY + String.format("/list/%s/%s/%s", mFeaturedType.name().toLowerCase(), ts.name().toLowerCase(), page);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

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
                    e.printStackTrace();
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
        RequestSingleton.add(getContext(), request);
    }

    @Override
    public void refresh() {
        if (isAdded() && mAdapter != null) {
            mAdapter.reset();
        }
    }
}
