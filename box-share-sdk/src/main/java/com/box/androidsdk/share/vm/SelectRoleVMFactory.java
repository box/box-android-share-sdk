package com.box.androidsdk.share.vm;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.box.androidsdk.content.models.BoxCollaboration;

import java.util.ArrayList;
import java.util.List;

public class SelectRoleVMFactory implements ViewModelProvider.Factory {

    private List<BoxCollaboration.Role> mRoles = new ArrayList<>();
    private boolean mAllowOwnerRole = false;
    private BoxCollaboration.Role mSelectedRole;
    private boolean mAllowRemove = false;
    private BoxCollaboration mCollaboration = new BoxCollaboration();

    public SelectRoleVMFactory(List<BoxCollaboration.Role>
                                roles, boolean allowOwnerRole, BoxCollaboration.Role selectedRole, boolean allowRemove,
                        BoxCollaboration collaboration) {
        this.mRoles = roles;
        this.mAllowOwnerRole = allowOwnerRole;
        this.mSelectedRole = selectedRole;
        this.mAllowRemove = allowRemove;
        this.mCollaboration = collaboration;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SelectRoleShareVM.class)) {
            return (T) new SelectRoleShareVM(mRoles, mAllowOwnerRole, mSelectedRole, mAllowRemove, mCollaboration);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
