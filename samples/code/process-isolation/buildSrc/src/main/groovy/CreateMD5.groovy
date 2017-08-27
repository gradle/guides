import javax.inject.Inject
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkerExecutor
import org.gradle.workers.IsolationMode

class CreateMD5 extends SourceTask {
    WorkerExecutor workerExecutor

    @OutputDirectory
    File destinationDir

    @Input
    String codecVersion = "1.9"

    @Inject
    public CreateMD5(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor
    }

    @Internal
    Set<File> getCodecClasspath() {
        return project.configurations.detachedConfiguration(
            project.dependencies.create("commons-codec:commons-codec:${codecVersion}")
        ).files
    }

    @TaskAction
    void createHashes() {
        source.files.each { File sourceFile ->
            File md5File = new File(destinationDir, "${sourceFile.name}.md5")
            workerExecutor.submit(GenerateMD5) {
                isolationMode = IsolationMode.PROCESS // <1>
                forkOptions { options ->
                    options.maxHeapSize = "64m" // <2>
                }
                params = [ sourceFile, md5File ]
                classpath getCodecClasspath()
            }
        }
    }
}
