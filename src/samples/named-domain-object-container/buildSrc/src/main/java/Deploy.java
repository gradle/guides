import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class Deploy extends DefaultTask {
    private String url;

    public void setUrl(String url) {
        this.url = url;
    }
    
    @Input
    public String getUrl() {
        return url;
    }
    
    @TaskAction
    public void deploy() {
        System.out.println("Deploying to URL " + url);
    }
}