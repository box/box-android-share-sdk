package com.box.androidsdk.share.usx.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.databinding.UsxFragmentSharedLinkAccessBinding;
import com.box.androidsdk.share.vm.ActionbarTitleVM;
import com.box.androidsdk.share.vm.PresenterData;
import com.box.androidsdk.share.vm.ShareVMFactory;
import com.box.androidsdk.share.vm.SharedLinkVM;

import java.util.Date;
import java.util.GregorianCalendar;


public class SharedLinkAccessFragment extends BoxFragment {

    private static final String DATE_FRAGMENT_TAG = "datePicker";
    private static final String PASSWORD_FRAGMENT_TAG = "passwordFrag";
    private static final String ACCESS_RADIAL_FRAGMENT_TAG = "accessFrag";

    private SharedLinkVM mShareLinkVM;
    private UsxFragmentSharedLinkAccessBinding binding;

    private SharedLinkAccessNotifiers notifier = new SharedLinkAccessNotifiers() {
        @Override
        public void notifyAccessLevelChange(BoxSharedLink.Access newAccess) {
            if (newAccess != null && newAccess != mShareLinkVM.getShareItem().getSharedLink().getEffectiveAccess()) {
                changeAccess(newAccess);
            }

        }

        @Override
        public void notifyDownloadChange(boolean download) {
            changeDownloadPermission(download);
        }

        @Override
        public void notifyRequirePassword(boolean required) {
            if (required) {
                showPasswordChooserDialog();
            } else {
                showSpinner(R.string.box_sharesdk_updating_link_access, R.string.boxsdk_Please_wait);
                changePassword(null);
            }
        }

        @Override
        public void notifyExpireLink(boolean expire) {
            if (expire) {
                showDatePicker(new Date());
            } else {
                try {
                    showSpinner(R.string.box_sharesdk_updating_link_access, R.string.boxsdk_Please_wait);
                    mShareLinkVM.removeExpiryDate((BoxCollaborationItem) mShareLinkVM.getShareItem());
                } catch (Exception e) {
                    dismissSpinner();
                }
            }
        }
    };

    public interface SharedLinkAccessNotifiers {
        void notifyAccessLevelChange(BoxSharedLink.Access access);
        void notifyDownloadChange(boolean download);
        void notifyRequirePassword(boolean required);
        void notifyExpireLink(boolean expire);
    }


    @Override
    public Class<SharedLinkVM> getVMClass() {
        return SharedLinkVM.class;
    }

    @Override
    protected void setTitles() {
        ActionbarTitleVM actionbarTitleVM = ViewModelProviders.of(getActivity()).get(ActionbarTitleVM.class);
        actionbarTitleVM.setTitle(getString(R.string.box_sharesdk_title_link_access));
        actionbarTitleVM.setSubtitle(null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.usx_fragment_shared_link_access, container, false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        View view = binding.getRoot();

        setTitles();

        mShareLinkVM = ViewModelProviders.of(getActivity(), mShareVMFactory).get(SharedLinkVM.class);
        mShareLinkVM.getSharedLinkedItem().observe(getViewLifecycleOwner(), onBoxItemComplete);

        setupUi();

        return view;
    }


    private void setupUi() {
        binding.setViewModel(mShareLinkVM);
        //binding.accessRadioGroup.setViewModel(mShareLinkVM);
        //binding.accessRadioGroup.setShareItem(mShareLinkVM.getShareItem());
        if (mShareLinkVM.getActiveRadioButtons().isEmpty()) mShareLinkVM.setActiveRadioButtons(mShareLinkVM.generateActiveButtons());

        binding.setSharedLinkAccessNotifier(notifier);
       // binding.accessRadioGroup.setSharedLinkAccessNotifier(notifier);
        binding.setOnPasswordListener(v -> showPasswordChooserDialog());
        binding.setOnDateListener(v -> showDatePicker(mShareLinkVM.getShareItem().getSharedLink().getUnsharedDate()));
        refreshUI();

    }



    /**
     * Modifies the share link access
     *
     * @param access the share link access level
     */
    private void changeAccess(final BoxSharedLink.Access access){
        if (access == null){
            // Should not be possible to get here.
            return;
        }

        showSpinner(R.string.box_sharesdk_updating_link_access, R.string.boxsdk_Please_wait);
        mShareLinkVM.changeAccessLevel((BoxCollaborationItem) mShareLinkVM.getShareItem(), access);
    }

    /**
     * Modifies the download permssion of the share item
     *
     * @param canDownload whether or not the item can be downloaded
     */
    private void changeDownloadPermission(boolean canDownload){
        try {
            mShareLinkVM.changeDownloadPermission((BoxCollaborationItem) mShareLinkVM.getShareItem(), canDownload);
        } catch (Exception e){
            showToast("Bookmarks do not have a permission that can be changed.");
        }
    }

    /**
     * Sets the password to the provided string
     *
     * @param password the password to set on the shared item
     */
    private void changePassword(final String password) {

        mShareLinkVM.changePassword((BoxCollaborationItem) mShareLinkVM.getShareItem(), password);
    }

    /**
     * Displays the dialog for the user to set a password for the shared link
     */
    private void showPasswordChooserDialog(){
        if (getFragmentManager().findFragmentByTag(PASSWORD_FRAGMENT_TAG) != null){
            return;
        }
        PasswordDialogFragment fragment = PasswordDialogFragment.
                createFragment(R.string.box_sharesdk_password, R.string.box_sharesdk_set_password, R.string.box_sharesdk_ok, R.string.box_sharesdk_cancel, new PositiveNegativeDialogFragment.OnPositiveOrNegativeButtonClickedListener() {
                    @Override
                    public void onPositiveButtonClicked(PositiveNegativeDialogFragment fragment) {
                        try {
                            showSpinner();
                            changePassword(((PasswordDialogFragment) fragment).getPassword());
                        } catch(Exception e) {
                            dismissSpinner();
                            showToast("Invalid password");
                        }
                    }

                    @Override
                    public void onNegativeButtonClicked(PositiveNegativeDialogFragment fragment) {
                        refreshUI();
                    }
                });
        fragment.show(getActivity().getSupportFragmentManager(), PASSWORD_FRAGMENT_TAG);
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
        DatePickerFragment fragment = DatePickerFragment.createFragment(date, new DatePickerDialog.OnDateSetListener() {

            /**
             * Handles when a date is selected on the DatePickerFragment
             *
             * @param view the DatePicker view
             * @param year the year
             * @param month the month
             * @param day the day
             */
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                // Do something with the date chosen by the user
                GregorianCalendar calendar = new GregorianCalendar(year, month, day);
                try {
                    showSpinner(R.string.box_sharesdk_updating_link_access, R.string.boxsdk_Please_wait);
                    mShareLinkVM.setExpiryDate((BoxCollaborationItem) mShareLinkVM.getShareItem(), calendar.getTime());
                } catch (Exception e){
                    dismissSpinner();
                    showToast("invalid time selected");
                }
            }
        }, new PositiveNegativeDialogFragment.OnPositiveOrNegativeButtonClickedListener() {
            @Override
            public void onPositiveButtonClicked(PositiveNegativeDialogFragment fragment) {

            }

            @Override
            public void onNegativeButtonClicked(PositiveNegativeDialogFragment fragment) {
                refreshUI();
            }
        });
        fragment.show(getActivity().getSupportFragmentManager(), DATE_FRAGMENT_TAG);
    }

    public static SharedLinkAccessFragment newInstance(BoxItem boxItem, ShareVMFactory factory) {
        Bundle args = BoxFragment.getBundle(boxItem);
        SharedLinkAccessFragment fragment = new SharedLinkAccessFragment();
        fragment.setArguments(args);
        fragment.mShareVMFactory = factory;
        return fragment;
    }


    private Observer<PresenterData<BoxItem>> onBoxItemComplete = boxItemPresenterData -> {
        dismissSpinner();
        if (!boxItemPresenterData.isHandled()) {
            if (boxItemPresenterData.isSuccess() && boxItemPresenterData.getData() != null) {
                //data might still be null if the original request was not BoxRequestItem
                setShareItem(boxItemPresenterData.getData());
            } else {
                if(boxItemPresenterData.getStrCode() != PresenterData.NO_MESSAGE) {
                    showToast(boxItemPresenterData.getStrCode());
                }
                refreshUI();
            }
        }
    };


    public void refreshUI() {
        if (mShareLinkVM.getShareItem().getSharedLink() == null) {
            showToast(R.string.box_sharesdk_problem_accessing_this_shared_link);
            getActivity().finish();
        } else {
            binding.setShareItem(mShareLinkVM.getShareItem());
        }

    }

    public void setShareItem(BoxItem item) {
        mShareLinkVM.setShareItem(item);
        refreshUI();
       // binding.accessRadioGroup.setShareItem(mShareLinkVM.getShareItem());
    }
}
