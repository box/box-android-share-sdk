package com.box.androidsdk.share.usx.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.activities.BoxCollaborationsActivity;
import com.box.androidsdk.share.usx.activities.BoxActivity;
import com.box.androidsdk.share.usx.activities.BoxInviteCollaboratorsActivity;
import com.box.androidsdk.share.usx.fragments.SharedLinkAccessFragment;
import com.box.androidsdk.share.usx.fragments.UsxFragment;

/**
 * Activity used to share/unshare an item from Box. The intent to launch this activity can be retrieved via the static getLaunchIntent method
 */
public class BoxUsxActivity extends BoxActivity {

    private static int REQUEST_COLLABORATORS = 32;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usx_activity_usx);
        initToolbar();
    }

    @Override
    protected void initializeUi() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment == null || fragment instanceof UsxFragment) {
            setupUsxFragment();
        }
    }

    private void setupUsxFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        mFragment = UsxFragment.newInstance(baseShareVM.getShareItem(), new UsxFragment.ClickListener() {
            @Override
            public void editAccessClicked() {
                setupSharedLinkAccessFragment();
            }

            @Override
            public void inviteCollabsClicked() {
                startActivityForResult(BoxInviteCollaboratorsActivity.getLaunchIntent(BoxUsxActivity.this,
                        (BoxCollaborationItem) baseShareVM.getShareItem(), mSession), REQUEST_COLLABORATORS);
            }

            @Override
            public void collabsClicked() {
                startActivityForResult(BoxCollaborationsActivity.getLaunchIntent(BoxUsxActivity.this,
                        (BoxCollaborationItem) baseShareVM.getShareItem(), mSession), REQUEST_COLLABORATORS);
            }
        }, mShareVMFactory);
        ft.replace(R.id.fragmentContainer, mFragment);
        ft.commit();
    }


    private void setupSharedLinkAccessFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        SharedLinkAccessFragment fragment = SharedLinkAccessFragment.newInstance(baseShareVM.getShareItem(), mShareVMFactory);
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment instanceof SharedLinkAccessFragment) {
            setupUsxFragment();
        } else {
            super.onBackPressed();
        }
    }


    //
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_COLLABORATORS){
            ((UsxFragment)mFragment).refreshInitialsViews();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Gets a fully formed intent that can be used to start the activity with
     *
     * @param context context to launch the intent in
     * @param item item to view share link information for
     * @param session the session to view the share link information with
     * @return the intent to launch the activity
     */
    public static Intent getLaunchIntent(Context context, BoxItem item, BoxSession session) {
        if (session == null || session.getUser() == null)
            throw new IllegalArgumentException("Invalid user associated with Box session.");

        Intent intent = new Intent(context, BoxUsxActivity.class);
        intent.putExtra(CollaborationUtils.EXTRA_ITEM, item);
        intent.putExtra(CollaborationUtils.EXTRA_USER_ID, session.getUser().getId());
        return intent;
    }

}
