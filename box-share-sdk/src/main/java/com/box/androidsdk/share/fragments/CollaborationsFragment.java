package com.box.androidsdk.share.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.box.androidsdk.content.BoxApiCollaboration;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborator;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.models.BoxVoid;
import com.box.androidsdk.content.requests.BoxRequest;
import com.box.androidsdk.content.requests.BoxRequestsShare;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.utils.BoxLogUtils;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.adapters.CollaboratorsAdapter;
import com.box.androidsdk.share.api.BoxShareController;

import java.util.ArrayList;

public class CollaborationsFragment extends Fragment implements AdapterView.OnItemClickListener, CollaborationRolesDialog.OnRoleSelectedListener {

    protected static final String TAG = CollaborationsFragment.class.getName();
    public static final String BOX_FOLDER = "CollaborationsFragment.BoxFolder";
    public static final String BOX_SESSION_USER_ID = "CommentsFragment.BoxSession.UserId";
    protected BoxFolder mFolder;
    protected ListView mCollaboratorsListView;
    protected TextView mNoCollaboratorsText;
    protected CollaboratorsAdapter mCollaboratorsAdapter;
    protected ArrayList<BoxCollaboration.Role> mRoles = null;
    private BoxIteratorCollaborations mCollaborationsList;

    private BoxSession mSession;
    protected BoxShareController mController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle args = getArguments();
        mFolder = (BoxFolder) args.getSerializable(BOX_FOLDER);
        mSession = new BoxSession(getActivity(), args.getString(BOX_SESSION_USER_ID));
        mController = new BoxShareController(new BoxApiFolder(mSession), new BoxApiCollaboration(mSession));
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

        fetchCollaborations();

        // Get serialized roles or fetch them if they are not available
        if (mFolder.getAllowedInviteeRoles() == null) {
            fetchRoles();
        } else {
            mRoles = mFolder.getAllowedInviteeRoles();
        }

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CollaboratorsAdapter.ViewHolder holder = (CollaboratorsAdapter.ViewHolder) view.getTag();
        if (holder != null && holder.collaboration != null && holder.collaboration.getItem() != null) {
            BoxCollaboration.Role[] rolesArr = mRoles.toArray(new BoxCollaboration.Role[mRoles.size()]);
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
        BoxRequest req;
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

    /**
     * Executes the request to retrieve collaborations for the folder
     */
    private void fetchCollaborations() {
        if (mFolder == null || SdkUtils.isBlank(mFolder.getId())) {
            Toast.makeText(getActivity(), getString(R.string.box_sharesdk_cannot_view_collaborations), Toast.LENGTH_LONG).show();
            return;
        }

        mController.fetchCollaborations(mFolder, mCollaborationsListener);
    }

    /**
     * Executes the request to retrieve the available roles for the folder
     */
    private void fetchRoles() {
        if (mFolder == null || SdkUtils.isBlank(mFolder.getId())) {
            return;
        }

        mController.fetchRoles(mFolder, mRolesListener);
    }

    private void updateUi(final BoxIteratorCollaborations collabs){
        if (collabs != null && collabs.size() > 0) {
            hideView(mNoCollaboratorsText);
            showView(mCollaboratorsListView);
            mCollaboratorsAdapter.setItems(collabs);
        } else {
            hideView(mCollaboratorsListView);
            showView(mNoCollaboratorsText);
        }
    }

    /**
     * Helper method to hide a view ie. set the visibility to View.GONE
     *
     * @param view the view that should be hidden
     */
    protected void hideView(View view){
        view.setVisibility(View.GONE);
    }

    /**
     * Helper method to show a view ie. set the visibility to View.VISIBLE
     *
     * @param view the view that should be shown
     */
    protected void showView(View view){
        view.setVisibility(View.VISIBLE);
    }

    private BoxFutureTask.OnCompletedListener<BoxIteratorCollaborations> mCollaborationsListener =
        new BoxFutureTask.OnCompletedListener<BoxIteratorCollaborations>() {
            @Override
            public void onCompleted(final BoxResponse<BoxIteratorCollaborations> response) {
                final Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccess() && mFolder != null) {
                            BoxIteratorCollaborations collabs = response.getResult();
                            mCollaborationsList = collabs;
                            updateUi(collabs);
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
                        if (response.isSuccess() && mFolder != null) {
                            BoxFolder folder = response.getResult();
                            mRoles = folder.getAllowedInviteeRoles();
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
                        if (response.isSuccess() && mFolder != null) {
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
                        if (response.isSuccess() && mFolder != null) {
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
        Bundle args = new Bundle();
        args.putSerializable(BOX_FOLDER, folder);
        args.putString(BOX_SESSION_USER_ID, sessionUserId);
        CollaborationsFragment fragment = new CollaborationsFragment();
        fragment.setArguments(args);
        return fragment;
    }


}
