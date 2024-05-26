# <img src="app/src/main/ic_launcher-playstore.png" width="60px"> LIME: Adkiller for LINE

[![Latest Release](https://img.shields.io/github/v/release/Chipppppppppp/LIME?label=latest)](https://github.com/Chipppppppppp/LIME/releases/latest)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 概要

This is an Xposed Module to clean [**LINE**](https://line.me).

LINE を掃除する Xposed Module です。

## 使用方法
LINEアプリの <kbd>ホーム</kbd> > <kbd>⚙</kbd> から｢**設定**｣に入り、右上の｢**LIME**｣のボタンより開けます。

<details><summary>画像を閲覧</summary>

<a href="#"><img src="https://github.com/Chipppppppppp/LIME/assets/78024852/2f344ce7-1329-4564-b500-1dd79e586ea9" width="400px" alt="Sample screenshot"></a>

</details>

また、トーク画面右上の <kbd>⁝</kbd> からスイッチをオンにすると**未読のまま閲覧**できます。(このスイッチは設定で削除可能です）

※返信すると未読が解除されてしまうのでご注意ください

<details><summary>画像を閲覧</summary>

<a href="#"><img src="https://github.com/Chipppppppppp/LIME/assets/78024852/bd391a83-b041-4282-9eec-fe71b3b19aa0" width="400px" alt="Sample screenshot"></a>

</details>

## インストール

初めに、以下のサイトの中から、  
**LINE 14.3.2** と **LIME 1.8.0** の APK をダウンロードしてください｡

> [!IMPORTANT]
> 分割 APK は使用しないでください

LI**N**E
- [APKMirror](https://www.apkmirror.com/uploads/?appcategory=line)
- [APKPure](https://apkpure.net/jp/line-calls-messages/jp.naver.line.android/versions)
- [APKCombo](https://apkcombo.com/ja/line/jp.naver.line.android/old-versions/)

LI**M**E
- [Release](https://github.com/Chipppppppppp/LIME/releases/latest)

### Root 端末 (Magisk)

1. [**LSPosed**](https://github.com/LSPosed/LSPosed) をインストール
2. LI**N**E アプリと LI**M**E アプリを両方ともインストール
3. Google Play ストア や [Aurora Store](https://auroraoss.com) からの自動アップデートを防ぐために、[**Hide My Applist**](https://github.com/Dr-TSNG/Hide-My-Applist) で LINE アプリを隠す
4. LSPosed のモジュールから LIME に移動し、<kbd>モジュールの有効化</kbd> と LINE アプリにチェックを入れる

### 非 Root 端末

※非 Root 端末では､ Googleアカウント(ドライブ)を使用したトーク履歴の復元には対応していません。また、発/着信音が鳴らない可能性が有ります。

1. [**LSPatch**](https://github.com/LSPosed/LSPatch) をインストール  
  ※フォークで開発されている [**NPatch**](https://github.com/HSSkyBoy/NPatch) では不具合が発生する可能性があります
2. **LSPatch** アプリを開き、<kbd>管理</kbd> > 右下の<kbd>＋</kbd> > <kbd>ストレージからapkを選択</kbd> >  先程ダウンロードした LI**N**E の APK を選択 > <kbd>統合</kbd> → <kbd>モジュールを埋め込む</kbd> > <kbd>インストールされているアプリを選択</kbd> > LI**M**E にチェックを入れて <kbd>＋</kbd> > <kbd>パッチを開始</kbd> より、パッチを適用

> [!TIP]
> <kbd>ディレクトリの選択</kbd>と出てきた場合は、<kbd>OK</kbd> を押してファイルピッカーを起動し、任意のディレクトリ下にフォルダを作成し、<kbd>このフォルダを使用</kbd> > <kbd>許可</kbd>を押す

3. [**Shizuku**](https://github.com/RikkaApps/Shizuku) を使用している場合は <kbd>インストール</kbd> を押して続行する  
  使用していない場合は、ファイルエクスプローラー等の別のアプリからインストールする

> [!IMPORTANT]
> 既にPlay ストアからインストールした LINE アプリがインストールされている場合は、署名が競合するため、最初にアンインストールを行ってください。

## 複数デバイスログイン

複数デバイスログインには1つの方法が用意されています。どちらの方法でも、[WSA-Script](https://github.com/YT-Advanced/WSA-Script) を使用して Windows 上の Android で同様の操作を行うことで、Windows での複数デバイスログインが可能です。

~~1. PC としてログインする~~　現在この方法は機能していません　利用した場合、＊一時的にアカウントのご利用が停止されるおそれがあります

~~PC (Windows) 版 LINE に偽装します。これにより PC 版 LINE は強制ログアウトされますが、使えない機能がある PC 版 LINE を Android 版 LINE に移すことができます。~~

~~※片方のデバイスが iOS の場合、Letter Sealing がうまくいかずメッセージを受信できない場合があるので、[この方法](https://github.com/Chipppppppppp/LIME/issues/88#issuecomment-2012001059) に従って Letter Sealing ガチャを行ってください。(キーはだれかとのチャットの右上の <kbd>☰</kbd> > <kbd>設定</kbd> > <kbd>暗号化キー</kbd> から確認できます。)~~

~~- メリット：メッセージの同期に問題がない、LIME は片方の端末に入れるだけで良い、非 Root でも可能~~
~~- デメリット：3 端末以上でログインできない、2 端末目でサービスアイコンが表示されない~~

~~#### 手順~~

~~1. もう一つの端末に LINE と LIME をインストールする~~
~~2. LINE ログイン画面で、「PC (DESKTOPWIN) に偽装」にチェックを入れる~~
~~3. <kbd>設定</kbd> > <kbd>アプリ</kbd> > <kbd>LINE</kbd> より、LINE アプリの設定画面から「強制停止」と「ストレージとキャッシュ」の「キャッシュを削除」をタップ~~
~~4. LINE アプリを再度開き、「Log in as secondary device」をタップしてログインする~~
~~5. ログイン後、LINE の設定から「トークのバックアップ・復元」をタップし、2 週間より前のトークを復元する~~

### 2. Android ID を偽装する

この方法は**両方のデバイスを Root 化している**場合のみ可能です。<https://jesuscorona.hatenablog.com/entry/2019/02/10/010920> にあるように、メッセージの同期などに若干の遅れが生じることに注意が必要です。

- メリット：3 端末以上でもログイン可能、すべてのサービスを使用可能
- デメリット：メッセージの同期に遅れが生じる、Root 限定

#### 手順

1. LINE と LIME をインストールする
2. LINE ログイン画面で、「複数デバイスログイン (Android ID を偽装)」にチェックを入れる
3. <kbd>設定</kbd> > <kbd>アプリ</kbd> > <kbd>LINE</kbd> より、LINE アプリの設定画面から「強制停止」と「ストレージとキャッシュ」の「キャッシュを削除」をタップ
4. LINE アプリを再度開き、ログインする
5. ログイン後、[Swift Backup](https://play.google.com/store/apps/details?id=org.swiftapps.swiftbackup) を利用して LINE アプリをバックアップ (詳しくは[こちら](https://blog.hogehoge.com/2022/01/android-swift-backup.html))
6. Swift Backup のバックアップフォルダをもう一つの端末に移し、バックアップした LINE をインストール (詳しくは[こちら](https://blog.hogehoge.com/2022/05/SwiftBackup2.html))
7. LINE アプリを**開かず**に先に LIME をインストールする

## 問題の報告

新たなバグや修正方法を見つけた場合は、[報告](https://github.com/Chipppppppppp/LIME/issues/new/choose)をお願いします。

> [!NOTE]
> 日本語がわかる場合は日本語で記述してください。

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=Chipppppppppp/LIME&type=Date)](https://star-history.com/#Chipppppppppp/LIME&Date)
