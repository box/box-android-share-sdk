package com.box.androidsdk.share.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.content.requests.BoxRequestItem;
import com.box.androidsdk.content.requests.BoxRequestUpdateSharedItem;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.share.R;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by varungupta on 3/5/2016.
 */
public class SharedLinkFragment extends BoxFragment implements PositiveNegativeDialogFragment.OnPositiveOrNegativeButtonClickedListener{

    private static final String UNSHARE_WARNING_TAG = "com.box.sharesdk.unshare_warning";
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

    Button mCopyLinkButton;
    Button mSendLinkButton;
    Button mEditLinkAccessButton;
    private boolean hasSetupUi = false;

    private View.OnClickListener mOnEditLinkAccessButtonClickListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shared_link, container, false);

        mSharedLinkUrlText = (TextView)view.findViewById(R.id.shared_link_url_text);
        mSharedLinkUrlSwitch = (Switch)view.findViewById(R.id.shared_link_url_switch);
        mSharedLinkUrlSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && mShareItem.getSharedLink() == null){
                    // create the shared link with default values.
                    createDefaultShareItem();
                }
                else if (!isChecked && mShareItem.getSharedLink() != null){
                    displayUnshareWarning();
                }

            }
        });

        mEnableShareLinkInstructions = (TextView) view.findViewById(R.id.enable_share_link_instructions);
        mSharedLinkPasswordProtectedLayout = view.findViewById(R.id.shared_link_password_protected_layout);
        mSharedLinkExpirationLayout = view.findViewById(R.id.shared_link_expiration_layout);
        mSharedLinkOptionsLayout = view.findViewById(R.id.shared_link_options_layout);
        mSharedLinkAccessLayout = view.findViewById(R.id.shared_link_access_layout);
        mSharedLinkDownloadPermissionLayout = view.findViewById(R.id.shared_link_download_layout);

        mSharedLinkAccess = (TextView)view.findViewById(R.id.shared_link_access_text);
        mSharedLinkDownloadPermission = (TextView)view.findViewById(R.id.shared_link_download_text);
        mSharedLinkPasswordProtected = (TextView)view.findViewById(R.id.shared_link_password_protected_text);
        mSharedLinkExpiration = (TextView)view.findViewById(R.id.shared_link_expiration_text);

        mCopyLinkButton = (Button) view.findViewById(R.id.shared_link_copy_link_btn);
        mCopyLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( mShareItem.getSharedLink() != null) {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
                    BoxSharedLink sharedLink = mShareItem.getSharedLink();
                    ClipData clipData = ClipData.newPlainText("", sharedLink.getURL());
                    clipboard.setPrimaryClip(clipData);
                    mController.showToast(getActivity(), R.string.box_sharesdk_link_copied_to_clipboard);
                }
            }
        });

        mSendLinkButton = (Button) view.findViewById(R.id.shared_link_send_link_btn);
        mSendLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, String.format(getString(R.string.box_sharesdk_I_have_shared_x_with_you), mShareItem.getName()));
                emailIntent.putExtra(Intent.EXTRA_TEXT, mShareItem.getSharedLink().getURL());
                emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(Intent.createChooser(emailIntent, getString(R.string.box_sharesdk_send_with)));
            }
        });

        mEditLinkAccessButton = (Button)view.findViewById(R.id.share_edit_link_access);
        if (mOnEditLinkAccessButtonClickListener != null) {
            mEditLinkAccessButton.setOnClickListener(mOnEditLinkAccessButtonClickListener);
        }

        // if we do not have a shared link try refreshing
        if (mShareItem.getSharedLink() == null){
            refreshShareItemInfo();
        } else {
            setupUi();
        }

        return view;
    }

    public void setOnEditLinkAccessButtonClickListener(View.OnClickListener listener) {
        mOnEditLinkAccessButtonClickListener = listener;
        if (mEditLinkAccessButton != null) {
            mEditLinkAccessButton.setOnClickListener(mOnEditLinkAccessButtonClickListener);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_sharedlink, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.box_sharesdk_refresh){
            refreshShareItemInfo();
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

    /**
     * Displays the modal to confirm if the user wants to unshare the item
     */
    private void displayUnshareWarning(){
        if (getFragmentManager().findFragmentByTag(UNSHARE_WARNING_TAG) != null){
            return;
        }
        PositiveNegativeDialogFragment.createFragment(R.string.box_sharesdk_disable_title,
                R.string.box_sharesdk_disable_message, R.string.box_sharesdk_disable_share_link,
                R.string.box_sharesdk_cancel, this)
                .show(getActivity().getSupportFragmentManager(), UNSHARE_WARNING_TAG);
    }

    /**
     * Updates the share link UI with the current mShareItem
     */
    private void updateUi(){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupUi();
                }
            });
    }

    /**
     * Initializes the UI controls
     */
    private void setupUi(){
        hasSetupUi = true;
        BoxSharedLink link =  mShareItem.getSharedLink();
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
     * Executes the create shared link request for the appropriate item type of getMainItem
     */
    private void createDefaultShareItem(){
        showSpinner();
        mController.createDefaultSharedLink(mShareItem, mBoxItemListener);
    }

    /**
     * Executes the disable share link request for the appropriate item type of getMainItem
     */
    private void disableShareItem(){
        showSpinner();
        mController.disableShareLink(mShareItem, mBoxItemListener);
    }

    /**
     * Executes the get info request for the appropriate item type of getMainItem
     */
    public void refreshShareItemInfo(){
        showSpinner();
        mController.fetchItemInfo(mShareItem, mBoxItemListener);
    }



    private BoxFutureTask.OnCompletedListener<BoxItem> mBoxItemListener =
            new BoxFutureTask.OnCompletedListener<BoxItem>() {
                @Override
                public void onCompleted(final BoxResponse<BoxItem> response) {
                    dismissSpinner();
                    final Activity activity = getActivity();
                    if (activity == null) {
                        return;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.isSuccess()) {
                                if (response.getRequest() instanceof BoxRequestItem) {
                                    mShareItem = response.getResult();
                                    updateUi();
                                }
                            } else {
                                if (response.getException() instanceof BoxException) {
                                    if (((BoxException) response.getException()).getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                                        if (!hasSetupUi) {
                                            // if we have never shown ui before do it now.
                                            updateUi();
                                        }
                                        return;
                                    }
                                }
                                // reset ui to previous object.
                                if (response.getRequest() instanceof BoxRequestItem && mShareItem.getId().equals(((BoxRequestItem) response.getRequest()).getId())) {
                                    if (response.getRequest() instanceof BoxRequestUpdateSharedItem) {
                                        mController.showToast(getActivity(), R.string.box_sharesdk_unable_to_modify_toast);
                                    } else {
                                        mController.showToast(getActivity(), R.string.box_sharesdk_problem_accessing_this_shared_link);
                                    }
                                }
                                setupUi();
                            }
                        }
                    });
                }
            };

    public static SharedLinkFragment newInstance(BoxItem item) {
        Bundle args = BoxFragment.getBundle(item);
        SharedLinkFragment fragment = new SharedLinkFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
