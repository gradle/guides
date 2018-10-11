import ServerEnvironmentPlugin
import ServerEnvironment

apply<ServerEnvironmentPlugin>()

extensions.configure("environments") {
    (this as NamedDomainObjectContainer<ServerEnvironment>).let {
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

}
