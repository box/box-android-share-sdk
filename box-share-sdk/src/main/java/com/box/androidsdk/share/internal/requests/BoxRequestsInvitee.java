package com.box.androidsdk.share.internal.requests;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.requests.BoxCacheableRequest;
import com.box.androidsdk.content.requests.BoxRequestList;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;


public class BoxRequestsInvitee {
    /**
     * Request to get a collection's items.
     */
    public static class GetInvitees extends BoxRequestList<BoxIteratorInvitees, GetInvitees> implements BoxCacheableRequest<BoxIteratorInvitees> {
        private static final long serialVersionUID = 972965042279973942L;
        private static final String FIELD_FILTER_TERM = "filter_term";

        /**
         * Creates a get invitees request with the below parameters.
         *
         * @param id id of the collection
         * @param url URL of the collection items endpoint.
         * @param session the authenticated session that will be used to make the request with.
         */
        public GetInvitees(String id, String url, BoxSession session) {
            super(BoxIteratorInvitees.class, id, url, session);
        }

        @Override
        public BoxIteratorInvitees sendForCachedResult() throws BoxException {
            return handleSendForCachedResult();
        }

        @Override
        public BoxFutureTask<BoxIteratorInvitees> toTaskForCachedResult() throws BoxException {
            return handleToTaskForCachedResult();
        }

        public GetInvitees setFilterTerm(String filterTerm) {
            mQueryMap.put(FIELD_FILTER_TERM, filterTerm);
            return this;
        }
    }
}
