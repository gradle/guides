import java.io.File;
import org.gradle.api.Action;

public class SiteExtension {
    private File outputDir;
    private final CustomData customData = new CustomData();
    
    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }
    
    public File getOutputDir() {
        return outputDir;
    }

    public CustomData getCustomData() {
        return customData;
    }
    
    public void customData(Action<? super CustomData> action) {
        action.execute(customData);
    }
}