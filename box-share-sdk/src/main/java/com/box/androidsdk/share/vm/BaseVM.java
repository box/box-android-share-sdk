package com.box.androidsdk.share.vm;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.share.sharerepo.ShareRepoInterface;

public class BaseVM extends ViewModel {

    protected final ShareRepoInterface mShareRepo;
    protected final MutableLiveData<BoxCollaborationItem> mShareItem;

    public BaseVM(ShareRepoInterface shareRepo, BoxCollaborationItem shareItem) {
        this.mShareRepo = shareRepo;
        this.mShareItem = new MutableLiveData<BoxCollaborationItem>();
        this.mShareItem.postValue(shareItem);
    }


}
