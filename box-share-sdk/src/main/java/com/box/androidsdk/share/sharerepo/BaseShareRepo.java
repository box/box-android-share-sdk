package com.box.androidsdk.share.sharerepo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;

import java.util.List;

/**
 * The Base ShareRepo class that will be used by ViewModels to make calls to the backend and should be extended by other ShareRepos
 */
public abstract class BaseShareRepo {
    protected ShareController mController;

    protected final MutableLiveData<BoxResponse<BoxIteratorInvitees>> mInvitees = new MutableLiveData<>();
    protected final MutableLiveData<BoxResponse<BoxCollaborationItem>> mFetchRoleItem = new MutableLiveData<>();
    protected final MutableLiveData<BoxResponse<BoxResponseBatch>> mInviteCollabBatch = new MutableLiveData<>();

    public BaseShareRepo(ShareController controller) {
        this.mController = controller;
    }

    /**
     * Get a list of invitees based on the filter term on an item and update the corresponding LiveData.
     * @param boxCollaborationItem the item to get invitees on
     * @param filter the filter term
     */
    public abstract void getInviteesApi(BoxCollaborationItem boxCollaborationItem, String filter);

    /**
     * Get an item with allowed roles for the invitees and update the corresponding LiveData.
     * @param boxCollaborationItem the item to fetch roles on
     */
    public abstract void fetchRolesApi(BoxCollaborationItem boxCollaborationItem);

    /**
     * Add collaborators to an item based on the selectedRole and emails.
     * @param boxCollaborationItem the item to add collaborators on
     * @param selectedRole the role for the new collaborators
     * @param emails the list of collaborators to invite
     */
    public abstract void addCollabsApi(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails);

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
    public LiveData<BoxResponse<BoxCollaborationItem>> getFetchRoleItem() {
        return mFetchRoleItem;
    }

    /**
     * Returns a LiveData which holds a batch of responses for each collaborator invited.
     * @return a LiveData which holds a batch of responses for each collaborator invited
     */
    public LiveData<BoxResponse<BoxResponseBatch>> getInviteCollabBatch() {
        return mInviteCollabBatch;
    }
}
