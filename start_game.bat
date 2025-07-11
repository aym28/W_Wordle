@echo off
title Wordle Game Launcher

:: --- このバッチファイルについての説明 ---
echo ==========================================================
echo  Wordle 対戦ゲーム 自動起動バッチ
echo  1. Javaファイルのコンパイル
echo  2. サーバーの起動 (新しいウィンドウ)
echo  3. クライアント2つの起動 (新しいウィンドウ x2)
echo ==========================================================
echo.

:: --- ステップ1: Javaファイルのコンパイル ---
echo Javaファイルをコンパイルしています...
javac *.java

:: コンパイルエラーがないかチェック
if errorlevel 1 (
    echo.
    echo !!! コンパイル中にエラーが発生しました。!!!
    echo スクリプトを中断します。
    pause
    exit /b
)

echo コンパイルが正常に完了しました。
echo.

:: --- ステップ2: サーバーの起動 ---
echo サーバーを起動します...
start "Wordle Server" java WordleServer

:: サーバーが完全に起動するのを少し待つ
echo サーバーの初期化を待っています...
timeout /t 2 /nobreak > nul

:: --- ステップ3: クライアントの起動 ---
echo クライアントを2つ起動します...
start "Wordle Client 1" java WordleClient
start "Wordle Client 2" java WordleClient

echo.
echo 完了: サーバー1台とクライアント2台が起動しました。
echo このウィンドウは閉じます。
echo.