package com.box.androidsdk.share.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.share.sharerepo.ShareRepo;
import com.box.androidsdk.share.utils.ShareSDKTransformer;

public class CollaboratorsInitialsVM extends BaseShareVM {

    private final LiveData<PresenterData<BoxIteratorCollaborations>> mCollaborations;

    public CollaboratorsInitialsVM(ShareRepo shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
        ShareSDKTransformer transformer = new ShareSDKTransformer();
        mCollaborations = Transformations.map(shareRepo.getCollaborations(), response -> transformer.getIntialsViewCollabsPresenterData(response, getCollaborationsValue()));
    }

    public void fetchCollaborations(BoxCollaborationItem item) {
        mShareRepo.fetchCollaborations(item);
    }

    public LiveData<PresenterData<BoxIteratorCollaborations>> getCollaborations() {
        return mCollaborations;
    }

    private BoxIteratorCollaborations getCollaborationsValue() {
        if (mCollaborations != null && mCollaborations.getValue() != null) {
            return mCollaborations.getValue().getData();
        }
        return null;
    }
}
