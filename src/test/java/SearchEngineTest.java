import org.junit.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;

public class SearchEngineTest {

    @Test
    public void walkThroughDirTree() {
        SearchEngine temp = new SearchEngine();
        String dir = "C:\\Users\\iordn\\Desktop\\test";
        dir = dir.replace("\\", "/");
        List<Path> res = temp.walkThroughDirTree(dir);
        assertNotNull(res);
    }

    @Test
    public void getAllFiles() {
        SearchEngine temp = new SearchEngine();
        String dir = "C:\\Users\\iordn\\Desktop\\test";
        dir = dir.replace("\\", "/");
        List<Path> res = temp.getAllFiles(dir);
        assertNotNull(res);
    }

    @Test
    public void doesThePathExist() {
        String path = "helloSearchEngine";
        SearchEngine temp = new SearchEngine();
        assertFalse(temp.doesThePathExist(path));
    }

    @Test
    public void getAllMatches() {
        String path = "helloSearchEngine";
        String word = "hi";
        SearchEngine temp = new SearchEngine();
        assertFalse(temp.getAllMatches(path,word));
    }

}