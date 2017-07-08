import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;

public class DataProcessingPlugin implements Plugin<Project> {
    public void apply(Project project) {
        final Configuration config = project.getConfigurations().create("dataFiles")
            .setVisible(false)
            .setDescription("The data artifacts to be processed for this plugin.");

        config.defaultDependencies(new Action<DependencySet>() {
            public void execute(DependencySet dependencies) {
                dependencies.add(project.getDependencies().create("com.company:data:1.4.6"));
            }
        });

        project.getTasks().withType(DataProcessing.class, new Action<DataProcessing>() {
            public void execute(DataProcessing dataProcessing) {
                dataProcessing.setDataFiles(config);
            }
        });
    }
}