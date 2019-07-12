package com.box.androidsdk.share.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
    private final LiveData<PresenterData<BoxIteratorCollaborations>> mCollaborations;

    public CollaborationsShareVM(ShareRepo shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
        ShareSDKTransformer transformer = new ShareSDKTransformer();
        mCollaborations = Transformations.map(shareRepo.getCollaborations(), transformer::getCollaborationsPresenterData);
        mDeleteCollaboration = Transformations.map(shareRepo.getDeleteCollaboration(), transformer::getDeleteCollaborationPresenterData);
        mUpdateOwner = Transformations.map(shareRepo.getUpdateOwner(), transformer::getUpdateOwnerPresenterData);
        mUpdateCollaboration = Transformations.map(shareRepo.getUpdateCollaboration(), transformer::getUpdateCollaborationPresenterData);
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
}
