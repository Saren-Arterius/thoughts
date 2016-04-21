package net.wtako.thoughts.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import net.wtako.thoughts.R;
import net.wtako.thoughts.Thoughts;
import net.wtako.thoughts.activities.MainActivity;
import net.wtako.thoughts.adapters.ThoughtsAdapter;
import net.wtako.thoughts.data.Thought;
import net.wtako.thoughts.utils.RequestSingleton;
import net.wtako.thoughts.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class ThoughtWidgetProvider extends android.appwidget.AppWidgetProvider {

    @Override
    public void onUpdate(final Context ctx, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        String url = Thoughts.AUTHORITY + "/thought";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    Thought thought = Thoughts.sGson.fromJson(response.getString("thought"), Thought.class);

                    String hashTags = StringUtils.hashTagsHuman(thought.getHashTags());
                    CharSequence content = ThoughtsAdapter.getBypassInstance(ctx)
                            .markdownToSpannable(thought.getContent());
                    String date = StringUtils.timeDiffHuman(thought.getDate(), new Date());

                    for (int appWidgetId : appWidgetIds) {
                        // Create an Intent to launch ExampleActivity
                        Intent intent = new Intent(ctx, MainActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);

                        // Get the layout for the App Widget and attach an on-click listener
                        // to the button
                        RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget_thought);

                        views.setOnClickPendingIntent(R.id.thought_content, pendingIntent);
                        views.setTextViewText(R.id.thought_id, "#" + thought.getID());
                        views.setTextViewText(R.id.thought_title, thought.getTitle());
                        views.setTextViewText(R.id.thought_content, content);
                        views.setTextViewText(R.id.thought_hashtags, hashTags);
                        views.setTextViewText(R.id.thought_rating, (thought.getRating() > 0 ? "+" : "") + thought.getRating());
                        views.setTextViewText(R.id.thought_post_date, date);

                        // Tell the AppWidgetManager to perform an update on the current app widget
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestSingleton.add(ctx, request);

    }

}