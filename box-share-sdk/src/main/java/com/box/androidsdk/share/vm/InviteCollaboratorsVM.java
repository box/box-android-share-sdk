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
import com.box.androidsdk.share.sharerepo.BaseShareRepo;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A ViewModel for holding data needed for InviteCollaborators Screen
 */
public class InviteCollaboratorsVM extends BaseVM {

    private LiveData<DataWrapper<BoxCollaborationItem>> mFetchRoleItem;
    private LiveData<InviteCollaboratorsDataWrapper> mAddCollabs;
    private LiveData<DataWrapper<BoxIteratorInvitees>> mInvitees;
    public InviteCollaboratorsVM(BaseShareRepo shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
        mFetchRoleItem = Transformations.map(shareRepo.getFetchRoleItem(), response -> createFetchRoleItemData(response));
        mAddCollabs = Transformations.map(shareRepo.getInviteCollabBatch(), response -> createAddCollabsItemData(response));
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
    public LiveData<DataWrapper<BoxCollaborationItem>> getFetchRoleItem() {
        return mFetchRoleItem;
    }

    /**
     * Returns a LiveData which holds a data wrapper that contains the status message from the response for adding new collaborators.
     * @return a LiveData which holds a data wrapper that contains
     */
    public LiveData<InviteCollaboratorsDataWrapper> getAddCollabs() {
        return mAddCollabs;
    }

    /**
     * Returns a LiveData which holds a data wrapper that contains a list of invitees that can be invited and a string resource code.
     * @return a LiveData which holds a data wrapper that contains a list of invitees that can be invited and a string resource code
     */
    public LiveData<DataWrapper<BoxIteratorInvitees>> getInvitees() {
        return mInvitees;
    }

    /**
     * Helper method for transforming BoxResponse to UI Model for fetchRoleApi
     * @param response the response to transform on
     * @return the transformed data
     */
    private DataWrapper<BoxCollaborationItem> createFetchRoleItemData(BoxResponse<BoxCollaborationItem> response) {
        final DataWrapper<BoxCollaborationItem> data = new DataWrapper<BoxCollaborationItem>();
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
    private DataWrapper<BoxIteratorInvitees> createGetInviteesItemData(BoxResponse<BoxIteratorInvitees> response) {
        final DataWrapper<BoxIteratorInvitees> data = new DataWrapper<BoxIteratorInvitees>();
        if (response.isSuccess()) {
            final BoxIteratorInvitees invitees = response.getResult();
            data.success(invitees);
        } else {
            BoxException boxException = (BoxException) response.getException();
            int responseCode = boxException.getResponseCode();
            int strCode = -1;
            if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                strCode = R.string.box_sharesdk_insufficient_permissions;
            } else if (boxException.getErrorType() == BoxException.ErrorType.NETWORK_ERROR) {
                strCode = R.string.box_sharesdk_network_error;
            }
            data.failure(strCode);
        }
        return data;
    }
    /**
     * Helper method for transforming BoxResponse to UI Model for addCollabsApi
     * @param response the response to transform
     * @return the transformed model
     */
    private InviteCollaboratorsDataWrapper createAddCollabsItemData(BoxResponse<BoxResponseBatch> response) {
        return handleCollaboratorsInvited(response.getResult());
    }

    /**
     * Helper method for helping with transformation of BoxBatchResponse to UI Model
     * @param responses the batch response to transform
     * @return the transformed model
     */
    @VisibleForTesting
    private InviteCollaboratorsDataWrapper handleCollaboratorsInvited(BoxResponseBatch responses) {
        int strCode = R.string.box_sharesdk_generic_error; //default generic error
        boolean mInvitationFailed;
        String subMssg;

        int alreadyAddedCount = 0;
        boolean didRequestFail = false;
        String name = "";
        HashSet<Integer> failureCodes = generateFailureCodes();
        List<String> failedCollaboratorsList = new ArrayList<>();


        for (BoxResponse<BoxCollaboration> r : responses.getResponses()) {
            if (!r.isSuccess()) {
                if (checkIfKnownFailure(r, failureCodes)) {
                    name = updateFailureStats(r, failedCollaboratorsList);
                    alreadyAddedCount += name != null ? 1 : 0;
                }
                didRequestFail = true;
            }
        }

        if (didRequestFail) {
            String[] result = processRequestFailure(failedCollaboratorsList, name, alreadyAddedCount);
            strCode = Integer.parseInt(result[0]);
            subMssg = result[1];
        } else {
            String[] result = processRequestSuccess(responses);
            strCode = Integer.parseInt(result[0]);
            subMssg = result[1];
        }

        mInvitationFailed = (didRequestFail && !failedCollaboratorsList.isEmpty());

        return new InviteCollaboratorsDataWrapper(subMssg, strCode, mInvitationFailed);
    }

    /**
     * A helper method for updating attributes used for keeping track of stats for failure
     * @param r the response to get request infos from
     * @param failedCollaboratorsList list of failed collabs to be updated
     * @return the name of collaborator that is already added
     */
    @VisibleForTesting
    String updateFailureStats(BoxResponse<BoxCollaboration> r, List<String> failedCollaboratorsList) {
        String code = ((BoxException) r.getException()).getAsBoxError().getCode();
        BoxUser user = (BoxUser) ((BoxRequestsShare.AddCollaboration) r.getRequest()).getAccessibleBy();

        if (alreadyAddedFailure(code)) {
            return user == null ? "" : user.getLogin();
        } else {
            failedCollaboratorsList.add(user == null ? "" : user.getLogin());
            return null;
        }
    }

    /**
     * Helper method for processing request if all requests were successful
     * @param responses the responses that was successful
     * @return index 0 is string resource code, index 1 is the string formatted part of the message.
     */
    @VisibleForTesting
    String[] processRequestSuccess(BoxResponseBatch responses) {
        String[] res = new String[2];
        if (responses.getResponses().size() == 1) {
            BoxCollaboration collaboration = (BoxCollaboration) responses.getResponses().get(0).getResult();
            if (collaboration.getAccessibleBy() == null) {
                res[0] = Integer.toString(R.string.box_sharesdk_collaborators_invited);
            } else {
                String login = ((BoxUser)(collaboration).getAccessibleBy()).getLogin();
                res[0] = Integer.toString(R.string.box_sharesdk_collaborator_invited);
                res[1] = login;
            }

        } else {
            res[0] = Integer.toString(R.string.box_sharesdk_collaborators_invited);
        }
        return res;
    }
    /**
     * Helper method for processing request if any requests were successful
     * @param failedCollaboratorsList the list of collaborators for whom requests were not successful
     * @param name the name of a collaborator that is already added
     * @param alreadyAddedCount how many collaborators were already added
     * @return index 0 is string resource code, index 1 is the string formatted part of the message.
     */
    @VisibleForTesting
    String[] processRequestFailure(List<String> failedCollaboratorsList, String name, int alreadyAddedCount) {
        String[] res = new String[2];
        if (!failedCollaboratorsList.isEmpty()) {
            StringBuilder collaborators = new StringBuilder();
            for (int i = 0; i < failedCollaboratorsList.size(); i++) {
                collaborators.append(failedCollaboratorsList.get(i));
                if (i < failedCollaboratorsList.size() - 1) {
                    collaborators.append(' ');
                }
            }
            res[0] = Integer.toString(R.string.box_sharesdk_following_collaborators_error);
            res[1] = collaborators.toString();

        } else if (alreadyAddedCount == 1) {
            res[0] = Integer.toString(R.string.box_sharesdk_has_already_been_invited);
            res[1] = name;
        } else if (alreadyAddedCount > 1) {
            res[0] = Integer.toString(R.string.box_sharesdk_num_has_already_been_invited);
            res[1] = Integer.toString(alreadyAddedCount);
        } else {
            res[0] = Integer.toString(R.string.box_sharesdk_unable_to_invite);
        }
        return res;
    }

    private boolean alreadyAddedFailure(String code) {
        return !SdkUtils.isBlank(code) && code.equals(BoxRequestsShare.AddCollaboration.ERROR_CODE_USER_ALREADY_COLLABORATOR);
    }

    private boolean checkIfKnownFailure(BoxResponse<BoxCollaboration> r, HashSet<Integer> failureCodes) {
        return r.getException() instanceof BoxException && failureCodes.contains(((BoxException) r.getException()).getResponseCode());
    }

    /**
     * Generates failure codes for checking known errors
     * @return a HashSet of known errors
     */
    private HashSet<Integer> generateFailureCodes() {
        HashSet<Integer> failureCodes = new HashSet<>();
        failureCodes.add(HttpURLConnection.HTTP_BAD_REQUEST );
        failureCodes.add(HttpURLConnection.HTTP_FORBIDDEN);

        return failureCodes;
    }

}
