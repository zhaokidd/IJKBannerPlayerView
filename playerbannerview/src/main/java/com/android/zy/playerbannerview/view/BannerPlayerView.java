package com.android.zy.playerbannerview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.zy.playerbannerview.R;
import com.android.zy.playerbannerview.adapter.BannerPlayerAdapter;
import com.android.zy.playerbannerview.event.ScreenLockEvent;
import com.android.zy.playerbannerview.event.SwitchFragmentEvent;
import com.android.zy.playerbannerview.helper.BannerIJKPlayerHelper;
import com.android.zy.playerbannerview.helper.BannerTextureHelper;
import com.android.zy.playerbannerview.listener.IBannerLocationListener;
import com.android.zy.playerbannerview.model.IPlayerMediaData;
import com.android.zy.playerbannerview.util.DensityUtil;
import com.android.zy.playerbannerview.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import tv.danmaku.ijk.media.player.IMediaPlayer;


/**
 * Created by zy on 2017/11/30.
 */

public class BannerPlayerView<T extends IPlayerMediaData> extends FrameLayout implements
        BannerIJKPlayerHelper.OnBannerPlayerListener,
        BannerTextureHelper.onTextureViewChangeListener,
        IBannerLocationListener {
    private final static String TAG = "BannerPlayerView";
    private final static int ID_VIEWPAGER = 10008;
    private final static int DELAY_START_PLAY = 100;
    private final static int DELAY_START_NEW_PLAY = 500;

    private final static int DEFAULT_VIEWPAGER_HEIGHT = 165;//dp
    private final static int DEFAULT_BANNER_HEIGHT = 270;

    private RelativeLayout rootLayout; //root-view
    private ViewGroup mViewParent = null; //parent view of the player
    private ImageView mBackgroundImg;//播放前要显示的背景图
    private ViewPager mViewPager; //viewPagaer
    private BannerPlayerAdapter<T> bannerPlayerAdapter;
    private BannerTextureHelper mTextureHelper;
    private BannerIJKPlayerHelper mIjkHelper;

    //flag
    private boolean mNeedPlay = false;//第一次进入和循环切换的时候需要设置为true
    private boolean mIsViewPagerDragging = false;//viewPager是否正在拖拽
    private boolean mIsHorizontallyDragging = false;//记录页面是否正在水平横滑（横滑状态不再在surfaceDestroyed里记录进度，避免记录错误进度）
    private boolean mIsBannerPlayerViewSelected = true;//viewPager所在的fragment如果有相邻的fragment时，会因为提前加载而导致后台播放视频

    private String mUrl;
    private int mCurrItem = 1; //current pager position
    private TextView tvMediaTitle;//影片名称
    private List<T> itemBeen = new ArrayList<>(); //存储顶部播放信息
    private Runnable mStartNewPlayerRunnable = null;

    //attr
    private float mTextSize;// title text size
    private int mColor;//title text res
    private int mBackgroundPicRes;//background pic res
    private int mForegroundPicRes;//foreground pic res
    private int mViewPagerMargin;
    private int mViewPagerOffset;//margin of the sibling views in viewpager
    private int mTitleTopMargin;
    private int mBannerHeight;
    private int mViewPagerHeight;

    public BannerPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public BannerPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.BannerPlayerView,       //which attrs we want to use
                R.attr.banner_player_view_style,   // default style in app Theme
                R.style.DefaultBannerPlayerView); //  default style in styles.xml

        mColor = typedArray.getColor(
                R.styleable.BannerPlayerView_bpv_title_text_color,
                context.getResources().getColor(R.color.default_banner_back_color));
        mTextSize = typedArray.getDimensionPixelSize(
                R.styleable.BannerPlayerView_bpv_title_text_size,
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        16.0f,
                        context.getResources().getDisplayMetrics()));
        mBackgroundPicRes = typedArray.getResourceId(
                R.styleable.BannerPlayerView_bpv_background_pic,
                0);
        mForegroundPicRes = typedArray.getResourceId(
                R.styleable.BannerPlayerView_bpv_foreground_pic, 0);
        mViewPagerMargin =
                typedArray.getDimensionPixelOffset(
                        R.styleable.BannerPlayerView_bpv_viewpager_margin,
                        DensityUtil.dip2px(context, 20));
        mViewPagerOffset =
                typedArray.getDimensionPixelOffset(
                        R.styleable.BannerPlayerView_bpv_viewpager_offset,
                        DensityUtil.dip2px(context, 10));
        mTitleTopMargin =
                typedArray.getDimensionPixelOffset(
                        R.styleable.BannerPlayerView_bpv_title_top_margin,
                        DensityUtil.dip2px(context, 10));
        mViewPagerHeight = typedArray.getDimensionPixelOffset(
                R.styleable.BannerPlayerView_bpv_viewpager_height, DEFAULT_VIEWPAGER_HEIGHT);
        mBannerHeight = typedArray.getDimensionPixelOffset(
                R.styleable.BannerPlayerView_bpv_banner_height, DEFAULT_BANNER_HEIGHT);
        if (mViewPagerHeight <= 0) {
            mViewPagerHeight = DensityUtil.dip2px(context, DEFAULT_VIEWPAGER_HEIGHT);
        }
        if (mBannerHeight <= 0) {
            mBannerHeight = DensityUtil.dip2px(context, DEFAULT_BANNER_HEIGHT);
        }
        initView();
    }

    private void initView() {
        //add root layout
        rootLayout = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, mBannerHeight);
        rootLayout.setClipChildren(false);
        rootLayout.setLayerType(LAYER_TYPE_HARDWARE, new Paint());
        rootLayout.setBackgroundResource(mBackgroundPicRes);
        rootLayout.setLayoutParams(params);

        //add viewpager
        RelativeLayout.LayoutParams viewPagerParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, mViewPagerHeight);
        viewPagerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        viewPagerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        viewPagerParams.rightMargin = mViewPagerMargin;
        viewPagerParams.leftMargin = mViewPagerMargin;
        mViewPager = new ViewPager(getContext());
        mViewPager.setClipChildren(false);
        mViewPager.setOffscreenPageLimit(4); //set page off limit
        mViewPager.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        mViewPager.setPageMargin(mViewPagerOffset);
        mViewPager.setOnPageChangeListener(mOnPageChangeListener);
        mViewPager.setLayoutParams(viewPagerParams);
        mViewPager.setId(ID_VIEWPAGER);
        rootLayout.addView(mViewPager, viewPagerParams);

        //add mediaTitle
        tvMediaTitle = new TextView(getContext());
        tvMediaTitle.setTextColor(mColor);
        tvMediaTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        RelativeLayout.LayoutParams textViewParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        textViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        textViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        textViewParams.addRule(RelativeLayout.BELOW, mViewPager.getId());
        textViewParams.topMargin = DensityUtil.dip2px(getContext(), mTitleTopMargin);
        tvMediaTitle.setLayoutParams(textViewParams);
        rootLayout.addView(tvMediaTitle, textViewParams);
        addView(rootLayout);
//        setOnScrollChangeListener(mScrollListener);
        mIjkHelper = new BannerIJKPlayerHelper(getContext(), this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.i(TAG, "onAttachedToWindow()...");
        EventBus.getDefault().register(this);
    }

    /**
     * only be called when use {@link View#setVisibility(int)} or go back to launcher
     */
    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        Log.i(TAG, "onVisibilityChanged() visible=" + (visibility == VISIBLE));
        super.onVisibilityChanged(changedView, visibility);
        mNeedPlay = visibility == VISIBLE;
        if (mNeedPlay) {
            releaseSoftPlayer();
            startPlay(mCurrItem);
        } else {
            releaseSoftPlayer();
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        Log.i(TAG, "onWindowVisibilityChanged()...visible=" + (visibility == VISIBLE));
        Log.i(TAG, "[windowToken]:" + getWindowToken());
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            mNeedPlay = true;
            releaseSoftPlayer();
            startPlay(mCurrItem);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.i(TAG, "onDetachedFromWindow()...");
        super.onDetachedFromWindow();
        BannerIJKPlayerHelper.setmPreItem(getContext(), mCurrItem);
        releaseSoftPlayer();
        mNeedPlay = false;
        EventBus.getDefault().unregister(this);
    }

    /**
     * view无法感知到外部锁屏事件，需要在外部控件的生命周期里适时地发送该Event来控制播放
     */
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onScreenLockEvent(ScreenLockEvent event) {
        if (event != null) {
            if (event.isScreenLock()) {
                releaseSoftPlayer();
            } else {
                releaseSoftPlayer();
                startPlay(mCurrItem);
            }
        }
    }

    /***
     *
     *  banner所在的viewpager嵌入在Fragemnt-A中，
     *  从Fragment-C切换到相邻的Fragment-B，会导致Fragment-A提前加载
     *  通过在外部发送该event来改变Banner的选中状态,避免提前后台播放
     */
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onSwitchFragmentEvent(SwitchFragmentEvent event) {
        if (event != null && mIjkHelper != null) {
            mNeedPlay = event.startPlay;
            mIjkHelper.startOrPausePlay(event.startPlay);
            mIsBannerPlayerViewSelected = event.startPlay;
        }
    }

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(final int position) {
            Log.i(TAG, "onPageSelected() position=" + position);
            if (!mNeedPlay && mCurrItem == position) {
                return;
            }

            //save progress before change position when scroll the viewPager horizontally
            mIjkHelper.saveProgress(getContext(), mCurrItem);
            mCurrItem = position;
            if (position == 0 || position == itemBeen.size() - 1) {
                return;
            }
            mIsViewPagerDragging = false;
            mIsHorizontallyDragging = true;
            mViewPager.removeCallbacks(mStartNewPlayerRunnable);
            mStartNewPlayerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!mIsViewPagerDragging) {
                        startPlay(position);
                    }
                }
            };
            releaseSoftPlayer();
            mViewPager.postDelayed(mStartNewPlayerRunnable, DELAY_START_NEW_PLAY);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            Log.i(TAG, "[state]:" + state);
            if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                mIsViewPagerDragging = true;
            }
            if (state != ViewPager.SCROLL_STATE_IDLE) {
                return;
            }

            if (bannerPlayerAdapter.getCount() > 1) {
                if (mCurrItem < 1) {
                    mCurrItem = bannerPlayerAdapter.getCount() - 2;
                    mNeedPlay = true;
                    mViewPager.setCurrentItem(mCurrItem, false);
                } else if (mCurrItem > bannerPlayerAdapter.getCount() - 2) {
                    mCurrItem = 1;
                    mNeedPlay = true;
                    mViewPager.setCurrentItem(mCurrItem, false);
                }
            }
        }
    };


    private void showIvBackground(final T itemBean) {
        if (mViewParent != null && itemBean != null && !TextUtils.isEmpty(itemBean.getMediaUrl())) {
            if (mBackgroundImg != null) {
                mViewParent.removeView(mBackgroundImg);
            }
            mBackgroundImg = new ImageView(getContext());
            mBackgroundImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mBackgroundImg.setImageResource(mForegroundPicRes);
            mViewParent.addView(mBackgroundImg, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
//            ImageUtil.displayWithNoCache(getContext(), mBackgroundImg, itemBean.getMediaUrl());
        }
    }

    private void startPlay(final int position) {
        if (itemBeen != null && itemBeen.size() > 0 && position < itemBeen.size()) {
            T mediaItem = itemBeen.get(position);
            if (mediaItem != null) {
                mUrl = mediaItem.getMediaUrl();
                Log.i(TAG, "url=" + mUrl);
            }

            View pagerView = mViewPager.findViewById(position);
            if (pagerView != null) {
                Log.i(TAG, "pagerView!=null");
                mViewParent = (ViewGroup) pagerView;
                showIvBackground(mediaItem);
                if (mediaItem != null && !TextUtils.isEmpty(mediaItem.getMediaTitle())) {
                    tvMediaTitle.setText(mediaItem.getMediaTitle());
                }
                mTextureHelper = new BannerTextureHelper(this, mUrl);
                mTextureHelper.init(getContext(), mViewParent);
                mTextureHelper.setIJKMediaHelper(mIjkHelper);
            } else {
                Log.e(TAG, "startPlay()... pagerView==null");
                rootLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        releaseSoftPlayer();
                        startPlay(position);
                    }
                }, DELAY_START_PLAY);
            }
        }
    }

    private void releaseSoftPlayer() {
        if (mViewParent != null) {
            mViewParent.removeAllViews();
        }
        mIjkHelper.releaseSoftPlayerAsync(getContext(), mCurrItem);
    }

    public void notifyDataSetChanged(List<T> been) {
        Log.i(TAG, "notifyDataSetChanged");
        if (been != null) {
            itemBeen.clear();
            itemBeen.addAll(been);
            if (itemBeen.size() > 2) {
                itemBeen.add(0, been.get(been.size() - 1));
                itemBeen.add(been.get(0));
            }

            bannerPlayerAdapter.notifyDataSetChanged(itemBeen);
            if (itemBeen.size() > 0 && itemBeen.size() <= 2) {
                mCurrItem = BannerIJKPlayerHelper.getmPreItem(getContext());
                mViewPager.setCurrentItem(mCurrItem);
            } else if (itemBeen.size() > 2) {
                mViewPager.setCurrentItem(1);
            }
        }
    }

    @Override
    public boolean onPrepared(IMediaPlayer iMediaPlayer) {
        Log.i(TAG, "onPrepared");
        if (mViewParent != null && mBackgroundImg != null) {
            mViewParent.removeView(mBackgroundImg);
        }
        mIsHorizontallyDragging = false;
        if (mViewParent != null && mBackgroundImg != null) {
            mViewParent.removeView(mBackgroundImg);
        }
        if (mIsBannerPlayerViewSelected) {
            mIjkHelper.startOrPausePlay(mIsBannerPlayerViewSelected);
        }
        return mNeedPlay && mIsBannerPlayerViewSelected;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i(TAG, "onSizeChanged");
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int progress) {
        if (progress >= 98) {
            if (mViewParent != null && mBackgroundImg != null) {
                mViewParent.removeView(mBackgroundImg);
            }
        }
    }

    @Override
    public void onStartPlay() {

    }

    @Override
    public void onSurfaceTextureAvaliable(Surface surface, int width, int height) {
        if (mIjkHelper != null && !TextUtils.isEmpty(mUrl) && getVisibility() == VISIBLE) {
            mIjkHelper.setUpPlayer(mUrl, surface, null);
            try {
                mIjkHelper.startPlay(getContext(), mCurrItem);
            } catch (Exception e) {
                Log.e(TAG, "[onSurfaceTextureAvailable] error occurs");
            }
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (!mIsHorizontallyDragging) {
            mIjkHelper.saveProgress(getContext(), mCurrItem);
            mIsHorizontallyDragging = false;
        }
        mIjkHelper.releasePlayer(getContext(), mCurrItem);
        return false;
    }


    @Override
    public void onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {

    }

    @Override
    public void onSetUpPlayer(SurfaceView surfaceView) {
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
    }


    public void setmViewParent(ViewGroup mViewParent) {
        this.mViewParent = mViewParent;
    }


    public BannerPlayerAdapter<T> getBannerPlayerAdapter() {
        return bannerPlayerAdapter;
    }

    public void bindAdapter(BannerPlayerAdapter<T> bannerPlayerAdapter) {
        this.bannerPlayerAdapter = bannerPlayerAdapter;
        if (mViewPager != null) {
            mViewPager.setAdapter(bannerPlayerAdapter);
        }
    }

    @Override
    public void onPositionChanged() {
        int[] locations = new int[2];
        getLocationInWindow(locations);
        if (locations[1] + mBannerHeight < 20) {
            mIjkHelper.startOrPausePlay(false);
        } else if (locations[1] > 0) {
            mIjkHelper.startOrPausePlay(true);
        }
    }
}
