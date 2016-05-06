package com.box.androidsdk.share.internal;

import com.box.androidsdk.content.BoxApi;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.internal.BoxInternalApi;
import com.box.androidsdk.share.internal.requests.BoxRequestsInvitee;

public class BoxApiInvitee extends BoxApi {
    public static int LIMIT = 1000;
    /**
     * Constructs a BoxApiInvitee with the provided BoxSession
     *
     * @param session authenticated session to use with the BoxApiInvitee
     */
    public BoxApiInvitee(BoxSession session) {
        super(session);
    }

    public BoxRequestsInvitee.GetInvitees getInviteesRequest(final String id) {
        BoxRequestsInvitee.GetInvitees request = new BoxRequestsInvitee.GetInvitees(id, BoxInternalApi.getInvitesUri(getBaseUri(), id), mSession);
        request.setLimit(LIMIT);
        return request;
    }
}
