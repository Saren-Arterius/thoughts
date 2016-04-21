package net.wtako.thoughts.utils;

import android.content.Context;

import com.google.gson.reflect.TypeToken;

import net.wtako.thoughts.data.MyThought;
import net.wtako.thoughts.data.Thought;

import java.util.List;

public class Database {

    private static DatabaseData<String> mSearchHistory;
    private static DatabaseData<String> mMonitoredHashTags;
    private static DatabaseData<Thought> mFavouriteThoughts;
    private static DatabaseData<MyThought> mMyThoughts;

    public static synchronized DatabaseData<String> getSearchHistory(Context ctx) {
        if (mSearchHistory != null) {
            return mSearchHistory;
        }
        mSearchHistory = new DatabaseData<>(ctx, "search_history", new TypeToken<List<String>>() {
        }.getType());
        return mSearchHistory;
    }

    public static synchronized DatabaseData<String> getMonitoredHashTags(Context ctx) {
        if (mMonitoredHashTags != null) {
            return mMonitoredHashTags;
        }
        mMonitoredHashTags = new DatabaseData<>(ctx, "monitored_hashtags", new TypeToken<List<String>>() {
        }.getType());
        return mMonitoredHashTags;
    }

    public static synchronized DatabaseData<Thought> getFavouriteThoughts(Context ctx) {
        if (mFavouriteThoughts != null) {
            return mFavouriteThoughts;
        }
        mFavouriteThoughts = new DatabaseData<>(ctx, "favourite_thoughts", new TypeToken<List<Thought>>() {
        }.getType());
        return mFavouriteThoughts;
    }

    public static synchronized DatabaseData<MyThought> getMyThoughts(Context ctx) {
        if (mMyThoughts != null) {
            return mMyThoughts;
        }
        mMyThoughts = new DatabaseData<>(ctx, "my_thoughts", new TypeToken<List<MyThought>>() {
        }.getType());
        return mMyThoughts;
    }

}