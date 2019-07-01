package com.box.androidsdk.share.vm;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.box.androidsdk.content.models.BoxCollaboration;


import java.util.ArrayList;
import java.util.List;

/**
 * This ViewModel is for an activity which just needs to pass data back to the previous activity.
 */
public class SelectRoleShareVM extends ViewModel {


    private List<BoxCollaboration.Role> mRoles;
    private boolean mAllowOwnerRole = false;
    private MutableLiveData<BoxCollaboration.Role> mSelectedRole = new MutableLiveData<>();
    private boolean mAllowRemove = false;
    private BoxCollaboration mCollaboration;

    /**
     * Returns a list of valid roles for collaborator.
     * @return a list of valid roles for collaborator
     */
    public List<BoxCollaboration.Role> getRoles() {
        return mRoles;
    }

    /**
     * Returns true if the collaborator can be given owner permission.
     * @return true if the collaborator can be given owner permission
     */
    public boolean isOwnerRoleAllowed() {
        return mAllowOwnerRole;
    }

    /**
     * Returns a Live Data that holds the selected collaboration role.
     * @return a Live Data that holds the selected collaboration role
     */
    public MutableLiveData<BoxCollaboration.Role> getSelectedRole() {
        return mSelectedRole;
    }

    /**
     * Returns true if the collaborator can be removed.
     * @return true if the collaborator can be removed
     */
    public boolean isRemoveAllowed() {
        return mAllowRemove;
    }

    /**
     * Returns collaboration information on the item.
     * @return collaboration information on the item
     */
    public BoxCollaboration getCollaboration() {
        return mCollaboration;
    }

    /**
     * Set a new selected role.
     * @param selectedRole the new selected role.
     */
    public void setSelectedRole(BoxCollaboration.Role selectedRole) {
        this.mSelectedRole.postValue(selectedRole);
    }

    /**
     * Set a new list of valid roles
     * @param roles the new list of valid roles.
     */
    public void setRoles(List<BoxCollaboration.Role> roles) {
        this.mRoles = roles;
    }

    /**
     * Set a new permission for allowing owner role for a collaborator.
     * @param allowOwnerRole the new permission for allowing owner role for a collaborator.
     */
    public void setAllowOwnerRole(boolean allowOwnerRole) {
        this.mAllowOwnerRole = allowOwnerRole;
    }

    /**
     * Set a new permission for allowing removing a collaborator.
     * @param allowRemove the new permission for allowing removing collaborator
     */
    public void setAllowRemove(boolean allowRemove) {
        this.mAllowRemove = allowRemove;
    }

    /**
     * Set new collaboration information on the item.
     * @param collaboration the new collaboration information
     */
    public void setCollaboration(BoxCollaboration collaboration) {
        this.mCollaboration = collaboration;
    }
}
