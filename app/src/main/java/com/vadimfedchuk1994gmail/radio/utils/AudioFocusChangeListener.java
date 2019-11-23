package com.vadimfedchuk1994gmail.radio.utils;

import android.media.AudioManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

public class AudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener  {

    private MediaSessionCompat.Callback mediaSessionCallback;

    public AudioFocusChangeListener(MediaSessionCompat.Callback mediaSessionCallback){
        this.mediaSessionCallback = mediaSessionCallback;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.d("MyLog", "Фокус получен!");
                mediaSessionCallback.onPlay();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.d("MyLog", "Фокус забран!");
                mediaSessionCallback.onPause();
                break;

            default:
                Log.d("MyLog", "Фокус забран!");
                mediaSessionCallback.onPause();
                break;
        }
    }
}
