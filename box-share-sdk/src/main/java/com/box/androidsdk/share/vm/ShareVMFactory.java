package com.box.androidsdk.share.vm;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.share.sharerepo.ShareRepo;

/**
 * A VM Factory that should be used for generating ViewModels
 */
public class ShareVMFactory implements ViewModelProvider.Factory {

    private final ShareRepo mShareRepo;
    private final BoxCollaborationItem mShareItem;

    public ShareVMFactory(ShareRepo shareRepo, BoxCollaborationItem shareItem) {
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
