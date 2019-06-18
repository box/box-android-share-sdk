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

    @Override
    public void fetchRolesApi(BoxCollaborationItem boxCollaborationItem) {
        handleTaskAndPostValue(mController.fetchRoles(boxCollaborationItem), mFetchRoleItem);
    }


    @Override
    public void addCollabsApi(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails) {
        handleTaskAndPostValue(mController.addCollaborations(boxCollaborationItem, selectedRole, emails), mInviteCollabBatch);
    }
}