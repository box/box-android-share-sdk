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
     * Returns a LiveData which holds a list of valid roles for collaborator.
     * @return a LiveData which holds a list of valid roles for collaborator
     */
    public LiveData<List<BoxCollaboration.Role>> getRoles() {
        return mRoles;
    }

    /**
     * Returns a LiveData which tells whether the collaborator can be given owner permission or not.
     * @return a LiveData which tells whether the collaborator can be given owner permission or not
     */
    public LiveData<Boolean> getAllowOwnerRole() {
        return mAllowOwnerRole;
    }

    /**
     * Returns a LiveData which tells which role is selected.
     * @return a LiveData which tells which role is selected
     */
    public LiveData<BoxCollaboration.Role> getSelectedRole() {
        return mSelectedRole;
    }

    /**
     * Returns a LiveData which tells whether the collaborator can be removed.
     * @return a LiveData which tells whether the collaborator can be removed
     */
    public LiveData<Boolean> getAllowRemove() {
        return mAllowRemove;
    }

    /**
     * Returns a LiveData which tells the collaboration rules on the item.
     * @return  LiveData which tells the collaboration rules on the item
     */
    public LiveData<BoxCollaboration> getCollaboration() {
        return mCollaboration;
    }

    /**
     * Updates list of valid roles
     * @param roles the new list of valid roles
     */
    public void updateRoles(List<BoxCollaboration.Role> roles) {
        this.mRoles.postValue(roles);
    }

    /**
     * Updates whether owner role is allowed or not
     * @param allowOwnerRole the new permission level for owner role
     */
    public void updateAllowOwnerRole(Boolean allowOwnerRole) {
        this.mAllowOwnerRole.postValue(allowOwnerRole);
    }

    /**
     * Updates the currently selected role
     * @param mSelectedRole the new selected role
     */
    public void updateSelectedRole(BoxCollaboration.Role mSelectedRole) {
        this.mSelectedRole.postValue(mSelectedRole);
    }

    /**
     * Updates whether the collaborator can be removed or not
     * @param mAllowRemove the new permission level for removing
     */
    public void updateAllowRemove(Boolean mAllowRemove) {
        this.mAllowRemove.postValue(mAllowRemove);
    }

    /**
     * Updates collaborating settings on the item
     * @param collaboration the new collaboration setting
     */
    public void updateCollaboration(BoxCollaboration collaboration) {
        this.mCollaboration.postValue(collaboration);
    }
}
