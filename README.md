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

# 画面スクリーンショット

<img width="20%" alt="Screenshot_20250819-215227" src="https://github.com/user-attachments/assets/3ce12715-f794-4434-8de2-c50baca0c584" />
<p>最初の画面です。</p>
<br />

<img width="20%" alt="Screenshot_20250819-215237" src="https://github.com/user-attachments/assets/10b88255-2abb-4e7e-82ae-81bf61ab07af" />
<p>入力エラーの画面です。</p>
<br />

<img width="20%" alt="Screenshot_20250819-215248" src="https://github.com/user-attachments/assets/e77ee82e-2f2f-4967-b832-975fd8f3b695" />
<p>施設の地図表示の画面です。</p>
<br />

<img width="20%" alt="Screenshot_20250819-215256" src="https://github.com/user-attachments/assets/a79f7dc3-eec0-4728-9545-d531d2cb37dd" />
<p>検索値の数値入力です。</p>
<br />

# 2025/08/19 火曜日 22:27 時点

このシステムが稼働するには、各施設ごとに受け入れ可能な高齢者や幼児の人数を持たなければいけません。LifeLine.php の方で管理システムにできるかもしれません。



