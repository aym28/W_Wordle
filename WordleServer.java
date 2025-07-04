import java.io.*;
import java.net.*;

public class WordleServer {
    // ポート番号
    public static final int PORT = 8080;
    // ゲーム設定
    public static final int MAX_GAME_ROUND = 6;
    public static final int WORD_SIZE = 5;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Wordleサーバーを起動しました。ポート: " + PORT);
        System.out.println("2人のプレイヤーの接続を待っています...");

        try {
            // プレイヤーAの接続を待つ
            Socket playerASocket = serverSocket.accept();
            System.out.println("プレイヤーAが接続しました: " + playerASocket);
            PrintWriter outA = new PrintWriter(playerASocket.getOutputStream(), true);
            BufferedReader inA = new BufferedReader(new InputStreamReader(playerASocket.getInputStream()));
            outA.println("プレイヤーAとして接続しました。対戦相手を待っています...");

            // プレイヤーBの接続を待つ
            Socket playerBSocket = serverSocket.accept();
            System.out.println("プレイヤーBが接続しました: " + playerBSocket);
            PrintWriter outB = new PrintWriter(playerBSocket.getOutputStream(), true);
            BufferedReader inB = new BufferedReader(new InputStreamReader(playerBSocket.getInputStream()));
            outB.println("プレイヤーBとして接続しました。");

            // --- ゲームセッション開始 ---
            outA.println("対戦相手が見つかりました。ゲームを開始します。");
            outB.println("対戦相手が見つかりました。ゲームを開始します。");
            System.out.println("ゲームセッションを開始します。");

            // 1. 各プレイヤーにお題となる単語を【並行して】決めさせる
            // スレッド間で単語を受け渡すための配列
            final String[] answerWords = new String[2]; // [0] = PlayerA入力, [1] = PlayerB入力
            WordList wordList = new WordList();

            // Player Aの単語入力を担当するスレッド
            Thread threadA = new Thread(() -> {
                try {
                    outA.println("対戦相手のお題となる単語(5文字)を入力してください。");
                    while (true) {
                        String word = inA.readLine();
                        if (word != null && wordList.isInList(word.toLowerCase())) {
                            answerWords[0] = word.toLowerCase(); // Player Bが当てる単語
                            break;
                        }
                        outA.println("エラー: その単語はリストにありません。もう一度入力してください。");
                    }
                    outA.println("お題を設定しました。相手の入力を待っています...");
                } catch (IOException e) {
                    System.err.println("PlayerAとの通信中にエラーが発生しました: " + e.getMessage());
                }
            });

            // Player Bの単語入力を担当するスレッド
            Thread threadB = new Thread(() -> {
                try {
                    outB.println("対戦相手のお題となる単語(5文字)を入力してください。");
                    while (true) {
                        String word = inB.readLine();
                        if (word != null && wordList.isInList(word.toLowerCase())) {
                            answerWords[1] = word.toLowerCase(); // Player Aが当てる単語
                            break;
                        }
                        outB.println("エラー: その単語はリストにありません。もう一度入力してください。");
                    }
                    outB.println("お題を設定しました。相手の入力を待っています...");
                } catch (IOException e) {
                    System.err.println("PlayerBとの通信中にエラーが発生しました: " + e.getMessage());
                }
            });

            // 両方のスレッドを開始
            threadA.start();
            threadB.start();

            // 両方のスレッドが終了するまで待つ
            try {
                threadA.join();
                threadB.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // スレッドから受け取った単語を変数に格納
            String ansStrB = answerWords[0]; // PlayerBが当てるべき単語
            String ansStrA = answerWords[1]; // PlayerAが当てるべき単語
            
            // 2. ゲームの準備
            Player playerA = new Player(MAX_GAME_ROUND, WORD_SIZE, ansStrA);
            Player playerB = new Player(MAX_GAME_ROUND, WORD_SIZE, ansStrB);
            boolean winA = false;
            boolean winB = false;

            outA.println("------------------------------\nGame Start\n------------------------------");
            outB.println("------------------------------\nGame Start\n------------------------------");

            // 3. メインゲームループ
            for (int gameCount = 0; gameCount < MAX_GAME_ROUND; gameCount++) {
                // PlayerAのターン
                outA.printf("(ROUND %d) PlayerA: 単語を入力してください。\n", gameCount + 1);
                outB.printf("(ROUND %d) 相手のターンです。待機してください...\n", gameCount + 1);
                String guessA;
                while (true) {
                    guessA = inA.readLine();
                    if (guessA != null && wordList.isInList(guessA.toLowerCase())) break;
                    outA.println("エラー: その単語はリストにありません。もう一度入力してください。");
                }
                playerA.pushAnswer(guessA.toLowerCase(), gameCount);
                outA.println(playerA.getAnswerSheetString());
                winA = playerA.judgeAnswer(guessA.toLowerCase());
                outA.println("------------------------------");

                // PlayerBのターン
                outB.printf("(ROUND %d) PlayerB: 単語を入力してください。\n", gameCount + 1);
                outA.printf("(ROUND %d) 相手のターンです。待機してください...\n", gameCount + 1);
                String guessB;
                while (true) {
                    guessB = inB.readLine();
                    if (guessB != null && wordList.isInList(guessB.toLowerCase())) break;
                    outB.println("エラー: その単語はリストにありません。もう一度入力してください。");
                }
                playerB.pushAnswer(guessB.toLowerCase(), gameCount);
                outB.println(playerB.getAnswerSheetString());
                winB = playerB.judgeAnswer(guessB.toLowerCase());
                outB.println("------------------------------");

                // 勝利判定
                if (winA || winB) break;
            }

            // 4. 結果発表
            String finalMessageA, finalMessageB;
            if (winA && winB) {
                finalMessageA = "引き分けです。両者正解！";
                finalMessageB = "引き分けです。両者正解！";
            } else if (winA) {
                finalMessageA = "あなたの勝利です！";
                finalMessageB = "あなたの負けです...。相手は正解しました。";
            } else if (winB) {
                finalMessageA = "あなたの負けです...。相手は正解しました。";
                finalMessageB = "あなたの勝利です！";
            } else {
                finalMessageA = "引き分けです。両者時間切れ。";
                finalMessageB = "引き分けです。両者時間切れ。";
            }

            // 各プレイヤーに結果と正解の単語を送信
            outA.println(finalMessageA);
            outA.println("あなたが当てる単語は「" + ansStrA + "」でした。");
            outA.println("相手が当てる単語は「" + ansStrB + "」でした。");

            outB.println(finalMessageB);
            outB.println("あなたが当てる単語は「" + ansStrB + "」でした。");
            outB.println("相手が当てる単語は「" + ansStrA + "」でした。");

            // 5. 接続を閉じる
            System.out.println("ゲームセッションを終了します。");
            playerASocket.close();
            playerBSocket.close();

        } finally {
            serverSocket.close();
            System.out.println("サーバーをシャットダウンしました。");
        }
    }
}
