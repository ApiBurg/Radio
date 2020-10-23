package com.vadimfedchuk1994gmail.radio.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.vadimfedchuk1994gmail.radio.R;

public class InfoFragment extends Fragment implements View.OnClickListener {

    private Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        Typeface mGeometriaFace = Typeface.createFromAsset(mContext.getAssets(), "geometria.ttf");
        Typeface mGeometriaBoldFace = Typeface.createFromAsset(mContext.getAssets(), "geometria_bold.ttf");
        TextView mTitleView = view.findViewById(R.id.info_title);
        mTitleView.setTypeface(mGeometriaFace);

        TextView mLocationRadioView = view.findViewById(R.id.location_name);
        mLocationRadioView.setTypeface(mGeometriaBoldFace);

        TextView mFmNumberView = view.findViewById(R.id.number_fm);
        mFmNumberView.setTypeface(mGeometriaBoldFace);

        TextView mLicenseView = view.findViewById(R.id.license);
        mLicenseView.setTypeface(mGeometriaBoldFace);

        TextView mLiveTitleButtonView = view.findViewById(R.id.live_two);
        mLiveTitleButtonView.setTypeface(mGeometriaFace);

        TextView mButtonLive = view.findViewById(R.id.info_buttonNumberLive);
        mButtonLive.setTypeface(mGeometriaFace);
        mButtonLive.setOnClickListener(this);

        TextView mAddDepTitle = view.findViewById(R.id.info_addDep);
        mAddDepTitle.setTypeface(mGeometriaFace);

        TextView mButtonAddDep = view.findViewById(R.id.info_buttonAvertising);
        mButtonAddDep.setTypeface(mGeometriaFace);
        mButtonAddDep.setOnClickListener(this);

        TextView mEditionTitle = view.findViewById(R.id.info_edition);
        mEditionTitle.setTypeface(mGeometriaFace);

        TextView mButtonEdition = view.findViewById(R.id.info_buttonEdition);
        mButtonEdition.setTypeface(mGeometriaFace);
        mButtonEdition.setOnClickListener(this);

        TextView mLinkSite = view.findViewById(R.id.info_linkSite);
        SpannableString content = new SpannableString("www.puls-radio.ru");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        mLinkSite.setText(content);
        mLinkSite.setTypeface(mGeometriaBoldFace);
        mLinkSite.setOnClickListener(this);

        TextView mLocationName = view.findViewById(R.id.info_locationName);
        mLocationName.setTypeface(mGeometriaBoldFace);
        mLocationName.setOnClickListener(this);

        TextView mEmailInfo = view.findViewById(R.id.info_email);
        mEmailInfo.setTypeface(mGeometriaBoldFace);
        mEmailInfo.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.info_buttonNumberLive:
                String phone = "+78362630088";
                Intent intentNumberLive =
                        new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intentNumberLive);
                break;

            case R.id.info_buttonAvertising:
                String phoneAddDep = "+78362422828";
                Intent intentAddDep =
                        new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneAddDep, null));
                startActivity(intentAddDep);
                break;

            case R.id.info_buttonEdition:
                String phoneEdition = "+78362426926";
                Intent intentEdition =
                        new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneEdition, null));
                startActivity(intentEdition);
                break;

            case R.id.info_email:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","fm@puls-radio.ru", null));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, "fm@puls-radio.ru"); // String[] addresses
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                break;

            case R.id.info_locationName:
                Uri gmmIntentUri = Uri.parse("geo:56.6381615,47.8792697");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(mContext.getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
                break;

            case R.id.info_linkSite:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.puls-radio.ru"));
                startActivity(browserIntent);
                break;

            default:
        }
    }
}



























