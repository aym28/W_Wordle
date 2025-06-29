import java.io.*;
import java.net.*;
import java.util.Scanner;

public class WordleClient {
    // 接続するサーバーのポート番号
    public static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        InetAddress addr = InetAddress.getByName("localhost");
        System.out.println("サーバーに接続します: " + addr + " ポート: " + PORT);

        try (
            // サーバーとの接続を確立
            Socket socket = new Socket(addr, PORT);
            // サーバーへの送信用
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // サーバーからの受信用
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // ユーザーからのキーボード入力用
            Scanner stdIn = new Scanner(System.in)
        ) {
            System.out.println("サーバーに接続完了。");

            String serverMessage;
            // サーバーからのメッセージを待ち受け、表示し続けるループ
            while ((serverMessage = in.readLine()) != null) {
                System.out.println(serverMessage);

                // もしサーバーから入力要求が来たら、ユーザーの入力を待って送信する
                if (serverMessage.contains("入力してください")) {
                    String userInput = stdIn.nextLine();
                    out.println(userInput);
                }
                
                // ゲーム終了を示すメッセージが来たらループを抜ける
                if (serverMessage.contains("勝利") || serverMessage.contains("負け") || serverMessage.contains("引き分け")) {
                    // 最終結果を2行分受信して表示
                    System.out.println(in.readLine());
                    System.out.println(in.readLine());
                    break;
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("ホストに接続できません: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/Oエラー: " + e.getMessage());
        } finally {
            System.out.println("接続を終了します。");
        }
    }
}