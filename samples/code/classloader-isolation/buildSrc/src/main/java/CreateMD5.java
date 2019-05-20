import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.*;
import org.gradle.process.JavaForkOptions;
import org.gradle.workers.*;

import javax.inject.Inject;
import java.io.File;
import java.util.Set;

public class CreateMD5 extends SourceTask {
    private final WorkerExecutor workerExecutor;
    private final DirectoryProperty destinationDirectory;
    private final ConfigurableFileCollection codecClasspath; // <1>

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
        for (File sourceFile : getSource().getFiles()) {
            File md5File = destinationDirectory.file(sourceFile.getName() + ".md5").get().getAsFile();
            workerExecutor.submit(GenerateMD5.class, new Action<WorkerConfiguration>() {
                @Override
                public void execute(WorkerConfiguration config) {
                    config.setIsolationMode(IsolationMode.CLASSLOADER);
                    config.params(sourceFile, md5File);
                    config.classpath(codecClasspath); // <2>
                }
            });
        }
    }
}
