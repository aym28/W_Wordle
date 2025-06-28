import java.util.Scanner;
import java.util.Arrays;

public class Main {
  public static final int MAX_GAME_ROUND = 6;
  public static final int WORD_SIZE = 5;

  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    String ansStrA;
    String ansStrB;
    System.out.println("PlayerA: Enter the answer word.");
    while (true) {
      ansStrA = sc.nextLine().toLowerCase();
      if (WordList.isInList(ansStrA)) {break;}
      System.out.println("Error. The word is not in the word list.");
    }
    System.out.println("PlayerB: Enter the answer word.");
    while (true) {
      ansStrB = sc.nextLine().toLowerCase();
      if (WordList.isInList(ansStrB)) {break;}
      System.out.println("Error. The word is not in the word list.");
    }
    System.out.println("------------------------------");
    System.out.println("Game Start");
    System.out.println("------------------------------");
    Player playerA = new Player(MAX_GAME_ROUND, WORD_SIZE, ansStrB);
    Player playerB = new Player(MAX_GAME_ROUND, WORD_SIZE, ansStrA);
    int gameCount = 0;
    boolean winA = false;
    boolean winB = false;
    String answerA;
    String answerB;
    while (gameCount < MAX_GAME_ROUND) {
      System.out.printf("(ROUND %d) PlayerA: Enter a word.\n", gameCount + 1);
      while (true) {
        answerA = sc.nextLine().toLowerCase();
        if (WordList.isInList(answerA)) {break;}
        System.out.println("Error. The word is not in the word list.");
      }
      playerA.pushAnswer(answerA, gameCount);
      playerA.printAnswerSheet();
      winA = playerA.judgeAnswer(answerA);
      System.out.println("------------------------------");
      System.out.printf("(ROUND %d) PlayerB: Enter a word.\n", gameCount + 1);
      while (true) {
        answerB = sc.nextLine().toLowerCase();
        if (WordList.isInList(answerB)) {break;}
        System.out.println("Error. The word is not in the word list.");
      }
      playerB.pushAnswer(answerB, gameCount);
      playerB.printAnswerSheet();
      winB = playerB.judgeAnswer(answerB);
      System.out.println("------------------------------");
      if (winA || winB) {break;}
      gameCount++;
    }
    if (winA && winB) {
      System.out.println("Draw. Both players answered correctly.");
    }
    else if (winA) {
      System.out.println("PlayerA won.");
    }
    else if (winB) {
      System.out.println("PlayerB won.");
    }
    else {
      System.out.println("Draw. No players answered correctly.");
    }
    System.out.println("PlayerA's word: " + ansStrA);
    System.out.println("PlayerB's word: " + ansStrB);
  }
}

class Player {
  int MAX_GAME_ROUND;
  int WORD_SIZE;
  String answer;
  String[] answerSheet;
  final String COLOR_GREEN = "\u001b[00;42m";
  final String COLOR_YELLOW = "\u001b[00;43m";
  final String COLOR_END = "\u001b[00m";

  Player(int maxGameRound, int wordSize, String answer) {
    this.MAX_GAME_ROUND = maxGameRound;
    this.WORD_SIZE = wordSize;
    this.answer = answer;
    this.answerSheet = new String[MAX_GAME_ROUND];
    Arrays.fill(this.answerSheet, "");
  }

  public void printAnswerSheet() {
    for (var word : this.answerSheet) {
      if (word.equals("")) {
        System.out.print(" _ _ _ _ _");
      }
      else {
        for (int i = 0; i < WORD_SIZE; i++) {
          System.out.print(" ");
          if (word.charAt(i) == this.answer.charAt(i)) {
            System.out.print(COLOR_GREEN + word.charAt(i) + COLOR_END);
          }
          else if (this.answer.contains(String.valueOf(word.charAt(i)))) {
            System.out.print(COLOR_YELLOW + word.charAt(i) + COLOR_END);
          }
          else {
            System.out.print(word.charAt(i));
          }
        }
      }
      System.out.println("");
    }
  }

  public void pushAnswer(String word, int gameCount) {
    this.answerSheet[gameCount] = word;
  }

  public boolean judgeAnswer(String word) {
    return (word.equals(this.answer));
  }
}

