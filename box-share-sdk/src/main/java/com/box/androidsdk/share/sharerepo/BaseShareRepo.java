package com.box.androidsdk.share.sharerepo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;

/**
 * The Base ShareRepo class that will be used by ViewModels to make calls to the backend and should be extended by other ShareRepos
 */
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

    /**
     * Get mInvitees which is a list of invitees based on your filter
     * @return mInvitees
     */
    public LiveData<BoxResponse<BoxIteratorInvitees>> getInvitees() {
        return mInvitees;
    }

    /**
     * Get mFetchRoleItem which is the item which will hold allowed roles for new invitees
     * @return mFetchRoleItem
     */
    public LiveData<BoxResponse<BoxCollaborationItem>> getFetchRoleItem() {
        return mFetchRoleItem;
    }

    /**
     * Get mInviteCollabBatch which is a batch of responses for each collaborator invited.
     * @return mInviteColalbBatch
     */
    public LiveData<BoxResponse<BoxResponseBatch>> getInviteCollabBatch() {
        return mInviteCollabBatch;
    }
}
