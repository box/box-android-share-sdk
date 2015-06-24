Box Android Share SDK
==============
Ce kit de développement vous permet de gérer facilement des liens partagés et des collaborateurs dans Box.

####Liens partagés
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####Collaborateurs
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

Configuration développeur
--------------
Pour obtenir ce kit de développement, ajoutez-le en tant que dépendance Maven en clonant la source dans votre projet ou en téléchargeant l'un des JAR précompilés disponibles sur la page de téléchargements de GitHub.

Ce kit de développement est doté des dépendances suivantes et devra être intégré dans votre projet :
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

Premiers pas
--------------
Vous aurez besoin d'une BoxSession et du BoxItem qui se trouve [box-content-sdk](https://github.com/box/box-android-content-sdk). Pour plus d'informations, veuillez consulter la documentation du sdkbox-content-.
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####Lien partagé vers un fichier ou dossier
Pour gérer un élément de lien partagé, lancez l'activité suivante :
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
Cette activité vous permet de gérer tous les paramètres d'un lien partagé (vers un fichier ou dossier spécifique) : restrictions concernant le mot de passe, autorisations, date d'expiration, niveau d'accès, etc.

####Collaborateurs sur les dossiers
Pour gérer les collaborateurs sur les dossiers, lancez l'activité suivante :
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
Cette activité vous permet de gérer l'accès des utilisateurs à un dossier spécifique et leurs autorisations sur ce dossier. 

Application test
--------------
Vous trouverez une application test dans le dossier [box-share-sample](../../tree/master/box-share-sample).

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/

