package com.vadimfedchuk1994gmail.radio.fragments;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.vadimfedchuk1994gmail.radio.Live;
import com.vadimfedchuk1994gmail.radio.R;
import com.vadimfedchuk1994gmail.radio.intarfaces.SelectFragmentCallBack;
import com.vadimfedchuk1994gmail.radio.service.PlayerRadioService;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;

public class PlayerFragment extends Fragment implements View.OnClickListener {

    private Context mContext;
    private boolean jobService = false;
    private boolean isVisibilityFragment = false;
    private CircleImageView mPlayerControl;
    private ImageView mPulseImageView;
    private TextView mPlayTime, mCurrentTrack;
    private SelectFragmentCallBack selectFragmentCallBack;

    private MediaControllerCompat mediaController;
    private MediaControllerCompat.Callback callback;
    private ServiceConnection mServiceConnection;
    private PlayerRadioService.PlayerServiceBinder playerServiceBinder;
    private boolean playing;
    private PowerManager.WakeLock wakeLock;

    public PlayerFragment(SelectFragmentCallBack selectFragmentCallBack){
        this.selectFragmentCallBack = selectFragmentCallBack;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        jobService = isMyServiceRunning();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        mPulseImageView = view.findViewById(R.id.player_pulse);
        mPulseImageView.setImageResource(R.drawable.pulse);
        mPlayerControl = view.findViewById(R.id.player_control);
        mPlayerControl.setOnClickListener(this);

        if(jobService){
            mPlayerControl.setImageResource(R.drawable.pause);
            mPulseImageView.setImageResource(R.drawable.pulse_on);
        } else {
            mPlayerControl.setImageResource(R.drawable.play);
            mPulseImageView.setImageResource(R.drawable.pulse);
        }

        Typeface geometriaFace = Typeface.createFromAsset(mContext.getAssets(), "geometria.ttf");
        mPlayTime = view.findViewById(R.id.player_playTime);
        mPlayTime.setTypeface(geometriaFace);
        mCurrentTrack = view.findViewById(R.id.textView_current_track);
        mCurrentTrack.setTypeface(geometriaFace);

        SharedPreferences sPref = mContext.getSharedPreferences("AppDB", MODE_PRIVATE);
        String playTimeString = sPref.getString("PLAY_TIME", "");
        String playMusicNameString = sPref.getString("PLAY_NAME", "");
        mPlayTime.setText(playTimeString);
        mCurrentTrack.setText(playMusicNameString);

        TextView mButtonStartActivityLive = view.findViewById(R.id.player_buttonLive);
        mButtonStartActivityLive.setTypeface(geometriaFace);
        mButtonStartActivityLive.setOnClickListener(this);

        CircleImageView mInfoIcon = view.findViewById(R.id.player_info);
        CircleImageView mVkIcon = view.findViewById(R.id.player_vk);
        CircleImageView mInstagramIcon = view.findViewById(R.id.player_instagram);
        CircleImageView mWhatsappIcon = view.findViewById(R.id.player_whatsapp);
        CircleImageView mViberIcon = view.findViewById(R.id.player_viber);
        CircleImageView mTelegramIcon = view.findViewById(R.id.player_telegram);

        mInfoIcon.setOnClickListener(this);
        mVkIcon.setOnClickListener(this);
        mInstagramIcon.setOnClickListener(this);
        mWhatsappIcon.setOnClickListener(this);
        mViberIcon.setOnClickListener(this);
        mTelegramIcon.setOnClickListener(this);

        initParams();
        lock();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isVisibilityFragment = true;
        //updatePlayTrack();
    }

    @Override
    public void onPause() {
        super.onPause();
        isVisibilityFragment = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences sPref = mContext.getSharedPreferences("AppDB", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("PLAY_TIME", String.valueOf(mPlayTime.getText()));
        ed.putString("PLAY_NAME", String.valueOf(mCurrentTrack.getText()));
        ed.apply();
    }

    private void initParams() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.player_control:
                if (mediaController != null){
                    if(!playing){
                        ContextCompat.startForegroundService(mContext, new Intent(mContext, PlayerRadioService.class));
                        mPlayerControl.setImageResource(R.drawable.pause);
                        mediaController.getTransportControls().play();
                        mPulseImageView.setImageResource(R.drawable.pulse_on);
                    } else {
                        mPlayerControl.setImageResource(R.drawable.play);
                        mediaController.getTransportControls().pause();
                        mPulseImageView.setImageResource(R.drawable.pulse);
                    }
                } else {
                    mPlayerControl.setImageResource(R.drawable.pause);
                    startServicePlayRadio();
                    new Thread(() -> {
                        boolean th = true;
                        while (th){
                            if(mediaController != null){
                                mediaController.getTransportControls().play();
                                th = false;
                            }
                        }
                    }).start();
                }
                break;

            case R.id.player_buttonLive:
                Intent liveIntent = new Intent(mContext, Live.class);
                startActivity(liveIntent);
                break;

            case R.id.player_info:
                selectFragmentCallBack.selectFragmentCallBack(3);
                break;

            case R.id.player_vk:
                Intent vkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/yolapulsradio"));
                mContext.startActivity(vkIntent);
                break;

            case R.id.player_instagram:
                Intent instagramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/pulsradio_yo"));
                mContext.startActivity(instagramIntent);
                break;

            case R.id.player_whatsapp:
                Intent whatsAppIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://api.whatsapp.com/send?phone=+79177112525"));
                mContext.startActivity(whatsAppIntent);
                break;

            case R.id.player_viber:
                try {
                    String number = "+79177112525";
                    Uri uri = Uri.parse("tel:" + Uri.encode(number));
                    Intent viberIntent = new Intent("android.intent.action.VIEW");
                    viberIntent.setClassName("com.viber.voip", "com.viber.voip.WelcomeActivity");
                    viberIntent.setData(uri);
                    mContext.startActivity(viberIntent);
                } catch (ActivityNotFoundException e) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                    alertDialog.setTitle(R.string.error).setMessage(R.string.viber_not_found)
                            .setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.cancel());
                    alertDialog.show();
                }
                break;

            case R.id.player_telegram:
                try {
                    Intent telegramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=puls_radio_yo"));
                    mContext.startActivity(telegramIntent);
                } catch (ActivityNotFoundException e) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                    alertDialog.setTitle(R.string.error).setMessage(R.string.telegram_not_found)
                            .setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.cancel());
                    alertDialog.show();
                }
                break;
        }
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
                    mPlayerControl.setImageResource(R.drawable.pause);
                    mPulseImageView.setImageResource(R.drawable.pulse_on);
                } else {
                    mPlayerControl.setImageResource(R.drawable.play);
                    mPulseImageView.setImageResource(R.drawable.pulse);
                }
            }
        };

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                playerServiceBinder = (PlayerRadioService.PlayerServiceBinder) service;
                mediaController = new MediaControllerCompat(getContext(), playerServiceBinder.getMediaSessionToken());
                mediaController.registerCallback(callback);
                callback.onPlaybackStateChanged(mediaController.getPlaybackState());
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

        if(getActivity() != null){
            getActivity().bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        }
    }

    private boolean isMyServiceRunning() {
        if(getContext() == null) return false;
        ActivityManager manager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
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
        if(getContext() != null){
            PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "MyApp::MyWakelockTag");
            }
            wakeLock.acquire(10*60*1000L /*10 minutes*/);
            WifiManager wm = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiManager.WifiLock wfl = null;
            if (wm != null) {
                wfl = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "sync_all_wifi");
            }
            if (wfl != null) {
                wfl.acquire();
            }
        }
    }

    private void updatePlayTrack() {
        new Thread(() -> {
            while (isVisibilityFragment){
                if(getActivity() != null){
                    getActivity().runOnUiThread(this::getMusicPlay);
                }
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getMusicPlay() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://mobile.puls-radio.ru/puls.txt", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                String[] play = response.split(";");
                String playMusic = play[1].trim();
                if(isVisibilityFragment){
                    mPlayTime.setText(String.valueOf(play[0]));
                    mCurrentTrack.setText(playMusic);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            }
        });
    }
}
