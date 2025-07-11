import java.awt.*;
import java.awt.event.*;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.geom.AffineTransform;

public class W_Wordle_UI {
    GameFrame k;

    public W_Wordle_UI() {
        JFrame frame = new JFrame("W_Wordle");

        // --- GridBagLayoutに変更 ---
        frame.setLayout(new GridBagLayout());
        frame.setSize(450, 450);      

        GridBagConstraints gbc = new GridBagConstraints();

        // --- ロゴパネル（固定高さ） ---
        LogoPanel logoPanel = new LogoPanel();
        logoPanel.setPreferredSize(new Dimension(450, 65));
        logoPanel.setMinimumSize(new Dimension(450, 65));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        frame.add(logoPanel, gbc);

        // --- ボタンパネル ---
        JPanel buttonPanel = new JPanel();
        JButton b = new JButton("開始");
            JButton result_dbg = new JButton("勝敗画面開発");
        Listener listener = new Listener(frame, b);
            WinListener winl = new WinListener(frame, result_dbg);
            result_dbg.addActionListener(winl);
        b.addActionListener(listener);
        buttonPanel.add(b);
        buttonPanel.add(result_dbg);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        frame.add(buttonPanel, gbc);

        // --- アイコン設定 ---
        ImageIcon icon = new ImageIcon("icon.png"); // 相対/絶対パス
        frame.setIconImage(icon.getImage());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        
        frame.setVisible(true);
        frame.setMinimumSize(frame.getSize());

        // frameの最小サイズを指定する
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int minWidth = 450;  // 最小幅（ウィンドウ全体）
                int minHeight = 500; // 最小高
                int w = frame.getWidth();
                int h = frame.getHeight();
                boolean resized = false;

                if (w < minWidth) {
                    w = minWidth;
                    resized = true;
                }
                if (h < minHeight) {
                    h = minHeight;
                    resized = true;
                }

                if (resized) {
                    frame.setSize(w, h);
                }
            }
    });

    }

    public class Listener implements ActionListener {
        JFrame frame;
        JButton b;
        GameFrame k;

        Listener(JFrame frame, JButton b) {
            this.frame = frame;
            this.b = b;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == b) {
                frame.getContentPane().removeAll();
                k = new GameFrame(frame);
                frame.revalidate(); // 再レイアウト
                frame.repaint();    // 再描画
            }
        }
    }

    // 勝敗表示
    public class WinListener implements ActionListener {
        JFrame frame;
        JButton result_dbg;
        WinListener(JFrame frame, JButton b) {
            this.frame = frame;
            result_dbg = b;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == result_dbg) {
                ResultDialog r = new ResultDialog(frame, "win", false);
                r.setVisible(true);
            }
        }
    }
}


class LogoPanel extends JPanel {
    Image img = Toolkit.getDefaultToolkit().getImage("WWlogo.png");
    LogoPanel() {
        this.setBackground(Color.white);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 真ん中に寄せたいなぁ 2025/7/9
        g.drawImage(img, 5, 5, 430, 50, this);
    } 
}

class GameFrame {
    JFrame frame;
    WordsArea wordsArea;

    public GameFrame(JFrame frame) {
        this.frame = frame;
        frame.setLayout(new GridBagLayout()); // frame に GridBagLayout を適用
        GridBagConstraints gbcFrame = new GridBagConstraints();

        // --- ロゴ部分（上部） ---
        JPanel logoPanelParent = new JPanel(new GridLayout(1, 1));
        LogoPanel logoPanel = new LogoPanel();
        logoPanelParent.add(logoPanel);
        logoPanelParent.setPreferredSize(new Dimension(450, 65));
        logoPanel.setMinimumSize(new Dimension(450, 65));

        gbcFrame.gridx = 0;
        gbcFrame.gridy = 0;
        gbcFrame.weightx = 1.0;
        gbcFrame.weighty = 0.0;
        gbcFrame.fill = GridBagConstraints.HORIZONTAL;
        frame.add(logoPanelParent, gbcFrame);

        // --- 中央部分（WordsArea, KeyBoardPanel, TextPanel を含む） ---
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcPanel = new GridBagConstraints();

        // --- WordsArea をスクロール対応で追加 ---
        wordsArea = new WordsArea();
        JScrollPane scrollPane = new JScrollPane(wordsArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        scrollPane.setMinimumSize(new Dimension(400, 200));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        gbcPanel.gridx = 0;
        gbcPanel.gridy = 0;
        gbcPanel.weightx = 1.0;
        gbcPanel.weighty = 1.0; // WordsArea は高さを自由に伸ばせる
        gbcPanel.fill = GridBagConstraints.BOTH;
        panel.add(scrollPane, gbcPanel);

        // --- KeyBoardPanel（固定サイズ） ---
        KeyBoardPanel k = new KeyBoardPanel(frame);
        k.setPreferredSize(new Dimension(400, 130));
        k.setMinimumSize(new Dimension(400, 130));

        gbcPanel.gridy = 1;
        gbcPanel.weighty = 0.0;
        gbcPanel.fill = GridBagConstraints.HORIZONTAL;
        panel.add(k, gbcPanel);

        // --- TextPanel（固定サイズ） ---
        TextPanel textPanel = new TextPanel(k, new WordList(), wordsArea);
        textPanel.setPreferredSize(new Dimension(400, 100));
        textPanel.setMinimumSize(new Dimension(400, 100));

        gbcPanel.gridy = 2;
        gbcPanel.weighty = 0.0;
        panel.add(textPanel, gbcPanel);

        // --- 中央 panel を frame に追加 ---
        gbcFrame.gridy = 1;
        gbcFrame.weighty = 1.0;
        gbcFrame.fill = GridBagConstraints.BOTH;
        frame.add(panel, gbcFrame);

        // --- フレーム設定 ---
        frame.setSize(450, 500);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- ウィンドウの最小サイズを守る ---
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int minWidth = 450;
                int minHeight = 500;
                int w = frame.getWidth();
                int h = frame.getHeight();
                boolean resized = false;

                if (w < minWidth) {
                    w = minWidth;
                    resized = true;
                }
                if (h < minHeight) {
                    h = minHeight;
                    resized = true;
                }

                if (resized) {
                    frame.setSize(w, h);
                }
            }
        });
    }
}

class WordsArea extends JPanel {
    private JPanel contentPanel;

    WordsArea() {
        setLayout(new BorderLayout());

        // ラベルを上に追加
        JLabel label = new JLabel("入力したWord", SwingConstants.CENTER);
        add(label, BorderLayout.NORTH);

        // WordPanelを並べるパネル
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.GRAY);

        // スクロール対応（スクロール非表示でもOK）
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void addWordPanel(WordPanel wp) {
        // 高さが 0 のままだと表示されないので明示的にサイズを設定
        wp.setMaximumSize(new Dimension(400, 70));  // 横幅制限あり
        wp.setPreferredSize(new Dimension(400, 70)); // 必須
        wp.setAlignmentX(Component.CENTER_ALIGNMENT); // 中央寄せ（任意）
        contentPanel.add(wp);
        contentPanel.revalidate();
        contentPanel.repaint();
        wp.flipWord();
    }
}



class KeyBoardPanel extends JPanel {
    Color[] usedCharCol = new Color[100];
    JFrame frame;
    boolean isRightHidden = false;
    boolean isLeftHidden = false;

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
        int space = 40;
        int init_spacing = windowWidth / 2 - 200;

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

        // 右半分を隠すおじゃま機能 booleanをいじるメソッドを用意する
        if(isRightHidden) {
            g.setColor(Color.BLACK);
            g.fillRect(init_spacing + 195,0,220,200);
        }
        if(isLeftHidden) {
            g.setColor(Color.RED);
            g.fillRect(init_spacing - 15,0,220,200);
        }
    }

    void drawChar(Graphics g, char c, int x, int y) {
        g.setColor(usedCharCol[c - 'A']);
        g.fillRect(x, y, 34, 34);
        g.setColor(Color.white);
        drawStringCenter(g, String.valueOf(c), x + 17, y + 17);
    }

    // キーボードの色リストを更新するメソッド．
    // updateCol('A',Color.myyellow); でAの表示色を黄色にする
    void updateCol(char c, Color col) {
        usedCharCol[(int) c - (int) 'A'] = col;
        frame.repaint();
    }

    // キーボードの1文字が参照済みか判定する
    boolean isUpdated(char c) {
        if(usedCharCol[(int) c - (int) 'A'].equals(Color.GRAY)) {
            // Color.GRAYのときは初期状態なので一度も参照されていない
            return false;
        } else {
            // 他の色だと参照済み
            return true;
        }
    }

    public static void drawStringCenter(Graphics g, String text, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        Rectangle rectText = fm.getStringBounds(text, g).getBounds();
        x = x - rectText.width / 2;
        y = y - rectText.height / 2 + fm.getMaxAscent();
        g.drawString(text, x, y);
    }
}

// テキスト入力用JPanel
class TextPanel extends JPanel {
    JTextArea textArea = new JTextArea();
    JLabel label = new JLabel("Input your GUESS.  ");
    JLabel notify = new JLabel("Ready");
    GridBagConstraints gbc = new GridBagConstraints();
    KeyBoardPanel keyBoardPanel;
    char[] input_char;
    boolean isAcceptInGram;
    JPanel wordsArea;  // 入力済みword表示エリア
    
    TextPanel(KeyBoardPanel k, WordList wordList, WordsArea wordsArea) {
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


        // ここで6文字制限を追加
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
                        input_char = input.toCharArray();
                        textArea.setText("");

                        isAcceptInGram = true;
                        for(int i = 0; i < 5; i++) {
                            int j = (int) input_char[i];
                            
                            if(isInputAccepted(j)){
                                notify.setText("Input is Accepted");
                                // 小文字入力にも対応
                                if(97 <= j && j <= 122) input_char[i] = (char) (j - 32);
                                System.out.println("char" + i + "," + input_char[i]);
                            } else {
                                isAcceptInGram = false;
                                System.out.println("Error: 入力された文字列が[A-Za-z]^5に入っていません");
                                notify.setText("Error: 入力された文字列が[A-Za-z]^5に入っていません");
                            }
                        }
                    } else {
                        isAcceptInGram = false;
                        System.out.println("Error: 入力された文字列の長さが適合しません");
                        notify.setText("Error: 入力された文字列の長さが適合しません");
                    }
                    if(isAcceptInGram) {
                        // wordListが小文字で書かれているので小文字に変換
                        for(int i = 0; i < 5; i++) {
                            input_char[i] = (char) ((int) input_char[i] + ((int) 'a') - ((int) 'A'));
                        }

                        // WordListのインスタンスが立っているので対応したい
                        if(wordList.isInList(input)) {
                            System.out.println("in list! " + input);
                            // ひとまず，入力された文字列すべてをacceptして，色を反映
                            for(int i = 0; i < 5; i++){
                                char tmp_c = (char)((int)input_char[i] + (int)'A' - (int)'a');
                                if(k.isUpdated(tmp_c) == false) {
                                    // キーボード画面に変化がある．判定と合わせて色に変化を付ける
                                    k.updateCol(tmp_c,Color.GRAY);
                                    /*
                                        このへんにコードを書く
                                    */

                                } else {
                                    // 最終的に消してOK
                                    System.out.println("Already updated");
                                }
                            }
                            // wordをエリアに反映
                            Word word = new Word(input);
                            WordPanel wordPanel = new WordPanel(word);
                            wordsArea.addWordPanel(wordPanel);
                        } else {
                            notify.setText("This word is not in List.");
                            System.out.println("out of list " + input);
                        }
                    }
                }
            }
        });
    }

    // inputの文字が[A-Za-z]か判定
    boolean isInputAccepted(int input_num) {
        if((input_num < 97 || input_num > 122) && (input_num < 65 || input_num > 90)) {
            return false;
        } else {
            return true;
        }
    }
}

// Word1つが表示されるエリアを作る
class WordPanel extends JPanel {
    Word word = new Word("00000");
    boolean[] isColored = new boolean[GLOBALVALS.wordLen];
    Color myyellow = new Color(255, 204, 0);

    // animation
    float[] flipProgress = new float[GLOBALVALS.wordLen]; // 0.0〜1.0
    boolean[] flipped = new boolean[GLOBALVALS.wordLen];  // 裏返し済みか
    Timer timer;

    public WordPanel(Word word_in){
        word = word_in;
        this.setPreferredSize(new Dimension(400, 70));
        this.setBackground(Color.GRAY);
        for (int i = 0; i < GLOBALVALS.wordLen; i++) {
            flipProgress[i] = 0f;
            flipped[i] = false;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int boxSize = 50;
        int spacing = 10;
        int windowWidth = getWidth();
        int totalWidth = GLOBALVALS.wordLen * (boxSize + spacing) - spacing;
        int x0 = (windowWidth - totalWidth) / 2;

        for (int i = 0; i < GLOBALVALS.wordLen; i++) {
            int x = x0 + i * (boxSize + spacing);
            int y = 10;

            float progress = flipProgress[i];
            double scale = Math.cos(Math.PI * progress); // 1→0→-1

            // 背面の色へ切り替えるタイミング
            if (progress < 0.5) {
                g2.setColor(Color.DARK_GRAY); // 背面
            } else {
                switch (word.isCorrect[i]) {
                    case -1: g2.setColor(Color.GRAY); break;
                    case 0: g2.setColor(Color.GREEN); break;
                    case 1: g2.setColor(myyellow); break;
                    default: g2.setColor(Color.BLACK); break;
                }
            }

            // 描画用 transform の保存
            AffineTransform old = g2.getTransform();

            // スケーリング中心位置に合わせる
            g2.translate(x + boxSize / 2, y + boxSize / 2);
            g2.scale(scale, 1.0); // 横方向スケーリング
            g2.translate(-(x + boxSize / 2), -(y + boxSize / 2));

            g2.fillRect(x, y, boxSize, boxSize);

            // 文字の描画（反転時は描かない）
            if (progress < 0.6) {
                // 背面なので文字は描かない
            } else {
                // ここで scale が負の場合に文字が反転しないように修正
                // scaleが負の場合、Graphics2Dはすでに反転しているため、
                // 文字の描画自体を反転させる必要がある
                // もしくは、scaleを絶対値にして再適用する
                // 最も簡単なのは、transformをリセットして、文字は別途描画すること
                g2.setTransform(old); // ここで一旦transformをリセットする

                g2.setColor(Color.white);
                Font font = new Font("ＭＳ 明朝", Font.BOLD, 32);
                g2.setFont(font);
                // 文字を中央に描画
                drawStringCenter(g2, String.valueOf(word.char_array[i]), x + boxSize / 2, y + boxSize / 2);
            }

            // transform を戻す
            g2.setTransform(old); // 文字を描画した後、元のtransformに戻す
        }
    }


    public static void drawStringCenter(Graphics g,String text,int x,int y){
        FontMetrics fm = g.getFontMetrics();
        Rectangle rectText = fm.getStringBounds(text, g).getBounds();
        x=x-rectText.width/2;
        y=y-rectText.height/2+fm.getMaxAscent();
        g.drawString(text, x, y);
    }
    
    public void flipWord() {
        timer = new Timer(16, null); // 約60FPS

        timer.addActionListener(new ActionListener() {
            int frameCount = 0;
            final int maxFrames = 30; // 30フレームで1文字のアニメーション
            int currentIndex = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentIndex >= GLOBALVALS.wordLen) {
                    timer.stop();
                    return;
                }

                flipProgress[currentIndex] = (float) frameCount / maxFrames;

                if (flipProgress[currentIndex] >= 0.5 && !flipped[currentIndex]) {
                    flipped[currentIndex] = true;
                    // このタイミングで isCorrect を適用（裏返る瞬間）
                    // 特に何も処理しないでも isCorrect[] を使って描画してるならOK
                }

                repaint();

                frameCount++;
                if (frameCount > maxFrames) {
                    frameCount = 0;
                    currentIndex++;
                }
            }
        });

        timer.start();
    }
}

class Word {
    char[] char_array = new char[GLOBALVALS.wordLen];
    public int[] isCorrect = new int[GLOBALVALS.wordLen];  // 各文字の正解判定, -1:不正解 0: 位置違い 1: 正解

    Word(String str_in) {
        char[] c = str_in.toCharArray();
        for(int i = 0; i < GLOBALVALS.wordLen; i++){
            char_array[i] = c[i];
        }

            // develop
            for(int i = 0; i < GLOBALVALS.wordLen; i++){
                isCorrect[i] = 0;
            }
            isCorrect[2] = 1;
    }
    
}

class ResultDialog extends JDialog {
    public ResultDialog(JFrame owner, String message, boolean isWin) {
        super(owner, "ゲーム結果", true); // true でモーダルにする

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(isWin ? Color.GREEN.darker() : Color.RED);
        label.setPreferredSize(new Dimension(250, 80));
        add(label, BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        // OKを押したらどこに遷移する？
        okButton.addActionListener(e -> System.exit(0));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(owner); // 親フレームの中央に表示
    }
}


class GLOBALVALS {
    public static int wordLen = 5;
}