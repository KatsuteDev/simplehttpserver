module SimpleHttpServer {

    requires jdk.httpserver;
    requires java.net.http;

    exports dev.katsute.simplehttpserver.handler.file;
    exports dev.katsute.simplehttpserver.handler.throttler;
    exports dev.katsute.simplehttpserver.handler;
    exports dev.katsute.simplehttpserver;

}