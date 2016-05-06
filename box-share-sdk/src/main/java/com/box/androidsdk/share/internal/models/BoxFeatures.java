package com.box.androidsdk.share.internal.models;

import com.box.androidsdk.content.models.BoxJsonObject;
import com.eclipsesource.json.JsonObject;

import java.util.HashSet;

/**
 * Represents a list of supported features for the user
 */
public class BoxFeatures extends BoxJsonObject {
    public static final String USER_FEATURE_LIST = "user_feature_list";

    public static final String FEATURE_PASSWORD_PROTECT_LINKS = "password_protected_shared_links";

    /**
     * Default constructor for features
     */
    public BoxFeatures() {
        super();
    }

    /**
     * Constructs a BoxEntity with the provided map values
     *
     * @param object - json representing this object
     */
    public BoxFeatures(JsonObject object) {
        super(object);
    }

    /**
     * Helper method that will parse into a known child of BoxEntity.
     *
     * @param json JsonObject representing a BoxEntity or one of its known children.
     * @return a BoxEntity or one of its known children.
     */
    public static BoxFeatures createEntityFromJson(final JsonObject json) {
        BoxFeatures features = new BoxFeatures();
        if (json == null)
            return null;

        features.createFromJson(json);
        return features;
    }


    public HashSet<String> getFeatures() {
        return getPropertyAsStringHashSet(USER_FEATURE_LIST);
    }

    public boolean hasFeature(final String featureName) {
        if (getFeatures() != null) {
            return getFeatures().contains(featureName);
        }

        return false;
    }

    public boolean hasPasswordProtectForSharedLinks() {
        return hasFeature(FEATURE_PASSWORD_PROTECT_LINKS);
    }

}
