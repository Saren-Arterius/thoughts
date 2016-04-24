package net.wtako.thoughts.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import net.wtako.thoughts.R;
import net.wtako.thoughts.Thoughts;
import net.wtako.thoughts.activities.MyThoughtsActivity;
import net.wtako.thoughts.adapters.ThoughtsAdapter;
import net.wtako.thoughts.data.MyThought;
import net.wtako.thoughts.data.Thought;
import net.wtako.thoughts.interfaces.ILoadMore;
import net.wtako.thoughts.utils.Database;
import net.wtako.thoughts.utils.ItemClickSupport;
import net.wtako.thoughts.utils.MiscUtils;
import net.wtako.thoughts.utils.RequestSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PreviewThoughtFragment extends BaseFragment implements ILoadMore {


    protected LinearLayoutManager mLayoutManager;
    protected ThoughtsAdapter mAdapter;
    @Bind(R.id.thought_list)
    RecyclerView mRecyclerView;
    @Bind(R.id.submit_button)
    Button mSubmitButton;

    public static Fragment newInstance() {
        return new PreviewThoughtFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_preview_thought, container, false);
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

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.submit_thought)
                        .content(R.string.submit_thought_confirm)
                        .positiveText(R.string.ok)
                        .negativeText(R.string.cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Thought thought = mAdapter.getDataset().get(0);
                                if (thought != null && thought instanceof MyThought) {
                                    submitThought((MyThought) thought);
                                }
                            }
                        }).show();
            }
        });
        return rootView;
    }

    private void submitThought(MyThought thought) {
        String url = Thoughts.AUTHORITY;
        if (thought.getAdminToken() != null) {
            url += String.format("/thought/%s/%s", thought.getID(), thought.getAdminToken());
        } else {
            url += "/thought";
        }
        final ProgressDialog pd = ProgressDialog.show(getActivity(),
                getString(R.string.submitting_thought), getString(R.string.please_wait));
        try {
            JsonObjectRequest request = new JsonObjectRequest(thought.getAdminToken() != null ? Request.Method.POST : Request.Method.PUT,
                    url, new JSONObject(Thoughts.sGson.toJson(thought)), new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    pd.dismiss();
                    try {
                        MyThought myThought = Thoughts.sGson.fromJson(response.getString("thought"), MyThought.class);
                        //MyGcmListenerService.mNotified.append(myThought.getID(), true);
                        Database.getMyThoughts(getActivity()).prependOrBringToFront(myThought).save(true);
                        Intent intent = new Intent(getContext(), MyThoughtsActivity.class);
                        intent.putExtra("new", myThought.getID());
                        startActivity(intent);
                        getActivity().finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Snackbar.make(ButterKnife.findById(getActivity(), android.R.id.content),
                                getString(R.string.network_problem_message), Snackbar.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    pd.dismiss();
                    Snackbar.make(ButterKnife.findById(getActivity(), android.R.id.content),
                            MiscUtils.msgFromVolleyError(getActivity(), error), Snackbar.LENGTH_LONG).show();
                }
            });
            RequestSingleton.add(getContext(), request);
        } catch (JSONException e) {
            pd.dismiss();
            e.printStackTrace();
            Snackbar.make(ButterKnife.findById(getActivity(), android.R.id.content),
                    getString(R.string.unknown_error), Snackbar.LENGTH_LONG).show();
        }
    }

    public void feedThought(MyThought thought) {
        mAdapter.reset();
        mAdapter.getDataset().add(thought);
        mAdapter.setHasMore(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void loadMore(int page) {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.setHasMore(false);
                mAdapter.notifyDataSetChanged();
            }
        });
    }
}
