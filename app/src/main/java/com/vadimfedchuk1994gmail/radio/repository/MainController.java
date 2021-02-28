package com.vadimfedchuk1994gmail.radio.repository;

import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.vadimfedchuk1994gmail.radio.BuildConfig;
import com.vadimfedchuk1994gmail.radio.intarfaces.MainViewCallBack;
import com.vadimfedchuk1994gmail.radio.models.VersionAppPOJO;

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
        client.get("http://univer-fm.ru", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Gson gson = new Gson();
                VersionAppPOJO json = gson.fromJson(response, VersionAppPOJO.class);
                int versionCode = BuildConfig.VERSION_CODE;
                if(versionCode != json.getVersion()){
                    if(condition) viewCallBack.showDialogNewVersion();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }


}
