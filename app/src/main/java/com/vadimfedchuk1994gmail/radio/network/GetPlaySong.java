package com.vadimfedchuk1994gmail.radio.network;

import android.util.Log;

import com.vadimfedchuk1994gmail.radio.intarfaces.SongCallBack;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.TimerTask;


public class GetPlaySong extends TimerTask {

    private SongCallBack callBack;
    private boolean job;
    private String controlSong = null;

    public GetPlaySong(SongCallBack songCallBack){
        this.callBack = songCallBack;
        job = true;
    }

    public void stop(){
        job = false;
    }

    public void play(){
        job = true;
    }

    @Override
    public void run() {
        if (!job) return;
        URL url = null;
        String text = null;
        try {
            url = new URL("http://mobile.puls-radio.ru/puls.txt");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            if (url != null) {
                text = new Scanner(url.openStream(), "windows-1251").useDelimiter("\\A").next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (text != null) {
            try {
                String song = text.substring(6).trim();
                String response = song.substring(0, song.length() - 3);

                String[] play = text.split(";");
                String playTime = play[0].trim();
                String playName = play[1].trim();

                if(job){
                    callBack.playSong(playName, playTime, true);
                }
            } catch (RuntimeException ignored){ }
        }

    }

}
