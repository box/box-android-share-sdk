package com.box.androidsdk.share.internal;

import com.box.androidsdk.content.BoxApi;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.share.internal.requests.BoxRequestFeatures;


public class BoxApiFeatures extends BoxApi {
    /**
     * Constructs a BoxApi with the provided BoxSession.
     *
     * @param session authenticated session to use with the BoxApi.
     */
    public BoxApiFeatures(BoxSession session) {
        super(session);
    }

    public BoxRequestFeatures getSupportedFeatures() {
        return new BoxRequestFeatures(mSession);
    }
}
