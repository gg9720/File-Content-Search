import org.apache.commons.io.FilenameUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jsoup.Jsoup;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.apache.commons.lang3.StringEscapeUtils.escapeXml11;

public class SearchEngine implements ISearch {

    private static final String arch = "zip";
    private static final String arch1 = "jar";
    private static final String arch2 = "html";
    private static final String arch3 = "xml";


    /**
     * Method which accepts path to folder and traverses the whole directory
     * tree with all subdirectories.
     *
     * @param folder an absolute path to folder which we will traverse
     * @return list with all files in the @param
     */
    public List<Path> walkThroughDirTree(String folder) {
        List<Path> listOfFiles = new LinkedList<>();
        if (doesThePathExist(folder)) {
            try {
                Files.walk(Paths.get(folder)).forEach(path -> {
                    listOfFiles.add(path);
                });
            } catch (IOException e) {
                System.out.println("Exception thrown: " + e);
            }
        } else {
            return null;
        }
        return listOfFiles;

    }


    /**
     * Method that returns list which includes Path to files. These files
     * we will process to search about the word.
     *
     * @param rootFolder a path to the given folder from user
     * @return Returns list with Path to all files plus unzipped archives
     */
    public List<Path> getAllFiles(String rootFolder) {
        List<Path> pathsOfAllFiles = walkThroughDirTree(rootFolder);
        try {

            for (int i = 0; i < pathsOfAllFiles.size(); i++) {

                String ext1 = FilenameUtils.getExtension(pathsOfAllFiles.get(i).toString());
                File temp = new File(pathsOfAllFiles.get(i).toString());
                if (temp.isDirectory()) {
                    continue;
                }
                if (ext1.equals(arch1)) {
                    decompressJARArchive(pathsOfAllFiles.get(i).toString(), rootFolder);
                }
                if (ext1.equals(arch)) {
                    decompressArchive(pathsOfAllFiles.get(i).toString(), rootFolder);
                }
            }
        } catch (NullPointerException ex) {
            System.out.println("No existing path " + ex);
        } catch (IOException e) {
            e.printStackTrace();
        }
        pathsOfAllFiles = walkThroughDirTree(rootFolder);
//        for (Path s : pathsOfAllFiles) {
//            System.out.println(FilenameUtils.getExtension(s.toString()));
//        }

        return pathsOfAllFiles;
    }


    /**
     * @param folder given String which needs to be the Path to the folder
     * @return if there is a Path to the folder or not
     */
    @Override
    public boolean doesThePathExist(String folder) {
        File temp = new File(folder);
        return temp.exists();
    }

    /**
     * The method which by given folder and word is searching inside
     * files for matches.
     *
     * @param rootFolder Path to the folder that the user needs
     * @param word       for this String word we will search in each file
     *                   of the folder
     * @return returns true if there are any matches and false if
     * the user selected wrong path to folder or No Matches
     */
    @Override
    public boolean getAllMatches(String rootFolder, String word) {
        List<Path> paths = getAllFiles(rootFolder);
        if (paths == null) {
            return false;
        }
        TreeSet<Match> results = new TreeSet<>();
        for (int i = 0; i < paths.size(); i++) {
            if (FilenameUtils.getExtension(paths.get(i).toString()).equals(arch3)) {
                if (parseXMLAndSearch(paths.get(i).toString(), word)) {
                    try {
                        Match xmlFile = new Match(Files.size(paths.get(i)), paths.get(i).toString());
                        results.add(xmlFile);
                        continue;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (FilenameUtils.getExtension(paths.get(i).toString()).equals(arch2)) {
                if (parseHTMLAndSearch(paths.get(i).toString(), word)) {
                    try {
                        Match htmlFile = new Match(Files.size(paths.get(i)), paths.get(i).toString());
                        results.add(htmlFile);
                        continue;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            File temp = new File(paths.get(i).toString());


            if (temp.isDirectory()) {
                continue;
            }
            try {
                FileInputStream fis = new FileInputStream(paths.get(i).toString());
                Scanner sc = new Scanner(fis);

                while (sc.hasNextLine()) {
                    String x = sc.nextLine();
                    if (x.contains(word)) {
                        Match tempp = new Match(Files.size(paths.get(i)), paths.get(i).toString());
                        results.add(tempp);
                    }
                }
                sc.close();
            } catch (IOException e) {
                System.out.println("Exception thrown: " + e);
            }
        }
        if (results.isEmpty()) {
            System.out.println("No matches");
            return false;
        }
        System.out.println("Files containing the given word: ");
        for (Match e : results) {
            System.out.println(e);
        }
        return true;

    }

    @Override
    public boolean parseXMLAndSearch(String pathName, String word) {
        try {
            File inputFile = new File(pathName);
            SAXBuilder saxBuilder = new SAXBuilder();

            Document doc = saxBuilder.build(inputFile);
            Element classElement = (Element) doc.getRootElement();
            ;
            String res = escapeXml11(classElement.getChildren().toString());
            //System.out.println(res);
            if (res.contains(word)) {
                return true;
            }
            if (classElement.getValue().equals(word)) {
                return true;
            }
            List<Element> studentList = classElement.getChildren();
            for (Element e : studentList) {
                String temp = escapeXml11(e.getChildren().toString());
                if (temp.contains(word)) {
                    return true;
                }
                if (e.getValue().contains(word)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return false;
    }

    @Override
    public boolean parseHTMLAndSearch(String pathName, String word) {

        File inputFile = new File(pathName);
        org.jsoup.nodes.Document doc = null;
        try {
            doc = Jsoup.parse(inputFile, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc.getElementsMatchingOwnText(word).size() > 0;
    }

    /**
     * Unzipping the ZIP file format archive. Reads the archive file
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

    /**
     * The same logic as the method above
     *
     * @param pathName Path to the archive file
     * @param destName Path to the folder in which we want to unzip the archive
     */
    @Override
    public void decompressJARArchive(String pathName, String destName) throws IOException {

        JarFile jarfile = null;
        try {
            jarfile = new JarFile(pathName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        java.util.Enumeration<JarEntry> allJarEntries = jarfile.entries();
        while (allJarEntries.hasMoreElements())
        {
            JarEntry jarEntry = allJarEntries.nextElement();

            File currFileFromArchive = new File(destName, jarEntry.getName());
            if(!currFileFromArchive.exists())
            {
                currFileFromArchive.getParentFile().mkdirs();
                currFileFromArchive = new File(destName, jarEntry.getName());
            }
            if(jarEntry.isDirectory())
            {
                continue;
            }
            InputStream is = jarfile.getInputStream(jarEntry);
            FileOutputStream fo = new FileOutputStream(currFileFromArchive);
            while(is.available()>0)
            {
                fo.write(is.read());
            }
            fo.close();
            is.close();
        }

    }
}


