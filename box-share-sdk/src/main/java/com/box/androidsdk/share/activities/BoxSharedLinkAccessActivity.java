package com.box.androidsdk.share.activities;

import android.app.Activity;
import android.app.Application;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.Toast;

import com.box.androidsdk.content.BoxApiBookmark;
import com.box.androidsdk.content.BoxApiCollaboration;
import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.models.BoxBookmark;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.content.requests.BoxRequestItem;
import com.box.androidsdk.content.requests.BoxRequestUpdateSharedItem;
import com.box.androidsdk.content.requests.BoxRequestsBookmark;
import com.box.androidsdk.content.requests.BoxRequestsFile;
import com.box.androidsdk.content.requests.BoxRequestsFolder;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.api.BoxShareController;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.fragments.AccessRadialDialogFragment;
import com.box.androidsdk.share.fragments.DatePickerFragment;
import com.box.androidsdk.share.fragments.PasswordDialogFragment;
import com.box.androidsdk.share.fragments.PositiveNegativeDialogFragment;
import com.box.androidsdk.share.fragments.SharedLinkAccessFragment;
import com.box.androidsdk.share.fragments.SharedLinkFragment;

import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * Activity used to modify the share link access of an item from Box. The intent to launch this activity can be retrieved via the static getLaunchIntent method
 */
public class BoxSharedLinkAccessActivity extends BoxThreadPoolExecutorActivity {

    private static ThreadPoolExecutor mApiExecutor;
    private static final int DEFAULT_TIMEOUT = 30 * 1000; // using 30 seconds as the default timeout.
    private static final ConcurrentLinkedQueue<BoxResponse> SHARED_LINK_RESPONSE_QUEUE = new ConcurrentLinkedQueue<BoxResponse>();

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
    public Queue<BoxResponse> getResponseQueue() {
        return SHARED_LINK_RESPONSE_QUEUE;
    }

    @Override
    public ThreadPoolExecutor getApiExecutor(Application application) {
        if (mApiExecutor == null){
            mApiExecutor = BoxThreadPoolExecutorActivity.createTaskMessagingExecutor(application, getResponseQueue());
        }
        return mApiExecutor;
    }

    @Override
    public void handleBoxResponse(final BoxResponse response){
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra(ResultInterpreter.EXTRA_BOX_ITEM, getMainItem());
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
