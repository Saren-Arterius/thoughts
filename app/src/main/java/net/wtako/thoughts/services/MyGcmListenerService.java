package net.wtako.thoughts.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.SparseBooleanArray;

import com.google.android.gms.gcm.GcmListenerService;

import net.wtako.thoughts.R;
import net.wtako.thoughts.Thoughts;
import net.wtako.thoughts.activities.MainActivity;

public class MyGcmListenerService extends GcmListenerService {

    private static final SparseBooleanArray mNotified = new SparseBooleanArray();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        if (mNotified.get(Integer.parseInt(data.getString("id")))) {
            return;
        }
        mNotified.append(Integer.parseInt(data.getString("id")), true);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("show_latest", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri soundUri;
        try {
            soundUri = Uri.parse(Thoughts.getSP(getBaseContext()).getString("notifications_new_message_ringtone", null));
        } catch (Exception ignored) {
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_new_releases_black_24dp)
                .setColor(getResources().getColor(R.color.colorAccent))
                .setContentTitle(getString(R.string.hashtag_matched, data.getString("hashtag")))
                .setContentText(getString(R.string.hashtag_matched_id_title, data.getString("id"), data.getString("title")))
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());

        if (Thoughts.getSP(getBaseContext()).getBoolean("notifications_new_message_vibrate", false)) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(250);
        }
    }
}