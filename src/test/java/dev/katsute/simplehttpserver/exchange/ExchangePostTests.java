package dev.katsute.simplehttpserver.exchange;

import dev.katsute.simplehttpserver.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

final class ExchangePostTests {

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

        {
            final HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8080/exchange").openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            try(final OutputStream OS = conn.getOutputStream()){
                final byte[] bytes = "key=value&alt=a%2B%3F%26%7D".getBytes(StandardCharsets.UTF_8);
                OS.write(bytes, 0, bytes.length);
            }

            conn.getOutputStream().flush();
            conn.getOutputStream().close();

            Requests.getBody(conn);
        }

        exchange = exchangeRef.get();

        server.stop();
    }

    @Test
    final void testPOST(){
        Assertions.assertEquals("POST", exchange.getRequestMethod().toUpperCase());
        Assertions.assertTrue(exchange.hasPost());
        Assertions.assertEquals("value", exchange.getPostMap().get("key"));
        Assertions.assertEquals("a+?&}", exchange.getPostMap().get("alt"));

        Assertions.assertNull(exchange.getMultipartFormData());
    }

}
