Box Android Share SDK
==============
このSDKを使用すると、Box上の共有リンクやコラボレータの管理が簡単にできるようになります。

####共有リンク
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####コラボレータ
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

開発者向け設定
--------------
このSDKは、maven dependencyとして追加するか、プロジェクト内にソースを複製するか、 GitHubのリリースページからコンパイル済みのJARの1つをダウンロードすることで入手できます。

このSDKには次のような依存性があり、プロジェクトに含まれている必要があります:
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

クイックスタート
--------------
[box-content-sdk](https://github.com/box/box-android-content-sdk)のBoxItemおよびBoxSessionが必要です。その他の詳細については、box-content-sdkの文書を参照してください。
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####ファイルまたはフォルダの共有リンク
項目の共有リンクを管理するには、次の処理を実行してください。
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
この処理を実行すると、(特定のファイルまたはフォルダの) 共有リンクの設定をすべて管理できるようになります (パスワード制限、権限、有効期限、アクセスレベルなど) 。

####フォルダのコラボレータ
フォルダのコラボレータを管理するには、次の処理を開始してください。	
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
この処理を実行すると、特定のフォルダへのユーザーアクセスや権限を管理できるようになります。 

サンプルのアプリケーション
--------------
サンプルのアプリケーションは、[box-share-sample](../../tree/master/box-share-sample)フォルダ内にあります。

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/

