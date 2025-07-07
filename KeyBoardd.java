import java.awt.*;
import java.util.Scanner;
import javax.swing.*;

public class KeyBoardd {
    public static void main(String[] args) {
        JFrame frame = new JFrame("KeyBoardPanel debug");
        KeyBoardPanel k = new KeyBoardPanel();
        frame.getContentPane().add(k);
        frame.setBackground(Color.white);
        frame.setLayout(new GridLayout(3,1));
        frame.setSize(400,500);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Scanner sc = new Scanner(System.in);
        while(true){
            k.updateCol(sc.nextLine().toCharArray()[0],Color.yellow);
            frame.repaint();
        }
    }
}

class KeyBoardPanel extends JPanel {
    Color[] usedCharCol = new Color[100];

    KeyBoardPanel() {
        for (int j = 0; j < 26; j++) {
            usedCharCol[j] = Color.gray;
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // パネルサイズに応じた調整
        int windowWidth = getWidth();
        int space = windowWidth / 12 + 5;
        int init_spacing = windowWidth / 35;

        // フォント設定
        Font font = new Font("ＭＳ 明朝", Font.BOLD, 32);
        g.setFont(font);

        // ---- 1段目（Q〜P）
        int x = init_spacing;
        int y = 10;
        for (char c : "QWERTYUIOP".toCharArray()) {
            drawChar(g, c, x, y);
            x += space;
        }

        // ---- 2段目（A〜L）
        x = init_spacing + space / 2;
        y += 40;
        for (char c : "ASDFGHJKL".toCharArray()) {
            drawChar(g, c, x, y);
            x += space;
        }

        x = init_spacing + 3 * space / 2;
        y += 40;
        for( char c : "ZXCVBNM".toCharArray()) {
            drawChar(g, c, x, y);
            x += space;
        }
    }

    void drawChar(Graphics g, char c, int x, int y) {
        g.setColor(usedCharCol[c - 'A']);
        g.fillRect(x, y, 34, 34);
        g.setColor(Color.white);
        drawStringCenter(g, String.valueOf(c), x + 17, y + 17);
    }

    // キーボードの色リストを更新するメソッド．
    // updateCol('A',Color.yellow); でAの表示色を黄色にする
    void updateCol(char c, Color col) {
        usedCharCol[(int) c - 65] = col;
    }

    public static void drawStringCenter(Graphics g, String text, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        Rectangle rectText = fm.getStringBounds(text, g).getBounds();
        x = x - rectText.width / 2;
        y = y - rectText.height / 2 + fm.getMaxAscent();
        g.drawString(text, x, y);
    }
}
