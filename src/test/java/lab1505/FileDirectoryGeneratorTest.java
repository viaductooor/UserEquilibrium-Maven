package lab1505;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.lab1505.ue.fileutil.FileDirectoryGenerator;

public class FileDirectoryGeneratorTest {

    @Test
    @RepeatedTest(value=10,name = RepeatedTest.LONG_DISPLAY_NAME)
    public void createSameNameFiles() {
        String filename = "test";
        String suffix = "txt";
        File file = FileDirectoryGenerator.createFileAutoRename(filename, suffix);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(file.getAbsolutePath());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}