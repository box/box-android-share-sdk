package com.box.androidsdk.share.utils;

import com.box.androidsdk.content.models.BoxBookmark;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.share.usx.fragments.SharedLinkAccessFragment;

public class SharedLinkAccessToggleListeners {

    public static void onAccessLevelCheckChanged(boolean checked, BoxSharedLink.Access access, SharedLinkAccessFragment.SharedLinkAccessNotifiers notifiers) {
        if (checked) {
            notifiers.notifyAccessLevelChange(access);
        }
    }


    public static void onDownloadToggle(boolean checked, BoxItem shareItem, SharedLinkAccessFragment.SharedLinkAccessNotifiers notifiers) {
        if (shareItem instanceof BoxBookmark || shareItem.getSharedLink().getPermissions().getCanDownload() == checked ){
            // if there is no change or we are busy with another task then do nothing.
            return;
        }
        notifiers.notifyDownloadChange(checked);
    }

    public static void onPasswordToggle(boolean checked, BoxItem shareItem, SharedLinkAccessFragment.SharedLinkAccessNotifiers notifiers) {
        if (shareItem.getSharedLink().getIsPasswordEnabled() == checked){
            // if there is no change or we are busy with another task then do nothing.
            return;
        }
        notifiers.notifyRequirePassword(checked);
      }

    public static void onExpireToggle(boolean checked, BoxItem shareItem, SharedLinkAccessFragment.SharedLinkAccessNotifiers notifiers) {
        if ((shareItem.getSharedLink().getUnsharedDate() != null) == checked) {
            // if there is no change or we are busy with another task then do nothing.
            return;
        }
        notifiers.notifyExpireLink(checked);
    }
}
