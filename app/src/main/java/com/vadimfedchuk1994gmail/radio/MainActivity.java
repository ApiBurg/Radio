/* email of the author of the code soloviev_alexey@bk.ru */

package com.vadimfedchuk1994gmail.radio;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.vadimfedchuk1994gmail.radio.fragments.InfoFragment;
import com.vadimfedchuk1994gmail.radio.fragments.PlayListFragment;
import com.vadimfedchuk1994gmail.radio.fragments.PlayerFragment;
import com.vadimfedchuk1994gmail.radio.intarfaces.MainViewCallBack;
import com.vadimfedchuk1994gmail.radio.repository.MainController;


public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener, MainViewCallBack {

    private Context context;
    private BottomNavigationView mBottomNavigationView;
    private PowerManager.WakeLock wakeLock;
    private PlayerFragment mPlayerFragment;
    private MainController mainController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        mPlayerFragment  = new PlayerFragment();
        mFragmentTransaction.add(R.id.main_container, mPlayerFragment);
        mFragmentTransaction.commitNowAllowingStateLoss();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        //getSupportFragmentManager().popBackStack();
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(itemId == R.id.action_playlist){
            mFragmentTransaction.replace(R.id.main_container, new PlayListFragment());
        } else if(itemId == R.id.action_play){
            mFragmentTransaction.replace(R.id.main_container, mPlayerFragment);
        } else if(itemId == R.id.action_info){
            mFragmentTransaction.replace(R.id.main_container, new InfoFragment());
        }
        mFragmentTransaction.commit();
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
        //getSupportFragmentManager().popBackStack();
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        mFragmentTransaction.replace(R.id.main_container, new InfoFragment());
        mFragmentTransaction.commit();
        mBottomNavigationView.setSelectedItemId(R.id.action_info);
    }

}
