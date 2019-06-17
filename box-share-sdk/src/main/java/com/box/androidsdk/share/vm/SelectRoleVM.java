package com.box.androidsdk.share.vm;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.widget.RadioButton;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.share.sharerepo.BaseShareRepo;

import java.util.ArrayList;
import java.util.List;

public class SelectRoleVM extends BaseVM {


    private MutableLiveData<List<BoxCollaboration.Role>> mRoles = new MutableLiveData<List<BoxCollaboration.Role>>();
    private MutableLiveData<Boolean> mAllowOwnerRole = new MutableLiveData<Boolean>();
    private MutableLiveData<BoxCollaboration.Role> mSelectedRole = new MutableLiveData<BoxCollaboration.Role>();
    private MutableLiveData<Boolean> mAllowRemove = new MutableLiveData<Boolean>();
    private MutableLiveData<BoxCollaboration> mCollaboration = new MutableLiveData<BoxCollaboration>();

    public SelectRoleVM(BaseShareRepo shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
    }

    public LiveData<List<BoxCollaboration.Role>> getmRoles() {
        return mRoles;
    }

    public LiveData<Boolean> getmAllowOwnerRole() {
        return mAllowOwnerRole;
    }

    public LiveData<BoxCollaboration.Role> getmSelectedRole() {
        return mSelectedRole;
    }

    public LiveData<Boolean> getmAllowRemove() {
        return mAllowRemove;
    }

    public LiveData<BoxCollaboration> getmCollaboration() {
        return mCollaboration;
    }

    public void updatemRoles(List<BoxCollaboration.Role> roles) {
        this.mRoles.postValue(roles);
    }

    public void updatemAllowOwnerRole(Boolean allowOwnerRole) {
        this.mAllowOwnerRole.postValue(allowOwnerRole);
    }

    public void updatemSelectedRole(BoxCollaboration.Role mSelectedRole) {
        this.mSelectedRole.postValue(mSelectedRole);
    }

    public void updatemAllowRemove(Boolean mAllowRemove) {
        this.mAllowRemove.postValue(mAllowRemove);
    }

    public void updatemCollaboration(BoxCollaboration collaboration) {
        this.mCollaboration.postValue(collaboration);
    }
}
