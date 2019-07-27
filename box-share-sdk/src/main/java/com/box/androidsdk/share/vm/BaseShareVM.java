package com.box.androidsdk.share.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.views.BoxAvatarView;
import com.box.androidsdk.share.sharerepo.ShareRepo;

import java.io.Serializable;


/**
 * The base ViewModel class which should be extended by other ViewModels
 * Hold a BoxCollaborationItem and a BaseShareRepo that is required in all ViewModels
 */
public class BaseShareVM extends ViewModel {

    protected final ShareRepo mShareRepo;
    protected BoxItem mShareItem;
    private final LiveData<PresenterData<BoxItem>> mItemInfo;

    public BaseShareVM(ShareRepo shareRepo, BoxCollaborationItem shareItem) {
        this.mShareRepo = shareRepo;
        this.mShareItem = shareItem;
        mItemInfo = Transformations.map(mShareRepo.getItemInfo(), response -> {
            PresenterData<BoxItem> data = new PresenterData<>();
            if (response.isSuccess()) {
                data.success(response.getResult());
            } else {
                data.setException(response.getException());
            }
            return data;
        });
    }

    /**
     * Returns the iem the user is currently doing share operation on.
     * @return the item the user is currently doing share operation on
     */
    public BoxItem getShareItem() {
        return mShareItem;
    }


    /**
     * Update the share item with a new share item.
     * @param shareItem the new share item.
     */
    public void setShareItem(BoxItem shareItem) {
        this.mShareItem = shareItem;
    }


    /**
     * Makes a backend call through share repo to get information about the item.
     * @param item the item to get information on
     */
    public void fetchItemInfo(BoxItem item) {
        mShareRepo.fetchItemInfo(item);
    }

    /**
     * Returns a LiveData which holds info about the share item.
     * @return the share item
     */
    public LiveData<PresenterData<BoxItem>> getItemInfo() {
        return mItemInfo;
    }


    /**
     * Returns user id of the current user.
     * @return user id of the current user
     */
    public String getUserId() {
        return mShareRepo.getUserId();
    }

    /**
     * Returns the avatar controller for displaying collaborators' avatars.
     * @return the avatar controller for displaying collaborators' avatars
     */
    public  <E extends BoxAvatarView.AvatarController & Serializable> E getAvatarController() {
        return mShareRepo.getAvatarController();
    }
}
