## 導入の仕方

1. [**JingMatrix LSPatch**](https://github.com/JingMatrix/LSPatch/) をインストール

1.5パッチするapkについて
LINE 14.19.1 <br>

・arm64-v8a + armeabi-v7a<br>（基本的にこれでok）
https://www.apkmirror.com/apk/line-corporation/line/line-14-19-1-release/

クラッシュやなんらかのエラーが発生した場合、自分の端末のアーキテクチャに対応するファイルをダウンロード

[アーキテクチャの確認](https://play.google.com/store/apps/details?id=com.ytheekshana.deviceinfo)→CPU→サーポートされているABI

・armeabi-v7a
https://line-android-universal-download.line-scdn.net/line-14.19.1.apk

・arm64-v8a https://d.apkpure.com/b/XAPK/jp.naver.line.android?versionCode=141910383&nc=arm64-v8a&sv=28


M apk tool <br>
https://maximoff.su/apktool/?lang=en
でapkに変換してからパッチしてください

エラーが発生する場合以下からダウンロード<br>
https://t.me/LsPosedLIMEs/36/83


2. **LSPatch** アプリを開き、<kbd>管理</kbd> > 右下の <kbd>＋</kbd> > <kbd>ストレージからapkを選択</kbd> >  先程ダウンロードした LI**N**E の APK を選択 > <kbd>統合</kbd> → <kbd>モジュールを埋め込む</kbd> > <kbd>インストールされているアプリを選択</kbd> > LI**M**E にチェックを入れて <kbd>＋</kbd> > <kbd>パッチを開始</kbd> より、パッチを適用

※[この方法](https://github.com/Chipppppppppp/LIME/issues/50#issuecomment-2174842592) を用いればトークの復元が可能なようです。

> [!TIP]
> <kbd>ディレクトリの選択</kbd>と出てきた場合は、<kbd>OK</kbd> を押してファイルピッカーを起動し、任意のディレクトリ下にフォルダを作成し、<kbd>このフォルダを使用</kbd> > <kbd>許可</kbd>を押す

3. [**Shizuku**](https://github.com/RikkaApps/Shizuku) を使用している場合は <kbd>インストール</kbd> を押して続行する  
  使用していない場合は、ファイルエクスプローラー等の別のアプリからインストールする


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
