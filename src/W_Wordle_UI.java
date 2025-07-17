import java.awt.*;
import java.awt.event.*;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.ArrayList;

public class W_Wordle_UI {
    GameFrame k;
    JFrame frame;
    public static int PORT = 8080;
    int x, y;
    WordleClientThread clientThread;
    JButton connectButton;

    public W_Wordle_UI(int x, int y) {
        frame = new JFrame("W_Wordle");
        frame.setLocation(x, y);

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

        // 通信設定パネル
        JPanel comPanel = new JPanel();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;
        JLabel portLabel = new JLabel("ポート番号");
        JTextArea portText = new JTextArea();
        portText.setPreferredSize(new Dimension(50,20));
        JLabel portNum = new JLabel(Integer.toString(PORT));
        JButton portSetButton = new JButton("設定");
        PortSetListener portSetListener = new PortSetListener(portText, portNum, portSetButton);
        portSetButton.addActionListener(portSetListener);

        comPanel.add(portLabel);
        comPanel.add(portText);
        comPanel.add(portNum);
        comPanel.add(portSetButton);
        frame.add(comPanel, gbc);

        JPanel explanation = new JPanel();
        JTextPane explainText = new JTextPane();

        // スタイル付きテキストの設定
        StyledDocument doc = explainText.getStyledDocument();

        // スタイル1: 通常文字
        SimpleAttributeSet normal = new SimpleAttributeSet();
        StyleConstants.setFontFamily(normal, "MS ゴシック");
        StyleConstants.setFontSize(normal, 12);

        // スタイル2: 太字 + 赤色
        SimpleAttributeSet boldRed = new SimpleAttributeSet();
        StyleConstants.setFontFamily(boldRed, "MS ゴシック");
        StyleConstants.setFontSize(boldRed, 26);
        StyleConstants.setBold(boldRed, true);
        StyleConstants.setForeground(boldRed, new Color(128,0,0));

        // テキストを挿入（例: "Waseda Wordleとは --About Waseda Wordle" の一部を強調）
        try {
doc.insertString(doc.getLength(), "\n", normal);
doc.insertString(doc.getLength(), "Waseda Wordleとは -About Waseda\nWordle\n", boldRed);
doc.insertString(doc.getLength(), "「Waseda Wordle（ワセダ ワードル）」は、早稲田大学の学生によって開発され\n",normal);
doc.insertString(doc.getLength(), "た、5文字の英単語を使った2人対戦型のWordle風ゲームです。プレイヤーは交互\n",normal);
doc.insertString(doc.getLength(), "に単語を推測し、色によるフィードバックをもとに相手より早く正解を導き出す\n",normal);
doc.insertString(doc.getLength(), "ことを目指します。チャット接続を通じて、遠隔でも対戦可能。ヒント機能など、\n",normal);
doc.insertString(doc.getLength(), "ゲームを盛り上げる多彩な仕掛けが搭載されています。\n", normal);
doc.insertString(doc.getLength(), "-Waseda Wordle is a two-player competitive Wordle-style game developed by\n",normal);
doc.insertString(doc.getLength(), "students of Waseda University. Players take turns guessing 5-letter words\n",normal);
doc.insertString(doc.getLength(), "and aim to deduce the correct word faster than their opponent, using color-coded\n",normal);
doc.insertString(doc.getLength(), "feedback. Online play is supported through chat-based connection, and various\n",normal);
doc.insertString(doc.getLength(), "features such as hint items enhance the gameplay experience.\n",normal);
            //doc.insertString(doc.getLength(), " Wordle", normal);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        explanation.setLayout(new BorderLayout());
        Color defaultBg = UIManager.getColor("Panel.background");
        explainText.setBackground(defaultBg);
        explanation.add(explainText, BorderLayout.CENTER);
        
        explanation.add(explainText);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;
        frame.add(explanation, gbc);

        JPanel empty = new JPanel();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        frame.add(empty, gbc);

        // --- 接続パネル ---
        JPanel connectPanel = new JPanel();
        connectButton = new JButton("接続");
        connectButton.addActionListener(e -> {
        clientThread = new WordleClientThread(
                this, "localhost", PORT,
                msg -> SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, msg)),
                () -> JOptionPane.showInputDialog(frame, "お題として5文字の単語を入力してください:")
            );
            clientThread.start();
        });
        connectPanel.add(connectButton);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;
        frame.add(connectPanel, gbc);

        // --- アイコン設定 ---
        ImageIcon icon = new ImageIcon("res/WWicon.png"); // 適宜パス調整必要
        frame.setIconImage(icon.getImage());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();

        frame.setVisible(true);
        frame.setMinimumSize(frame.getSize());

        // frameの最小サイズを指定する
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int minWidth = 450;  // 最小幅
                int minHeight = 500; // 最小高さ
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

    public void startGame() {
        frame.getContentPane().removeAll();
        k = new GameFrame(frame, clientThread);
        frame.revalidate();
        frame.repaint();
    }

    // 接続開始ボタンのリスナー（開始ボタンはGameFrameへ切り替え）
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
                k = new GameFrame(frame, clientThread);
                frame.revalidate();
                frame.repaint();
            }
        }
    }

    // ポート設定ボタンのリスナー
    public class PortSetListener implements ActionListener {
        JLabel portNum;
        JTextArea portText;
        JButton portSetButton;
        PortSetListener(JTextArea portText, JLabel portNum, JButton portSetButton) {
            this.portNum = portNum;
            this.portText = portText;
            this.portSetButton = portSetButton;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == portSetButton) {
                String text = portText.getText().trim();
                try {
                    int parsedPort = Integer.parseInt(text);
                    if (parsedPort < 0 || parsedPort > 65535) {
                        throw new IllegalArgumentException("ポート番号は 0〜65535 の範囲で入力してください。");
                    }
                    PORT = parsedPort;
                    portNum.setText(String.valueOf(PORT));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "ポート番号は整数で入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "入力エラー", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}


class LogoPanel extends JPanel {
    Image img = Toolkit.getDefaultToolkit().getImage("res/WWlogo.png");
    LogoPanel() {
        this.setBackground(Color.white);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, 5, 5, 430, 50, this);
    } 
}

class GameFrame {
    JFrame frame;
    WordsArea wordsArea;
    TextPanel textPanel;
    WordleClientThread clientThread;
    KeyBoardPanel k;

    public GameFrame(JFrame frame, WordleClientThread clientThread) {
        this.clientThread = clientThread;
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

        // --- WordsArea を直接追加（WordsArea は自身でスクロールを持つ） ---
        wordsArea = new WordsArea();
        wordsArea.setPreferredSize(new Dimension(400, 200));
        wordsArea.setMinimumSize(new Dimension(400, 200));

        gbcPanel.gridx = 0;
        gbcPanel.gridy = 0;
        gbcPanel.weightx = 1.0;
        gbcPanel.weighty = 1.0; // WordsArea は高さを自由に伸ばせる
        gbcPanel.fill = GridBagConstraints.BOTH;
        panel.add(wordsArea, gbcPanel);

        // --- KeyBoardPanel（固定サイズ） ---
        k = new KeyBoardPanel(frame);
        k.setPreferredSize(new Dimension(400, 130));
        k.setMinimumSize(new Dimension(400, 130));

        gbcPanel.gridy = 1;
        gbcPanel.weighty = 0.0;
        gbcPanel.fill = GridBagConstraints.HORIZONTAL;
        panel.add(k, gbcPanel);

        // --- TextPanel（固定サイズ） ---
        textPanel = new TextPanel(k, new WordList(), wordsArea, clientThread);
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

        // --- メニューバー(開発者向け) ---
        if(GLOBALVALS.isEdit) {
            MenuPanel menuPanel = new MenuPanel();
            JMenuBar menuBar = menuPanel.createMenuBar(wordsArea, k);
            frame.setJMenuBar(menuBar);
        }

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

    public TextPanel getTextPanel() {
        return textPanel;
    }

    public KeyBoardPanel getKeyBoardPanel() {
        return k;
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

// 開発者向けメニュー
class MenuPanel {
    WordsArea wordsArea;
    KeyBoardPanel keyBoardPanel;
    
    public MenuPanel() {
    }

    public JMenuBar createMenuBar(WordsArea wordsArea, KeyBoardPanel keyBoardPanel) {
        this.wordsArea = wordsArea;
        this.keyBoardPanel = keyBoardPanel;

        JMenuBar menuBar = new JMenuBar();
        int menuFontSize = 12;
        JMenu editScript = new JMenu("開発者モード");
        editScript.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, menuFontSize));
        JMenu actionMenu = new JMenu("アイテム");
        actionMenu.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, menuFontSize));

        JMenuItem blackOut = new JMenuItem("ブラックアウト");
        blackOut.addActionListener(e -> blackOut());
        JMenuItem leftHide = new JMenuItem("レフトハイド");
        leftHide.addActionListener(e -> leftHide());
        JMenuItem rightHide = new JMenuItem("ライトハイド");
        rightHide.addActionListener(e -> rightHide());
        JMenuItem hideCancel = new JMenuItem("ハイド解除");
        hideCancel.addActionListener(e -> hideCancel());

        menuBar.add(editScript);
        actionMenu.add(blackOut);
        actionMenu.add(leftHide);
        actionMenu.add(rightHide);
        actionMenu.add(hideCancel);

        menuBar.add(actionMenu);
        

        return menuBar;
    }

    public void blackOut() {
        wordsArea.makeAllBlack();
    }

    public void leftHide() {
        keyBoardPanel.isLeftHidden = true;
        keyBoardPanel.repaint();
    }
    public void rightHide() {
        keyBoardPanel.isRightHidden = true;
        keyBoardPanel.repaint();
    }
    public void hideCancel() {
        keyBoardPanel.isLeftHidden = false;
        keyBoardPanel.isRightHidden = false;
        keyBoardPanel.repaint();
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
    WordleClientThread clientThread; // メッセージ送信用
    JButton itemButton;

    TextPanel(KeyBoardPanel k, WordList wordList, WordsArea wordsArea, WordleClientThread clientThread) {
        this.clientThread = clientThread;
        this.keyBoardPanel = k;
        this.setLayout(new GridBagLayout());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(textArea, gbc);

        itemButton = new JButton("アイテム購入");
        itemButton.setEnabled(false);
        itemButton.addActionListener(e -> {
            clientThread.sendMessage("item");
            System.out.println("item");
            ItemShop itemShop = new ItemShop(clientThread, this);
            clientThread.setItemShop(itemShop);
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(itemButton, gbc);

        textArea.setPreferredSize(new Dimension(100, 20));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        // 入力制限
        ((AbstractDocument) textArea.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (fb.getDocument().getLength() + string.length() <= 5) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                int currentLength = fb.getDocument().getLength();
                int overLimit = (currentLength + text.length()) - 5 - length;
                if (overLimit <= 0) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });

        // 入力キー処理
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    String input = textArea.getText();

                    if (input.length() == 5) {
                        input_char = input.toCharArray();
                        textArea.setText("");

                        isAcceptInGram = true;
                        for (int i = 0; i < 5; i++) {
                            int j = (int) input_char[i];
                            if (isInputAccepted(j)) {
                                notify.setText("Input is Accepted");
                                if (97 <= j && j <= 122) input_char[i] = (char) (j - 32);
                            } else {
                                isAcceptInGram = false;
                                clientThread.closableMessage.showMessage(k, "入力された文字列が[A-Za-z]^5に入っていません", "Error");
                                break;
                            }
                        }
                    } else {
                        isAcceptInGram = false;
                        notify.setText("Error: 入力された文字列の長さが適合しません");
                        clientThread.closableMessage.showMessage(k, "入力された文字列の長さが適合しません", "Error");
                    }

                    if (isAcceptInGram) {
                        for (int i = 0; i < 5; i++) {
                            input_char[i] = (char) ((int) input_char[i] + ('a' - 'A'));
                        }
                        input = new String(input_char);

                        if (wordList.isInList(input)) {
                            clientThread.sendMessage(input);
                            for (int i = 0; i < 5; i++) {
                                char tmp_c = (char) ((int) input_char[i] + 'A' - 'a');
                                if (!k.isUpdated(tmp_c)) {
                                    k.updateCol(tmp_c, Color.GRAY);
                                }
                            }
                        } else {
                            notify.setText("This word is not in List.");
                            clientThread.closableMessage.showMessage(k, "単語リストにありません", "Woops!");
                        }
                    }
                }
            }
        });
    }

    boolean isInputAccepted(int input_num) {
        return (input_num >= 65 && input_num <= 90) || (input_num >= 97 && input_num <= 122);
    }

    void setEndState(String str) {
        this.removeAll();
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel gameOverLabel = new JLabel(str);
        gameOverLabel.setFont(new Font("MS ゴシック", Font.BOLD, 24));
        gameOverLabel.setForeground(Color.RED);

        gbc.gridy = 0;
        this.add(gameOverLabel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));

        JButton exitButton = new JButton("終了");
        exitButton.setFont(new Font("MS ゴシック", Font.PLAIN, 16));
        exitButton.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
        });

        JButton retryButton = new JButton("タイトルに戻る");
        retryButton.setFont(new Font("MS ゴシック", Font.PLAIN, 16));
        retryButton.addActionListener(e -> {
            new W_Wordle_UI(100, 100);
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
        });

        buttonPanel.add(exitButton);
        buttonPanel.add(retryButton);

        gbc.gridy = 1;
        this.add(buttonPanel, gbc);

        this.revalidate();
        this.repaint();
    }

    void stopTextEnter() {
        System.out.println("stop");
        textArea.setBackground(Color.GRAY);
        textArea.setForeground(Color.WHITE);
        SwingUtilities.invokeLater(() -> textArea.setText("Wait..."));
        textArea.setEditable(false);
        textArea.repaint();
    }

    void resumeTextEnter() {
        System.out.println("resume");
        textArea.setBackground(Color.WHITE);
        textArea.setForeground(Color.BLACK);
        textArea.setEditable(true);
        SwingUtilities.invokeLater(() -> textArea.setText(""));
    }
}


class WordsArea extends JPanel {
    private JPanel contentPanel;
    private JScrollPane scrollPane;

    // WordPanelのインスタンスを管理するためのリスト
    private List<WordPanel> wordPanels = new ArrayList<>();

    public WordsArea() {
        setLayout(new BorderLayout());

        // 上部のラベル
        JLabel label = new JLabel("入力したWord", SwingConstants.CENTER);
        add(label, BorderLayout.NORTH);

        // WordPanelを縦に並べるパネル
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.GRAY);

        // contentPanelをスクロール可能にするスクロールペイン
        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    // 一番下までスクロールするメソッド
    public void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }

    // WordPanelを追加するメソッド
    public void addWordPanel(WordPanel wp) {
        wordPanels.add(wp);

        // 横幅はスクロール領域に合わせて固定（必要に応じて調整）
        wp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70)); // 横幅は制限せず、縦70に固定
        wp.setPreferredSize(new Dimension(400, 70));
        wp.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(wp);
        contentPanel.revalidate();
        contentPanel.repaint();

        wp.flipWord(); // アニメーション等（もしあれば）

        scrollToBottom();
    }

    // すべてのWordPanelの色を黒にする例
    public void makeAllBlack() {
        for (WordPanel wp : wordPanels) {
            wp.setAllColorBlack();
        }
    }

    // サーバーからのメッセージでWordPanelを作成して追加
    public void addWordByMsg(String word, int[] judgment) {
        if (word.length() != GLOBALVALS.wordLen || judgment.length != GLOBALVALS.wordLen) {
            throw new IllegalArgumentException("文字列または判定配列の長さが不正です");
        }

        Word w = new Word(word);
        for (int i = 0; i < GLOBALVALS.wordLen; i++) {
            w.isCorrect[i] = judgment[i];
        }

        WordPanel wp = new WordPanel(w);
        addWordPanel(wp);
    }
}

// Word1つが表示されるエリアを作る
class WordPanel extends JPanel {
    Word word;
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

    public void setAllColorBlack() {    // wordPanelの5文字すべてを黒くする
        // wordの正誤判定をすべて黒設定して再描画すればいけそう
        for(int i = 0; i < GLOBALVALS.wordLen; i++) {
            word.isCorrect[i] = 2;
        }
        repaint();
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
                    case -1: g2.setColor(Color.LIGHT_GRAY); break;
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
    
    // 正解を変更した時に正解判定色を変更する
    public void apdateIsCorrect(Word ans) {
        for(int i = 0; i < GLOBALVALS.wordLen; i++) {
            if(this.char_array[i] == ans.char_array[i]) {
                this.isCorrect[i] = 0;
            } else {
                this.isCorrect[i] = -1;
                for(int j = 0; j < GLOBALVALS.wordLen; j++){
                    if(this.char_array[i] == ans.char_array[j]) {
                        this.isCorrect[i] = 1;
                        break;
                    }
                }
            }
        }
    }
}

class GLOBALVALS {
    public static int wordLen = 5;
    public static boolean isEdit = false;    // 開発者向けオプション
}

class ItemShop{            //アイテムショップフレーム
    JFrame frame;
    WordleClientThread wordleClientThread;
    TextPanel textPanel;

    ItemShop(WordleClientThread wordleClientThread, TextPanel textPanel) {
        this.wordleClientThread = wordleClientThread;
        this.textPanel = textPanel;
        frame = new JFrame("ItemShop");
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbcFrame = new GridBagConstraints();
            
        //for文でitemshopフレームを作成
        for(Item item : Item.values()) {
            ImageIcon rawicon = new ImageIcon("./res/"+item.name()+"img.png");
            Image scaledImg = rawicon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            ImageIcon scaledicon = new ImageIcon(scaledImg);
            JButton itembutton = new JButton(item.getName(), scaledicon);
            int index = item.ordinal();

            gbcFrame.gridx = index;
            gbcFrame.gridy = 0;
            gbcFrame.fill = GridBagConstraints.NONE;  // 拡がらせない
            gbcFrame.insets = new Insets(2, 2, 2, 2); // マージン狭め
            gbcFrame.anchor = GridBagConstraints.CENTER;
            itembutton.setHorizontalTextPosition(SwingConstants.CENTER);
            itembutton.setVerticalTextPosition(SwingConstants.BOTTOM);
            itembutton.setToolTipText("<html>コスト:"+item.getCost()+"<BR>"+ item.getDescription()+"</html>");
            itembutton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                   wordleClientThread.sendMessage(String.valueOf(index+1));
                }
            });
            frame.add(itembutton, gbcFrame);
        }
        
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 閉じる直前にメソッドを1つ実行
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 任意の処理をここで実行
                wordleClientThread.sendMessage("9");
                textPanel.resumeTextEnter();    // 入力の受付を再開
                textPanel.itemButton.setEnabled(true);
            }
        });

        frame.setSize(900, 150);
        frame.setVisible(true);
    }

    void closeAction() {

    }
}
