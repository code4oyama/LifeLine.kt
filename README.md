# LifeLine.kt プロジェクト設定ガイド

## 必要な環境変数

プロジェクトを正しく動作させるために、以下のAPIエンドポイントURLを設定してください。

- `sakuraApiUrl`  
  例: `https://example.sakura.ne.jp/api/`

- `apiUrl`  
  例: `https://example.com/api/`

これらはアプリの通信先APIのURLです。環境や用途に応じて適切な値を設定してください。

---

## Google Maps APIキーの設定


`AndroidManifest.xml` 内で、Google Maps APIキーを以下のように設定する必要があります。

```xml
<meta-data
  android:name="com.google.android.geo.API_KEY"
  android:value="YOUR_API_KEY" />
```

- `android:value` の部分に、Google Cloud Consoleで取得した有効なAPIキー（例: `AIza...`）を設定してください。
- APIキーは、Google Cloud Consoleで「Maps SDK for Android」を有効化し、SHA-1証明書フィンガープリントとパッケージ名を正しく登録したものを使用してください。

---

## 注意事項

- APIキーやURLは、セキュリティの観点から外部に漏れないよう管理してください。
- 詳細な設定手順は [Google公式ドキュメント](https://developers.google.com/maps/documentation/android-sdk/start) を参照してください。
