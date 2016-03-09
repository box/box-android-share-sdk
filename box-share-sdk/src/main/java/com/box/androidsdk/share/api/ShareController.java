package com.box.androidsdk.share.api;

import android.content.Context;

import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxObject;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.content.models.BoxVoid;
import com.box.androidsdk.content.requests.BoxRequest;
import com.box.androidsdk.content.requests.BoxRequestUpdateSharedItem;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.share.internal.models.BoxListInvitees;

import java.util.Date;

/**
 * Created by varungupta on 3/4/2016.
 */
public interface ShareController {
    BoxFutureTask<BoxItem> fetchItemInfo(BoxItem boxItemr);
    BoxRequestUpdateSharedItem getCreatedSharedLinkRequest(BoxItem boxItem);
    BoxFutureTask<BoxItem> createDefaultSharedLink(BoxItem boxItem);
    BoxFutureTask<BoxItem> disableShareLink(BoxItem boxItemr);
    BoxFutureTask<BoxIteratorCollaborations> fetchCollaborations(BoxFolder boxFolder);
    BoxFutureTask<BoxFolder> fetchRoles(BoxFolder boxFolder);
    BoxFutureTask<BoxCollaboration> updateCollaboration(BoxCollaboration collaboration, BoxCollaboration.Role selectedRole);
    BoxFutureTask<BoxVoid> deleteCollaboration(BoxCollaboration collaboration);
    BoxFutureTask<BoxResponseBatch> addCollaborations(BoxFolder boxFolder, BoxCollaboration.Role selectedRole, String[] emails);
    BoxFutureTask<BoxListInvitees> getInvitees(BoxFolder boxFolder);
    <E extends BoxObject> BoxFutureTask<E> executeRequest(final Class<E> clazz, final BoxRequest request);
    void showToast(Context context, CharSequence text);
    void showToast(Context context, int resId);
}
