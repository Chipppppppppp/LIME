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
