package com.box.androidsdk.share.internal;

import com.box.androidsdk.content.models.BoxEntity;
import com.box.androidsdk.content.models.BoxIterator;

/**
 * Class representing a list of items in Box Invitees.
 */
public class BoxListInvitees extends BoxIterator<BoxInvitee> {

    private static final long serialVersionUID = 1900245905334373228L;

    @Override
    protected BoxJsonObjectCreator<BoxInvitee> getObjectCreator() {
        return BoxEntity.getBoxJsonObjectCreator(BoxInvitee.class);
    }
}