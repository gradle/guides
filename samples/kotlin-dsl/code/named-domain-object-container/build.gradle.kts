import ServerEnvironmentPlugin
import ServerEnvironment

apply<ServerEnvironmentPlugin>()

configure<NamedDomainObjectContainer<ServerEnvironment>> {
    create("dev") {
        url = "http://localhost:8080"
    }

    create("staging") {
        url = "http://staging.enterprise.com"
    }

    create("production") {
        url = "http://prod.enterprise.com"
    }
}
