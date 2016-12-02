package com.box.androidsdk.share.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.box.androidsdk.content.BoxApiBookmark;
import com.box.androidsdk.content.BoxApiCollaboration;
import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.api.BoxShareController;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.fragments.BoxFragment;
import com.box.androidsdk.share.internal.BoxApiFeatures;
import com.box.androidsdk.share.internal.BoxApiInvitee;

/**
 * Base class for all activities that make API requests through the Box Content SDK. This class is responsible for
 * showing a loading spinner while a request is executing and then hiding it when the request is complete.
 *
 * All BoxRequest tasks should be submitted to getApiExecutor and then handled by overriding handleBoxResponse
 */
public abstract class BoxActivity extends ActionBarActivity {

    protected BoxSession mSession;
    protected BoxItem mShareItem;
    protected BoxFragment mFragment;
    protected ShareController mController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String userId = null;
        if (savedInstanceState != null && savedInstanceState.getSerializable(CollaborationUtils.EXTRA_ITEM) != null){
            userId = savedInstanceState.getString(CollaborationUtils.EXTRA_USER_ID);
            mShareItem = (BoxItem)savedInstanceState.getSerializable(CollaborationUtils.EXTRA_ITEM);

        } else if (getIntent() != null) {
            userId = getIntent().getStringExtra(CollaborationUtils.EXTRA_USER_ID);
            mShareItem = (BoxItem)getIntent().getSerializableExtra(CollaborationUtils.EXTRA_ITEM);
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
                mController.showToast(BoxActivity.this, R.string.box_sharesdk_session_is_not_authenticated);
            }

            @Override
            public void onLoggedOut(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
                
            }
        });
        mSession.authenticate();
        mController = new BoxShareController(mSession);
    }


    @Override
    public void finish() {
        Intent data = new Intent();
        mFragment.addResult(data);
        setResult(Activity.RESULT_OK, data);
        super.finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
       outState.putSerializable(CollaborationUtils.EXTRA_ITEM,mShareItem);
       outState.putString(CollaborationUtils.EXTRA_USER_ID, mSession.getUser().getId());
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

    // Class to interpret result from share SDK activities
    public static class ResultInterpreter {

        Intent mData;

        public ResultInterpreter(Intent data){
            mData = data;
        }

        public BoxItem getBoxItem() {
            return (BoxItem) mData.getSerializableExtra(CollaborationUtils.EXTRA_ITEM);
        }

        public BoxIteratorCollaborations getCollaborations() {
            return (BoxIteratorCollaborations) mData.getSerializableExtra(CollaborationUtils.EXTRA_COLLABORATIONS);
        }
    }
}
