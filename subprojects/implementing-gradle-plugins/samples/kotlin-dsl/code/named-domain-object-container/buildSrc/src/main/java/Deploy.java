import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.provider.Property;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class Deploy extends DefaultTask {
    private Property<String> url;

    @Inject
    public Deploy(ObjectFactory objects) {
        this.url = objects.property(String.class);
    }

    @Input
    public Property<String> getUrl() {
        return url;
    }
    
    @TaskAction
    public void deploy() {
        System.out.println("Deploying to URL " + url.get());
    }
}