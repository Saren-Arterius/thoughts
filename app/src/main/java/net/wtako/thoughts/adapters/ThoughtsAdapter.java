package net.wtako.thoughts.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.style.RelativeSizeSpan;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.wtako.thoughts.R;
import net.wtako.thoughts.Thoughts;
import net.wtako.thoughts.activities.SearchActivity;
import net.wtako.thoughts.activities.SubmitThoughtActivity;
import net.wtako.thoughts.data.Thought;
import net.wtako.thoughts.fragments.SearchThoughtsFragment;
import net.wtako.thoughts.handlers.ImageGetter;
import net.wtako.thoughts.handlers.ThoughtActionMenuSetup;
import net.wtako.thoughts.interfaces.ILoadMore;
import net.wtako.thoughts.utils.StringUtils;
import net.wtako.thoughts.widgets.TouchTextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;

public class ThoughtsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static Bypass sBypass;
    private final LayoutInflater mLayoutInflater;
    private final ILoadMore mLoadMoreCallback;
    private List<Thought> mDataset = new ArrayList<>();
    private Context mCtx;
    private SparseBooleanArray mExpanded = new SparseBooleanArray();
    private boolean mHasMore = true;
    private int mCurrentPage;

    public ThoughtsAdapter(Context ctx, ILoadMore loadMore) {
        mCtx = ctx;
        mLayoutInflater = LayoutInflater.from(ctx);
        mLoadMoreCallback = loadMore;
    }

    public synchronized static Bypass getBypassInstance(Context ctx) {
        if (sBypass != null) {
            return sBypass;
        }
        sBypass = new Bypass(ctx);
        return sBypass;
    }

    @Override
    public int getItemViewType(int position) {
        if (position + (mCtx instanceof SubmitThoughtActivity ? 0 : 1) == getItemCount()) {
            return mHasMore ? 1 : 2;
        }
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        switch (viewType) {
            case 2:
                vh = new NoMoreViewHolder(mLayoutInflater.inflate(R.layout.item_no_more,
                        parent, false));
                break;
            case 1:
                vh = new LoadMoreViewHolder(mLayoutInflater.inflate(R.layout.item_load_more_progress,
                        parent, false));
                break;
            default:
                vh = new ThoughtsViewHolder(mLayoutInflater.inflate(R.layout.item_thought,
                        parent, false));
                break;
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ThoughtsViewHolder) {
            Thought thought = mDataset.get(position);
            ThoughtsViewHolder tvh = (ThoughtsViewHolder) holder;
            tvh.feedWith(mCtx, thought, mExpanded.get(position));
        } else if (holder instanceof LoadMoreViewHolder && mHasMore) {
            mLoadMoreCallback.loadMore(mCurrentPage);
        } else if (holder instanceof NoMoreViewHolder) {
            ((NoMoreViewHolder) holder).mNoMoreResults.setText(mCtx.getString(R.string.there_is_no_more_results));
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size() + (mCtx instanceof SubmitThoughtActivity ? 0 : 1);
    }

    public List<Thought> getDataset() {
        return mDataset;
    }

    public void onLoadMoreFail() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoadMoreCallback.loadMore(mCurrentPage);
            }
        }, 3000L);
    }

    public void onLoadMoreSuccess() {
        mCurrentPage++;
    }

    public void setHasMore(boolean hasMore) {
        this.mHasMore = hasMore;
    }

    public void expandThought(int position) {
        if (position >= mDataset.size()) {
            return;
        }
        mExpanded.append(position, true);
        notifyItemChanged(position);
    }

    public void reset() {
        mHasMore = true;
        mCurrentPage = 0;
        mDataset.clear();
        mExpanded.clear();
        notifyDataSetChanged();
    }

    public static class ThoughtsViewHolder extends RecyclerView.ViewHolder {

        private final ImageGetter mImageGetter;
        private final Float mScale;
        @Bind(R.id.thought_id)
        TextView mID;
        @Bind(R.id.thought_title)
        TextView mTitle;
        @Bind(R.id.thought_content)
        TextView mContent;
        @Bind(R.id.thought_hashtags)
        TextView mHashTags;
        @Bind(R.id.thought_rating)
        TextView mRating;
        @Bind(R.id.thought_post_date)
        TextView mPostDate;
        @Bind(R.id.thought_action_menu)
        ImageView mActionMenu;

        public ThoughtsViewHolder(View parent) {
            super(parent);
            ButterKnife.bind(this, parent);
            mScale = Float.valueOf(Thoughts.getSP(parent.getContext()).getString("thoughts_font_size", "1"));
            mImageGetter = new ImageGetter(mContent);
        }

        @SuppressLint("SetTextI18n")
        public void feedWith(final Context ctx, final Thought thought, boolean expanded) {
            CharSequence content;
            if (expanded) {
                mTitle.setSingleLine(false);
                mHashTags.setSingleLine(false);
                mContent.setMaxLines(Integer.MAX_VALUE);
                content = ThoughtsAdapter.getBypassInstance(ctx)
                        .markdownToSpannable(thought.getContent(), mImageGetter);
                mContent.setMovementMethod(TouchTextView.LocalLinkMovementMethod.getInstance());
            } else {
                content = ThoughtsAdapter.getBypassInstance(ctx)
                        .markdownToSpannable(thought.getContent());
            }
            if (mScale != 1f && content instanceof Spannable) {
                ((Spannable) content).setSpan(new RelativeSizeSpan(mScale), 0, content.length() - 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            mID.setText("#" + thought.getID());
            mTitle.setText(thought.getTitle());

            mContent.setText(content);

            mHashTags.setText(StringUtils.hashTagsHuman(thought.getHashTags()));
            mHashTags.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu menu = new PopupMenu(ctx, mHashTags);
                    for (String hashTag : thought.getHashTags()) {
                        menu.getMenu().add("#" + hashTag);
                    }
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Intent intent = new Intent(ctx, SearchActivity.class);
                            intent.putExtra(SearchThoughtsFragment.ARG_PREFILL, item.getTitle());
                            ctx.startActivity(intent);
                            return true;
                        }
                    });
                    menu.show();
                }
            });
            mRating.setText((thought.getRating() > 0 ? "+" : "") + thought.getRating());
            mPostDate.setText(StringUtils.timeDiffHuman(thought.getDate(), new Date()));

            new ThoughtActionMenuSetup.Builder(ctx, mActionMenu).setThought(thought).build().doSetup();
        }
    }

    public static class LoadMoreViewHolder extends RecyclerView.ViewHolder {

        public LoadMoreViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class NoMoreViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_no_more_results)
        TextView mNoMoreResults;

        public NoMoreViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


}