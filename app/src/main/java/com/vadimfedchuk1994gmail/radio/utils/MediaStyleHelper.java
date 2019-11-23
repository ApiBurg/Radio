package com.vadimfedchuk1994gmail.radio.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import com.vadimfedchuk1994gmail.radio.R;

public class MediaStyleHelper {

    public static NotificationCompat.Builder from(
            Context context, MediaSessionCompat mediaSession) {
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.logo);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "25");
        builder
                .setSmallIcon(R.drawable.ic_action_notification)
                .setContentTitle("Пульс-Радио")
                .setContentText("103.8 FM")
                .setLargeIcon(icon)
                .setDeleteIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        return builder;
    }
}
