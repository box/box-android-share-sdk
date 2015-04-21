package com.box.androidsdk.share.activities;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.box.androidsdk.content.BoxApiCollaboration;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborator;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxListCollaborations;
import com.box.androidsdk.content.requests.BoxRequest;
import com.box.androidsdk.content.requests.BoxRequestsFolder;
import com.box.androidsdk.content.requests.BoxRequestsShare;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.adapters.CollaboratorsAdapter;
import com.box.androidsdk.share.fragments.CollaborationRolesDialog;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Activity used to show and modify the collaborations of a folder. The intent to launch this activity can be retrieved via the static getLaunchIntent method
 */
public class BoxCollaborationsActivity extends BoxThreadPoolExecutorActivity implements AdapterView.OnItemClickListener, CollaborationRolesDialog.OnRoleSelectedListener {

    /**
     * Extra intent parameter to specify the folder id of the collaborations that should be retrieved
     */
    public static final String EXTRA_FOLDER_ID = "extraFolderId";

    /**
     * Extra serializable parameter to save the {@link com.box.androidsdk.content.models.BoxListCollaborations} in the saved instance state bundle
     */
    protected static final String EXTRA_COLLABORATIONS_LIST = "extraCollaborationsList";

    protected static final String TAG = BoxCollaborationsActivity.class.getName();
    protected static final int INVITE_COLLABS_REQUEST_CODE = 1;
    protected BoxFolder mFolder;
    protected ListView mCollaboratorsListView;
    protected TextView mNoCollaboratorsText;
    protected CollaboratorsAdapter mCollaboratorsAdapter;
    protected ArrayList<BoxCollaboration.Role> mRoles = null;

    private static final ConcurrentLinkedQueue<BoxResponse> COLLABORATIONS_RESPONSE_QUEUE = new ConcurrentLinkedQueue<BoxResponse>();
    private static ThreadPoolExecutor mApiExecutor;
    private BoxListCollaborations mCollaborationsList;

    @Override
    public ThreadPoolExecutor getApiExecutor(Application application) {
        if (mApiExecutor == null){
            mApiExecutor = BoxThreadPoolExecutorActivity.createTaskMessagingExecutor(application, getResponseQueue());
        }
        return mApiExecutor;
    }

    @Override
    public Queue<BoxResponse> getResponseQueue() {
        return COLLABORATIONS_RESPONSE_QUEUE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collaborations);
        initToolbar();

        if (mShareItem == null || mShareItem.getType() == null || !mShareItem.getType().equals(BoxFolder.TYPE)) {
            Toast.makeText(this, R.string.box_sharesdk_selected_item_not_expected_type, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mFolder = (BoxFolder) mShareItem;

        mCollaboratorsListView = (ListView) findViewById(R.id.collaboratorsList);
        mCollaboratorsListView.setDivider(null);
        mCollaboratorsAdapter = new CollaboratorsAdapter(this);
        mCollaboratorsListView.setAdapter(mCollaboratorsAdapter);
        mCollaboratorsListView.setOnItemClickListener(this);
        mNoCollaboratorsText = (TextView) findViewById(R.id.no_collaborators_text);

        // Get serialized collaborations or fetch them if they are not available
        if (savedInstanceState != null && savedInstanceState.getSerializable(EXTRA_COLLABORATIONS_LIST) != null){
            mCollaborationsList = (BoxListCollaborations) savedInstanceState.getSerializable(EXTRA_COLLABORATIONS_LIST);
            updateUi(mCollaborationsList);
        } else {
            fetchCollaborations();
        }

        // Get serialized roles or fetch them if they are not available
        if (mFolder.getAllowedInviteeRoles() == null) {
            fetchRoles();
        } else {
            mRoles = mFolder.getAllowedInviteeRoles();
        }
    }

    @Override
    protected void setMainItem(BoxItem boxItem) {
        mFolder = (BoxFolder) boxItem;
        super.setMainItem(boxItem);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(EXTRA_COLLABORATIONS_LIST, mCollaborationsList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_collaborate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.box_sharesdk_action_add) {
            if (mRoles != null) {
                BoxCollaboration.Role[] rolesArr = mRoles.toArray(new BoxCollaboration.Role[mRoles.size()]);
                Intent inviteCollabsIntent = BoxInviteCollaboratorsActivity.getLaunchIntent(this, mFolder, mSession, rolesArr, rolesArr[0]);
                startActivityForResult(inviteCollabsIntent, INVITE_COLLABS_REQUEST_CODE);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case INVITE_COLLABS_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    // New collaborators have been invited so we should refresh
                    fetchCollaborations();
                }
                break;
        }
    }

    private void updateUi(final BoxListCollaborations collabs){
        if (collabs != null && collabs.size() > 0) {
            hideView(mNoCollaboratorsText);
            showView(mCollaboratorsListView);
            mCollaboratorsAdapter.setItems(collabs);
        } else {
            hideView(mCollaboratorsListView);
            showView(mNoCollaboratorsText);
        }
    }

    @Override
    public void handleBoxResponse(BoxResponse response){
        if (response.isSuccess()) {
            if (response.getRequest() instanceof  BoxRequestsFolder.GetFolderInfo) {
                BoxFolder folder = (BoxFolder) response.getResult();
                mRoles = folder.getAllowedInviteeRoles();
                mShareItem = folder;
            } else if (response.getRequest() instanceof BoxRequestsFolder.GetCollaborations) {
                BoxListCollaborations collabs = (BoxListCollaborations) response.getResult();
                mCollaborationsList = collabs;
                updateUi(collabs);
            } else if (response.getRequest() instanceof BoxRequestsShare.UpdateCollaboration) {
                mCollaboratorsAdapter.update((BoxCollaboration) response.getResult());
            } else if (response.getRequest() instanceof BoxRequestsShare.DeleteCollaboration) {
                BoxRequestsShare.DeleteCollaboration req = (BoxRequestsShare.DeleteCollaboration) response.getRequest();
                mCollaboratorsAdapter.delete(req.getId());
                if (mCollaboratorsAdapter.getCount() == 0) {
                    hideView(mCollaboratorsListView);
                    showView(mNoCollaboratorsText);
                };
            }
        } else {
            int resId;
            if (response.getRequest() instanceof BoxRequestsFolder.GetFolderInfo) {
                resId = R.string.box_sharesdk_cannot_view_collaborations;
            } else {
                resId = R.string.box_sharesdk_network_error;
            }

            Toast.makeText(this, getString(resId), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Uses the ThreadPoolExecutor provided by getApiExecutor to execute the {@link com.box.androidsdk.content.requests.BoxRequest}
     *
     * @param request the request to be executed
     */
    protected void executeRequest(BoxRequest request){
        getApiExecutor(getApplication()).execute(request.setTimeOut(DEFAULT_TIMEOUT).toTask());
    }

    /**
     * Executes the request to retrieve collaborations for the folder
     */
    private void fetchCollaborations() {
        if (mFolder == null || SdkUtils.isBlank(mFolder.getId())) {
            Toast.makeText(this, getString(R.string.box_sharesdk_cannot_view_collaborations), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        BoxRequestsFolder.GetCollaborations collabsReq = new BoxApiFolder(mSession).getCollaborationsRequest(mFolder.getId());
        executeRequest(collabsReq);
    }

    /**
     * Executes the request to retrieve the available roles for the folder
     */
    private void fetchRoles() {
        BoxRequestsFolder.GetFolderInfo rolesReq = new BoxApiFolder(mSession)
                .getInfoRequest(mFolder.getId())
                .setFields(BoxFolder.FIELD_ALLOWED_INVITEE_ROLES);

        executeRequest(rolesReq);
    }

    /**
     * Updates the UI with the provided collaborations
     *
     * @param collabs list of collaborations
     */
    private void setCollaborations(BoxListCollaborations collabs) {
        if (collabs != null && collabs.size() > 0) {
            hideView(mNoCollaboratorsText);
            showView(mCollaboratorsListView);
            mCollaborationsList = collabs;
            mCollaboratorsAdapter.setItems(collabs);
        } else {
            hideView(mCollaboratorsListView);
            showView(mNoCollaboratorsText);
        }
    }

    /**
     * Gets a fully formed intent that can be used to start the activity with
     *
     * @param context context to launch the intent with
     * @param folder folder to retrieve collaborations for
     * @param session the session to view the folders collaborations with
     * @return the intent to launch the activity
     */
    public static Intent getLaunchIntent(Context context, BoxFolder folder, BoxSession session) {
        if (folder == null || SdkUtils.isBlank(folder.getId()))
            throw new IllegalArgumentException("A valid folder must be provided for retrieving collaborations");
        if (session == null || session.getUser() == null || SdkUtils.isBlank(session.getUser().getId()))
            throw new IllegalArgumentException("A valid user must be provided for retrieving collaborations");

        Intent collabIntent = new Intent(context, BoxCollaborationsActivity.class);

        collabIntent.putExtra(EXTRA_ITEM, folder);
        collabIntent.putExtra(EXTRA_FOLDER_ID, folder.getId());
        collabIntent.putExtra(EXTRA_USER_ID, session.getUser().getId());
        return collabIntent;
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
            req = new BoxApiCollaboration(mSession).getDeleteRequest(collaboration.getId());
        } else {
            BoxCollaboration.Role selectedRole = rolesDialog.getSelectedRole();
            if (selectedRole == null || selectedRole == collaboration.getRole())
                return;

            req = new BoxApiCollaboration(mSession)
                    .getUpdateRequest(collaboration.getId())
                    .setNewRole(selectedRole);
        }

        executeRequest(req);
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra(ResultInterpreter.EXTRA_BOX_ITEM, mFolder);
        data.putExtra(ResultInterpreter.EXTRA_COLLABORATIONS, mCollaborationsList);
        setResult(RESULT_OK, data);
        super.finish();
    }

    public static class ResultInterpreter extends BoxSharedLinkAccessActivity.ResultInterpreter {

        public static String EXTRA_COLLABORATIONS = "extraCollaborations";

        public ResultInterpreter(final Intent intent){
            super(intent);
        }

        public BoxListCollaborations getCollaborations(){
            return (BoxListCollaborations)mIntent.getSerializableExtra(EXTRA_COLLABORATIONS);
        }
    }

    public static ResultInterpreter createResultInterpreter(final Intent data){
        return new ResultInterpreter(data);
    }
}
