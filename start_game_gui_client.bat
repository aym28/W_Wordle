@echo off
title Wordle Game Launcher

:: エンコードの問題を解決
chcp 65001

echo ==========================================================
echo  Wordle 対戦ゲーム GUI 自動起動バッチ
echo ==========================================================
echo.

:: --- ステップ1: Javaファイルのコンパイル ---
echo srcフォルダ内のJavaファイルをコンパイルしています...
javac -d bin src/*.java

:: コンパイルエラーがないかチェック
if errorlevel 1 (
    echo.
    echo !!! コンパイル中にエラーが発生しました。!!!
    echo スクリプトを中断します。
    pause
    exit /b
)

echo コンパイルが正常に完了しました。（.classファイルはbinフォルダに出力）
echo.

:: --- ステップ2: クライアントの起動 ---
echo クライアントを1つ起動します...
start /MIN "Wordle Client 1" java -cp bin;res W_Wordle_UI_control

echo.
echo 完了: サーバー1台が起動しました。
echo このウィンドウは自動で閉じます。
echo.