package com.box.androidsdk.share.vm;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.share.sharerepo.ShareRepoInterface;

public class SelectRoleVM extends BaseVM {

    public SelectRoleVM(ShareRepoInterface shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
    }
    public LiveData<DataWrapper<BoxCollaborationItem>> fetchRoles(BoxCollaborationItem item) {
        return mShareRepo.fetchRoles(item);
    }
}
