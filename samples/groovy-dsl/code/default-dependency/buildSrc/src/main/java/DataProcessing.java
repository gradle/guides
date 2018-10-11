import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

public class DataProcessing extends DefaultTask {
    private final ConfigurableFileCollection dataFiles;
    
    public DataProcessing() {
        dataFiles = getProject().files();
    }

    @InputFiles
    public FileCollection getDataFiles() {
        return dataFiles;
    }

    public void setDataFiles(FileCollection dataFiles) {
        this.dataFiles.setFrom(dataFiles);
    }

    @TaskAction
    public void process() {
        System.out.println(getDataFiles().getFiles());
    }
}