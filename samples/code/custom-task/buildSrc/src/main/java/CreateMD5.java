import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

class CreateMD5 extends SourceTask { // <1>
    @OutputDirectory
    File destinationDir; // <2>

    @Inject
    public CreateMD5() {
        super();
    }

    @TaskAction
    public void createHashes() {
        for (File sourceFile : getSource().getFiles()) { // <3>
            try {
                InputStream stream = new FileInputStream(sourceFile);
                System.out.println("Generating MD5 for " + sourceFile.getName() + "...");
                Thread.sleep(3000); // <4>
                File md5File = new File(destinationDir, sourceFile.getName() + ".md5"); // <5>
                FileUtils.writeStringToFile(md5File, DigestUtils.md5Hex(stream), (String) null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
