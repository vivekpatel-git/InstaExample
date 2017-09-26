package com.example.wmtandroid5.instaexample;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.wmtandroid5.instaexample.backgroundTask.GetLocationPostAsyncTask;
import com.example.wmtandroid5.instaexample.utils.Constant;

import net.londatiga.android.instagram.Instagram;
import net.londatiga.android.instagram.InstagramRequest;
import net.londatiga.android.instagram.InstagramSession;
import net.londatiga.android.instagram.InstagramUser;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by wmtandroid5 on 25/9/17.
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.btnLogin)
    Button btnLogin;
    private InstagramSession mInstagramSession;
    private Instagram mInstagram;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_layout);

        initView();
    }

    private void initView() {
        btnLogin.setOnClickListener(this);

        mInstagram = new Instagram(this, Constant.CLIENT_ID, Constant.CLIENT_SECRET, Constant.REDIRECT_URI);

        mInstagramSession = mInstagram.getSession();

        if (mInstagramSession.isActive()) {
            finish();
            startActivity(new Intent(LoginActivity.this, InstaUserProfileActivity.class));
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnLogin) {
            mInstagram.authorize(mAuthListener);
        }
    }

    private Instagram.InstagramAuthListener mAuthListener = new Instagram.InstagramAuthListener() {
        @Override
        public void onSuccess(InstagramUser user) {
            finish();
            startActivity(new Intent(LoginActivity.this, InstaUserProfileActivity.class));
        }

        @Override
        public void onError(String error) {
            showToast(error);
        }

        @Override
        public void onCancel() {
            showToast("OK. Maybe later?");

        }
    };

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


}
