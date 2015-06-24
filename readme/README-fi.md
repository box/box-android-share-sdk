Box Android Share SDK
==============
Tämän ohjelmankehityspaketin (SDK) avulla voidaan ottaa käyttöön Boxin jakamislinkkien ja yhteistyökumppaneiden hallinta.

####Jaetut linkit
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####Yhteistyökumppanit
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

Kehittäjän asetukset
--------------
SDK voidaan lisätä Maven-riippuvuutena, kloonaamalla lähdekoodi projektiin tai lataamalla jokin esikoostetuista JAR-paketeista GitHubin julkaisusivulla.

Tässä SDK:ssa on seuraavat riippuvuudet, jotka on liitettävä projektiin:
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

Pika-aloitus
--------------
Tarvitset BoxSession ja BoxItem [box-content-sdk](https://github.com/box/box-android-content-sdk). Lisätietoja on box-content-sdk -ohjeissa.
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####Tiedoston tai kansion jaettu linkki
Voit hallita jaettuja linkkejä ottamalla käyttöön seuraavan toiminnon:
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
Tämän toiminnon avulla voit hallita kaikkia (tietyn tiedoston tai kansion) jaettujen linkkien asetuksia, kuten salasanarajoituksia, käyttöoikeuksia, vanhenemispäivää, ja oikeustasoja.

####Kansion yhteistyökumppanit
Voit hallita kansion yhteistyökumppaneita ottamalla käyttöön seuraavan toiminnon:
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
Tämän toiminnon avulla voit hallita tietyn kansion käyttäjiä ja käyttöoikeuksia. 

Mallisovellus
--------------
Mallisovellus on kansiossa [box-share-sample](../../tree/master/box-share-sample).

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/

