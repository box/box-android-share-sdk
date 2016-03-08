package com.box.androidsdk.share.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.box.androidsdk.content.BoxApiBookmark;
import com.box.androidsdk.content.BoxApiCollaboration;
import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.api.BoxShareController;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.fragments.InviteCollaboratorsFragment;

/**
 * Activity used to allow users to invite additional collaborators to the folder. Email addresses will auto complete from the phones address book
 * as well as Box's internal invitee endpoint. The intent to launch this activity can be retrieved via the static getLaunchIntent method
 */
public class BoxInviteCollaboratorsActivity extends BoxActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_collaborators);
        initToolbar();

        mFragment = (InviteCollaboratorsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (mFragment == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_NONE);
            mFragment = InviteCollaboratorsFragment.newInstance((BoxFolder) mShareItem, mSession);
            ft.add(R.id.fragmentContainer, mFragment);
            ft.commit();
        }
        mFragment.SetController(mController);
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
