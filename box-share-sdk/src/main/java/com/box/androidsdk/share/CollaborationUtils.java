package com.box.androidsdk.share;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.TextView;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;

public class CollaborationUtils {

    public static final String EXTRA_ITEM = "com.box.androidsdk.share.CollaborationUtils.ExtraItem";
    public static final String EXTRA_USER_ID = "com.box.androidsdk.share.CollaborationUtils.ExtraUserId";
    public static final String EXTRA_COLLABORATIONS = "com.box.androidsdk.share.CollaborationUtils.ExtraCollaborations";
    public static String EXTRA_OWNER_UPDATED = "com.box.androidsdk.share.CollaborationUtils.ExtraOwnerUpdated";

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


    public static void setInitialsThumb(Context context, TextView initialsView, int number) {
        Drawable drawable = initialsView.getResources().getDrawable(R.drawable.initials_count_thumb_background);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            initialsView.setBackground(drawable);
        } else {
            initialsView.setBackgroundDrawable(drawable);
        }
        initialsView.setText(String.format(context.getResources().getString(R.string.box_sharedsdk_collaborators_initials_count), number));
        initialsView.setTextColor(context.getResources().getColor(R.color.box_sharesdk_initials_count_color));
    }
}
