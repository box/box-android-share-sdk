Box Android Share SDK
==============
Ten zestaw SDK umożliwia łatwe zarządzanie łączami udostępnionymi i współpracą w usłudze Box.

####Łącza udostępnione
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####Współpracownicy
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

Konfiguracja dla programisty
--------------
Zestaw SDK można pobrać, dodając go jako element zależności Maven, klonując źródło do projektu lub pobierając jeden ze skompilowanych wstępnie plików JAR ze strony wydań w serwisie GitHub.

Ten zestaw SDK charakteryzuje się następującymi zależnościami, które należy ująć w projekcie:
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

Szybkie rozpoczynanie
--------------
Wymagane są elementy BoxSession i BoxItem z: [box-content-sdk](https://github.com/box/box-android-content-sdk). Dodatkowe szczegóły można znaleźć w dokumentacji box-content-sdk.
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####Łącze udostępnione pliku lub folderu
Aby zarządzać łączem udostępnionym elementu, uruchom następujące działanie:
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
Działanie to umożliwia zarządzanie wszystkimi ustawieniami łącza udostępnionego (dla konkretnego pliku lub folderu): ograniczaniem hasła, uprawnieniami, datą ważności, poziomem dostępu itp.

####Współpracownicy folderu
Aby zarządzać współpracownikami folderu, uruchom następujące działanie:
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
To działanie umożliwia zarządzanie dostępem i uprawnieniami użytkownika dotyczącymi konkretnego folderu. 

Przykładowa aplikacja
--------------
Przykładową aplikację można znaleźć w folderze: [box-share-sample](../../tree/master/box-share-sample).

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/

