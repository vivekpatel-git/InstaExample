package com.example.wmtandroid5.instaexample.backgroundTask;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.example.wmtandroid5.instaexample.bean.InstaBean;
import com.example.wmtandroid5.instaexample.utils.Constant;

import net.londatiga.android.instagram.InstagramRequest;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wmtandroid5 on 25/9/17.
 */

public class GetLocationPostAsyncTask extends AsyncTask<URL, Integer, Long> {

    String accessToken;
    Context context;
    NotificationManager mNotifyManager;

    public GetLocationPostAsyncTask(String accessToken, NotificationManager mNotifyManager, Context context) {
        this.accessToken = accessToken;
        this.mNotifyManager = mNotifyManager;
        this.context = context;
    }

    @Override
    protected Long doInBackground(URL... urls) {
        long result = 0;
        try {
            List<NameValuePair> params = new ArrayList<NameValuePair>(1);

            InstagramRequest request = new InstagramRequest(accessToken);
            String response = request.createRequest("GET", "/users/self/media/recent", params);

            if (!response.equals("")) {
                JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
                JSONArray jsonData = jsonObj.getJSONArray("data");

                int length = jsonData.length();

                if (length > 0) {
                    Constant.datalist = new ArrayList<InstaBean>();

                    for (int i = 0; i < length; i++) {
                        JSONObject jsonPhoto = jsonData.getJSONObject(i).getJSONObject("images").getJSONObject("low_resolution");
                        InstaBean instaBean = new InstaBean();

                        try {
                            if (jsonData.getJSONObject(i).getJSONObject("location") != null) {
                                JSONObject jsonLocation = jsonData.getJSONObject(i).getJSONObject("location");
                                instaBean.setUrl(jsonPhoto.getString("url"));
                                instaBean.setLatituted(jsonLocation.getDouble("latitude"));
                                instaBean.setLongituted(jsonLocation.getDouble("longitude"));
                                instaBean.setLocationName(jsonLocation.getString("name"));
                            } else {
                                instaBean.setUrl(jsonPhoto.getString("url"));
                                instaBean.setLatituted(0);
                                instaBean.setLongituted(0);
                                instaBean.setLocationName("");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Constant.datalist.add(instaBean);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Long aLong) {
        super.onPostExecute(aLong);
        mNotifyManager.cancel(1);
        context.sendBroadcast(new Intent("com.example.wmtandroid5.instaexample.updateUI"));
    }
}
