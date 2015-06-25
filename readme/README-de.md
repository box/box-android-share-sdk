Box Android Share SDK
==============
Mithilfe dieses SDKs können Sie Freigabe-Links und Mitarbeiter auf Box problemlos verwalten.

####Freigabe-Links
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####Mitarbeiter
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

Einrichtung durch Entwickler
--------------
Sie können das SDK abrufen, indem Sie es als Maven-Abhängigkeit hinzufügen, die Quelle in Ihrem Projekt klonen oder eine der vorkompilierten JAR-Dateien von der Veröffentlichungsseite auf GitHub herunterladen.

Dieses SDK hat die folgenden Abhängigkeiten und muss in Ihr Projekt eingebunden werden:
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

Kurzanleitung
--------------
Sie benötigen eine BoxSession und das BoxItem aus dem [box-content-sdk](https://github.com/box/box-android-content-sdk). Weitere Informationen finden Sie in der box-content-sdk-Dokumentation.
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####Freigabe-Link für eine Datei oder einen Ordner
Zum Verwalten eines Freigabe-Links für ein Element starten Sie die folgende Aktivität:
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
Diese Aktivität ermöglicht Ihnen die Verwaltung aller Einstellungen eines Freigabe-Links (für eine bestimmte Datei oder einen bestimmten Ordner): Kennwortbeschränkung, Berechtigungen, Ablaufdatum, Zugriffsebene usw.

####Ordner-Mitarbeiter
Um Ordner-Mitarbeiter zu verwalten, starten Sie die folgende Aktivität:
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
Über diese Aktivität können Sie den Benutzerzugriff auf und die Benutzerberechtigungen für einen bestimmten Ordner verwalten. 

Beispiel-App
--------------
Eine Beispiel-App ist im Ordner [box-share-sample](../../tree/master/box-share-sample) enthalten.

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/

