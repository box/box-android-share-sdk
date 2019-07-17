package com.box.androidsdk.share.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxVoid;
import com.box.androidsdk.content.requests.BoxRequest;
import com.box.androidsdk.share.sharerepo.ShareRepo;
import com.box.androidsdk.share.utils.ShareSDKTransformer;

public class CollaborationsShareVM extends BaseShareVM{
    private final LiveData<PresenterData<BoxRequest>> mDeleteCollaboration;
    private final LiveData<PresenterData<BoxVoid>> mUpdateOwner;
    private final LiveData<PresenterData<BoxCollaboration>> mUpdateCollaboration;
    private final LiveData<PresenterData<BoxCollaborationItem>> mRoleItem;
    private final LiveData<PresenterData<BoxIteratorCollaborations>> mCollaborations;
    private boolean mOwnerUpdated;

    public CollaborationsShareVM(ShareRepo shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
        ShareSDKTransformer transformer = new ShareSDKTransformer();
        mCollaborations = Transformations.map(shareRepo.getCollaborations(), transformer::getCollaborationsPresenterData);
        mDeleteCollaboration = Transformations.map(shareRepo.getDeleteCollaboration(), transformer::getDeleteCollaborationPresenterData);
        mUpdateOwner = Transformations.map(shareRepo.getUpdateOwner(), transformer::getUpdateOwnerPresenterData);
        mUpdateCollaboration = Transformations.map(shareRepo.getUpdateCollaboration(), transformer::getUpdateCollaborationPresenterData);
        mRoleItem = Transformations.map(shareRepo.getRoleItem(),  transformer::getFetchRolesItemPresenterData);
        mOwnerUpdated = false;
    }

    /**
     * Make a backend call through share repo to delete a collaboration.
     * @param collaboration the collaboration that will be deleted
     */
    public void deleteCollaboration(BoxCollaboration collaboration) {
        mShareRepo.deleteCollaboration(collaboration);
    }

    public void updateCollaboration(BoxCollaboration collaboration, BoxCollaboration.Role role) {
        mShareRepo.updateCollaboration(collaboration, role);
    }

    public void updateOwner(BoxCollaboration collaboration) {
        mShareRepo.updateOwner(collaboration);
    }

    public void fetchCollaborations(BoxCollaborationItem item) {
        mShareRepo.fetchCollaborations(item);
    }

    public LiveData<PresenterData<BoxRequest>> getDeleteCollaboration() {
        return mDeleteCollaboration;
    }

    public LiveData<PresenterData<BoxVoid>> getUpdateOwner() {
        return mUpdateOwner;
    }

    public LiveData<PresenterData<BoxCollaboration>> getUpdateCollaboration() {
        return mUpdateCollaboration;
    }

    public LiveData<PresenterData<BoxIteratorCollaborations>> getCollaborations() {
        return mCollaborations;
    }

    public boolean isOwnerUpdated() {
        return mOwnerUpdated;
    }

    public void setOwnerUpdated(boolean ownerUpdated) {
        this.mOwnerUpdated = ownerUpdated;
    }

    /**
     * Makes a backend call through share repo for fetching roles.
     * @param item the item to fetch roles on
     */
    public void fetchRoles(BoxCollaborationItem item) {
        mShareRepo.fetchRolesFromRemote(item);
    }

    /**
     * Returns a LiveData which holds a data wrapper that contains a box item that has allowed roles for invitees and a string resource code.
     * @return a LiveData which holds a data wrapper that contains box item that has allowed roles for invitees and a string resource code
     */
    public LiveData<PresenterData<BoxCollaborationItem>> getRoleItem() {
        return mRoleItem;
    }



}
