package com.box.androidsdk.share.vm;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.share.sharerepo.BaseShareRepo;

public class ShareVMFactory implements ViewModelProvider.Factory {

    private final BaseShareRepo mShareRepo;
    private final BoxCollaborationItem mShareItem;

    public ShareVMFactory(BaseShareRepo shareRepo, BoxCollaborationItem shareItem) {
        this.mShareRepo = shareRepo;
        this.mShareItem = shareItem;
    }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SelectRoleVM.class)) {
            return (T) new SelectRoleVM(mShareRepo, mShareItem);
        } else if (modelClass.isAssignableFrom(InviteCollaboratorsVM.class)) {
            return (T) new InviteCollaboratorsVM(mShareRepo, mShareItem);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
