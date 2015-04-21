package com.box.androidsdk.share.internal;

import com.box.androidsdk.content.models.BoxJsonObject;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import java.util.Map;

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
     * @param map - map of keys and values of the object
     */
    public BoxInvitee(Map<String, Object> map) {
        super(map);
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
        String id = (String) mProperties.get(FIELD_NAME);
        if (id == null) {
            return (String) mProperties.get(FIELD_NAME);
        }
        return id;
    }

    /**
     * Gets the type of the entity
     *
     * @return the entity type
     */
    public String getEmail() {
        String type = (String) mProperties.get(FIELD_EMAIL);
        if (type == null) {
            return (String) mProperties.get(FIELD_EMAIL);
        }
        return type;
    }

    @Override
    protected void parseJSONMember(JsonObject.Member member) {
        String memberName = member.getName();
        JsonValue value = member.getValue();
        if (memberName.equals(FIELD_NAME)) {
            this.mProperties.put(FIELD_NAME, value.asString());
            return;
        } else if (memberName.equals(FIELD_EMAIL)) {
            this.mProperties.put(FIELD_EMAIL, value.asString());
            return;
        }

        super.parseJSONMember(member);
    }

    @Override
    public String toString() {
        return this.getEmail();
    }
}
