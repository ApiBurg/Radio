package com.vadimfedchuk1994gmail.radio.intarfaces;

import com.vadimfedchuk1994gmail.radio.models.PlayListPOJO;

import java.util.ArrayList;

public interface PlayListViewCallBack {
    void playListOnSuccess(ArrayList<PlayListPOJO> responseObject);
    void playlistOnFailure(int statusCode);
}
