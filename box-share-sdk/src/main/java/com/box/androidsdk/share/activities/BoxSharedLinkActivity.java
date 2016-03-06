package com.box.androidsdk.share.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.box.androidsdk.content.BoxApiBookmark;
import com.box.androidsdk.content.BoxApiCollaboration;
import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.api.BoxShareController;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.fragments.SharedLinkFragment;

/**
 * Activity used to share/unshare an item from Box. The intent to launch this activity can be retrieved via the static getLaunchIntent method
 */
public class BoxSharedLinkActivity extends BoxActivity implements View.OnClickListener{

    private SharedLinkFragment mFragment;
    private static final int REQUEST_SHARED_LINK_ACCESS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_link);
        initToolbar();

        ShareController controller = new BoxShareController(new BoxApiFile(mSession),
                new BoxApiFolder(mSession), new BoxApiBookmark(mSession), new BoxApiCollaboration(mSession));
        mFragment = (SharedLinkFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (mFragment == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_NONE);
            mFragment = SharedLinkFragment.newInstance(mShareItem);
            ft.add(R.id.fragmentContainer, mFragment);
            ft.commit();
        }
        mFragment.SetController(controller);
        mFragment.setOnEditLinkAccessButtonClickListener(this);
    }

    @Override
    public void onClick(View v) {
        startActivityForResult(BoxSharedLinkAccessActivity.getLaunchIntent(BoxSharedLinkActivity.this, mShareItem, mSession), REQUEST_SHARED_LINK_ACCESS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SHARED_LINK_ACCESS){
            mFragment.refreshShareItemInfo();
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

        Intent intent = new Intent(context, BoxSharedLinkActivity.class);
        intent.putExtra(EXTRA_ITEM, item);
        intent.putExtra(EXTRA_USER_ID, session.getUser().getId());
        return intent;
    }
}
