package com.box.androidsdk.share.sharerepo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;

public class ShareRepo implements ShareRepoInterface {

    ShareController mController;

    private final MutableLiveData<BoxResponse<BoxIteratorInvitees>> mInvitees = new MutableLiveData<BoxResponse<BoxIteratorInvitees>>();

    private final MutableLiveData<BoxResponse<BoxCollaborationItem>> mShareItem = new MutableLiveData<BoxResponse<BoxCollaborationItem>>();

    private final MutableLiveData<BoxResponse<BoxResponseBatch>> mInviteCollabBatch = new MutableLiveData<BoxResponse<BoxResponseBatch>>();

    public ShareRepo(ShareController controller) {
        this.mController = controller;
    }

    /**
     * Returns a LiveData<BoxResponse<BoxIteratorInvitees>> that will be observed by ViewModel to react to its changes
     * @param boxCollaborationItem the item to get a list of invitees on
     * @param filter the filter term
     * @return a LiveData<BoxResponse<BoxIteratorInvitees>> object that holds a reponse with a list of invitees filtered based on the filter term
     */
    @Override
    public LiveData<BoxResponse<BoxIteratorInvitees>> getInvitees(BoxCollaborationItem boxCollaborationItem, String filter) {
        handleTaskAndPostValue(mController.getInvitees(boxCollaborationItem,filter), mInvitees);
        return mInvitees;
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
     * @returna a LiveData<BoxResponse<BoxCollaborationItem>> object that holds a response with a list of collaboration roles applicable.
     */
    @Override
    public LiveData<BoxResponse<BoxCollaborationItem>> fetchRoles(BoxCollaborationItem boxCollaborationItem) {
        handleTaskAndPostValue(mController.fetchRoles(boxCollaborationItem), mShareItem);
        return mShareItem;
    }

    /**
     * Returns a LiveData<BoxResponse<BoxResponseBatch>> that will be observed by ViewModel to react to its changes
     * @param boxCollaborationItem the item to add collaborators on
     * @param selectedRole the collaboration role selected for the new collaborators
     * @param emails the list of collaborators that will be invited
     * @return a LiveData<BoxResponse<BoxResponseBatch>> object that holds a response with a list of response for each collaborator.
     */
    @Override
    public LiveData<BoxResponse<BoxResponseBatch>> addCollabs(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails) {
        handleTaskAndPostValue(mController.addCollaborations(boxCollaborationItem, selectedRole, emails), mInviteCollabBatch);
        return mInviteCollabBatch;
    }


    public MutableLiveData<BoxResponse<BoxIteratorInvitees>> getmInvitees() {
        return mInvitees;
    }

    public MutableLiveData<BoxResponse<BoxCollaborationItem>> getmShareItem() {
        return mShareItem;
    }

    public MutableLiveData<BoxResponse<BoxResponseBatch>> getmInviteCollabBatch() {
        return mInviteCollabBatch;
    }
}