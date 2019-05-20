import org.gradle.api.Action;
import org.gradle.api.tasks.*;
import org.gradle.workers.*;
import org.gradle.api.file.DirectoryProperty;

import javax.inject.Inject;
import java.io.File;

public class CreateMD5 extends SourceTask {
    private final WorkerExecutor workerExecutor; // <1>
    private final DirectoryProperty destinationDirectory;

    @Inject
    public CreateMD5(WorkerExecutor workerExecutor) { // <2>
        super();
        this.workerExecutor = workerExecutor;
        this.destinationDirectory = getProject().getObjects().directoryProperty();
    }

    @OutputDirectory
    public DirectoryProperty getDestinationDirectory() {
        return destinationDirectory;
    }

    @TaskAction
    public void createHashes() {
        for (File sourceFile : getSource().getFiles()) {
            File md5File = destinationDirectory.file(sourceFile.getName() + ".md5").get().getAsFile();
            workerExecutor.submit(GenerateMD5.class, new Action<WorkerConfiguration>() { // <3>
                @Override
                public void execute(WorkerConfiguration config) {
                    config.setIsolationMode(IsolationMode.NONE); // <4>
                    config.params(sourceFile, md5File); // <5>
                }
            });
        }
    }
}
