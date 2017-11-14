import org.gradle.api.Action;
import org.gradle.api.tasks.*;
import org.gradle.workers.*;

import javax.inject.Inject;
import java.io.File;

class CreateMD5 extends SourceTask {
    private final WorkerExecutor workerExecutor; // <1>

    @OutputDirectory
    File destinationDir;

    @Inject
    public CreateMD5(WorkerExecutor workerExecutor) { // <2>
        super();
        this.workerExecutor = workerExecutor;
    }

    @TaskAction
    public void createHashes() {
        for (File sourceFile : getSource().getFiles()) {
            File md5File = new File(destinationDir, sourceFile.getName() + ".md5");
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
