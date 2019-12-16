import ServerEnvironmentPlugin
import ServerEnvironment

apply<ServerEnvironmentPlugin>()

configure<NamedDomainObjectContainer<ServerEnvironment>> {
    create("dev") {
        url.set("http://localhost:8080")
    }

    create("staging") {
        url.set("http://staging.enterprise.com")
    }

    create("production") {
        url.set("http://prod.enterprise.com")
    }
}
