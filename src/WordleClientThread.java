import java.io.*;
import java.net.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import javax.swing.JFrame;
import java.awt.Color;

public class WordleClientThread extends Thread {
    W_Wordle_UI ui;
    private String host;
    private int port;
    private Consumer<String> messageHandler;
    private Supplier<String> inputSupplier;
    PrintWriter out;
    ClosableMessage closableMessage;

    public WordleClientThread(W_Wordle_UI ui, String host, int port, Consumer<String> messageHandler, Supplier<String> inputSupplier) {
        this.ui = ui;
        this.host = host;
        this.port = port;
        this.messageHandler = messageHandler;
        this.inputSupplier = inputSupplier;
    }

    @Override
    public void run() {
        closableMessage = new ClosableMessage();
        try (
            Socket socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out_tmp = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)
        ) {
            out = out_tmp;
            String line;
            while ((line = in.readLine()) != null) {

                if(line.contains("接続しました")) {
                    ui.connectButton.setEnabled(false);
                }

                // メッセージ"Game Start"を受け取ったらゲーム画面に移行する
                if(line.contains("Game Start")) {
                    ui.startGame();
                    closableMessage.closeAll();     // ゲーム開始後に待機通知を気にする人なんていないので消す
                    System.out.println("ゲームを開始する処理");
                }

                if(ui.k != null) {
                    if(line.contains("('item'でアイテムストア)")) {
                        ui.k.getTextPanel().itemButton.setEnabled(true);
                    } else {
                        ui.k.getTextPanel().itemButton.setEnabled(false);
                    }
                }

                // アイテムショップの処理
                if(line.contains("|ITEM")) {
                    line = line.replace("|ITEM","");
                    closableMessage.showMessage(ui.frame,line,"アイテムショップ");
                    ui.k.getTextPanel().stopTextEnter();
                    continue;
                }
                

                // PROMPTがついたら入力を促す
                if (line.endsWith("|PROMPT")) {
                    if(ui.k != null) {
                            ui.k.getTextPanel().resumeTextEnter();
                        }
                    if(line.contains("お題")) {
                        String promptMessage = line.replace("|PROMPT", "").trim();

                        // UIスレッドで入力を取得する
                        String userInput = inputSupplier.get();  // 例: JOptionPane.showInputDialog(...)
                        if (userInput != null && !userInput.trim().isEmpty()) {
                            out.println(userInput.trim());
                        } else {
                            out.println("");  // 空文字列も送る（キャンセルされた場合）
                        }
                    } 
                    if(line.contains("推測")) {
                        //closableMessage.closeAll();
                        closableMessage.showMessage(ui.frame, "推測する単語を入力してください。('item'でアイテムストア)", "あなたのターンです");
                    }
                } else if(line.contains("勝利") || line.contains("負け") || line.contains("引き分け")) {
                    closableMessage.closeAll();
                    if(ui.k != null) {
                        if(line.contains("勝利")) {
                            ui.k.getTextPanel().setEndState("勝利");
                        } else if(line.contains("負け")) {
                            ui.k.getTextPanel().setEndState("敗北");
                        } else if(line.contains("引き分け")) {
                            ui.k.getTextPanel().setEndState("引き分け");
                        }
                    }
                    line = line.replace("/n","\n"); // 改行を復号
                    
                    closableMessage.showMessage(ui.frame, line, "ゲーム結果");
                    //javax.swing.JOptionPane.showMessageDialog(ui.frame, line, "ゲーム結果", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                } else if(   line.contains("お題を設定しました。相手の入力を待っています...")
                          || line.contains("対戦相手を待っています...")
                          || line.contains("待機")) {
                    if(ui.k != null) {
                        ui.k.getTextPanel().stopTextEnter();
                    }
                    closableMessage.showMessage(ui.frame, line, "待機");
                } else if(line.contains("|ANS")) {  // 正解判定が返ってきたとき
                    line = line.replace("|ANS", "");   // タグを除く
                    if(ui.k != null) {
                        ui.k.getTextPanel().stopTextEnter();
                    }
                    System.out.println("ANS msg: " + line);

                    
                    String[] tokens = line.split(" ");
                    String guessedWord = tokens[0];
                    int[] judgment = new int[tokens.length - 1];
                    for (int i = 1; i < tokens.length; i++) {
                        judgment[i - 1] = Integer.parseInt(tokens[i]);
                    }
                    System.out.println("str: " + guessedWord);
                    System.out.println("judge: " + Arrays.toString(judgment));
                    // 解答判定のメッセージが届いたのでここでWordPanelに反映するロジックを書く
                    // 答案文字列と判定は分別済
                    //ui.k.wordsArea.scrollToBottom();
                    ui.k.wordsArea.addWordByMsg(guessedWord, judgment);

                    // キーボードにも反映する
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        // スレッドが中断されたらループを抜ける
                        break;
                    }
                    for(int i = 0; i < GLOBALVALS.wordLen; i++) {
                        char c = guessedWord.toUpperCase().toCharArray()[i];
                        switch (judgment[i]) {
                            case 0:
                                ui.k.getKeyBoardPanel().updateCol(c,Color.GREEN);
                                break;
                            case 1:
                                ui.k.getKeyBoardPanel().updateCol(c, new Color(255, 204, 0));
                                break;
                            case -1:
                                ui.k.getKeyBoardPanel().updateCol(c, Color.BLACK);
                                break;
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        // スレッドが中断されたらループを抜ける
                        break;
                    }

                } {
                    // 通常メッセージ表示
                    //messageHandler.accept(line);
                    //if (ui.k != null) { // Make sure GameFrame (k) has been initialized
                        //ui.k.getTextPanel().notify.setText(line);
                    //}
                }
                
            }
        } catch (IOException e) {
            e.printStackTrace();
            closableMessage.showMessage(ui.frame, "サーバーとの接続中にエラーが発生しました", "エラー");
        }
    }

    // メッセージ送信用, 推測とアイテムを送信するときに使う．
    public void sendMessage(String msg) {
        if (out != null) {
            System.out.println("SendMsg");
            out.println(msg);
            out.flush();
        }
    }
}

// 自動で閉じることができるメッセージダイアログ
class ClosableMessage {
    private final List<JDialog> dialogs = new ArrayList<>();

    public void showMessage(Component parent, String message, String title) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = pane.createDialog(parent, title);
        dialog.setModal(false);

        // 位置調整：frameの下側に表示（親コンポーネントの下中央あたり）
        if (parent instanceof JFrame) {
            JFrame frame = (JFrame) parent;
            int x = frame.getX() + frame.getWidth() / 2 - dialog.getWidth() / 2;
            int y = frame.getY() + frame.getHeight() - 200; // 下から50px上あたり
            dialog.setLocation(x, y);
        }

        dialog.setVisible(true);
        dialogs.add(dialog);
    }

    public void closeAll() {
        for (JDialog d : dialogs) {
            if (d.isShowing()) {
                d.dispose();
            }
        }
        dialogs.clear();
    }
}