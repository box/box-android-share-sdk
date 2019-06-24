package com.box.androidsdk.share.vm;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxUser;
import com.box.androidsdk.content.requests.BoxRequestsShare;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;
import com.box.androidsdk.share.sharerepo.ShareRepo;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A ViewModel for holding data needed for InviteCollaborators Screen
 */
public class InviteCollaboratorsShareVM extends BaseShareVM {

    private LiveData<PresenterData<BoxCollaborationItem>> mFetchRoleItem;
    private LiveData<InviteCollaboratorsPresenterData> mAddCollabs;
    private LiveData<PresenterData<BoxIteratorInvitees>> mInvitees;

    public InviteCollaboratorsShareVM(ShareRepo shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
        mFetchRoleItem = Transformations.map(shareRepo.getFetchRoleItem(), response -> createFetchRoleItemData(response));
        mAddCollabs = Transformations.map(shareRepo.getAddCollabsBatch(), response -> InviteCollaboratorsPresenterData.getPresenterDataFromBoxResponse(response));
        mInvitees = Transformations.map(shareRepo.getInvitees(), response -> createGetInviteesItemData(response));
    }

    /**
     * Makes a backend call through share repo for fetching roles.
     * @param item the item to fetch roles on
     */
    public void fetchRolesApi(BoxCollaborationItem item) {
        mShareRepo.fetchRolesApi(item);
    }

    /**
     * Makes a backend call through share repo for adding new collaborators.
     * @param boxCollaborationItem the item to add collaborators on
     * @param selectedRole the role for the new collaborators
     * @param emails a list of collaborators represented in emails
     */
    public void addCollabsApi(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails) {
        mShareRepo.addCollabsApi(boxCollaborationItem, selectedRole, emails);
    }

    /**
     * Makes a backend call through share repo for getting invitees.
     * @param boxCollaborationItem the item to get invitees on
     * @param filter the term used for filtering invitees
     */
    public void getInviteesApi(BoxCollaborationItem boxCollaborationItem, String filter) {
        mShareRepo.getInviteesApi(boxCollaborationItem, filter);
    }

    /**
     * Returns a LiveData which holds a data wrapper that contains a box item that has allowed roles for invitees and a string resource code.
     * @return a LiveData which holds a data wrapper that contains box item that has allowed roles for invitees and a string resource code
     */
    public LiveData<PresenterData<BoxCollaborationItem>> getFetchRoleItem() {
        return mFetchRoleItem;
    }

    /**
     * Returns a LiveData which holds a data wrapper that contains the status message from the response for adding new collaborators.
     * @return a LiveData which holds a data wrapper that contains
     */
    public LiveData<InviteCollaboratorsPresenterData> getAddCollabs() {
        return mAddCollabs;
    }

    /**
     * Returns a LiveData which holds a data wrapper that contains a list of invitees that can be invited and a string resource code.
     * @return a LiveData which holds a data wrapper that contains a list of invitees that can be invited and a string resource code
     */
    public LiveData<PresenterData<BoxIteratorInvitees>> getInvitees() {
        return mInvitees;
    }

    /**
     * Helper method for transforming BoxResponse to UI Model for fetchRoleApi
     * @param response the response to transform on
     * @return the transformed data
     */
    private static PresenterData<BoxCollaborationItem> createFetchRoleItemData(BoxResponse<BoxCollaborationItem> response) {
        final PresenterData<BoxCollaborationItem> data = new PresenterData<BoxCollaborationItem>();
        if (response.isSuccess()) {
            BoxCollaborationItem collaborationItem = response.getResult();
            data.success(collaborationItem);
        } else {
            data.failure(R.string.box_sharesdk_network_error);
        }
        return data;
    }
    /**
     * Helper method for transforming BoxResponse to UI Model for getInviteesApi
     * @param response the response to transform
     * @return the transformed model
     */
    private static PresenterData<BoxIteratorInvitees> createGetInviteesItemData(BoxResponse<BoxIteratorInvitees> response) {
        final PresenterData<BoxIteratorInvitees> data = new PresenterData<BoxIteratorInvitees>();
        if (response.isSuccess()) {
            final BoxIteratorInvitees invitees = response.getResult();
            data.success(invitees);
        } else {
            BoxException boxException = (BoxException) response.getException();
            int responseCode = boxException.getResponseCode();
            int errorStrCode = R.string.box_sharesdk_generic_error;
            if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                errorStrCode = R.string.box_sharesdk_insufficient_permissions;
            } else if (boxException.getErrorType() == BoxException.ErrorType.NETWORK_ERROR) {
                errorStrCode = R.string.box_sharesdk_network_error;
            }
            data.failure(errorStrCode);
        }
        return data;
    }


}
