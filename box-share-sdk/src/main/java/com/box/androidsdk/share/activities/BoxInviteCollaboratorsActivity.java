package com.box.androidsdk.share.activities;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.box.androidsdk.content.BoxApiCollaboration;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxUser;
import com.box.androidsdk.content.requests.BoxRequestBatch;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.content.requests.BoxRequest;
import com.box.androidsdk.content.requests.BoxRequestsFolder;
import com.box.androidsdk.content.requests.BoxRequestsShare;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.internal.BoxApiInvitee;
import com.box.androidsdk.internal.BoxInviteeResponse;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.adapters.InviteeAdapter;
import com.box.androidsdk.share.fragments.CollaborationRolesDialog;
import com.box.androidsdk.share.internal.BoxListInvitees;

import java.net.HttpURLConnection;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Activity used to allow users to invite additional collaborators to the folder. Email addresses will auto complete from the phones address book
 * as well as Box's internal invitee endpoint. The intent to launch this activity can be retrieved via the static getLaunchIntent method
 */
public class BoxInviteCollaboratorsActivity extends BoxThreadPoolExecutorActivity implements View.OnClickListener, CollaborationRolesDialog.OnRoleSelectedListener {

    /**
     * Extra serializable intent parameter to specify the {@link com.box.androidsdk.content.models.BoxCollaboration.Role} that should be selected
     */
    public static final String EXTRA_SELECTED_ROLE = "extraSelectedRole";

    /**
     * Extra serializable intent parameter to specify the array of {@link com.box.androidsdk.content.models.BoxCollaboration.Role} that can be selected
     */
    public static final String EXTRA_ROLES = "extraRoles";

    /**
     * Extra serializable intent parameter to save the {@link com.box.androidsdk.share.internal.BoxListInvitees invitees} in the saved instance state bundle
     */
    protected static final String EXTRA_INVITEES = "extraInvitees";

    protected static final String TAG = BoxInviteCollaboratorsActivity.class.getName();

    private static ThreadPoolExecutor mApiExecutor;
    private static final ConcurrentLinkedQueue<BoxResponse> INVITE_COLLABORATOR_RESPONSE_QUEUE = new ConcurrentLinkedQueue<BoxResponse>();
    private BoxCollaboration.Role mSelectedRole;
    private BoxFolder mFolder;
    private BoxCollaboration.Role[] mRoles;
    private BoxSession mSession;
    private BoxListInvitees mInvitees;

    private Button mRoleButton;
    private MultiAutoCompleteTextView mAutoComplete;
    private InviteeAdapter mAdapter;

    @Override
    public ThreadPoolExecutor getApiExecutor(Application application) {
        if (mApiExecutor == null){
            mApiExecutor = BoxThreadPoolExecutorActivity.createTaskMessagingExecutor(application, getResponseQueue());
        }
        return mApiExecutor;
    }

    @Override
    public Queue<BoxResponse> getResponseQueue() {
        return INVITE_COLLABORATOR_RESPONSE_QUEUE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_collaborators);
        initToolbar();

        mRoleButton = (Button) findViewById(R.id.invite_collaborator_role);
        mRoleButton.setOnClickListener(this);
        mAutoComplete = (MultiAutoCompleteTextView) findViewById(R.id.invite_collaborator_autocomplete);
        mAutoComplete.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        mAdapter = new InviteeAdapter(this);
        mAutoComplete.setAdapter(mAdapter);

        Intent intent = getIntent();
        String userId = intent.getStringExtra(EXTRA_USER_ID);
        mSession = new BoxSession(this, userId);
        mFolder = (BoxFolder) mShareItem;

        // Get serialized roles or fetch them if they are not available
        if (mFolder != null && mFolder.getAllowedInviteeRoles() != null) {
            mRoles = mFolder.getAllowedInviteeRoles().toArray(new BoxCollaboration.Role[mFolder.getAllowedInviteeRoles().size()]);
            BoxCollaboration.Role selectedRole = (BoxCollaboration.Role) intent.getSerializableExtra(EXTRA_SELECTED_ROLE);
            setSelectedRole(selectedRole);
        } else {
            fetchRoles();
        }

        // Get serialized invitees or fetch them if they are not available
        if (savedInstanceState != null && savedInstanceState.getSerializable(EXTRA_INVITEES) != null) {
            mInvitees = (BoxListInvitees) savedInstanceState.getSerializable(EXTRA_INVITEES);
            mAdapter.setInvitees(mInvitees);
        } else {
            fetchInvitees();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(EXTRA_INVITEES, mInvitees);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void handleBoxResponse(BoxResponse response) {
        if (response.isSuccess()) {
            if (response.getRequest() instanceof  BoxRequestsFolder.GetFolderInfo) {
                BoxFolder folder = (BoxFolder) response.getResult();
                mRoles = folder.getAllowedInviteeRoles().toArray(new BoxCollaboration.Role[folder.getAllowedInviteeRoles().size()]);
                if (mSelectedRole != null) {
                    setSelectedRole(mSelectedRole);
                } else {
                    BoxCollaboration.Role selectedRole = mRoles != null && mRoles.length > 0 ? mRoles[0] : null;
                    setSelectedRole(selectedRole);
                }
            } else if (response.getRequest() instanceof BoxRequestBatch) {
                handleCollaboratorsInvited((BoxResponseBatch) response.getResult());
            }
        } else {
            Toast.makeText(this, R.string.box_sharesdk_network_error, Toast.LENGTH_LONG);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_invite_collaborators, menu);
        return true;
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
     * Gets a fully formed intent that can be used to start the activity with
     *
     * @param context context to launch the intent in
     * @param folder folder to add collaborators to
     * @param session the session to add collaborators with
     * @return the intent to launch the activity
     */
    public static Intent getLaunchIntent(Context context, BoxFolder folder, BoxSession session) {
        if (folder == null || SdkUtils.isBlank(folder.getId()))
            throw new IllegalArgumentException("A valid folder must be provided for retrieving collaborations");
        if (session == null || session.getUser() == null ||  SdkUtils.isBlank(session.getUser().getId()))
            throw new IllegalArgumentException("A valid user must be provided for retrieving collaborations");

        Intent inviteIntent = new Intent(context, BoxInviteCollaboratorsActivity.class);
        inviteIntent.putExtra(EXTRA_ITEM, folder);
        inviteIntent.putExtra(EXTRA_USER_ID, session.getUser().getId());
        return inviteIntent;
    }


    /**
     * Gets a fully formed intent that can be used to start the activity with. This overload allows the ability to avoid an extra network call
     * by providing the roles in the intent
     *
     * @param context context to launch the intent in
     * @param folder folder to add collaborators to
     * @param session the session to add collaborators with
     * @param roles array of roles
     * @param selectedRole the role that will be selected by default
     * @return the intent to launch the activity
     */
    public static Intent getLaunchIntent(Context context, BoxFolder folder, BoxSession session, BoxCollaboration.Role[] roles, BoxCollaboration.Role selectedRole) {
        Intent inviteIntent = getLaunchIntent(context, folder, session);
        inviteIntent.putExtra(EXTRA_ROLES, roles);
        inviteIntent.putExtra(EXTRA_SELECTED_ROLE, selectedRole);
        return inviteIntent;
    }

    /**
     * Sets the selected role in the UI
     *
     * @param role the collaboration role to select
     */
    private void setSelectedRole(BoxCollaboration.Role role) {
        mSelectedRole = role;
        mRoleButton.setText(createTitledSpannable(getString(R.string.box_sharesdk_access) , CollaborationUtils.getRoleName(this, role)));
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
     * Executes the request to retrieve the available roles for the folder
     */
    private void fetchRoles() {
        BoxRequestsFolder.GetFolderInfo rolesReq = new BoxApiFolder(mSession)
                .getInfoRequest(mFolder.getId())
                .setFields(BoxFolder.FIELD_ALLOWED_INVITEE_ROLES);

        executeRequest(rolesReq);
    }

    /**
     * Executes the request to retrieve the invitees that can be auto-completed
     */
    private void fetchInvitees() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                BoxInviteeResponse response = new BoxApiInvitee().getInviteesForFolder(mFolder.getId(), mSession.getAuthInfo().accessToken());

                if (response.getResponseCode() >= HttpURLConnection.HTTP_OK && response.getResponseCode() < HttpURLConnection.HTTP_MULT_CHOICE) {
                    final BoxListInvitees invitees = new BoxListInvitees();
                    invitees.createFromJson(response.getResponse());
                    mInvitees = invitees;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.setInvitees(invitees);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Executes the request to add collaborations to the folder
     */
    private void addCollaborations() {
        String emails = mAutoComplete.getText().toString();
        if (!SdkUtils.isBlank(emails)) {
            BoxRequestBatch batchRequest = new BoxRequestBatch();
            String[] emailParts = emails.split(",");
            for (int i = 0; i < emailParts.length; ++i) {
                String email = emailParts[i].trim();
                if (!SdkUtils.isBlank(email)) {
                    batchRequest.addRequest(new BoxApiCollaboration(mSession).getAddRequest(mFolder.getId(), mSelectedRole, email));
                }
            }
            executeRequest(batchRequest);
        }
    }

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
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        if (responses.getResponses().size() == alreadyAddedCount) {
            setResult(RESULT_CANCELED);
        } else {
            setResult(RESULT_OK);
        }
        finish();
    }
}
