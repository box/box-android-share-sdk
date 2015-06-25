Box Android Share SDK
==============
Met deze SDK kunt u gedeelde links en medebewerkers gemakkelijk beheren op Box.

####Gedeelde links
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####Medebewerkers
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

Developer-installatie
--------------
De SDK kan worden verkregen door deze toe te voegen als maven-afhankelijkheid, door de bron in uw project te klonen, of door een van de voorgecompileerde JARs te downloaden via de release-pagina op GitHub.

Deze SDK heeft de volgende afhankelijkheden en moet worden opgenomen in uw project:
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

Aan de slag
--------------
U hebt een BoxSession en een BoxItem uit de [box-content-sdk](https://github.com/box/box-android-content-sdk) nodig. Raadpleeg de documentatie van de box-content-sdk voor meer informatie.
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####Gedeelde link naar bestand of map
Als u een gedeelde link van een item wilt beheren, start u de volgende activiteit:
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
Met deze activiteit kunt u alle instellingen van een gedeelde link (naar een bepaald bestand of bepaalde map) beheren: wachtwoordbeperking, machtigingen, vervaldatum, toegangsniveau, enz.

####Medebewerkers van mappen
Als u medebewerkers van mappen wilt beheren, start u de volgende activiteit:
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
Met deze activiteit kunt u gebruikerstoegang en machtigingen voor een bepaalde map beheren. 

Voorbeeldapp
--------------
U kunt een voorbeeldapp vinden in de map [box-share-sample](../../tree/master/box-share-sample).

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/

