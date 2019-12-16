package guide

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import groovy.transform.CompileStatic

@CompileStatic
class Handler implements HttpHandler {
    @Override
    void handle(HttpExchange httpExchange) throws IOException {
        Byte[] response = "Got it!".bytes
        httpExchange.sendResponseHeaders(200, response.size())

        httpExchange.responseBody.withStream { w ->
            w << response
        }
    }
}
