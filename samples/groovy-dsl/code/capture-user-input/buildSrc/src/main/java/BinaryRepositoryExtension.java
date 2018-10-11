import org.gradle.api.Project;
import org.gradle.api.provider.Property;

public class BinaryRepositoryExtension {
    private final Property<String> serverUrl;

    public BinaryRepositoryExtension(Project project) {
        serverUrl = project.getObjects().property(String.class);
    }

    public Property<String> getServerUrl() {
        return serverUrl;
    }
}
