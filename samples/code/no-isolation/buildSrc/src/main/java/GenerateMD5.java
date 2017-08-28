import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class GenerateMD5 implements Runnable {
    private final File sourceFile;
    private final File md5File;

    @Inject // <1>
    public GenerateMD5(File sourceFile, File md5File) { // <2>
        this.sourceFile = sourceFile;
        this.md5File = md5File;
    }

    @Override
    public void run() {
        try {
            InputStream stream = new FileInputStream(sourceFile);
            System.out.println("Generating MD5 for " + sourceFile.getName() + "...");
            Thread.sleep(3000);
            FileUtils.writeStringToFile(md5File, DigestUtils.md5Hex(stream), (String) null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
