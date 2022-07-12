package dev.katsute.simplehttpserver.server;

import dev.katsute.simplehttpserver.SimpleHttpServer;
import dev.katsute.simplehttpserver.SimpleHttpsServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

final class ServerCreateTests {

    @Test
    final void testHttpCreate() throws IOException{
        final SimpleHttpServer server = SimpleHttpServer.create();

        assertThrows(IllegalStateException.class, server::start);

        server.bind(8080);
        assertEquals(8080, server.getAddress().getPort());

        assertDoesNotThrow(server::start);
        assertThrows(IllegalStateException.class, server::start);

        assertDoesNotThrow(() -> server.stop());
        assertDoesNotThrow(() -> server.stop(), "Second stop should not throw an exception");
    }

    @Test
    final void testHttpsCreate() throws IOException{
        final SimpleHttpsServer server = SimpleHttpsServer.create();

        assertThrows(IllegalStateException.class, server::start);

        server.bind(8080);
        assertEquals(8080, server.getAddress().getPort());

        assertDoesNotThrow(server::start);
        assertThrows(IllegalStateException.class, server::start);

        assertDoesNotThrow(() -> server.stop());
        assertDoesNotThrow(() -> server.stop(), "Second stop should not throw an exception");
    }

}
