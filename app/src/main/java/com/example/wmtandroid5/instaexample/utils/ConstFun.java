package com.example.wmtandroid5.instaexample.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.SimpleDateFormat;

/**
 * Created by techflitter on 26/9/17.
 */

public class ConstFun {

    public static String getCurrentDateAndTime() {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new java.util.Date()).toString();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
