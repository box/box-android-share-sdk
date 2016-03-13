package com.box.androidsdk.share.fragments;

import android.Manifest;
import android.app.Activity;
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
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.models.BoxUser;
import com.box.androidsdk.content.requests.BoxRequestBatch;
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

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InviteCollaboratorsFragment extends BoxFragment implements View.OnClickListener, CollaborationRolesDialog.OnRoleSelectedListener {

    private static final Integer MY_PERMISSIONS_REQUEST_READ_CONTACTS = 32;
    protected static final String TAG = InviteCollaboratorsFragment.class.getName();
    public static final String EXTRA_ACCESS_TOKEN = "InviteCollaboratorsFragment.ExtraAccessToken";
    private Button mRoleButton;
    private ChipCollaborationView mAutoComplete;
    private InviteeAdapter mAdapter;
    private BoxCollaboration.Role mSelectedRole;
    private ArrayList<BoxCollaboration.Role> mRoles;
    private String mAccessToken;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_collaborators, container, false);

        mRoleButton = (Button) view.findViewById(R.id.invite_collaborator_role);
        mRoleButton.setOnClickListener(this);
        mAutoComplete = (ChipCollaborationView) view.findViewById(R.id.invite_collaborator_autocomplete);
        mAutoComplete.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        mAdapter = new InviteeAdapter(getActivity());
        mAutoComplete.setAdapter(mAdapter);

        if (getArguments() != null) {
            Bundle args = getArguments();
            mAccessToken = args.getString(EXTRA_ACCESS_TOKEN);
        }

        // Get serialized roles or fetch them if they are not available
        if (getFolder() != null && getFolder().getAllowedInviteeRoles() != null) {
            mRoles = getFolder().getAllowedInviteeRoles();
            BoxCollaboration.Role selectedRole = mRoles.get(0);
            setSelectedRole(selectedRole);
        } else {
            fetchRoles();
        }

        fetchInvitees();

        requestPermissionsIfNecessary();
        return view;
    }

    private void requestPermissionsIfNecessary() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.invite_collaborator_role) {
            CollaborationRolesDialog rolesDialog = CollaborationRolesDialog.newInstance(mRoles, mSelectedRole, getString(R.string.box_sharesdk_access), false, null);
            rolesDialog.show(getFragmentManager(), TAG);
        }
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
        showSpinner();
        mController.getInvitees(getFolder()).addOnCompletedListener(mGetInviteesListener);
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
                    dismissSpinner();
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
                                mController.showToast(getActivity(), getString(R.string.box_sharesdk_network_error));
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
                msg = String.format(Locale.ENGLISH, getString(R.string.box_sharesdk_has_already_been_invited), name);
            } else if (alreadyAddedCount > 1) {
                msg = String.format(Locale.ENGLISH, getString(R.string.box_sharesdk_num_has_already_been_invited), alreadyAddedCount);
            } else {
                msg = getString(R.string.box_sharesdk_network_error);
            }
        } else {
            msg = getString(R.string.box_sharesdk_collaborators_invited);
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

    public static InviteCollaboratorsFragment newInstance(BoxFolder folder, BoxSession session) {
        Bundle args = BoxFragment.getBundle(folder);
        args.putString(EXTRA_ACCESS_TOKEN, session.getAuthInfo().accessToken());
        InviteCollaboratorsFragment fragment = new InviteCollaboratorsFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
