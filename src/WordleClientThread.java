import java.io.*;
import java.net.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

public class WordleClientThread extends Thread {
    W_Wordle_UI ui;
    private String host;
    private int port;
    private Consumer<String> messageHandler;
    private Supplier<String> inputSupplier;
    PrintWriter out;

    public WordleClientThread(W_Wordle_UI ui, String host, int port, Consumer<String> messageHandler, Supplier<String> inputSupplier) {
        this.ui = ui;
        this.host = host;
        this.port = port;
        this.messageHandler = messageHandler;
        this.inputSupplier = inputSupplier;
    }

    @Override
    public void run() {
        ClosableMessage closableMessage = new ClosableMessage();
        try (
            Socket socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out_tmp = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)
        ) {
            out = out_tmp;
            String line;
            while ((line = in.readLine()) != null) {

                // メッセージ"Game Start"を受け取ったらゲーム画面に移行する
                if(line.contains("Game Start")) {
                    ui.startGame();
                    closableMessage.closeAll();     // ゲーム開始後に待機通知を気にする人なんていないので消す
                    System.out.println("ゲームを開始する処理");
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
                        closableMessage.closeAll();
                        closableMessage.showMessage(ui.frame, "推測する単語を入力してください。('item'でアイテムストア)", "あなたのターンです");
                    }
                } else if(line.contains("勝利") || line.contains("負け")) {
                    closableMessage.closeAll();
                    if(ui.k != null) {
                        ui.k.getTextPanel().stopTextEnter();
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
                    } else {
                    // 通常メッセージ表示
                    //messageHandler.accept(line);
                    if (ui.k != null) { // Make sure GameFrame (k) has been initialized
                        ui.k.getTextPanel().notify.setText(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            closableMessage.showMessage(ui.frame, "サーバーとの接続中にエラーが発生しました", "エラー");
        }
    }

    // メッセージ送信用
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
        dialog.setModal(false); // モードレスで表示
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