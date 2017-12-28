package com.android.zy.playerbannerview.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;


@SuppressLint("NewApi")
public class NetWorkUtil {
    private final static String TAG = "NetWorkUtil";

    /**
     * 检测网络连接是否通畅
     *
     * @return 网络是否连接
     */
    public static boolean isNetWorkConnected(Context context) {
        boolean isConnectionAlive = false;
        //检测API是不是小于23，因为到了API23之后getNetworkInfo(int networkType)方法被弃用
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //获取ConnectivityManager对象对应的NetworkInfo对象
            //获取WIFI连接的信息
            NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //获取移动数据连接的信息
            NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                Log.e(TAG, "WIFI已连接,移动数据已连接");
            } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
                Log.e(TAG, "WIFI已连接,移动数据已断开");
            } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                Log.e(TAG, "WIFI已断开,移动数据已连接");
            } else {
                Log.e(TAG, "WIFI已断开,移动数据已断开");
            }

            if (wifiNetworkInfo.isConnected() || dataNetworkInfo.isConnected()) {
                Log.i(TAG, "isConnected:send request");
                isConnectionAlive = true;
            }

        } else {
            //API大于23时使用下面的方式进行网络监听
            Log.i(TAG, "API >= M(23)");
            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            //获取所有网络连接的信息
            Network[] networks = connMgr.getAllNetworks();
            //用于存放网络连接信息
            StringBuilder sb = new StringBuilder();
            //通过循环将网络信息逐个取出来
            boolean isConnnected = false;
            for (int i = 0; i < networks.length; i++) {
                //获取ConnectivityManager对象对应的NetworkInfo对象
                NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                if (networkInfo.isConnectedOrConnecting()) {
                    isConnnected = true;
                }
                sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
            }
            if (isConnnected) {
                isConnectionAlive = true;
            }

        }
        return isConnectionAlive;
    }
}
