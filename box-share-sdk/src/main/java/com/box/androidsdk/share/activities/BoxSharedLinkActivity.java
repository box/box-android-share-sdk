package com.box.androidsdk.share.activities;

import android.app.Activity;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.box.androidsdk.content.BoxApiBookmark;
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
import com.box.androidsdk.content.requests.BoxRequest;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.requests.BoxRequestUpdateSharedItem;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.fragments.PositiveNegativeDialogFragment;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Activity used to share/unshare an item from Box. The intent to launch this activity can be retrieved via the static getLaunchIntent method
 */
public class BoxSharedLinkActivity extends BoxThreadPoolExecutorActivity implements PositiveNegativeDialogFragment.OnPositiveOrNegativeButtonClickedListener{

    private BoxApiFile mFileApi;
    private BoxApiFolder mFolderApi;
    private BoxApiBookmark mBookmarkApi;

    private static ThreadPoolExecutor mApiExecutor;
    private static final String UNSHARE_WARNING_TAG = "com.box.sharesdk.unshare_warning";
    private static final ConcurrentLinkedQueue<BoxResponse> SHARED_LINK_RESPONSE_QUEUE = new ConcurrentLinkedQueue<BoxResponse>();

    Switch mSharedLinkUrlSwitch;
    TextView mSharedLinkUrlText;
    TextView mSharedLinkAccess;
    TextView mSharedLinkDownloadPermission;
    TextView mSharedLinkPasswordProtected;
    TextView mSharedLinkExpiration;
    TextView mEnableShareLinkInstructions;

    View mSharedLinkAccessLayout;
    View mSharedLinkDownloadPermissionLayout;

    View mSharedLinkPasswordProtectedLayout;
    View mSharedLinkExpirationLayout;
    View mSharedLinkOptionsLayout;

    Button mEditLinkAccessButton;

    private static final int REQUEST_SHARED_LINK_ACCESS = 100;
    private boolean hasSetupUi = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_link);
        initToolbar();
        mFileApi = new BoxApiFile(mSession);
        mFolderApi = new BoxApiFolder(mSession);
        mBookmarkApi = new BoxApiBookmark(mSession);

        mSharedLinkUrlText = (TextView)this.findViewById(R.id.shared_link_url_text);
        mSharedLinkUrlSwitch = (Switch)this.findViewById(R.id.shared_link_url_switch);
        mSharedLinkUrlSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isTaskInProgress()){
                    return;
                }
                if (isChecked && getMainItem().getSharedLink() == null){
                    // create the shared link with default values.
                    createDefaultShareItem();
                }
                else if (!isChecked && getMainItem().getSharedLink() != null){
                    displayUnshareWarning();
                }

            }
        });

        mEnableShareLinkInstructions = (TextView) this.findViewById(R.id.enable_share_link_instructions);
        mSharedLinkPasswordProtectedLayout = this.findViewById(R.id.shared_link_password_protected_layout);
        mSharedLinkExpirationLayout = this.findViewById(R.id.shared_link_expiration_layout);
        mSharedLinkOptionsLayout = this.findViewById(R.id.shared_link_options_layout);
        mSharedLinkAccessLayout = this.findViewById(R.id.shared_link_access_layout);
        mSharedLinkDownloadPermissionLayout = this.findViewById(R.id.shared_link_download_layout);

        mSharedLinkAccess = (TextView)this.findViewById(R.id.shared_link_access_text);
        mSharedLinkDownloadPermission = (TextView)this.findViewById(R.id.shared_link_download_text);
        mSharedLinkPasswordProtected = (TextView)this.findViewById(R.id.shared_link_password_protected_text);
        mSharedLinkExpiration = (TextView)this.findViewById(R.id.shared_link_expiration_text);
        mEditLinkAccessButton = (Button)this.findViewById(R.id.share_edit_link_access);
        mEditLinkAccessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(BoxSharedLinkAccessActivity.getLaunchIntent(BoxSharedLinkActivity.this, getMainItem(), mSession) , REQUEST_SHARED_LINK_ACCESS);
            }
        });

        // if we do not have a shared link try refreshing
        if (getMainItem().getSharedLink() == null){
            refreshShareItemInfo(false);
        } else {
            setupUi();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SHARED_LINK_ACCESS){
            updateUi(BoxSharedLinkAccessActivity.createResultInterpreter(data).getBoxItem());
        }
        super.onActivityResult(requestCode, resultCode, data);
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
    public void handleBoxResponse(BoxResponse response){
        if (response.isSuccess()) {
            if (response.getRequest() instanceof BoxRequestItem){
                BoxItem item = (BoxItem)response.getResult();
                updateUi(item);
            }
        } else {
            if (response.getException() instanceof BoxException){
                if (((BoxException)response.getException()).getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                    if (!hasSetupUi){
                        // if we have never shown ui before do it now.
                        updateUi(getMainItem());
                    }
                    return;
                }
            }
            // reset ui to previous object.
            if (response.getRequest() instanceof BoxRequestItem && getMainItem().getId().equals(((BoxRequestItem) response.getRequest()).getId())) {
                if (response.getRequest() instanceof BoxRequestUpdateSharedItem) {
                    Toast.makeText(BoxSharedLinkActivity.this, R.string.box_sharesdk_unable_to_modify_toast, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(BoxSharedLinkActivity.this, R.string.box_sharesdk_problem_accessing_this_shared_link, Toast.LENGTH_LONG).show();
                }
            }
            setupUi();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sharedlink, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.box_sharesdk_refresh){
            refreshShareItemInfo(true);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPositiveButtonClicked(PositiveNegativeDialogFragment fragment) {
        // because we are only showing one now no need to check fragment.
        disableShareItem();
    }

    @Override
    public void onNegativeButtonClicked(PositiveNegativeDialogFragment fragment) {
        // because we are only showing one now no need to check fragment.
        setupUi();
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra(BoxSharedLinkAccessActivity.ResultInterpreter.EXTRA_BOX_ITEM, getMainItem());
        setResult(Activity.RESULT_OK, data);
        super.finish();
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

    /**
     * Uses the ThreadPoolExecutor provided by getApiExecutor to execute the {@link com.box.androidsdk.content.requests.BoxRequest}
     *
     * @param request the request to be executed
     */
    protected void executeRequest(BoxRequest request){
        getApiExecutor(getApplication()).execute(request.setTimeOut(DEFAULT_TIMEOUT).toTask());
    }


    /**
     * Check to see if the threadpool executor is currently in the middle of a task and if so show a spinner dialog again.
     */
    protected void resumeSpinnerIfNecessary(){
        if (mApiExecutor == null){
            return;
        } else  if (mApiExecutor.getActiveCount() > 0) {
            showSpinner(R.string.box_sharesdk_title_link_access, R.string.boxsdk_Please_wait);
        }
    }

    /**
     * Displays the modal to confirm if the user wants to unshare the item
     */
    private void displayUnshareWarning(){
        if (getFragmentManager().findFragmentByTag(UNSHARE_WARNING_TAG) != null){
            return;
        }
        PositiveNegativeDialogFragment.createFragment(R.string.box_sharesdk_disable_title, R.string.box_sharesdk_disable_message,R.string.box_sharesdk_disable_share_link, R.string.box_sharesdk_cancel).show(getFragmentManager(), UNSHARE_WARNING_TAG);
    }

    /**
     * Updates the share link UI with the provided {@link com.box.androidsdk.content.models.BoxItem}
     *
     * @param sharedItem the BoxItem to update the share link UI with
     */
    private void updateUi(final BoxItem sharedItem){
        if (getMainItem().getId().equals(sharedItem.getId())) {
            setMainItem(sharedItem);
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    setupUi();
                }
            });
        }
    }

    /**
     * Initializes the UI controls
     */
    private void setupUi(){
        hasSetupUi = true;
        BoxSharedLink link =  getMainItem().getSharedLink();
        if (link != null){
            showView(mSharedLinkOptionsLayout);
            hideView(mEnableShareLinkInstructions);
            mSharedLinkUrlSwitch.setMaxLines(2);
            mSharedLinkUrlText.setText(link.getURL());
            mSharedLinkUrlSwitch.setChecked(true);
            BoxSharedLink.Access access = link.getEffectiveAccess();
            if (access != null){
                switch(access){
                    case OPEN:
                        mSharedLinkAccess.setText(R.string.box_sharesdk_accessible_public);
                        break;
                    case COLLABORATORS:
                        mSharedLinkAccess.setText(R.string.box_sharesdk_accessible_collaborator);
                        break;
                    case COMPANY:
                        mSharedLinkAccess.setText(R.string.box_sharesdk_accessible_company);
                        break;
                }
                showView(mSharedLinkAccessLayout);
            }
            if (link.getPermissions() != null && link.getPermissions().getCanDownload()){
                mSharedLinkDownloadPermission.setText(R.string.box_sharesdk_downloads_allowed);
            } else {
                mSharedLinkDownloadPermission.setText(R.string.box_sharesdk_downloads_disabled);
            }
            showView(mSharedLinkDownloadPermissionLayout);
            if (link.getIsPasswordEnabled()){
                mSharedLinkPasswordProtected.setText(R.string.box_sharesdk_password_protected);
                showView(mSharedLinkPasswordProtectedLayout);
            } else {
                hideView(mSharedLinkPasswordProtectedLayout);
            }
            Date unsharedDate = link.getUnsharedDate();
            if (unsharedDate == null){
                hideView(mSharedLinkExpirationLayout);
            } else {
                mSharedLinkExpiration.setText(String.format(getResources().getString(R.string.box_sharesdk_link_expires_on_x_date), SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(unsharedDate)));
                showView(mSharedLinkExpirationLayout);
            }
        }
        else {
            showView(mEnableShareLinkInstructions);
            mSharedLinkUrlSwitch.setChecked(false);
            hideView(mSharedLinkOptionsLayout);
        }

    }

    /**
     * Handles the click event for the send link button and launches the email intent
     *
     * @param v the view that was clicked
     */
    public void onSendLinkButtonClicked(View v) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, String.format(getString(R.string.box_sharesdk_I_have_shared_x_with_you), getMainItem().getName()));
        emailIntent.putExtra(Intent.EXTRA_TEXT, getMainItem().getSharedLink().getURL());
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(Intent.createChooser(emailIntent, getString(R.string.box_sharesdk_send_with)));

    }

    /**
     * Handles the click event for the copy link button and copies the shared link to the clipboard
     *
     * @param v the view that was clicked
     */
    public void onCopyLinkButtonClicked(View v) {
        if ( getMainItem().getSharedLink() != null) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Activity.CLIPBOARD_SERVICE);
            BoxSharedLink sharedLink =  getMainItem().getSharedLink();
            ClipData clipData = ClipData.newPlainText("", sharedLink.getURL());
            clipboard.setPrimaryClip(clipData);
            Toast.makeText(this, R.string.box_sharesdk_link_copied_to_clipboard ,Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Executes the create shared link request for the appropriate item type of getMainItem
     */
    private void createDefaultShareItem(){
        if (getMainItem() instanceof BoxFile){
            executeRequest(mFileApi.getCreateSharedLinkRequest(getMainItem().getId()).setFields(BoxSharedLinkAccessActivity.REQUIRED_FIELDS));
        } else if (getMainItem() instanceof BoxFolder){
            executeRequest(mFolderApi.getCreateSharedLinkRequest(getMainItem().getId()).setFields(BoxSharedLinkAccessActivity.REQUIRED_FIELDS));
        } else if (getMainItem() instanceof BoxBookmark){
            executeRequest(mBookmarkApi.getCreateSharedLinkRequest(getMainItem().getId()).setFields(BoxSharedLinkAccessActivity.REQUIRED_FIELDS));
        }
    }

    /**
     * Executes the disable share link request for the appropriate item type of getMainItem
     */
    private void disableShareItem(){
        if (getMainItem() instanceof BoxFile){
            executeRequest(mFileApi.getDisableSharedLinkRequest(getMainItem().getId()));
        } else if (getMainItem() instanceof BoxFolder){
            executeRequest(mFolderApi.getDisableSharedLinkRequest(getMainItem().getId()));
        } else if (getMainItem() instanceof BoxBookmark){
            executeRequest(mBookmarkApi.getDisableSharedLinkRequest(getMainItem().getId()));
        }
    }

    /**
     * Executes the get info request for the appropriate item type of getMainItem
     */
    private void refreshShareItemInfo(boolean checkEtag){
        String etag = null;
        if (checkEtag){
            etag = getMainItem().getEtag();
        }
        if (getMainItem() instanceof BoxFile){
            executeRequest(mFileApi.getInfoRequest(getMainItem().getId()).setFields(BoxSharedLinkAccessActivity.REQUIRED_FIELDS).setIfNoneMatchEtag(etag));
        } else if (getMainItem() instanceof BoxFolder){
            executeRequest(mFolderApi.getInfoRequest(getMainItem().getId()).setFields(BoxSharedLinkAccessActivity.REQUIRED_FIELDS).setIfNoneMatchEtag(etag));
        } else if (getMainItem() instanceof BoxBookmark){
            executeRequest(mBookmarkApi.getInfoRequest(getMainItem().getId()).setFields(BoxSharedLinkAccessActivity.REQUIRED_FIELDS).setIfNoneMatchEtag(etag));
        }
    }

    /**
     * Result interpreter that allows the updated BoxItem information to be retrieved from another activity
     *
     * @param data the intent data to set
     * @return the ResultInterpreter
     */
    public static BoxSharedLinkAccessActivity.ResultInterpreter createResultInterpreter(final Intent data){
        return new BoxSharedLinkAccessActivity.ResultInterpreter(data);
    }


}
