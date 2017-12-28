package com.android.zy.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ScrollView;

import com.android.zy.myapplication.adapter.MyBannerPlayerAdapter;
import com.android.zy.myapplication.model.MyMediaBean;
import com.android.zy.playerbannerview.adapter.BannerPlayerAdapter;
import com.android.zy.playerbannerview.event.ScreenLockEvent;
import com.android.zy.playerbannerview.helper.BannerIJKPlayerHelper;
import com.android.zy.playerbannerview.view.BannerPlayerView;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MainActivity extends Activity {
    private final static String TAG = "MainActivity";
    private final static String TEST_MEDIA_URL = "http://cds-wotv.jstv.com/mp4/70013872_700.mp4?token=EDC475629F04DBB0?auth_key=1514278897-0-0-0d70a7f473e39c0770cb1b37e6357fbd";
    //    private final static String TEST_MEDIA_URL = "";
    private BannerPlayerView<MyMediaBean> mBannerPlayerView = null;
    private BannerPlayerAdapter<MyMediaBean> mBannerPlayerAdapter = null;
    private MyScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BannerIJKPlayerHelper.resetVideoProgress(this);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //发送解锁事件
        EventBus.getDefault().post(new ScreenLockEvent(false));
    }

    @Override
    protected void onPause() {
        super.onPause();
        //发送锁屏事件
        EventBus.getDefault().post(new ScreenLockEvent(true));
    }

    private void initView() {
        mBannerPlayerView = findViewById(R.id.banner_player_view);
        //create adapter
        mBannerPlayerAdapter = new MyBannerPlayerAdapter(this);
        //bind adapter
        mBannerPlayerView.bindAdapter(mBannerPlayerAdapter);
        //notify change
        mBannerPlayerView.notifyDataSetChanged(produceFakeData());

        mScrollView = findViewById(R.id.scrollView);
        mScrollView.setmBannerLocationListener(mBannerPlayerView);

    }

    private List<MyMediaBean> produceFakeData() {
        List<MyMediaBean> mediaBeen = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MyMediaBean myMediaBean = new MyMediaBean();
            myMediaBean.setmMediaTitle("测试BannerPlayerView_" + i);
            myMediaBean.setmMediaUrl(TEST_MEDIA_URL);
            mediaBeen.add(myMediaBean);
        }
        return mediaBeen;
    }
}
