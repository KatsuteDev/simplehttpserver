package dev.katsute.simplehttpserver.server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import dev.katsute.simplehttpserver.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.BindException;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
class ServerTests {

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

    @Nested
    final class BindTests {

        @Test
        final void testBind() throws IOException{
            final SimpleHttpServer server = SimpleHttpServer.create();
            assertNull(server.getAddress());
            assertDoesNotThrow(() -> server.bind(8080));
            assertTrue(server.getAddress().getAddress().isAnyLocalAddress());
        }

        @Test
        final void testOccupied() throws IOException{
            final SimpleHttpServer server = SimpleHttpServer.create(8080);
            server.start();

            Assertions.assertThrows(BindException.class, () -> SimpleHttpServer.create(8080));

            server.stop();

            Assertions.assertDoesNotThrow(() -> SimpleHttpServer.create(8080));
        }

    }

    @Nested
    final class CreateTests {

        @SuppressWarnings("SpellCheckingInspection")
        @Test
        final void testHttpCreate() throws IOException{
            final SimpleHttpServer server = SimpleHttpServer.create();

            assertThrows(IllegalStateException.class, server::start, "Unbinded server should throw an excaption");

            server.bind(8080);
            assertEquals(8080, server.getAddress().getPort());

            assertDoesNotThrow(server::start);
            assertThrows(IllegalStateException.class, server::start);

            assertDoesNotThrow(() -> server.stop());
            assertDoesNotThrow(() -> server.stop(), "Second stop should not throw an exception");
        }

        @SuppressWarnings("SpellCheckingInspection")
        @Test
        final void testHttpsCreate() throws IOException{
            final SimpleHttpsServer server = SimpleHttpsServer.create();

            assertThrows(IllegalStateException.class, server::start, "Unbinded server should throw an excaption");

            server.bind(8080);
            assertEquals(8080, server.getAddress().getPort());

            assertDoesNotThrow(server::start);
            assertThrows(IllegalStateException.class, server::start);

            assertDoesNotThrow(() -> server.stop());
            assertDoesNotThrow(() -> server.stop(), "Second stop should not throw an exception");
        }

    }

    @Nested
    final class ContextTests {

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

            assertSame(handler, server.getContextHandler(server.createContext("", handler)));
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

        @Test
        final void testDuplicateContext() throws IOException{
            final SimpleHttpServer server = SimpleHttpServer.create();

            server.createContext("");
            server.createContext("", HttpExchange::close); // supposed to throw an exception, docs are invalid
        }

    }

}
