package com.box.androidsdk.share.vm;

import androidx.lifecycle.ViewModel;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.share.sharerepo.ShareRepo;


/**
 * The base ViewModel class which should be extended by other ViewModels
 * Hold a BoxCollaborationItem and a BaseShareRepo that is required in all ViewModels
 */
public class BaseShareVM extends ViewModel {

    protected final ShareRepo mShareRepo;
    protected BoxCollaborationItem mShareItem;

    public BaseShareVM(ShareRepo shareRepo, BoxCollaborationItem shareItem) {
        this.mShareRepo = shareRepo;
        this.mShareItem = shareItem;
    }

    /**
     * Returns a LiveData which holds the item the user is currently doing share operation on.
     * @return a LiveData which holds the item the user is currently doing share operation on
     */
    public BoxCollaborationItem getShareItem() {
        return mShareItem;
    }

    /**
     * Update the share item with a new share item.
     * @param shareItem the new share item.
     */
    public void setShareItem(BoxCollaborationItem shareItem) {
        this.mShareItem = shareItem;
    }
}
