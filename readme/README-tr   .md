Box Android Share SDK
==============
Bu SDK Box'ta paylaşılan bağlantıları ve işbirlikçilerini kolaylıkla yönetmenize olanak sağlar.

####Paylaşılmış Bağlantılar
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####İşbirlikçiler
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

Geliştirici Kurulumu
--------------
SDK kaynağı projenize kopyalayarak ya da GitHub konumundaki bültenlerde bulunan ön derlenmiş JAR öğelerinden birini indirerek faydalı bir araç olarak eklenmesi yoluyla elde edilebilir.

Bu SDK aşağıdaki maddelere bağlıdır ve bu SDK'nın projenizde bulunması gerekir:
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

Hızlı Başlangıç
--------------
[box-content-sdk](https://github.com/box/box-android-content-sdk) konumundan bir BoxSession ve BoxItem'e ihtiyaç duyacaksınız. Daha fazla ayrıntı için lütfen box-content-sdk belgesine bakınız.
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####Paylaşılmış Dosya veya Klasör Bağlantıları
Paylaşılmış Bağlantılarda, aşağıdaki etkinliği başlatın:
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
Bu etkinlik Paylaşılmış Bağlantının (belirli bir Dosya ya da Klasör için) tüm ayarlarını yönetmenize imkan tanır: parola kısıtlamaları, izinler, sona erme tarihi, erişim seviyesi vb.

####Klasör İşbirlikçileri
Klasör işbirlikçilerini yönetmek için aşağıdaki etkinliği başlatın:
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
Bu etkinlik kullanıcı erişimini ve belirli bir klasör üzerindeki izinleri yönetmenize olanak sağlar. 

Örnek Uygulama
--------------
Örnek bir uygulama [box-share-sample](../../tree/master/box-share-sample) klasöründe bulunur.

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/

