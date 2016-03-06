package com.box.androidsdk.share.activities;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.Toast;

import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.R;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Base class for all activities that make API requests through the Box Content SDK. This class is responsible for
 * showing a loading spinner while a request is executing and then hiding it when the request is complete.
 *
 * All BoxRequest tasks should be submitted to getApiExecutor and then handled by overriding handleBoxResponse
 */
public abstract class BoxActivity extends ActionBarActivity {

    public static final String EXTRA_ITEM = "extraItem";
    public static final String EXTRA_USER_ID = "extraUserId";

    protected BoxSession mSession;
    protected BoxItem mShareItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String userId = null;
        if (savedInstanceState != null && savedInstanceState.getSerializable(EXTRA_ITEM) != null){
            userId = savedInstanceState.getString(EXTRA_USER_ID);
            mShareItem = (BoxItem)savedInstanceState.getSerializable(EXTRA_ITEM);

        } else if (getIntent() != null) {
            userId = getIntent().getStringExtra(EXTRA_USER_ID);
            mShareItem = (BoxItem)getIntent().getSerializableExtra(EXTRA_ITEM);
        }

        if (SdkUtils.isBlank(userId)) {
            Toast.makeText(this, R.string.box_sharesdk_session_is_not_authenticated, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (mShareItem == null){
            Toast.makeText(this, R.string.box_sharesdk_no_item_selected, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mSession = new BoxSession(this, userId);
        mSession.setSessionAuthListener(new BoxAuthentication.AuthListener() {
            @Override
            public void onRefreshed(BoxAuthentication.BoxAuthenticationInfo info) {

            }

            @Override
            public void onAuthCreated(BoxAuthentication.BoxAuthenticationInfo info) {

            }

            @Override
            public void onAuthFailure(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
                finish();
                Toast.makeText(BoxActivity.this, R.string.box_sharesdk_session_is_not_authenticated, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLoggedOut(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
                
            }
        });
        mSession.authenticate();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
       outState.putSerializable(EXTRA_ITEM,mShareItem);
       outState.putString(EXTRA_USER_ID, mSession.getUser().getId());
        super.onSaveInstanceState(outState);
    }


    /**
     * Helper method to initialize the activity with the default toolbar for the Share SDK.
     * This will show a material themed toolbar with a back button that will finish the Activity.
     */
    protected void initToolbar() {
        Toolbar actionBar = (Toolbar) findViewById(R.id.box_action_bar);
        setSupportActionBar(actionBar);
        actionBar.setTitle(getTitle());
        actionBar.setNavigationIcon(R.drawable.ic_box_sharesdk_arrow_back_grey_24dp);
        actionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
