package dev.katsute.simplehttpserver.exchange;

import dev.katsute.simplehttpserver.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

final class ExchangeGetTests {

    private static SimpleHttpServer server;

    private static SimpleHttpExchange exchange;

    @BeforeAll
    static void beforeAll() throws IOException{
        server = SimpleHttpServer.create(8080);

        AtomicReference<SimpleHttpExchange> exchangeRef = new AtomicReference<>();

        server.createContext("exchange", (SimpleHttpHandler) e -> {
            exchangeRef.set(e);
            e.send(200);
            e.close();
        });

        server.start();

        Requests.getBody("http://localhost:8080/exchange?key=value&alt=a%2B%3F%26%7D");
        exchange = exchangeRef.get();

        server.stop();
    }

    @Test
    final void testGET(){
        Assertions.assertEquals("GET", exchange.getRequestMethod().toUpperCase());
        Assertions.assertTrue(exchange.hasGet());
        Assertions.assertEquals("value", exchange.getGetMap().get("key"));
        Assertions.assertEquals("a+?&}", exchange.getGetMap().get("alt"));
    }

}