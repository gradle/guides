import org.gradle.workers.WorkParameters;
import org.gradle.api.provider.Property;

import java.io.File;

public interface MD5WorkParameters extends WorkParameters {
    Property<File> getSourceFile(); // <1>
    Property<File> getMD5File();
}