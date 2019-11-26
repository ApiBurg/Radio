package com.vadimfedchuk1994gmail.radio;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vadimfedchuk1994gmail.radio.intarfaces.SongCallBack;
import com.vadimfedchuk1994gmail.radio.network.GetPlaySong;
import com.vadimfedchuk1994gmail.radio.service.PlayerRadioService;
import com.vadimfedchuk1994gmail.radio.utils.ExternalOnClickListener;

import java.util.Timer;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements SongCallBack, View.OnClickListener {

    private Context mContext;
    private boolean jobService = false;
    private CircleImageView control;
    private TextView currentTrack;
    private String currentTrackString;

    private PlayerRadioService.PlayerServiceBinder playerServiceBinder;
    private MediaControllerCompat mediaController;
    private boolean playing;

    private Timer timer;
    private GetPlaySong getPlaySong;
    private ServiceConnection mServiceConnection;
    private MediaControllerCompat.Callback callback;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initParams();
        initView();
        lock();
        if(jobService) startServicePlayRadio();
        Log.d("MyLog", "onCreate");
    }

    @Override
    public void songCallBack(final String songName, boolean state) {
        if(state){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(currentTrackString == null) currentTrackString = currentTrack.getText().toString();
                    if(currentTrackString.length() != songName.length()){
                        currentTrack.setText(songName);
                        currentTrackString = songName;
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        if (mediaController != null){
            if(!playing){
                ContextCompat.startForegroundService(mContext, new Intent(mContext, PlayerRadioService.class));
                control.setImageResource(R.drawable.pause_button);
                mediaController.getTransportControls().play();
            } else {
                control.setImageResource(R.drawable.play_button);
                mediaController.getTransportControls().pause();
            }
        } else {
            control.setImageResource(R.drawable.pause_button);
            startServicePlayRadio();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean th = true;
                    while (th){
                        if(mediaController != null){
                            mediaController.getTransportControls().play();
                            th = false;
                        }
                    }
                }
            }).start();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getPlaySong.play();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getPlaySong.stop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!playing & !jobService) {
            if(mediaController == null) return;
            mediaController.getTransportControls().stop();
            try {
                unbindService(mServiceConnection);
                stopService(new Intent(mContext, PlayerRadioService.class));
            } catch (IllegalArgumentException e) {
                Log.d("MyLog", String.valueOf(e));
            }
        }

        playerServiceBinder = null;
        if (mediaController != null) {
            mediaController.unregisterCallback(callback);
            mediaController = null;
        }
        getPlaySong.stop();
        timer.cancel();
        if(wakeLock != null) wakeLock.release();
        try {
            unbindService(mServiceConnection);
        } catch (IllegalArgumentException e) {
            Log.d("MyLog", String.valueOf(e));
        }
    }

    private void initParams() {
        mContext = this;
        jobService = isMyServiceRunning();
        SongCallBack songCallBack = this;
        getPlaySong = new GetPlaySong(songCallBack);
        timer = new Timer();
        timer.schedule(getPlaySong, 0, 5000);
    }

    private void initView(){
       CircleImageView vkLogo = findViewById(R.id.vk_imageView);
        CircleImageView instagramLogo = findViewById(R.id.instagram_imageView);
        CircleImageView infoLogo = findViewById(R.id.info_imageView);
        control = findViewById(R.id.control_imageView);

        if(jobService){
            control.setImageResource(R.drawable.pause_button);
        } else {
            control.setImageResource(R.drawable.play_button);
        }

        currentTrack = findViewById(R.id.textView_current_track);
        currentTrack.setSelected(true);
        CircleImageView viberLogo = findViewById(R.id.viber_imageView);
        CircleImageView telegramLogo = findViewById(R.id.telegram_imageView);
        CircleImageView whatsAppLogo = findViewById(R.id.whatsapp_imageView);
        ExternalOnClickListener externalOnClickListener = new ExternalOnClickListener(mContext);
        vkLogo.setOnClickListener(externalOnClickListener);
        instagramLogo.setOnClickListener(externalOnClickListener);
        infoLogo.setOnClickListener(externalOnClickListener);
        viberLogo.setOnClickListener(externalOnClickListener);
        telegramLogo.setOnClickListener(externalOnClickListener);
        whatsAppLogo.setOnClickListener(externalOnClickListener);
        control.setOnClickListener(this);
    }

    private void startServicePlayRadio() {
        Intent intent = new Intent(mContext, PlayerRadioService.class);
        ContextCompat.startForegroundService(mContext, intent);
        callback = new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                if (state == null) return;
                playing = state.getState() == PlaybackStateCompat.STATE_PLAYING;
                if(playing) {
                    control.setImageResource(R.drawable.pause_button);
                } else {
                    control.setImageResource(R.drawable.play_button);
                }
            }
        };

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                playerServiceBinder = (PlayerRadioService.PlayerServiceBinder) service;
                try {
                    mediaController = new MediaControllerCompat(MainActivity.this, playerServiceBinder.getMediaSessionToken());
                    mediaController.registerCallback(callback);
                    callback.onPlaybackStateChanged(mediaController.getPlaybackState());
                } catch (RemoteException e) {
                    mediaController = null;
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                playerServiceBinder = null;
                if (mediaController != null) {
                    mediaController.unregisterCallback(callback);
                    mediaController = null;
                }
            }
        };

        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (PlayerRadioService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

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
