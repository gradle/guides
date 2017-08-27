import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

class CreateMD5 extends SourceTask { // <1>
    @OutputDirectory
    File destinationDir // <2>

    @TaskAction
    void createHashes() {
        source.files.each { File sourceFile -> // <3>
            sourceFile.withInputStream { InputStream stream ->
                println "Generating MD5 for ${sourceFile.name}..."
                sleep 3000 // <4>
                new File(destinationDir, "${sourceFile.name}.md5").text = DigestUtils.md5Hex(stream) // <5>
            }
        }
    }
}
