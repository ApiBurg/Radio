package com.vadimfedchuk1994gmail.radio.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.net.Uri;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.vadimfedchuk1994gmail.radio.LiveVk;
import com.vadimfedchuk1994gmail.radio.MainActivity;
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
        mContext = getContext();
        if(screenInches < Double.parseDouble("4.7")){
            view = inflater.inflate(R.layout.fragment_player_mini, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_player, container, false);
        }

        ImageView mLogoView = view.findViewById(R.id.logo_imageView);
        Glide.with(mContext).load(R.drawable.logo).into(mLogoView);

        mPulseImageView = view.findViewById(R.id.player_pulse);
        setPulse(false);

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
                setPulse(true);
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
            if(mServiceConnection == null) startServicePlayRadio(false);
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
                if(getActivity() != null) getActivity().unbindService(mServiceConnection);
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
                try {
                    if(mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING){
                        mediaController.getTransportControls().pause();
                    } else {
                        mediaController.getTransportControls().play();
                    }
                } catch (NullPointerException e){
                    mPlayerControl.setImageResource(R.drawable.pause);
                    setPulse(false);
                }
            }
        } else if(viewId == R.id.player_buttonLive){
            Intent liveIntent = new Intent(mContext, LiveVk.class);
            startActivity(liveIntent);
        } else if(viewId == R.id.player_vk){
            Intent vkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/yolapulsradio"));
            mContext.startActivity(vkIntent);
        } else if(viewId == R.id.player_instagram){
            Intent instagramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/pulsradio_yo"));
            mContext.startActivity(instagramIntent);
        } else if(viewId == R.id.player_whatsapp){
            Intent whatsAppIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://api.whatsapp.com/send?phone=+79177112525"));
            mContext.startActivity(whatsAppIntent);
        } else if(viewId == R.id.player_viber){
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
        } else if(viewId == R.id.player_telegram){
            try {
                Intent telegramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=puls_radio_yo"));
                mContext.startActivity(telegramIntent);
            } catch (ActivityNotFoundException e) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                alertDialog.setTitle(R.string.error).setMessage(R.string.telegram_not_found)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.cancel());
                alertDialog.show();
            }
        } else if(viewId == R.id.player_info){
            if(getActivity() != null) ((MainActivity) getActivity()).setInfoFragment();
        }
    }

    private void initParams() {
        playTime = "-- --";
        playSongName = "Обновление данных...";
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
                    setPulse(true);


                } else {
                    mPlayerControl.setImageResource(R.drawable.play);
                    setPulse(false);
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
                        setPulse(true);
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
        if(getActivity() != null) getActivity().bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }


    private void setPulse(boolean pulse){
        try {
            if(pulse){
                if(getContext() != null){
                    Context context = getContext();
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int screenWidth = displaymetrics.widthPixels;
                    float dp_one = 619 / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
                    float scale = context.getResources().getDisplayMetrics().density;
                    int dp_final = ((int) (dp_one * scale + 0.5f));
                    ViewGroup.LayoutParams params =  mPulseImageView.getLayoutParams();
                    params.width = screenWidth;
                    params.height = dp_final;
                    mPulseImageView.setLayoutParams(params);
                    Glide.with(context).load(R.drawable.pulse_on).into(mPulseImageView);
                }
            } else {
                if(getContext() != null) {
                    Context context = getContext();
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int screenWidth = displaymetrics.widthPixels;
                    float dp_one = 52 / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
                    float scale = context.getResources().getDisplayMetrics().density;
                    int dp_final = ((int) (dp_one * scale + 0.5f));
                    ViewGroup.LayoutParams params =  mPulseImageView.getLayoutParams();
                    params.width = screenWidth;
                    params.height = dp_final;
                    mPulseImageView.setLayoutParams(params);
                    Glide.with(context).load(R.drawable.pulse).into(mPulseImageView);
                }
            }
        } catch (NullPointerException e){
            Log.d("MyLog", "Произошла ошибка: "+e);
        }
    }

}
