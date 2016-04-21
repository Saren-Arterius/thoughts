package net.wtako.thoughts.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import net.wtako.thoughts.R;
import net.wtako.thoughts.data.MyThought;
import net.wtako.thoughts.data.Thought;
import net.wtako.thoughts.utils.StringUtils;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

public class EditThoughtFragment extends BaseFragment {

    @Bind(R.id.title)
    EditText mTitle;
    @Bind(R.id.content)
    EditText mContent;
    @Bind(R.id.hashtags)
    EditText mHashTags;

    @State
    String mTitleString;
    @State
    String mContentString;
    @State
    String mHashTagsString;

    public static Fragment newInstance() {
        return new EditThoughtFragment();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveToStates();
    }

    private void saveToStates() {
        mTitleString = StringUtils.normalize(mTitle.getText().toString());
        mContentString = StringUtils.normalize(mContent.getText().toString());
        mHashTagsString = StringUtils.normalize(mHashTags.getText().toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_thought, container, false);
        ButterKnife.bind(this, rootView);
        mTitle.setText(mTitleString);
        mContent.setText(mContentString);
        mHashTags.setText(mHashTagsString);
        return rootView;
    }

    public MyThought getThought() {
        saveToStates();
        return new MyThought(0, mTitleString, mContentString, 0,
                StringUtils.getHashTagsFromString(mHashTagsString), new Date());
    }

    public void feedThought(Thought thought) {
        mTitle.setText(thought.getTitle());
        mContent.setText(thought.getContent());
        mHashTags.setText(StringUtils.hashTagsHuman(thought.getHashTags()));
        saveToStates();
    }
}
