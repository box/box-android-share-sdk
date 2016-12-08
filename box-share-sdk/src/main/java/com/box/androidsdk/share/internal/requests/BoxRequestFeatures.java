package com.box.androidsdk.share.internal.requests;

/**
 * Request to get supported features for the user
 */

import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.requests.BoxRequest;
import com.box.androidsdk.internal.BoxInternalApi;
import com.box.androidsdk.share.internal.models.BoxFeatures;

/**
 * Request to get supported features for the user.
 */
public class  BoxRequestFeatures extends BoxRequest<BoxFeatures, BoxRequestFeatures>  {
    private static final long serialVersionUID = 972964042278973942L;

    /**
     * Creates a get invitees request with the below parameters.
     *
     * @param session the authenticated session that will be used to make the request with.
     */
    public BoxRequestFeatures(BoxSession session) {
        super(BoxFeatures.class, BoxInternalApi.FEATURES_URI, session);
        mRequestMethod = Methods.GET;
    }

}