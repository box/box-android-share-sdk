package com.box.androidsdk.share.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.box.androidsdk.content.BoxApiBookmark;
import com.box.androidsdk.content.BoxApiCollaboration;
import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.api.BoxShareController;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.fragments.CollaborationsFragment;

/**
 * Activity used to show and modify the collaborations of a folder. The intent to launch this activity can be retrieved via the static getLaunchIntent method
 */
public class BoxCollaborationsActivity extends BoxActivity {

    protected static final String TAG = BoxCollaborationsActivity.class.getName();
    protected static final int INVITE_COLLABS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collaborations);
        initToolbar();

        if (mShareItem == null || mShareItem.getType() == null || !mShareItem.getType().equals(BoxFolder.TYPE)) {
            Toast.makeText(this, R.string.box_sharesdk_selected_item_not_expected_type, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mFragment = (CollaborationsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (mFragment == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_NONE);
            mFragment = CollaborationsFragment.newInstance((BoxFolder) mShareItem);
            ft.add(R.id.fragmentContainer, mFragment);
            ft.commit();
        }
        mFragment.SetController(mController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_collaborate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.box_sharesdk_action_add) {
            BoxCollaboration.Role[] rolesArr = ((CollaborationsFragment)mFragment).getRoles();
            if (rolesArr != null) {
                Intent inviteCollabsIntent = BoxInviteCollaboratorsActivity.getLaunchIntent(this, (BoxFolder) mShareItem, mSession);
                startActivityForResult(inviteCollabsIntent, INVITE_COLLABS_REQUEST_CODE);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case INVITE_COLLABS_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    // New collaborators have been invited so we should refresh
                    ((CollaborationsFragment)mFragment).fetchCollaborations();
                }
                break;
        }
    }

    /**
     * Gets a fully formed intent that can be used to start the activity with
     *
     * @param context context to launch the intent with
     * @param folder folder to retrieve collaborations for
     * @param session the session to view the folders collaborations with
     * @return the intent to launch the activity
     */
    public static Intent getLaunchIntent(Context context, BoxFolder folder, BoxSession session) {
        if (folder == null || SdkUtils.isBlank(folder.getId()))
            throw new IllegalArgumentException("A valid folder must be provided for retrieving collaborations");
        if (session == null || session.getUser() == null || SdkUtils.isBlank(session.getUser().getId()))
            throw new IllegalArgumentException("A valid user must be provided for retrieving collaborations");

        Intent collabIntent = new Intent(context, BoxCollaborationsActivity.class);

        collabIntent.putExtra(CollaborationUtils.EXTRA_ITEM, folder);
        collabIntent.putExtra(CollaborationUtils.EXTRA_USER_ID, session.getUser().getId());
        return collabIntent;
    }
}
