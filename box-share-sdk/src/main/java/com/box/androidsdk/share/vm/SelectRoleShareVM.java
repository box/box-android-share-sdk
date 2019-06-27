package com.box.androidsdk.share.vm;

import androidx.lifecycle.ViewModel;

import com.box.androidsdk.content.models.BoxCollaboration;


import java.util.ArrayList;
import java.util.List;

/**
 * This ViewModel is for an activity which just needs to pass data back to the previous activity.
 */
public class SelectRoleShareVM extends ViewModel {


    private List<BoxCollaboration.Role> mRoles = new ArrayList<>();
    private boolean mAllowOwnerRole = false;
    private BoxCollaboration.Role mSelectedRole;
    private boolean mAllowRemove = false;
    private BoxCollaboration mCollaboration = new BoxCollaboration();

    public SelectRoleShareVM(List<BoxCollaboration.Role>
                        roles, boolean allowOwnerRole, BoxCollaboration.Role selectedRole, boolean allowRemove,
                             BoxCollaboration collaboration) {
        this.mRoles = roles;
        this.mAllowOwnerRole = allowOwnerRole;
        this.mSelectedRole = selectedRole;
        this.mAllowRemove = allowRemove;
        this.mCollaboration = collaboration;
    }

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
    public boolean isAllowOwnerRole() {
        return mAllowOwnerRole;
    }

    /**
     * Returns the selected collaboration role.
     * @return the selected collaboration role
     */
    public BoxCollaboration.Role getSelectedRole() {
        return mSelectedRole;
    }

    /**
     * Returns true if the collaborator can be removed.
     * @return true if the collaborator can be removed
     */
    public boolean isAllowRemove() {
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
     * Set a new value for selected role.
     * @param selectedRole the new selected role.
     */
    public void setSelectedRole(BoxCollaboration.Role selectedRole) {
        this.mSelectedRole = selectedRole;
    }
}
