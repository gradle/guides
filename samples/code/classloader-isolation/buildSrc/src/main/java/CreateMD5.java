import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;
import org.gradle.workers.*;

import javax.inject.Inject;
import java.io.File;
import java.util.Set;

class CreateMD5 extends SourceTask {
    private final WorkerExecutor workerExecutor;

    @OutputDirectory
    File destinationDir;

    @InputFiles
    FileCollection codecClasspath; // <1>

    @Inject
    public CreateMD5(WorkerExecutor workerExecutor) {
        super();
        this.workerExecutor = workerExecutor;
    }

    @TaskAction
    public void createHashes() {
        for (File sourceFile : getSource().getFiles()) {
            File md5File = new File(destinationDir, sourceFile.getName() + ".md5");
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
