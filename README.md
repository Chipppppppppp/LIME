# <img src="app/src/main/ic_launcher-playstore.png" width="60px"> LIME: Adkiller for LINE

[![Latest Release](https://img.shields.io/github/v/release/Chipppppppppp/LIME?label=latest)](https://github.com/Chipppppppppp/LIME/releases/latest)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 概要

This is an Xposed Module to clean [**LINE**](https://line.me). It suppports **LINE 14.1.3**.

LINE を掃除する Xposed Module です。**LINE 14.1.3** に対応しています。

## 使用方法
LINEアプリの <kbd>ホーム</kbd> → <kbd>⚙</kbd> から｢**設定**｣に入り、右上の｢**LIME**｣のボタンより開けます。

<a href="#"><img src="https://github.com/Chipppppppppp/LIME/assets/78024852/5fbb4819-d14f-4f07-93d4-44b172bcf137" width="400px" alt="Sample screenshot"></a>

## インストール

### Root 端末

1. [**Magisk**](https://github.com/topjohnwu/Magisk) 及び [**LSPosed**](https://github.com/LSPosed/LSPosed) をインストール
2. LINE 14.1.3 をインストールしていない場合、ストアアプリでアップデートするか LINE をアンインストールして再度 LINE 14.1.3 の APK をインストールする

> [!TIP]
> LINE 14.1.3 の APK は次のいずれかからダウンロード可能：[APKMirror](https://www.apkmirror.com/uploads/?appcategory=line), [APKPure](https://apkpure.net/jp/line-calls-messages/jp.naver.line.android/versions), [APKCombo](https://apkcombo.com/ja/line/jp.naver.line.android/old-versions/)

4. Google Play や Aurora Store の自動アップデートを防ぐために、[**Hide My Applist**](https://github.com/Dr-TSNG/Hide-My-Applist) で LINE アプリを隠す
5. [Releases](https://github.com/Chipppppppppp/LIME/releases/latest) から APK をダウンロード & インストール
3. LSPosed のモジュールから LIME に移動し、<kbd>モジュールの有効化</kbd> と LINE アプリにチェックを入れる

### 非 Root 端末

1. [**LSPatch**](https://github.com/LSPosed/LSPatch) をインストール  
  ※フォークで開発されている [**NPatch**](https://github.com/HSSkyBoy/NPatch) では不具合が発生する可能性があります
2. [Releases](https://github.com/Chipppppppppp/LIME/releases/latest) から LI**M**E の APK をダウンロード & インストール
4. 以下のいずれかから LI**N**E 14.1.3 の APK を DL
  - [APKMirror](https://www.apkmirror.com/uploads/?appcategory=line)
  - [APKPure](https://apkpure.net/jp/line-calls-messages/jp.naver.line.android/versions)
  - [APKCombo](https://apkcombo.com/ja/line/jp.naver.line.android/old-versions/)
4. **LSPatch** アプリを開き、<kbd>管理</kbd> → 右下の<kbd>＋</kbd> → <kbd>ストレージからapkを選択</kbd> →  LI**N**E の APK を選択 → <kbd>統合</kbd> → <kbd>モジュールを埋め込む</kbd> → <kbd>インストールされているアプリを選択</kbd>→ LI**M**E にチェックを入れて <kbd>＋</kbd> → <kbd>パッチを開始</kbd> より、パッチを適用

> [!TIP]
> <kbd>ディレクトリの選択</kbd>と出てきた場合は、<kbd>OK</kbd>を押してファイルピッカーを起動し、任意のディレクトリ下にフォルダを作成し、<kbd>このフォルダを使用</kbd> → <kbd>許可</kbd>を押す

5. [**Shizuku**](https://github.com/RikkaApps/Shizuku) を使用している場合は <kbd>インストール</kbd> を押して続行する

> [!TIP]
> LINE を既にインストールしている場合は一旦アンインストールする

## 問題の報告

新たなバグや修正方法を見つけた場合は、[報告](https://github.com/Chipppppppppp/LIME/issues/new/choose)をお願いします。

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=Chipppppppppp/LIME&type=Date)](https://star-history.com/#Chipppppppppp/LIME&Date)
