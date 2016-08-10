package com.box.androidsdk.share.api;

import android.content.Context;
import android.widget.Toast;

import com.box.androidsdk.content.BoxApiBookmark;
import com.box.androidsdk.content.BoxApiCollaboration;
import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxBookmark;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxObject;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.models.BoxVoid;
import com.box.androidsdk.content.requests.BoxRequest;
import com.box.androidsdk.content.requests.BoxRequestBatch;
import com.box.androidsdk.content.requests.BoxRequestUpdateSharedItem;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.content.views.BoxAvatarView;
import com.box.androidsdk.content.views.DefaultAvatarController;
import com.box.androidsdk.share.internal.BoxApiFeatures;
import com.box.androidsdk.share.internal.BoxApiInvitee;
import com.box.androidsdk.share.internal.models.BoxFeatures;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BoxShareController implements ShareController {
    private BoxApiFile mFileApi;
    private BoxApiFolder mFolderApi;
    private BoxApiBookmark mBookmarkApi;
    private BoxApiCollaboration mCollabApi;
    private BoxApiInvitee mInviteeApi;
    private BoxSession mSession;
    private BoxApiFeatures mFeaturesApi;
    private DefaultAvatarController mAvatarController;

    public BoxShareController(BoxSession session) {
        mSession = session;
        mFileApi = new BoxApiFile(session);
        mFolderApi = new BoxApiFolder(session);
        mBookmarkApi = new BoxApiBookmark(session);
        mCollabApi = new BoxApiCollaboration(session);
        mInviteeApi = new BoxApiInvitee(session);
        mFeaturesApi = new BoxApiFeatures(session);
        mAvatarController = new DefaultAvatarController(session);
    }

    @Override
    public BoxFutureTask<BoxItem> fetchItemInfo(BoxItem boxItem) {
        BoxRequest request = null;
        if (boxItem instanceof BoxFile) {
            request = mFileApi.getInfoRequest(boxItem.getId());
        } else if (boxItem instanceof BoxFolder) {
            request = mFolderApi.getInfoRequest(boxItem.getId());
        } else if (boxItem instanceof BoxBookmark) {
            request = mBookmarkApi.getInfoRequest(boxItem.getId());
        }

        BoxFutureTask<BoxItem> task = new BoxFutureTask<BoxItem>(BoxItem.class, request);
        getApiExecutor().submit(task);
        return task;
    }

    /**
     * Gets the request to create a shared link with
     *
     * @return the shared link update request
     */
    @Override
    public BoxRequestUpdateSharedItem getCreatedSharedLinkRequest(BoxItem boxItem){
        if (boxItem instanceof BoxFile) {
            return mFileApi.getCreateSharedLinkRequest(boxItem.getId()).setFields(BoxFile.ALL_FIELDS);
        } else if (boxItem instanceof BoxFolder) {
            return mFolderApi.getCreateSharedLinkRequest(boxItem.getId()).setFields(BoxFolder.ALL_FIELDS);
        } else if (boxItem instanceof BoxBookmark) {
            return mBookmarkApi.getCreateSharedLinkRequest(boxItem.getId()).setFields(BoxBookmark.ALL_FIELDS);
        }
        // should never hit this scenario.
        return null;
    }

    @Override
    public BoxFutureTask<BoxItem> createDefaultSharedLink(BoxItem boxItem) {
        BoxRequest request = getCreatedSharedLinkRequest(boxItem);
        return executeRequest(BoxItem.class, request);
    }

    @Override
    public BoxFutureTask<BoxItem> disableShareLink(BoxItem boxItem) {
        BoxRequest request = null;
        if (boxItem instanceof BoxFile) {
            request = mFileApi.getDisableSharedLinkRequest(boxItem.getId()).setFields(BoxFile.ALL_FIELDS);
        } else if (boxItem instanceof BoxFolder) {
            request = mFolderApi.getDisableSharedLinkRequest(boxItem.getId()).setFields(BoxFolder.ALL_FIELDS);
        } else if (boxItem instanceof BoxBookmark) {
            request = mBookmarkApi.getDisableSharedLinkRequest(boxItem.getId()).setFields(BoxBookmark.ALL_FIELDS);
        }

        return executeRequest(BoxItem.class, request);
    }

    @Override
    public BoxFutureTask<BoxIteratorCollaborations> fetchCollaborations(BoxFolder boxFolder) {
        BoxFutureTask<BoxIteratorCollaborations> task = mFolderApi
                .getCollaborationsRequest(boxFolder.getId()).toTask();
        getApiExecutor().submit(task);
        return task;
    }

    @Override
    public BoxFutureTask<BoxFolder> fetchRoles(BoxFolder boxFolder) {
        BoxFutureTask<BoxFolder> task = mFolderApi.getInfoRequest(boxFolder.getId()).setFields(BoxFolder.FIELD_ALLOWED_INVITEE_ROLES).toTask();
        getApiExecutor().submit(task);
        return task;
    }

    @Override
    public BoxFutureTask<BoxCollaboration> updateCollaboration(BoxCollaboration collaboration, BoxCollaboration.Role selectedRole) {
        BoxFutureTask<BoxCollaboration> task = mCollabApi.getUpdateRequest(collaboration.getId()).setNewRole(selectedRole).toTask();
        getApiExecutor().submit(task);
        return task;
    }

    @Override
    public BoxFutureTask<BoxVoid> updateOwner(BoxCollaboration collaboration) {
        BoxFutureTask<BoxVoid> task = mCollabApi.getUpdateOwnerRequest(collaboration.getId()).toTask();
        getApiExecutor().submit(task);
        return task;
    }

    @Override
    public BoxFutureTask<BoxVoid> deleteCollaboration(BoxCollaboration collaboration) {
        BoxFutureTask<BoxVoid> task = mCollabApi.getDeleteRequest(collaboration.getId()).toTask();
        getApiExecutor().submit(task);
        return task;
    }

    @Override
    public BoxFutureTask<BoxResponseBatch> addCollaborations(BoxFolder boxFolder, BoxCollaboration.Role selectedRole, String[] emails) {
        BoxRequestBatch batchRequest = new BoxRequestBatch();
        for (String email: emails) {
            String trimmedEmail = email.trim();
            if (!SdkUtils.isBlank(trimmedEmail)) {
                batchRequest.addRequest(mCollabApi.getAddRequest(boxFolder.getId(), selectedRole, trimmedEmail));
            }
        }

        BoxFutureTask<BoxResponseBatch> task = batchRequest.toTask();
        getApiExecutor().submit(task);
        return task;
    }

    @Override
    public BoxFutureTask<BoxIteratorInvitees> getInvitees(BoxFolder boxFolder, String filter) {
        BoxFutureTask<BoxIteratorInvitees> task = mInviteeApi.getInviteesRequest(boxFolder.getId()).setFilterTerm(filter).toTask();
        getApiExecutor().submit(task);
        return task;
    }

    @Override
    public <E extends BoxObject> BoxFutureTask<E> executeRequest(Class<E> clazz, BoxRequest request) {
        BoxFutureTask<E> task = new BoxFutureTask<E>(clazz, request);
        getApiExecutor().submit(task);
        return task;
    }

    @Override
    public void showToast(Context context, CharSequence text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showToast(Context context, int resId) {
        showToast(context, context.getResources().getText(resId));
    }

    @Override
    public BoxFutureTask<BoxFeatures> getSupportedFeatures() {
        BoxFutureTask<BoxFeatures> task = mFeaturesApi.getSupportedFeatures().toTask();
        getApiExecutor().submit(task);
        return task;
    }

    @Override
    public String getCurrentUserId() {
        return mSession.getUserId();
    }

    @Override
    public <E extends BoxAvatarView.AvatarController & Serializable> E getAvatarController() {
        return (E)mAvatarController;
    }

    private static ThreadPoolExecutor mApiExecutor;

    protected ThreadPoolExecutor getApiExecutor() {
        if (mApiExecutor == null || mApiExecutor.isShutdown()) {
            mApiExecutor = new ThreadPoolExecutor(1, 1, 3600, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        }
        return mApiExecutor;
    }
}
