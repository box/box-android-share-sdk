Box Android Share SDK
==============
Dette utviklersettet gjør det mulig å administrere delingskoblinger og samarbeidspartnere i Box.

####Delingskoblinger
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####Samarbeidspartnere
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

Konfigurasjon for utvikler
--------------
Du kan få tak i utviklersettet ved å legge det til som en Maven-avhengighet og klone kilden inn i prosjektet, eller ved å laste ned en av de forhåndskompilerte JAR-filene fra utgivelsessiden på GitHub.

Dette utviklersettet har følgende avhengigheter og må inkluderes i prosjektet:
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

Hurtigstart
--------------
Du trenger en BoxSession og BoxItem fra [box-content-sdk](https://github.com/box/box-android-content-sdk). Se dokumentasjonen for box-content-sdk for å få mer informasjon.
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####Delingskobling for fil eller mappe
Start følgende aktivitet for å administrere en delingskobling for et element:
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
Denne aktiviteten gjør at du kan administrere alle innstillingene for en delingskobling (for en bestemt fil eller mappe): passordbeskyttelse, tillatelser, utløpsdato, tilgangsnivå med flere.

####Samarbeidspartnere for mappe
Start følgende aktivitet for å administrere samarbeidspartnere for mappe:
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
Denne aktiviteten gjør at du kan administrere brukertilgang og -tillatelser for en bestemt mappe. 

Eksempelapplikasjon
--------------
Du finner en eksempelapplikasjon i mappen [box-share-sample](../../tree/master/box-share-sample)

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/

