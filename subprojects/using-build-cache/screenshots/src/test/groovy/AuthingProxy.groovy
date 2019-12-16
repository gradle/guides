import com.google.common.base.Throwables
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.exec.Promise
import ratpack.groovy.test.embed.GroovyEmbeddedApp
import ratpack.http.TypedData
import ratpack.http.client.HttpClient
import ratpack.test.CloseableApplicationUnderTest

final class AuthingProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthingProxy)

    private AuthingProxy() {
    }

    static CloseableApplicationUnderTest to(String host, String username, String password) {
        GroovyEmbeddedApp.of {
            registryOf {
                it.add(HttpClient, HttpClient.of { it.poolSize(32).maxContentLength(Integer.MAX_VALUE) })
            }
            handlers {
                all { HttpClient httpClient ->
                    boolean needsBody = context.request.method != 'GET' && context.request.contentLength != -1
                    Promise<TypedData> getBody = needsBody ? context.request.body : Promise.ofNull()
                    getBody.then { body ->
                        def targetUri = new URI("https://" + host + "/" + request.rawUri.replaceFirst("/", ""))
                        httpClient.requestStream(targetUri) {
                            it.headers.copy(request.headers)
                            it.headers.set("Host", host)
                            it.basicAuth(username, password)
                            it.redirects(5)
                            if (needsBody) {
                                it.method(context.request.method)
                                it.body { b ->
                                    b.bytes(body.bytes)
                                }
                            }
                        } onError {
                            LOGGER.error("Error proxying $targetUri:", it)
                            response.status(502).send("$it: \n${Throwables.getStackTraceAsString(it)}")
                        } then {
                            if (!(it.status.is2xx() || it.status.is3xx())) {
                                LOGGER.info("AuthingProxy: Got $it.status.code when proxying $context.request.method $targetUri")
                            }
                            it.forwardTo(response)
                        }
                    }
                }
            }
        }
    }

}
