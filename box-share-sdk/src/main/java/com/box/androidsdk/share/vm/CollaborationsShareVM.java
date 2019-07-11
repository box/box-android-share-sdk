package com.box.androidsdk.share.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxVoid;
import com.box.androidsdk.share.sharerepo.ShareRepo;

public class CollaborationsShareVM extends BaseShareVM{
    private final LiveData<PresenterData<BoxVoid>> mDeleteCollaboration;
    private final LiveData<PresenterData<BoxVoid>> mUpdateOwner;
    private final LiveData<PresenterData<BoxVoid>> mUpdateCollaboration;
    private final LiveData<PresenterData<BoxIteratorCollaborations>> mCollaborations;

    public CollaborationsShareVM(ShareRepo shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
        mCollaborations = new MutableLiveData<>();
        mDeleteCollaboration = new MutableLiveData<>();
        mUpdateOwner = new MutableLiveData<>();
        mUpdateCollaboration = new MutableLiveData<>();
    }

    /**
     * Make a backend call through share repo to delete a collaboration.
     * @param collaboration the collaboration that will be deleted
     */
    public void deleteCollaborationRemote(BoxCollaboration collaboration) {
        mShareRepo.deleteCollaboration(collaboration);
    }

    public void updateCollaborationRemote(BoxCollaboration collaboration, BoxCollaboration.Role role) {
        mShareRepo.updateCollaboration(collaboration, role);
    }

    public void updateOwnerRemote(BoxCollaboration collaboration) {
        mShareRepo.updateOwner(collaboration);
    }

    public void fetchCollaborationsRemote(BoxCollaborationItem item) {
        mShareRepo.fetchCollaborations(item);
    }
}
