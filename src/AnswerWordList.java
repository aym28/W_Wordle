import java.util.Arrays;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AnswerWordList {
  HashMap<String, Boolean> words;

  AnswerWordList() {
    words = new HashMap<>();
    try (BufferedReader textFile = new BufferedReader(new FileReader("res/answer_words.txt"))){
      String line;
      while ((line = textFile.readLine()) != null) {
        words.put(line, true);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public boolean isInList(String word) {
    return words.getOrDefault(word, false);
  }
}
