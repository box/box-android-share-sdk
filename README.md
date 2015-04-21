Box Android Share SDK
==============
This SDK enables the ability to easily manage share links and collaborators on Box.

Developer Setup
--------------
The SDK can be obtained by adding it as a maven dependency, cloning the source into your project, or by downloading one of the precompiled JARs from the releases page on GitHub.

This SDK has the following dependencies and will need to be included if you use the JAR:
* minimal-json v0.9.1 (for maven: com.eclipsesource.minimal-json:minimal-json:0.9.1)
* box-content-sdk (for maven: )

Quickstart
--------------
You will need a BoxSession and the BoxItem from the [box-content-sdk](https://github.com/box/box-android-content-sdk). Please refer to the documentation of the box-content-sdk for additional details.
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

To launch the activity to manage an item Shared Link (in this case a Folder but it works also for Files):
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```

To launch the activity to manage Folder Collaborators:
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```

Sample App
--------------
A sample app can be found in the [box-share-sample](../../tree/master/box-share-sample) folder.

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/​
