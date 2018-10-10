import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public class Generate extends DefaultTask {
    private int fileCount;
    private String content;
    private File generatedFileDir;

    @Input
    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    @Input
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @OutputDirectory
    public File getGeneratedFileDir() {
        return generatedFileDir;
    }

    public void setGeneratedFileDir(File generatedFileDir) {
        this.generatedFileDir = generatedFileDir;
    }

    @TaskAction
    public void perform() throws IOException {
        for (int i = 1; i <= fileCount; i++) {
            writeFile(new File(generatedFileDir, i + ".txt"), content);
        }
    }

    private void writeFile(File destination, String content) throws IOException {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(destination));
            output.write(content);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
}