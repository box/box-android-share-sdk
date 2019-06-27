package com.box.androidsdk.share.sharerepo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;


/**
 * This is the ShareRepo that will be used by ViewModel to make calls to the backend.
 */
public class ShareRepo  {

    private ShareController mController;

    private final MutableLiveData<BoxResponse<BoxIteratorInvitees>> mInvitees = new MutableLiveData<>();
    private final MutableLiveData<BoxResponse<BoxCollaborationItem>> mRoleItem = new MutableLiveData<>();
    private final MutableLiveData<BoxResponse<BoxResponseBatch>> mInviteCollabsBatchResponse = new MutableLiveData<>();

    public ShareRepo(ShareController controller) {
        this.mController = controller;
    }

    /**
     * Get a list of invitees based on the filter term on an item and update the corresponding LiveData.
     * @param boxCollaborationItem the item to get invitees on
     * @param filter the filter term
     */
    public void fetchInviteesFromRemote(BoxCollaborationItem boxCollaborationItem, String filter) {
        handleTaskAndPostValue(mController.getInvitees(boxCollaborationItem,filter), mInvitees);
    }

    /**
     * Post the response from the task to a LiveData.
     * @param task the task to wait for respond from
     * @param source the LiveData to update with the response
     */
    private void handleTaskAndPostValue(BoxFutureTask task, final MutableLiveData source) {
        task.addOnCompletedListener(response -> source.postValue(response));
    }

    /**
     * Get an item with allowed roles for the invitees and update the corresponding LiveData.
     * @param boxCollaborationItem the item to fetch roles on
     */
    public void fetchRolesFromRemote(BoxCollaborationItem boxCollaborationItem) {
        handleTaskAndPostValue(mController.fetchRoles(boxCollaborationItem), mRoleItem);
    }

    /**
     * Invite collaborators to an item based on the selectedRole and emails.
     * @param boxCollaborationItem the item to add collaborators on
     * @param selectedRole the role for the new collaborators
     * @param emails the list of collaborators to invite
     */
    public void inviteCollabs(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails) {
        handleTaskAndPostValue(mController.addCollaborations(boxCollaborationItem, selectedRole, emails), mInviteCollabsBatchResponse);
    }

    /**
     * Returns a LiveData which holds a list of invitees based on your filter.
     * @return a LiveData which holds a list of invitees based on your filter
     */
    public LiveData<BoxResponse<BoxIteratorInvitees>> getInvitees() {
        return mInvitees;
    }

    /**
     * Returns a LiveData which holds the item with allowed roles for new invitees.
     * @return a LiveData which holds the item with allowed roles for new invitees
     */
    public LiveData<BoxResponse<BoxCollaborationItem>> getRoleItem() {
        return mRoleItem;
    }

    /**
     * Returns a LiveData which holds a batch of responses for each collaborator invited.
     * @return a LiveData which holds a batch of responses for each collaborator invited
     */
    public LiveData<BoxResponse<BoxResponseBatch>> getInviteCollabsBatchResponse() {
        return mInviteCollabsBatchResponse;
    }
}