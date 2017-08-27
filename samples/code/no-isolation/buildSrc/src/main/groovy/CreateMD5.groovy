import javax.inject.Inject
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import org.gradle.workers.IsolationMode

class CreateMD5 extends SourceTask {
    WorkerExecutor workerExecutor // <1>

    @OutputDirectory
    File destinationDir

    @Inject // <2>
    public CreateMD5(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor
    }

    @TaskAction
    void createHashes() {
        source.files.each { File sourceFile ->
            File md5File = new File(destinationDir, "${sourceFile.name}.md5")
            workerExecutor.submit(GenerateMD5) { // <3>
                isolationMode = IsolationMode.NONE // <4>
                params = [ sourceFile, md5File ] // <5>
            }
        }
    }
}
