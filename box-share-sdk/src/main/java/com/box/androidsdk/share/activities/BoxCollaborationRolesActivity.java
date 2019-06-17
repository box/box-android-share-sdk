package com.box.androidsdk.share.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.fragments.CollaborationRolesDialog;
import com.box.androidsdk.share.fragments.CollaborationsFragment;
import com.box.androidsdk.share.fragments.CollaboratorsRolesFragment;
import com.box.androidsdk.share.fragments.SharedLinkFragment;

import java.util.ArrayList;
import java.util.List;

public class BoxCollaborationRolesActivity extends BoxActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collaboration_roles);
        initToolbar();
    }

    @Override
    protected void initToolbar() {
        super.initToolbar();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); //to get blackText on status bar. will be moved to BoxActivity after all screens are updated.
    }

    @Override
    protected void initializeUi() {
        mFragment = (SharedLinkFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (mFragment == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_NONE);
            String collaboratorName = (String) getIntent().getSerializableExtra(CollaboratorsRolesFragment.ARGS_NAME);
            ArrayList<BoxCollaboration.Role> roles = (ArrayList<BoxCollaboration.Role>) getIntent().getSerializableExtra((CollaboratorsRolesFragment.ARGS_ROLES));
            BoxCollaboration.Role selectedRole = (BoxCollaboration.Role) getIntent().getSerializableExtra(CollaboratorsRolesFragment.ARGS_SELECTED_ROLE);
            boolean allowRemove = getIntent().getBooleanExtra(CollaboratorsRolesFragment.ARGS_ALLOW_REMOVE, false);
            boolean allowOwnerRole = getIntent().getBooleanExtra(CollaboratorsRolesFragment.ARGS_ALLOW_OWNER_ROLE, false);
            BoxCollaboration collaboration = (BoxCollaboration)getIntent().getSerializableExtra(CollaboratorsRolesFragment.ARGS_SERIALIZABLE_EXTRA);
            mFragment = CollaboratorsRolesFragment.newInstance((BoxCollaborationItem) mShareItem, roles, selectedRole, collaboratorName, allowRemove, allowOwnerRole, collaboration);
            ft.add(R.id.fragmentContainer, mFragment);
            ft.commit();
        }
        mFragment.setController(mController);
    }

    public static Intent getLaunchIntent(Context context, BoxItem item, BoxSession session) {
        if (session == null || session.getUser() == null)
            throw new IllegalArgumentException("Invalid user associated with Box session.");

        Intent intent = new Intent(context, BoxCollaborationRolesActivity.class);
        intent.putExtra(CollaborationUtils.EXTRA_ITEM, item);
        intent.putExtra(CollaborationUtils.EXTRA_USER_ID, session.getUser().getId());
        return intent;
    }
    /**
     * Gets a fully formed intent that can be used to start the activity with
     *
     * @param context context to launch the intent in
     * @param item item to view share link information for
     * @param session the session to view the share link information with
     * @return the intent to launch the activity
     */
    public static Intent getLaunchIntent(Context context, BoxItem item, BoxSession session, ArrayList<BoxCollaboration.Role> roles,
                                         BoxCollaboration.Role selectedRole, String name, boolean allowRemove, boolean allowOwnerRole, BoxCollaboration collaboration) {
        Intent intent = getLaunchIntent(context, item, session);
        intent.putExtra(CollaboratorsRolesFragment.ARGS_ROLES, roles);
        intent.putExtra(CollaboratorsRolesFragment.ARGS_SELECTED_ROLE, selectedRole);
        intent.putExtra(CollaboratorsRolesFragment.ARGS_NAME, name);
        intent.putExtra(CollaboratorsRolesFragment.ARGS_ALLOW_REMOVE, allowRemove);
        intent.putExtra(CollaboratorsRolesFragment.ARGS_ALLOW_OWNER_ROLE, allowOwnerRole);
        intent.putExtra(CollaboratorsRolesFragment.ARGS_SERIALIZABLE_EXTRA, collaboration);
        return intent;
    }
}
