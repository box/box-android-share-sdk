Box Android Share SDK
==============
This SDK enables the ability to easily manage shared links and collaborators on Box.

####Shared Links
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####Collaborators
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>


Developer Setup
--------------
The SDK can be obtained by adding it as a maven dependency, cloning the source into your project, or by downloading one of the precompiled JARs from the releases page on GitHub.

Gradle: 
```groovy 
compile 'com.box:box-android-share-sdk:1.0.0'
```
Maven: 
```xml
<dependency>
    <groupId>com.box</groupId>
    <artifactId>box-android-share-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

If not using Gradle or Maven, this SDK has the following dependencies and will need to be included in your project:
* [box-android-sdk](https://github.com/box/box-android-sdk) (maven: `com.box:box-android-sdk:3.0.2`)


Quickstart
--------------
You will need a BoxSession and the BoxItem from the [box-content-sdk](https://github.com/box/box-android-content-sdk). Please refer to the documentation of the box-content-sdk for additional details.
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####File or Folder Shared Link
To manage an item Shared Link, launch the following activity:
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
This activity allows you to manage all the settings of a Shared Link (for a specific File or Folder): password restriction, permissions, expiration date, access level, etc.

####Folder Collaborators
To manage Folder collaborators, launch the following activity:
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
This activity allows you to manage user access and permissions to a specific Folder. 

Sample App
--------------
A sample app can be found in the [box-share-sample](../../tree/master/box-share-sample) folder.

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/​
