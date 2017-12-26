package com.android.zy.myapplication.model;

import com.android.zy.playerbannerview.model.IPlayerMediaData;

/**
 * Created by zy on 2017/12/26.
 */

public class MyMediaBean implements IPlayerMediaData {
    private String mMediaUrl;
    private String mMediaTitle;

    @Override
    public String getMediaUrl() {
        return mMediaUrl;
    }

    @Override
    public String getMediaTitle() {
        return mMediaTitle;
    }

    public void setmMediaUrl(String mMediaUrl) {
        this.mMediaUrl = mMediaUrl;
    }


    public void setmMediaTitle(String mMediaTitle) {
        this.mMediaTitle = mMediaTitle;
    }

}
