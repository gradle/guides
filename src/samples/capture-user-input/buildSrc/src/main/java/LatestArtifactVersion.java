import org.gradle.api.DefaultTask;
import org.gradle.api.provider.PropertyState;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class LatestArtifactVersion extends DefaultTask {
    private final PropertyState<String> serverUrl;

    public LatestArtifactVersion() {
        serverUrl = getProject().property(String.class);
    }

    @Input
    public String getServerUrl() {
        return serverUrl.get();
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl.set(serverUrl);
    }

    public void setServerUrl(Provider<String> serverUrl) {
        this.serverUrl.set(serverUrl);
    }

    @TaskAction
    public void resolveLatestVersion() {
        // Access the raw value during the execution phase of the build lifecycle
        System.out.println("Retrieving latest artifact version from URL " + getServerUrl());

        // do additional work
    }
}