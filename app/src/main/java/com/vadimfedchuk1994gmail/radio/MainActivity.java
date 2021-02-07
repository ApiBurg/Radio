/* email of the author of the code soloviev_alexey@bk.ru */

package com.vadimfedchuk1994gmail.radio;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.vadimfedchuk1994gmail.radio.fragments.InfoFragment;
import com.vadimfedchuk1994gmail.radio.fragments.PlayListFragment;
import com.vadimfedchuk1994gmail.radio.fragments.PlayerFragment;
import com.vadimfedchuk1994gmail.radio.intarfaces.FragmentSelectCallBack;

public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener, FragmentSelectCallBack {

    private FragmentSelectCallBack fragmentSelectCallBack;
    private BottomNavigationView mBottomNavigationView;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initParams();
        initView();
        lock();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(wakeLock != null) wakeLock.release();
    }

    @Override
    public void selectFragmentCallBack(int position) {
        selectFragment();
    }

    private void initParams() {
        fragmentSelectCallBack = this;
        FirebaseMessaging.getInstance().subscribeToTopic("translation");
        FirebaseMessaging.getInstance().subscribeToTopic("android_translation");
    }

    private void initView() {
        mBottomNavigationView = findViewById(R.id.main_bottomNavigationView);
        mBottomNavigationView.setSelectedItemId(R.id.action_play);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        mFragmentTransaction.add(R.id.main_container, new PlayerFragment());
        mFragmentTransaction.commit();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        int itemId = item.getItemId();
        if(itemId == R.id.action_playlist){
            mFragmentTransaction.replace(R.id.main_container, new PlayListFragment());
        } else if(itemId == R.id.action_play){
            mFragmentTransaction.replace(R.id.main_container, new PlayerFragment());
        } else if(itemId == R.id.action_info){
            mFragmentTransaction.replace(R.id.main_container, new InfoFragment());
        }
        mFragmentTransaction.commit();
        getSupportFragmentManager().popBackStack();
        return true;
    }

    public void selectFragment(){
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        mFragmentTransaction.add(R.id.main_container, new InfoFragment());
        mFragmentTransaction.commit();
        getSupportFragmentManager().popBackStack();
        mBottomNavigationView.setSelectedItemId(R.id.action_info);
    }

    private void lock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyApp::MyWakelockTag");
        }
        wakeLock.acquire(10*60*1000L /*10 minutes*/);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.WifiLock wfl = null;
        if(wm != null) {
            wfl = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "sync_all_wifi");
        }
        if (wfl != null) { wfl.acquire(); }
    }

}
