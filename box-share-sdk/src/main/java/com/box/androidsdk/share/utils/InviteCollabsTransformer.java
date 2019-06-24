package com.box.androidsdk.share.utils;

import androidx.annotation.VisibleForTesting;

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
import com.box.androidsdk.share.vm.InviteCollaboratorsPresenterData;
import com.box.androidsdk.share.vm.PresenterData;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A utility class for transforming BoxResponses into PresenterData for InviteCollaboratorsShareVM.
 */
public class InviteCollabsTransformer {

    private static HashSet<Integer> failureCodes;
    private static char divider = ' ';
    static {
        failureCodes = getFailureCodes();
    }


    /**
     * Helper method for transforming BoxResponse to UI Model for fetchRoleApi.
     * @param response the response to transform on
     * @return the transformed data
     */
    public static PresenterData<BoxCollaborationItem> getFetchRolesItemPresenterData(BoxResponse<BoxCollaborationItem> response) {
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
     * Helper method for transforming BoxResponse to UI Model for getInviteesApi.
     * @param response the response to transform
     * @return the transformed model
     */
    public static PresenterData<BoxIteratorInvitees> getInviteesPresenterData(BoxResponse<BoxIteratorInvitees> response) {
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
     * Helper method for transforming BoxResponse to UI Model for inviteCollabsApi.
     * @param response the response to transform
     * @return the transformed model
     */
    public static InviteCollaboratorsPresenterData getInviteCollabsPresenterDataFromBoxResponse(BoxResponse<BoxResponseBatch> response) {
        return getInviteCollabsPresenterData(response.getResult());
    }

    /**
     * Helper method for transforming BoxBatchResponse to UI Model for inviting collabs.
     * @param responses the batch response to transform
     * @return the transformed model
     */
    private static InviteCollaboratorsPresenterData getInviteCollabsPresenterData(BoxResponseBatch responses) {
        int alreadyAddedCount = 0;
        boolean didRequestSuceed = true;
        String name = "";

        List<String> failedCollaboratorsList = new ArrayList<>();
        for (BoxResponse<BoxCollaboration> r : responses.getResponses()) {
            if (!r.isSuccess()) {
                if (isKnownFailure(r, failureCodes)) {
                    String code = ((BoxException) r.getException()).getAsBoxError().getCode();
                    BoxUser user = (BoxUser) ((BoxRequestsShare.AddCollaboration) r.getRequest()).getAccessibleBy();
                    name = user == null ? "" : user.getLogin();
                    if (isAlreadyAddedFailure(code)) {
                        alreadyAddedCount++;
                    } else {
                        failedCollaboratorsList.add(name);
                    }
                }
                didRequestSuceed = false;
            }
        }

        if (didRequestSuceed) {
            return getPresenterDataForSuccessfulRequest(responses);
        } else {
            return getPresenterDataForFailedRequest(failedCollaboratorsList, name, alreadyAddedCount);
        }
    }

    /**
     * Returns a UI Model of a Box Response for inviting new collaborators for a successful request.
     * @param responses the responses that was successful
     * @return a UI Model of a Box Response for inviting new collaborators for a successful request
     */
    @VisibleForTesting
    static InviteCollaboratorsPresenterData getPresenterDataForSuccessfulRequest(BoxResponseBatch responses) {
        if (responses.getResponses().size() == 1) {
            BoxCollaboration collaboration = (BoxCollaboration) responses.getResponses().get(0).getResult();
            if (collaboration.getAccessibleBy() == null) {
                return new InviteCollaboratorsPresenterData(null, R.string.box_sharesdk_collaborators_invited);
            } else {
                String name = ((BoxUser)(collaboration).getAccessibleBy()).getLogin();
                return new InviteCollaboratorsPresenterData(name, R.string.box_sharesdk_collaborator_invited);
            }

        } else {
            return new InviteCollaboratorsPresenterData(null, R.string.box_sharesdk_collaborators_invited);
        }
    }
    /**
     * Returns a UI Model of a Box Response for inviting new collaborators for a failed request.
     * @param failedCollaboratorsList the list of collaborators for whom requests were not successful
     * @param name the name of a collaborator that is already added
     * @param alreadyAddedCount how many collaborators were already added
     * @return a UI Model of a Box Response for inviting new collaborators for a failed request
     */
    @VisibleForTesting
    static InviteCollaboratorsPresenterData getPresenterDataForFailedRequest(List<String> failedCollaboratorsList, String name, int alreadyAddedCount) {
        if (!failedCollaboratorsList.isEmpty()) {
            StringBuilder collaborators = new StringBuilder();
            for (int i = 0; i < failedCollaboratorsList.size(); i++) {
                collaborators.append(failedCollaboratorsList.get(i));
                if (i < failedCollaboratorsList.size() - 1) {
                    collaborators.append(divider);
                }
            }
            return new InviteCollaboratorsPresenterData(collaborators.toString(), R.string.box_sharesdk_following_collaborators_error, true, alreadyAddedCount, true);

        } else if (alreadyAddedCount >= 1) {
            return new InviteCollaboratorsPresenterData(name, R.plurals.box_sharesdk_already_been_invited, false, alreadyAddedCount, false);
        }  else {
            return new InviteCollaboratorsPresenterData(null, R.string.box_sharesdk_unable_to_invite, true, alreadyAddedCount, false);
        }
    }

    private static boolean isAlreadyAddedFailure(String code) {
        return !SdkUtils.isBlank(code) && code.equals(BoxRequestsShare.AddCollaboration.ERROR_CODE_USER_ALREADY_COLLABORATOR);
    }

    private static boolean isKnownFailure(BoxResponse<BoxCollaboration> r, HashSet<Integer> failureCodes) {
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
