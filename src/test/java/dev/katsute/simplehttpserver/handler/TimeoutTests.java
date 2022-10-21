package dev.katsute.simplehttpserver.handler;

import dev.katsute.simplehttpserver.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

final class TimeoutTests {

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
    final void testTimeout(){
        server.createContext("timeout/timeout", new TimeoutHandler(
            (SimpleHttpHandler) exchange -> {
                try{
                    Thread.sleep(4000);
                    exchange.send(200);
                }catch(InterruptedException ignored){}
            },
            2,
            TimeUnit.SECONDS
        ));

        final AtomicInteger code = new AtomicInteger();

        Assertions.assertTimeoutPreemptively(
            Duration.of(3, ChronoUnit.SECONDS),
            () -> code.set(Requests.getCode("http://localhost:8080/timeout/timeout"))
        );

        Assertions.assertEquals(408, code.get());
    }

    @Test
    final void testTimeoutSeconds(){
        server.createContext("timeout/nou", new TimeoutHandler(
            (SimpleHttpHandler) exchange -> {
                try{
                    Thread.sleep(4000);
                    exchange.send(200);
                }catch(InterruptedException ignored){}
            },
            2
        ));

        final AtomicInteger code = new AtomicInteger();

        Assertions.assertTimeoutPreemptively(
            Duration.of(3, ChronoUnit.SECONDS),
            () -> code.set(Requests.getCode("http://localhost:8080/timeout/nou"))
        );

        Assertions.assertEquals(408, code.get());
    }

    @Test
    final void testTimeoutPass(){
        server.createContext("timeout/pass", new TimeoutHandler(
            (SimpleHttpHandler) exchange -> {
                try{
                    Thread.sleep(4000);
                    exchange.send(200);
                }catch(InterruptedException ignored){}
            },
            5,
            TimeUnit.SECONDS
        ));

        final AtomicInteger code = new AtomicInteger();

        Assertions.assertTimeout(
            Duration.of(5, ChronoUnit.SECONDS),
            () -> code.set(Requests.getCode("http://localhost:8080/timeout/pass"))
        );

        Assertions.assertEquals(200, code.get());
    }

}
