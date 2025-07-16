import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WordleServer {
    // ポート番号
    public static final int PORT = 8080;
    // ゲーム設定
    public static final int MAX_GAME_ROUND = 6;
    public static final int WORD_SIZE = 5;

    // --- 処理メソッドからアクセスできるよう、クラス変数として定義 ---
    private static Player playerA;
    private static Player playerB;
    private static PrintWriter outA;
    private static PrintWriter outB;
    private static BufferedReader inA;
    private static BufferedReader inB;
    private static WordList wordList;
    private static AnswerWordList answerWordList;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Wordleサーバーを起動しました。ポート: " + PORT);
        System.out.println("2人のプレイヤーの接続を待っています...");

        try {
            // プレイヤーAの接続を待つ
            Socket playerASocket = serverSocket.accept();
            outA = new PrintWriter(playerASocket.getOutputStream(), true);
            inA = new BufferedReader(new InputStreamReader(playerASocket.getInputStream()));
            System.out.println("プレイヤーAが接続しました: " + playerASocket);
            outA.println("プレイヤーAとして接続しました。対戦相手を待っています...");

            // プレイヤーBの接続を待つ
            Socket playerBSocket = serverSocket.accept();
            outB = new PrintWriter(playerBSocket.getOutputStream(), true);
            inB = new BufferedReader(new InputStreamReader(playerBSocket.getInputStream()));
            System.out.println("プレイヤーBが接続しました: " + playerBSocket);
            outB.println("プレイヤーBとして接続しました。");

            // --- ゲームセッション開始 ---
            outA.println("対戦相手が見つかりました。ゲームを開始します。");
            outB.println("対戦相手が見つかりました。ゲームを開始します。");
            System.out.println("ゲームセッションを開始します。");

            // 1. 各プレイヤーにお題となる単語を【並行して】決めさせる
            final String[] answerWords = new String[2];
            wordList = new WordList();
            answerWordList = new AnswerWordList();

            Thread threadA = new Thread(() -> {
                try {
                    outA.println("対戦相手のお題となる単語(5文字)を入力してください。|PROMPT");
                    while (true) {
                        String word = inA.readLine();
                        if (word != null && word.length() == WORD_SIZE && answerWordList.isInList(word.toLowerCase())) {
                            answerWords[0] = word.toLowerCase();
                            break;
                        }
                        outA.println("エラー: その単語はお題に設定できません。もう一度入力してください。|PROMPT");
                    }
                    outA.println("お題を設定しました。相手の入力を待っています...");
                } catch (IOException e) {
                    System.err.println("PlayerAとの通信中にエラーが発生しました: " + e.getMessage());
                }
            });

            Thread threadB = new Thread(() -> {
                try {
                    outB.println("対戦相手のお題となる単語(5文字)を入力してください。|PROMPT");
                    while (true) {
                        String word = inB.readLine();
                        if (word != null && word.length() == WORD_SIZE && answerWordList.isInList(word.toLowerCase())) {
                            answerWords[1] = word.toLowerCase();
                            break;
                        }
                        outB.println("エラー: その単語はお題に設定できません。もう一度入力してください。|PROMPT");
                    }
                    outB.println("お題を設定しました。相手の入力を待っています...");
                } catch (IOException e) {
                    System.err.println("PlayerBとの通信中にエラーが発生しました: " + e.getMessage());
                }
            });

            threadA.start();
            threadB.start();
            try {
                threadA.join();
                threadB.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String ansStrB = answerWords[0];
            String ansStrA = answerWords[1];

            // 2. ゲームの準備
            playerA = new Player(MAX_GAME_ROUND, WORD_SIZE, ansStrA);
            playerB = new Player(MAX_GAME_ROUND, WORD_SIZE, ansStrB);
            boolean winA = false;
            boolean winB = false;

            outA.println("Game Start");
            outB.println("Game Start");

            // 3. メインゲームループ
            for (int gameCount = 0; gameCount < MAX_GAME_ROUND; gameCount++) {
                playerA.addPoints(10);
                playerA.resetTurnState();
                playerB.addPoints(10);
                playerB.resetTurnState();

                // PlayerAのターン
                winA = executePlayerTurn(playerA, playerB, outA, outB, inA, gameCount, "PlayerA");
                // ダブルムーブの処理
                if (playerA.hasDoubleMove()) {
                    playerA.consumeDoubleMove();
                    if (!winA) {
                        outA.println("\n--- ダブルムーブ！ ---");
                        winA = executePlayerTurn(playerA, playerB, outA, outB, inA, gameCount, "PlayerA");
                    }
                }
                if (winA) break;

                // PlayerBのターン
                winB = executePlayerTurn(playerB, playerA, outB, outA, inB, gameCount, "PlayerB");
                // ダブルムーブの処理
                if (playerB.hasDoubleMove()) {
                    playerB.consumeDoubleMove();
                    if (!winB) {
                        outB.println("\n--- ダブルムーブ！ ---");
                        winB = executePlayerTurn(playerB, playerA, outB, outA, inB, gameCount, "PlayerB");
                    }
                }
                if (winB) break;
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
            
            // Player A 自身のお題の結果表示
            if (!playerA.getOriginalAnswer().equals(playerA.answer)) {
                finalMessageA = finalMessageA.concat("/nあなたが当てる単語は当初「");
                finalMessageA = finalMessageA.concat(playerA.getOriginalAnswer());
                finalMessageA = finalMessageA.concat("」でしたが、カオスチェンジにより「");
                finalMessageA = finalMessageA.concat(playerA.answer);
                finalMessageA = finalMessageA.concat("」に変更されました。");
            } else {
                finalMessageA = finalMessageA.concat("/nあなたが当てる単語は「");
                finalMessageA = finalMessageA.concat(playerA.answer);
                finalMessageA = finalMessageA.concat("」でした。");
            }
            // Player A にとっての相手のお題の結果表示
            if (!playerB.getOriginalAnswer().equals(playerB.answer)) {
                finalMessageA = finalMessageA.concat("/n相手が当てる単語は当初「");
                finalMessageA = finalMessageA.concat(playerB.getOriginalAnswer());
                finalMessageA = finalMessageA.concat("」でしたが、カオスチェンジにより「");
                finalMessageA = finalMessageA.concat(playerB.answer);
                finalMessageA = finalMessageA.concat("」に変更されました。");
            } else {
                finalMessageA = finalMessageA.concat("/n相手が当てる単語は「");
                finalMessageA = finalMessageA.concat(playerB.answer);
                finalMessageA = finalMessageA.concat("」でした。");
            }
            outA.println(finalMessageA);

            
            // Player B 自身のお題の結果表示
            if (!playerB.getOriginalAnswer().equals(playerB.answer)) {
                finalMessageB = finalMessageB.concat("/nあなたが当てる単語は当初「");
                finalMessageB = finalMessageB.concat(playerB.getOriginalAnswer());
                finalMessageB = finalMessageB.concat("」でしたが、カオスチェンジにより「");
                finalMessageB = finalMessageB.concat(playerB.answer);
                finalMessageB = finalMessageB.concat("」に変更されました。");
            } else {
                finalMessageB = finalMessageB.concat("/nあなたが当てる単語は「");
                finalMessageB = finalMessageB.concat(playerB.answer);
                finalMessageB = finalMessageB.concat("」でした。");
            }
            // Player B にとっての相手のお題の結果表示
            if (!playerA.getOriginalAnswer().equals(playerA.answer)) {
                finalMessageB = finalMessageB.concat("/n相手が当てる単語は当初「");
                finalMessageB = finalMessageB.concat(playerA.getOriginalAnswer());
                finalMessageB = finalMessageB.concat("」でしたが、カオスチェンジにより「");
                finalMessageB = finalMessageB.concat(playerA.answer);
                finalMessageB = finalMessageB.concat("」に変更されました。");
            } else {
                finalMessageB = finalMessageB.concat("/n相手が当てる単語は「");
                finalMessageB = finalMessageB.concat(playerA.answer);
                finalMessageB = finalMessageB.concat("」でした。");
            }
            outB.println(finalMessageB);

            // 5. 接続を閉じる
            System.out.println("ゲームセッションを終了します。");
            playerASocket.close();
            playerBSocket.close();

        } finally {
            serverSocket.close();
            System.out.println("サーバーをシャットダウンしました。");
        }
    }

    /**
     * 1人のプレイヤーの1ターン全体の処理を実行する
     * @return ゲームに勝利した場合はtrue
     */
    private static boolean executePlayerTurn(Player currentPlayer, Player opponent, PrintWriter currentOut, PrintWriter opponentOut, BufferedReader currentIn, int gameCount, String playerName) throws IOException {
        currentOut.println("\n--- (ROUND " + (gameCount + 1) + ") " + playerName + ": あなたのターンです ---");
        opponentOut.println("\n(ROUND " + (gameCount + 1) + ") 相手のターンです。待機してください...");

        String guess;
        while (true) {
            currentOut.println("ポイント: " + currentPlayer.getPoints());
            // サイレンス状態ならメッセージを表示
            if (currentPlayer.isSilenced()) {
                currentOut.println("【アイテム封印中】");
            }
            currentOut.println("推測する単語を入力してください。('item'でアイテムストア)|PROMPT");

            String input = currentIn.readLine();
            if (input == null) return true;

            if ("item".equalsIgnoreCase(input)) {
                handleItemStore(currentPlayer, opponent, currentOut, currentIn);
                currentOut.println("\n--- (ROUND " + (gameCount + 1) + ") " + playerName + ": あなたのターンです ---");
                continue;
            }

            if (wordList.isInList(input.toLowerCase())) {
                guess = input.toLowerCase();
                break;
            }
            currentOut.println("エラー: その単語はリストにありません。");
        }

        currentPlayer.pushAnswer(guess, gameCount);
        currentOut.println(currentPlayer.getAnswerSheetString());

        // 最新の解答を答え合わせしてメッセージとして送信する
        currentOut.println(currentPlayer.getLatestAnswerString().concat("|ANS"));
        System.out.println(currentPlayer.getLatestAnswerString().concat("|ANS"));
        //currentOut.println("------------------------------");

        // ターンが終了するこのタイミングでサイレンスを解除
        if (currentPlayer.isSilenced()) {
            currentOut.println("アイテム封印の効果が解除されました。");
            currentPlayer.setSilenced(false);
        }

        return currentPlayer.judgeAnswer(guess);
    }

    /**
     * アイテムストアの表示と、購入・即時使用の処理
     */
    private static void handleItemStore(Player user, Player opponent, PrintWriter out, BufferedReader in) throws IOException {
        if (!user.canUseItem()) {
            out.println("エラー: アイテムは使用できません (今ターン使用済み or 封印されています)");
            return;
        }

        out.println("\n--- アイテムストア (購入すると即座に使用します) ---");
        List<Item> shopList = List.of(Item.values());
        for (int i = 0; i < shopList.size(); i++) {
            Item item = shopList.get(i);
            out.printf("%d: %s (%d P) - %s\n", i + 1, item.getName(), item.getCost(), item.getDescription());
        }
        out.println("使用するアイテムの番号を入力してください (戻る場合は'0')|PROMPT");

        String itemNumberStr = in.readLine();
        try {
            int itemNumber = Integer.parseInt(itemNumberStr);
            if (itemNumber > 0 && itemNumber <= shopList.size()) {
                Item itemToUse = shopList.get(itemNumber - 1);

                if (itemToUse == Item.CHAOS_CHANGE && user.hasUsedChaosChange()) {
                    out.println("エラー: カオスチェンジは1ゲームに1回しか使用できません。");
                    return;
                }

                if (user.getPoints() >= itemToUse.getCost()) {
                    user.usePoints(itemToUse.getCost());
                    user.consumeItemTurn();
                    executeItemEffect(user, opponent, itemToUse, out, in);
                } else {
                    out.println("ポイントが足りません。");
                }
            }
        } catch (NumberFormatException e) {
            out.println("無効な入力です。");
        }
    }

    /**
     * アイテムの効果を実際に実行する
     */
    private static void executeItemEffect(Player user, Player opponent, Item item, PrintWriter out, BufferedReader in) throws IOException {
        out.println("「" + item.getName() + "」を使用しました。");

        switch (item) {
            case SENGAN:
                // out.println("--- 相手の盤面 ---");
                out.println(opponent.getAnswerSheetString());
                // out.println("--------------------");
                break;
            case TENKEI_PIECE: {
                Set<Character> userAnswerChars = user.answer.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
                Set<Character> userGuessedChars = user.getAllGuessedChars();
                userAnswerChars.removeAll(userGuessedChars);

                if (userAnswerChars.isEmpty()) {
                    out.println("結果: 新しく開示できる文字はありませんでした。");
                } else {
                    Character revealedChar = new ArrayList<>(userAnswerChars).get(0);
                    out.println("結果: 「" + revealedChar + "」があなたの答えに含まれています。(黄色)");
                }
                break;
            }
            case TENKEI_WORD: {
                List<Integer> unknownGreenIndexes = new ArrayList<>();
                for (int i = 0; i < user.answer.length(); i++) {
                    boolean isAlreadyGreen = false;
                    for(String guess : user.answerSheet) {
                        if (!guess.isEmpty() && guess.length() > i && guess.charAt(i) == user.answer.charAt(i)) {
                            isAlreadyGreen = true;
                            break;
                        }
                    }
                    if (!isAlreadyGreen) {
                        unknownGreenIndexes.add(i);
                    }
                }

                if (unknownGreenIndexes.isEmpty()) {
                    out.println("結果: 新しく開示できる緑色の文字はありませんでした。");
                } else {
                    int revealedIndex = unknownGreenIndexes.get(0);
                    char revealedChar = user.answer.charAt(revealedIndex);
                    out.println("結果: あなたの答えの" + (revealedIndex + 1) + "文字目は「" + revealedChar + "」です。(緑色)");
                }
                break;
            }
            case DOUBLE_MOVE:
                user.grantDoubleMove();
                out.println("次の推測の後、もう一度あなたのターンになります。");
                break;
            case SILENCE:
                opponent.setSilenced(true);
                out.println("次の相手のターン、アイテム使用を封じました。");
                break;
            case WORD_SCAN: {
                out.println("スキャンする種類を選んでください (1: 母音, 2: 子音)|PROMPT");
                String choice = in.readLine();
                String vowels = "aiueo";
                Set<Character> foundChars = new HashSet<>();
                if ("1".equals(choice)) {
                    for (char c : user.answer.toCharArray()) {
                        if (vowels.indexOf(c) != -1) foundChars.add(c);
                    }
                    out.println("結果(母音): " + foundChars);
                } else if ("2".equals(choice)) {
                    for (char c : user.answer.toCharArray()) {
                        if (vowels.indexOf(c) == -1) foundChars.add(c);
                    }
                    out.println("結果(子音): " + foundChars);
                } else {
                    out.println("無効な選択です。");
                }
                break;
            }
            case QUESTION:
                out.println("調べたい文字を1文字入力してください。|PROMPT");
                String charToAsk = in.readLine();
                if (charToAsk != null && charToAsk.length() == 1) {
                    if (user.answer.contains(charToAsk.toLowerCase())) {
                        out.println("結果: その文字はあなたの答えに【含まれています】(黄色)");
                    } else {
                        out.println("結果: その文字はあなたの答えに【含まれていません】(黒)");
                    }
                } else {
                    out.println("入力が無効です。");
                }
                break;
            case CHAOS_CHANGE:
                out.println("相手の新しいお題となる単語(5文字)を入力してください。|PROMPT");
                while (true) {
                    String newWord = in.readLine();
                    if (newWord != null && newWord.length() == WORD_SIZE && answerWordList.isInList(newWord.toLowerCase())) {
                        opponent.answer = newWord.toLowerCase();
                        out.println("お題を「" + newWord.toLowerCase() + "」に再設定しました。");
                        user.setUsedChaosChange();
                        break;
                    }
                    out.println("エラー: その単語はリストにありません。もう一度入力してください。|PROMPT");
                }
                break;
        }
    }
}