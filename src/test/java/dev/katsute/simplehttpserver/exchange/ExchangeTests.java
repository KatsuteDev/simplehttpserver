package dev.katsute.simplehttpserver.exchange;

import com.sun.net.httpserver.HttpContext;
import dev.katsute.simplehttpserver.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

final class ExchangeTests {

    private static SimpleHttpServer server;

    private static SimpleHttpExchange exchange;
    private static HttpContext context;

    @BeforeAll
    static void beforeAll() throws IOException{
        server = SimpleHttpServer.create(8080);

        AtomicReference<SimpleHttpExchange> exchangeRef = new AtomicReference<>();
        AtomicReference<String> responseRef = new AtomicReference<>();

        context = server.createContext("exchange", (SimpleHttpHandler) e -> {
            exchangeRef.set(e);
            responseRef.set(e.toString());
            e.send(responseRef.get());
        });

        server.start();

        final String res = Requests.getBody("http://localhost:8080/exchange");

        Assertions.assertEquals(responseRef.get(), res);

        server.stop();

        exchange = exchangeRef.get();
    }

    @Test
    final void testReference(){
        Assertions.assertNotNull(exchange);
        Assertions.assertSame(server.getHttpServer(), exchange.getHttpServer());
        Assertions.assertNotNull(exchange.getHttpExchange());
    }

    @Test
    final void testLocation(){
        Assertions.assertEquals("/exchange", exchange.getRequestURI().getPath());

        Assertions.assertNotNull(exchange.getLocalAddress());
        Assertions.assertNotNull(exchange.getRemoteAddress());

        Assertions.assertSame(context, exchange.getHttpContext());

        Assertions.assertEquals("HTTP/1.1", exchange.getProtocol());
    }

    @Test
    final void testGetPost(){
        Assertions.assertFalse(exchange.hasGet());
        Assertions.assertNull(exchange.getRawGet());
        Assertions.assertTrue(exchange.getGetMap().isEmpty());

        Assertions.assertFalse(exchange.hasPost());
        Assertions.assertNull(exchange.getRawPost());
        Assertions.assertNull(exchange.getMultipartFormData());
        Assertions.assertTrue(exchange.getPostMap().isEmpty());
    }

    @Test
    final void testResponse(){
        Assertions.assertEquals(200, exchange.getResponseCode());
        Assertions.assertTrue(exchange.getCookies().isEmpty());
    }

}
