package com.vadimfedchuk1994gmail.radio.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.vadimfedchuk1994gmail.radio.ApplicationInfo;
import com.vadimfedchuk1994gmail.radio.R;

public class ExternalOnClickListener implements View.OnClickListener {

    private Context mContext;

    public ExternalOnClickListener(Context context) {
        this.mContext = context;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        }
    }
}
