package com.vadimfedchuk1994gmail.radio.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.vadimfedchuk1994gmail.radio.R;

public class InfoFragment extends Fragment {

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

        return view;
    }
}



























