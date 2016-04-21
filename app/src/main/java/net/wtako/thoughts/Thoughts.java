package net.wtako.thoughts;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

public class Thoughts extends Application {

    /*
    Thought(id, title, content, date, accessToken)
    Vote(ip, thoughtID, score)
    HashTag(id, name)
    ThoughtHashTag(thoughtID, hashTagID)

    Listing: GET /list/:type/:timespan/:page -> {success: bool, hasMore: bool, thoughts: [{id: int, title: String, content: String, hashTags: [Strings], rating: int, date: Date}]}
    Get thought: GET /thought/:id -> {success: bool, thought: {id: int, title: String, content: String, hashTags: [Strings], rating: int, date: Date}}
    New thought: PUT /thought <- {id: int, title: String, content: String, hashTags: [Strings]} -> {success: bool, thought: {id: int, title: String, content: String, hashTags: [Strings], rating: int, date: Date, adminToken: String}}
    Edit thought: POST /thought/:id/:adminToken <- {id: int, title: String, content: String, hashTags: [Strings]} -> {success: bool, thought: {id: int, title: String, content: String, hashTags: [Strings], rating: int, date: Date}}
    Delete thought: DELETE /thought/:id/:adminToken -> {success: bool}

    Upvote, Downvote: POST /vote/:up_or_down/:id -> {success: bool, id: int, new_score: int}
    Search: POST /search <- {query: String, page: int} -> {success: bool, hasMore: bool, thoughts: [{id: int, title: String, content: String, hashTags: [Strings], rating: int, date: Date}]}
     */

    // public static final String AUTHORITY = "http://192.168.0.100:3000";
    public static final String AUTHORITY = "https://thoughts.wtako.net";
    public static final Gson sGson = new Gson();
    private static Context sInstance;
    private static SharedPreferences sSharedPreferences;

    public static Context getContextInstance() {
        return sInstance;
    }

    public static SharedPreferences getSP(Context mCtx) {
        if (sSharedPreferences == null) {
            sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mCtx == null ? sInstance : mCtx);
        }
        return sSharedPreferences;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Fabric.with(this, new Crashlytics());
        sInstance = getApplicationContext();
    }

}
