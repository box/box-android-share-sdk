package com.box.androidsdk.share.usx.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
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
import com.box.androidsdk.content.models.BoxPermission;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.databinding.UsxFragmentSharedLinkBinding;
import com.box.androidsdk.share.vm.ActionbarTitleVM;
import com.box.androidsdk.share.vm.CollaboratorsInitialsVM;
import com.box.androidsdk.share.vm.PresenterData;
import com.box.androidsdk.share.vm.ShareVMFactory;
import com.box.androidsdk.share.vm.SharedLinkVM;

import java.util.EnumSet;

/**
 * Created by varungupta on 3/5/2016.
 */
public class UsxFragment extends BoxFragment {

    private ClickListener mListener;

    @Override
    public Class<SharedLinkVM> getVMClass() {
        return SharedLinkVM.class;
    }

    @Override
    protected void setTitles() {
        ActionbarTitleVM actionbarTitleVM = ViewModelProviders.of(getActivity()).get(ActionbarTitleVM.class);
        actionbarTitleVM.setTitle(mSharedLinkVm.getShareItem().getName());
        actionbarTitleVM.setSubtitle(CollaborationUtils.getSubtitleForItemType(getContext(), mSharedLinkVm.getShareItem().getType()));
    }

    public interface UsxNotifiers {
        void notifyUnshare();

        void notifyShare();

        void linkClicked();
    }

    public interface ClickListener {
        void editAccessClicked();
        void inviteCollabsClicked();
        void collabsClicked();
    }

    private static final String UNSHARE_WARNING_TAG = "com.box.sharesdk.unshare_warning";
    private UsxFragmentSharedLinkBinding binding;
    private SharedLinkVM mSharedLinkVm;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.usx_fragment_shared_link, container, false);
        mSharedLinkVm = ViewModelProviders.of(getActivity(), mShareVMFactory).get(SharedLinkVM.class);
        setupListeners();

        binding.setShareItem(mSharedLinkVm.getShareItem());
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
        mSharedLinkVm.getItemInfo().observe(getViewLifecycleOwner(), onBoxItemComplete);
        mSharedLinkVm.getSharedLinkedItem().observe(getViewLifecycleOwner(), onBoxItemComplete);


        View view = binding.getRoot();

        binding.setOnShareViaListener(v -> showShareVia());
        return view;
    }

    private boolean isAllowedToInvite() {
        EnumSet<BoxItem.Permission> permissions = mSharedLinkVm.getShareItem().getPermissions();
        return permissions != null && permissions.contains(BoxItem.Permission.CAN_INVITE_COLLABORATOR);
    }

    private boolean isAllowedToShare() {
        EnumSet<BoxItem.Permission> permissions = mSharedLinkVm.getShareItem().getPermissions();
        return permissions != null && permissions.contains(BoxItem.Permission.CAN_SHARE);
    }

    private void setupListeners() {
        binding.setOnInviteCollabsClickListener(v -> mListener.inviteCollabsClicked());
        binding.setOnEditAccessClickListener(v -> mListener.editAccessClicked());
        binding.setOnCollabsListener(v -> mListener.collabsClicked());

        binding.setOnCopyLinkListener(v -> copyLink());
        CollaboratorsInitialsVM vm = ViewModelProviders.of(getActivity(), mShareVMFactory).get(CollaboratorsInitialsVM.class);
        binding.initialViews.setArguments(vm);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSharedLinkVm.getShareItem().getSharedLink() == null) {
            showSpinner(0);
            mSharedLinkVm.fetchItemInfo(mSharedLinkVm.getShareItem());
        }
    }

    private Observer<PresenterData<BoxItem>> onBoxItemComplete = presenterData -> {
        if (!presenterData.isHandled()) {
            dismissSpinner();
            if (presenterData.isSuccess() && presenterData.getData() != null) {
                //data might still be null if the original request was not BoxRequestItem
                setShareItem(presenterData.getData());
            } else {
                if (presenterData.getStrCode() != PresenterData.NO_MESSAGE) {
                    showToast(presenterData.getStrCode());
                }
                refreshUI();
            }
        }
    };

    private void refreshUI() {
        binding.setShareItem(mSharedLinkVm.getShareItem()); //data binding is used to display data based on this item. This will force the UI to refresh.
    }

    public void refreshInitialsViews() {
        if (binding !=  null && binding.initialViews != null) {
            binding.initialViews.refreshView();
        }

    }

    public static UsxFragment newInstance(BoxItem item, ClickListener listener, ShareVMFactory factory) {
        Bundle args = BoxFragment.getBundle(item);
        UsxFragment fragment = new UsxFragment();
        fragment.setArguments(args);
        fragment.mListener = listener;
        fragment.mShareVMFactory = factory;
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
            BoxSharedLink sharedLink = mSharedLinkVm.getShareItem().getSharedLink();
            ClipData clipData = ClipData.newPlainText("", sharedLink.getURL());
            clipboard.setPrimaryClip(clipData);
            showToast(R.string.box_sharesdk_link_copied_to_clipboard);
        }
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
                        refreshUI();
                    }
                })
                .show(getActivity().getSupportFragmentManager(), UNSHARE_WARNING_TAG);
    }

    public void setShareItem(BoxItem item) {
        mSharedLinkVm.setShareItem(item);
        refreshUI();
    }

    private void showShareVia() {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, String.format(getString(R.string.box_sharesdk_I_have_shared_x_with_you), mSharedLinkVm.getShareItem().getName()));
        emailIntent.putExtra(Intent.EXTRA_TEXT, mSharedLinkVm.getShareItem().getSharedLink().getURL());
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(Intent.createChooser(emailIntent, getString(R.string.box_sharesdk_send_with)));
    }

}
