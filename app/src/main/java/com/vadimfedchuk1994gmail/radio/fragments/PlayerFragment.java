package com.vadimfedchuk1994gmail.radio.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;
import com.vadimfedchuk1994gmail.radio.LiveVk;
import com.vadimfedchuk1994gmail.radio.R;
import com.vadimfedchuk1994gmail.radio.intarfaces.FragmentSelectCallBack;
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
    private FragmentSelectCallBack selectFragmentCallBack;

    private MediaControllerCompat mediaController;
    private MediaControllerCompat.Callback callback;
    private PlayerRadioService.PlayerServiceBinder playerServiceBinder;
    private boolean playing;
    private GetPlaySong getPlaySong;
    private boolean isResponsePlay = false;
    private String playName, playTime;
    private MyServiceRunning myServiceRunning;
    private ServiceConnection mServiceConnection;
    private Timer timer;
    private double screenInches;
    private SongCallBack songCallBack;

    public PlayerFragment(FragmentSelectCallBack selectFragmentCallBack){
        this.selectFragmentCallBack = selectFragmentCallBack;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        initParams();
        myServiceRunning = new MyServiceRunning(mContext);
        if(myServiceRunning.isMyServiceRunning()){ startServicePlayRadio(); }

        DisplayMetrics dm = new DisplayMetrics();
        if(getActivity() != null) {
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        }
        double x = Math.pow(dm.widthPixels/dm.xdpi,2);
        double y = Math.pow(dm.heightPixels/dm.ydpi,2);
        screenInches = Math.sqrt(x+y);
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
        Picasso.with(mContext).load(R.drawable.logo).into(mLogoView);

        mPulseImageView = view.findViewById(R.id.player_pulse);
        Picasso.with(mContext).load(R.drawable.pulse).into(mPulseImageView);
        mPlayerControl = view.findViewById(R.id.player_control);
        mPlayerControl.setOnClickListener(this);

        if(myServiceRunning.isMyServiceRunning()){
            if (mediaController != null){
                if(mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING){
                    mPlayerControl.setImageResource(R.drawable.pause);
                    Picasso.with(mContext).load(R.drawable.pulse_on).into(mPulseImageView);
                } else {
                    mPlayerControl.setImageResource(R.drawable.play);
                    Picasso.with(mContext).load(R.drawable.pulse).into(mPulseImageView);
                }
            } else {
                mPlayerControl.setImageResource(R.drawable.pause);
                Picasso.with(mContext).load(R.drawable.pulse_on).into(mPulseImageView);
            }
        } else {
            mPlayerControl.setImageResource(R.drawable.play);
            Picasso.with(mContext).load(R.drawable.pulse).into(mPulseImageView);
        }

        Typeface geometriaFace = Typeface.createFromAsset(mContext.getAssets(), "geometria.ttf");
        mPlayTime = view.findViewById(R.id.player_playTime);
        mPlayTime.setTypeface(geometriaFace);
        mCurrentTrack = view.findViewById(R.id.textView_current_track);
        mCurrentTrack.setTypeface(geometriaFace);

        if(!isResponsePlay){
            mPlayTime.setText("-- --");
            mCurrentTrack.setText("Обновление данных...");
        } else {
            mPlayTime.setText(playTime);
            mCurrentTrack.setText(playName);
        }

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
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPlaySong = new GetPlaySong(songCallBack);
        timer = new Timer();
        timer.schedule(getPlaySong, 0, 5000);
        getPlaySong.play();

    }

    @Override
    public void onPause() {
        super.onPause();
        getPlaySong.stop();
        timer.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        if(!playing & !myServiceRunning.isMyServiceRunning()) {
            if(mediaController == null) return;
            mediaController.getTransportControls().stop();
            try {
                if(getActivity() != null){
                    getActivity().unbindService(mServiceConnection);
                    getActivity().stopService(new Intent(mContext, PlayerRadioService.class));
                }
            } catch (IllegalArgumentException e) {
                Log.d("MyLog", String.valueOf(e));
            }
        }

        playerServiceBinder = null;
        if (mediaController != null) {
            mediaController.unregisterCallback(callback);
            mediaController = null;
        }


        try {
            if(getActivity() != null) getActivity().unbindService(mServiceConnection);
        } catch (IllegalArgumentException e) {
            Log.d("MyLog", String.valueOf(e));
        }
    }

    @Override
    public void songCallBack(String songName, String playTime, boolean state) {
        mPlayTime.setText(playTime);
        mCurrentTrack.setText(songName);
        this.playTime = playTime;
        this.playName = songName;
        if(!isResponsePlay) isResponsePlay = true;
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
                        Picasso.with(mContext).load(R.drawable.pulse_on).into(mPulseImageView);
                    } else {
                        mPlayerControl.setImageResource(R.drawable.play);
                        mediaController.getTransportControls().pause();
                        Picasso.with(mContext).load(R.drawable.pulse).into(mPulseImageView);
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
                Intent liveIntent = new Intent(mContext, LiveVk.class);
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

    private void initParams() {
        songCallBack = this;
    }

    private void startServicePlayRadio() {
        Activity activity =  getActivity();
        Intent intent = new Intent(getActivity(), PlayerRadioService.class);
        ContextCompat.startForegroundService(mContext, intent);
        callback = new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                if(state == null) return;;
                playing = state.getState() == PlaybackStateCompat.STATE_PLAYING;
                if(playing) {
                    mPlayerControl.setImageResource(R.drawable.pause);
                    Picasso.with(mContext).load(R.drawable.pulse_on).into(mPulseImageView);
                } else {
                    mPlayerControl.setImageResource(R.drawable.play);
                    Picasso.with(mContext).load(R.drawable.pulse).into(mPulseImageView);
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

        if(activity != null){
            activity.bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        }
    }

}
