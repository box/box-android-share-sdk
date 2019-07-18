package com.box.androidsdk.share.utils;

import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.usx.fragments.UsxFragment;

public class SharedLinkBindingAdapters {

    public static void onLinkClick(boolean checked, UsxFragment.UsxNotifiers notifiers) {
        if (checked) notifiers.linkClicked();
    }

    public static void onSharedLinkToggle(boolean checked, BoxSharedLink link, UsxFragment.UsxNotifiers notifiers) {
        if (checked && link == null) {
            notifiers.notifyShare();
        }  else if (!checked && link != null){
            notifiers.notifyUnshare();
        }
    }

    @BindingAdapter(value = {"linkAccess"})
    public static void setAccess(TextView textView, BoxSharedLink link) {
        String text = "";
        if (link != null) {
            BoxSharedLink.Access access = link.getAccess();
            if (access != null){
                switch(access){
                    case OPEN:
                        text = textView.getResources().getString(R.string.box_sharesdk_accessible_public);
                        break;
                    case COLLABORATORS:
                        text = textView.getResources().getString(R.string.box_sharesdk_accessible_collaborator);
                        textView.setText(text);
                        return;
                    case COMPANY:
                        text = textView.getResources().getString(R.string.box_sharesdk_accessible_company);
                        break;
                }
            }
            if (!text.isEmpty()) {
                text += "\n";
            }
            if (link.getPermissions() != null && link.getPermissions().getCanDownload()) {
                text += textView.getResources().getString(R.string.box_sharesdk_downloads_allowed);
            } else {
                text += textView.getResources().getString(R.string.box_sharesdk_downloads_disabled);
            }
        }
        textView.setText(text);
    }

}
