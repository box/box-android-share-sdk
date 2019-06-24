package com.box.androidsdk.share.vm;

import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxUser;
import com.box.androidsdk.content.requests.BoxRequestsShare;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.R;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A special extension of PresenterData to hold data for inviting a new Collaborator
 */
public class InviteCollaboratorsPresenterData extends PresenterData<String> {

    private boolean mInvitationFailed;
    private int mAlreadyAddedCount;
    private boolean mShowSnackbar;

    private static HashSet<Integer> failureCodes;

    static {
        failureCodes = getFailureCodes();
    }

    public InviteCollaboratorsPresenterData() {

    }

    /**
     * A constructor created to help with a successful request
     * @param data the data to display
     * @param strCode the string resource code for the message
     */
    public InviteCollaboratorsPresenterData(String data, @StringRes @PluralsRes int strCode) {
        super(data, strCode);
        this.mInvitationFailed = false;
        this.mAlreadyAddedCount = 0;
        this.mShowSnackbar = false;
    }

    public InviteCollaboratorsPresenterData(String data, int strCode, boolean invitationFailed, int alreadyAddedCount, boolean showSnackbar) {
        super(data, strCode);
        this.mInvitationFailed = invitationFailed;
        this.mAlreadyAddedCount = alreadyAddedCount;
        this.mShowSnackbar = showSnackbar;
    }

    public boolean showSnackBar() {
        return mShowSnackbar;
    }

    /**
     * Returns true if all invitations succeeded.
     * @return true if all invitations succeeded
     */
    @Override
    public boolean isSuccess() {
        return !mInvitationFailed;
    }

    /**
     * Returns true if the String formatted part exists.
     * @return true if the String formatted part exists
     */
    public boolean hasSubMessage() {
        return getData() != null;
    }

    /**
     * Helper method for transforming BoxResponse to UI Model for addCollabsApi
     * @param response the response to transform
     * @return the transformed model
     */
    public static InviteCollaboratorsPresenterData getPresenterDataFromBoxResponse(BoxResponse<BoxResponseBatch> response) {
        return getPresenterData(response.getResult());
    }

    /**
     * Helper method for helping with transformation of BoxBatchResponse to UI Model
     * @param responses the batch response to transform
     * @return the transformed model
     */
    private static InviteCollaboratorsPresenterData getPresenterData(BoxResponseBatch responses) {
        int alreadyAddedCount = 0;
        boolean didRequestSuceed = true;
        String name = "";

        List<String> failedCollaboratorsList = new ArrayList<>();
        for (BoxResponse<BoxCollaboration> r : responses.getResponses()) {
            if (!r.isSuccess()) {
                if (checkIfKnownFailure(r, failureCodes)) {
                    String code = ((BoxException) r.getException()).getAsBoxError().getCode();
                    BoxUser user = (BoxUser) ((BoxRequestsShare.AddCollaboration) r.getRequest()).getAccessibleBy();
                    name = user == null ? "" : user.getLogin();
                    if (alreadyAddedFailure(code)) {
                        alreadyAddedCount++;
                    }
                }
                didRequestSuceed = false;
            }
        }

        if (didRequestSuceed) {
            return getPresenterDataForSuccessfulRequest(responses);
        } else {
            return getPresenterDataforFailedRequest(failedCollaboratorsList, name, alreadyAddedCount);
        }
    }

    /**
     * Returns a UI Model of a Box Response for Inviting new Collaborators for a successful request.
     * @param responses the responses that was successful
     * @return a UI Model of a Box Response for Inviting new Collaborators for a successful request
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
     * Returns a UI Model of a Box Response for Inviting new Collaborators for a failed request.
     * @param failedCollaboratorsList the list of collaborators for whom requests were not successful
     * @param name the name of a collaborator that is already added
     * @param alreadyAddedCount how many collaborators were already added
     * @return a UI Model of a Box Response for Inviting new Collaborators for a failed request
     */
    @VisibleForTesting
    static InviteCollaboratorsPresenterData getPresenterDataforFailedRequest(List<String> failedCollaboratorsList, String name, int alreadyAddedCount) {
        char divider = ' ';
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
            return new InviteCollaboratorsPresenterData(name, R.plurals.already_been_invited, false, alreadyAddedCount, false);
        }  else {
            return new InviteCollaboratorsPresenterData(null, R.string.box_sharesdk_unable_to_invite, true, alreadyAddedCount, false);
        }
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
