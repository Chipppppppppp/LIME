# <img src="app/src/main/ic_launcher-playstore.png" width="60px"> LIME: Adkiller for LINE

[![Latest Release](https://img.shields.io/github/v/release/Chipppppppppp/LIME?label=latest)](https://github.com/Chipppppppppp/LIME/releases)
[![License](https://img.shields.io/github/license/Chipppppppppp/LIME)](https://github.com/Chipppppppppp/LIME/blob/main/LICENSE)

## 概要

This is an Xposed Module to clean LINE. 

LINE を掃除する Xposed Module です。

## インストール

- Magisk, [LSPosed](https://github.com/LSPosed/LSPosed) をインストール
- [Releases](https://github.com/Chipppppppppp/LIME/releases) から apk ファイルをインストール
  - Play プロテクトによりブロックされた場合、「詳細」から「インストールする」をタップ
- LSPosed のモジュールから LIME に移動し、「モジュールの有効化」と LINE アプリにチェックを入れる

## 機能

- Delete the VOOM icon: 画面下部の VOOM アイコンの削除
- Delete ads: 広告の削除
- Redirect Web View: Web View を好きなブラウザにリダイレクト
  - Open in your browser: ブラウザで開く

## 既知の問題

- 稀にホームタブの広告の空白が残る
- Web View を Custom Tabs にリダイレクトした場合、下部に余白が生じる
- LSPosed でのアイコンがデフォルトになっている

新たなバグや修正方法を見つけた場合報告をお願いします。
