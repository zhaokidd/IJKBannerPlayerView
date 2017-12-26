package com.android.zy.myapplication.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.android.zy.myapplication.model.MyMediaBean;
import com.android.zy.playerbannerview.adapter.BannerPlayerAdapter;

import java.util.List;

/**
 * Created by zy on 2017/12/26.
 * adapter
 */

public class MyBannerPlayerAdapter extends BannerPlayerAdapter<MyMediaBean> {
    public MyBannerPlayerAdapter(Context context) {
        super(context);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return super.isViewFromObject(view, object);
    }

    @Override
    public void notifyDataSetChanged(List list) {
        super.notifyDataSetChanged(list);
    }

    @Override
    public Context getmContext() {
        return super.getmContext();
    }
}
