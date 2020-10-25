package com.vadimfedchuk1994gmail.radio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class LiveVk extends AppCompatActivity {

    private boolean isInitView = false;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        initToolBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isInitView){
            isInitView = true;
            initView();
        }
    }

    private void initToolBar() {
        Toolbar toolbar = findViewById(R.id.live_toolbar);
        toolbar.setTitle(R.string.live);
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_black_24);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {
        WebView mWebView = findViewById(R.id.live_webView);
        mWebView.setVisibility(View.GONE);
        mProgressBar = findViewById(R.id.live_progressBar);
        mProgressBar.setVisibility(View.VISIBLE);
        if(isOnline()){
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.setWebViewClient(new WebViewClient(){

                public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                    return super.shouldOverrideUrlLoading(webView, url);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                }
            });

            mWebView.setWebChromeClient(new WebChromeClient(){

                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    if(newProgress > 90){
                        mProgressBar.setVisibility(View.GONE);
                        mWebView.setVisibility(View.VISIBLE);
                    }
                }
            });
            mWebView.loadUrl("https://m.vk.com/videos-21699335");
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
