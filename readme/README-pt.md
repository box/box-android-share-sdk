Box Android Share SDK
==============
O SDK possibilita gerenciar facilmente os links compartilhados e os colaboradores no Box.

####Links compartilhados
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####Colaboradores
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

Configuração para programadores
--------------
É possível obter o SDK adicionando-o como uma dependência maven, clonando a fonte no projeto ou baixando um dos JARs pré-compilados na página de liberações no GitHub.

O SDK tem as seguintes dependências que devem ser incluídas no projeto:
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

Início rápido
--------------
Você precisará de uma BoxSession e do BoxItem do [box-content-sdk](https://github.com/box/box-android-content-sdk). Consulte a documentação do box-content-sdk para mais detalhes.
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####Link compartilhado do arquivo ou da pasta
Para gerenciar um link compartilhado do item, inicie a seguinte atividade:
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
Esta atividade permite que você gerencie todas as configurações de um link compartilhado (para um determinado arquivo ou pasta): restrição de senha, permissões, data de expiração, nível de acesso etc.

####Colaboradores da pasta
Para gerenciar os colaboradores da pasta, inicie a seguinte atividade:
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
Esta atividade permite que você gerencie o acesso do usuário e as permissões de uma pasta específica. 

Aplicativo de amostragem
--------------
Um aplicativo de amostragem pode ser encontrado na pasta [box-share-sample](../../tree/master/box-share-sample).

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/

