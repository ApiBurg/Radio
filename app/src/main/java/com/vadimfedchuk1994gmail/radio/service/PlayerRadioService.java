package com.vadimfedchuk1994gmail.radio.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.PriorityTaskManager;
import com.vadimfedchuk1994gmail.radio.MainActivity;
import com.vadimfedchuk1994gmail.radio.R;
import com.vadimfedchuk1994gmail.radio.utils.MediaStyleHelper;

import static com.google.android.exoplayer2.DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS;
import static com.google.android.exoplayer2.DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS;
import static com.google.android.exoplayer2.DefaultLoadControl.DEFAULT_MAX_BUFFER_MS;
import static com.google.android.exoplayer2.DefaultLoadControl.DEFAULT_MIN_BUFFER_MS;

public class PlayerRadioService extends Service  {

    private Context mContext;
    private SimpleExoPlayer exoPlayer;
    private MediaSessionCompat mediaSession;
    final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            | PlaybackStateCompat.ACTION_STOP
                            | PlaybackStateCompat.ACTION_PAUSE
                            | PlaybackStateCompat.ACTION_PLAY_PAUSE
                            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);

    private DataSource.Factory dataSourceFactory;
    private ExtractorsFactory extractorsFactory;
    private AudioManager audioManager;
    private int volumeMusic;
    private int currentState = PlaybackStateCompat.STATE_PAUSED;
    int notification_id = 12313;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mediaSession = new MediaSessionCompat(this, "PlayerService");
        mediaSession.setCallback(mediaSessionCallback);
        Intent mediaButtonIntent = new Intent(
                Intent.ACTION_MEDIA_BUTTON, Uri.parse("http://178.208.85.117:8000/puls"), mContext, MediaButtonReceiver.class);
        mediaSession.setMediaButtonReceiver(
                PendingIntent.getBroadcast(mContext, 0, mediaButtonIntent, 0));
        myStartForeground();
        initializationExoPlayer();
        lock();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myStartForeground();
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.release();
        exoPlayer.release();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PlayerServiceBinder();
    }

    public class PlayerServiceBinder extends Binder {
        public MediaSessionCompat.Token getMediaSessionToken() {
            return mediaSession.getSessionToken();
        }
    }

    private void initializationExoPlayer() {
        Handler mainHandler = new Handler();
        RenderersFactory renderersFactory = new DefaultRenderersFactory(getApplicationContext());
        TrackSelector trackSelector = new DefaultTrackSelector();
        PriorityTaskManager priorityTaskManager = new PriorityTaskManager();
        DefaultAllocator defaultAllocator = new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE);
        LoadControl loadControl =
                new DefaultLoadControl(defaultAllocator, 7000, 50000,
                        5500, 6500, C.LENGTH_UNSET,
                        true, priorityTaskManager);
        exoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, renderersFactory, trackSelector, loadControl);
        AudioAttributes.Builder builderAudio = new AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC);
        exoPlayer.setAudioAttributes(builderAudio.build());
        dataSourceFactory = new DefaultDataSourceFactory(mContext, "ExoplayerDemo");
        extractorsFactory = new DefaultExtractorsFactory();
        exoPlayer.setPlayWhenReady(true);
        MediaSource mediaSource =
                    new ExtractorMediaSource(Uri.parse("http://178.208.85.117:8000/puls"), dataSourceFactory,
                            extractorsFactory, mainHandler, null);
        exoPlayer.prepare(mediaSource, false, false);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) volumeMusic =  audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        exoPlayer.addListener(new Player.EventListener() {

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.d("MyLog", "onLoadingChanged! Показываем прогрес бар!");
                mediaSession.setPlaybackState(
                        stateBuilder.setState(PlaybackStateCompat.STATE_CONNECTING,
                                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if(playbackState == PlaybackStateCompat.STATE_PLAYING){
                    mediaSession.setPlaybackState(
                            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
                    registerReceiver(
                            becomingNoisyReceiver,
                            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
                }
            }
        });
    }


    MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlay() {
            int audioFocusResult = audioManager.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if(audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return;
            mediaSession.setActive(true);
            exoPlayer.setPlayWhenReady(true);
            currentState = PlaybackStateCompat.STATE_PLAYING;
            refreshNotificationAndForegroundStatus(currentState);
        }

        @Override
        public void onPause() {
            exoPlayer.setPlayWhenReady(false);
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            currentState = PlaybackStateCompat.STATE_PAUSED;
            refreshNotificationAndForegroundStatus(currentState);
            if(becomingNoisyReceiver != null){
                try {
                    unregisterReceiver(becomingNoisyReceiver);
                } catch (IllegalArgumentException e) {
                    Log.d("MyLog", String.valueOf(e));
                }
            }
        }

        @Override
        public void onStop() {
            Log.d("MyLog", "onStop! Останваливаем трансляцию!");
            exoPlayer.setPlayWhenReady(false);
            mediaSession.setActive(false);
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            currentState = PlaybackStateCompat.STATE_STOPPED;
            if(becomingNoisyReceiver != null){
                try {
                    unregisterReceiver(becomingNoisyReceiver);
                } catch (IllegalArgumentException e) {
                    Log.d("MyLog", String.valueOf(e));
                }
            }
            audioManager.abandonAudioFocus(audioFocusChangeListener);
            stopForeground(true);
            stopSelf();
        }

    };

    private void forcedPlay(){
        int audioFocusResult = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if(audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return;
        mediaSession.setActive(true);
        mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
        ExtractorMediaSource
                mediaSource =
                new ExtractorMediaSource(Uri.parse("http://178.208.85.117:8000/puls"),
                        dataSourceFactory, extractorsFactory, null, null);
        exoPlayer.prepare(mediaSource, false, false);
        exoPlayer.setPlayWhenReady(true);
        registerReceiver(
                becomingNoisyReceiver,
                new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        currentState = PlaybackStateCompat.STATE_PLAYING;
        refreshNotificationAndForegroundStatus(currentState);
    }

    private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeMusic, 0);
                            mediaSessionCallback.onPlay();
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 30, 0);
                            break;

                        default:
                            mediaSessionCallback.onPause();
                            mediaSession.setPlaybackState(
                                    stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
                            break;
                    }
                }
            };

    private void refreshNotificationAndForegroundStatus(int playbackState) {
        createNotificationChannel();
        switch (playbackState) {
            case PlaybackStateCompat.STATE_PLAYING:
                startForeground(notification_id, getNotification(currentState));
                break;

            case PlaybackStateCompat.STATE_PAUSED:
                NotificationManagerCompat.from(PlayerRadioService.this)
                        .notify(notification_id, getNotification(playbackState));
                stopForeground(false);
                break;

            default:
                stopForeground(true);
                break;
        }
    }

    private void myStartForeground(){
        createNotificationChannel();
        startForeground(notification_id, getNotification(currentState));
    }

    private Notification getNotification(int playbackState) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        NotificationCompat.Builder builder = MediaStyleHelper.from(mContext, mediaSession);

        if (playbackState == PlaybackStateCompat.STATE_PLAYING)
            builder.addAction(
                    new NotificationCompat.Action(
                            android.R.drawable.ic_media_pause, getString(R.string.pause),
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                                    this,
                                    PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        else
            builder.addAction(
                    new NotificationCompat.Action(
                            android.R.drawable.ic_media_play, getString(R.string.play),
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                                    this,
                                    PlaybackStateCompat.ACTION_PLAY_PAUSE)));

        builder.addAction(
                new NotificationCompat.Action(R.drawable.ic_notification_close, getString(R.string.previous),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                this, PlaybackStateCompat.ACTION_STOP)));

        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0,1)
                .setMediaSession(mediaSession.getSessionToken()));
        builder.setSmallIcon(R.drawable.ic_action_notification);
        builder.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        builder.setShowWhen(false);
        builder.setContentIntent(pendingIntent);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setOnlyAlertOnce(true);
        return builder.build();
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= 26){
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("25", getString(R.string.notification), importance);
            channel.setDescription(getString(R.string.notification_state_play));
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                mediaSessionCallback.onStop();
            }
        }
    };

    private void lock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyApp::MyWakelockTag");
        }
        wakeLock.acquire(10*60*1000L /*10 minutes*/);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.WifiLock wfl = null;
        if (wm != null) {
            wfl = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "sync_all_wifi");
        }
        if (wfl != null) {
            wfl.acquire();
        }
    }


}
