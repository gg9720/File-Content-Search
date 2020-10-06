import org.apache.commons.io.FilenameUtils;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SearchEngine implements ISearch {

    private static final String arch = "zip";
    private static final String arch1 = "jar";


    /** Method which accepts path to folder and traverses the whole directory
     * tree with all subdirectories.
     *
     * @param folder an absolute path to folder which we will traverse
     * @return list with all files in the @param
     */
    public List<Path> walkThroughDirTree(String folder) {
        List<Path> temp = new LinkedList<>();
        if (doesThePathExist(folder)) {
            try {
                Files.walk(Paths.get(folder)).forEach(path -> {
                    temp.add(path);
                });
            } catch (IOException e) {
                System.out.println("Exception thrown: " + e);
            }
        }
        else{
            return null;
        }
        return temp;

    }

    /** Method that returns list which includes Path to files. These files
     * we will process to search about the word.
     *
     * @param rootFolder a path to the given folder from user
     * @return Returns list with Path to all files plus unzipped archives
     */
    public List<Path> getAllFiles(String rootFolder) {
        List<Path> paths = walkThroughDirTree(rootFolder);
        try {

            for (int i = 0; i < paths.size(); i++) {

                String ext1 = FilenameUtils.getExtension(paths.get(i).toString());
//            System.out.println(ext1);
//            System.out.println("===================");
                File temp = new File(paths.get(i).toString());
                if (temp.isDirectory()) {
                    continue;
                }
                if (ext1.equals(arch1)) {
                    decompressJARArchive(paths.get(i).toString(), rootFolder);
                }
                if (ext1.equals(arch)) {
                    decompressArchive(paths.get(i).toString(), rootFolder);
                }
            }
        }catch(NullPointerException ex){
            System.out.println("No existing path " + ex);
        }
        paths = walkThroughDirTree(rootFolder);

        return paths;
    }

    /**
     *
     * @param folder given String which needs to be the Path to the folder
     * @return if there is a Path to the folder or not
     */
    @Override
    public boolean doesThePathExist(String folder) {
        File temp = new File(folder);
        return temp.exists();
    }

    /** The method which by given folder and word is searching inside
     * files for matches.
     *
     * @param rootFolder Path to the folder that the user needs
     * @param word for this String word we will search in each file
     *             of the folder
     * @return returns true if there are any matches and false if
     * the user selected wrong path to folder or No Matches
     */
    @Override
    public boolean getAllMatches(String rootFolder, String word) {
        List<Path> paths = getAllFiles(rootFolder);
        if(paths==null){
            return false;
        }
        TreeSet<Match> results = new TreeSet<>();
        for (int i = 0; i < paths.size(); i++) {
            File temp = new File(paths.get(i).toString());
            if (temp.isDirectory()) {
                continue;
            }
            try {
                FileInputStream fis = new FileInputStream(paths.get(i).toString());
                Scanner sc = new Scanner(fis);    //file to be scanned
                //returns true if there is another line to read
                while (sc.hasNextLine()) {
                    String x = sc.nextLine();
                    if (x.contains(word)) {
                        //System.out.println(" Path " + paths.get(i) + "  contains the word");
                        Match tempp = new Match(Files.size(paths.get(i)), paths.get(i).toString());
                        results.add(tempp);
                    }
                }
                sc.close();     //closes the scanner
            } catch (IOException e) {
                System.out.println("Exception thrown: " + e);
            }
        }
        if (results.isEmpty()) {
            System.out.println("No matches");
            return false;
        }
        for (Match e : results) {
            System.out.println(e);
        }
        return true;

    }

    /** Unzipping the ZIP file format archive. Reads the archive file
     * by file as ZipEntries and writing the information into new files
     * in the destination folder
     *
     * @param pathName Path to the archive file
     * @param destName Path to the folder in which we want to unzip the archive
     */
    @Override
    public void decompressArchive(String pathName, String destName) {
        pathName = pathName.replace("\\", "/");

        destName = destName.replace("\\", "/");

        File destDirectoryFolder = new File(destName);


        byte[] buffer = new byte[1024];
        try {

            ZipInputStream zis = new ZipInputStream(new FileInputStream(pathName));
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                String filePath = destDirectoryFolder + File.separator + zipEntry.getName();
                //System.out.println("Unzipping " + filePath);
                if (!zipEntry.isDirectory()) {
                    FileOutputStream fos = new FileOutputStream(filePath);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                } else {
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Exception thrown: " + e);
        }

    }

    /** The same logic as the method above
     *
     * @param pathName Path to the archive file
     * @param destName Path to the folder in which we want to unzip the archive
     */
    @Override
    public void decompressJARArchive(String pathName, String destName) {
        try {

            JarFile jar = new JarFile(pathName);
            Enumeration jEntries = jar.entries();
            while (jEntries.hasMoreElements()) {
                JarEntry file = (JarEntry) jEntries.nextElement();
                File f = new File(destName + java.io.File.separator + file.getName());

                InputStream is = jar.getInputStream(file);
                FileOutputStream fos = new FileOutputStream(f);
                while (is.available() > 0) {
                    fos.write(is.read());
                }
                fos.close();
                is.close();
            }
        } catch (IOException e) {
            System.out.println("Exception thrown: " + e);
        }
    }


}

