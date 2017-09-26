package com.example.wmtandroid5.instaexample;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wmtandroid5.instaexample.adapter.UserProfileAdapter;
import com.example.wmtandroid5.instaexample.backgroundTask.GetLocationPostAsyncTask;
import com.example.wmtandroid5.instaexample.db.DatabaseHandler;
import com.example.wmtandroid5.instaexample.utils.CircleTransformation;
import com.example.wmtandroid5.instaexample.utils.ConstFun;
import com.example.wmtandroid5.instaexample.utils.Constant;
import com.example.wmtandroid5.instaexample.views.RevealBackgroundView;
import com.squareup.picasso.Picasso;

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
 * Created by techflitter on 26/9/17.
 */

public class InstaUserProfileActivity extends BaseActivity {

    private static final int USER_OPTIONS_ANIMATION_DELAY = 300;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();

    @BindView(R.id.vRevealBackground)
    RevealBackgroundView vRevealBackground;
    @BindView(R.id.rvUserProfile)
    RecyclerView rvUserProfile;

    @BindView(R.id.tlUserProfileTabs)
    TabLayout tlUserProfileTabs;

    @BindView(R.id.ivUserProfilePhoto)
    ImageView ivUserProfilePhoto;
    @BindView(R.id.vUserDetails)
    View vUserDetails;
    @BindView(R.id.vUserStats)
    View vUserStats;
    @BindView(R.id.vUserProfileRoot)
    View vUserProfileRoot;
    @BindView(R.id.txtUserName)
    TextView txtUserName;
    @BindView(R.id.txtUserId)
    TextView txtUserId;
    @BindView(R.id.txtCountFollowingBy)
    TextView txtCountFollowingBy;
    @BindView(R.id.txtCountFollows)
    TextView txtCountFollows;
    @BindView(R.id.txtCountMedia)
    TextView txtCountMedia;
    @BindView(R.id.btnLogout)
    Button btnLogout;

    private int avatarSize;
    private String profilePhoto;
    private UserProfileAdapter userPhotosAdapter;

    private InstagramSession mInstagramSession;
    private Instagram mInstagram;
    InstagramUser instagramUser;
    private MyReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instagram_layout);

        mInstagram = new Instagram(this, Constant.CLIENT_ID, Constant.CLIENT_SECRET, Constant.REDIRECT_URI);
        mInstagramSession = mInstagram.getSession();

        instagramUser = mInstagramSession.getUser();

        this.avatarSize = getResources().getDimensionPixelSize(R.dimen.user_profile_avatar_size);
        this.profilePhoto = instagramUser.profilPicture;

        txtUserName.setText(instagramUser.fullName);
        txtUserId.setText("@" + instagramUser.username);
        txtCountMedia.setText(instagramUser.media);
        txtCountFollows.setText(instagramUser.follows);
        txtCountFollowingBy.setText(instagramUser.followed_by);

        Picasso.with(this)
                .load(profilePhoto)
                .placeholder(R.drawable.img_circle_placeholder)
                .resize(avatarSize, avatarSize)
                .centerCrop()
                .transform(new CircleTransformation())
                .into(ivUserProfilePhoto);

        setupTabs();
        setupUserProfileGrid();
        rvUserProfile.setVisibility(View.VISIBLE);
        tlUserProfileTabs.setVisibility(View.VISIBLE);
        vUserProfileRoot.setVisibility(View.VISIBLE);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInstagramSession.reset();
                DatabaseHandler.getInstance(context).deleteUserMeidaAll();
                DatabaseHandler.getInstance(context).deleteUserMeidaLocationAll();    
                startActivity(new Intent(InstaUserProfileActivity.this, LoginActivity.class));
            }
        });

        Constant.photolist = DatabaseHandler.getInstance(this).getAllUserMediaList();

        userPhotosAdapter = new UserProfileAdapter(this, Constant.photolist);
        rvUserProfile.setAdapter(userPhotosAdapter);
        animateUserProfileOptions();
        animateUserProfileHeader();

        refreshData();
    }

    public void refreshData() {
        if (ConstFun.isNetworkAvailable(InstaUserProfileActivity.this)) {
            new GetInstaPostAsycTask(instagramUser.accessToken, InstaUserProfileActivity.this).execute();
        } else {
            Toast.makeText(this, "Internet connection not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tlUserProfileTabs != null) {
            tlUserProfileTabs.getTabAt(0).select();
        }
        receiver = new MyReceiver(new Handler(), InstaUserProfileActivity.this);
        registerReceiver(receiver, new IntentFilter("com.example.wmtandroid5.instaexample.updateUI"));
    }

    private void setupTabs() {
        tlUserProfileTabs.addTab(tlUserProfileTabs.newTab().setIcon(R.mipmap.ic_grid_on_white));
        tlUserProfileTabs.addTab(tlUserProfileTabs.newTab().setIcon(R.mipmap.ic_place_white));

        tlUserProfileTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tlUserProfileTabs.getSelectedTabPosition() == 1) {
                    startActivity(new Intent(InstaUserProfileActivity.this, LocationMapActivity.class));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setupUserProfileGrid() {
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        rvUserProfile.setLayoutManager(layoutManager);
        rvUserProfile.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                userPhotosAdapter.setLockedAnimations(true);
            }
        });
    }

    private void animateUserProfileOptions() {
        tlUserProfileTabs.setTranslationY(-tlUserProfileTabs.getHeight());
        tlUserProfileTabs.animate().translationY(0).setDuration(300).setStartDelay(USER_OPTIONS_ANIMATION_DELAY).setInterpolator(INTERPOLATOR);
    }

    private void animateUserProfileHeader() {
        vUserProfileRoot.setTranslationY(-vUserProfileRoot.getHeight());
        ivUserProfilePhoto.setTranslationY(-ivUserProfilePhoto.getHeight());
        vUserDetails.setTranslationY(-vUserDetails.getHeight());
        vUserStats.setAlpha(0);

        vUserProfileRoot.animate().translationY(0).setDuration(300).setInterpolator(INTERPOLATOR);
        ivUserProfilePhoto.animate().translationY(0).setDuration(300).setStartDelay(100).setInterpolator(INTERPOLATOR);
        vUserDetails.animate().translationY(0).setDuration(300).setStartDelay(200).setInterpolator(INTERPOLATOR);
        vUserStats.animate().alpha(1).setDuration(200).setStartDelay(400).setInterpolator(INTERPOLATOR).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh_action) {
            refreshData();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class MyReceiver extends BroadcastReceiver {

        private final Handler handler; // Handler used to execute code on the UI thread
        InstaUserProfileActivity activity;

        public MyReceiver(Handler handler, InstaUserProfileActivity activity) {
            this.activity = activity;
            this.handler = handler;
        }

        @Override
        public void onReceive(final Context context, Intent intent) {
            // Post the UI updating code to our Handler
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Update data", Toast.LENGTH_LONG).show();
                    if (Constant.photolist != null && Constant.photolist.size() > 0) {
                        DatabaseHandler.getInstance(context).deleteUserMeidaAll();
                        DatabaseHandler.getInstance(context).addUserMediaList(Constant.photolist);
                    }
                    if (Constant.datalist != null && Constant.datalist.size() > 0) {
                        DatabaseHandler.getInstance(context).deleteUserMeidaLocationAll();
                        DatabaseHandler.getInstance(context).addUserMediaWithLocationList(Constant.datalist);
                    }
                    Constant.photolist = DatabaseHandler.getInstance(context).getAllUserMediaList();
                    activity.userPhotosAdapter.setData(Constant.photolist);
                }
            });
        }
    }

    class GetInstaPostAsycTask extends AsyncTask<URL, Integer, Long> {

        String accessToken;
        Context context;
        NotificationManager mNotifyManager;

        public GetInstaPostAsycTask(String accessToken, Context context) {
            this.accessToken = accessToken;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setContentTitle("Refresh Data");
            mBuilder.setSmallIcon(R.drawable.ic_launcher);
            mBuilder.setProgress(0, 0, true);
            mNotifyManager.notify(1, mBuilder.build());
        }

        @Override
        protected Long doInBackground(URL... urls) {
            long result = 0;
            Log.d("mytag", "accessToken::" + accessToken);
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>(1);

                InstagramRequest request = new InstagramRequest(accessToken);
                String response = request.createRequest("GET", "/users/self/media/recent", params);

                if (!response.equals("")) {
                    JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
                    JSONArray jsonData = jsonObj.getJSONArray("data");

                    int length = jsonData.length();

                    if (length > 0) {
                        Constant.photolist = new ArrayList<String>();

                        for (int i = 0; i < length; i++) {
                            JSONObject jsonPhoto = jsonData.getJSONObject(i).getJSONObject("images").getJSONObject("low_resolution");
                            Constant.photolist.add(jsonPhoto.getString("url"));
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
            new GetLocationPostAsyncTask(mInstagramSession.getAccessToken(), mNotifyManager, context).execute();
        }
    }
}
