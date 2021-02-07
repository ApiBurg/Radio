package com.vadimfedchuk1994gmail.radio.repository;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.vadimfedchuk1994gmail.radio.intarfaces.PlayListViewCallBack;
import com.vadimfedchuk1994gmail.radio.models.PlayListPOJO;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class PlayListController {

    private final PlayListViewCallBack viewCallBack;
    private boolean condition;

    public PlayListController(PlayListViewCallBack viewCallBack){
        this.viewCallBack = viewCallBack;
        condition = true;
    }

    public void repositoryCondition(boolean condition) {
        this.condition = condition;
    }

    public void getPlayListObject(){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://mobile.puls-radio.ru/puls10.txt", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = null;
                try {
                    response = new String(responseBody, "windows-1251");
                } catch (UnsupportedEncodingException e) {
                    Log.d("MyLog", "Произошла ошибка при декодирование кодировки: "+e);
                    if(condition) viewCallBack.playlistOnFailure(601);
                }
                startParserPlayList(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if(condition) viewCallBack.playlistOnFailure(statusCode);
            }
        });
    }

    private void startParserPlayList(String response) {
        ArrayList<PlayListPOJO> object = new ArrayList<>();
        String[] playListArray = response.split("\n");
        for (String s : playListArray) {
            String[] item = s.split(";");
            try {
                object.add(new PlayListPOJO(item[1], item[0]));
            } catch (ArrayIndexOutOfBoundsException e){
                Log.d("MyLog", "Произошла ошибка ArrayIndexOutOfBoundsException: "+e);
                Log.d("MyLog", "Полученная строка: "+response);
                if(condition) viewCallBack.playlistOnFailure(600);
                return;
            }
        }
        if(condition) viewCallBack.playListOnSuccess(object);
    }
}
