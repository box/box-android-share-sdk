Box Android Share SDK
==============
Med detta SDK kan du enkelt hantera delade länkar och medarbetare på Box.

####Delade länkar
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####Medarbetare
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

Utvecklarkonfiguration
--------------
Du kan hämta aktuellt SDK genom att lägga till det som ett expertberoende, vilket klonar källan till ditt projekt, eller genom att hämta en av de förkompilerade JAR-filerna från programsidan på GitHub.

Detta SDK har följande beroenden och måste inkluderas i projektet:
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

Snabbstart
--------------
Du behöver en BoxSession och BoxItem från [box-content-sdk](https://github.com/box/box-android-content-sdk). Mer information finns i dokumentationen till box-content-sdk.
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####Delad länk för fil eller mapp
Om du vill hantera delad länk för objekt startar du följande aktivitet:
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
Med den här aktiviteten kan du hantera alla inställningar för en delad länk (för en särskild fil eller mapp): lösenordsbegränsning, behörigheter, förfallodatum, åtkomstnivå osv.

####Mappmedarbetare
Om du vill hantera mappmedarbetare startar du följande aktivitet:
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
Med den här aktiviteten kan du hantera användaråtkomst och behörigheter för en särskild mapp. 

Exempelapp
--------------
En exempelapp finns i mappen [box-share-sample](../../tree/master/box-share-sample).

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/

