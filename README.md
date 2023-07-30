# DriveLapseApp

<img src="https://github.com/vascarpenter/DriveLapseApp/blob/main/ss.jpg" width="50%" />

https://github.com/vascarpenter/DriveLapseApp

- Androidでサーバに立てたapiへaccessして通過時刻を記録しよう
  - サーバは自分で作ってね
  - 上のradiobuttonでルートを選択
  - SETボタンを押すと現時刻をフィールドにいれてくれる
  - SUBMITで送信

### このandroidアプリをコンパイルする前に

- build.gradle :app から 4つの文字列を参照しているので
- `~/.gradle/gradle.properties` に追加しておく


```
# 自分のサイトにあった設定に差し替えてね
driveserverurl=https://ogehage.tk
drivegetapi=get_api?apiaccesskey=ACCESSKEY
drivepostapi=post_api
driveapikey=ACCESSKEY
```

- `app/src/main/res/values/routes.xml`に下記を作成
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string-array name="routes">
    <item>自宅→P1:東京→P2:有楽町→P3:新橋→浜松町</item>
.. 行きの分を2-3個
    <item>浜松町→P1:新橋→P2:有楽町→P3:東京→自宅</item>
.. 帰りの分を同数
    </string-array>
</resources>
```
- string array は　行きと帰りの2倍 `<item>..</item>` を作っておくこと
  - gradle.propertiesでの指定では必ず文字化けして使い物にならなかった

- kotlin ソース内で参照してます
- https アクセスがネームサーバーの問題でできなくなったので　http接続でAPIアクセスするようにした
  (AndroidManifest.xml と xml/network_security_config.xml を変更)
