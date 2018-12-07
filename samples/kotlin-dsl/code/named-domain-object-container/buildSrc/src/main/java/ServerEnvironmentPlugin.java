import org.gradle.api.*;

public class ServerEnvironmentPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        NamedDomainObjectContainer<ServerEnvironment> serverEnvironmentContainer = project.container(ServerEnvironment.class);
        project.getExtensions().add("environments", serverEnvironmentContainer);

        serverEnvironmentContainer.all(new Action<ServerEnvironment>() {
            public void execute(ServerEnvironment serverEnvironment) {
                String env = serverEnvironment.getName();
                String capitalizedServerEnv = env.substring(0, 1).toUpperCase() + env.substring(1);
                String taskName = "deployTo" + capitalizedServerEnv;
                project.getTasks().register(taskName, Deploy.class, new Action<Deploy>() {
                    public void execute(Deploy task) {
                        task.setUrl(serverEnvironment.getUrl());
                    }
                });
            }
        });
    }
}