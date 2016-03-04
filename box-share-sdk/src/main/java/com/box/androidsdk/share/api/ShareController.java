package com.box.androidsdk.share.api;

import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;

/**
 * Created by varungupta on 3/4/2016.
 */
public interface ShareController {
    void fetchCollaborations(BoxFolder boxFolder, BoxFutureTask.OnCompletedListener<BoxIteratorCollaborations> onCompletedListener);
    void fetchRoles(BoxFolder boxFolder, BoxFutureTask.OnCompletedListener<BoxFolder> onCompletedListener);
}
