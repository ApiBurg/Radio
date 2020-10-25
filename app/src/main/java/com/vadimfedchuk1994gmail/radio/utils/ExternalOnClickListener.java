package com.vadimfedchuk1994gmail.radio.utils;

import android.content.Context;
import android.view.View;

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
