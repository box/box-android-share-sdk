package com.box.androidsdk.share;

import android.content.Context;

import com.box.androidsdk.content.models.BoxCollaboration;

public class CollaborationUtils {

    public static String getRoleName(Context context, BoxCollaboration.Role role) {
        switch(role) {
            case EDITOR:
                return context.getString(R.string.box_sharesdk_role_name_editor);
            case VIEWER:
                return context.getString(R.string.box_sharesdk_role_name_viewer);
            case PREVIEWER:
                return context.getString(R.string.box_sharesdk_role_name_previewer);
            case UPLOADER:
                return context.getString(R.string.box_sharesdk_role_name_uploader);
            case PREVIEWER_UPLOADER:
                return context.getString(R.string.box_sharesdk_role_name_previewer_uploader);
            case VIEWER_UPLOADER:
                return context.getString(R.string.box_sharesdk_role_name_viewer_uploader);
            case CO_OWNER:
                return context.getString(R.string.box_sharesdk_role_name_co_owner);
            case OWNER:
                return context.getString(R.string.box_sharesdk_role_name_owner);
            default:
                return "";
        }
    }

    public static String getRoleDescription(Context context, BoxCollaboration.Role role) {
        switch(role) {
            case EDITOR:
                return context.getString(R.string.box_sharesdk_role_description_editor);
            case VIEWER:
                return context.getString(R.string.box_sharesdk_role_description_viewer);
            case PREVIEWER:
                return context.getString(R.string.box_sharesdk_role_description_previewer);
            case UPLOADER:
                return context.getString(R.string.box_sharesdk_role_description_uploader);
            case PREVIEWER_UPLOADER:
                return context.getString(R.string.box_sharesdk_role_description_previewer_uploader);
            case VIEWER_UPLOADER:
                return context.getString(R.string.box_sharesdk_role_description_viewer_uploader);
            case CO_OWNER:
                return context.getString(R.string.box_sharesdk_role_description_co_owner);
            case OWNER:
                return context.getString(R.string.box_sharesdk_role_description_owner);
            default:
                return "";
        }
    }

    public static String getCollaborationStatusText(Context context, BoxCollaboration.Status status) {
        switch (status) {
            case PENDING:
                return context.getString(R.string.box_sharesdk_invited_status);
            case REJECTED:
                return context.getString(R.string.box_sharesdk_rejected_status);
            default:
                return "";
        }
    }
}
