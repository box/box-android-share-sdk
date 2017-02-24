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

import java.util.Arrays;
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

    private String[] mFolderShareFields;
    private String[] mFileShareFields;
    private String[] mBookmarkShareFields;

    public BoxShareController(BoxSession session) {
        mSession = session;
        mFileApi = new BoxApiFile(session);
        mFolderApi = new BoxApiFolder(session);
        mBookmarkApi = new BoxApiBookmark(session);
        mCollabApi = new BoxApiCollaboration(session);
        mInviteeApi = new BoxApiInvitee(session);
        mFeaturesApi = new BoxApiFeatures(session);

        mFolderShareFields = initializeShareFieldsArray(BoxFolder.ALL_FIELDS);
        mFileShareFields = initializeShareFieldsArray(BoxFile.ALL_FIELDS);
        mBookmarkShareFields = initializeShareFieldsArray(BoxBookmark.ALL_FIELDS);
        mAvatarController = new DefaultAvatarController(session);
    }

    private String[] initializeShareFieldsArray(String[] originalFields) {
        String[] shareFieldsArray = Arrays.copyOf(originalFields, originalFields.length + 1);
        shareFieldsArray[originalFields.length] = BoxItem.FIELD_ALLOWED_SHARED_LINK_ACCESS_LEVELS;
        return shareFieldsArray;
    }

    /**
     * Gets information about given box item
     *
     * @param boxItem Box item object passed by the caller (eg. file, folder and bookmark)
     * @return an instance of BoxFutureTask that asynchronously executes a request to obtain
     *         info about a box item
     */
    @Override
    public BoxFutureTask<BoxItem> fetchItemInfo(BoxItem boxItem) {
        BoxRequest request = null;
        if (boxItem instanceof BoxFile) {
            request = mFileApi.getInfoRequest(boxItem.getId()).setFields(mFileShareFields);
        } else if (boxItem instanceof BoxFolder) {
            request = mFolderApi.getInfoRequest(boxItem.getId()).setFields(mFolderShareFields);
        } else if (boxItem instanceof BoxBookmark) {
            request = mBookmarkApi.getInfoRequest(boxItem.getId()).setFields(mBookmarkShareFields);
        }

        BoxFutureTask<BoxItem> task = new BoxFutureTask<BoxItem>(BoxItem.class, request);
        getApiExecutor().submit(task);
        return task;
    }

    /**
     * Gets shared link update request for given box item
     *
     * @param boxItem Box item object passed by the caller (eg. file, folder and bookmark)
     * @return The shared link update request
     */
    @Override
    public BoxRequestUpdateSharedItem getCreatedSharedLinkRequest(BoxItem boxItem){
        if (boxItem instanceof BoxFile) {
            return mFileApi.getCreateSharedLinkRequest(boxItem.getId()).setFields(mFileShareFields);
        } else if (boxItem instanceof BoxFolder) {
            return mFolderApi.getCreateSharedLinkRequest(boxItem.getId()).setFields(mFolderShareFields);
        } else if (boxItem instanceof BoxBookmark) {
            return mBookmarkApi.getCreateSharedLinkRequest(boxItem.getId()).setFields(mBookmarkShareFields);
        }
        // should never hit this scenario.
        return null;
    }

    /**
     * Gets the default shared link for an item
     *
     * @param boxItem Box item object passed by the caller (eg. file, folder and bookmark)
     * @return instance of BoxFutureTask that asynchronously executes a request to create
     *         default shared link
     */
    @Override
    public BoxFutureTask<BoxItem> createDefaultSharedLink(BoxItem boxItem) {
        BoxRequest request = getCreatedSharedLinkRequest(boxItem);
        return executeRequest(BoxItem.class, request);
    }

    /**
     * Disables share link for a box item
     *
     * @param boxItem Box item object passed by the caller (eg. file, folder and bookmark)
     * @return instance of BoxFutureTask that asynchronously executes a request to disable
     *         the shared link
     */
    @Override
    public BoxFutureTask<BoxItem> disableShareLink(BoxItem boxItem) {
        BoxRequest request = null;
        if (boxItem instanceof BoxFile) {
            request = mFileApi.getDisableSharedLinkRequest(boxItem.getId()).setFields(mFileShareFields);
        } else if (boxItem instanceof BoxFolder) {
            request = mFolderApi.getDisableSharedLinkRequest(boxItem.getId()).setFields(mFolderShareFields);
        } else if (boxItem instanceof BoxBookmark) {
            request = mBookmarkApi.getDisableSharedLinkRequest(boxItem.getId()).setFields(mBookmarkShareFields);
        }

        return executeRequest(BoxItem.class, request);
    }

    /**
     * Gets the collaborations on a box folder
     *
     * @param boxFolder Box folder for which collaborations need to be fetched
     * @return instance of BoxFutureTask that asynchronously executes a request to fetch
     *         collaborations on box folder
     */
    @Override
    public BoxFutureTask<BoxIteratorCollaborations> fetchCollaborations(BoxFolder boxFolder) {
        BoxFutureTask<BoxIteratorCollaborations> task = mFolderApi
                .getCollaborationsRequest(boxFolder.getId()).toTask();
        getApiExecutor().submit(task);
        return task;
    }

    /**
     * Gets a list of roles allowed for folder collaboration invitees
     *
     * @param boxFolder Box folder for which roles need to be fetched
     * @return instance of BoxFutureTask that asynchronously executes a request to fetch roles
     *         of invitees for folder collaboration
     */
    @Override
    public BoxFutureTask<BoxFolder> fetchRoles(BoxFolder boxFolder) {
        BoxFutureTask<BoxFolder> task = mFolderApi.getInfoRequest(boxFolder.getId()).setFields(BoxFolder.FIELD_ALLOWED_INVITEE_ROLES).toTask();
        getApiExecutor().submit(task);
        return task;
    }

    /**
     * Updates collaboration with the new selected role
     *
     * @param collaboration The collaboration that needs to be updated
     * @param selectedRole New role selected for collaboration
     * @return instance of BoxFutureTask that asynchronously executes a request to update
     *         collaborations with new role
     */
    @Override
    public BoxFutureTask<BoxCollaboration> updateCollaboration(BoxCollaboration collaboration, BoxCollaboration.Role selectedRole) {
        BoxFutureTask<BoxCollaboration> task = mCollabApi.getUpdateRequest(collaboration.getId()).setNewRole(selectedRole).toTask();
        getApiExecutor().submit(task);
        return task;
    }

    /**
     * Changes role to owner given a collaboration object
     *
     * @param collaboration The collaboration that needs to be updated
     * @return instance of BoxFutureTask that asynchronously executes a request to update
     *         the collaboration role to owner
     */
    @Override
    public BoxFutureTask<BoxVoid> updateOwner(BoxCollaboration collaboration) {
        BoxFutureTask<BoxVoid> task = mCollabApi.getUpdateOwnerRequest(collaboration.getId()).toTask();
        getApiExecutor().submit(task);
        return task;
    }

    /**
     * Deletes the given collaboration
     *
     * @param collaboration Collaboration that needs to be deleted
     * @return instance of BoxFutureTask that asynchronously executes a request to delete the
     *         collaboration
     */
    @Override
    public BoxFutureTask<BoxVoid> deleteCollaboration(BoxCollaboration collaboration) {
        BoxFutureTask<BoxVoid> task = mCollabApi.getDeleteRequest(collaboration.getId()).toTask();
        getApiExecutor().submit(task);
        return task;
    }

    /**
     * Adds a list of users as collaborators to a folder by using their emails
     *
     * @param boxFolder Box Folder to be collaborated upon
     * @param selectedRole Role for folder collaboration
     * @param emails Emails of collaborators
     * @return instance of BoxFutureTask that asynchronously executes a batch request to add
     *         collaborators on a given folder
     */
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

    /**
     * Gets invitees for a given box folder
     *
     * @param boxFolder Given Box folder
     * @param filter Filter to fine tune the list of invitee, caller is looking for
     * @return instance of BoxFutureTask that asynchronously executes a request to get list of
     *         invitees for a given box folder
     */
    @Override
    public BoxFutureTask<BoxIteratorInvitees> getInvitees(BoxFolder boxFolder, String filter) {
        BoxFutureTask<BoxIteratorInvitees> task = mInviteeApi.getInviteesRequest(boxFolder.getId()).setFilterTerm(filter).toTask();
        getApiExecutor().submit(task);
        return task;
    }

    /**
     * Executes a request on given Box model object
     *
     * @param clazz Name of Box model class
     * @param request BoxRequest object
     * @return instance of BoxFutureTask that asynchronously executes a request to complete the task
     */
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

    /**
     * Get a list of supported features for the user
     *
     * @return instance of BoxFutureTask that asynchronously executes a request to fetch the supported features
     */
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
