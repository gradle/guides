import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.War;

public class InhouseConventionWarPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getTasks().withType(War.class).configureEach(new Action<War>() {
            public void execute(War war) {
                war.setWebXml(project.file("src/someWeb.xml"));
            }
        });
    }
}