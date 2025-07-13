import java.io.*;
import java.net.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WordleClientThread extends Thread {
    private String host;
    private int port;
    private Consumer<String> messageHandler;
    private Supplier<String> inputSupplier;

    public WordleClientThread(String host, int port, Consumer<String> messageHandler, Supplier<String> inputSupplier) {
        this.host = host;
        this.port = port;
        this.messageHandler = messageHandler;
        this.inputSupplier = inputSupplier;
    }

    @Override
    public void run() {
        try (
            Socket socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                // PROMPTがついたら入力を促す
                if (line.endsWith("|PROMPT")) {
                    String promptMessage = line.replace("|PROMPT", "").trim();

                    // UIスレッドで入力を取得する
                    String userInput = inputSupplier.get();  // 例: JOptionPane.showInputDialog(...)
                    if (userInput != null && !userInput.trim().isEmpty()) {
                        out.println(userInput.trim());
                    } else {
                        out.println("");  // 空文字列も送る（キャンセルされた場合）
                    }

                } else {
                    // 通常メッセージ表示
                    messageHandler.accept(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageHandler.accept("サーバとの接続中にエラーが発生しました。");
        }
    }
}
