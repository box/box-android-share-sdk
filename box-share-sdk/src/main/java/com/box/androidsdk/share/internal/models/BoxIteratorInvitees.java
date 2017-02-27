package com.box.androidsdk.share.internal.models;

import com.box.androidsdk.content.models.BoxEntity;
import com.box.androidsdk.content.models.BoxIterator;
import com.box.androidsdk.share.internal.models.BoxInvitee;

/**
 * Class representing a iterator of items in Box Invitees.
 */
public class BoxIteratorInvitees extends BoxIterator<BoxInvitee> {

    private static final long serialVersionUID = 1900245905334373228L;

    private transient BoxJsonObjectCreator<BoxInvitee> representationCreator;

    @Override
    protected BoxJsonObjectCreator<BoxInvitee> getObjectCreator() {
        if (representationCreator != null){
            return representationCreator;
        }
        representationCreator = BoxEntity.getBoxJsonObjectCreator(BoxInvitee.class);
        return representationCreator;
    }
}
