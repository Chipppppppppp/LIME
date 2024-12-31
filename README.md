# <img src="app/src/main/ic_launcher-playstore.png" width="60px"> LIME: Adkiller for Beta LINEs

# 更新について
更新内容を記載していないReleaseについては、差分を押してください、必要でない更新でなければ無理して更新を行う必要はありません。
更新名による判断<br>
(例)v1.12.5

v1.12.6 →適応するLINEバージョンの変更<br>
v1.12.5a, v1.12.5a1→仕様変更

 
 # *端末別READ*
 
 [ROOT](https://github.com/areteruhiro/LIMEs/blob/master/README%20for%20root.md) 


 [LsPatch](https://github.com/areteruhiro/LIMEs/blob/master/README%20for%20LsPatch.md) 


## 確認済みのバグやエラー
アプリがクラッシュする→トラッキング通信をブロックするを無効にしてください

## トーク履歴のリストアについて

一度アプリをアンインストールした場合以下に従ってください。

①リストアしたいファイルを移動させる<br>
②LINEを開いてバックアップ<br>
③移動させたファイルの名前を`naver_line_backup.db`に変更する<br>
④LIME backup フォルダに入れ替え<br>
で、移動させる<br>
⑤リストアボタンを押す　

## トーク画像のリストアについて

①chats_backupフォルダを長押しして別のフォルダに移動<br>
②LINEを開いてトーク画像フォルダのバックアップを開始をクリック<br>
④別の場所にあるフォルダを　LIME backup フォルダに移動させ入れ替え<br>

⑤トーク画像のリストアボタンを押す　

ファイルエクスプローラーは以下を使用してください（エラーなどの報告に対応しやすくするためです）
https://play.google.com/store/apps/details?id=me.zhanghai.android.files


方法が怪しい場合以下の動画を参照してから、リストアを行ってください。(データが上書きされリストアできなくなる恐れがあります)
https://youtu.be/94JN4NLGdOI


## トーク履歴の自動バックアップについて

[Macro SAMPLE](https://drive.usercontent.google.com/u/0/uc?id=1rhZPmoMbti_l1JaX2EbjcRKUePkWlIXU&export=download)

または以下を参考にしてください
https://github.com/areteruhiro/LIMEs/issues/10



# 寄付
* [100円 PayPay](https://qr.paypay.ne.jp/p2p01_qIqiHEfm7jWiXaKd)<br>
* [300円
PayPay](https://qr.paypay.ne.jp/p2p01_oc9qTsEoIg8kn8Gy)<br>

* [Free yen
PayPay](https://qr.paypay.ne.jp/p2p01_oc9qTsEoIg8kn8Gy)<br>


* [Amazon Gift Card](https://www.amazon.co.jp/gp/product/B004N3APGO) Send to (limebeta.dev@gmail.com)<br>
* [GitHub Sponsors](https://github.com/sponsors/areteruhiro)


## 概要

このアプリで追加されている機能は、いずれPRするものがおおいですが、機能の追加を優先しているため、修正が必要なものが多く、このような形で公開させていただいております。

LIME開発者様 感謝しています


## Thank you

LIME  開発者
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

また、トーク画面上の <kbd>トグル又は✉️ボタン</kbd> かオン（緑または、未開封）にすると**未読のまま閲覧**ができます。(このスイッチは設定で削除可能です）

※返信すると未読が解除されてしまうのでご注意ください

<details><summary>画像を閲覧</summary>
<img src="https://github.com/user-attachments/assets/a9ee3b95-f785-4fac-9937-b904fe84f7b2" width="400px" alt="Sample screenshot">
</details>


## 機能

- 不要なボトムバーのアイコンの削除
- ボトムバーのアイコンのラベルの削除
- 広告・おすすめの削除
- サービスのラベルを削除
- 通知の「通知をオフ」アクションを削除
- WebView を既定のブラウザで開く
  <br>
- 送信取り消しの拒否
　-add 取り消されたメッセージの保存機能 
- 常にミュートメッセージとして送信
  - 送信時「通常メッセージ」を選択すれば通知されます
- トラッキング通信のブロック(この機能は自己責任です)
  - `noop`, `pushRecvReports`, `reportDeviceState`, `reportLocation`, `reportNetworkStatus` がブロックされます
- 通信内容をログに出力
- 通信内容を改変
 [- JavaScript で通信内容を改変できます](https://github.com/areteruhiro/LIMEs/blob/master/JavaRead.md)
  <br>
- ナビゲーションバーを黒色に固定化、ブラックテーマをナチュラルブラックに変更
- 非表示にしたチャットの再表示を無効化
- LsPatch用　着信音を鳴らす
- サービスの項目の削除
    <br>
- 未読のまま閲覧ボタンの位置調整
- 常に既読をつけないの仕様変更
  <br>
- トークのバックアップ、リストア
- 既読者の確認 
- 音声ボタンの無効化
- 通知に画像を添付、通知を更新させない機能
  -添付される画像 が別のものになる場合　DOWNLOADの LIMEbackp のwait_time.txtでミリ秒で調整できます
- 登録しているグループ名の通知を無効化
  →ONにしている間に招待されたグループが自動で追加されます。
- 更新されたプロフィールの削除

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



