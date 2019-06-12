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

    private final MutableLiveData<BoxResponse<BoxResponseBatch>> mInvitingCollabsChecker = new MutableLiveData<BoxResponse<BoxResponseBatch>>();

    public ShareRepo(ShareController controller) {
        this.mController = controller;
    }

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

    @Override
    public LiveData<BoxResponse<BoxCollaborationItem>> fetchRoles(BoxCollaborationItem boxCollaborationItem) {
        handleTaskAndPostValue(mController.fetchRoles(boxCollaborationItem), mShareItem);
        return mShareItem;
    }

    @Override
    public LiveData<BoxResponse<BoxResponseBatch>> addCollabs(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails) {
        handleTaskAndPostValue(mController.addCollaborations(boxCollaborationItem, selectedRole, emails), mInvitingCollabsChecker);
        return mInvitingCollabsChecker;
    }


}