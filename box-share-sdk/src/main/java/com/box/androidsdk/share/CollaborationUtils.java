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

    private static final int[] THUMB_COLORS = new int[] { 0xff9e9e9e, 0xff63d6e4, 0xffff5f5f, 0xff7ed54a, 0xffaf21f4,
            0xffff9e57, 0xffe54343, 0xff5dc8a7, 0xfff271a4, 0xff2e71b6, 0xffe26f3c, 0xff768fba, 0xff56c156, 0xffefcf2e,
            0xff4dc6fc, 0xff501785, 0xffee6832, 0xffffb11d, 0xffde7ff1 };

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

    public static void setInitialsThumb(Context context, TextView initialsView, String fullName) {
        char initial1 = '\u0000';
        char initial2 = '\u0000';
        if (fullName != null) {
            String[] nameParts = fullName.split(" ");
            if (nameParts[0].length() > 0) {
                initial1 = nameParts[0].charAt(0);
            }
            if (nameParts.length > 1) {
                initial2 = nameParts[nameParts.length - 1].charAt(0);
            }
        }
        Drawable drawable = initialsView.getResources().getDrawable(R.drawable.thumb_background);
        drawable.setColorFilter(THUMB_COLORS[(initial1 + initial2) % THUMB_COLORS.length], PorterDuff.Mode.MULTIPLY);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            initialsView.setBackground(drawable);
        } else {
            initialsView.setBackgroundDrawable(drawable);
        }
        initialsView.setText(initial1 + "" + initial2);
        initialsView.setTextAppearance(context, R.style.TextAppearance_AppCompat_Subhead);
        initialsView.setTextColor(context.getResources().getColor(R.color.box_sharesdk_background));
    }

    public static void setInitialsThumb(Context context, TextView initialsView, int number) {
        Drawable drawable = initialsView.getResources().getDrawable(R.drawable.initials_count_thumb_background);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            initialsView.setBackground(drawable);
        } else {
            initialsView.setBackgroundDrawable(drawable);
        }
        initialsView.setText(String.format(context.getResources().getString(R.string.box_sharedsdk_collaborators_initials_count), number));
        initialsView.setTextAppearance(context, R.style.TextAppearance_AppCompat_Subhead);
        initialsView.setTextColor(context.getResources().getColor(R.color.box_sharesdk_initials_count_color));
    }
}
