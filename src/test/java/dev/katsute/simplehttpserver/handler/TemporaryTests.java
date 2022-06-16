package dev.katsute.simplehttpserver.handler;

import dev.katsute.simplehttpserver.*;
import org.junit.jupiter.api.*;

import java.io.IOException;

final class TemporaryTests {

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
    final void testFirst(){
        server.createContext("temp", new TemporaryHandler((SimpleHttpHandler) e -> e.send(200)));
        Assertions.assertEquals(200, Requests.getCode("http://localhost:8080/temp"));
        Assertions.assertEquals(404, Requests.getCode("http://localhost:8080/temp"));
    }

    @Test
    final void testTime() throws InterruptedException{
        server.createContext("temp", new TemporaryHandler((SimpleHttpHandler) e -> e.send(200), 1000));

        Thread.sleep(1200);

        Assertions.assertEquals(404, Requests.getCode("http://localhost:8080/temp"));
    }

    @Test
    final void testTimeFirst(){
        server.createContext("temp", new TemporaryHandler((SimpleHttpHandler) e -> e.send(200), 1000));
        Assertions.assertEquals(200, Requests.getCode("http://localhost:8080/temp"));
        Assertions.assertEquals(404, Requests.getCode("http://localhost:8080/temp"));
    }

}
