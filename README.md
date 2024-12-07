# <img src="app/src/main/ic_launcher-playstore.png" width="60px"> LIME: Adkiller for Beta LINEs

## 以下に準じます
https://github.com/Chipppppppppp/LIME

 ## 通知が届かないの解決方法
下記の、LINE 14.3.2をインストールしパッチを行い、インストールを行って、ログインをしてください。


ログイン後、LIMEに適応するバージョンのLINEにパッチを行って、**更新**するようにしてください。

https://apkcombo.com/ja/line/jp.naver.line.android/download/phone-14.3.2-apk

推奨LsPatch<br>
https://github.com/JingMatrix/LSPatch/actions

# 更新について
更新内容を記載していないReleaseについては、差分を押してください、必要でない更新でなければ無理して更新を行う必要はありません。
更新名による判断<br>
(例)v1.12.5

v1.12.6 →適応するLINEバージョンの変更<br>
v1.12.5a, v1.12.5a1→仕様変更

## LsPatchを利用の方へ 


## 確認済みのバグやエラー
アプリがクラッシュする→トラッキング通信をブロックするを無効にしてください


## トーク履歴のリストアについて

一度アプリをアンインストールした場合以下に従ってください。

①リストアしたいファイルを移動させる<br>
②LINEを開いてバックアップ<br>
③移動させたファイルの名前を`naver_line_backup.db`に変更する<br>
④LIME backup フォルダに入れ替え<br>
で、移動させる<br>
⑤リストアさせる


ファイルエクスプローラーは以下を使用してください（エラーなどの報告に対応しやすくするためです）
https://play.google.com/store/apps/details?id=me.zhanghai.android.files


方法が怪しい場合以下の動画を参照してから、リストアを行ってください。(データが上書きされリストアできなくなる恐れがあります)
https://youtu.be/94JN4NLGdOI


## トーク履歴の自動バックアップについて

[Macro SAMPLE](https://drive.usercontent.google.com/u/0/uc?id=1rhZPmoMbti_l1JaX2EbjcRKUePkWlIXU&export=download)

## 概要

このアプリで追加されている機能は、いずれPRするものがおおいですが、機能の追加を優先しているため、修正が必要なものが多く、このような形で公開させていただいております。

LIME開発者様 感謝しています


## Thank you

LIME  開発者（コラボレーター）
https://github.com/Chipppppppppp

コラボレーター
https://github.com/s1204IT


apks→apk

①AntiSplit<br>
https://github.com/AbdurazaaqMohammed/AntiSplit-M


②M apk tool<br>
https://maximoff.su/apktool/?lang=en

Icon
https://github.com/reindex-ot

バグ報告、仕様提案
5チャンネラー
https://egg.5ch.net/test/read.cgi/android/1729438846/



## 使用方法
LINEアプリの <kbd>ホーム</kbd> > <kbd>⚙</kbd> から｢**設定**｣に入り、右上の｢**LIME**｣のボタンより開けます。また、Root ユーザーは LI**M**E アプリから設定することも可能です。クローンアプリなどでは LI**M**E 側からしか設定できない場合があるようです。

<details><summary>画像を閲覧</summary>

<a href="#"><img src="https://github.com/Chipppppppppp/LIME/assets/78024852/2f344ce7-1329-4564-b500-1dd79e586ea9" width="400px" alt="Sample screenshot"></a>

</details>

また、トーク画面上の <kbd>トグル又は✉️ボタン</kbd> からスイッチをオン（緑）にすると**未読のまま閲覧**できます。(このスイッチは設定で削除可能です）

※返信すると未読が解除されてしまうのでご注意ください

<details><summary>画像を閲覧</summary>

<a href="#"><img src="https://github.com/Chipppppppppp/LIME/assets/78024852/bd391a83-b041-4282-9eec-fe71b3b19aa0" width="400px" alt="Sample screenshot"></a>

</details>

## 機能

- 不要なボトムバーのアイコンの削除
- ボトムバーのアイコンのラベルの削除
- 広告・おすすめの削除
- サービスのラベルを削除
- 通知の「通知をオフ」アクションを削除
- WebView を既定のブラウザで開く
- 常に既読をつけない
- 未読のまま閲覧
  - トーク画面右上メニューのスイッチから設定できます (スイッチは削除可能)
- 送信取り消しの拒否
- 常にミュートメッセージとして送信
  - 送信時「通常メッセージ」を選択すれば通知されます
- トラッキング通信のブロック
  - `noop`, `pushRecvReports`, `reportDeviceState`, `reportLocation`, `reportNetworkStatus` がブロックされます
- 通信内容をログに出力
- 通信内容を改変
  - JavaScript で通信内容を改変できます (後述)
- ナビゲーションバーを黒色に固定化
- 非表示にしたチャットの再表示を無効化
- LsPatch用　着信音を鳴らす
- サービスの項目の削除
- トークのバックアップ、リストア
- 既読者の確認


### JavaScript で通信内容を改変する

設定の「リクエストを改変」、「レスポンスを改変」では、Rhino の JavaScript コードを記述することで自由に通信内容を改変できます。これを利用して新たな機能が実装可能なことを確認済みです (`HOOK_SAMPLE.md`)。

あらかじめ `data` という変数が用意されており、以下のプロパティが含まれます。

- `type`: `REQUEST` または `RESPONSE` となる `Enum` 型
- `name`: 通信の名前
- `value`: 通信内容

※`data` は、[こちらのクラス](https://github.com/Chipppppppppp/LIME/blob/master/app/src/main/java/io/github/chipppppppppp/lime/hooks/Communication.java) のインスタンスで、「通信内容をログに出力」で確認できます。

また、`console.log` で `XposedBridge` にログを出力できます。エラーが発生した場合もここに出力されます。
リクエスト・レスポンスともに、JavaScript は他の処理より早く実行され、「通信内容にログを出力」は最後に実行されます。
Rhino の仕様、特に **Java 文字列との比較に `equals` を用いる**必要があることに注意してください。

### Root 端末 (Magisk)

1. [**LSPosed**](https://github.com/LSPosed/LSPosed) をインストール
2. LI**N**E アプリと LI**M**E アプリを両方ともインストール
3. Google Play ストアの自動アップデートを防ぐために、[**Update Locker**](https://github.com/Xposed-Modules-Repo/ru.mike.updatelocker) や [**Hide My Applist**](https://github.com/Dr-TSNG/Hide-My-Applist) で LINE アプリを指定する  
  [Aurora Store](https://auroraoss.com) の場合はブラックリストを使用
4. LSPosed のモジュールから LIME に移動し、<kbd>モジュールの有効化</kbd> と LINE アプリにチェックを入れる

### 非 Root 端末

> [!WARNING]
> 非 root 端末では､ 以下の問題があります  
> - Google アカウント (ドライブ) を使用したトーク履歴の復元ができない  
>   ([この方法](https://github.com/Chipppppppppp/LIME/issues/50#issuecomment-2174842592) でログインすれば可能)   
> - 着信が入るとクラッシュ  
> - コインの購入が不可  
> - LINE Pay の一部の機能が使用不可  
> -　△ Wear OS (スマートウォッチ)での連携

1. [**LSPatch**](https://github.com/LSPosed/LSPatch) をインストール  
  ※フォークで開発されている [**NPatch**](https://github.com/HSSkyBoy/NPatch) では不具合が発生する可能性があります。  
  また、**LSPosed 公式** の LSPatch を利用してアプリがクラッシュする場合は、フォークで開発されている [**JingMatrix LSPatch**](https://github.com/JingMatrix/LSPatch/) を利用してパッチを適用すると正常に動作する場合があります。

2. **LSPatch** アプリを開き、<kbd>管理</kbd> > 右下の <kbd>＋</kbd> > <kbd>ストレージからapkを選択</kbd> >  先程ダウンロードした LI**N**E の APK を選択 > <kbd>ローカル</kbd> →   <kbd>パッチを開始</kbd> 

※[この方法](https://github.com/Chipppppppppp/LIME/issues/50#issuecomment-2174842592) を用いればトークの復元が可能なようです。

> [!TIP]
> <kbd>ディレクトリの選択</kbd>と出てきた場合は、<kbd>OK</kbd> を押してファイルピッカーを起動し、任意のディレクトリ下にフォルダを作成し、<kbd>このフォルダを使用</kbd> > <kbd>許可</kbd>を押す

3. [**Shizuku**](https://github.com/RikkaApps/Shizuku) を使用している場合は <kbd>インストール</kbd> を押して続行する  
  使用していない場合は、ファイルエクスプローラー等の別のアプリからインストールする

> [!IMPORTANT]
> 既に Playストア からインストールした LINE アプリがインストールされている場合は、署名が競合するため、最初にアンインストールを行ってください。


### 1. デバイス、アプリバージョンを偽装してログイン
この機能は自己責任です

### 3. Android ID を偽装する
この方法は**両方のデバイスを Root 化している**場合のみ可能です。  
<https://jesuscorona.hatenablog.com/entry/2019/02/10/010920> にあるように、メッセージの同期などに若干の遅れが生じることに注意が必要です。

<details>

- メリット：3 端末以上でもログイン可能・すべてのサービスを使用可能
- デメリット：メッセージの同期に遅れが生じる・Root 限定

#### 手順

1. LINE と LIME をインストールする
2. LINE ログイン画面で、「複数デバイスログイン (Android ID を偽装)」にチェックを入れる
3. <kbd>設定</kbd> > <kbd>アプリ</kbd> > <kbd>LINE</kbd> より、LINE アプリの設定画面から「強制停止」と「ストレージとキャッシュ」の「キャッシュを削除」をタップ
4. LINE アプリを再度開き、ログインする
5. ログイン後、[Swift Backup](https://play.google.com/store/apps/details?id=org.swiftapps.swiftbackup) を利用して LINE アプリをバックアップ (詳しくは[こちら](https://blog.hogehoge.com/2022/01/android-swift-backup.html))
6. Swift Backup のバックアップフォルダをもう一つの端末に移し、バックアップした LINE をインストール (詳しくは[こちら](https://blog.hogehoge.com/2022/05/SwiftBackup2.html))
7. LINE アプリを**開かず**に先に LIME をインストールする

</details>

## 問題の報告

新たなバグや修正方法を見つけた場合は、報告 をお願いします。

> [!NOTE]
> 日本語がわかる場合は日本語で記述してください。
