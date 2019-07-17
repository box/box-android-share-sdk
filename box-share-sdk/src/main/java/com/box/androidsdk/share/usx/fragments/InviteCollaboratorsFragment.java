package com.box.androidsdk.share.usx.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.share.databinding.UsxFragmentInviteCollaboratorsBinding;
import com.box.androidsdk.share.internal.models.BoxInvitee;
import com.box.androidsdk.share.vm.ActionbarTitleVM;
import com.box.androidsdk.share.vm.InviteCollaboratorsPresenterData;
import com.box.androidsdk.share.vm.InviteCollaboratorsShareVM;
import com.box.androidsdk.share.vm.PresenterData;
import com.box.androidsdk.share.vm.SelectRoleShareVM;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MultiAutoCompleteTextView;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.utils.BoxLogUtils;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.usx.adapters.InviteeAdapter;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;
import com.tokenautocomplete.TokenCompleteTextView;

import java.util.List;

/**
 * Fragment to let users invite collaborators on an item.
 *
 * There are two listeners used here:
 * 1. InviteCollaboratorsListener is used to set up a listener by the parent Activity or Fragment on this Fragment.
 * 2. ShowCollaboratorsListener is used to set up a listener by this fragment on the child custom view called CollaboratorsInitialsView.
 */

public class InviteCollaboratorsFragment extends BoxFragment implements TokenCompleteTextView.TokenListener<BoxInvitee> {



    private static final Integer MY_PERMISSIONS_REQUEST_READ_CONTACTS = 32;
    public static final String TAG = InviteCollaboratorsFragment.class.getName();
    public static final String EXTRA_USE_CONTACTS_PROVIDER = "InviteCollaboratorsFragment.ExtraUseContactsProvider";
    public static final String EXTRA_COLLAB_SELECTED_ROLE = "collabSelectedRole";

    private String mFilterTerm;
    UsxFragmentInviteCollaboratorsBinding binding;

    private View.OnClickListener mOnEditAccessListener;
    InviteCollaboratorsShareVM mInviteCollaboratorsShareVM;
    SelectRoleShareVM mSelectRoleShareVM;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.usx_fragment_invite_collaborators, container,false);
        View view = binding.getRoot();
        binding.setLifecycleOwner(getViewLifecycleOwner());

        mSelectRoleShareVM = ViewModelProviders.of(getActivity()).get(SelectRoleShareVM.class);

        InviteeAdapter adapter = createInviteeAdapter(getActivity());
        MultiAutoCompleteTextView.CommaTokenizer tokenizer = new MultiAutoCompleteTextView.CommaTokenizer();
        adapter.setInviteeAdapterListener(createInviteeAdapterListener());

        binding.setAdapter(adapter);
        binding.setTokenizer(tokenizer);
        binding.setOnRoleClickedListener(mOnEditAccessListener);
        binding.setOnSendInvitationClickedListener(v -> addCollaborations());
        binding.setTokenListener(this);
        binding.setCollaboratorsPresent(mSelectRoleShareVM.isSendInvitationEnabled());

        binding.inviteCollaboratorAutocomplete.requestFocus();

        mFilterTerm = "";
        mInviteCollaboratorsShareVM = ViewModelProviders.of(getActivity(), mShareVMFactory).get(InviteCollaboratorsShareVM.class);
        mInviteCollaboratorsShareVM.setInvitationSucceded(true);

        mInviteCollaboratorsShareVM.getRoleItem().observe(this, onRoleItemChange);
        mInviteCollaboratorsShareVM.getInvitees().observe(this, onInviteesChanged);
        mInviteCollaboratorsShareVM.getInviteCollabs().observe(this, onInviteCollabs);


        if (mSelectRoleShareVM.getSelectedRole() == null && savedInstanceState != null) {
            String selected_role_enum = savedInstanceState.getString(EXTRA_COLLAB_SELECTED_ROLE);
            if (selected_role_enum != null){
                setSelectedRole(BoxCollaboration.Role.fromString(selected_role_enum));
            }
        }

        // Get serialized roles or fetch them if they are not available
        if (getCollaborationItem() != null && getCollaborationItem().getAllowedInviteeRoles() != null) {
            if(getCollaborationItem().getPermissions().contains(BoxItem.Permission.CAN_INVITE_COLLABORATOR)) {
                mSelectRoleShareVM.setRoles(getCollaborationItem().getAllowedInviteeRoles());
                if (mSelectRoleShareVM.getSelectedRole().getValue() == null) {
                    BoxCollaboration.Role defaultRole = getBestDefaultRole(getCollaborationItem().getDefaultInviteeRole(), mSelectRoleShareVM.getRoles());
                    setSelectedRole(defaultRole);
                }
            } else {
                showNoPermissionToast();
                getActivity().finish();
            }
        } else {
            fetchRoles();
        }

        fetchInvitees();
        if (getArguments().getBoolean(EXTRA_USE_CONTACTS_PROVIDER)){
            requestPermissionsIfNecessary();
        }

        for (BoxInvitee invitee: mInviteCollaboratorsShareVM.getInviteesList()) {
            binding.inviteCollaboratorAutocomplete.addObject(invitee);
        }

        binding.setRole(mSelectRoleShareVM.getSelectedRole());
        return view;
    }

    private Observer<PresenterData<BoxCollaborationItem>> onRoleItemChange = presenter -> {
        dismissSpinner();
        if (presenter.isSuccess() && getCollaborationItem() != null) {
            if (getCollaborationItem().getPermissions().contains(BoxItem.Permission.CAN_INVITE_COLLABORATOR)) {
                BoxCollaborationItem collaborationItem = presenter.getData();
                mSelectRoleShareVM.setRoles(collaborationItem.getAllowedInviteeRoles());
                BoxCollaboration.Role role = mSelectRoleShareVM.getSelectedRole().getValue();
                if (role != null) {
                    setSelectedRole(role);
                } else {
                    List<BoxCollaboration.Role> roles = mSelectRoleShareVM.getRoles();
                    BoxCollaboration.Role selectedRole = roles != null && roles.size() > 0 ? getBestDefaultRole(collaborationItem.getDefaultInviteeRole(), roles) : null;
                    setSelectedRole(selectedRole);
                }
                mInviteCollaboratorsShareVM.setShareItem(collaborationItem);
            } else {
                showNoPermissionToast();
                getActivity().finish();
            }
        } else {
            //need to log Exception
            BoxLogUtils.e(InviteCollaboratorsFragment.class.getName(), "Fetch roles request failed",
                    presenter.getException());
            showToast(getString(presenter.getStrCode()));
        }
    };

    private Observer<PresenterData<BoxIteratorInvitees>> onInviteesChanged = presenter -> {
        if (presenter.isSuccess()) {
            binding.getAdapter().setInvitees(presenter.getData());
        } else {
            BoxLogUtils.e(InviteCollaboratorsFragment.class.getName(), "get invitees request failed",
                    presenter.getException());
            showToast(getString(presenter.getStrCode()) + ((BoxException)presenter.getException()).getResponseCode()); //need response code
        }
    };

    private Observer<InviteCollaboratorsPresenterData> onInviteCollabs = presenter -> {
        dismissSpinner();
        String message;
        int alreadyAddedCount = presenter.getAlreadyAdddedCount();
        if (presenter.isNonNullData()) {
            if (alreadyAddedCount == 1) {
                message = getResources().getQuantityString(presenter.getStrCode(), alreadyAddedCount, presenter.getData());
            } else if (alreadyAddedCount > 1) {
                message = getResources().getQuantityString(presenter.getStrCode(), alreadyAddedCount, String.valueOf(alreadyAddedCount));
            } else {
                message = getString(presenter.getStrCode(), presenter.getData());
            }

        } else {
            message = getString(presenter.getStrCode());
        }
        if (presenter.isSnackBarMessage()) {
            showSnackBar(message);
            //Snackbar.make(getView(), message, Snackbar.LENGTH_INDEFINITE).show();
        } else {
            showToast(message);
            getActivity().finish();
        }
        mInviteCollaboratorsShareVM.setInvitationSucceded(presenter.isSuccess());
    };

    @Override
    public int getActivityResultCode() {
        if (mInviteCollaboratorsShareVM.isInvitationSucceded()) {
            return Activity.RESULT_OK;

        }
        return Activity.RESULT_CANCELED;
    }

    @Override
    protected void setTitles() {
        ActionbarTitleVM actionbarTitleVM = ViewModelProviders.of(getActivity()).get(ActionbarTitleVM.class);
        actionbarTitleVM.setTitle(getString(R.string.box_sharesdk_invite_collaborators_activity_title));
        actionbarTitleVM.setSubtitle(null);
    }


    public void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_INDEFINITE);
        View snackbarLayout = snackbar.getView();
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) snackbarLayout.getLayoutParams();
        int height = (int)getResources().getDimension(R.dimen.box_sharesdk_small_cell_height);
        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, height);
        snackbar.show();
    }




    private BoxCollaboration.Role getBestDefaultRole(String roleName, List<BoxCollaboration.Role> roles){
        try {
            return BoxCollaboration.Role.fromString(roleName);
        } catch (IllegalArgumentException e){
            BoxLogUtils.e("invalid role name " + roleName, e);
            return roles.get(0);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mSelectRoleShareVM.getSelectedRole() != null) {
            outState.putString(EXTRA_COLLAB_SELECTED_ROLE, mSelectRoleShareVM.getSelectedRole().getValue().toString());
        }
        super.onSaveInstanceState(outState);
    }

    private InviteeAdapter createInviteeAdapter(final Context context){
        return new InviteeAdapter(context) {
            @Override
            protected boolean isReadContactsPermissionAvailable() {
                return getArguments().getBoolean(EXTRA_USE_CONTACTS_PROVIDER, true) && super.isReadContactsPermissionAvailable();
            }
        };
    }
    private InviteeAdapter.InviteeAdapterListener createInviteeAdapterListener() {
        return constraint -> {
            if (constraint.length() >= 3) {
                String firstThreeChars = constraint.subSequence(0, 3).toString();
                if (!firstThreeChars.equals(mFilterTerm)) {
                    mFilterTerm = firstThreeChars;
                    fetchInvitees();
                }
            }
        };
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Attach the listener to view once createView is complete
    }


    private void requestPermissionsIfNecessary() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    public void setOnEditAccessListener(View.OnClickListener listener) {
        mOnEditAccessListener = listener;
    }
    /**
     * Executes the request to retrieve the available roles for the item
     */
    private void fetchRoles() {
        if (getCollaborationItem() == null || SdkUtils.isBlank(getCollaborationItem().getId())) {
            return;
        }

        showSpinner(R.string.box_sharesdk_fetching_collaborators, R.string.boxsdk_Please_wait);
        mInviteCollaboratorsShareVM.fetchRoles(getCollaborationItem());
    }
    /**
     * Executes the request to retrieve the invitees that can be auto-completed
     */
    private void fetchInvitees() {
        if (getCollaborationItem() instanceof BoxFolder) {
            // Currently this request is only supported for folders.
            mInviteCollaboratorsShareVM.fetchInvitees(getCollaborationItem(), mFilterTerm);
        }
    }

    /**
     * Executes the request to add collaborations to the item
     */
    public void addCollaborations() {
        List<BoxInvitee> invitees = mInviteCollaboratorsShareVM.getInviteesList();
        String[] emailParts = new String[invitees.size()];
        int i = 0;
        for (BoxInvitee invitee: invitees) {
            emailParts[i++] = invitee.getEmail();
        }

        showSpinner(R.string.box_sharesdk_adding_collaborators, R.string.boxsdk_Please_wait);
        mInviteCollaboratorsShareVM.inviteCollabs(getCollaborationItem(), mSelectRoleShareVM.getSelectedRole().getValue(), emailParts);
    }



    @NonNull
    private String getItemType(BoxItem boxItem) {
        if (boxItem instanceof BoxFolder) {
            return getString(com.box.sdk.android.R.string.boxsdk_folder);
        } else if (boxItem instanceof BoxFile) {
            return getString(com.box.sdk.android.R.string.boxsdk_file);
        } else {
            //default return folder as the type
            return getString(com.box.sdk.android.R.string.boxsdk_folder);
        }
    }

    private void showNoPermissionToast() {
        showToast(R.string.box_sharesdk_insufficient_permissions);
    }

    /**
     * Sets the selected role in the UI
     *
     * @param role the collaboration role to select
     */
    private void setSelectedRole(BoxCollaboration.Role role) {
        mSelectRoleShareVM.setSelectedRole(role);
    }

    protected BoxCollaborationItem getCollaborationItem() {
        return (BoxCollaborationItem) mInviteCollaboratorsShareVM.getShareItem();
    }

    public static InviteCollaboratorsFragment newInstance(BoxCollaborationItem collaborationItem) {
        return newInstance(collaborationItem, true);
    }

    public static InviteCollaboratorsFragment newInstance(BoxCollaborationItem collaborationItem, boolean useContactsProvider) {
        Bundle args = BoxFragment.getBundle(collaborationItem);
        InviteCollaboratorsFragment fragment = new InviteCollaboratorsFragment();
        args.putBoolean(EXTRA_USE_CONTACTS_PROVIDER, useContactsProvider);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onTokenAdded(BoxInvitee token) {
        mInviteCollaboratorsShareVM.setInviteesList(binding.inviteCollaboratorAutocomplete.getObjects());
        mSelectRoleShareVM.setSendInvitationEnabled(true);
    }

    @Override
    public void onTokenRemoved(BoxInvitee token) {
        mInviteCollaboratorsShareVM.setInviteesList(binding.inviteCollaboratorAutocomplete.getObjects());
        if (mInviteCollaboratorsShareVM.getInviteesList().isEmpty()) mSelectRoleShareVM.setSendInvitationEnabled(false);
    }

}
