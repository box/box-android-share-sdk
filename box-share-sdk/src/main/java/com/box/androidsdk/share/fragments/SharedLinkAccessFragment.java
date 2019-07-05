package com.box.androidsdk.share.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxBookmark;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.content.requests.BoxRequestItem;
import com.box.androidsdk.content.requests.BoxRequestUpdateSharedItem;
import com.box.androidsdk.content.requests.BoxRequestsFile;
import com.box.androidsdk.content.requests.BoxRequestsFolder;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.internal.models.BoxFeatures;

import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class SharedLinkAccessFragment extends BoxFragment
        implements DatePickerDialog.OnDateSetListener, PositiveNegativeDialogFragment.OnPositiveOrNegativeButtonClickedListener  {

    private static final String DATE_FRAGMENT_TAG = "datePicker";
    private static final String PASSWORD_FRAGMENT_TAG = "passwordFrag";
    private static final String ACCESS_RADIAL_FRAGMENT_TAG = "accessFrag";
    private View mAccessLayout;
    private TextView mAccessText;
    private Button mPasswordButton;
    private Button mExpiresButton;

    private Switch mAllowDownloadsBtn;
    private Switch mRequirePasswordBtn;
    private Switch mExpireLinkBtn;

    private View mPasswordHeader;

    private View mPasswordSection;
    private View mLinkExpirationSection;

    private boolean mPasswordProtectedLinksSupported = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController.getSupportedFeatures().addOnCompletedListener(new BoxFutureTask.OnCompletedListener<BoxFeatures>() {
            @Override
            public void onCompleted(BoxResponse<BoxFeatures> response) {
                if (response.isSuccess()) {
                    mPasswordProtectedLinksSupported = response.getResult().hasPasswordProtectForSharedLinks();

                } else {
                    mPasswordProtectedLinksSupported = true;
                    //Defaulting to true - if they aren't indeed supported, this will fail later when attempting to set password.
                }
                if (mPasswordProtectedLinksSupported && getView() != null && checkIfHasRequiredFields(mShareItem)) {
                    updateUi();
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shared_link_access, container, false);

        mAccessLayout = view.findViewById(R.id.shared_link_access_layout);
        mAccessLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAccessChooserDialog();
            }
        });
        mAccessText = (TextView) view.findViewById(R.id.shared_link_access_text);
        mAccessText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAccessChooserDialog();
            }
        });
        mPasswordButton = (Button)view.findViewById(R.id.shared_link_password_btn);
        mPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordChooserDialog();
            }
        });
        mExpiresButton = (Button)view.findViewById(R.id.shared_link_expires_on_btn);
        mPasswordSection = view.findViewById(R.id.password_section);
        mLinkExpirationSection = view.findViewById(R.id.link_expiration_section);
        mExpiresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(mShareItem.getSharedLink().getUnsharedDate());
            }
        });

        mAllowDownloadsBtn = (Switch)view.findViewById(R.id.shared_link_allow_download_btn);
        mAllowDownloadsBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mShareItem instanceof BoxBookmark || mShareItem.getSharedLink().getPermissions().getCanDownload() == isChecked ){
                    // if there is no change or we are busy with another task then do nothing.
                    return;
                }
                changeDownloadPermission(isChecked);
            }
        });
        mRequirePasswordBtn = (Switch)view.findViewById(R.id.share_link_require_password_btn);
        mRequirePasswordBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mShareItem.getSharedLink().getIsPasswordEnabled() == isChecked ){
                    // if there is no change or we are busy with another task then do nothing.
                    return;
                }
                if (isChecked) {
                    showPasswordChooserDialog();
                } else {
                    showSpinner(R.string.box_sharesdk_updating_link_access, R.string.boxsdk_Please_wait);
                    mController.executeRequest(BoxItem.class, mController.getCreatedSharedLinkRequest(mShareItem).setPassword(null)).addOnCompletedListener(mBoxItemListener);
                }
            }
        });
        mExpireLinkBtn = (Switch)view.findViewById(R.id.shared_link_expire_link_btn);
        mExpireLinkBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if ((mShareItem.getSharedLink().getUnsharedDate() != null) == isChecked) {
                    // if there is no change or we are busy with another task then do nothing.
                    return;
                }
                if (isChecked) {
                    showDatePicker(new Date());
                } else {
                    try {
                        showSpinner(R.string.box_sharesdk_updating_link_access, R.string.boxsdk_Please_wait);
                        mController.executeRequest(BoxItem.class, mController.getCreatedSharedLinkRequest(mShareItem).setRemoveUnsharedAtDate()).addOnCompletedListener(mBoxItemListener);
                    } catch (ParseException e) {
                        dismissSpinner();
                    }
                }
            }
        });

        mPasswordHeader = view.findViewById(R.id.box_sharesdk_password_header);
        if (!checkIfHasRequiredFields(mShareItem)){
            // we need to refresh since the item given to us is not complete.
            refreshShareItemInfo();
        } else {
            setupUi();
        }


        return view;
    }

    @Override
    public void onPositiveButtonClicked(PositiveNegativeDialogFragment fragment) {
        if (fragment instanceof PasswordDialogFragment){
            try {
                showSpinner();
                changePassword(((PasswordDialogFragment) fragment).getPassword());
            } catch (Exception e){
                dismissSpinner();
                mController.showToast(getActivity(), "invalid password");
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

    /**
     * Updates the UI with the provided BoxItem
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
     * Displays the DatePickerFragment
     *
     * @param date the default date that should be selected
     */
    private void showDatePicker(Date date){
        if (getFragmentManager().findFragmentByTag(DATE_FRAGMENT_TAG) != null){
            return;
        }
        DatePickerFragment fragment = DatePickerFragment.createFragment(date, this, this);
        fragment.show(getActivity().getSupportFragmentManager(), DATE_FRAGMENT_TAG);
    }

    /**
     * Displays the dialog for the user to set a password for the shared link
     */
    private void showPasswordChooserDialog(){
        if (getFragmentManager().findFragmentByTag(PASSWORD_FRAGMENT_TAG) != null){
            return;
        }
        PasswordDialogFragment fragment = PasswordDialogFragment.createFragment(R.string.box_sharesdk_password, R.string.box_sharesdk_set_password, R.string.box_sharesdk_ok, R.string.box_sharesdk_cancel, this);
        fragment.show(getActivity().getSupportFragmentManager(), PASSWORD_FRAGMENT_TAG);
    }

    /**
     * Displays the access dialog for the user to select the appropriate access
     */
    private void showAccessChooserDialog(){
        if (getFragmentManager().findFragmentByTag(ACCESS_RADIAL_FRAGMENT_TAG) != null){
            return;
        }
        if (mShareItem.getAllowedSharedLinkAccessLevels() != null){
            AccessRadialDialogFragment fragment = AccessRadialDialogFragment.createFragment(mShareItem, this);
            fragment.show(getActivity().getSupportFragmentManager(), ACCESS_RADIAL_FRAGMENT_TAG);
        } else {
            mController.showToast(getContext(),R.string.box_sharesdk_network_error);
        }
    }

    /**
     * Initializes the UI
     */
    private void setupUi(){
        BoxSharedLink link =  mShareItem.getSharedLink();
        if (link != null) {
            BoxSharedLink.Access access = link.getEffectiveAccess();
            if (access != null) {
                String accessDescription = "";
                switch (access) {
                    case OPEN:
                        accessDescription = getResources().getString(R.string.box_sharesdk_access_public);
                        break;
                    case COLLABORATORS:
                        if (mShareItem instanceof BoxFile){
                            accessDescription = getResources().getString(R.string.box_sharesdk_access_collaborator_file);
                        } else {
                            accessDescription = getResources().getString(R.string.box_sharesdk_access_collaborator);
                        }
                        break;
                    case COMPANY:
                        accessDescription = getResources().getString(R.string.box_sharesdk_access_company);
                }
                mAccessText.setText(accessDescription);
            }
            if (mShareItem instanceof BoxBookmark || (access != null && access == BoxSharedLink.Access.COLLABORATORS)) {
                hideView(mAllowDownloadsBtn);
            } else {
                showView(mAllowDownloadsBtn);
                mAllowDownloadsBtn.setChecked(link.getPermissions() != null && link.getPermissions().getCanDownload());
            }

            if ((access != null && access == BoxSharedLink.Access.COLLABORATORS) || !mPasswordProtectedLinksSupported){
                hideView(mPasswordSection);
            } else {
                showView(mPasswordSection);
                mRequirePasswordBtn.setChecked(link.getIsPasswordEnabled());
                if (link.getIsPasswordEnabled()) {
                    mPasswordButton.setText(createTitledSpannable(getResources().getString(R.string.box_sharesdk_password), "*****"));
                    showView(mPasswordButton);
                } else {
                    hideView(mPasswordButton);
                }
            }
            if (mPasswordProtectedLinksSupported) {
                showView(mLinkExpirationSection);
                mExpireLinkBtn.setChecked(link.getUnsharedDate() != null);
                if (link.getUnsharedDate() != null) {
                    mExpiresButton.setText(createTitledSpannable(getResources().getString(R.string.box_sharesdk_expire_on), SimpleDateFormat.getDateInstance().format(link.getUnsharedDate())));
                    showView(mExpiresButton);
                } else {
                    hideView(mExpiresButton);
                }
            } else {
                hideView(mLinkExpirationSection);
            }

        } else {
            mController.showToast(getActivity(),getText(R.string.box_sharesdk_problem_accessing_this_shared_link));
            getActivity().finish();
        }
    }

    /**
     * Modifies the download permssion of the share item
     *
     * @param canDownload whether or not the item can be downloaded
     */
    private void changeDownloadPermission(boolean canDownload){
        if (mShareItem instanceof BoxFile) {
            showSpinner(R.string.box_sharesdk_updating_link_access, R.string.boxsdk_Please_wait);
            mController.executeRequest(BoxItem.class, ((BoxRequestsFile.UpdatedSharedFile) mController.getCreatedSharedLinkRequest(mShareItem)).setCanDownload(canDownload)).addOnCompletedListener(mBoxItemListener);
        }
        else if (mShareItem instanceof BoxFolder) {
            showSpinner(R.string.box_sharesdk_updating_link_access, R.string.boxsdk_Please_wait);
            mController.executeRequest(BoxItem.class, ((BoxRequestsFolder.UpdateSharedFolder) mController.getCreatedSharedLinkRequest(mShareItem)).setCanDownload(canDownload)).addOnCompletedListener(mBoxItemListener);
        }
        else if (mShareItem instanceof BoxBookmark) {
            mController.showToast(getActivity(), "Bookmarks do not have a permission that can be changed.");
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
            mController.showToast(getActivity(), "No access chosen");
            return;
        }

        showSpinner(R.string.box_sharesdk_updating_link_access, R.string.boxsdk_Please_wait);
        mController.executeRequest(BoxItem.class, mController.getCreatedSharedLinkRequest(mShareItem).setAccess(access)).addOnCompletedListener(mBoxItemListener);
    }

    
    public static SharedLinkAccessFragment newInstance(BoxItem boxItem) {
        Bundle args = BoxFragment.getBundle(boxItem);
        SharedLinkAccessFragment fragment = new SharedLinkAccessFragment();
        fragment.setArguments(args);
        return fragment;
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
            showSpinner(R.string.box_sharesdk_updating_link_access, R.string.boxsdk_Please_wait);
            mController.executeRequest(BoxItem.class, mController.getCreatedSharedLinkRequest(mShareItem).setUnsharedAt(calendar.getTime())).addOnCompletedListener(mBoxItemListener);
        } catch (Exception e){
            dismissSpinner();
            mController.showToast(getActivity(), "invalid time selected");
        }
    }

    /**
     * Sets the password to the provided string
     *
     * @param password the password to set on the shared item
     * @throws ParseException
     */
    private void changePassword(final String password) throws ParseException{
        mController.executeRequest(BoxItem.class, mController.getCreatedSharedLinkRequest(mShareItem).setPassword(password)).addOnCompletedListener(mBoxItemListener);
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
    public void refreshShareItemInfo() {
        showSpinner();
        mController.fetchItemInfo(mShareItem).addOnCompletedListener(mBoxItemListener);
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
                                    if (checkIfHasRequiredFields(response.getResult())) {
                                        mShareItem = response.getResult();
                                        updateUi();
                                    }
                                }
                            } else {
                                if (response.getException() instanceof BoxException) {
                                    BoxException boxException = (BoxException) response.getException();
                                    int responseCode = boxException.getResponseCode();
                                    if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                                        return;
                                    }

                                    if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                                        mController.showToast(getActivity(), R.string.box_sharesdk_insufficient_permissions);
                                        setupUi();
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

}
