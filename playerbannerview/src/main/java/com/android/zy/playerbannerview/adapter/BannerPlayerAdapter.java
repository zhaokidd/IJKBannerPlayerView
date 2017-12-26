package com.android.zy.playerbannerview.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.zy.playerbannerview.R;
import com.android.zy.playerbannerview.model.IPlayerMediaData;
import com.android.zy.playerbannerview.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zy on 2017/12/1.
 */

public class BannerPlayerAdapter<T extends IPlayerMediaData> extends PagerAdapter {
    private final String TAG = "BannerPlayerAdapter";
    private Context mContext;
    private int mHeightBanner = 270;
    private List<T> mList = new ArrayList<>();

    public BannerPlayerAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null;
        final T item = mList.get(position);
        if (item != null) {
            view = new FrameLayout(mContext);
            view.setBackgroundColor(mContext.getResources().getColor(R.color.default_viewpager_back_color));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    DensityUtil.dip2px(mContext, mHeightBanner));
            view.setId(position);
            container.addView(view, params);
        }
        return view;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        ((ViewPager) container).removeView((View) object);
    }

    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void notifyDataSetChanged(List<T> list) {
        if (list != null) {
            mList.clear();
            mList.addAll(list);
            notifyDataSetChanged();
        }
    }


    public Context getmContext() {
        return mContext;
    }


}
