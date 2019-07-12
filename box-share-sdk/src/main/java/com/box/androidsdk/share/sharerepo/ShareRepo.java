package com.box.androidsdk.share.sharerepo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.content.models.BoxVoid;
import com.box.androidsdk.content.requests.BoxRequestsFile;
import com.box.androidsdk.content.requests.BoxRequestsFolder;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.content.views.BoxAvatarView;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.internal.models.BoxFeatures;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;

import java.util.Date;


/**
 * This is the ShareRepo that will be used by ViewModel to make calls to the backend.
 */
public class ShareRepo  {

    private ShareController mController;

    private final MutableLiveData<BoxResponse<BoxIteratorInvitees>> mInvitees = new MutableLiveData<>();
    private final MutableLiveData<BoxResponse<BoxCollaborationItem>> mRoleItem = new MutableLiveData<>();
    private final MutableLiveData<BoxResponse<BoxResponseBatch>> mInviteCollabsBatchResponse = new MutableLiveData<>();

    private final MutableLiveData<BoxResponse<BoxItem>> mItemInfo = new MutableLiveData<>();

    private final MutableLiveData<BoxResponse<BoxItem>> mSharedLinkedItem = new MutableLiveData<>(); //this item will be used for doing any shared link related operations

    private final MutableLiveData<BoxResponse<BoxIteratorCollaborations>> mCollaborations = new MutableLiveData<>();
    private final MutableLiveData<BoxResponse<BoxFeatures>> mSupportedFeatures = new MutableLiveData<>();

    private final MutableLiveData<BoxResponse<BoxVoid>> mDeleteCollaboration = new MutableLiveData<>();
    private final MutableLiveData<BoxResponse<BoxVoid>> mUpdateOwner = new MutableLiveData<>();
    private final MutableLiveData<BoxResponse<BoxCollaboration>> mUpdateCollaboration = new MutableLiveData<>();

    public ShareRepo(ShareController controller) {
        this.mController = controller;
    }

    /**
     * Get a list of invitees based on the filter term on an item and update the corresponding LiveData.
     * @param boxCollaborationItem the item to get invitees on
     * @param filter the filter term
     */
    public void fetchInviteesFromRemote(BoxCollaborationItem boxCollaborationItem, String filter) {
        handleTaskAndPostValue(mController.getInvitees(boxCollaborationItem,filter), mInvitees);
    }

    /**
     * Post the response from the task to a LiveData.
     * @param task the task to wait for respond from
     * @param source the LiveData to update with the response
     */
    private void handleTaskAndPostValue(BoxFutureTask task, final MutableLiveData source) {
        task.addOnCompletedListener(response -> source.postValue(response));
    }

    /**
     * Get an item with allowed roles for the invitees and update the corresponding LiveData.
     * @param boxCollaborationItem the item to fetch roles on
     */
    public void fetchRolesFromRemote(BoxCollaborationItem boxCollaborationItem) {
        handleTaskAndPostValue(mController.fetchRoles(boxCollaborationItem), mRoleItem);
    }

    /**
     * Fetch information about an item
     * @param boxCollaborationItem the item to fetch information on
     */
    public void fetchItemInfo(BoxItem boxCollaborationItem) {
        handleTaskAndPostValue(mController.fetchItemInfo(boxCollaborationItem), mItemInfo);
    }

    /**
     * Invite collaborators to an item based on the selectedRole and emails.
     * @param boxCollaborationItem the item to add collaborators on
     * @param selectedRole the role for the new collaborators
     * @param emails the list of collaborators to invite
     */
    public void inviteCollabs(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails) {
        handleTaskAndPostValue(mController.addCollaborations(boxCollaborationItem, selectedRole, emails), mInviteCollabsBatchResponse);
    }

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
    public LiveData<BoxResponse<BoxCollaborationItem>> getRoleItem() {
        return mRoleItem;
    }

    /**
     * Returns a LiveData which holds a batch of responses for each collaborator invited.
     * @return a LiveData which holds a batch of responses for each collaborator invited
     */
    public LiveData<BoxResponse<BoxResponseBatch>> getInviteCollabsBatchResponse() {
        return mInviteCollabsBatchResponse;
    }

    /**
     * Returns a LiveData which holds information about the item.
     * @return a LiveData which holds information about the item
     */
    public LiveData<BoxResponse<BoxItem>> getItemInfo() {
        return mItemInfo;
    }

    /**
     * Create a default shared link of an item.
     * @param boxCollaborationItem the item to get share link on
     */
    public void createDefaultSharedLink(BoxCollaborationItem boxCollaborationItem) {
        handleTaskAndPostValue(mController.createDefaultSharedLink(boxCollaborationItem), mSharedLinkedItem);
    }

    /**
     * Disable shared link of an item.
     * @param boxCollaborationItem the item to disable share link on
     */
    public void disableSharedLink(BoxCollaborationItem boxCollaborationItem) {
        handleTaskAndPostValue(mController.disableShareLink(boxCollaborationItem), mSharedLinkedItem);
    }

    /**
     * Change the download permission of a share item
     * @param boxCollaborationItem the item to change download permission on
     * @param canDownload the new download permission
     */
    public void changeDownloadPermission(BoxCollaborationItem boxCollaborationItem, boolean canDownload) {
        if (boxCollaborationItem instanceof BoxFile) {
            handleTaskAndPostValue(mController.executeRequest(BoxItem.class, ((BoxRequestsFile.UpdatedSharedFile) mController.getCreatedSharedLinkRequest(boxCollaborationItem)).setCanDownload(canDownload)), mSharedLinkedItem);
        }
        else if (boxCollaborationItem instanceof BoxFolder) {
            handleTaskAndPostValue(mController.executeRequest(BoxItem.class, ((BoxRequestsFolder.UpdateSharedFolder) mController.getCreatedSharedLinkRequest(boxCollaborationItem)).setCanDownload(canDownload)), mSharedLinkedItem);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Change expiry date of a box collaboration item.
     * @param boxCollaborationItem the item to change expiry date on
     * @param date the expiry date
     * @throws Exception
     */
    public void setExpiryDate(BoxCollaborationItem boxCollaborationItem, Date date) throws Exception {
        handleTaskAndPostValue(mController.executeRequest(BoxItem.class, mController.getCreatedSharedLinkRequest(boxCollaborationItem).setUnsharedAt(date)), mSharedLinkedItem);
    }

    /**
     * Change access level of a box collaboration item.
     * @param boxCollaborationItem the item to change access level on
     * @param access the new access level
     */
    public void changeAccessLevel(BoxCollaborationItem boxCollaborationItem, BoxSharedLink.Access access) {
        handleTaskAndPostValue(mController.executeRequest(BoxItem.class, mController.getCreatedSharedLinkRequest(boxCollaborationItem).setAccess(access)), mSharedLinkedItem);
    }

    /**
     * Change password of a box collaboration item.
     * @param boxCollaborationItem the item to change password on
     * @param password the new password
     */
    public void changePassword(BoxCollaborationItem boxCollaborationItem, String password) {
        handleTaskAndPostValue(mController.executeRequest(BoxItem.class, mController.getCreatedSharedLinkRequest(boxCollaborationItem).setPassword(password)), mSharedLinkedItem);
    }

    /**
     * Fetch collaborators for a box collaboration item.
     * @param boxCollaborationItem the item to fetch collaborations for
     */
    public void fetchCollaborations(BoxCollaborationItem boxCollaborationItem) {
        handleTaskAndPostValue(mController.fetchCollaborations(boxCollaborationItem), mCollaborations);
    }

    /**
     * Fetch supported features for the shared link.
     */
    public void fetchSupportedFeatures() {
        handleTaskAndPostValue(mController.getSupportedFeatures(), mSupportedFeatures); //this api might not be working as intended.
    }

    /**
     * Returns a LiveData which holds supported features for the shared link.
     * @return a LiveData which holds supported features for the shared link
     */
    public LiveData<BoxResponse<BoxFeatures>> getSupportFeatures() {
        return mSupportedFeatures;
    }


    /**
     * Returns a LiveData which holds information related to sharing about the item.
     * @return a LiveData which holds information related to sharing about the item
     */
    public LiveData<BoxResponse<BoxItem>> getShareLinkedItem() {
        return mSharedLinkedItem;
    }

    /**
     * Returns a LiveData which holds collaborations information about the item.
     * @returns a LiveData which holds collaborations information about the item
     */
    public LiveData<BoxResponse<BoxIteratorCollaborations>> getCollaborations() {
        return mCollaborations;
    }

    /**
     * Returns a controller for avatar.
     * @return a controller for avatar
     */
    public BoxAvatarView.AvatarController getAvatarController() {
        return mController.getAvatarController();
    }

    /**
     * Returns a LiveData which holds information about a deleted collaboration.
     * @return a LiveData which holds information about a deleted collaboration
     */
    public LiveData<BoxResponse<BoxVoid>> getDeleteCollaboration() {
        return mDeleteCollaboration;
    }

    /**
     * Returns a LiveData which holds information about a updated collaboration.
     * @return a LiveData which holds information about a updated collaboration
     */
    public LiveData<BoxResponse<BoxCollaboration>> getUpdateCollaboration() {
        return mUpdateCollaboration;
    }

    /**
     * Returns a LiveData which holds information about a updated owner collaboration.
     * @return a LiveData which holds information about a updated owner collaboration
     */
    public LiveData<BoxResponse<BoxVoid>> getUpdateOwner() {
        return mUpdateOwner;
    }

    /**
     * Delete a collaboration from a box collaboration item.
     * @param collaboration the box collaboration item which will be modified
     */
    public void deleteCollaboration(BoxCollaboration collaboration) {
        handleTaskAndPostValue(mController.deleteCollaboration(collaboration), mDeleteCollaboration);
    }
    /**
     * Update a collaboration from a box collaboration item.
     * @param collaboration the box collaboration item which will be modified
     */
    public void updateCollaboration(BoxCollaboration collaboration, BoxCollaboration.Role role) {
        handleTaskAndPostValue(mController.updateCollaboration(collaboration, role), mUpdateCollaboration);
    }
    /**
     * Update the owner of a box collaboration item.
     * @param collaboration the box collaboration item which will be modified
     */
    public void updateOwner(BoxCollaboration collaboration) {
        handleTaskAndPostValue(mController.updateOwner(collaboration), mUpdateOwner);
    }


    /**
     * Returns current user's id.
     * @return current user's id
     */
    public String getUserId() {
        return mController.getCurrentUserId();
    }


}