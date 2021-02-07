package com.vadimfedchuk1994gmail.radio.fragments;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.vadimfedchuk1994gmail.radio.R;
import com.vadimfedchuk1994gmail.radio.intarfaces.SongCallBack;
import com.vadimfedchuk1994gmail.radio.network.GetPlaySong;
import com.vadimfedchuk1994gmail.radio.service.PlayerRadioService;
import com.vadimfedchuk1994gmail.radio.utils.MyServiceRunning;

import java.util.Timer;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.BIND_AUTO_CREATE;

public class PlayerFragment extends Fragment implements View.OnClickListener, SongCallBack {

    private Context mContext;
    private CircleImageView mPlayerControl;
    private ImageView mPulseImageView;
    private TextView mPlayTime, mCurrentTrack;
    private MediaControllerCompat mediaController;
    private PlayerRadioService.PlayerServiceBinder playerServiceBinder;
    private ServiceConnection mServiceConnection;
    private double screenInches;
    private GetPlaySong getPlaySong;

    private String playTime, playSongName;
    private MediaControllerCompat.Callback callback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initParams();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = null;
        if(screenInches < Double.parseDouble("4.7")){
            view = inflater.inflate(R.layout.fragment_player_mini, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_player, container, false);
        }

        ImageView mLogoView = view.findViewById(R.id.logo_imageView);
        Glide.with(mContext).load(R.drawable.logo).into(mLogoView);
        mPulseImageView = view.findViewById(R.id.player_pulse);
        Glide.with(mContext).load(R.drawable.pulse).into(mPulseImageView);
        mPlayerControl = view.findViewById(R.id.player_control);
        mPlayerControl.setOnClickListener(this);
        Typeface geometriaFace = Typeface.createFromAsset(mContext.getAssets(), "geometria.ttf");
        mPlayTime = view.findViewById(R.id.player_playTime);
        mPlayTime.setTypeface(geometriaFace);
        mCurrentTrack = view.findViewById(R.id.textView_current_track);
        mCurrentTrack.setTypeface(geometriaFace);
        mPlayTime.setText(playTime);
        mCurrentTrack.setText(playSongName);
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

        if(mediaController != null){
            if(mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING){
                mPlayerControl.setImageResource(R.drawable.pause);
                Glide.with(mContext).load(R.drawable.pulse_on).into(mPulseImageView);
            }
        }

        initMediaCallBack();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPlaySong = new GetPlaySong(this);
        getPlaySong.play();
        new Timer().schedule(getPlaySong, 0, 5000);
        MyServiceRunning myServiceRunning = new MyServiceRunning(mContext);
        if(myServiceRunning.isMyServiceRunning()) {
            Log.d("MyLog", "СЕРВИС ЗАПУЩЕН!");
            if(mServiceConnection == null) startServicePlayRadio(false);
        } else {
            Log.d("MyLog", "СЕРВИС НЕ ЗАПУЩЕН!");
        }
    }

    @Override
    public void playSong(String songName, String time, boolean state) {
        mPlayTime.setText(time);
        mCurrentTrack.setText(songName);
        this.playTime = time;
        this.playSongName = songName;
    }

    @Override
    public void onPause() {
        super.onPause();
        getPlaySong.stop();
    }

    @Override
    public void onDestroy() {
        if(mediaController != null){
            if(mediaController.getPlaybackState().getState() != PlaybackStateCompat.STATE_PLAYING){
                getActivity().unbindService(mServiceConnection);
            }
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if(viewId == R.id.player_control){
            if (mediaController == null){
                mPlayerControl.setImageResource(R.drawable.pause);
                startServicePlayRadio(true);
            } else {
                if(mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING){
                    mediaController.getTransportControls().pause();
                } else {
                    mediaController.getTransportControls().play();
                }
            }
        }
    }

    private void initParams() {
        playTime = "-- --";
        playSongName = "Обновление данных...";
        mContext = getContext();
        DisplayMetrics dm = new DisplayMetrics();
        if(getActivity() != null) getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        double x = Math.pow(dm.widthPixels/dm.xdpi,2);
        double y = Math.pow(dm.heightPixels/dm.ydpi,2);
        screenInches = Math.sqrt(x+y);
    }

    private void initMediaCallBack() {
        callback = new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                if(state == null) return;
                if(state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    mPlayerControl.setImageResource(R.drawable.pause);
                    mPulseImageView.setImageResource(R.drawable.pulse_on);
                } else {
                    mPlayerControl.setImageResource(R.drawable.play);
                    mPulseImageView.setImageResource(R.drawable.pulse);
                }
            }
        };
    }

    private void startServicePlayRadio(boolean newPlaySession) {
        Intent intent = new Intent(getActivity(), PlayerRadioService.class);
        ContextCompat.startForegroundService(mContext, intent);
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                playerServiceBinder = (PlayerRadioService.PlayerServiceBinder) service;
                mediaController = new MediaControllerCompat(getContext(), playerServiceBinder.getMediaSessionToken());
                mediaController.registerCallback(callback);
                callback.onPlaybackStateChanged(mediaController.getPlaybackState());
                if(newPlaySession) {
                    mediaController.getTransportControls().play();
                } else {
                    if(mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING){
                        Glide.with(mContext).load(R.drawable.pulse_on).into(mPulseImageView);
                    }
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
        getActivity().bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

}
