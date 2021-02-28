package com.vadimfedchuk1994gmail.radio.repository;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.vadimfedchuk1994gmail.radio.intarfaces.MainViewCallBack;

import cz.msebera.android.httpclient.Header;

public class MainController {

    private MainViewCallBack viewCallBack;
    private boolean condition;

    public MainController(MainViewCallBack viewCallBack){
        this.viewCallBack = viewCallBack;
    }

    public void repositoryCondition(boolean condition) {
        this.condition = condition;
    }

    public void getLastVersionApp(){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }


}
