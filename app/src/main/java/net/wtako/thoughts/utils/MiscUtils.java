package net.wtako.thoughts.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.android.volley.VolleyError;

import net.wtako.thoughts.R;

public class MiscUtils {

    public static int parseInt(String number, int defValue) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static int parseInt(String number) {
        return parseInt(number, 0);
    }

    public static void openURL(Context ctx, String url) {
        openURL(ctx, Uri.parse(url));
    }

    public static void openURL(Context ctx, Uri uri) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            ctx.startActivity(browserIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String msgFromVolleyError(Context ctx, VolleyError error) {
        error.printStackTrace();
        try {
            switch (error.networkResponse.statusCode) {
                case 401:
                    return ctx.getString(R.string.wrong_credentials);
                default:
                    if (error.networkResponse.data.length > 1000) {
                        return ctx.getString(R.string.network_problem_message);
                    }
                    return new String(error.networkResponse.data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ctx.getString(R.string.network_problem_message);
        }
    }

}
