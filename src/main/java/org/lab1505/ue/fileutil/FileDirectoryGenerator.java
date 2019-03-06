package org.lab1505.ue.fileutil;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The class is used to create a File simply.
 */
public class FileDirectoryGenerator {
    private final static String BASE_URL = "output";

    /**
     * Get a date string of the format yyyy-MM-dd.
     * 
     * @return the present date string
     */
    private static String getLocalDateString() {
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return time.format(formatter);
    }

    /**
     * Create a File whose relative directory is like "output/2019-3-5/filename".
     * 
     * @param fileName name of the file
     * @return the file with its parent folders
     */
    public static File createDefaultFile(String fileName) {
        String url = getBaseDirectory() + fileName;
        File file = new File(url);
        File parent = new File(file.getParent());
        if (!parent.exists()) {
            parent.mkdirs();
        }
        return file;
    }

    /**
     * Create a File whose relative directory is like
     * "output/2019-3-5/filename.suffix". Rename it automatically if there already
     * exists one same file. Its parent directory is to be created if there is not.
     * 
     * @param filename the file name without suffix
     * @param suffix   the extension name of the file
     * 
     * @return the file
     */
    public static File createFileAutoRename(String filename, String suffix) {
        String pre = getBaseDirectory() + filename;
        int count = 1;
        File file = new File(pre + "." + suffix);
        File parent = new File(file.getParent());
        if (!parent.exists()) {
            parent.mkdirs();
        }
        if (file.exists()) {
            for (;;) {
                file = new File(pre + "_" + count++ + "." + suffix);
                if (!file.exists()) {
                    return file;
                }
            }
        } else {
            return file;
        }
    }

    private static String getBaseDirectory() {
        return BASE_URL + "/" + getLocalDateString() + "/";
    }
}