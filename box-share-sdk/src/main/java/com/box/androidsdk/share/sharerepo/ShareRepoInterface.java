package com.box.androidsdk.share.sharerepo;

import android.arch.lifecycle.LiveData;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;

public interface ShareRepoInterface {
    public LiveData<BoxResponse<BoxIteratorInvitees>> getInvitees(BoxCollaborationItem boxCollaborationItem, String filter);
    public LiveData<BoxResponse<BoxCollaborationItem>> fetchRoles(BoxCollaborationItem boxCollaborationItem);
    public LiveData<BoxResponse<BoxResponseBatch>> addCollabs(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails);
}