import java.util.Arrays;

class Player {
    int MAX_GAME_ROUND;
    int WORD_SIZE;
    String answer;
    String[] answerSheet;
    // コンソール用のカラーコード
    final String COLOR_GREEN = "\u001b[00;42m";
    final String COLOR_YELLOW = "\u001b[00;43m";
    final String COLOR_END = "\u001b[00m";

    Player(int maxGameRound, int wordSize, String answer) {
        this.MAX_GAME_ROUND = maxGameRound;
        this.WORD_SIZE = wordSize;
        this.answer = answer;
        this.answerSheet = new String[MAX_GAME_ROUND];
        Arrays.fill(this.answerSheet, "");
    }

    /**
     * 回答シートの状態を、色付けされた文字列として生成して返す
     * @return 回答シートの文字列
     */
    public String getAnswerSheetString() {
        final boolean[] checked = new boolean[WORD_SIZE];
        final int[] result = new int[WORD_SIZE];        //1:green, 2:yellow, 0:glay    //enum型の方がいいかも？
        StringBuilder sb = new StringBuilder();

        for (String word : this.answerSheet) {
            if (word.equals("")) {
                sb.append(" _ _ _ _ _");
            } else {
                for (int i = 0; i < WORD_SIZE; i++) {       //初期化、緑判定
                    checked[i] = false;
                    result[i] = 0;
                    if (word.charAt(i) == this.answer.charAt(i)) {
                        checked[i] = true;
                        result[i] = 1;
                    }
                }
                for (int i = 0; i < WORD_SIZE; i++) {       //黄判定、灰判定
                    if (result[i] == 1) continue;

                    boolean allfound = false;       //答えの単語にも含まれるこの文字は、答えでの登場回数分だけ予測の単語で見つかった
                    for (int j = 0; j < WORD_SIZE; j++) {
                        if (word.charAt(i) == this.answer.charAt(j) && !checked[j]) {
                            checked[i] = true;
                            result[i] = 2;
                            allfound = true;
                            break;
                        }
                        if (!allfound) result[i] = 0;
                    }
                }
                for (int i = 0; i < WORD_SIZE; i++) {
                    sb.append(" ");
                    switch(result[i]) {
                        case 1:
                            sb.append(COLOR_GREEN).append(word.charAt(i)).append(COLOR_END);
                            break;
                        case 2:
                            sb.append(COLOR_YELLOW).append(word.charAt(i)).append(COLOR_END);
                            break;
                        default:
                            sb.append(word.charAt(i));
                            break;
                    }
                }
            }
            sb.append("\n"); // 各行の終わりに改行を追加
        }
        return sb.toString();
    }

    /**
     * プレイヤーの回答を記録する
     * @param word 入力された単語
     * @param gameCount 現在のラウンド数
     */
    public void pushAnswer(String word, int gameCount) {
        this.answerSheet[gameCount] = word;
    }

    /**
     * 入力された単語が正解かどうかを判定する
     * @param word 入力された単語
     * @return 正解ならtrue
     */
    public boolean judgeAnswer(String word) {
        return (word.equals(this.answer));
    }
}
