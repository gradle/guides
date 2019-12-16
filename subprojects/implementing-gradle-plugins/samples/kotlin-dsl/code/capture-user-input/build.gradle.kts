import BinaryRepositoryVersionPlugin
import BinaryRepositoryExtension

apply<BinaryRepositoryVersionPlugin>()

configure<BinaryRepositoryExtension> {
    serverUrl.set("http://my.company.com/maven2")
}
