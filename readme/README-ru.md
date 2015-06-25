Box Android Share SDK
==============
Данный пакет для разработки ПО (SDK) предоставляет возможность простого управления общими ссылками и соавторами в Box.

####Общие ссылки
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####Соавторы
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

Настройка разработчика
--------------
Пакет SDK можно получить, добавив его в качестве интеллектуальной зависимости, выполнив клонирование источника в проект, или скачав один из предварительно скомпилированных файлов JAR со страницы выпусков на GitHub.

Данный пакет SDK содержит следующие зависимости и должен быть включен в ваш проект:
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

Краткое руководство
--------------
Вам потребуется BoxSession и BoxItem из [box-content-sdk](https://github.com/box/box-android-content-sdk). Дополнительные сведения см. в документации к box-content-sdk.
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####Общая ссылка к файлу или папке
Для управления общей ссылкой на элемент запустите следующее действие:
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
Данное действие позволяет управлять всеми настройками общей ссылки (для определенного файла или папки): ограничения паролем, права, дата окончания срока действия, уровень доступа и т. д.

####Соавторы папки
Для управления соавторами папки запустите следующее действие:
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
Данное действие позволяет управлять доступом и правами пользователей для определенной папки. 

Пробное приложение
--------------
Пробное приложение можно найти в папке [box-share-sample](../../tree/master/box-share-sample).

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/

