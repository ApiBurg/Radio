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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class PlayListFragment extends Fragment {

    private Context mContext;
    private ArrayList<PlayListPOJO> obj = new ArrayList<>();
    private PlayListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;

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
        queryDataFromServer();
    }

    private void queryDataFromServer() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://puls-radio.ru/playlist/", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                startParserPlayList(responseBody);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }

        });
    }

    private void startParserPlayList(byte[] responseBody) {
        new Thread(() -> {
            String response = new String(responseBody);
            Document document = Jsoup.parse(response, "puls-radio.ru");
            Elements elementsTrackName = document.select("div.track");
            Elements elementsDate = document.select("div.d");
            obj.clear();
            for (int i = 0; i < elementsTrackName.size(); i++){
                obj.add(new PlayListPOJO(elementsTrackName.get(i).text(), elementsDate.get(i).text()));
                if(i == 50) break;
            }
            if(getActivity() != null){
                getActivity().runOnUiThread(() -> {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mProgressBar.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                });
            }
        }).start();
    }

}
