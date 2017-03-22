package guide;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SimpleHttpServer {

    public SimpleHttpServer(int port,final String path ) throws IOException {
        this.port = port;
        this.path = path;
        httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1",port),0);
        httpServer.createContext( this.path , new Handler() );
        httpServer.setExecutor(null);
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }

    public void close() {
        stop();
    }

    public int getPort() {
        return this.port;
    }

    private int port = 0;
    private HttpServer httpServer;
    private final String path;
}

