package com.android.zy.playerbannerview.helper;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by zy on 2017/12/1.
 */

public class BannerTextureHelper {
    private final static String TAG = "BannerTextureHelper";
    private String mUrl;
    Surface mSurface = null;
    TextureView mTextureView = null;
    BannerIJKPlayerHelper mIjkPlayerHelper;
    private onTextureViewChangeListener textureViewChangeListener;

    public BannerTextureHelper(onTextureViewChangeListener listener, String mUrl) {
        this.textureViewChangeListener = listener;
        this.mUrl = mUrl;
    }

    private void createTextureView(Context context) {
        mTextureView = new TextureView(context);
        mTextureView.setSurfaceTextureListener(textureListener);
    }

    private void addToParentView(ViewGroup parentView) {
        if (mTextureView != null && parentView != null) {
            parentView.addView(mTextureView, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    public void init(Context context, @NonNull ViewGroup parentView) {
        createTextureView(context);
        addToParentView(parentView);
    }


    public void setIJKMediaHelper(BannerIJKPlayerHelper helper) {
        this.mIjkPlayerHelper = helper;
    }


    private TextureView.SurfaceTextureListener
            textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.i(TAG, "[onSurfaceTextureAvailable]: width" + width + "  height" + height);
            mSurface = new Surface(surface);
            if (textureViewChangeListener != null) {
                textureViewChangeListener.onSurfaceTextureAvaliable(mSurface, width, height);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mSurface = new Surface(surface);

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (textureViewChangeListener != null) {
                return textureViewChangeListener.onSurfaceTextureDestroyed(surface);
            }
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    public interface onTextureViewChangeListener {
        public void onSurfaceTextureAvaliable(Surface surface, int width, int height);

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture);
    }
}
