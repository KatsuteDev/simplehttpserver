package dev.katsute.simplehttpserver.handler;

import dev.katsute.simplehttpserver.Requests;
import dev.katsute.simplehttpserver.SimpleHttpServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

final class RedirectTests {

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
    final void testRedirect() throws IOException{
        server.createContext("redirect", new RedirectHandler("https://github.com/KatsuteDev/simplehttpserver"));
        Assertions.assertEquals(308, Requests.getCode("http://localhost:8080/redirect"));
        Assertions.assertEquals("https://github.com/KatsuteDev/simplehttpserver", getRedirectedURL("http://localhost:8080/redirect"));
    }

    private static String getRedirectedURL(final String url) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.getInputStream();
        return conn.getHeaderField("Location") != null ? getRedirectedURL(conn.getHeaderField("Location")) : url;
    }

}
