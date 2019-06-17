package com.box.androidsdk.share.sharerepo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;
import com.box.androidsdk.share.vm.DataWrapper;
import com.box.androidsdk.share.vm.InviteCollaboratorsDataWrapper;

public abstract class BaseShareRepo {
    protected  ShareController mController;

    protected final MutableLiveData<BoxResponse<BoxIteratorInvitees>> mInvitees = new MutableLiveData<BoxResponse<BoxIteratorInvitees>>();
    protected final MutableLiveData<BoxResponse<BoxCollaborationItem>> mFetchRoleItem = new MutableLiveData<BoxResponse<BoxCollaborationItem>>();
    protected final MutableLiveData<BoxResponse<BoxResponseBatch>> mInviteCollabBatch = new MutableLiveData<BoxResponse<BoxResponseBatch>>();
    public BaseShareRepo(ShareController controller) {
        this.mController = controller;
    }
    public abstract void getInviteesApi(BoxCollaborationItem boxCollaborationItem, String filter);
    public abstract void fetchRolesApi(BoxCollaborationItem boxCollaborationItem);
    public abstract void addCollabsApi(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails);

    public MutableLiveData<BoxResponse<BoxIteratorInvitees>> getmInvitees() {
        return mInvitees;
    }

    public MutableLiveData<BoxResponse<BoxCollaborationItem>> getmFetchRoleItem() {
        return mFetchRoleItem;
    }

    public MutableLiveData<BoxResponse<BoxResponseBatch>> getmInviteCollabBatch() {
        return mInviteCollabBatch;
    }
}
