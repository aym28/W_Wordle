import java.awt.*;
import java.awt.event.*;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.text.*;

public class KeyBoardd {
    public static void main(String[] args) {
        JFrame frame = new JFrame("KeyBoardPanel debug");
        KeyBoardPanel k = new KeyBoardPanel(frame);

        // テキスト入力用
        TextPanel textPanel = new TextPanel(k);
        frame.getContentPane().add(k,SwingConstants.CENTER);
        frame.getContentPane().add(textPanel);
        frame.setBackground(Color.white);
        frame.setLayout(new GridLayout(3,1));
        frame.setSize(400,500);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}

class KeyBoardPanel extends JPanel {
    Color[] usedCharCol = new Color[100];
    JFrame frame;

    KeyBoardPanel(JFrame f) {
        this.frame = f;
        for (int j = 0; j < 26; j++) {
            usedCharCol[j] = Color.gray;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // パネルサイズに応じた調整
        int windowWidth = getWidth();
        int space = windowWidth / 12 + 5;
        if(space > 50) space = 50;
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
        frame.repaint();
    }

    public static void drawStringCenter(Graphics g, String text, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        Rectangle rectText = fm.getStringBounds(text, g).getBounds();
        x = x - rectText.width / 2;
        y = y - rectText.height / 2 + fm.getMaxAscent();
        g.drawString(text, x, y);
    }
}

// テキスト入力を司る
class TextPanel extends JPanel {
    JTextArea textArea = new JTextArea();
    JLabel label = new JLabel("Input your GUESS.  ");
    JLabel notify = new JLabel("Ready");
    GridBagConstraints gbc = new GridBagConstraints();
    KeyBoardPanel keyBoardPanel;
    
    TextPanel(KeyBoardPanel k) {
        this.keyBoardPanel = k;
        this.setLayout(new GridBagLayout());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(label,gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(textArea,gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;  
        add(notify,gbc);
        textArea.setPreferredSize(new Dimension(100,20));


        // ★ ここで6文字制限を追加
        ((AbstractDocument) textArea.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (fb.getDocument().getLength() + string.length() <= 5) {
                    super.insertString(fb, offset, string, attr);
                } // 6文字目以降は無視
            }
        @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                int currentLength = fb.getDocument().getLength();
                int overLimit = (currentLength + text.length()) - 5 - length;
                if (overLimit <= 0) {
                    super.replace(fb, offset, length, text, attrs);
                } // 6文字目以降は無視
            }
        });

        // 改行禁止
        // エンターでの文字列取得
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume(); // 改行を防ぐ
                    String input = textArea.getText();

                    // ここら辺で入力文字列を処理する．
                    // 範囲外文字の拒否と大文字小文字の処理が必要
                    if(input.length() == 5) {
                        // 5文字のWordの入力を受け付ける
                        System.out.println("入力された文字列: " + input);
                        char[] input_char = input.toCharArray();
                        textArea.setText("");
                        for(int i = 0; i < 5; i++) {
                            int j = (int) input_char[i];
                            
                            if(isInputAccepted(j)){
                                notify.setText("Input is Accepted");
                                // 小文字入力にも対応
                                if(97 <= j && j <= 122) input_char[i] = (char) (j - 32);
                                System.out.println("char" + i + "," + input_char[i]);

                                // ひとまず，入力された文字列すべてをacceptして，色を反映
                                k.updateCol(input_char[i],Color.black);
                            } else {
                                System.out.println("Error: 入力された文字列が[A-Za-z]^5に入っていません");
                                notify.setText("Error: 入力された文字列が[A-Za-z]^5に入っていません");
                            }
                        }
                    } else {
                        System.out.println("Error: 入力された文字列の長さが適合しません");
                        notify.setText("Error: 入力された文字列の長さが適合しません");
                    }

                    
                    
                }
            }
        });
    }

    boolean isInputAccepted(int input_num) {
        if((input_num < 97 || input_num > 122) && (input_num < 65 || input_num > 90)) {
            return false;
        } else {
            return true;
        }
    }
}
