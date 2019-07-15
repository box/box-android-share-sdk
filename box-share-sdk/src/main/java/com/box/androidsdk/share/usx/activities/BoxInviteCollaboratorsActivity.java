package com.box.androidsdk.share.usx.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.api.BoxShareController;
import com.box.androidsdk.share.usx.fragments.CollaboratorsRolesFragment;
import com.box.androidsdk.share.usx.fragments.InviteCollaboratorsFragment;
import com.box.androidsdk.share.sharerepo.ShareRepo;
import com.box.androidsdk.share.utils.FragmentTitle;
import com.box.androidsdk.share.vm.SelectRoleShareVM;
import com.box.androidsdk.share.vm.ShareVMFactory;

/**
 * Activity used to allow users to invite additional collaborators to the folder. Email addresses will auto complete from the phones address book
 * as well as Box's internal invitee endpoint. The intent to launch this activity can be retrieved via the static getLaunchIntent method
 */
public class BoxInviteCollaboratorsActivity extends BoxActivity implements View.OnClickListener {

    SelectRoleShareVM selectRoleShareVM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usx_activity_invite_collaborators);
        initToolbar();
        selectRoleShareVM = ViewModelProviders.of(this).get(SelectRoleShareVM.class);
    }

    @Override
    protected void initializeUi() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment == null || fragment instanceof InviteCollaboratorsFragment) {
            setupInviteCollabFragment();
        } else {
            setTitles(fragment);
        }
    }

    private void setupInviteCollabFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        mFragment = InviteCollaboratorsFragment.newInstance((BoxCollaborationItem) baseShareVM.getShareItem());
        ft.replace(R.id.fragmentContainer, mFragment, InviteCollaboratorsFragment.TAG);
        ft.commit();
        ((InviteCollaboratorsFragment)mFragment).setOnEditAccessListener(this);
        mFragment.setVMFactory(new ShareVMFactory(
                new ShareRepo(new BoxShareController(mSession)),
                (BoxCollaborationItem) baseShareVM.getShareItem()));
        setTitles(mFragment);
    }


    @Override
    public void onClick(View v) {
        selectRoleShareVM.setAllowOwnerRole(false);
        selectRoleShareVM.setAllowRemove(false);
        selectRoleShareVM.setCollaboration(null);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        CollaboratorsRolesFragment rolesFragment = CollaboratorsRolesFragment.newInstance();
        ft.replace(R.id.fragmentContainer, rolesFragment, CollaboratorsRolesFragment.TAG);
        selectRoleShareVM.setShowSend(false);
        ft.commit();
        setTitles(rolesFragment);
        notifyActionBarChanged();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment instanceof CollaboratorsRolesFragment) { //currently displayed fragment was CollaboratorRoles
            setupInviteCollabFragment();
            selectRoleShareVM.setShowSend(true);
        } else {
            super.onBackPressed();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_invite_collaborators, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem sendMenuItem = menu.findItem(R.id.box_sharesdk_action_send);
        selectRoleShareVM.isShowSend().observe(this, showSend -> {
            sendMenuItem.setVisible(showSend);
            //sendMenuItem.setEnabled(showSend);
        });
        selectRoleShareVM.isSendInvitationEnabled().observe(this, enabled -> {
            if (enabled) {
                sendMenuItem.setEnabled(true);
                sendMenuItem.setIcon(R.drawable.ic_box_sharesdk_send_black_24dp);
            } else {
                sendMenuItem.setEnabled(false);
                sendMenuItem.setIcon(R.drawable.ic_box_sharesdk_send_light_24dp);
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.box_sharesdk_action_send) {
            ((InviteCollaboratorsFragment)mFragment).addCollaborations();
        }

        return super.onOptionsItemSelected(item);
    }
}
