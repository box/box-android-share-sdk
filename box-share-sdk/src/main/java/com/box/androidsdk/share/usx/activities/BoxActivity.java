package com.box.androidsdk.share.usx.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.box.androidsdk.content.BoxConfig;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.api.BoxShareController;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.sharerepo.ShareRepo;
import com.box.androidsdk.share.usx.fragments.BoxFragment;
import com.box.androidsdk.share.vm.ActionbarTitleVM;
import com.box.androidsdk.share.vm.BaseShareVM;
import com.box.androidsdk.share.vm.ShareVMFactory;

/**
 * Base class for all activities that make API requests through the Box Content SDK. This class is responsible for
 * showing a loading spinner while a request is executing and then hiding it when the request is complete.
 *
 * All BoxRequest tasks should be submitted to getApiExecutor and then handled by overriding handleBoxResponse
 */
public abstract class BoxActivity extends AppCompatActivity {

    protected BoxSession mSession;
    protected BoxFragment mFragment;
    protected ProgressDialog mProgress;
    protected BaseShareVM baseShareVM;
    protected ShareVMFactory mShareVMFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 23)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        else {
            setTheme(R.style.ShareTheme);
        }

        BoxItem shareItem = null;
        super.onCreate(savedInstanceState);
        if (BoxConfig.IS_FLAG_SECURE){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        String userId = null;
        if (savedInstanceState != null && savedInstanceState.getSerializable(CollaborationUtils.EXTRA_ITEM) != null){
            userId = savedInstanceState.getString(CollaborationUtils.EXTRA_USER_ID);
            shareItem = (BoxItem)savedInstanceState.getSerializable(CollaborationUtils.EXTRA_ITEM);

        } else if (getIntent() != null) {
            userId = getIntent().getStringExtra(CollaborationUtils.EXTRA_USER_ID);
            shareItem = (BoxItem)getIntent().getSerializableExtra(CollaborationUtils.EXTRA_ITEM);
        }

        if (SdkUtils.isBlank(userId)) {
            Toast.makeText(this, R.string.box_sharesdk_session_is_not_authenticated, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (shareItem == null){
            Toast.makeText(this, R.string.box_sharesdk_no_item_selected, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mSession = new BoxSession(this, userId);
        ShareController mController = new BoxShareController(mSession);
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
                Toast.makeText(BoxActivity.this, R.string.box_sharesdk_session_is_not_authenticated, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoggedOut(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {

            }
        });
        mSession.authenticate();
        baseShareVM = ViewModelProviders.of(this, new ShareVMFactory(new ShareRepo(mController), (BoxCollaborationItem) shareItem)).get(BaseShareVM.class);
        if (!isSharedItemSufficient()){
            mProgress = ProgressDialog.show(this, getText(R.string.boxsdk_Please_wait), getText(R.string.boxsdk_Please_wait), true, false);
            baseShareVM.fetchItemInfo(shareItem);
            baseShareVM.getItemInfo().observe(this, response -> {
                if (response.isSuccess()) {
                    baseShareVM.setShareItem(response.getData());
                    if (mProgress != null && mProgress.isShowing()) {
                        mProgress.dismiss();
                    }
                    mShareVMFactory = new ShareVMFactory(new ShareRepo(new BoxShareController(mSession)), (BoxCollaborationItem) response.getData());
                    initializeUi();
                }
            });
        } else {
            mShareVMFactory = new ShareVMFactory(new ShareRepo(new BoxShareController(mSession)), (BoxCollaborationItem) shareItem);
            initializeUi();
        }
    }

    @Override
    protected void onDestroy() {
        if (mProgress != null && mProgress.isShowing()){
            mProgress.dismiss();
        }
        super.onDestroy();
    }

    protected boolean isSharedItemSufficient(){
        return !SdkUtils.isBlank(baseShareVM.getShareItem().getName()) && baseShareVM.getShareItem().getPermissions() != null;
    }

    protected abstract void initializeUi();


    @Override
    public void finish() {
        Intent data = new Intent();
        mFragment.addResult(data);
        setResult(mFragment.getActivityResultCode(), data);
        super.finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(CollaborationUtils.EXTRA_ITEM,baseShareVM.getShareItem());
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
        actionBar.setNavigationIcon(R.drawable.ic_box_sharesdk_arrow_back_black_24dp);
        actionBar.setNavigationOnClickListener(v -> onBackPressed());

        ActionbarTitleVM actionbarTitleVM = ViewModelProviders.of(this).get(ActionbarTitleVM.class);
        actionbarTitleVM.getTitle().observe(this, actionBar::setTitle);
        actionbarTitleVM.getSubtitle().observe(this, actionBar::setSubtitle);

        if (actionBar.getTitle() == null) {
            actionBar.setTitle(getTitle()); //set default title if no title is given by fragment
        }
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

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    protected void showToast(@StringRes int strRes) {
        Toast.makeText(this, getString(strRes), Toast.LENGTH_SHORT).show();
    }
}
