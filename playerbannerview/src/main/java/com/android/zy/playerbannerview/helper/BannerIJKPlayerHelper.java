package com.android.zy.playerbannerview.helper;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.android.zy.playerbannerview.util.NetWorkUtil;
import com.android.zy.playerbannerview.util.SharedPreferencesUtil;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by zy on 2017/12/1
 */

public class BannerIJKPlayerHelper {
    private final static String TAG = "BannerIJKPlayerHelper";
    private final static String KEY_LAST_ITEM_POSITION = "key_last_item_position";
    private final static String KEY_PREF_PLAY_POSITION = "key_banner_play_position";

    private IjkMediaPlayer mPreviousPlayer = null;
    private IjkMediaPlayer mCurrentPlayer = null;
    private OnBannerPlayerListener onBannerPlayerListener;

    private Context mContext;
    private int mCurrentPosition; //viewpager position

    public BannerIJKPlayerHelper(@NonNull Context context, OnBannerPlayerListener listener) {
        this.mContext = context;
        this.onBannerPlayerListener = listener;
    }

    public static int getmPreItem(Context context) {
        return getLastItemPosition(context);
    }

    public static void setmPreItem(Context context, int itemPosition) {
        saveLastItemPosition(context, itemPosition);
    }


    private IMediaPlayer.OnInfoListener infoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
            if (mCurrentPlayer != null) {
                Log.i(TAG, "onInfo: what==" + what);
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        Log.i(TAG, "buffering start");
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        Log.i(TAG, "buffering end");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        Log.i(TAG, "rendering start");
                        break;
                }
            }
            return true;
        }
    };

    private IMediaPlayer.OnCompletionListener completionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            Log.i(TAG, "onCompletion");
            //do nothing ,loop the mediaplayer by call MediaPlayer.setLooping(true)
            if (iMediaPlayer != null && NetWorkUtil.isNetWorkConnected(mContext)) {
                Log.i(TAG, "new loop");
                iMediaPlayer.stop();
                iMediaPlayer.prepareAsync();
            }
        }
    };


    private IMediaPlayer.OnPreparedListener preparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            Log.i(TAG, "onPrepared");
            if (onBannerPlayerListener != null) {
                boolean isPrepared = onBannerPlayerListener.onPrepared(iMediaPlayer);
                if (!isPrepared) {
                    iMediaPlayer.pause();
                    return;
                }
            }

            Log.i(TAG, "start to seek");
            long progress = getVideoProgress(mContext, mCurrentPosition);
            if (progress > 0) {
                mCurrentPlayer.seekTo(progress);
            } else {
                mCurrentPlayer.start();
            }
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener bufferingUpdateListener =
            new IMediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int progress) {
                    Log.i(TAG, "onPlayerBuffering()...v=" + progress);
                    if (onBannerPlayerListener != null) {
                        onBannerPlayerListener.onBufferingUpdate(iMediaPlayer, progress);
                    }
                }
            };


    public void prepareToPlay() {
        if (mCurrentPlayer != null) {
            mCurrentPlayer.prepareAsync();
            mCurrentPlayer.setOnCompletionListener(completionListener);
        }
    }

    public void startPlay(Context context, int position) {
        this.mCurrentPosition = position;
        prepareToPlay();
    }

    public void startOrPausePlay(boolean isStart) {
        if (mCurrentPlayer != null) {
            if (isStart) {
                mCurrentPlayer.start();
            } else {
                mCurrentPlayer.pause();
            }
        }
    }

    public void setUpPlayer(@NonNull String url, Surface surface, SurfaceView surfaceView) {
        mCurrentPlayer = new IjkMediaPlayer();
        mCurrentPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mCurrentPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format",
                IjkMediaPlayer.SDL_FCC_RV32);
        mCurrentPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 12);
        mCurrentPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
        mCurrentPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", "android");
        mCurrentPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        mCurrentPlayer.setOnBufferingUpdateListener(bufferingUpdateListener);
        mCurrentPlayer.setOnInfoListener(infoListener);
        mCurrentPlayer.setScreenOnWhilePlaying(true);
        mCurrentPlayer.setOnPreparedListener(preparedListener);
        mCurrentPlayer.setOnCompletionListener(completionListener);
        try {
            mCurrentPlayer.setDataSource(url);
            if (surfaceView != null) {
                mCurrentPlayer.setDisplay(surfaceView.getHolder());
            } else if (surface != null && mCurrentPlayer != null) {
                mCurrentPlayer.setSurface(surface);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param position viewPager position
     */
    public void releasePlayer(Context context, int position) {
        mPreviousPlayer = mCurrentPlayer;
        mCurrentPlayer = null;
        if (mPreviousPlayer != null) {
            try {
                mPreviousPlayer.setVolume(0, 0);
                mPreviousPlayer.stop();
                mPreviousPlayer.release();
                mPreviousPlayer = null;
            } catch (Exception e) {
                Log.e(TAG, "[releasePlayer]:error");
            }
        }
    }

    /**
     * 异步释放播放器资源(其实作用似乎并不大，github上ijk开发者提过异步释放并不能完全解决anr的问题)
     */
    public void releaseSoftPlayerAsync(final Context context, final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                releasePlayer(context, position);
            }
        }).start();
    }

    private static void saveLastItemPosition(Context context, int lastItemPosition) {
        SharedPreferencesUtil.putInt(context, KEY_LAST_ITEM_POSITION, lastItemPosition);
    }

    private static int getLastItemPosition(Context context) {
        return SharedPreferencesUtil.getInt(context, KEY_LAST_ITEM_POSITION, 0);
    }

    public void saveProgress(Context context, int currentItemPosition) {
        long progress = 0;
        if (mCurrentPlayer != null) {
            progress = mCurrentPlayer.getCurrentPosition();
        }
        if (progress > 0) {
            saveVideoProgress(context, currentItemPosition, progress);
        }
    }

    private void saveVideoProgress(Context context, int currentItem, long playPosition) {
        SharedPreferencesUtil.putLong(context, KEY_PREF_PLAY_POSITION + currentItem, playPosition);
    }

    private long getVideoProgress(Context context, int currentItem) {
        return SharedPreferencesUtil.getLong(context, KEY_PREF_PLAY_POSITION + currentItem, 0);
    }

    /**
     * 调用此静态方法主动清空当前存储的播放记录，可以在Application中调用，也可以在Activity生命周期中调用.
     * 从而实现在不同阶段存储播放记录
     */
    public static void resetVideoProgress(Context context) {
        for (int i = 0; i < 10; i++) {
            SharedPreferencesUtil.putLong(context, KEY_PREF_PLAY_POSITION + i, 0);
        }
        SharedPreferencesUtil.putInt(context, KEY_LAST_ITEM_POSITION, 0);
    }

    public interface OnBannerPlayerListener {
        public boolean onPrepared(IMediaPlayer iMediaPlayer);

        public void onInfo(IMediaPlayer iMediaPlayer, int what, int extra);

        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int progress);

        public void onStartPlay();

        public void onSetUpPlayer(SurfaceView surfaceView);

        public void onCompletion(IMediaPlayer iMediaPlayer);
    }
}
