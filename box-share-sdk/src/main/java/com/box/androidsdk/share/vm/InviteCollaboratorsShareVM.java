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
    private static HashSet<Integer> failureCodes;
    static {
        failureCodes = getFailureCodes();
    }
    public InviteCollaboratorsShareVM(ShareRepo shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
        mFetchRoleItem = Transformations.map(shareRepo.getFetchRoleItem(), response -> createFetchRoleItemData(response));
        mAddCollabs = Transformations.map(shareRepo.getAddCollabsBatch(), response -> createAddCollabsItemData(response));
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
    /**
     * Helper method for transforming BoxResponse to UI Model for addCollabsApi
     * @param response the response to transform
     * @return the transformed model
     */
    private static InviteCollaboratorsPresenterData createAddCollabsItemData(BoxResponse<BoxResponseBatch> response) {
        return getAddCollabsItemData(response.getResult());
    }

    /**
     * Helper method for helping with transformation of BoxBatchResponse to UI Model
     * @param responses the batch response to transform
     * @return the transformed model
     */
    private static InviteCollaboratorsPresenterData getAddCollabsItemData(BoxResponseBatch responses) {
        int strCode = R.string.box_sharesdk_generic_error; //default generic error
        boolean mInvitationFailed;
        String subMssg;

        int alreadyAddedCount = 0;
        boolean didRequestFail = false;
        String name = "";

        List<String> failedCollaboratorsList = new ArrayList<>();
        for (BoxResponse<BoxCollaboration> r : responses.getResponses()) {
            if (!r.isSuccess()) {
                if (checkIfKnownFailure(r, failureCodes)) {
                    String[] res = getFailureStats(r);
                    name = res[0];
                    alreadyAddedCount += name != null ? 1 : 0;
                    if (res[1] != null) {
                        failedCollaboratorsList.add(res[1]);
                    }
                }
                didRequestFail = true;
            }
        }

        if (didRequestFail) {
            String[] result = getAddCollabsRequestFailure(failedCollaboratorsList, name, alreadyAddedCount);
            strCode = Integer.parseInt(result[0]);
            subMssg = result[1];
        } else {
            String[] result = getAddCollabsRequestSuccess(responses);
            strCode = Integer.parseInt(result[0]);
            subMssg = result[1];
        }

        mInvitationFailed = (didRequestFail && !failedCollaboratorsList.isEmpty());

        return new InviteCollaboratorsPresenterData(subMssg, strCode, mInvitationFailed);
    }

    /**
     * A helper method for updating attributes used for keeping track of stats for failure
     * @param r the response to get request infos from
     * @return index 0 is name of the person already added; index 1 is the name of collaborator that failed (only one of them will be used at a time)
     */
    @VisibleForTesting
    static String[] getFailureStats(BoxResponse<BoxCollaboration> r) {
        String[] res = new String[2];
        String code = ((BoxException) r.getException()).getAsBoxError().getCode();
        BoxUser user = (BoxUser) ((BoxRequestsShare.AddCollaboration) r.getRequest()).getAccessibleBy();

        String name = user == null ? "" : user.getLogin();
        if (alreadyAddedFailure(code)) {
            res[0] = name;
        } else {
            res[1] = name;
        }
        return res;
    }

    /**
     * Helper method for processing request if all requests were successful
     * @param responses the responses that was successful
     * @return index 0 is string resource code, index 1 is the string formatted part of the message.
     */
    @VisibleForTesting
    static String[] getAddCollabsRequestSuccess(BoxResponseBatch responses) {
        String[] res = new String[2];
        if (responses.getResponses().size() == 1) {
            BoxCollaboration collaboration = (BoxCollaboration) responses.getResponses().get(0).getResult();
            if (collaboration.getAccessibleBy() == null) {
                res[0] = Integer.toString(R.string.box_sharesdk_collaborators_invited);
            } else {
                res[0] = Integer.toString(R.string.box_sharesdk_collaborator_invited);
                res[1] = ((BoxUser)(collaboration).getAccessibleBy()).getLogin();
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
    static String[] getAddCollabsRequestFailure(List<String> failedCollaboratorsList, String name, int alreadyAddedCount) {
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

    private static boolean alreadyAddedFailure(String code) {
        return !SdkUtils.isBlank(code) && code.equals(BoxRequestsShare.AddCollaboration.ERROR_CODE_USER_ALREADY_COLLABORATOR);
    }

    private static boolean checkIfKnownFailure(BoxResponse<BoxCollaboration> r, HashSet<Integer> failureCodes) {
        return r.getException() instanceof BoxException && failureCodes.contains(((BoxException) r.getException()).getResponseCode());
    }

    /**
     * Generates failure codes for checking known errors
     * @return a HashSet of known errors
     */
    private static HashSet<Integer> getFailureCodes() {
        HashSet<Integer> failureCodes = new HashSet<>();
        failureCodes.add(HttpURLConnection.HTTP_BAD_REQUEST);
        failureCodes.add(HttpURLConnection.HTTP_FORBIDDEN);

        return failureCodes;
    }

}
