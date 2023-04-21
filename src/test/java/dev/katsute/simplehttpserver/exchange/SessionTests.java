package dev.katsute.simplehttpserver.exchange;

import dev.katsute.simplehttpserver.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

final class SessionTests {

    private static SimpleHttpServer server;
    private static final HttpSessionHandler sh = new HttpSessionHandler();

    private static SimpleHttpExchange exchange;

    @BeforeAll
    static void beforeAll() throws IOException{
        server = SimpleHttpServer.create(8080);
        server.setSessionHandler(sh);

        AtomicReference<SimpleHttpExchange> exchangeRef = new AtomicReference<>();
        AtomicReference<String> responseRef = new AtomicReference<>();

        server.createContext("session", (SimpleHttpHandler) e -> {
            exchangeRef.set(e);
            responseRef.set(e.toString());
            e.send(responseRef.get());
        });

        server.start();

        final String res = Requests.getBody("http://localhost:8080/session");

        Assertions.assertEquals(responseRef.get(), res);

        server.stop();

        exchange = exchangeRef.get();
    }

    @Test
    final void testSession() throws InterruptedException{
        final HttpSession session = sh.getSession(exchange);
        Assertions.assertNotNull(session);
        Assertions.assertEquals("__session-id=\"" + session.getSessionID() + '"', exchange.getResponseHeaders().getFirst("Set-Cookie"));
        Assertions.assertEquals(session.getSessionID(), Requests.Cookies.getCookieStore().get(URI.create("http://localhost:8080/session")).get(0).getValue());

        long was;
        Assertions.assertTrue(session.getCreationTime() < System.currentTimeMillis());
        Assertions.assertTrue((was = session.getLastAccessed()) < System.currentTimeMillis());
        session.update();

        Thread.sleep(100);

        Assertions.assertTrue(was != session.getLastAccessed());
        Assertions.assertTrue(session.getLastAccessed() < System.currentTimeMillis());
    }

}