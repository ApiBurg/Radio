/* email of the author of the code soloviev_alexey@bk.ru */

package com.vadimfedchuk1994gmail.radio;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.vadimfedchuk1994gmail.radio.fragments.InfoFragment;
import com.vadimfedchuk1994gmail.radio.fragments.PlayerFragment;
import com.vadimfedchuk1994gmail.radio.fragments.PlayListFragment;
import com.vadimfedchuk1994gmail.radio.intarfaces.SelectFragmentCallBack;

public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener, SelectFragmentCallBack {

    private SelectFragmentCallBack selectFragmentCallBack;
    private Fragment mPlayerFragment, mPlayListFragment, mInfoFragment;
    private BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initParams();
        initView();
    }

    @Override
    public void selectFragmentCallBack(int position) {
        selectFragment();
    }

    private void initParams() {
        selectFragmentCallBack = this;
        FirebaseMessaging.getInstance().unsubscribeFromTopic("translation");
    }

    private void initView() {
        mBottomNavigationView = findViewById(R.id.main_bottomNavigationView);
        mBottomNavigationView.setSelectedItemId(R.id.action_play);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);
        mPlayerFragment = new PlayerFragment(selectFragmentCallBack);
        mPlayListFragment = new PlayListFragment();
        mInfoFragment = new InfoFragment();
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        mFragmentTransaction.add(R.id.main_container, mPlayerFragment);
        mFragmentTransaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (item.getItemId()){
            case R.id.action_playlist:
                mFragmentTransaction.replace(R.id.main_container, mPlayListFragment);
                break;

            case R.id.action_play:
                mFragmentTransaction.replace(R.id.main_container, mPlayerFragment);
                break;

            case R.id.action_info:
                mFragmentTransaction.replace(R.id.main_container, mInfoFragment);
                break;
        }
        mFragmentTransaction.commit();
        return true;
    }

    public void selectFragment(){
        mBottomNavigationView.setSelectedItemId(R.id.action_info);
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        mFragmentTransaction.replace(R.id.main_container, mInfoFragment);
        mFragmentTransaction.commit();
    }
}
