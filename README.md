# <img src="app/src/main/ic_launcher-playstore.png" width="60px"> LIME: Adkiller for Beta LINEs

 # 導入方法
 
 [ROOT](https://github.com/areteruhiro/LIMEs/blob/master/README%20for%20root.md) 


 [LsPatch](https://github.com/areteruhiro/LIMEs/blob/master/README%20for%20LsPatch.md) 
 

## 概要

このアプリで追加されている機能は、いずれPRするものがおおいですが、機能の追加を優先しているため、修正が必要なものが多く、このような形で公開させていただいております。

[機能リスト](https://github.com/areteruhiro/LIMEs/blob/master/FunctionLIST.md) 
 


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


以下の方々のおかげで開発を継続できています。大変感謝しています。<br>
We are very grateful to the following people for making this possible:

@Akira Kansaki
@ハチワレ
@WE ZARD 

継続した開発時間の確保のため寄付のほどお願いいたします。<br>
Please donate to ensure continued development time.

# 寄付
* [100円 PayPay](https://qr.paypay.ne.jp/p2p01_qIqiHEfm7jWiXaKd)<br>
* [300円
PayPay](https://qr.paypay.ne.jp/p2p01_oc9qTsEoIg8kn8Gy)<br>

* [Free yen
PayPay](https://qr.paypay.ne.jp/p2p01_oc9qTsEoIg8kn8Gy)<br>


* [Amazon Gift Card](https://www.amazon.co.jp/gp/product/B004N3APGO) Send to (limebeta.dev@gmail.com)<br>
* [GitHub Sponsors](https://github.com/sponsors/areteruhiro)

* [PayPal](Contact us / お問い合わせください)


# Support Server
https://t.me/LsPosedLIMEs

# List of donors


# 更新について
更新内容を記載していないReleaseについては、差分を押してください、必要でない更新でなければ無理して更新を行う必要はありません。
更新名による判断<br>
(例)v1.12.5

v1.12.6 →適応するLINEバージョンの変更<br>
v1.12.5a, v1.12.5a1→仕様変更


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

To foreigners, please translate your report into English and submit it rather than translating it into Japanese.



