package com.box.androidsdk.share.internal;

import com.box.androidsdk.content.models.BoxList;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Class representing a list of items in Box Invitees.
 */
public class BoxListInvitees extends BoxList<BoxInvitee> {

    private static final long serialVersionUID = 1900245905334373228L;

    @Override
    protected void parseJSONMember(JsonObject.Member member) {
        String memberName = member.getName();
        JsonValue value = member.getValue();
        if (memberName.equals(FIELD_TOTAL_COUNT)) {
            this.mProperties.put(FIELD_TOTAL_COUNT, value.asLong());
            return;
        } else if (memberName.equals(FIELD_OFFSET)) {
            this.mProperties.put(FIELD_OFFSET, value.asLong());
            return;
        } else if (memberName.equals(FIELD_LIMIT)) {
            this.mProperties.put(FIELD_LIMIT, value.asLong());
            return;
        } else if (memberName.equals(FIELD_ENTRIES)) {
            JsonArray entries = value.asArray();
            for (JsonValue entry : entries) {
                JsonObject obj = entry.asObject();
                BoxInvitee invitee = BoxInvitee.createEntityFromJson(obj);
                if (invitee != null) {
                    collection.add(invitee);
                }
            }
            mProperties.put(FIELD_ENTRIES, collection);
            return;
        }

        super.parseJSONMember(member);
    }
}
