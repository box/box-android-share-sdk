Box Android Share SDK
==============
이 SDK를 사용하여 Box에서 공유 링크 및 공동 작업자를 손쉽게 관리할 수 있는 기능을 활성화합니다.

####공유 링크
<img src="https://cloud.box.com/shared/static/cvdtf4475mf39r47s066de79ukpwlwwv.png" width="200"/>
<img src="https://cloud.box.com/shared/static/gqi9a9xzucjd9u9vkmf1zzwulbvnlbki.png" width="200"/>
<img src="https://cloud.box.com/shared/static/xh0n3ewuk1s68o9x8z195fgknqj41ij3.png" width="200"/>

####공동 작업자
<img src="https://cloud.box.com/shared/static/855dkoj2nyk1obtiqpc2k5dr1o85tpp9.png" width="200"/>
<img src="https://cloud.box.com/shared/static/pz3ujyihzwd7du9bqtrn5cqveg5pzdqo.png" width="200"/>
<img src="https://cloud.box.com/shared/static/7r90gmo7zq3q4zs5otjvi0bf4s1ya01g.png" width="200"/>

개발자 설정
--------------
SDK는 Maven 종속성으로 추가하거나, 프로젝트에 소스를 복제하거나, GitHub의 릴리스 페이지에서 미리 컴파일된 JAR 중 하나를 다운로드하여 획득할 수 있습니다.

이 SDK는 다음과 같은 종속성을 가지고 있으며, 프로젝트에 포함돼야 합니다.
* [minimal-json v0.9.1](https://github.com/ralfstx/minimal-json) (maven: `com.eclipsesource.minimal-json:minimal-json:0.9.1`)
* [box-content-sdk](https://github.com/box/box-android-content-sdk) (maven: `coming soon`)

빠른 시작
--------------
[box-content-sdk](https://github.com/box/box-android-content-sdk)의 BoxItem 및 BoxSession이 필요합니다. 자세한 내용은 box-content-sdk의 문서를 참고하십시오.
```java
    BoxSession session = new BoxSession(MainActivity.this);
    BoxFolder folder = new BoxApiFolder(session).getInfo("<FOLDER_ID>").send();
```

####파일 또는 폴더 공유 링크
항목 공유 링크를 관리하려면 다음 활동을 실행합니다.
```java
    startActivity(BoxSharedLinkActivity.getLaunchIntent((MainActivity.this, folder, session));
```
이 활동을 통해 특정 파일 및 폴더에 대한 공유 링크의 암호 제한, 권한, 만료일, 액세스 레벨 등 모든 설정을 관리할 수 있습니다.

####폴더 공동 작업자
폴더 공동 작업자를 관리하려면 다음과 같은 활동을 실행하십시오.
```java
    startActivity(BoxCollaborationsActivity.getLaunchIntent(MainActivity.this, folder, session));
```
이 활동을 통해 특정 폴더에 대한 사용자 액세스 및 권한을 관리할 수 있습니다. 

샘플 앱
--------------
샘플 앱은 [box-share-sample](../../tree/master/box-share-sample) 폴더에서 확인할 수 있습니다.

Copyright and License
---------------------
Copyright 2015 Box, Inc. All rights reserved.

Licensed under the Box Terms of Service; you may not use this file except in compliance with the License.
You may obtain a copy of the License at https://www.box.com/legal/termsofservice/

