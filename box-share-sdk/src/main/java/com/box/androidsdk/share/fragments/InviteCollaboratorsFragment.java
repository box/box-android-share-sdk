package com.box.androidsdk.share.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxUser;
import com.box.androidsdk.content.requests.BoxRequestsShare;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.content.utils.BoxLogUtils;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.adapters.InviteeAdapter;
import com.box.androidsdk.share.internal.models.BoxInvitee;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;
import com.box.androidsdk.share.ui.ChipCollaborationView;
import com.box.androidsdk.share.views.CollaboratorsInitialsView;
import com.tokenautocomplete.TokenCompleteTextView;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to let users invite collaborators on a folder.
 *
 * There are two listeners used here:
 * 1. InviteCollaboratorsListener is used to set up a listener by the parent Activity or Fragment on this Fragment.
 * 2. ShowCollaboratorsListener is used to set up a listener by this fragment on the child custom view called CollaboratorsInitialsView.
 */

public class InviteCollaboratorsFragment extends BoxFragment implements View.OnClickListener, CollaborationRolesDialog.OnRoleSelectedListener, TokenCompleteTextView.TokenListener<BoxInvitee>, InviteeAdapter.InviteeAdapterListener, CollaboratorsInitialsView.ShowCollaboratorsListener {

    // Should be implemented by the parent Fragment or Activity
    public interface InviteCollaboratorsListener {
        void onShowCollaborators(BoxIteratorCollaborations collaborations);
        void onCollaboratorsPresent();
        void onCollaboratorsAbsent();
    }

    private static final Integer MY_PERMISSIONS_REQUEST_READ_CONTACTS = 32;
    public static final String TAG = InviteCollaboratorsFragment.class.getName();
    public static final String EXTRA_USE_CONTACTS_PROVIDER = "InviteCollaboratorsFragment.ExtraUseContactsProvider";
    private Button mRoleButton;
    private ChipCollaborationView mAutoComplete;
    private InviteeAdapter mAdapter;
    private BoxCollaboration.Role mSelectedRole;
    private ArrayList<BoxCollaboration.Role> mRoles;
    private InviteCollaboratorsListener mInviteCollaboratorsListener;
    private String mFilterTerm;
    private CollaboratorsInitialsView mCollabInitialsView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_collaborators, container, false);

        mFilterTerm = "";
        mRoleButton = (Button) view.findViewById(R.id.invite_collaborator_role);
        mRoleButton.setOnClickListener(this);
        mAutoComplete = (ChipCollaborationView) view.findViewById(R.id.invite_collaborator_autocomplete);
        mAutoComplete.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        mAdapter = createInviteeAdapter(getActivity());
        mAdapter.setInviteeAdapterListener(this);
        mAutoComplete.setAdapter(mAdapter);
        mAutoComplete.setTokenListener(this);

        mCollabInitialsView = (CollaboratorsInitialsView) view.findViewById(R.id.collaboratorsInitialsView);
        mCollabInitialsView.setArguments((BoxFolder) mShareItem, mController);

        // Get serialized roles or fetch them if they are not available
        if (getFolder() != null && getFolder().getAllowedInviteeRoles() != null) {
            mRoles = getFolder().getAllowedInviteeRoles();
            BoxCollaboration.Role selectedRole = mRoles.get(0);
            setSelectedRole(selectedRole);
        } else {
            fetchRoles();
        }

        fetchInvitees();
        if (getArguments().getBoolean(EXTRA_USE_CONTACTS_PROVIDER)){
            requestPermissionsIfNecessary();
        }

        return view;
    }

    /**
     * Refresh the CollabInitialsView
     * This will invalidate the previous cached response that contains collaborators and instead
     * force the view to make a new request to refresh itself with new information (in case there are updates)
     */
    public void refreshUi() {
        if (mCollabInitialsView != null) {
            mCollabInitialsView.refreshView();
        }
    }

    protected InviteeAdapter createInviteeAdapter(final Context context){
        return new InviteeAdapter(context) {
            @Override
            protected boolean isReadContactsPermissionAvailable() {
                return getArguments().getBoolean(EXTRA_USE_CONTACTS_PROVIDER, true) && super.isReadContactsPermissionAvailable();
            }
        };
    }

    public boolean areCollaboratorsPresent() {
        if (mAutoComplete != null) {
            return mAutoComplete.getObjects().size() > 0;
        }

        return false;
    }

    @Override
    public void onShowCollaborators(BoxIteratorCollaborations collaborations) {
        mInviteCollaboratorsListener.onShowCollaborators(collaborations);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Attach the listener to view once createView is complete
        mCollabInitialsView.setShowCollaboratorsListener(this);
    }

    public void setInviteCollaboratorsListener(InviteCollaboratorsListener listener) {
        mInviteCollaboratorsListener = listener;
    }

    private void requestPermissionsIfNecessary() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    private void notifyInviteCollaboratorsListener() {
        if (mInviteCollaboratorsListener != null) {
            int count = mAutoComplete.getObjects().size();
            if (count > 0) {
                mInviteCollaboratorsListener.onCollaboratorsPresent();
            } else {
                mInviteCollaboratorsListener.onCollaboratorsAbsent();
            }
        }
    }

    @Override
    public void onFilterTermChanged(CharSequence constraint) {
        if (constraint.length() >= 3) {
            String firstThreeChars = constraint.subSequence(0, 3).toString();
            if (!firstThreeChars.equals(mFilterTerm)) {
                mFilterTerm = firstThreeChars;
                fetchInvitees();
            }
        }
    }

    @Override
    public void onTokenAdded(BoxInvitee token) {
        notifyInviteCollaboratorsListener();
    }

    @Override
    public void onTokenRemoved(BoxInvitee token) {
        notifyInviteCollaboratorsListener();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.invite_collaborator_role) {
            CollaborationRolesDialog rolesDialog = CollaborationRolesDialog.newInstance(mRoles, mSelectedRole, getString(R.string.box_sharesdk_access), false, false, null);
            rolesDialog.setOnRoleSelectedListener(this);
            rolesDialog.show(getFragmentManager(), TAG);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUi();
    }

    @Override
    public void onRoleSelected(CollaborationRolesDialog rolesDialog) {
        setSelectedRole(rolesDialog.getSelectedRole());
    }

    /**
     * Executes the request to retrieve the available roles for the folder
     */
    private void fetchRoles() {
        if (getFolder() == null || SdkUtils.isBlank(getFolder().getId())) {
            return;
        }

        showSpinner();
        mController.fetchRoles(getFolder()).addOnCompletedListener(mRolesListener);
    }

    private BoxFutureTask.OnCompletedListener<BoxFolder> mRolesListener =
            new BoxFutureTask.OnCompletedListener<BoxFolder>() {
                @Override
                public void onCompleted(final BoxResponse<BoxFolder> response) {
                    dismissSpinner();
                    final Activity activity = getActivity();
                    if (activity == null) {
                        return;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.isSuccess() && getFolder() != null) {
                                BoxFolder folder = response.getResult();
                                mRoles = folder.getAllowedInviteeRoles();
                                if (mSelectedRole != null) {
                                    setSelectedRole(mSelectedRole);
                                } else {
                                    BoxCollaboration.Role selectedRole = mRoles != null && mRoles.size() > 0 ? mRoles.get(0) : null;
                                    setSelectedRole(selectedRole);
                                }
                                mShareItem = folder;
                            } else {
                                BoxLogUtils.e(CollaborationsFragment.class.getName(), "Fetch roles request failed",
                                        response.getException());
                                mController.showToast(getActivity(), getString(R.string.box_sharesdk_network_error));
                            }
                        }
                    });
                }
            };

    /**
     * Executes the request to retrieve the invitees that can be auto-completed
     */
    private void fetchInvitees() {
        mController.getInvitees(getFolder(), mFilterTerm).addOnCompletedListener(mGetInviteesListener);
    }

    /**
     * Executes the request to add collaborations to the folder
     */
    public void addCollaborations() {
        List<BoxInvitee> invitees = mAutoComplete.getObjects();
        String[] emailParts = new String[invitees.size()];
        for (int i = 0; i < invitees.size(); i++) {
            emailParts[i] = invitees.get(i).getEmail();
        }

        showSpinner();
        mController.addCollaborations(getFolder(), mSelectedRole, emailParts).addOnCompletedListener(mAddCollaborationsListener);
    }

    private BoxFutureTask.OnCompletedListener<BoxIteratorInvitees> mGetInviteesListener =
            new BoxFutureTask.OnCompletedListener<BoxIteratorInvitees>() {
                @Override
                public void onCompleted(final BoxResponse<BoxIteratorInvitees> response) {
                    final Activity activity = getActivity();
                    if (activity == null) {
                        return;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.isSuccess()) {
                                final BoxIteratorInvitees invitees = response.getResult();
                                mAdapter.setInvitees(invitees);
                            } else {
                                BoxLogUtils.e(InviteCollaboratorsFragment.class.getName(), "get invitees request failed",
                                        response.getException());

                                if (response.getException() instanceof BoxException) {
                                    BoxException boxException = (BoxException) response.getException();
                                    int responseCode = boxException.getResponseCode();

                                    if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                                        mController.showToast(getActivity(), R.string.box_sharesdk_insufficient_permissions);
                                        return;
                                    } else if (boxException.getErrorType() == BoxException.ErrorType.NETWORK_ERROR) {
                                        mController.showToast(getActivity(), getString(R.string.box_sharesdk_network_error) + responseCode);
                                    }

                                }


                            }
                        }
                    });
                }
            };

    private BoxFutureTask.OnCompletedListener<BoxResponseBatch> mAddCollaborationsListener =
            new BoxFutureTask.OnCompletedListener<BoxResponseBatch>() {
                @Override
                public void onCompleted(final BoxResponse<BoxResponseBatch> response) {
                    dismissSpinner();
                    final Activity activity = getActivity();
                    if (activity == null) {
                        return;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleCollaboratorsInvited(response.getResult());
                        }
                    });
                }
            };

    /**
     * Handles the batch response of adding collaborations to the folder by showing error messages when needed
     * and finishing the activity afterwards
     *
     * @param responses the add collaborations batch response
     */
    private void handleCollaboratorsInvited(BoxResponseBatch responses) {
        int alreadyAddedCount = 0;
        boolean didRequestFail = false;
        String name = "";
        for (BoxResponse<BoxCollaboration> r : responses.getResponses()) {
            if (!r.isSuccess()) {
                if (r.getException() instanceof BoxException && ((BoxException) r.getException()).getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                    String code = ((BoxException) r.getException()).getAsBoxError().getCode();
                    if (!SdkUtils.isBlank(code) && code.equals(BoxRequestsShare.AddCollaboration.ERROR_CODE_USER_ALREADY_COLLABORATOR)) {
                        alreadyAddedCount++;
                        BoxUser user = (BoxUser) ((BoxRequestsShare.AddCollaboration) r.getRequest()).getAccessibleBy();
                        name = user == null ? "" : user.getLogin();
                    }
                }
                didRequestFail = true;
            }
        }

        String msg;
        if (didRequestFail) {
            if (alreadyAddedCount == 1) {
                msg = String.format(getString(R.string.box_sharesdk_has_already_been_invited), name);
            } else if (alreadyAddedCount > 1) {
                msg = String.format(getString(R.string.box_sharesdk_num_has_already_been_invited), Integer.toString(alreadyAddedCount));
            } else {
                msg = getString(R.string.box_sharesdk_unable_to_invite);
            }
        } else {
            if (responses.getResponses().size() == 1) {
                BoxCollaboration collaboration = (BoxCollaboration) responses.getResponses().get(0).getResult();
                if (collaboration.getAccessibleBy() == null) {
                    msg = getString(R.string.box_sharesdk_collaborators_invited);
                } else {
                    String login = ((BoxUser)(collaboration).getAccessibleBy()).getLogin();
                    msg = String.format(getString(R.string.box_sharesdk_collaborator_invited), login);
                }

            } else {
                msg = getString(R.string.box_sharesdk_collaborators_invited);
            }
        }
        mController.showToast(getActivity(), msg);
        if (responses.getResponses().size() == alreadyAddedCount) {
            getActivity().setResult(Activity.RESULT_CANCELED);
        } else {
            getActivity().setResult(Activity.RESULT_OK);
        }
        getActivity().finish();
    }

    /**
     * Sets the selected role in the UI
     *
     * @param role the collaboration role to select
     */
    private void setSelectedRole(BoxCollaboration.Role role) {
        mSelectedRole = role;
        mRoleButton.setText(createTitledSpannable(getString(R.string.box_sharesdk_access), CollaborationUtils.getRoleName(getActivity(), role)));
    }

    protected BoxFolder getFolder() {
        return (BoxFolder)mShareItem;
    }

    public static InviteCollaboratorsFragment newInstance(BoxFolder folder) {
        return newInstance(folder, true);
    }

    public static InviteCollaboratorsFragment newInstance(BoxFolder folder, boolean useContactsProvider) {
        Bundle args = BoxFragment.getBundle(folder);
        InviteCollaboratorsFragment fragment = new InviteCollaboratorsFragment();
        args.putBoolean(EXTRA_USE_CONTACTS_PROVIDER, useContactsProvider);
        fragment.setArguments(args);
        return fragment;
    }
}
