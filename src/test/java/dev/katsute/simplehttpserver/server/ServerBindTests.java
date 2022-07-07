package dev.katsute.simplehttpserver.server;

import dev.katsute.simplehttpserver.SimpleHttpServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.BindException;

import static org.junit.jupiter.api.Assertions.*;

final class ServerBindTests {

    @Test
    final void testBind() throws IOException{
        final SimpleHttpServer server = SimpleHttpServer.create();
        assertNull(server.getAddress());
        assertDoesNotThrow(() -> server.bind(8080));
        assertTrue(server.getAddress().getAddress().isAnyLocalAddress());

        // required to unbind
        server.start();
        server.stop();
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
