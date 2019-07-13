package com.box.androidsdk.share.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.share.internal.models.BoxFeatures;
import com.box.androidsdk.share.sharerepo.ShareRepo;
import com.box.androidsdk.share.utils.ShareSDKTransformer;

import java.sql.Date;

public class SharedLinkVM extends BaseShareVM {

    private final LiveData<PresenterData<BoxItem>> mShareLinkedItem;
    private final LiveData<PresenterData<BoxFeatures>> mSupportedFeatures;


    public SharedLinkVM(ShareRepo shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
        ShareSDKTransformer transformer = new ShareSDKTransformer();
        mShareLinkedItem = Transformations.map(shareRepo.getShareLinkedItem(),
                response -> transformer.getSharedLinkItemPresenterData(response, getShareItem()));

        mSupportedFeatures = Transformations.map(shareRepo.getSupportFeatures(), transformer::getSupportedFeaturePresenterData);

    }

    public void createDefaultSharedLink(BoxCollaborationItem item) {
        mShareRepo.createDefaultSharedLink(item);
    }

    public void disableSharedLink(BoxCollaborationItem item) {
        mShareRepo.disableSharedLink(item);
    }

    public void changeDownloadPermission(BoxCollaborationItem item, boolean canDownload) {
        mShareRepo.changeDownloadPermission(item, canDownload);
    }

    public void setExpiryDate(BoxCollaborationItem item, Date date) throws Exception {
        mShareRepo.setExpiryDate(item, date);
    }

    public void changeAccessLevel(BoxCollaborationItem item, BoxSharedLink.Access access) {
        mShareRepo.changeAccessLevel(item, access);
    }

    public void changePassword(BoxCollaborationItem item, String password) {
        mShareRepo.changePassword(item, password);
    }

    public void fetchSupportedFeatures() {
        mShareRepo.fetchSupportedFeatures();
    }

    public LiveData<PresenterData<BoxItem>> getSharedLinkedItem() {
        return mShareLinkedItem;
    }

    public LiveData<PresenterData<BoxFeatures>> getSupportedFeatures() {
        return mSupportedFeatures;
    }
}
