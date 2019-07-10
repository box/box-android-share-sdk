package com.box.androidsdk.share.utils;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.requests.BoxRequestItem;
import com.box.androidsdk.content.requests.BoxRequestUpdateSharedItem;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.vm.PresenterData;

import java.net.HttpURLConnection;

public class SharedLinkTransformer {

    /**
     * Helper method for transforming BoxResponse to UI Model for shared link operations.
     * @param response
     * @return
     */
    public PresenterData<BoxCollaborationItem> getSharedLinkItem(BoxResponse<BoxCollaborationItem> response) {
        final PresenterData<BoxCollaborationItem> data = new PresenterData<BoxCollaborationItem>();
        if (response.isSuccess()) {
            data.success(response.getResult());
        } else {
            if (response.getException() instanceof BoxException) {
                BoxException boxException = (BoxException) response.getException();
                int responseCode = boxException.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                    data.setException(response.getException());
                } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                    data.failure(R.string.box_sharesdk_insufficient_permissions, boxException);
                }
            }

            if (response.getRequest() instanceof BoxRequestItem) {
                if (response.getRequest() instanceof BoxRequestUpdateSharedItem) {
                    data.failure(R.string.box_sharesdk_unable_to_modify_toast, response.getException());
                } else {
                    data.failure(R.string.box_sharesdk_problem_accessing_this_shared_link, response.getException());
                }
            } else {
                data.setException(response.getException());
            }
        }
        return data;
    }
}
