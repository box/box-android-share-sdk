package com.box.androidsdk.share.internal.requests;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.requests.BoxCacheableRequest;
import com.box.androidsdk.content.requests.BoxRequestList;
import com.box.androidsdk.share.internal.models.BoxListInvitees;


public class BoxRequestsInvitee {
    /**
     * Request to get a collection's items.
     */
    public static class GetInvitees extends BoxRequestList<BoxListInvitees, GetInvitees> implements BoxCacheableRequest<BoxListInvitees> {
        private static final long serialVersionUID = 972965042279973942L;

        /**
         * Creates a get collection items with the default parameters.
         *
         * @param id id of the collection
         * @param url URL of the collection items endpoint.
         * @param session   the authenticated session that will be used to make the request with.
         */
        public GetInvitees(String id, String url, BoxSession session) {
            super(BoxListInvitees.class, id, url, session);
        }

        @Override
        public BoxListInvitees sendForCachedResult() throws BoxException {
            return handleSendForCachedResult();
        }

        @Override
        public BoxFutureTask<BoxListInvitees> toTaskForCachedResult() throws BoxException {
            return handleToTaskForCachedResult();
        }
    }
}
