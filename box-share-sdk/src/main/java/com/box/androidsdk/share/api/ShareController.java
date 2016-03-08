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

import java.util.Date;

/**
 * Created by varungupta on 3/4/2016.
 */
public interface ShareController {
    void fetchItemInfo(BoxItem boxItem, BoxFutureTask.OnCompletedListener<BoxItem> onCompletedListener);
    BoxRequestUpdateSharedItem getCreatedSharedLinkRequest(BoxItem boxItem);
    void createDefaultSharedLink(BoxItem boxItem, BoxFutureTask.OnCompletedListener<BoxItem> onCompletedListener);
    void disableShareLink(BoxItem boxItem, BoxFutureTask.OnCompletedListener<BoxItem> onCompletedListener);
    void fetchCollaborations(BoxFolder boxFolder, BoxFutureTask.OnCompletedListener<BoxIteratorCollaborations> onCompletedListener);
    void fetchRoles(BoxFolder boxFolder, BoxFutureTask.OnCompletedListener<BoxFolder> onCompletedListener);
    void updateCollaboration(BoxCollaboration collaboration, BoxCollaboration.Role selectedRole, BoxFutureTask.OnCompletedListener<BoxCollaboration> onCompletedListener);
    void deleteCollaboration(BoxCollaboration collaboration, BoxFutureTask.OnCompletedListener<BoxVoid> onCompletedListener);
    void addCollaborations(BoxFolder boxFolder, BoxCollaboration.Role selectedRole, String[] emails, BoxFutureTask.OnCompletedListener<BoxResponseBatch> onCompletedListener);
    <E extends BoxObject> void executeRequest(final Class<E> clazz, final BoxRequest request, BoxFutureTask.OnCompletedListener<E> onCompletedListener);
    void showToast(Context context, CharSequence text);
    void showToast(Context context, int resId);
}
