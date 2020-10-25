package com.vadimfedchuk1994gmail.radio.utils;

import android.app.ActivityManager;
import android.content.Context;

import com.vadimfedchuk1994gmail.radio.service.PlayerRadioService;

public class MyServiceRunning {

    private Context mContext;

    public MyServiceRunning(Context mContext){
        this.mContext = mContext;
    }

    public boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (PlayerRadioService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
