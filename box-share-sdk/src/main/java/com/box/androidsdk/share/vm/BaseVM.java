package com.box.androidsdk.share.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.share.sharerepo.BaseShareRepo;


/**
 * The base ViewModel class which should be extended by other ViewModels
 * Hold a BoxCollaborationItem and a BaseShareRepo that is required in all ViewModels
 */
public class BaseVM extends ViewModel {

    protected final BaseShareRepo mShareRepo;
    protected final MutableLiveData<BoxCollaborationItem> mShareItem;

    public BaseVM(BaseShareRepo shareRepo, BoxCollaborationItem shareItem) {
        this.mShareRepo = shareRepo;
        this.mShareItem = new MutableLiveData<>();
        this.mShareItem.postValue(shareItem);
    }

    /**
     * Returns a LiveData which holds the item the user is currently doing share operation on.
     * @return a LiveData which holds the item the user is currently doing share operation on
     */
    public LiveData<BoxCollaborationItem> getShareItem() {
        return mShareItem;
    }
}
