import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Please enter path and word to search in files");
        Scanner sc = new Scanner(System.in);
        String filepath = sc.next();
        String word = sc.next();
        SearchEngine temp = new SearchEngine();

        filepath = filepath.replace("\\", "/");

        temp.getAllMatches(filepath,word);
    }
}
