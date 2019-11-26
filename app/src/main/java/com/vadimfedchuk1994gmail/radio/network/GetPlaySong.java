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
        if(!job) return;
        Log.d("MyLog", "Получаем информацию о композиции");
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

        if(text != null){
            try {
                String song = text.substring(6).trim();
                String response = song.substring(0, song.length() - 3);
                Log.d("MyLog", response);
                if(controlSong != null ){
                    if(controlSong.length() != response.length() & job){
                        callBack.songCallBack(response, true);
                        controlSong = response;
                    }
                } else {
                    if(job)  {
                        callBack.songCallBack(response, true);
                        controlSong = response;
                    }
                }
            } catch (RuntimeException e){
                if(job) callBack.songCallBack("Получение композиции...", true);
            }
        }
    }

}
