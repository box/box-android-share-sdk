Box Android Share SDK
==============
Questo SDK ti permette di gestire agevolmente collegamenti condivisi e collaboratori in Box.

####Collegamenti condivisi
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####Collaboratori
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

Configurazione per sviluppatori
--------------
Puoi ottenere l'SDK aggiungendolo come dipendenza Maven, clonando l'origine nel progetto o scaricando uno dei JAR precompilati dalla pagina delle versioni su GitHub.

Questo SDK dispone delle seguenti dipendenze che dovranno essere incluse nel tuo progetto:
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

Avvio rapido
--------------
Occorrono BoxSession e BoxItem disponibili in [box-content-sdk](https://github.com/box/box-android-content-sdk). Per ulteriori dettagli, fai riferimento alla documentazione di box-content-sdk.
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####Collegamento condiviso a file o cartella
Per gestire un elemento Collegamento condiviso, avvia la seguente attività:
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
Questa attività consente di gestire tutte le impostazioni di un collegamento condiviso (per un file o una cartella specifica): limitazioni di password, autorizzazioni, data di scadenza, livello di accesso, ecc.

####Collaboratori nella cartella
Per gestire i collaboratori nella cartella,avvia la seguente attività:
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
Questa attività consente di gestire le autorizzazioni e l'accesso degli utenti per una cartella specifica. 

App di esempio
--------------
Puoi trovare un'app di esempio nella cartella [box-share-sample](../../tree/master/box-share-sample).

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/

