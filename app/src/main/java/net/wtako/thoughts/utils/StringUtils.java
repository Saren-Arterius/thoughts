package net.wtako.thoughts.utils;

import android.content.Context;

import net.wtako.thoughts.interfaces.IHasStringRes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StringUtils {

    public static String timeDiffHuman(Date d1, Date d2) {
        if (d1 == null || d2 == null) {
            return "N/A";
        }
        long diff = Math.abs(d1.getTime() - d2.getTime());
        long year = diff / (365 * 24 * 60 * 60 * 1000L);
        if (year >= 2) {
            return String.valueOf(year) + "y";
        }
        long month = diff / (30 * 24 * 60 * 60 * 1000L);
        if (month >= 6) {
            return String.valueOf(month) + "M";
        }
        long day = diff / (24 * 60 * 60 * 1000L);
        if (day >= 2) {
            return String.valueOf(day) + "d";
        }
        long hour = diff / (60 * 60 * 1000L);
        if (hour >= 2) {
            return String.valueOf(hour) + "h";
        }
        long minute = diff / (60 * 1000L);
        if (minute >= 1) {
            return String.valueOf(minute) + "m";
        }
        return "< 1m";
    }

    public static List<String> getHashTagsFromString(String hashTags) {
        List<String> hashTagList = new ArrayList<>();
        for (String hashTag : hashTags.split(",|\\s+")) {
            hashTag = hashTag.trim();
            if (hashTag.length() == 0) {
                continue;
            }
            if (hashTag.charAt(0) == '#') {
                hashTag = hashTag.substring(1);
            }
            hashTagList.add(hashTag);
        }
        return hashTagList;
    }

    public static String hashTagsHuman(List<String> hashTags) {
        String buffer = "";
        for (String hashTag : hashTags) {
            buffer += "#" + hashTag + " ";
        }
        return buffer.trim();
    }

    public static String[] getStringArray(Context ctx, IHasStringRes[] enums) {
        String[] strings = new String[enums.length];
        for (int i = 0; i < enums.length; i++) {
            strings[i] = enums[i].getString(ctx);
        }
        return strings;
    }

    public static String normalize(Object obj) {
        if (obj == null) {
            return "";
        }
        return String.valueOf(obj);
    }
}
