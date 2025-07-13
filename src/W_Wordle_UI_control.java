public class W_Wordle_UI_control {
    public static void main(String args[]) {
        // ウィンドウの数と位置調整
        int x = 0;
        int y = 0;
        if (args.length >= 1) {
            if ("2".equals(args[0])) {
                x = 450; // 2つ目のウィンドウは右側にずらす
                y = 0;
            }
        }

        // メインGUIの表示
        W_Wordle_UI ui = new W_Wordle_UI(x, y);
    }
}