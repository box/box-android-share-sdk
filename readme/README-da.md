Box Android Share SDK
==============
Denne SDK gør det muligt nemt at administrere delte links og samarbejdspartnere i Box.

####Delte links
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####Samarbejdspartnere
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

Udviklerkonfiguration
--------------
Denne SDK kan opnås ved at tilføje den som et maven dependency, hvor du kloner kilden ind i dit projekt, eller ved at downloade en af de precompilede JAR-filer fra siden med programudgaver på GitHub.

Denne SDK har følgende dependency'er, og de skal inkluderes i dit projekt:
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

Hurtig start
--------------
Du har brug for en BoxSession og et BoxItem fra [box-content-sdk](https://github.com/box/box-android-content-sdk). Se i dokumentationen til box-content-sdk for at få mere at vide.
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####Delt link til fil eller mappe
For at administrere et delt link til et element skal du starte følgende aktivitet:
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
Denne aktivitet gør det muligt for dig at administrere indstillingerne for et delt link (til en specifik fil eller mappe): adgangskodebegrænsning, tilladelser, udløbsdato, adgangsniveau osv.

####Mappesamarbejdspartnere
For at administrere mappesamarbejdspartnere skal du starte følgende aktivitet:
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
Denne aktivitet gør det muligt for dig at administrere brugeradgang og -tilladelser til en specifik mappe. 

Demo-app
--------------
Der findes en demo-app i mappen [box-share-sample](../../tree/master/box-share-sample).

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/

