package com.box.androidsdk.share.sharerepo;

import android.arch.lifecycle.LiveData;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;
import com.box.androidsdk.share.vm.DataWrapper;
import com.box.androidsdk.share.vm.InvitingCollabDataWrapper;

public interface ShareRepoInterface {
    public LiveData<DataWrapper<BoxIteratorInvitees>> getInvitees(BoxCollaborationItem boxCollaborationItem, String filter);
    public LiveData<DataWrapper<BoxCollaborationItem>> fetchRoles(BoxCollaborationItem boxCollaborationItem);
    public LiveData<InvitingCollabDataWrapper> addCollabs(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails);
}
