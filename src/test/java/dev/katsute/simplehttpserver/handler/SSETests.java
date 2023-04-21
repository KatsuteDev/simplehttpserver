package dev.katsute.simplehttpserver.handler;

import dev.katsute.simplehttpserver.Requests;
import dev.katsute.simplehttpserver.SimpleHttpServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

final  class SSETests {

    private static SimpleHttpServer server;

    @BeforeAll
    static void beforeAll() throws IOException{
        server = SimpleHttpServer.create(8080);
        server.start();
    }

    @AfterAll
    static void afterAll(){
        server.stop();
    }

    @Test
    final void testSSE() throws InterruptedException{
        final SSEHandler handler = new SSEHandler();
        server.createContext("sse", handler);

        handler.push("event1");

        final AtomicReference<String> data = new AtomicReference<>();

        new Thread(() -> data.set(Requests.getBody("http://localhost:8080/sse", true))).start();

        handler.push("event2");
        handler.push("event3");

        Thread.sleep(6000); // must have long delay

        Assertions.assertEquals("id: 2\ndata: event1\n\nid: 2\ndata: event2\n\nid: 2\ndata: event3", data.get());
    }

}