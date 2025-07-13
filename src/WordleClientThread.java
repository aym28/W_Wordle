import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WordleClientThread extends Thread {
    private String host;
    private int port;
    private Consumer<String> displayCallback; // メッセージ受信用コールバック
    private Supplier<String> inputSupplier;   // 入力用コールバック

    public WordleClientThread(String host, int port, Consumer<String> displayCallback, Supplier<String> inputSupplier) {
        this.host = host;
        this.port = port;
        this.displayCallback = displayCallback;
        this.inputSupplier = inputSupplier;
    }

    @Override
    public void run() {
        try (
            Socket socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                String[] parts = serverMessage.split("\\|", 2);
                String displayMessage = parts[0].replace("\\n", "\n");
                String command = (parts.length > 1) ? parts[1] : "";

                displayCallback.accept(displayMessage);

                if ("PROMPT".equals(command)) {
                    String userInput = inputSupplier.get();
                    out.println(userInput);
                }

                if (serverMessage.contains("勝利") || serverMessage.contains("負け") || serverMessage.contains("引き分け")) {
                    displayCallback.accept(in.readLine());
                    displayCallback.accept(in.readLine());
                    break;
                }
            }
        } catch (IOException e) {
            displayCallback.accept("エラー: " + e.getMessage());
        }
    }
}
