# <img src="app/src/main/ic_launcher-playstore.png" width="60px"> LIME: Adkiller for LINE

[![Latest Release](https://img.shields.io/github/v/release/Chipppppppppp/LIME?label=latest)](https://github.com/Chipppppppppp/LIME/releases/latest)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 概要

<!--
README に直接対応バージョンを書く場合は、インストール章のリリースタグやAPKダウンロードサイトのURLを固定してください。
-->

This is an Xposed Module to clean [**LINE**](https://line.me). It supports **LINE 14.2.0**.

LINE を掃除する Xposed Module です。**LINE 14.2.0** に対応しています。

## 使用方法
LINEアプリの <kbd>ホーム</kbd> → <kbd>⚙</kbd> から｢**設定**｣に入り、右上の｢**LIME**｣のボタンより開けます。

<details><summary>画像を閲覧</summary>

<a href="#"><img src="https://github.com/Chipppppppppp/LIME/assets/78024852/2f344ce7-1329-4564-b500-1dd79e586ea9" width="400px" alt="Sample screenshot"></a>

</details>

また、トーク画面右上のメニューからスイッチをオンにすると**未読のまま閲覧**できます。(このスイッチは設定で削除可能です）

※返信すると未読が解除されてしまうのでご注意ください

<details><summary>画像を閲覧</summary>

<a href="#"><img src="https://github.com/Chipppppppppp/LIME/assets/78024852/bd391a83-b041-4282-9eec-fe71b3b19aa0" width="400px" alt="Sample screenshot"></a>

</details>

## インストール

初めに、以下のサイトの中から、**LINE 14.2.0** と **LIME 1.7.0** の APK をダウンロードしてください｡

> [!IMPORTANT]
> 分割 APK は使用しないでください

<!-- バージョンリスト
- [APKMirror](https://www.apkmirror.com/uploads/?appcategory=line)
- [APKPure](https://apkpure.net/jp/line-calls-messages/jp.naver.line.android/versions)
- [APKCombo](https://apkcombo.com/ja/line/jp.naver.line.android/old-versions/)
-->
LINE 14.2.0
- [APKMirror](https://www.apkmirror.com/apk/line-corporation/line/line-14-2-0-release/line-calls-messages-14-2-0-2-android-apk-download/)
- [APKPure](https://d.apkpure.net/b/APK/jp.naver.line.android?versionCode=140200233&nc=arm64-v8a%2Carmeabi-v7a&sv=26) (直リンク)
- [APKCombo](https://apkcombo.com/ja/line/jp.naver.line.android/download/phone-14.2.0-apk)

LIME 1.7.0
- [Release](https://github.com/Chipppppppppp/LIME/releases/download/v1.7.0/LIME-v1.7.0apk) (直リンク)

### Root 端末 (Magisk)

1. [**LSPosed**](https://github.com/LSPosed/LSPosed) をインストール
2. LI**N**E アプリと LI**M**E アプリを両方ともインストール
3. Google Playストア や [Aurora Store](https://auroraoss.com) からの自動アップデートを防ぐために、[**Hide My Applist**](https://github.com/Dr-TSNG/Hide-My-Applist) で LINE アプリを隠す
4. LSPosed のモジュールから LIME に移動し、<kbd>モジュールの有効化</kbd> と LINE アプリにチェックを入れる

### 非 Root 端末

※非 Root では二週間より前のトークバックアップにまだ対応していません。

1. [**LSPatch**](https://github.com/LSPosed/LSPatch) をインストール  
  ※フォークで開発されている [**NPatch**](https://github.com/HSSkyBoy/NPatch) では不具合が発生する可能性があります
2. **LSPatch** アプリを開き、<kbd>管理</kbd> → 右下の<kbd>＋</kbd> → <kbd>ストレージからapkを選択</kbd> →  先程ダウンロードした LI**N**E の APK を選択 → <kbd>統合</kbd> → <kbd>モジュールを埋め込む</kbd> → <kbd>インストールされているアプリを選択</kbd> → LI**M**E にチェックを入れて <kbd>＋</kbd> → <kbd>パッチを開始</kbd> より、パッチを適用

> [!TIP]
> <kbd>ディレクトリの選択</kbd>と出てきた場合は、<kbd>OK</kbd>を押してファイルピッカーを起動し、任意のディレクトリ下にフォルダを作成し、<kbd>このフォルダを使用</kbd> → <kbd>許可</kbd>を押す

3. [**Shizuku**](https://github.com/RikkaApps/Shizuku) を使用している場合は <kbd>インストール</kbd> を押して続行する  
  使用していない場合は、ファイルエクスプローラー等の別のアプリからインストールする

> [!IMPORTANT]
> 既にPlay ストアからインストールした LINE アプリがインストールされている場合は、署名が競合するため、最初にアンインストールを行ってください。

## 複数デバイスログイン

両方のデバイスを Root 化している場合、複数デバイスログインが可能です。また、[WSA-Script](https://github.com/YT-Advanced/WSA-Script) を使用して Windows 上の Android で同様の操作を行うことで、Windows での複数デバイスログインも可能です。

手順：

1. LINE と LIME をインストールする
1. LINE ログイン画面で、「複数デバイスログイン (Android ID を偽装)」にチェックを入れてログイン
2. ログイン後、[Swift Backup](https://play.google.com/store/apps/details?id=org.swiftapps.swiftbackup) を利用して LINE アプリをバックアップ (詳しくは[こちら](https://blog.hogehoge.com/2022/01/android-swift-backup.html))
3. SwiftBackup のバックアップフォルダをもう一つの端末に移し、バックアップした LINE をインストール (詳しくは[こちら](https://blog.hogehoge.com/2022/05/SwiftBackup2.html))
4. もう一つの端末に LIME をインストールする

※https://jesuscorona.hatenablog.com/entry/2019/02/10/010920 にあるように、メッセージの同期などに遅れが生じます。

## 問題の報告

新たなバグや修正方法を見つけた場合は、[報告](https://github.com/Chipppppppppp/LIME/issues/new/choose)をお願いします。

> [!NOTE]
> 日本語がわかる場合は日本語で記述してください。

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=Chipppppppppp/LIME&type=Date)](https://star-history.com/#Chipppppppppp/LIME&Date)
