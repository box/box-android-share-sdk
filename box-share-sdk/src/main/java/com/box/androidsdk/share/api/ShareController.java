package com.box.androidsdk.share.api;

import android.content.Context;

import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxObject;
import com.box.androidsdk.content.models.BoxVoid;
import com.box.androidsdk.content.requests.BoxRequest;
import com.box.androidsdk.content.requests.BoxRequestUpdateSharedItem;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.content.views.BoxAvatarView;
import com.box.androidsdk.share.internal.models.BoxFeatures;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;

import java.io.Serializable;

public interface ShareController {
    BoxFutureTask<BoxItem> fetchItemInfo(BoxItem boxItemr);
    BoxRequestUpdateSharedItem getCreatedSharedLinkRequest(BoxItem boxItem);
    BoxFutureTask<BoxItem> createDefaultSharedLink(BoxItem boxItem);
    BoxFutureTask<BoxItem> disableShareLink(BoxItem boxItemr);
    BoxFutureTask<BoxIteratorCollaborations> fetchCollaborations(BoxCollaborationItem boxCollaborationItem);
    BoxFutureTask<BoxCollaborationItem> fetchRoles(BoxCollaborationItem boxCollaborationItem);
    BoxFutureTask<BoxCollaboration> updateCollaboration(BoxCollaboration collaboration, BoxCollaboration.Role selectedRole);
    BoxFutureTask<BoxVoid> updateOwner(BoxCollaboration collaboration);
    BoxFutureTask<BoxVoid> deleteCollaboration(BoxCollaboration collaboration);
    BoxFutureTask<BoxResponseBatch> addCollaborations(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails);
    BoxFutureTask<BoxIteratorInvitees> getInvitees(BoxCollaborationItem boxCollaborationItem, String filter);
    <E extends BoxAvatarView.AvatarController & Serializable> E getAvatarController();
    <E extends BoxObject> BoxFutureTask<E> executeRequest(final Class<E> clazz, final BoxRequest request);
    void showToast(Context context, CharSequence text);
    void showToast(Context context, int resId);
    BoxFutureTask<BoxFeatures> getSupportedFeatures();
    String getCurrentUserId();
}
