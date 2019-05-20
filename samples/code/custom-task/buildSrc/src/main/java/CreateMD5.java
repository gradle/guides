
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.*;
import org.gradle.process.JavaForkOptions;
import org.gradle.workers.*;

import javax.inject.Inject;
import java.io.File;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import java.io.FileInputStream;
import java.io.InputStream;

public class CreateMD5 extends SourceTask { // <1>
    private final WorkerExecutor workerExecutor;
    private final DirectoryProperty destinationDirectory; // <2>
    private final ConfigurableFileCollection codecClasspath;

    @Inject
    public CreateMD5(WorkerExecutor workerExecutor) {
        super();
        this.workerExecutor = workerExecutor;
        this.destinationDirectory = getProject().getObjects().directoryProperty();
        this.codecClasspath = getProject().getObjects().fileCollection();
    }

    @OutputDirectory
    public DirectoryProperty getDestinationDirectory() {
        return destinationDirectory;
    }

    @InputFiles
    public ConfigurableFileCollection getCodecClasspath() {
        return codecClasspath;
    }

    @TaskAction
    public void createHashes() {
        for (File sourceFile : getSource().getFiles()) { // <3>
            try {
                InputStream stream = new FileInputStream(sourceFile);
                System.out.println("Generating MD5 for " + sourceFile.getName() + "...");
                Thread.sleep(3000); // <4>
                File md5File = destinationDirectory.file(sourceFile.getName() + ".md5").get().getAsFile(); // <5>
                FileUtils.writeStringToFile(md5File, DigestUtils.md5Hex(stream), (String) null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
