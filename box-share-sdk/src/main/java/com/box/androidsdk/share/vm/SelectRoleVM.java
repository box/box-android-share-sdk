package com.box.androidsdk.share.vm;



import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.share.sharerepo.BaseShareRepo;

import java.util.List;

public class SelectRoleVM extends BaseVM {


    private MutableLiveData<List<BoxCollaboration.Role>> mRoles = new MutableLiveData<>();
    private MutableLiveData<Boolean> mAllowOwnerRole = new MutableLiveData<>();
    private MutableLiveData<BoxCollaboration.Role> mSelectedRole = new MutableLiveData<>();
    private MutableLiveData<Boolean> mAllowRemove = new MutableLiveData<>();
    private MutableLiveData<BoxCollaboration> mCollaboration = new MutableLiveData<>();

    public SelectRoleVM(BaseShareRepo shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
    }

    /**
     * Get mRoles which holds a list of valid roles for collaborator
     * @return mRoles
     */
    public LiveData<List<BoxCollaboration.Role>> getRoles() {
        return mRoles;
    }

    /**
     * Get mAllowOwnerRole which tells whether the collaborator can be given owner permission or not
     * @return mAllowOwnerRole
     */
    public LiveData<Boolean> getAllowOwnerRole() {
        return mAllowOwnerRole;
    }

    /**
     * Get mSelectedRole which tells which role is selected.
     * @return mSelectedRole
     */
    public LiveData<BoxCollaboration.Role> getSelectedRole() {
        return mSelectedRole;
    }

    /**
     * Get mAllowRemove which tells whether the collaborator can be removed
     * @return mAllowRemove
     */
    public LiveData<Boolean> getAllowRemove() {
        return mAllowRemove;
    }

    /**
     * Get mCollaboration which tells the collaboration rules on the item
     * @return mCollaboration
     */
    public LiveData<BoxCollaboration> getCollaboration() {
        return mCollaboration;
    }

    /**
     * Update list of valid roles
     * @param roles the new list of valid roles
     */
    public void updateRoles(List<BoxCollaboration.Role> roles) {
        this.mRoles.postValue(roles);
    }

    /**
     * Update whether owner role is allowed or not
     * @param allowOwnerRole the new permission level for owner role
     */
    public void updateAllowOwnerRole(Boolean allowOwnerRole) {
        this.mAllowOwnerRole.postValue(allowOwnerRole);
    }

    /**
     * Update the currently selected role
     * @param mSelectedRole the new selected role
     */
    public void updateSelectedRole(BoxCollaboration.Role mSelectedRole) {
        this.mSelectedRole.postValue(mSelectedRole);
    }

    /**
     * Update whether the collaborator can be removed or not
     * @param mAllowRemove the new permission level for removing
     */
    public void updateAllowRemove(Boolean mAllowRemove) {
        this.mAllowRemove.postValue(mAllowRemove);
    }

    /**
     * Update collaborating settings on the item
     * @param collaboration the new collaboration setting
     */
    public void updateCollaboration(BoxCollaboration collaboration) {
        this.mCollaboration.postValue(collaboration);
    }
}
