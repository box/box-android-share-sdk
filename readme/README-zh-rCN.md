Box Android Share SDK
==============
使用此 SDK 可轻松管理 Box 上的共享链接和协作者。

####共享链接
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####协作者
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

开发者设置
--------------
SDK 可通过以下两种方式获取：将 SDK 添加为 maven 依赖，并将源克隆到项目中；或者在 GitHub 的发布页面下载一个预编译的 JAR。

此 SDK 包括以下依赖，应包含在项目中：
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

快速入门
--------------
您将用到 BoxSession 和 [box-content-sdk](https://github.com/box/box-android-content-sdk) 的 BoxItem。请参考 box-content-sdk 文档，了解详细信息。
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####文件或文件夹共享链接
要管理项目共享链接，请开始以下操作：
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
您可以通过此操作管理一个共享链接的所有设置（针对一个特定文件或文件夹）：密码限制、权限、到期日、访问级别等。

####文件夹协作者
要管理文件夹协作者，请开始以下操作：
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
您可以通过此操作管理特定文件夹的用户访问和权限。 

应用程序示例
--------------
在 [box-share-sample](../../tree/master/box-share-sample) 文件夹中可以找到应用程序示例。

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/
