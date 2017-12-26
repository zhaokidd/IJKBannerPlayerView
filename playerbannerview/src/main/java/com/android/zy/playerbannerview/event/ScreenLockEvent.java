package com.android.zy.playerbannerview.event;

/**
 * Created by zy on 2017/9/12.
 */

public class ScreenLockEvent {
    private boolean isScreenLock;

    public ScreenLockEvent(boolean isLock) {
        this.isScreenLock = isLock;
    }

    public boolean isScreenLock() {
        return isScreenLock;
    }

    public void setScreenLock(boolean screenLock) {
        isScreenLock = screenLock;
    }
}
