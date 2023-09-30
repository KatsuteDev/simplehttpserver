package dev.katsute.simplehttpserver.server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import dev.katsute.simplehttpserver.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

final class ServerTests {

    @Test
    final void testReference() throws IOException{
        assertNotNull(SimpleHttpServer.create().getHttpServer());
    }

    @Test
    final void testProp() throws IOException{
        final SimpleHttpServer server = SimpleHttpServer.create();

        assertNull(server.getExecutor());
        assertNull(server.getSessionHandler());
        assertTrue(server.getContexts().isEmpty());
    }

    //

    @Test
    final void testRandomContext() throws IOException{
        final SimpleHttpServer server = SimpleHttpServer.create();

        final String test = server.getRandomContext();
        assertNotNull(test);
        server.createContext(test);
        for(int i = 0; i < 100; i++)
            assertNotEquals(test, server.getRandomContext());
    }

    @Test
    final void testRandomContextHead() throws IOException{
        final SimpleHttpServer server = SimpleHttpServer.create();

        final String test = server.getRandomContext("/head");
        assertNotNull(test);
        assertTrue(test.startsWith("/head/"));
        server.createContext(test);
        for(int i = 0; i < 100; i++)
            assertNotEquals(test, server.getRandomContext("/head"));
    }

    @Test
    final void testRemoveNullContext() throws IOException{
        final SimpleHttpServer server = SimpleHttpServer.create();

        assertThrows(NullPointerException.class, () -> server.removeContext((String) null));
        assertThrows(NullPointerException.class, () -> server.removeContext((HttpContext) null));
        assertThrows(IllegalArgumentException.class, () -> server.removeContext(""));
    }

    @Test
    final void testRemoveContext() throws IOException{
        final SimpleHttpServer server = SimpleHttpServer.create();

        server.createContext("");
        assertDoesNotThrow(() -> server.removeContext(""));
        assertDoesNotThrow(() -> server.removeContext(server.createContext("")));
    }

    @Test
    final void testRemoveNativeContext() throws IOException{
        final SimpleHttpServer server = SimpleHttpServer.create();

        server.getHttpServer().createContext("/");
        assertDoesNotThrow(() -> server.removeContext(""));

        assertDoesNotThrow(() -> server.removeContext(server.getHttpServer().createContext("/")));

        server.getHttpServer().removeContext(server.createContext("/"));
        assertDoesNotThrow(() -> server.createContext("/"));
    }

    @Test
    final void testCreateContext() throws IOException{
        final SimpleHttpServer server = SimpleHttpServer.create();

        server.createContext("");
        assertEquals(1, server.getContexts().size());

        final SimpleHttpHandler handler = SimpleHttpExchange::close;

        assertSame(handler, server.getContextHandler(server.createContext("close", handler)));
        assertEquals(2, server.getContexts().size());
    }

    @Test
    final void testCreateSlashContext() throws IOException{
        final SimpleHttpServer server = SimpleHttpServer.create();

        assertEquals("/", server.createContext("/").getPath());
        server.removeContext("/");
        assertEquals("/", server.createContext("\\").getPath());
        server.removeContext("/");
        assertEquals("/", server.createContext("").getPath());
        server.removeContext("/");
    }

    @Test @EnabledOnJre(value={JRE.JAVA_8})
    final void testDuplicateContext8() throws IOException{
        final SimpleHttpServer server = SimpleHttpServer.create();

        server.createContext("");

        assertDoesNotThrow(() -> server.createContext("", HttpExchange::close));
    }

    @Test @DisabledOnJre(value={JRE.JAVA_8})
    final void testDuplicateContext18() throws IOException{
        final SimpleHttpServer server = SimpleHttpServer.create();

        server.createContext("");

        assertThrows(IllegalArgumentException.class, () -> server.createContext("", HttpExchange::close));
    }

}