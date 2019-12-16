import org.gradle.api.Action;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
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
        WorkQueue workQueue = workerExecutor.noIsolation(); // <3>

        for (File sourceFile : getSource().getFiles()) {
            Provider<RegularFile> md5File = destinationDirectory.file(sourceFile.getName() + ".md5");
            workQueue.submit(GenerateMD5.class, parameters -> { // <4>
                parameters.getSourceFile().set(sourceFile);
                parameters.getMD5File().set(md5File);
            });
        }
    }
}
