package com.android.zy.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ScrollView;

import com.android.zy.playerbannerview.listener.IBannerLocationListener;

/**
 * Created by hp on 2017/12/29.
 */

public class MyScrollView extends ScrollView {
    private final static String TAG = "MyScrollView";
    private IBannerLocationListener mBannerLocationListener;

    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setmBannerLocationListener(IBannerLocationListener mBannerLocationListener) {
        if (null == mBannerLocationListener) {
            mBannerLocationListener = new IBannerLocationListener() {
                @Override
                public void onPositionChanged() {
                    //do nothin
                }
            };
        }
        this.mBannerLocationListener = mBannerLocationListener;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        //when match the condition
        if (mBannerLocationListener != null) {
            mBannerLocationListener.onPositionChanged();
        }
    }
}
