package com.box.androidsdk.share.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.view.View;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.api.BoxShareController;
import com.box.androidsdk.share.fragments.CollaboratorsRolesFragment;
import com.box.androidsdk.share.fragments.InviteCollaboratorsFragment;
import com.box.androidsdk.share.sharerepo.ShareRepo;
import com.box.androidsdk.share.vm.SelectRoleShareVM;
import com.box.androidsdk.share.vm.ShareVMFactory;

/**
 * Activity used to allow users to invite additional collaborators to the folder. Email addresses will auto complete from the phones address book
 * as well as Box's internal invitee endpoint. The intent to launch this activity can be retrieved via the static getLaunchIntent method
 */
public class BoxInviteCollaboratorsActivity extends BoxActivity implements View.OnClickListener{

    private static int REQUEST_SHOW_COLLABORATORS = 32;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_collaborators);
        initToolbar();

    }

    @Override
    protected void initToolbar() {
        super.initToolbar();
        //to get blackText on status bar. will be moved to BoxActivity after all screens are updated.
    }

    @Override
    protected void initializeUi() {
        mFragment = (InviteCollaboratorsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (mFragment == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_NONE);
            mFragment = InviteCollaboratorsFragment.newInstance((BoxCollaborationItem) mShareItem);
            ft.replace(R.id.fragmentContainer, mFragment, InviteCollaboratorsFragment.TAG);
            ft.commit();
        }
        mFragment.setController(mController);

        ((InviteCollaboratorsFragment)mFragment).setOnEditAccessListener(this);
        mFragment.setVMFactory(new ShareVMFactory(new ShareRepo(new BoxShareController(mSession)), (BoxCollaborationItem) mShareItem));
    }

    @Override
    public void onClick(View v) {
        ((InviteCollaboratorsFragment)mFragment).updateSelectRoleViewModelForInvitingNewCollabs();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        CollaboratorsRolesFragment rolesFragment = CollaboratorsRolesFragment.newInstance((BoxCollaborationItem) mShareItem);
        ft.replace(R.id.fragmentContainer, rolesFragment).addToBackStack("asd");
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getSupportFragmentManager().popBackStack();
        BoxCollaboration.Role role = ViewModelProviders.of(this).get(SelectRoleShareVM.class).getSelectedRole();
        ((InviteCollaboratorsFragment)mFragment).setRole(role);
    }

    /**
     * Gets a fully formed intent that can be used to start the activity with
     *
     * @param context context to launch the intent in
     * @param collaborationItem item to add collaborators to
     * @param session the session to add collaborators with
     * @return the intent to launch the activity
     */
    public static Intent getLaunchIntent(Context context, BoxCollaborationItem collaborationItem, BoxSession session) {
        if (collaborationItem == null || SdkUtils.isBlank(collaborationItem.getId()) || SdkUtils.isBlank(collaborationItem.getType()))
            throw new IllegalArgumentException("A valid collaboration item must be provided for retrieving collaborations");
        if (session == null || session.getUser() == null ||  SdkUtils.isBlank(session.getUser().getId()))
            throw new IllegalArgumentException("A valid user must be provided for retrieving collaborations");

        Intent inviteIntent = new Intent(context, BoxInviteCollaboratorsActivity.class);
        inviteIntent.putExtra(CollaborationUtils.EXTRA_ITEM, collaborationItem);
        inviteIntent.putExtra(CollaborationUtils.EXTRA_USER_ID, session.getUser().getId());
        return inviteIntent;
    }
}
