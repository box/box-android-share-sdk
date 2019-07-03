package com.box.androidsdk.share.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.view.Menu;
import android.view.MenuItem;
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
public class BoxInviteCollaboratorsActivity extends BoxActivity implements View.OnClickListener, InviteCollaboratorsFragment.InviteCollaboratorsListener {

    private static int REQUEST_SHOW_COLLABORATORS = 32;
    boolean mSendEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_collaborators);
        initToolbar();

    }

    @Override
    protected void initToolbar() {
        Toolbar actionBar = (Toolbar) findViewById(R.id.box_action_bar);
        setSupportActionBar(actionBar);
        actionBar.setTitle(getTitle());
        actionBar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        actionBar.setNavigationOnClickListener(v -> onBackPressed());
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    public void setActionBarTitle(String title) {
        setTitle(title);
        getSupportActionBar().setTitle(getTitle());
    }


    @Override
    protected void initializeUi() {
        mFragment = (InviteCollaboratorsFragment) getSupportFragmentManager().findFragmentByTag(InviteCollaboratorsFragment.TAG);
        if (mFragment == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_NONE);
            mFragment = InviteCollaboratorsFragment.newInstance((BoxCollaborationItem) mShareItem);
            ft.add(R.id.fragmentContainer, mFragment, InviteCollaboratorsFragment.TAG);
            ft.commit();
        }
        mFragment.setController(mController); //to prevent NOE since BoxFragment still uses this. (will be removed after all screens are implemented)

        ((InviteCollaboratorsFragment)mFragment).setOnEditAccessListener(this);
        ((InviteCollaboratorsFragment)mFragment).setCollaboratorsStateListener(this);
        mFragment.setVMFactory(new ShareVMFactory(new ShareRepo(new BoxShareController(mSession)), (BoxCollaborationItem) mShareItem));
    }

    @Override
    public void onClick(View v) {
        SelectRoleShareVM selectRoleShareVM = ViewModelProviders.of(this).get(SelectRoleShareVM.class);
        selectRoleShareVM.setAllowOwnerRole(false);
        selectRoleShareVM.setAllowRemove(false);
        selectRoleShareVM.setCollaboration(null);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        CollaboratorsRolesFragment rolesFragment = CollaboratorsRolesFragment.newInstance();
        ft.replace(R.id.fragmentContainer, rolesFragment, CollaboratorsRolesFragment.TAG).addToBackStack(null);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_invite_collaborators, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem sendMenuItem = menu.findItem(R.id.box_sharesdk_action_send);
        if (mSendEnabled) {
            sendMenuItem.setEnabled(true);
            sendMenuItem.setIcon(R.drawable.ic_box_sharesdk_send_black_24dp);
        } else {
            sendMenuItem.setEnabled(false);
            sendMenuItem.setIcon(R.drawable.ic_box_sharesdk_send_light_24dp);
        }
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



    @Override
    public void onCollaboratorsPresent() {
        if (!mSendEnabled) {
            mSendEnabled = true;
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onCollaboratorsAbsent() {
        if (mSendEnabled) {
            mSendEnabled = false;
            invalidateOptionsMenu();
        }
    }


}
