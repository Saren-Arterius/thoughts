package net.wtako.thoughts.data;

import java.util.Date;
import java.util.List;

public class MyThought extends Thought {

    String adminToken;

    public MyThought(int mID, String mTitle, String mContent, int mRating, List<String> mHashTags, Date mDate) {
        super(mID, mTitle, mContent, mRating, mHashTags, mDate);
    }

    public String getAdminToken() {
        return adminToken;
    }

    public void setAdminToken(String adminToken) {
        this.adminToken = adminToken;
    }

}
