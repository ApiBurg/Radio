package com.vadimfedchuk1994gmail.radio.controllers;

import android.os.Build;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.vadimfedchuk1994gmail.radio.BuildConfig;
import com.vadimfedchuk1994gmail.radio.intarfaces.MainViewCallBack;

import cz.msebera.android.httpclient.Header;

public class MainController {

    private final MainViewCallBack viewCallBack;
    private boolean condition;

    public MainController(MainViewCallBack viewCallBack){
        this.viewCallBack = viewCallBack;
        condition = true;
    }

    public void repositoryCondition(boolean condition) {
        this.condition = condition;
    }

    public void getLastVersionApp(){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://mobile.puls-radio.ru/Version.txt", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                int actualVersionApp = Integer.parseInt(response);
                if(actualVersionApp == 0) return;
                if(BuildConfig.VERSION_CODE < actualVersionApp) {
                    if(condition) viewCallBack.showDialogNewVersion();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }


}
