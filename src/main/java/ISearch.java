import java.nio.file.Path;
import java.util.List;

public interface ISearch {
    List<Path> walkThroughDirTree(String folder);

    List<Path> getAllFiles(String rootFolder);

    boolean doesThePathExist(String folder);

    boolean getAllMatches(String rootFolder, String word);

    void decompressArchive(String pathName, String destName);

    void decompressJARArchive(String pathName, String destName);


}
