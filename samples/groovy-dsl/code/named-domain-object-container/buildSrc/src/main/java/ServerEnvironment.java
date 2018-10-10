public class ServerEnvironment {
    private final String name;
    private String url;

    public ServerEnvironment(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }
}