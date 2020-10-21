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
            case R.id.control_imageView:
                break;

            case R.id.viber_imageView:
                try {
                    String number = "+79177112525";
                    Uri uri = Uri.parse("tel:" + Uri.encode(number));
                    Intent viberIntent = new Intent("android.intent.action.VIEW");
                    viberIntent.setClassName("com.viber.voip", "com.viber.voip.WelcomeActivity");
                    viberIntent.setData(uri);
                    mContext.startActivity(viberIntent);
                } catch (ActivityNotFoundException e) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                    alertDialog.setTitle(R.string.error).setMessage(R.string.viber_not_found)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                    alertDialog.show();
                }
                break;

            case R.id.telegram_imageView:
                try {
                    Intent telegramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=puls_radio_yo"));
                    mContext.startActivity(telegramIntent);
                } catch (ActivityNotFoundException e) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                    alertDialog.setTitle(R.string.error).setMessage(R.string.telegram_not_found)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                    alertDialog.show();
                }
                break;

            case R.id.whatsapp_imageView:
                Intent whatsAppIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://api.whatsapp.com/send?phone=+79177112525"));
                mContext.startActivity(whatsAppIntent);
                break;
        }
    }
}
