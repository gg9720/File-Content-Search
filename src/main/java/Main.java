import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Please enter path and word to search in files");
        Scanner sc = new Scanner(System.in);
        String filepath = sc.next();
        String word = sc.next();
        SearchEngine temp = new SearchEngine();

        filepath = filepath.replace("\\", "/");

        List<Path> list = temp.getAllFiles(filepath);

        temp.getAllMatches(filepath,word);
    }
}
