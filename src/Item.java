/**
 * ゲーム内で使用されるアイテムの種類とデータを定義するEnum
 */
public enum Item {
    // アイテム名(日本語名, コスト, 説明)
    SENGAN("千里眼", 80, "相手の盤面を一度だけ覗き見る"),
    TENKEI_PIECE("天啓の一片", 90, "まだ見つかっていない黄色の文字を1つ開示する"),
    TENKEI_WORD("天啓の一文字", 150, "まだ見つかっていない緑色の文字を1つ開示する"),
    DOUBLE_MOVE("ダブルムーブ", 180, "このターン、2連続で推測できる"),
    SILENCE("サイレンス", 120, "次の相手のターン、アイテム使用を封じる"),
    WORD_SCAN("ワードスキャン", 100, "答えに含まれる母音か子音のリストを表示する"),
    QUESTION("質問権", 50, "指定した1文字が答えに含まれるか(黒/黄)を知る"),
    CHAOS_CHANGE("カオスチェンジ", 300, "相手のお題を新しい単語に変更する(1ゲーム1回)");

    private final String name;
    private final int cost;
    private final String description;

    Item(String name, int cost, String description) {
        this.name = name;
        this.cost = cost;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 文字列から対応するItem Enumを検索する
     * @param text 検索するアイテム名
     * @return 対応するItem, 見つからなければnull
     */
    public static Item fromString(String text) {
        for (Item item : Item.values()) {
            // アイテムの日本語名で検索
            if (item.name.equalsIgnoreCase(text)) {
                return item;
            }
        }
        return null;
    }
}