package com.box.androidsdk.share.usx.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.databinding.UsxFragmentSharedLinkBinding;
import com.box.androidsdk.share.vm.ActionbarTitleVM;
import com.box.androidsdk.share.vm.CollaboratorsInitialsVM;
import com.box.androidsdk.share.vm.PresenterData;
import com.box.androidsdk.share.vm.SharedLinkVM;

/**
 * Created by varungupta on 3/5/2016.
 */
public class UsxFragment extends BoxFragment {

    @Override
    protected void setTitles() {
        ActionbarTitleVM actionbarTitleVM = ViewModelProviders.of(getActivity()).get(ActionbarTitleVM.class);
        actionbarTitleVM.setTitle(mSharedLinkVm.getShareItem().getName());
        actionbarTitleVM.setSubtitle(capitalizeFirstLetterOfEveryWord(mSharedLinkVm.getShareItem().getType()));
    }

    public interface UsxNotifiers {
        void notifyUnshare();

        void notifyShare();

        void linkClicked();
    }

    private static final String UNSHARE_WARNING_TAG = "com.box.sharesdk.unshare_warning";

    private View.OnClickListener mOnEditAccessClickListener;
    private View.OnClickListener mOnInviteCollabsClickListener;
    private View.OnClickListener mOnCollabsClickListener;
    UsxFragmentSharedLinkBinding binding;
    SharedLinkVM mSharedLinkVm;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.usx_fragment_shared_link, container, false);
        mSharedLinkVm = ViewModelProviders.of(getActivity(), mShareVMFactory).get(SharedLinkVM.class);
        setupListeners();

        binding.setShareItem(mSharedLinkVm.getShareItem());
        binding.setShareLinkVm(mSharedLinkVm);
        binding.setUsxNotifier(new UsxNotifiers() {
            @Override
            public void notifyUnshare() {
                displayUnshareWarning();
            }

            @Override
            public void notifyShare() {
                createDefaultShareItem();
            }

            @Override
            public void linkClicked() { copyLink();}
        });

        setTitles();


        mSharedLinkVm.getSharedLinkedItem().observe(this, onBoxItemComplete);

        mSharedLinkVm.getItemInfo().observe(this, onBoxItemComplete);



        View view = binding.getRoot();

        binding.setLifecycleOwner(getViewLifecycleOwner());
        return view;
    }

    private void setupListeners() {
        binding.setOnInviteCollabsClickListener(mOnInviteCollabsClickListener);
        binding.setOnEditAccessClickListener(mOnEditAccessClickListener);
        binding.setOnCollabsListener(mOnCollabsClickListener);
        binding.setOnCopyLinkListener(v -> copyLink());
        CollaboratorsInitialsVM vm = ViewModelProviders.of(getActivity(), mShareVMFactory).get(CollaboratorsInitialsVM.class);
        binding.initialViews.setArguments(vm);
    }

    public void refreshItemInfo() {
        showSpinner();
        mSharedLinkVm.fetchItemInfo(mSharedLinkVm.getShareItem());
    }

    private Observer<PresenterData<BoxItem>> onBoxItemComplete = boxItemPresenterData -> {
        dismissSpinner();
        if (boxItemPresenterData.isSuccess() && boxItemPresenterData.getData() != null) {
            //data might still be null if the original request was not BoxRequestItem
            setShareItem(boxItemPresenterData.getData());
        } else {
            if(boxItemPresenterData.getStrCode() != PresenterData.NO_MESSAGE) {
                showToast(boxItemPresenterData.getStrCode());
            }
        }
    };

    public void setOnEditLinkAccessButtonClickListener(View.OnClickListener onEditLinkAccessButtonClickListener) {
        this.mOnEditAccessClickListener = onEditLinkAccessButtonClickListener;
    }

    public void setOnInviteCollabsClickListener(View.OnClickListener onInviteCollabsClickListener) {
        this.mOnInviteCollabsClickListener = onInviteCollabsClickListener;
    }

    public void setOnCollabsListener(View.OnClickListener onInviteCollabsClickListener) {
        this.mOnCollabsClickListener = onInviteCollabsClickListener;
    }

    public void refreshInitialsViews() {
        if (binding !=  null && binding.initialViews != null) {
            binding.initialViews.refreshView();
        }

    }

    public static UsxFragment newInstance(BoxItem item) {
        Bundle args = BoxFragment.getBundle(item);
        UsxFragment fragment = new UsxFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Executes the create shared link request for the appropriate item type of getMainItem
     */
    private void createDefaultShareItem(){
        showSpinner(R.string.box_sharesdk_enabling_share_link, R.string.boxsdk_Please_wait);
        mSharedLinkVm.createDefaultSharedLink((BoxCollaborationItem) mSharedLinkVm.getShareItem());
    }

    /**
     * Executes the disable share link request for the appropriate item type of getMainItem
     */
    private void disableShareItem(){
        showSpinner(R.string.box_sharesdk_disabling_share_link, R.string.boxsdk_Please_wait);
        mSharedLinkVm.disableSharedLink((BoxCollaborationItem) mSharedLinkVm.getShareItem());
    }

    private void copyLink() {
        if (mSharedLinkVm.getShareItem().getSharedLink() != null) {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
            BoxSharedLink sharedLink = mShareItem.getSharedLink();
            ClipData clipData = ClipData.newPlainText("", sharedLink.getURL());
            clipboard.setPrimaryClip(clipData);
            showToast(R.string.box_sharesdk_link_copied_to_clipboard);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshItemInfo();
    }

    /**
     * Displays the modal to confirm if the user wants to un-share the item.
     */
    private void displayUnshareWarning() {
        if (getFragmentManager().findFragmentByTag(UNSHARE_WARNING_TAG) != null){
            return;
        }
        PositiveNegativeDialogFragment.createFragment(R.string.box_sharesdk_disable_title,
                R.string.box_sharesdk_disable_message, R.string.box_sharesdk_disable_share_link,
                R.string.box_sharesdk_cancel, new PositiveNegativeDialogFragment.OnPositiveOrNegativeButtonClickedListener() {
                    @Override
                    public void onPositiveButtonClicked(PositiveNegativeDialogFragment fragment) {
                        disableShareItem();
                    }

                    @Override
                    public void onNegativeButtonClicked(PositiveNegativeDialogFragment fragment) {
                        binding.sharedLinkSwitch.setChecked(true);
                    }
                })
                .show(getActivity().getSupportFragmentManager(), UNSHARE_WARNING_TAG);
    }

    public void setShareItem(BoxItem item) {
        mSharedLinkVm.setShareItem(item);
        binding.setShareItem(item);
    }

}
