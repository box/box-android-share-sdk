package com.box.androidsdk.share.activities;

import android.app.Activity;
import android.app.Application;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Switch;
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
import com.box.androidsdk.content.requests.BoxRequestUpdateSharedItem;
import com.box.androidsdk.content.requests.BoxRequestsBookmark;
import com.box.androidsdk.content.requests.BoxRequestsFile;
import com.box.androidsdk.content.requests.BoxRequestsFolder;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.fragments.AccessRadialDialogFragment;
import com.box.androidsdk.share.fragments.DatePickerFragment;
import com.box.androidsdk.share.fragments.PasswordDialogFragment;
import com.box.androidsdk.share.fragments.PositiveNegativeDialogFragment;

import org.apache.http.HttpStatus;

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
public class BoxSharedLinkAccessActivity extends BoxThreadPoolExecutorActivity implements DatePickerDialog.OnDateSetListener, DialogInterface.OnDismissListener, PasswordDialogFragment.OnPositiveOrNegativeButtonClickedListener  {

    /**
     * The required fields that must be returned from the API when making a share link request
     */
    public static String[] REQUIRED_FIELDS = new String[]{BoxItem.FIELD_SHARED_LINK, BoxItem.FIELD_NAME, BoxItem.FIELD_ALLOWED_SHARED_LINK_ACCESS_LEVELS};

    private BoxApiFile mFileApi;
    private BoxApiFolder mFolderApi;
    private BoxApiBookmark mBookmarkApi;

    private Button mAccessButton;
    private Button mPasswordButton;
    private Button mExpiresButton;

    private Switch mAllowDownloadsBtn;
    private Switch mRequirePasswordBtn;
    private Switch mExpireLinkBtn;

    private View mPasswordHeader;

    private static ThreadPoolExecutor mApiExecutor;
    private static final String DATE_FRAGMENT_TAG = "datePicker";
    private static final String PASSWORD_FRAGMENT_TAG = "passwordFrag";
    private static final String ACCESS_RADIAL_FRAGMENT_TAG = "accessFrag";
    private static final int DEFAULT_TIMEOUT = 30 * 1000; // using 30 seconds as the default timeout.
    private static final ConcurrentLinkedQueue<BoxResponse> SHARED_LINK_RESPONSE_QUEUE = new ConcurrentLinkedQueue<BoxResponse>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_link_access);
        initToolbar();
        mFileApi = new BoxApiFile(mSession);
        mFolderApi = new BoxApiFolder(mSession);
        mBookmarkApi = new BoxApiBookmark(mSession);

        mAccessButton = (Button)this.findViewById(R.id.shared_link_access_btn);
        mAccessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAccessChooserDialog();
            }
        });
        mPasswordButton = (Button)this.findViewById(R.id.shared_link_password_btn);
        mPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordChooserDialog();
            }
        });
        mExpiresButton = (Button)this.findViewById(R.id.shared_link_expires_on_btn);
        mExpiresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(getMainItem().getSharedLink().getUnsharedDate());
            }
        });

        mAllowDownloadsBtn = (Switch)this.findViewById(R.id.shared_link_allow_download_btn);
        mAllowDownloadsBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isTaskInProgress() || getMainItem() instanceof BoxBookmark || getMainItem().getSharedLink().getPermissions().getCanDownload() == isChecked ){
                    // if there is no change or we are busy with another task then do nothing.
                    return;
                }
                changeDownloadPermission(isChecked);
            }
        });
        mRequirePasswordBtn = (Switch)this.findViewById(R.id.share_link_require_password_btn);
        mRequirePasswordBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isTaskInProgress() ||  getMainItem().getSharedLink().getIsPasswordEnabled() == isChecked ){
                    // if there is no change or we are busy with another task then do nothing.
                    return;
                }
                if (isChecked) {
                    showPasswordChooserDialog();
                } else {
                    executeRequest((BoxRequestItem)getCreatedSharedLinkRequest().setPassword(null));
                }
            }
        });
        mExpireLinkBtn = (Switch)this.findViewById(R.id.shared_link_expire_link_btn);
        mExpireLinkBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isTaskInProgress() || (getMainItem().getSharedLink().getUnsharedDate() != null) == isChecked ){
                    // if there is no change or we are busy with another task then do nothing.
                    return;
                }
                if (isChecked) {
                    showDatePicker(new Date());
                } else {
                    try {
                        executeRequest((BoxRequestItem)getCreatedSharedLinkRequest().setRemoveUnsharedAtDate());
                    } catch (ParseException e){

                    }
                }
            }
        });

        mPasswordHeader = findViewById(R.id.box_sharesdk_password_header);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STARTING_TASK);
        filter.addAction(ACTION_ENDING_TASK);

        if (!isTaskInProgress() && !checkIfHasRequiredFields(getMainItem())){
            // we need to refresh since the item given to us is not complete.
            refreshShareItemInfo();
        } else {
            setupUi();
        }
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
        if (response.isSuccess()) {
            if (response.getRequest() instanceof BoxRequestItem){
                BoxItem item = (BoxItem)response.getResult();
                updateUi(item);
            }
        } else {
            if (response.getException() instanceof BoxException){
                if (((BoxException)response.getException()).getResponseCode() == HttpStatus.SC_NOT_MODIFIED){
                    return;
                }
            }
            // reset ui to previous object.
            if (response.getRequest() instanceof BoxRequestItem && getMainItem().getId().equals(((BoxRequestItem) response.getRequest()).getId())) {
                if (response.getRequest() instanceof BoxRequestUpdateSharedItem) {
                    Toast.makeText(BoxSharedLinkAccessActivity.this.getApplicationContext(), R.string.box_sharesdk_unable_to_modify_toast, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(BoxSharedLinkAccessActivity.this.getApplicationContext(), R.string.box_sharesdk_problem_accessing_this_shared_link, Toast.LENGTH_LONG).show();
                }
            }
            setupUi();
        }
    }

    @Override
    public void onPositiveButtonClicked(PositiveNegativeDialogFragment fragment) {
        if (fragment instanceof PasswordDialogFragment){
            try {
                changePassword(((PasswordDialogFragment) fragment).getPassword());
            } catch (Exception e){
                Toast.makeText(this, "invalid password", Toast.LENGTH_LONG).show();
            }
        }
        else if (fragment instanceof AccessRadialDialogFragment){
            changeAccess(((AccessRadialDialogFragment)fragment).getAccess());
        }
    }

    @Override
    public void onNegativeButtonClicked(PositiveNegativeDialogFragment fragment) {
        // reset ui since user didn't choose anything.
        setupUi();
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
            refreshShareItemInfo();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // this is currently only called by the DatePickerFragment.
        setupUi();
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

    /**
     * Handles when a date is selected on the DatePickerFragment
     *
     * @param view the DatePicker view
     * @param year the year
     * @param month the month
     * @param day the day
     */
    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        GregorianCalendar calendar = new GregorianCalendar(year, month, day);
        try {
            executeRequest((BoxRequestItem)getCreatedSharedLinkRequest().setUnsharedAt(calendar.getTime()));
        } catch (Exception e){
            Toast.makeText(BoxSharedLinkAccessActivity.this, "invalid time selected", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Updates the UI with the provided BoxItem
     *
     * @param sharedItem BoxItem to update the UI with
     */
    private void updateUi(final BoxItem sharedItem){
        if (!checkIfHasRequiredFields(sharedItem)){
            refreshShareItemInfo();
            return;
        }
        if (getMainItem().getId().equals(sharedItem.getId())) {
            setMainItem(sharedItem);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupUi();
                }
            });
        }
    }

    /**
     * Displays the DatePickerFragment
     *
     * @param date the default date that should be selected
     */
    private void showDatePicker(Date date){
        if (getFragmentManager().findFragmentByTag(DATE_FRAGMENT_TAG) != null){
            return;
        }
        DatePickerFragment fragment = DatePickerFragment.createFragment(date);
        fragment.show(getFragmentManager(), DATE_FRAGMENT_TAG);
    }

    /**
     * Displays the dialog for the user to set a password for the shared link
     */
    private void showPasswordChooserDialog(){
        if (getFragmentManager().findFragmentByTag(PASSWORD_FRAGMENT_TAG) != null){
            return;
        }
        PasswordDialogFragment fragment = PasswordDialogFragment.createFragment(R.string.box_sharesdk_password, R.string.box_sharesdk_set_password, R.string.box_sharesdk_ok, R.string.box_sharesdk_cancel );
        fragment.show(getFragmentManager(), PASSWORD_FRAGMENT_TAG);
    }

    /**
     * Displays the access dialog for the user to select the appropriate access
     */
    private void showAccessChooserDialog(){
        if (getFragmentManager().findFragmentByTag(ACCESS_RADIAL_FRAGMENT_TAG) != null){
            return;
        }
        AccessRadialDialogFragment fragment = AccessRadialDialogFragment.createFragment(getMainItem());
        fragment.show(getFragmentManager(), ACCESS_RADIAL_FRAGMENT_TAG);
    }

    /**
     * Initializes the UI
     */
    private void setupUi(){
        BoxSharedLink link =  getMainItem().getSharedLink();
        if (link != null) {
            BoxSharedLink.Access access = link.getEffectiveAccess();
            if (access != null) {
                String accessDescription = "";
                switch (access) {
                    case OPEN:
                        accessDescription = getResources().getString(R.string.box_sharesdk_access_public);
                        break;
                    case COLLABORATORS:
                        accessDescription = getResources().getString(R.string.box_sharesdk_access_collaborator);
                        break;
                    case COMPANY:
                        accessDescription = getResources().getString(R.string.box_sharesdk_access_company);
                }
                mAccessButton.setText(createTitledSpannable(getResources().getString(R.string.box_sharesdk_link_access), accessDescription));
            }
            if (getMainItem() instanceof BoxBookmark){
                hideView(mAllowDownloadsBtn);
            } else{
                mAllowDownloadsBtn.setChecked(link.getPermissions() != null && link.getPermissions().getCanDownload());
            }

            if (access != null && access == BoxSharedLink.Access.COLLABORATORS){
                hideView(mPasswordHeader);
                hideView(mRequirePasswordBtn);
                hideView(mPasswordButton);
            } else {
                showView(mPasswordHeader);
                showView(mRequirePasswordBtn);
                mRequirePasswordBtn.setChecked(link.getIsPasswordEnabled());
                if (link.getIsPasswordEnabled()) {
                    mPasswordButton.setText(createTitledSpannable(getResources().getString(R.string.box_sharesdk_password), "*****"));
                    showView(mPasswordButton);
                } else {
                    hideView(mPasswordButton);
                }
            }

            mExpireLinkBtn.setChecked(link.getUnsharedDate() != null);
            if (link.getUnsharedDate() != null) {
                mExpiresButton.setText(createTitledSpannable(getResources().getString(R.string.box_sharesdk_expire_on), SimpleDateFormat.getDateInstance().format(link.getUnsharedDate())));
                showView(mExpiresButton);
            } else {
                hideView(mExpiresButton);
            }
        } else {
            Toast.makeText(this,getText(R.string.box_sharesdk_problem_accessing_this_shared_link), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Modifies the download permssion of the share item
     *
     * @param canDownload whether or not the item can be downloaded
     */
    private void changeDownloadPermission(boolean canDownload){
        if (getMainItem() instanceof BoxFile) {
            executeRequest(mFileApi.getCreateSharedLinkRequest(getMainItem().getId()).setCanDownload(canDownload));
        }
        else if (getMainItem() instanceof BoxFolder) {
            executeRequest(mFolderApi.getCreateSharedLinkRequest(getMainItem().getId()).setCanDownload(canDownload));
        }
        else if (getMainItem() instanceof BoxBookmark) {
            Toast.makeText(this, "Bookmarks do not have a permission that can be changed.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Modifies the share link access
     *
     * @param access the share link access level
     */
    private void changeAccess(final BoxSharedLink.Access access){
        if (access == null){
            // Should not be possible to get here.
            Toast.makeText(this, "No access chosen", Toast.LENGTH_LONG).show();
            return;
        }
        executeRequest((BoxRequestItem)getCreatedSharedLinkRequest().setAccess(access));
    }

    /**
     * Uses the ThreadPoolExecutor provided by getApiExecutor to execute the {@link com.box.androidsdk.content.requests.BoxRequest}
     *
     * @param request the request to be executed
     */
    private void executeRequest(final BoxRequestItem request){
        getApiExecutor(getApplication()).execute(request.setFields(REQUIRED_FIELDS).setTimeOut(DEFAULT_TIMEOUT).toTask());
    }

    /**
     * Gets the request to create a shared link with
     *
     * @return the shared link update request
     */
    private BoxRequestUpdateSharedItem getCreatedSharedLinkRequest(){
        if (getMainItem() instanceof BoxFile) {
            return mFileApi.getCreateSharedLinkRequest(getMainItem().getId());
        }
        else if (getMainItem() instanceof BoxFolder) {
            return mFolderApi.getCreateSharedLinkRequest(getMainItem().getId());

        }
        else if (getMainItem() instanceof BoxBookmark) {
            return mBookmarkApi.getCreateSharedLinkRequest(getMainItem().getId());
        }
        // should never hit this scenario.
        return null;
    }

    /**
     * Sets the password to the provided string
     *
     * @param password the password to set on the shared item
     * @throws ParseException
     */
    private void changePassword(final String password) throws ParseException{
        executeRequest((BoxRequestItem)getCreatedSharedLinkRequest().setPassword(password));
    }

    /**
     * Check if the required fields are available on the BoxItem
     *
     * @param shareItem the BoxItem to verify
     * @return whether or not all the required fields are present
     */
    private boolean checkIfHasRequiredFields(BoxItem shareItem){
        return shareItem.getSharedLink() != null && shareItem.getAllowedSharedLinkAccessLevels() != null;
    }

    /**
     * Refreshes the information of the shared link
     */
    private void refreshShareItemInfo() {
        boolean hasRequredFields = checkIfHasRequiredFields(getMainItem());

        if (getMainItem() instanceof BoxFile){
            BoxRequestsFile.GetFileInfo request = mFileApi.getInfoRequest(getMainItem().getId());
            if (hasRequredFields){
                request.setIfNoneMatchEtag(getMainItem().getEtag());
            }
            executeRequest(request);
        } else if (getMainItem() instanceof BoxFolder){
            BoxRequestsFolder.GetFolderInfo request = mFolderApi.getInfoRequest(getMainItem().getId());
            if (hasRequredFields){
                request.setIfNoneMatchEtag(getMainItem().getEtag());
            }
            executeRequest(request);
        } else if (getMainItem() instanceof BoxBookmark){
            BoxRequestsBookmark.GetBookmarkInfo request = mBookmarkApi.getInfoRequest(getMainItem().getId());
            if (hasRequredFields){
                request.setIfNoneMatchEtag(getMainItem().getEtag());
            }
            executeRequest(request);
        }
    }

}
