package com.box.androidsdk.share.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.fragments.InviteCollaboratorsFragment;

/**
 * Activity used to allow users to invite additional collaborators to the folder. Email addresses will auto complete from the phones address book
 * as well as Box's internal invitee endpoint. The intent to launch this activity can be retrieved via the static getLaunchIntent method
 */
public class BoxInviteCollaboratorsActivity extends BoxActivity implements InviteCollaboratorsFragment.InviteCollaboratorsListener {

    private boolean mSendEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_collaborators);
        initToolbar();

        mFragment = (InviteCollaboratorsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (mFragment == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_NONE);
            mFragment = InviteCollaboratorsFragment.newInstance((BoxFolder) mShareItem);
            ft.add(R.id.fragmentContainer, mFragment);
            ft.commit();
        }
        mFragment.setController(mController);
        ((InviteCollaboratorsFragment)mFragment).setInviteCollaboratorsListener(this);
        mSendEnabled = ((InviteCollaboratorsFragment)mFragment).areCollaboratorsPresent();
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
            sendMenuItem.setIcon(R.drawable.ic_box_sharesdk_send_accent_24dp);
        } else {
            sendMenuItem.setEnabled(false);
            sendMenuItem.setIcon(R.drawable.ic_box_sharesdk_send_light_24dp);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onShowCollaborators(BoxIteratorCollaborations collaborations) {
        Intent collabsIntent = BoxCollaborationsActivity.getLaunchIntent(this, (BoxFolder) mShareItem, mSession, collaborations);
        startActivity(collabsIntent);
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

    /**
     * Gets a fully formed intent that can be used to start the activity with
     *
     * @param context context to launch the intent in
     * @param folder folder to add collaborators to
     * @param session the session to add collaborators with
     * @return the intent to launch the activity
     */
    public static Intent getLaunchIntent(Context context, BoxFolder folder, BoxSession session) {
        if (folder == null || SdkUtils.isBlank(folder.getId()))
            throw new IllegalArgumentException("A valid folder must be provided for retrieving collaborations");
        if (session == null || session.getUser() == null ||  SdkUtils.isBlank(session.getUser().getId()))
            throw new IllegalArgumentException("A valid user must be provided for retrieving collaborations");

        Intent inviteIntent = new Intent(context, BoxInviteCollaboratorsActivity.class);
        inviteIntent.putExtra(CollaborationUtils.EXTRA_ITEM, folder);
        inviteIntent.putExtra(CollaborationUtils.EXTRA_USER_ID, session.getUser().getId());
        return inviteIntent;
    }
}
