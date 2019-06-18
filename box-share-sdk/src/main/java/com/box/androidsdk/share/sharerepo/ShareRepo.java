package com.box.androidsdk.share.sharerepo;

import androidx.lifecycle.MutableLiveData;

import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.share.api.ShareController;

/**
 * A repo class that will be used by ViewModel to make calls to the backend
 */
public class ShareRepo extends BaseShareRepo {

    public ShareRepo(ShareController controller) {
        super(controller);
    }


    @Override
    /**
     * Get a list of invitees based on the filter term on an item and update the corresponding LiveData
     * @param boxCollaborationItem the item to get invitees on
     * @param filter the filter term
     */
    public void getInviteesApi(BoxCollaborationItem boxCollaborationItem, String filter) {
        handleTaskAndPostValue(mController.getInvitees(boxCollaborationItem,filter), mInvitees);
    }

    /**
     * Generic helper method to post the response from the task to a LiveData
     * @param task the task to wait for respond from
     * @param source the LiveData to update with the response
     */
    private void handleTaskAndPostValue(BoxFutureTask task, final MutableLiveData source) {
        task.addOnCompletedListener(new BoxFutureTask.OnCompletedListener() {
            @Override
            public void onCompleted(BoxResponse response) {
                source.postValue(response);
            }
        });
    }

    @Override
    /**
     * Get an item with allowed roles for the invitees and update the corresponding LiveData
     * @param boxCollaborationItem the item to fetch roles on
     */
    public void fetchRolesApi(BoxCollaborationItem boxCollaborationItem) {
        handleTaskAndPostValue(mController.fetchRoles(boxCollaborationItem), mFetchRoleItem);
    }

    @Override
    /**
     * Add collaborators to an item based on the selectedRole and emails.
     * @param boxCollaborationItem the item to add collaborators on
     * @param selectedRole the role for the new collaborators
     * @param emails the list of collaborators to invite
     */
    public void addCollabsApi(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails) {
        handleTaskAndPostValue(mController.addCollaborations(boxCollaborationItem, selectedRole, emails), mInviteCollabBatch);
    }
}