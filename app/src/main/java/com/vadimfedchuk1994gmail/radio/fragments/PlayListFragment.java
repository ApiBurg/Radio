/* email of the author of the code soloviev_alexey@bk.ru */

package com.vadimfedchuk1994gmail.radio.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.vadimfedchuk1994gmail.radio.R;
import com.vadimfedchuk1994gmail.radio.adapters.PlayListAdapter;
import com.vadimfedchuk1994gmail.radio.models.PlayListPOJO;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class PlayListFragment extends Fragment {

    private Context mContext;
    private ArrayList<PlayListPOJO> obj = new ArrayList<>();
    private PlayListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;
    private boolean isVisibilityFragment = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mAdapter = new PlayListAdapter(obj, getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        TextView mTitleToolBar = view.findViewById(R.id.playListCustomToolBar_title);
        Typeface geometriaFace = Typeface.createFromAsset(mContext.getAssets(), "geometria.ttf");
        mTitleToolBar.setTypeface(geometriaFace);

        mProgressBar = view.findViewById(R.id.playList_progressBar);
        if(obj.size() == 0)  mProgressBar.setVisibility(View.VISIBLE);

        mSwipeRefreshLayout = view.findViewById(R.id.playlist_swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorSwipeRefreshLayout);
        mSwipeRefreshLayout.setRefreshing(false);
        RecyclerView mRecyclerView = view.findViewById(R.id.playList_recyclerView);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mSwipeRefreshLayout.setOnRefreshListener(this::queryDataFromServer);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        isVisibilityFragment = true;
        queryDataFromServer();
    }

    @Override
    public void onPause() {
        super.onPause();
        isVisibilityFragment = false;
    }

    private void queryDataFromServer() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://mobile.puls-radio.ru/puls10.txt", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = null;
                try {
                    response = new String(responseBody, "windows-1251");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                startParserPlayList(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void startParserPlayList(String response) {
        new Thread(() -> {
            assert response != null;
            String[] playListArray = response.split("\n");
            for (String s : playListArray) {
                String[] item = s.split(";");
                obj.add(new PlayListPOJO(item[1], item[0]));
            }
            if(getActivity() != null & isVisibilityFragment){
                getActivity().runOnUiThread(() -> {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mProgressBar.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                });
            }
        }).start();
    }

}






































