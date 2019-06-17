package com.box.androidsdk.share.vm;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.support.annotation.Nullable;

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

public class InviteCollaboratorsVM extends BaseVM {

    private LiveData<DataWrapper<BoxCollaborationItem>> mFetchRoleItem;
    private LiveData<InviteCollaboratorsDataWrapper> mAddCollabItem;
    private LiveData<DataWrapper<BoxIteratorInvitees>> mInviteesItem;
    public InviteCollaboratorsVM(BaseShareRepo shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
        mFetchRoleItem = Transformations.map(shareRepo.getmFetchRoleItem(), new Function<BoxResponse<BoxCollaborationItem>, DataWrapper<BoxCollaborationItem>>() {
            @Override
            public DataWrapper<BoxCollaborationItem> apply(BoxResponse<BoxCollaborationItem> input) {
                return createFetchRoleItemData(input);
            }
        });
        mAddCollabItem = Transformations.map(shareRepo.getmInviteCollabBatch(), new Function<BoxResponse<BoxResponseBatch>, InviteCollaboratorsDataWrapper>() {
            @Override
            public InviteCollaboratorsDataWrapper apply(BoxResponse<BoxResponseBatch> input) {
                return createAddCollabItemData(input);
            }
        });
        mInviteesItem = Transformations.map(shareRepo.getmInvitees(), new Function<BoxResponse<BoxIteratorInvitees>, DataWrapper<BoxIteratorInvitees>>() {
            @Override
            public DataWrapper<BoxIteratorInvitees> apply(BoxResponse<BoxIteratorInvitees> input) {
                return createGetInviteesItemData(input);
            }
        });
    }

    /**
     * Fetch roles using the ShareRepo.
     * @param item the item to fetch roles on.
     */
    public void fetchRolesApi(BoxCollaborationItem item) {
        mShareRepo.fetchRolesApi(item);
    }

    /**
     *
     * @param boxCollaborationItem
     * @param selectedRole
     * @param emails
     */
    public void addCollabsApi(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails) {
        mShareRepo.addCollabsApi(boxCollaborationItem, selectedRole, emails);
    }

    public void getInviteesApi(BoxCollaborationItem boxCollaborationItem, String filter) {
        mShareRepo.getInviteesApi(boxCollaborationItem, filter);
    }



    public LiveData<DataWrapper<BoxCollaborationItem>> getmFetchRoleItem() {
        return mFetchRoleItem;
    }

    public LiveData<InviteCollaboratorsDataWrapper> getmAddCollabItem() {
        return mAddCollabItem;
    }

    public LiveData<DataWrapper<BoxIteratorInvitees>> getmInviteesItem() {
        return mInviteesItem;
    }

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

    private InviteCollaboratorsDataWrapper createAddCollabItemData(BoxResponse<BoxResponseBatch> response) {
        return handleCollaboratorsInvited(response.getResult());
    }

    private InviteCollaboratorsDataWrapper handleCollaboratorsInvited(BoxResponseBatch responses) {
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

        return new InviteCollaboratorsDataWrapper(mInvitationFailed, subMssg, strCode);
    }

}
