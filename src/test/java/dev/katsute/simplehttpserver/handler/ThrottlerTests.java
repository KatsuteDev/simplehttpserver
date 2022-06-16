package dev.katsute.simplehttpserver.handler;

import dev.katsute.simplehttpserver.*;
import dev.katsute.simplehttpserver.handler.throttler.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.Executors;

final class ThrottlerTests {

    private static SimpleHttpServer server;

    @BeforeAll
    static void beforeAll() throws IOException{
        server = SimpleHttpServer.create(8080);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    @AfterAll
    static void afterAll(){
        server.stop();
    }

    private static SimpleHttpHandler handler = exchange -> {
        try{
            Thread.sleep(1000);
        }catch(InterruptedException ex){
            throw new RuntimeException(ex);
        }
        exchange.send(200);
    };

    private static ExchangeThrottler ExchangeThrottle(final int conn){
        return new ExchangeThrottler(){

            @Override
            public final int getMaxConnections(final SimpleHttpExchange exchange){
                return conn;
            }

        };
    }

    private static ServerExchangeThrottler ServerExchangeThrottle(final int conn, final boolean bypass){
        return new ServerExchangeThrottler(){

            @Override
            public final int getMaxConnections(final SimpleHttpExchange exchange){
                return conn;
            }

            @Override
            public final boolean canIgnoreConnectionLimit(final SimpleHttpExchange exchange){
                return bypass;
            }

        };
    }

    @Nested
    final class Exchange {

        @Test
        final void testExchange0(){
            server.createContext("exchange-0", new ThrottledHandler(
                new ExchangeThrottler() {

                    @Override
                    public final int getMaxConnections(final SimpleHttpExchange exchange){
                        return 0;
                    }

                },
                handler
            ));

            Assertions.assertThrows(UncheckedIOException.class, () -> Requests.getCode("http://localhost:8080/exchange-0"));
        }

        @Test
        final void testExchange1() throws InterruptedException{
            server.createContext("exchange-1", new ThrottledHandler(
                new ExchangeThrottler() {

                    @Override
                    public final int getMaxConnections(final SimpleHttpExchange exchange){
                        return 1;
                    }

                },
                handler
            ));

            new Thread(() -> Requests.getCode("http://localhost:8080/exchange-1")).start();

            Thread.sleep(500);

            Assertions.assertThrows(UncheckedIOException.class, () -> Requests.getCode("http://localhost:8080/exchange-1"));
        }

        @Test
        final void testExchangeU() throws InterruptedException{
            server.createContext("exchange-U", new ThrottledHandler(
                new ExchangeThrottler() {

                    @Override
                    public final int getMaxConnections(final SimpleHttpExchange exchange){
                        return -1;
                    }

                },
                handler
            ));

            new Thread(() -> Requests.getCode("http://localhost:8080/exchange-U")).start();

            Thread.sleep(500);

            Assertions.assertEquals(200, Requests.getCode("http://localhost:8080/exchange-U"));
        }

    }

    @Nested
    final class ServerExchange {

        @Test
        final void testServerExchange0(){
            server.createContext("server/exchange-0", new ThrottledHandler(
                new ServerExchangeThrottler() {

                    @Override
                    public final int getMaxConnections(final SimpleHttpExchange exchange){
                        return 0;
                    }

                },
                handler
            ));

            Assertions.assertThrows(UncheckedIOException.class, () -> Requests.getCode("http://localhost:8080/server/exchange-0"));
        }

        @Test
        final void testServerExchange1() throws InterruptedException{
            server.createContext("server/exchange-1", new ThrottledHandler(
                new ServerExchangeThrottler() {

                    @Override
                    public final int getMaxConnections(final SimpleHttpExchange exchange){
                        return 1;
                    }

                },
                handler
            ));

            new Thread(() -> Requests.getCode("http://localhost:8080/server/exchange-1")).start();

            Thread.sleep(500);

            Assertions.assertThrows(UncheckedIOException.class, () -> Requests.getCode("http://localhost:8080/server/exchange-1"));
        }

        @Test
        final void testServerExchangeU() throws InterruptedException{
            server.createContext("server/exchange-U", new ThrottledHandler(
                new ServerExchangeThrottler() {

                    @Override
                    public final int getMaxConnections(final SimpleHttpExchange exchange){
                        return -1;
                    }

                },
                handler
            ));

            new Thread(() -> Requests.getCode("http://localhost:8080/server/exchange-U")).start();

            Thread.sleep(500);

            Assertions.assertEquals(200, Requests.getCode("http://localhost:8080/server/exchange-U"));
        }

    }

}
