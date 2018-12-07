import org.gradle.api.provider.Property;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class ServerEnvironment {
    private final String name;
    private Property<String> url;

    public ServerEnvironment(String name, ObjectFactory objectFactory) {
        this.name = name;
        this.url = objectFactory.property(String.class);
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    public String getName() {
        return name;
    }
    
    public Property<String> getUrl() {
        return url;
    }
}