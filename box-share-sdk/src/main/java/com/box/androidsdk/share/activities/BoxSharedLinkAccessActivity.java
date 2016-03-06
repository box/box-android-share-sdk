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
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.api.BoxShareController;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.fragments.SharedLinkAccessFragment;


/**
 * Activity used to modify the share link access of an item from Box. The intent to launch this activity can be retrieved via the static getLaunchIntent method
 */
public class BoxSharedLinkAccessActivity extends BoxActivity {

    SharedLinkAccessFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_link_access);
        initToolbar();

        ShareController controller = new BoxShareController(new BoxApiFile(mSession),
                new BoxApiFolder(mSession), new BoxApiBookmark(mSession), new BoxApiCollaboration(mSession));
        mFragment = (SharedLinkAccessFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (mFragment == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_NONE);
            mFragment = SharedLinkAccessFragment.newInstance(mShareItem);
            ft.add(R.id.fragmentContainer, mFragment);
            ft.commit();
        }
        mFragment.SetController(controller);
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra(ResultInterpreter.EXTRA_BOX_ITEM, mShareItem);
        setResult(Activity.RESULT_OK, data);
        super.finish();
    }


    /**
     * Gets a fully formed intent that can be used to start the activity with
     *
     * @param context context to launch the intent in
     * @param item the item to modify share link access for
     * @param session the session to modify share link access with
     * @return the intent to launch the activity
     */
    public static Intent getLaunchIntent(Context context, BoxItem item, BoxSession session) {
        if (session == null || session.getUser() == null)
            throw new IllegalArgumentException("Invalid user associated with Box session.");

        Intent intent = new Intent(context, BoxSharedLinkAccessActivity.class);

        intent.putExtra(EXTRA_ITEM, item);
        intent.putExtra(EXTRA_USER_ID, session.getUser().getId());
        return intent;
    }

    /**
     * Result interpreter that allows the updated BoxItem information to be retrieved from another activity
     *
     * @param data the intent data to set
     * @return the ResultInterpreter
     */
    public static ResultInterpreter createResultInterpreter(final Intent data){
        return new ResultInterpreter(data);
    }

    /**
     * Data object that can serialize data across activities
     */
    public static class ResultInterpreter {
        protected final Intent mIntent;

        static final String EXTRA_BOX_ITEM = "extraBoxItem";

        /**
         * Construct an object to easily access objects in an intent created from this activity.
         * @param intent an intent created by this activity that follows the rules if the interpreter.
         */
        public ResultInterpreter(final Intent intent){
            mIntent = intent;
        }

        public BoxItem getBoxItem(){
            return (BoxItem)mIntent.getSerializableExtra(EXTRA_BOX_ITEM);
        }
    }
}
