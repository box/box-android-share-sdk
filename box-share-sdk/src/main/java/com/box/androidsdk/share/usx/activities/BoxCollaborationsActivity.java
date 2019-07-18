package com.box.androidsdk.share.usx.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.usx.fragments.CollaborationsFragment;
import com.box.androidsdk.share.usx.fragments.CollaboratorsRolesFragment;

/**
 * Activity used to show and modify the collaborations of an item. The intent to launch this activity can be retrieved via the static getLaunchIntent method
 */
public class BoxCollaborationsActivity extends BoxActivity {

    protected static final String TAG = BoxCollaborationsActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usx_activity_collaborations);
        initToolbar();

        if (baseShareVM.getShareItem() == null || baseShareVM.getShareItem().getType() == null || !(baseShareVM.getShareItem() instanceof BoxCollaborationItem)) {
            showToast(R.string.box_sharesdk_selected_item_not_expected_type);
            finish();
            return;
        }

    }

    @Override
    protected void initializeUi() {


        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment == null || fragment instanceof CollaborationsFragment) {
            setupCollaborationsFragment();
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment instanceof CollaboratorsRolesFragment) {
            setupCollaborationsFragment();
        } else {
            super.onBackPressed();
        }

    }

    private void setupCollaborationsFragment() {
        BoxIteratorCollaborations collaborations = null;
        if (getIntent() != null) {
            collaborations = (BoxIteratorCollaborations)getIntent().getSerializableExtra(CollaborationUtils.EXTRA_COLLABORATIONS);
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        mFragment = CollaborationsFragment.newInstance( (BoxCollaborationItem) baseShareVM.getShareItem(), collaborations);
        mFragment.setVMFactory(mShareVMFactory);
        ((CollaborationsFragment)mFragment).setCallback(this::switchToRolesFragment);
        ft.replace(R.id.fragmentContainer, mFragment);
        ft.commit();
    }

    private void switchToRolesFragment() {
        CollaboratorsRolesFragment fragment = CollaboratorsRolesFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
    }
    /**
     * Gets a fully formed intent that can be used to start the activity with
     *
     * @param context context to launch the intent with
     * @param collaborationItem item to retrieve collaborations for
     * @param session the session to view the items collaborations with
     * @return the intent to launch the activity
     */
    public static Intent getLaunchIntent(Context context, BoxCollaborationItem collaborationItem, BoxSession session) {
        if (collaborationItem == null || SdkUtils.isBlank(collaborationItem.getId()) || SdkUtils.isBlank(collaborationItem.getType()))
            throw new IllegalArgumentException("A valid collaboration item must be provided for retrieving collaborations");
        if (session == null || session.getUser() == null || SdkUtils.isBlank(session.getUser().getId()))
            throw new IllegalArgumentException("A valid user must be provided for retrieving collaborations");

        Intent collabIntent = new Intent(context, BoxCollaborationsActivity.class);

        collabIntent.putExtra(CollaborationUtils.EXTRA_ITEM, collaborationItem);
        collabIntent.putExtra(CollaborationUtils.EXTRA_USER_ID, session.getUser().getId());
        return collabIntent;
    }

    /**
     * Gets a fully formed intent that can be used to start the activity with
     *
     * @param context context to launch the intent with
     * @param boxCollaborationItem item to retrieve collaborations for
     * @param session the session to view the items collaborations with
     * @return the intent to launch the activity
     */
    public static Intent getLaunchIntent(Context context, BoxCollaborationItem boxCollaborationItem, BoxSession session, BoxIteratorCollaborations collaborations) {
        Intent collabIntent = getLaunchIntent(context, boxCollaborationItem, session);
        collabIntent.putExtra(CollaborationUtils.EXTRA_COLLABORATIONS, collaborations);
        return collabIntent;
    }
}
