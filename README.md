# <img src="app/src/main/ic_launcher-playstore.png" width="60px"> LIME: Adkiller for LINE

[![Latest Release](https://img.shields.io/github/v/release/Chipppppppppp/LIME?label=latest)](https://github.com/Chipppppppppp/LIME/releases/latest)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 概要

This is an Xposed Module to clean [**LINE**](https://line.me).

LINE を掃除する Xposed Module です。

## 使用方法
LINEアプリの <kbd>ホーム</kbd> → <kbd>⚙</kbd> から｢**設定**｣に入り、右上の｢**LIME**｣のボタンより開けます。

<a href="#"><img src="https://github.com/Chipppppppppp/LIME/assets/78024852/8bdb25b6-e3be-49a7-adfc-0e2e925cdb03" width="400px" alt="Sample screenshot"></a>

## インストール

### Root 端末

1. [**Magisk**](https://github.com/topjohnwu/Magisk) 及び [**LSPosed**](https://github.com/LSPosed/LSPosed) をインストール
2. [Releases](https://github.com/Chipppppppppp/LIME/releases/latest) から APK を DL & インストール
> [!TIP]
> Play プロテクトによりブロックされた場合、<kbd>詳細</kbd> から <kbd>インストールする</kbd> をタップ
3. LSPosed のモジュールから LIME に移動し、<kbd>モジュールの有効化</kbd> と LINE アプリにチェックを入れる

### 非 Root 端末

1. [**LSPatch**](https://github.com/LSPosed/LSPatch) をインストール  
  ※フォークで開発されている [**NPatch**](https://github.com/HSSkyBoy/NPatch) では不具合が発生する可能性があります
2. [Releases](https://github.com/Chipppppppppp/LIME/releases/latest) から LI**M**E の APK を DL
3. 以下のリストの中から LI**N**E の APK を DL
  - [APKMirror](https://www.apkmirror.com/uploads/?appcategory=line)
  - [APKPure](https://apkpure.net/jp/line-calls-messages/jp.naver.line.android/versions)
  - [APKCombo](https://apkcombo.com/ja/line/jp.naver.line.android/old-versions/)
  - [LINE 公式](https://line-android-universal-download.line-scdn.net/line-apk-download.html) ※バージョン選択不可
4. **LSPatch** アプリを開き、<kbd>管理</kbd> → 右下の<kbd>＋</kbd> → <kbd>ストレージからapkを選択</kbd> →  LI**N**E の APK を選択 → <kbd>統合</kbd> → <kbd>モジュールを埋め込む</kbd> → <kbd>ストレージからapkを選択</kbd>→ LI**M**E の APK を選択 → <kbd>パッチを開始</kbd> より、パッチを適用

> [!TIP]
> <kbd>ディレクトリの選択</kbd>と出てきた場合は、<kbd>OK</kbd>を押してファイルピッカーを起動し、任意のディレクトリ下にフォルダを作成し、<kbd>このフォルダを使用</kbd> → <kbd>許可</kbd>を押す

5. [**Shizuku**](https://github.com/RikkaApps/Shizuku) を使用している場合は <kbd>インストール</kbd> を押して続行する

> [!WARNING]
> Google アカウントでのログインを行いたい場合は、**ADB** または [Termux](https://termux.dev) 等で **Shizuku** を用いてインストールする必要があります。  
> ADB: `adb install -i com.android.vending -r LINE-lspatched.apk`  
> Shizuku (shell): `pm install -i com.android.vending -r LINE-lspatched.apk`  
> ※内部シェルの場合は、`/data/local/tmp` に APK をコピーしてからインストールしてください

## 問題の報告

新たなバグや修正方法を見つけた場合は、[報告](https://github.com/Chipppppppppp/LIME/issues/new/choose)をお願いします。

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=Chipppppppppp/LIME&type=Date)](https://star-history.com/#Chipppppppppp/LIME&Date)
