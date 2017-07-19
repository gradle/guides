import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BinaryRepositoryVersionPlugin implements Plugin<Project> {
    public void apply(Project project) {
        BinaryRepositoryExtension extension = project.getExtensions().create("binaryRepo", BinaryRepositoryExtension.class, project);

        project.getTasks().create("latestArtifactVersion", LatestArtifactVersion.class, new Action<LatestArtifactVersion>() {
            public void execute(LatestArtifactVersion latestArtifactVersion) {
                latestArtifactVersion.setServerUrl(extension.getServerUrlProvider());
            }
        });
    }
}