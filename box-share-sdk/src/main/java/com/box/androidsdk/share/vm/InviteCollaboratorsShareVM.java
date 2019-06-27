package com.box.androidsdk.share.vm;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;
import com.box.androidsdk.share.sharerepo.ShareRepo;
import com.box.androidsdk.share.utils.InviteCollabsTransformer;


/**
 * A ViewModel for holding data needed for InviteCollaborators Screen
 */
public class InviteCollaboratorsShareVM extends BaseShareVM {

    private LiveData<PresenterData<BoxCollaborationItem>> mRoleItem;
    private LiveData<InviteCollaboratorsPresenterData> mInviteCollabs;
    private LiveData<PresenterData<BoxIteratorInvitees>> mInvitees;

    public InviteCollaboratorsShareVM(ShareRepo shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
        InviteCollabsTransformer transformer = new InviteCollabsTransformer();
        mRoleItem = Transformations.map(shareRepo.getRoleItem(), response -> transformer.getFetchRolesItemPresenterData(response));
        mInviteCollabs = Transformations.map(shareRepo.getInviteCollabsBatchResponse(), response -> transformer.getInviteCollabsPresenterDataFromBoxResponse(response));
        mInvitees = Transformations.map(shareRepo.getInvitees(), response -> transformer.getInviteesPresenterData(response));
    }


    //used for mocking the transformer since it's not VM job.
    @VisibleForTesting
    InviteCollaboratorsShareVM(ShareRepo shareRepo, BoxCollaborationItem shareItem, InviteCollabsTransformer transformer) {
        super(shareRepo, shareItem);
        mRoleItem = Transformations.map(shareRepo.getRoleItem(), response -> transformer.getFetchRolesItemPresenterData(response));
        mInviteCollabs = Transformations.map(shareRepo.getInviteCollabsBatchResponse(), response -> transformer.getInviteCollabsPresenterDataFromBoxResponse(response));
        mInvitees = Transformations.map(shareRepo.getInvitees(), response -> transformer.getInviteesPresenterData(response));
    }

    /**
     * Makes a backend call through share repo for fetching roles.
     * @param item the item to fetch roles on
     */
    public void fetchRolesFromRemote(BoxCollaborationItem item) {
        mShareRepo.fetchRolesFromRemote(item);
    }

    /**
     * Makes a backend call through share repo for adding new collaborators.
     * @param boxCollaborationItem the item to add collaborators on
     * @param selectedRole the role for the new collaborators
     * @param emails a list of collaborators represented in emails
     */
    public void inviteCollabs(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails) {
        mShareRepo.inviteCollabs(boxCollaborationItem, selectedRole, emails);
    }

    /**
     * Makes a backend call through share repo for getting invitees.
     * @param boxCollaborationItem the item to get invitees on
     * @param filter the term used for filtering invitees
     */
    public void fetchInviteesFromRemote(BoxCollaborationItem boxCollaborationItem, String filter) {
        mShareRepo.fetchInviteesFromRemote(boxCollaborationItem, filter);
    }

    /**
     * Returns a LiveData which holds a data wrapper that contains a box item that has allowed roles for invitees and a string resource code.
     * @return a LiveData which holds a data wrapper that contains box item that has allowed roles for invitees and a string resource code
     */
    public LiveData<PresenterData<BoxCollaborationItem>> getRoleItem() {
        return mRoleItem;
    }

    /**
     * Returns a LiveData which holds a data wrapper that contains the status message from the response for adding new collaborators.
     * @return a LiveData which holds a data wrapper that contains
     */
    public LiveData<InviteCollaboratorsPresenterData> getInviteCollabs() {
        return mInviteCollabs;
    }

    /**
     * Returns a LiveData which holds a data wrapper that contains a list of invitees that can be invited and a string resource code.
     * @return a LiveData which holds a data wrapper that contains a list of invitees that can be invited and a string resource code
     */
    public LiveData<PresenterData<BoxIteratorInvitees>> getInvitees() {
        return mInvitees;
    }

}
