import ServerEnvironmentPlugin
import ServerEnvironments

apply<ServerEnvironmentPlugin>()

configure<ServerEnvironments> {
    create("dev") {
        url = 'http://localhost:8080'
    }

    create("staging") {
        url = 'http://staging.enterprise.com'
    }

    create("production") {
        url = 'http://prod.enterprise.com'
    }
}
