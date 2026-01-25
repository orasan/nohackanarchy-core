# NohackAnarchy-Core

NohackAnarchyサーバー用のコアプラグインです。
基本的なサーバー管理機能、チャット管理、無視機能、各種表示切替機能を提供します。

## 機能
- **コア機能**: プラグイン管理 (`/nhancore reload`)
- **ユーティリティ**: セルフキル (`/suicide`)
- **無視機能**: プレイヤーや特定の単語を含むチャットを非表示 (`/ignore`, `/ignoreword`)
- **表示切替**: チャット、死亡ログ、実績通知の表示/非表示 (`/togglechat`, `/toggledeathmsgs`, `/toggleadv`)
- **拡張設定**: `config.yml` による詳細設定

## インストール
1. 最新の `.jar` ファイルをサーバーの `plugins` フォルダに配置します。
2. サーバーを再起動します。

## 開発
- Java 21
- Gradle
- Paper API 1.21

## ビルド
```bash
./gradlew build
```
ビルド成果物は `build/libs` に生成されます。
