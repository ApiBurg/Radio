package com.vadimfedchuk1994gmail.radio.network;

import android.util.Log;

import com.vadimfedchuk1994gmail.radio.intarfaces.SongCallBack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetPlaySong extends TimerTask {

    private SongCallBack callBack;
    private boolean job;

    public GetPlaySong(SongCallBack songCallBack){
        this.callBack = songCallBack;
        job = true;
    }

    public void stop(){
        job = false;
    }

    @Override
    public void run() {
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
            String song = text.substring(6, text.indexOf("\'"));
            callBack.songCallBack(song, true);
        }
    }

}
