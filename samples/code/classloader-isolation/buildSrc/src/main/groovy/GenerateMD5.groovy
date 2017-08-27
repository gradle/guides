import javax.inject.Inject
import org.apache.commons.codec.digest.DigestUtils

class GenerateMD5 implements Runnable {
    File sourceFile
    File md5File

    @Inject // <1>
    public GenerateMD5(File sourceFile, File md5File) { // <2>
        this.sourceFile = sourceFile
        this.md5File = md5File
    }

    @Override
    public void run() {
        println "Generating MD5 for ${sourceFile.name}..."
        sourceFile.withInputStream { stream ->
            md5File.text = DigestUtils.md5Hex(stream)
        }
    }
}
