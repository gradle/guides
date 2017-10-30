import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

public class BinaryRepositoryExtension {
    private final Property<String> serverUrl;

    public BinaryRepositoryExtension(Project project) {
        serverUrl = project.getObjects().property(String.class);
    }

    public String getServerUrl() {
        return serverUrl.get();
    }

    public Provider<String> getServerUrlProvider() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl.set(serverUrl);
    }
}