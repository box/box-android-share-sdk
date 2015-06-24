Box Android Share SDK
==============
本 SDK 可以輕鬆管理 Box 上的共用連結與合作者。

####共用的連結
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####合作者
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

開發人員設定
--------------
取得本 SDK 的方式：新增為 Maven Dependency、將原始碼複製到專案中，或是下載 GitHub Release 頁面上經過預先編譯的 JAR。

本 SDK 有下列 Dependency，必須包含在您的專案中：
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

快速使用指南
--------------
您需要 [box-content-sdk](https://github.com/box/box-android-content-sdk) 的 BoxSession 和 BoxItem。如需其他詳細資料，請參閱 box-content-sdk 說明文件。
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####檔案或資料夾共用連結
如欲管理項目的共用連結，請啟動下列動作：
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
此動作可讓您管理特定檔案或資料夾的共用連結的所有設定：密碼限制、權限、到期日、存取等級等。

####資料夾合作者
如欲管理資料夾合作者，請啟動下列動作：
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
此動作可讓您管理使用者對特定資料夾的存取權限。 

範例應用程式
--------------
[box-share-sample](../../tree/master/box-share-sample) 資料夾中有範例應用程式。

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/
