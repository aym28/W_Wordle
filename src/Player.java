import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

class Player {
    int MAX_GAME_ROUND;
    int WORD_SIZE;
    String answer;
    String[] answerSheet;

    // プレイヤーの状態
    private int points;
    private boolean hasUsedItemThisTurn;
    private boolean isSilenced;
    private boolean hasDoubleMove;
    String originalAnswer; // ゲーム開始時のお題を保存
    private boolean hasUsedChaosChange; // カオスチェンジを使用したか

    // コンソール用のカラーコード
    final String COLOR_GREEN = "\u001b[00;42m";
    final String COLOR_YELLOW = "\u001b[00;43m";
    final String COLOR_END = "\u001b[00m";

    Player(int maxGameRound, int wordSize, String answer) {
        this.MAX_GAME_ROUND = maxGameRound;
        this.WORD_SIZE = wordSize;
        this.answer = answer;
        this.originalAnswer = answer; // --- 初期お題を保存 ---
        this.answerSheet = new String[MAX_GAME_ROUND];
        Arrays.fill(this.answerSheet, "");

        // --- 初期化 ---
        this.points = 10000;
        this.hasUsedItemThisTurn = false;
        this.isSilenced = false;
        this.hasDoubleMove = false;
        this.hasUsedChaosChange = false;
    }
    // --- ポイント関連 ---
    public int getPoints() { return this.points; }
    public void addPoints(int amount) { this.points += amount; }
    public boolean usePoints(int amount) {
        if (this.points >= amount) {
            this.points -= amount;
            return true;
        }
        return false;
    }

    // --- ターン状態関連 ---
    public boolean canUseItem() { return !this.hasUsedItemThisTurn && !this.isSilenced; }
    public void consumeItemTurn() { this.hasUsedItemThisTurn = true; }
    public void setSilenced(boolean silenced) { this.isSilenced = silenced; }
    
    // --- サイレンス状態か確認する ---
    public boolean isSilenced() { return this.isSilenced; }

    public void resetTurnState() {
        this.hasUsedItemThisTurn = false;
    }

    // --- ダブルムーブ関連 ---
    public boolean hasDoubleMove() { return this.hasDoubleMove; }
    public void grantDoubleMove() { this.hasDoubleMove = true; }
    public void consumeDoubleMove() { this.hasDoubleMove = false; }

     // --- カオスチェンジ関連のメソッドを追加 ---
    public boolean hasUsedChaosChange() { return this.hasUsedChaosChange; }
    public void setUsedChaosChange() { this.hasUsedChaosChange = true; }
    public String getOriginalAnswer() { return this.originalAnswer; }


    public Set<Character> getAllGuessedChars() {
        Set<Character> guessedChars = new HashSet<>();
        for (String word : answerSheet) {
            if (!word.isEmpty()) {
                for (char c : word.toCharArray()) {
                    guessedChars.add(c);
                }
            }
        }
        return guessedChars;
    }

    public String getAnswerSheetString() {
        final boolean[] checked = new boolean[WORD_SIZE];
        final int[] result = new int[WORD_SIZE];
        StringBuilder sb = new StringBuilder();

        for (String word : this.answerSheet) {
            if (word.equals("")) {
                sb.append(" _ _ _ _ _");
            } else {
                for (int i = 0; i < WORD_SIZE; i++) {
                    checked[i] = false;
                    result[i] = 0;
                    if (word.charAt(i) == this.answer.charAt(i)) {
                        checked[i] = true;
                        result[i] = 1;
                    }
                }
                for (int i = 0; i < WORD_SIZE; i++) {
                    if (result[i] == 1) continue;
                    for (int j = 0; j < WORD_SIZE; j++) {
                        if (i != j && result[j] != 1 && !checked[j] && word.charAt(i) == this.answer.charAt(j)) {
                            checked[j] = true;
                            result[i] = 2;
                            break;
                        }
                    }
                }
                for (int i = 0; i < WORD_SIZE; i++) {
                    sb.append(" ");
                    switch(result[i]) {
                        case 1:
                            sb.append(COLOR_GREEN).append(word.charAt(i)).append(COLOR_END);
                            addPoints(20);
                            break;
                        case 2:
                            sb.append(COLOR_YELLOW).append(word.charAt(i)).append(COLOR_END);
                            addPoints(10);
                            break;
                        default:
                            sb.append(word.charAt(i));
                            break;
                    }
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // 最新の解答1件のみを取得する
    public String getLatestAnswerString() {
        for (int i = answerSheet.length - 1; i >= 0; i--) {
            String word = answerSheet[i];
            if (!word.equals("")) {
                Word w = new Word(word);
                w.apdateIsCorrect(new Word(this.answer));  // 正解と比較して判定配列を更新
                StringBuilder sb = new StringBuilder();
                sb.append(word);
                final boolean[] checked = new boolean[GLOBALVALS.wordLen];
                for (int j = 0; j < GLOBALVALS.wordLen; j++) {
                    checked[j] = false;
                    w.isCorrect[j] = -1;
                        if (word.charAt(j) == this.answer.charAt(j)) {
                            checked[j] = true;
                            w.isCorrect[j] = 0;
                        }
                }   
                for (int j = 0; j < GLOBALVALS.wordLen; j++) {
                    if (w.isCorrect[j] == 0) continue;
                    for (int t = 0; t < GLOBALVALS.wordLen; t++) {
                        if (j != t && w.isCorrect[t] != 0 && !checked[t] && word.charAt(j) == this.answer.charAt(t)) {
                            checked[t] = true;
                            w.isCorrect[j] = 1;
                            break;
                        }
                    }
                }
                
                for (int j = 0; j < GLOBALVALS.wordLen; j++) {
                    sb.append(" ").append(w.isCorrect[j]);      
                }
                return sb.toString();
            }
        }
        return "";
    }


    public void pushAnswer(String word, int gameCount) { this.answerSheet[gameCount] = word; }
    public boolean judgeAnswer(String word) { return (word.equals(this.answer)); }
}
