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

import com.vadimfedchuk1994gmail.radio.R;
import com.vadimfedchuk1994gmail.radio.adapters.PlayListAdapter;
import com.vadimfedchuk1994gmail.radio.controllers.PlayListController;
import com.vadimfedchuk1994gmail.radio.intarfaces.PlayListViewCallBack;
import com.vadimfedchuk1994gmail.radio.models.PlayListPOJO;

import java.util.ArrayList;

public class PlayListFragment extends Fragment implements PlayListViewCallBack {

    private Context mContext;
    private ArrayList<PlayListPOJO> obj = new ArrayList<>();
    private PlayListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;
    private PlayListController playListController;
    private TextView mErrorTextView;

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
        mSwipeRefreshLayout = view.findViewById(R.id.playlist_swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorSwipeRefreshLayout);
        mSwipeRefreshLayout.setRefreshing(false);
        RecyclerView mRecyclerView = view.findViewById(R.id.playList_recyclerView);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        playListController = new PlayListController(this);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            playListController.getPlayListObject();
        });
        mErrorTextView = view.findViewById(R.id.playList_error);
        mErrorTextView.setVisibility(View.GONE);
        Typeface mGeometriaFace = Typeface.createFromAsset(getContext().getAssets(), "geometria.ttf");
        mErrorTextView.setTypeface(mGeometriaFace);
        loadPlayListOnServer();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        playListController.repositoryCondition(true);
    }

    @Override
    public void onPause() {
        playListController.repositoryCondition(false);
        super.onPause();
    }

    @Override
    public void playListOnSuccess(ArrayList<PlayListPOJO> responseObject) {
        mErrorTextView.setVisibility(View.GONE);
        obj.clear();
        obj.addAll(responseObject);
        mSwipeRefreshLayout.setRefreshing(false);
        mProgressBar.setVisibility(View.GONE);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void playlistOnFailure(int statusCode) {
        obj.clear();
        mSwipeRefreshLayout.setRefreshing(false);
        mProgressBar.setVisibility(View.GONE);
        mAdapter.notifyDataSetChanged();
        mErrorTextView.setVisibility(View.VISIBLE);
    }

    private void loadPlayListOnServer() {
        if(obj.size() == 0){
            mErrorTextView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
            mAdapter.notifyDataSetChanged();
            playListController.getPlayListObject();
        }
    }

}






































