package com.box.androidsdk.share.sharerepo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.design.widget.Snackbar;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxUser;
import com.box.androidsdk.content.requests.BoxRequestsShare;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;
import com.box.androidsdk.share.vm.DataWrapper;
import com.box.androidsdk.share.vm.InvitingCollabDataWrapper;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ShareRepo implements ShareRepoInterface {

    ShareController mController;

    private final MutableLiveData<DataWrapper<BoxIteratorInvitees>> mInvitees = new MutableLiveData<DataWrapper<BoxIteratorInvitees>>();

    private final MutableLiveData<DataWrapper<BoxCollaborationItem>> mShareItem = new MutableLiveData<DataWrapper<BoxCollaborationItem>>();

    private final MutableLiveData<InvitingCollabDataWrapper> mInvitingCollabsChecker = new MutableLiveData<InvitingCollabDataWrapper>();

    public ShareRepo(ShareController controller) {
        this.mController = controller;
    }

    @Override
    public LiveData<DataWrapper<BoxIteratorInvitees>> getInvitees(BoxCollaborationItem boxCollaborationItem, String filter) {
        final DataWrapper<BoxIteratorInvitees> data = new DataWrapper<BoxIteratorInvitees>();
        mController.getInvitees(boxCollaborationItem, filter).addOnCompletedListener(new BoxFutureTask.OnCompletedListener<BoxIteratorInvitees>() {
            @Override
            public void onCompleted(BoxResponse<BoxIteratorInvitees> response) {
                if (response.isSuccess()) {
                    final BoxIteratorInvitees invitees = response.getResult();
                    data.success(invitees);
                    mInvitees.postValue(data);
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
            }
        });
        mInvitees.postValue(data);
        return mInvitees;
    }

    @Override
    public LiveData<DataWrapper<BoxCollaborationItem>> fetchRoles(BoxCollaborationItem boxCollaborationItem) {
        final DataWrapper<BoxCollaborationItem> data = new DataWrapper<BoxCollaborationItem>();
        mController.fetchRoles(boxCollaborationItem).addOnCompletedListener(new BoxFutureTask.OnCompletedListener<BoxCollaborationItem>() {
            @Override
            public void onCompleted(BoxResponse<BoxCollaborationItem> response) {
                if (response.isSuccess()) {
                    BoxCollaborationItem collaborationItem = response.getResult();
                    data.success(collaborationItem);
                } else {
                    data.failure(R.string.box_sharesdk_network_error);
                }
            }
        });
        mShareItem.postValue(data);
        return mShareItem;
    }

    @Override
    public LiveData<InvitingCollabDataWrapper> addCollabs(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails) {
        final InvitingCollabDataWrapper data = new InvitingCollabDataWrapper();
        mController.addCollaborations(boxCollaborationItem, selectedRole, emails).addOnCompletedListener(new BoxFutureTask.OnCompletedListener<BoxResponseBatch>() {
            @Override
            public void onCompleted(BoxResponse<BoxResponseBatch> response) {
                data.setValues(handleCollaboratorsInvited(response.getResult()));
            }
        });
        mInvitingCollabsChecker.postValue(data);
        return mInvitingCollabsChecker;
    }

    private InvitingCollabDataWrapper handleCollaboratorsInvited(BoxResponseBatch responses) {
        int strCode;
        boolean mInvitationFailed;
        String subMssg = null;

        int alreadyAddedCount = 0;
        boolean didRequestFail = false;
        String name = "";
        List<String> failedCollaboratorsList = new ArrayList<String>();
        for (BoxResponse<BoxCollaboration> r : responses.getResponses()) {
            if (!r.isSuccess()) {
                HashSet<Integer> failureCodes = new HashSet<Integer>();
                failureCodes.add(HttpURLConnection.HTTP_BAD_REQUEST );
                failureCodes.add(HttpURLConnection.HTTP_FORBIDDEN);
                if (r.getException() instanceof BoxException && failureCodes.contains(((BoxException) r.getException()).getResponseCode())) {
                    String code = ((BoxException) r.getException()).getAsBoxError().getCode();
                    BoxUser user = (BoxUser) ((BoxRequestsShare.AddCollaboration) r.getRequest()).getAccessibleBy();
                    if (!SdkUtils.isBlank(code) && code.equals(BoxRequestsShare.AddCollaboration.ERROR_CODE_USER_ALREADY_COLLABORATOR)) {
                        alreadyAddedCount++;
                        name = user == null ? "" : user.getLogin();
                    } else {
                        failedCollaboratorsList.add(user == null ? "" : user.getLogin());
                    }
                }
                didRequestFail = true;
            }
        }

        if (didRequestFail) {
            if (!failedCollaboratorsList.isEmpty()) {
                StringBuilder collaborators = new StringBuilder();
                for (int i = 0; i < failedCollaboratorsList.size(); i++) {
                    collaborators.append(failedCollaboratorsList.get(i));
                    if (i < failedCollaboratorsList.size() - 1) {
                        collaborators.append(' ');
                    }
                }
                strCode = R.string.box_sharesdk_following_collaborators_error;
                subMssg = collaborators.toString();

            } else if (alreadyAddedCount == 1) {
                strCode = R.string.box_sharesdk_has_already_been_invited;
                subMssg = name;
            } else if (alreadyAddedCount > 1) {
                strCode = R.string.box_sharesdk_num_has_already_been_invited;
                subMssg = Integer.toString(alreadyAddedCount);
            } else {
                strCode = R.string.box_sharesdk_unable_to_invite;
            }
        } else {
            if (responses.getResponses().size() == 1) {
                BoxCollaboration collaboration = (BoxCollaboration) responses.getResponses().get(0).getResult();
                if (collaboration.getAccessibleBy() == null) {
                    strCode = R.string.box_sharesdk_collaborators_invited;
                } else {
                    String login = ((BoxUser)(collaboration).getAccessibleBy()).getLogin();
                    strCode = R.string.box_sharesdk_collaborator_invited;
                    subMssg = login;
                }

            } else {
                strCode = R.string.box_sharesdk_collaborators_invited;
            }
        }

        mInvitationFailed = (didRequestFail && !failedCollaboratorsList.isEmpty());

        return new InvitingCollabDataWrapper(mInvitationFailed, subMssg, strCode);
    }


}
