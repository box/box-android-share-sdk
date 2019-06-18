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
    protected ShareController mController;

    protected final MutableLiveData<BoxResponse<BoxIteratorInvitees>> mInvitees = new MutableLiveData<>();
    protected final MutableLiveData<BoxResponse<BoxCollaborationItem>> mFetchRoleItem = new MutableLiveData<>();
    protected final MutableLiveData<BoxResponse<BoxResponseBatch>> mInviteCollabBatch = new MutableLiveData<>();

    public BaseShareRepo(ShareController controller) {
        this.mController = controller;
    }

    /**
     * Returns a LiveData<BoxResponse<BoxIteratorInvitees>> that will be observed by ViewModel to react to its changes
     * @param boxCollaborationItem the item to get a list of invitees on
     * @param filter the filter term
     * @return a LiveData<BoxResponse<BoxIteratorInvitees>> object that holds a reponse with a list of invitees filtered based on the filter term
     */
    public abstract void getInviteesApi(BoxCollaborationItem boxCollaborationItem, String filter);

    /**
     * Returns a LiveData<BoxResponse<BoxCollaborationItem>> that will be observed by ViewModel to react to its changes
     * @param boxCollaborationItem the item to fetch roles on
     * @return a LiveData<BoxResponse<BoxCollaborationItem>> object that holds a response with a list of collaboration roles applicable.
     */
    public abstract void fetchRolesApi(BoxCollaborationItem boxCollaborationItem);

    /**
     * Returns a LiveData<BoxResponse<BoxResponseBatch>> that will be observed by ViewModel to react to its changes
     * @param boxCollaborationItem the item to add collaborators on
     * @param selectedRole the collaboration role selected for the new collaborators
     * @param emails the list of collaborators that will be invited
     * @return a LiveData<BoxResponse<BoxResponseBatch>> object that holds a response with a list of response for each collaborator.
     */
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
     * Get mInviteCollabBatch which is the item which will hold a batch of BoxResponse for each collaborator invited.
     * @return mInviteCollabBatch
     */
    public LiveData<BoxResponse<BoxResponseBatch>> getInviteCollabBatch() {
        return mInviteCollabBatch;
    }
}
