package com.box.androidsdk.share.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborator;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxVoid;
import com.box.androidsdk.content.requests.BoxRequestsShare;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.utils.BoxLogUtils;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.adapters.CollaboratorsAdapter;

import java.util.ArrayList;

public class CollaborationsFragment extends BoxFragment implements AdapterView.OnItemClickListener, CollaborationRolesDialog.OnRoleSelectedListener {

    public static final String EXTRA_COLLABORATIONS = "CollaborationsFragment.ExtraCollaborations";
    protected static final String TAG = CollaborationsFragment.class.getName();
    protected ListView mCollaboratorsListView;
    protected TextView mNoCollaboratorsText;
    protected CollaboratorsAdapter mCollaboratorsAdapter;
    protected BoxIteratorCollaborations mCollaborations;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collaborations, container, false);

        mCollaboratorsListView = (ListView) view.findViewById(R.id.collaboratorsList);
        mCollaboratorsListView.setDivider(null);
        mCollaboratorsAdapter = new CollaboratorsAdapter(getActivity());
        mCollaboratorsListView.setAdapter(mCollaboratorsAdapter);
        mCollaboratorsListView.setOnItemClickListener(this);
        mNoCollaboratorsText = (TextView) view.findViewById(R.id.no_collaborators_text);

        if (savedInstanceState == null) {
            fetchCollaborations();
        }else {
            mCollaborations = (BoxIteratorCollaborations)savedInstanceState.getSerializable(EXTRA_COLLABORATIONS);
            updateUi();
        }

        // Get serialized roles or fetch them if they are not available
        if (getFolder().getAllowedInviteeRoles() == null) {
            fetchRoles();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(EXTRA_COLLABORATIONS, mCollaborations);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CollaboratorsAdapter.ViewHolder holder = (CollaboratorsAdapter.ViewHolder) view.getTag();
        if (holder != null && holder.collaboration != null && holder.collaboration.getItem() != null) {
            BoxCollaboration.Role[] rolesArr = getRoles();
            BoxCollaborator collaborator = holder.collaboration.getAccessibleBy();
            BoxCollaboration.Role role = holder.collaboration.getRole();
            String name = collaborator == null ? getString(R.string.box_sharesdk_another_person) : collaborator.getName();
            CollaborationRolesDialog rolesDialog = CollaborationRolesDialog.newInstance(rolesArr, role, name, true, holder.collaboration);
            rolesDialog.setOnRoleSelectedListener(this);
            rolesDialog.show(getFragmentManager(), TAG);
        }
    }

    @Override
    public void onRoleSelected(CollaborationRolesDialog rolesDialog) {
        BoxCollaboration collaboration = (BoxCollaboration) rolesDialog.getSerializableExtra();
        if (collaboration == null)
            return;

        if (rolesDialog.getIsRemoveCollaborationSelected()) {
            mController.deleteCollaboration(collaboration, mDeleteCollaborationListener);
        } else {
            BoxCollaboration.Role selectedRole = rolesDialog.getSelectedRole();
            if (selectedRole == null || selectedRole == collaboration.getRole())
                return;
            mController.updateCollaboration(collaboration, selectedRole, mUpdateCollaborationListener);
        }
    }

    public BoxFolder getFolder() {
        return (BoxFolder) mShareItem;
    }

    /**
     * Executes the request to retrieve collaborations for the folder
     */
    public void fetchCollaborations() {
        if (getFolder() == null || SdkUtils.isBlank(getFolder().getId())) {
            Toast.makeText(getActivity(), getString(R.string.box_sharesdk_cannot_view_collaborations), Toast.LENGTH_LONG).show();
            return;
        }

        showSpinner();
        mController.fetchCollaborations(getFolder(), mCollaborationsListener);
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

    private void updateUi(){
        if (mCollaborations != null && mCollaborations.size() > 0) {
            hideView(mNoCollaboratorsText);
            showView(mCollaboratorsListView);
            mCollaboratorsAdapter.setItems(mCollaborations);
        } else {
            hideView(mCollaboratorsListView);
            showView(mNoCollaboratorsText);
        }
    }

    public BoxCollaboration.Role[] getRoles() {
        if (getFolder().getAllowedInviteeRoles() != null) {
            return getFolder().getAllowedInviteeRoles().toArray(new BoxCollaboration.Role[getFolder().getAllowedInviteeRoles().size()]);
        }
        return null;
    }

    private BoxFutureTask.OnCompletedListener<BoxIteratorCollaborations> mCollaborationsListener =
        new BoxFutureTask.OnCompletedListener<BoxIteratorCollaborations>() {
            @Override
            public void onCompleted(final BoxResponse<BoxIteratorCollaborations> response) {
                dismissSpinner();
                final Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccess() && getFolder() != null) {
                            mCollaborations = response.getResult();
                            updateUi();
                        } else {
                            BoxLogUtils.e(CollaborationsFragment.class.getName(), "Fetch Collaborators request failed",
                                    response.getException());
                            Toast.makeText(getActivity(), getString(R.string.box_sharesdk_network_error), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        };

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

    private BoxFutureTask.OnCompletedListener<BoxVoid> mDeleteCollaborationListener =
        new BoxFutureTask.OnCompletedListener<BoxVoid>() {
            @Override
            public void onCompleted(final BoxResponse<BoxVoid> response) {
                final Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccess() && getFolder() != null) {
                            BoxRequestsShare.DeleteCollaboration req = (BoxRequestsShare.DeleteCollaboration) response.getRequest();
                            mCollaboratorsAdapter.delete(req.getId());
                            if (mCollaboratorsAdapter.getCount() == 0) {
                                hideView(mCollaboratorsListView);
                                showView(mNoCollaboratorsText);
                            };
                        } else {
                            BoxLogUtils.e(CollaborationsFragment.class.getName(), "Delete Collaborator request failed",
                                    response.getException());
                            Toast.makeText(getActivity(), getString(R.string.box_sharesdk_network_error), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        };

    private BoxFutureTask.OnCompletedListener<BoxCollaboration> mUpdateCollaborationListener =
        new BoxFutureTask.OnCompletedListener<BoxCollaboration>() {
            @Override
            public void onCompleted(final BoxResponse<BoxCollaboration> response) {
                final Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccess() && getFolder() != null) {
                            mCollaboratorsAdapter.update(response.getResult());
                        } else {
                            BoxLogUtils.e(CollaborationsFragment.class.getName(), "Update Collaborator request failed",
                                    response.getException());
                            Toast.makeText(getActivity(), getString(R.string.box_sharesdk_network_error), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        };

    public static CollaborationsFragment newInstance(BoxFolder folder, String sessionUserId) {
        Bundle args = BoxFragment.getBundle(folder, sessionUserId);
        CollaborationsFragment fragment = new CollaborationsFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
