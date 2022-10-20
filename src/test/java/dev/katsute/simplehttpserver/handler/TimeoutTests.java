package dev.katsute.simplehttpserver.handler;

import dev.katsute.simplehttpserver.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

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
                }catch(InterruptedException ignored){

                }
            },
            2,
            TimeUnit.SECONDS
        ));

        Assertions.assertTimeoutPreemptively(
            Duration.of((long) 2.1, TimeUnit.SECONDS.toChronoUnit()),
            () ->Requests.getBody("http://localhost:8080/timeout/timeout")
        );
    }

    @Test
    final void testTimeoutSeconds(){
        server.createContext("timeout/unit", new TimeoutHandler(
            (SimpleHttpHandler) exchange -> {
                try{
                    Thread.sleep(4000);
                }catch(final InterruptedException ignored){

                }
            },
            2
        ));

        Assertions.assertTimeoutPreemptively(
            Duration.of((long) 2.1, TimeUnit.SECONDS.toChronoUnit()),
            () ->Requests.getBody("http://localhost:8080/timeout/unit")
        );
    }

    @Test
    final void testTimeoutPass(){
        server.createContext("timeout/pass", new TimeoutHandler(
            (SimpleHttpHandler) exchange -> {
                try{
                    Thread.sleep(4000);
                }catch(InterruptedException ignored){

                }
            },
            5,
            TimeUnit.SECONDS
        ));

        Assertions.assertTimeout(
            Duration.of((long) 4.1, TimeUnit.SECONDS.toChronoUnit()),
            () ->Requests.getBody("http://localhost:8080/timeout/pass")
        );
    }

}
