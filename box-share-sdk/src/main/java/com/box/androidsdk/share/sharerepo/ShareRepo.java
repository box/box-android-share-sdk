package com.box.androidsdk.share.sharerepo;

import androidx.lifecycle.MutableLiveData;

import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.share.api.ShareController;

/**
 * A repo class that will be used by ViewModel to make calls to the backend.
 */
public class ShareRepo extends BaseShareRepo {



    public ShareRepo(ShareController controller) {
        super(controller);
    }

    /**
     * Returns a LiveData<BoxResponse<BoxIteratorInvitees>> that will be observed by ViewModel to react to its changes
     * @param boxCollaborationItem the item to get a list of invitees on
     * @param filter the filter term
     * @return a LiveData<BoxResponse<BoxIteratorInvitees>> object that holds a reponse with a list of invitees filtered based on the filter term
     */
    @Override
    public void getInviteesApi(BoxCollaborationItem boxCollaborationItem, String filter) {
        handleTaskAndPostValue(mController.getInvitees(boxCollaborationItem,filter), mInvitees);
    }
    private void handleTaskAndPostValue(BoxFutureTask task, final MutableLiveData source) {
        task.addOnCompletedListener(new BoxFutureTask.OnCompletedListener() {
            @Override
            public void onCompleted(BoxResponse response) {
                source.postValue(response);
            }
        });
    }

    /**
     * Returns a LiveData<BoxResponse<BoxCollaborationItem>> that will be observed by ViewModel to react to its changes
     * @param boxCollaborationItem the item to fetch roles on
     * @return a LiveData<BoxResponse<BoxCollaborationItem>> object that holds a response with a list of collaboration roles applicable.
     */
    @Override
    public void fetchRolesApi(BoxCollaborationItem boxCollaborationItem) {
        handleTaskAndPostValue(mController.fetchRoles(boxCollaborationItem), mFetchRoleItem);
    }

    /**
     * Returns a LiveData<BoxResponse<BoxResponseBatch>> that will be observed by ViewModel to react to its changes
     * @param boxCollaborationItem the item to add collaborators on
     * @param selectedRole the collaboration role selected for the new collaborators
     * @param emails the list of collaborators that will be invited
     * @return a LiveData<BoxResponse<BoxResponseBatch>> object that holds a response with a list of response for each collaborator.
     */
    @Override
    public void addCollabsApi(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails) {
        handleTaskAndPostValue(mController.addCollaborations(boxCollaborationItem, selectedRole, emails), mInviteCollabBatch);
    }
}