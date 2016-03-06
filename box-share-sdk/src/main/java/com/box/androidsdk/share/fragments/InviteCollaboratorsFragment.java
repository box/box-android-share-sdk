package com.box.androidsdk.share.fragments;

import android.app.Activity;
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
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.models.BoxUser;
import com.box.androidsdk.content.requests.BoxRequestBatch;
import com.box.androidsdk.content.requests.BoxRequestsShare;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.content.utils.BoxLogUtils;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.internal.BoxApiInvitee;
import com.box.androidsdk.internal.BoxInviteeResponse;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.adapters.InviteeAdapter;
import com.box.androidsdk.share.internal.BoxListInvitees;

import java.net.HttpURLConnection;
import java.util.Locale;

public class InviteCollaboratorsFragment extends BoxFragment implements View.OnClickListener, CollaborationRolesDialog.OnRoleSelectedListener {

    protected static final String TAG = InviteCollaboratorsFragment.class.getName();
    public static final String EXTRA_ACCESS_TOKEN = "InviteCollaboratorsFragment.ExtraAccessToken";
    private Button mRoleButton;
    private MultiAutoCompleteTextView mAutoComplete;
    private InviteeAdapter mAdapter;
    private BoxCollaboration.Role mSelectedRole;
    private BoxCollaboration.Role[] mRoles;
    private String mAccessToken;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_collaborators, container, false);

        mRoleButton = (Button) view.findViewById(R.id.invite_collaborator_role);
        mRoleButton.setOnClickListener(this);
        mAutoComplete = (MultiAutoCompleteTextView) view.findViewById(R.id.invite_collaborator_autocomplete);
        mAutoComplete.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        mAdapter = new InviteeAdapter(getActivity());
        mAutoComplete.setAdapter(mAdapter);

        if (getArguments() != null) {
            Bundle args = getArguments();
            mAccessToken = args.getString(EXTRA_ACCESS_TOKEN);
        }

        // Get serialized roles or fetch them if they are not available
        if (getFolder() != null && getFolder().getAllowedInviteeRoles() != null) {
            mRoles = getFolder().getAllowedInviteeRoles().toArray(new BoxCollaboration.Role[getFolder().getAllowedInviteeRoles().size()]);
            BoxCollaboration.Role selectedRole = mRoles[0];
            setSelectedRole(selectedRole);
        } else {
            fetchRoles();
        }

        fetchInvitees();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_invite_collaborators, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.box_sharesdk_action_send) {
            addCollaborations();
        }

        return super.onOptionsItemSelected(item);
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

        mController.fetchRoles(getFolder(), mRolesListener);
    }

    private BoxFutureTask.OnCompletedListener<BoxFolder> mRolesListener =
            new BoxFutureTask.OnCompletedListener<BoxFolder>() {
                @Override
                public void onCompleted(final BoxResponse<BoxFolder> response) {
                    final Activity activity = getActivity();
                    if (activity == null) {
                        return;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.isSuccess() && getFolder() != null) {
                                BoxFolder folder = response.getResult();
                                mRoles = folder.getAllowedInviteeRoles().toArray(new BoxCollaboration.Role[folder.getAllowedInviteeRoles().size()]);
                                if (mSelectedRole != null) {
                                    setSelectedRole(mSelectedRole);
                                } else {
                                    BoxCollaboration.Role selectedRole = mRoles != null && mRoles.length > 0 ? mRoles[0] : null;
                                    setSelectedRole(selectedRole);
                                }
                                mShareItem = folder;
                            } else {
                                BoxLogUtils.e(CollaborationsFragment.class.getName(), "Fetch roles request failed",
                                        response.getException());
                                Toast.makeText(getActivity(), getString(R.string.box_sharesdk_network_error), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            };

    /**
     * Executes the request to retrieve the invitees that can be auto-completed
     */
    private void fetchInvitees() {
        if (!SdkUtils.isBlank(mAccessToken)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BoxInviteeResponse response = new BoxApiInvitee().getInviteesForFolder(getFolder().getId(), mAccessToken);

                    if (response.getResponseCode() >= HttpURLConnection.HTTP_OK && response.getResponseCode() < HttpURLConnection.HTTP_MULT_CHOICE) {
                        final BoxListInvitees invitees = new BoxListInvitees();
                        invitees.createFromJson(response.getResponse());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.setInvitees(invitees);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    /**
     * Executes the request to add collaborations to the folder
     */
    public void addCollaborations() {
        String emails = mAutoComplete.getText().toString();
        if (!SdkUtils.isBlank(emails)) {
            BoxRequestBatch batchRequest = new BoxRequestBatch();
            String[] emailParts = emails.split(",");
            mController.addCollaborations(getFolder(), mSelectedRole, emailParts, mAddCollaborationsListener);
        }
    }

    private BoxFutureTask.OnCompletedListener<BoxResponseBatch> mAddCollaborationsListener =
            new BoxFutureTask.OnCompletedListener<BoxResponseBatch>() {
                @Override
                public void onCompleted(final BoxResponse<BoxResponseBatch> response) {
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
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
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
        mRoleButton.setText(createTitledSpannable(getString(R.string.box_sharesdk_access) , CollaborationUtils.getRoleName(getActivity(), role)));
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
