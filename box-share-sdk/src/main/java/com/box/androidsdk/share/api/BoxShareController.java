package com.box.androidsdk.share.api;

import com.box.androidsdk.content.BoxApiCollaboration;
import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxVoid;
import com.box.androidsdk.content.requests.BoxRequestBatch;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.internal.BoxApiInvitee;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by varungupta on 3/4/2016.
 */
public class BoxShareController implements ShareController {
    private BoxApiFolder mFolderApi;
    private BoxApiCollaboration mCollabApi;

    public BoxShareController(BoxApiFolder folderApi, BoxApiCollaboration collaborationApi) {
        mFolderApi = folderApi;
        mCollabApi = collaborationApi;
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

    private static ThreadPoolExecutor mApiExecutor;

    protected ThreadPoolExecutor getApiExecutor() {
        if (mApiExecutor == null || mApiExecutor.isShutdown()) {
            mApiExecutor = new ThreadPoolExecutor(1, 1, 3600, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        }
        return mApiExecutor;
    }
}
