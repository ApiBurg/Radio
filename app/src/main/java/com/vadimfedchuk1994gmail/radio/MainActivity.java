/* email of the author of the code soloviev_alexey@bk.ru */

package com.vadimfedchuk1994gmail.radio;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.vadimfedchuk1994gmail.radio.controllers.MainController;
import com.vadimfedchuk1994gmail.radio.fragments.InfoFragment;
import com.vadimfedchuk1994gmail.radio.fragments.PlayListFragment;
import com.vadimfedchuk1994gmail.radio.fragments.PlayerFragment;
import com.vadimfedchuk1994gmail.radio.intarfaces.MainViewCallBack;


public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener, MainViewCallBack {

    private Context context;
    private BottomNavigationView mBottomNavigationView;
    private PowerManager.WakeLock wakeLock;
    private MainController mainController;
    private PlayerFragment playerFragment;
    private PlayListFragment playListFragment;
    private InfoFragment infoFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playerFragment = new PlayerFragment();
        playListFragment = new PlayListFragment();
        infoFragment = new InfoFragment();
        initParams();
        initView();
        lock();
        mainController = new MainController(this);
        mainController.getLastVersionApp();
    }

    @Override
    protected void onDestroy() {
        if(wakeLock != null) wakeLock.release();
        mainController.repositoryCondition(false);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainController.repositoryCondition(true);
    }

    @Override
    public void showDialogNewVersion() {
        AlertDialog.Builder alertDialogBan = new AlertDialog.Builder(context);
        alertDialogBan.setTitle(R.string.new_version).setMessage(R.string.new_version_description)
                .setNegativeButton(R.string.close, (dialog, which) -> dialog.cancel())
                .setPositiveButton(R.string.app_update, (dialog, which) -> {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.vadimfedchuk1994gmail.radio"));
                    startActivity(i);
                });
        alertDialogBan.show();
    }

    private void initParams() {
        context = this;
        FirebaseMessaging.getInstance().subscribeToTopic("translation");
        FirebaseMessaging.getInstance().subscribeToTopic("android_translation");
    }

    private void initView() {
        mBottomNavigationView = findViewById(R.id.main_bottomNavigationView);
        mBottomNavigationView.setSelectedItemId(R.id.action_play);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_container, playListFragment, "playListFragment").hide(playListFragment)
                .add(R.id.main_container, infoFragment, "infoFragment").hide(infoFragment)
                .add(R.id.main_container, playerFragment, "playerFragment").commit();
        activeFragment = playerFragment;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.action_playlist){
            if(mBottomNavigationView.getSelectedItemId() == R.id.action_playlist) return true;
            getSupportFragmentManager().beginTransaction().hide(activeFragment).show(playListFragment).commit();
            activeFragment = playListFragment;
        } else if(itemId == R.id.action_play){
            if(mBottomNavigationView.getSelectedItemId() == R.id.action_play) return true;
            getSupportFragmentManager().beginTransaction().hide(activeFragment).show(playerFragment).commit();
            activeFragment = playerFragment;
        } else if(itemId == R.id.action_info){
            if(mBottomNavigationView.getSelectedItemId() == R.id.action_info) return true;
            getSupportFragmentManager().beginTransaction().hide(activeFragment).show(infoFragment).commit();
            activeFragment = infoFragment;
        }
        return true;
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

    public void setInfoFragment(){
        if(mBottomNavigationView.getSelectedItemId() == R.id.action_info) return;
        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(infoFragment).commit();
        activeFragment = infoFragment;
        mBottomNavigationView.setSelectedItemId(R.id.action_info);
    }

}
