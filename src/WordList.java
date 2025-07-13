import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WordList {
    private HashMap<String, Boolean> words;

    public WordList() {
        words = new HashMap<>();
        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    getClass().getResourceAsStream("../res/wordlist.txt"),
                    StandardCharsets.UTF_8
                )
            )
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                words.put(line.trim().toLowerCase(), true);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            System.err.println("wordlist.txt が見つかりませんでした。/res/ に配置してください。");
        }
    }

    public boolean isInList(String word) {
        return words.getOrDefault(word.toLowerCase(), false);
    }
}
