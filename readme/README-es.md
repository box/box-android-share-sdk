Box Android Share SDK
==============
Este SDK ofrece la posibilidad de gestionar fácilmente enlaces compartidos y colaboradores en Box.

####Enlaces compartidos
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####Colaboradores
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

Configuración para desarrolladores
--------------
El SDK puede obtenerse agregándolo como dependencia de Maven, clonando el original en su proyecto o descargando uno de los archivos JAR precompilados de la página de versiones de GitHub.

Este SDK tiene las siguientes dependencias, que deberán incluirse en su proyecto:
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

Cómo empezar
--------------
Necesitará una BoxSession y el BoxItem del [box-content-sdk](https://github.com/box/box-android-content-sdk). Consulte la documentación del box-content-sdk para obtener información adicional.
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####Enlaces compartidos a archivos o carpetas
Para gestionar un enlace compartido de un elemento, inicie la siguiente actividad:
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
Esta actividad le permite gestionar toda la configuración de un enlace compartido (asociado a un archivo o carpeta específico): restricciones de contraseña, permisos, fecha de vencimiento, nivel de acceso, etc.

####Colaboradores de carpetas
Para gestionar los colaboradores de las carpetas, inicie la siguiente actividad:
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
Esta actividad le permite gestionar el acceso y los permisos de los usuarios en una carpeta específica. 

Aplicación de ejemplo
--------------
Encontrará una aplicación de ejemplo en la carpeta [box-share-sample](../../tree/master/box-share-sample).

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/

