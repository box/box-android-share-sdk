package com.box.androidsdk.share.internal.models;

import com.box.androidsdk.content.models.BoxJsonObject;
import com.eclipsesource.json.JsonObject;

/**
 * This represents an invitee that can be invited to be a collaborator. This is currently still in development and is treated
 * as an internal endpoint at the moment, which is why it is not included as part of the box-content-sdk.
 */
public class BoxInvitee extends BoxJsonObject {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_EMAIL = "email";

    private static final long serialVersionUID = -7334617777668107716L;

    /**
     * Default constructor for invitees
     */
    public BoxInvitee() {
        super();
    }


    /**
     * Constructs a BoxEntity with the provided map values
     *
     * @param object - json representing this object
     */
    public BoxInvitee(JsonObject object) {
        super(object);
    }

    /**
     * Helper method that will parse into a known child of BoxEntity.
     *
     * @param json JsonObject representing a BoxEntity or one of its known children.
     * @return a BoxEntity or one of its known children.
     */
    public static BoxInvitee createEntityFromJson(final JsonObject json) {
        BoxInvitee invitee = new BoxInvitee();
        if (json == null)
            return null;

        invitee.createFromJson(json);
        return invitee;
    }

    /**
     * Gets the name.
     *
     * @return the name of the invitee
     */
    public String getName() {
        return getPropertyAsString(FIELD_NAME);
    }

    /**
     * Gets the type of the entity
     *
     * @return the entity type
     */
    public String getEmail() {
        return getPropertyAsString(FIELD_EMAIL);
    }

    @Override
    public String toString() {
        return this.getEmail();
    }
}
