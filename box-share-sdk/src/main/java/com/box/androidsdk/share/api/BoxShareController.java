package com.box.androidsdk.share.api;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by varungupta on 3/4/2016.
 */
public class BoxShareController implements ShareController {
    private BoxApiFolder mFolderApi;

    public BoxShareController(BoxApiFolder folderApi) {
        mFolderApi = folderApi;
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

    private static ThreadPoolExecutor mApiExecutor;

    protected ThreadPoolExecutor getApiExecutor() {
        if (mApiExecutor == null || mApiExecutor.isShutdown()) {
            mApiExecutor = new ThreadPoolExecutor(1, 1, 3600, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        }
        return mApiExecutor;
    }
}
