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


    /**
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
                    try {
                        decompressJARArchive(paths.get(i).toString(), rootFolder);
                    } catch (Exception e) {
                        System.out.println("Exception thrown: " + e);
                    }
                }
                if (ext1.equals(arch)) {
                    try {
                        decompressArchive(paths.get(i).toString(), rootFolder);
                    } catch (Exception e) {
                        System.out.println("Exception thrown: " + e);
                    }
                }
            }
        }catch(NullPointerException ex){
            System.out.println("No existing path " + ex);
        }
        paths = walkThroughDirTree(rootFolder);

        return paths;
    }

    @Override
    public boolean doesThePathExist(String folder) {
        File temp = new File(folder);
        return temp.exists();
    }

    @Override
    public void getAllMatches(String rootFolder, String word) {
        List<Path> paths = getAllFiles(rootFolder);
        if(paths==null){
            return;
        }
        TreeSet<Match> results = new TreeSet<>();
        if (paths == null) {
            return;
        }
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
        }
        for (Match e : results) {
            System.out.println(e);
        }

    }

    @Override
    public void decompressArchive(String fileName, String destName) {
        fileName = fileName.replace("\\", "/");

        destName = destName.replace("\\", "/");

        File destDirectoryFolder = new File(destName);


        byte[] buffer = new byte[1024];
        try {

            ZipInputStream zis = new ZipInputStream(new FileInputStream(fileName));
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

