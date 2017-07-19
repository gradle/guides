import org.gradle.api.Project;
import org.gradle.api.provider.PropertyState;
import org.gradle.api.provider.Provider;

public class BinaryRepositoryExtension {
    private final PropertyState<String> serverUrl;

    public BinaryRepositoryExtension(Project project) {
        serverUrl = project.property(String.class);
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