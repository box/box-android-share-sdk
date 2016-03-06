package com.box.androidsdk.share.api;

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
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.content.models.BoxVoid;
import com.box.androidsdk.content.requests.BoxRequest;
import com.box.androidsdk.content.requests.BoxRequestBatch;
import com.box.androidsdk.content.requests.BoxRequestUpdateSharedItem;
import com.box.androidsdk.content.requests.BoxRequestsFile;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.internal.BoxApiInvitee;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by varungupta on 3/4/2016.
 */
public class BoxShareController implements ShareController {
    private BoxApiFile mFileApi;
    private BoxApiFolder mFolderApi;
    private BoxApiBookmark mBookmarkApi;
    private BoxApiCollaboration mCollabApi;

    public BoxShareController(BoxApiFile fileApi, BoxApiFolder folderApi, BoxApiBookmark bookmarkApi, BoxApiCollaboration collaborationApi) {
        mFileApi = fileApi;
        mFolderApi = folderApi;
        mBookmarkApi = bookmarkApi;
        mCollabApi = collaborationApi;
    }

    @Override
    public void fetchItemInfo(BoxItem boxItem, BoxFutureTask.OnCompletedListener<BoxItem> onCompletedListener) {
        BoxRequest request = null;
        if (boxItem instanceof BoxFile) {
            request = mFileApi.getInfoRequest(boxItem.getId());
        } else if (boxItem instanceof BoxFolder) {
            request = mFolderApi.getInfoRequest(boxItem.getId());
        } else if (boxItem instanceof BoxBookmark) {
            request = mBookmarkApi.getInfoRequest(boxItem.getId());
        }

        BoxFutureTask<BoxItem> task = new BoxFutureTask<BoxItem>(BoxItem.class, request);
        task.addOnCompletedListener(onCompletedListener);
        getApiExecutor().submit(task);
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
    public void createDefaultSharedLink(BoxItem boxItem, BoxFutureTask.OnCompletedListener<BoxItem> onCompletedListener) {
        BoxRequest request = getCreatedSharedLinkRequest(boxItem);
        executeRequest(BoxItem.class, request, onCompletedListener);
    }

    @Override
    public void disableShareLink(BoxItem boxItem, BoxFutureTask.OnCompletedListener<BoxItem> onCompletedListener) {
        BoxRequest request = null;
        if (boxItem instanceof BoxFile) {
            request = mFileApi.getDisableSharedLinkRequest(boxItem.getId()).setFields(BoxFile.ALL_FIELDS);
        } else if (boxItem instanceof BoxFolder) {
            request = mFolderApi.getDisableSharedLinkRequest(boxItem.getId()).setFields(BoxFolder.ALL_FIELDS);
        } else if (boxItem instanceof BoxBookmark) {
            request = mBookmarkApi.getDisableSharedLinkRequest(boxItem.getId()).setFields(BoxBookmark.ALL_FIELDS);
        }

        executeRequest(BoxItem.class, request, onCompletedListener);
    }

    @Override
    public void fetchCollaborations(BoxFolder boxFolder, BoxFutureTask.OnCompletedListener<BoxIteratorCollaborations> onCompletedListener) {
        BoxFutureTask<BoxIteratorCollaborations> task = mFolderApi
                .getCollaborationsRequest(boxFolder.getId()).toTask();
        task.addOnCompletedListener(onCompletedListener);
        getApiExecutor().submit(task);
    }

    @Override
    public void fetchRoles(BoxFolder boxFolder, BoxFutureTask.OnCompletedListener<BoxFolder> onCompletedListener) {
        BoxFutureTask<BoxFolder> task = mFolderApi.getInfoRequest(boxFolder.getId()).setFields(BoxFolder.FIELD_ALLOWED_INVITEE_ROLES).toTask();
        task.addOnCompletedListener(onCompletedListener);
        getApiExecutor().submit(task);
    }

    @Override
    public void updateCollaboration(BoxCollaboration collaboration, BoxCollaboration.Role selectedRole, BoxFutureTask.OnCompletedListener<BoxCollaboration> onCompletedListener) {
        BoxFutureTask<BoxCollaboration> task = mCollabApi.getUpdateRequest(collaboration.getId()).setNewRole(selectedRole).toTask();
        task.addOnCompletedListener(onCompletedListener);
        getApiExecutor().submit(task);
    }

    @Override
    public void deleteCollaboration(BoxCollaboration collaboration, BoxFutureTask.OnCompletedListener<BoxVoid> onCompletedListener) {
        BoxFutureTask<BoxVoid> task = mCollabApi.getDeleteRequest(collaboration.getId()).toTask();
        task.addOnCompletedListener(onCompletedListener);
        getApiExecutor().submit(task);
    }

    @Override
    public void addCollaborations(BoxFolder boxFolder, BoxCollaboration.Role selectedRole, String[] emails, BoxFutureTask.OnCompletedListener<BoxResponseBatch> onCompletedListener) {
        BoxRequestBatch batchRequest = new BoxRequestBatch();
        for (String email: emails) {
            String trimmedEmail = email.trim();
            if (!SdkUtils.isBlank(trimmedEmail)) {
                batchRequest.addRequest(mCollabApi.getAddRequest(boxFolder.getId(), selectedRole, trimmedEmail));
            }
        }

        BoxFutureTask<BoxResponseBatch> task = batchRequest.toTask();
        task.addOnCompletedListener(onCompletedListener);
        getApiExecutor().submit(task);
    }

    @Override
    public <E extends BoxObject> void executeRequest(Class<E> clazz, BoxRequest request, BoxFutureTask.OnCompletedListener<E> onCompletedListener) {
        BoxFutureTask<E> task = new BoxFutureTask<E>(clazz, request);
        task.addOnCompletedListener(onCompletedListener);
        getApiExecutor().submit(task);
    }

    private static ThreadPoolExecutor mApiExecutor;

    protected ThreadPoolExecutor getApiExecutor() {
        if (mApiExecutor == null || mApiExecutor.isShutdown()) {
            mApiExecutor = new ThreadPoolExecutor(1, 1, 3600, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        }
        return mApiExecutor;
    }
}
