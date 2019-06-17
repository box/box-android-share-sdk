package com.box.androidsdk.share.sharerepo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;

public abstract class BaseShareRepo {
    protected  ShareController mController;

    protected final MutableLiveData<BoxResponse<BoxIteratorInvitees>> mInvitees = new MutableLiveData<>();
    protected final MutableLiveData<BoxResponse<BoxCollaborationItem>> mFetchRoleItem = new MutableLiveData<>();
    protected final MutableLiveData<BoxResponse<BoxResponseBatch>> mInviteCollabBatch = new MutableLiveData<>();

    public BaseShareRepo(ShareController controller) {
        this.mController = controller;
    }
    public abstract void getInviteesApi(BoxCollaborationItem boxCollaborationItem, String filter);
    public abstract void fetchRolesApi(BoxCollaborationItem boxCollaborationItem);
    public abstract void addCollabsApi(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails);

    public LiveData<BoxResponse<BoxIteratorInvitees>> getInvitees() {
        return mInvitees;
    }

    public LiveData<BoxResponse<BoxCollaborationItem>> getFetchRoleItem() {
        return mFetchRoleItem;
    }

    public LiveData<BoxResponse<BoxResponseBatch>> getInviteCollabBatch() {
        return mInviteCollabBatch;
    }
}
