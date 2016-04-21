package net.wtako.thoughts.handlers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import net.wtako.thoughts.R;
import net.wtako.thoughts.Thoughts;
import net.wtako.thoughts.activities.BaseThoughtActivity;
import net.wtako.thoughts.activities.SubmitThoughtActivity;
import net.wtako.thoughts.data.MyThought;
import net.wtako.thoughts.data.Thought;
import net.wtako.thoughts.interfaces.IUpdateThought;
import net.wtako.thoughts.utils.Database;
import net.wtako.thoughts.utils.MiscUtils;
import net.wtako.thoughts.utils.RequestSingleton;
import net.wtako.thoughts.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;

public class ThoughtActionMenuSetup implements PopupMenu.OnMenuItemClickListener {
    private final View mSnackbarAnchor;
    private final Activity mActivity;
    Thought mThought;
    ImageView mActionMenuButton;

    public ThoughtActionMenuSetup(Context ctx, ImageView button) {
        mActionMenuButton = button;
        mActivity = (Activity) ctx;
        mSnackbarAnchor = ButterKnife.findById(mActivity, android.R.id.content);
    }

    public void doSetup() {
        mActionMenuButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mActionMenuButton.setColorFilter(mActivity.getResources().getColor(R.color.material_grey_600));
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mActionMenuButton.setColorFilter(mActivity.getResources().getColor(R.color.cardview_dark_background));
                }
                return false;
            }
        });
        mActionMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(mActivity, mActionMenuButton);
                menu.inflate(((BaseThoughtActivity) mActivity).getThoughtActionMenuID());
                menu.setOnMenuItemClickListener(ThoughtActionMenuSetup.this);
                menu.show();
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (mSnackbarAnchor == null) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.action_upvote:
                vote(true);
                break;
            case R.id.action_downvote:
                vote(false);
                break;
            case R.id.action_favourite:
                if (!Database.getFavouriteThoughts(mActivity).contains(mThought)) {
                    Database.getFavouriteThoughts(mActivity).prependOrBringToFront(mThought).save(true);
                    Snackbar.make(mSnackbarAnchor, mActivity.getString(R.string.added_to_favourite, mThought.getID()),
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(mSnackbarAnchor, R.string.thought_exists_in_favourite,
                            Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_share:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.putExtra(Intent.EXTRA_TEXT, mActivity.getString(R.string.share_format,
                        mThought.getID(),
                        mThought.getTitle(),
                        mThought.getContent(),
                        StringUtils.hashTagsHuman(mThought.getHashTags())));
                sharingIntent.setType("text/plain");
                mActivity.startActivity(sharingIntent);
                break;
            case R.id.action_edit:
                if (mThought instanceof MyThought) {
                    Intent intent = new Intent(mActivity, SubmitThoughtActivity.class);
                    intent.putExtra("my_thought", Thoughts.sGson.toJson(mThought));
                    mActivity.startActivity(intent);
                }
                break;
            case R.id.action_delete:
                if (mThought instanceof MyThought) {
                    new MaterialDialog.Builder(mActivity).title(R.string.delete_thought)
                            .content(mActivity.getString(R.string.delete_thought_format, mThought.getID()))
                            .positiveText(R.string.ok)
                            .negativeText(R.string.cancel)
                            .positiveColorRes(R.color.md_edittext_error)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    deleteThought();
                                }
                            }).show();
                }
                break;
        }
        return false;
    }

    private void deleteThought() {
        String url = Thoughts.AUTHORITY + String.format("/thought/%s/%s", mThought.getID(),
                ((MyThought) mThought).getAdminToken());
        final ProgressDialog pd = ProgressDialog.show(mActivity,
                mActivity.getString(R.string.deleting, mThought.getID())
                , mActivity.getString(R.string.please_wait));
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                pd.dismiss();
                Database.getFavouriteThoughts(mActivity).getSavedData().remove(mThought);
                Database.getFavouriteThoughts(mActivity).save(true);
                if (mActivity instanceof IUpdateThought) {
                    ((IUpdateThought) mActivity).deleteThought(mThought.getID());
                }
                Snackbar.make(mSnackbarAnchor, mActivity.getString(R.string.delete_success,
                        mThought.getID()), Snackbar.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();
                Snackbar.make(mSnackbarAnchor, MiscUtils.msgFromVolleyError(mActivity, error),
                        Snackbar.LENGTH_LONG).show();
            }
        });
        RequestSingleton.add(mActivity, request);
    }

    private void vote(final boolean up) {
        String url = Thoughts.AUTHORITY + String.format("/vote/%s/%s", up ? "up" : "down", mThought.getID());
        final ProgressDialog pd = ProgressDialog.show(mActivity, mActivity.getString(R.string.voting) +
                (up ? " up" : " down"), mActivity.getString(R.string.please_wait));
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                pd.dismiss();
                try {
                    int newScore = response.getInt("new_score");
                    if (mActivity instanceof IUpdateThought) {
                        ((IUpdateThought) mActivity).updateScore(mThought.getID(), newScore);
                    }
                    Snackbar.make(mSnackbarAnchor, mActivity.getString(up ? R.string.upvote_success
                            : R.string.downvote_success, mThought.getID()), Snackbar.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Snackbar.make(mSnackbarAnchor, mActivity.getString(R.string.network_problem_message),
                            Snackbar.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();
                Snackbar.make(mSnackbarAnchor, MiscUtils.msgFromVolleyError(mActivity, error),
                        Snackbar.LENGTH_LONG).show();
            }
        });
        RequestSingleton.add(mActivity, request);
    }

    public static class Builder {

        final ThoughtActionMenuSetup mInstance;

        public Builder(Context ctx, ImageView actionMenuButton) {
            mInstance = new ThoughtActionMenuSetup(ctx, actionMenuButton);
        }

        public Builder setThought(Thought thought) {
            mInstance.mThought = thought;
            return this;
        }

        public ThoughtActionMenuSetup build() {
            return mInstance;
        }
    }


}