package dev.katsute.simplehttpserver.handler;

import dev.katsute.simplehttpserver.*;
import dev.katsute.simplehttpserver.handler.throttler.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.Executors;

final class ThrottlerTests {

    private static SimpleHttpServer server;

    @BeforeAll
    static void beforeAll() throws IOException{
        server = SimpleHttpServer.create(8080);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        server.setSessionHandler(new HttpSessionHandler());
    }

    @AfterAll
    static void afterAll(){
        server.stop();
    }

    private static final SimpleHttpHandler handler = exchange -> {
        try{
            Thread.sleep(500);
        }catch(InterruptedException ex){
            throw new RuntimeException(ex);
        }
        exchange.send(200);
    };

    private static ExchangeThrottler ExchangeThrottler(final int conn){
        return new ExchangeThrottler(){

            @Override
            public final int getMaxConnections(final SimpleHttpExchange exchange){
                return conn;
            }

        };
    }

    @Nested
    final class Exchange {

        @Test
        final void testExchange0(){
            server.createContext("exchange/0", new ThrottledHandler(ThrottlerTests.ExchangeThrottler(0), handler));
            Assertions.assertEquals(429, Requests.getCode("http://localhost:8080/exchange/0"));
        }

        @Test
        final void testExchange1() throws InterruptedException{
            server.createContext("exchange/1", new ThrottledHandler(ThrottlerTests.ExchangeThrottler(1), handler));

            new Thread(() -> Requests.getCode("http://localhost:8080/exchange/1")).start();
            Thread.sleep(250);

            Assertions.assertEquals(429, Requests.getCode("http://localhost:8080/exchange/1"));
        }

        @Test
        final void testExchangeU() throws InterruptedException{
            server.createContext("exchange/u", new ThrottledHandler(ThrottlerTests.ExchangeThrottler(-1), handler));

            new Thread(() -> Requests.getCode("http://localhost:8080/exchange/u")).start();
            Thread.sleep(250);

            Assertions.assertEquals(200, Requests.getCode("http://localhost:8080/exchange/u"));
        }

    }

    private static ServerExchangeThrottler ServerExchangeThrottler(final int conn, final int max, final boolean bypass){
        return new ServerExchangeThrottler(max){

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
    final class ServerExchange {

        @Test
        final void testExchange0(){
            server.createContext("server/exchange/0", new ThrottledHandler(ThrottlerTests.ServerExchangeThrottler(0, -1, false), handler));
            Assertions.assertEquals(429, Requests.getCode("http://localhost:8080/server/exchange/0"));
        }

        @Test
        final void testExchange1() throws InterruptedException{
            server.createContext("server/exchange/1", new ThrottledHandler(ThrottlerTests.ServerExchangeThrottler(1, -1, false), handler));

            new Thread(() -> Requests.getCode("http://localhost:8080/server/exchange/1")).start();
            Thread.sleep(250);

            Assertions.assertEquals(429, Requests.getCode("http://localhost:8080/server/exchange/1"));
        }

        @Test
        final void testExchangeU() throws InterruptedException{
            server.createContext("server/exchange/u", new ThrottledHandler(ThrottlerTests.ServerExchangeThrottler(-1, -1, false), handler));

            new Thread(() -> Requests.getCode("http://localhost:8080/server/exchange/u")).start();
            Thread.sleep(250);

            Assertions.assertEquals(200, Requests.getCode("http://localhost:8080/server/exchange/u"));
        }

        @Test
        final void testServerExchange0(){
            server.createContext("server/exchange-server/0", new ThrottledHandler(ThrottlerTests.ServerExchangeThrottler(-1, 0, false), handler));
            Assertions.assertEquals(429, Requests.getCode("http://localhost:8080/server/exchange-server/0"));
        }

        @Test
        final void testServerExchange1() throws InterruptedException{
            server.createContext("server/exchange-server/1", new ThrottledHandler(ThrottlerTests.ServerExchangeThrottler(-1, 1, false), handler));

            new Thread(() -> Requests.getCode("http://localhost:8080/server/exchange-server/1")).start();
            Thread.sleep(250);

            Assertions.assertEquals(429, Requests.getCode("http://localhost:8080/server/exchange-server/1"));
        }

        @Test
        final void testServerExchangeU() throws InterruptedException{
            server.createContext("server/exchange-server/u", new ThrottledHandler(ThrottlerTests.ServerExchangeThrottler(-1, -1, false), handler));

            new Thread(() -> Requests.getCode("http://localhost:8080/server/exchange-server/u")).start();
            Thread.sleep(250);

            Assertions.assertEquals(200, Requests.getCode("http://localhost:8080/server/exchange-server/u"));
        }

        @Test
        final void testServerExchangeBypass() throws InterruptedException{
            server.createContext("server/exchange-server/bypass", new ThrottledHandler(ThrottlerTests.ServerExchangeThrottler(-1, 0, true), handler));

            new Thread(() -> Requests.getCode("http://localhost:8080/server/exchange-server/bypass")).start();
            Thread.sleep(250);

            Assertions.assertEquals(200, Requests.getCode("http://localhost:8080/server/exchange-server/bypass"));
        }

    }

    private static SessionThrottler SessionThrottler(final HttpSessionHandler handler, final int conn){
        return new SessionThrottler(handler){

            @Override
            public final int getMaxConnections(final HttpSession session, final SimpleHttpExchange exchange){
                return conn;
            }

        };
    }

    @Nested
    final class Session {

        @AfterEach
        final void afterEach() throws InterruptedException{
            Thread.sleep(500);
        }

        @Test
        final void testSession0(){
            server.createContext("session/0", new ThrottledHandler(ThrottlerTests.SessionThrottler(server.getSessionHandler(), 0), handler));
            Assertions.assertEquals(429, Requests.getCode("http://localhost:8080/session/0"));
        }

        @Test
        final void testSession1() throws InterruptedException{
            server.createContext("session/1", new ThrottledHandler(ThrottlerTests.SessionThrottler(server.getSessionHandler(), 1), handler));

            Requests.getCode("http://localhost:8080/session/1"); // assign session first
            Thread.sleep(500);

            new Thread(() -> Requests.getCode("http://localhost:8080/session/1")).start();
            Thread.sleep(250);

            Assertions.assertEquals(429, Requests.getCode("http://localhost:8080/session/1"));
        }

        @Test
        final void testSessionU() throws InterruptedException{
            server.createContext("session/u", new ThrottledHandler(ThrottlerTests.SessionThrottler(server.getSessionHandler(), -1), handler));

            Requests.getCode("http://localhost:8080/session/u"); // assign session first
            Thread.sleep(500);

            new Thread(() -> Requests.getCode("http://localhost:8080/session/u")).start();
            Thread.sleep(250);

            Assertions.assertEquals(200, Requests.getCode("http://localhost:8080/session/u"));
        }

    }

    private static ServerSessionThrottler ServerSessionThrottler(final HttpSessionHandler handler, final int conn, final int max, final boolean bypass){
        return new ServerSessionThrottler(handler, max){

            @Override
            public final int getMaxConnections(final HttpSession session, final SimpleHttpExchange exchange){
                return conn;
            }

            @Override
            public final boolean canIgnoreConnectionLimit(final HttpSession session, final SimpleHttpExchange exchange){
                return bypass;
            }

        };
    }

    @Nested
    final class ServerSession {

        @AfterEach
        final void afterEach() throws InterruptedException{
            Thread.sleep(500);
        }

        @Test
        final void testSession0(){
            server.createContext("server/session/0", new ThrottledHandler(ThrottlerTests.ServerSessionThrottler(server.getSessionHandler(),0, -1, false), handler));
            Assertions.assertEquals(429, Requests.getCode("http://localhost:8080/server/session/0"));
        }

        @Test
        final void testSession1() throws InterruptedException{
            server.createContext("server/session/1", new ThrottledHandler(ThrottlerTests.ServerSessionThrottler(server.getSessionHandler(),1, -1, false), handler));

            Requests.getCode("http://localhost:8080/server/session/1"); // assign session first
            Thread.sleep(500);

            new Thread(() -> Requests.getCode("http://localhost:8080/server/session/1")).start();
            Thread.sleep(250);

            Assertions.assertEquals(429, Requests.getCode("http://localhost:8080/server/session/1"));
        }

        @Test
        final void testSessionU() throws InterruptedException{
            server.createContext("server/session/u", new ThrottledHandler(ThrottlerTests.ServerSessionThrottler(server.getSessionHandler(),-1, -1, false), handler));

            Requests.getCode("http://localhost:8080/server/session/u"); // assign session first
            Thread.sleep(500);

            new Thread(() -> Requests.getCode("http://localhost:8080/server/session/u")).start();
            Thread.sleep(250);

            Assertions.assertEquals(200, Requests.getCode("http://localhost:8080/server/session/u"));
        }

        @Test
        final void testServerSession0(){
            server.createContext("server/session-server/0", new ThrottledHandler(ThrottlerTests.ServerSessionThrottler(server.getSessionHandler(),-1, 0, false), handler));
            Assertions.assertEquals(429, Requests.getCode("http://localhost:8080/server/session-server/0"));
        }

        @Test
        final void testServerSession1() throws InterruptedException{
            server.createContext("server/session-server/1", new ThrottledHandler(ThrottlerTests.ServerSessionThrottler(server.getSessionHandler(),-1, 1, false), handler));

            Requests.getCode("http://localhost:8080/server/session-server/1"); // assign session first
            Thread.sleep(500);

            new Thread(() -> Requests.getCode("http://localhost:8080/server/session-server/1")).start();
            Thread.sleep(250);

            Assertions.assertEquals(429, Requests.getCode("http://localhost:8080/server/session-server/1"));
        }

        @Test
        final void testServerSessionU() throws InterruptedException{
            server.createContext("server/session-server/u", new ThrottledHandler(ThrottlerTests.ServerSessionThrottler(server.getSessionHandler(),-1, -1, false), handler));

            Requests.getCode("http://localhost:8080/server/session-server/u"); // assign session first
            Thread.sleep(500);

            new Thread(() -> Requests.getCode("http://localhost:8080/server/session-server/u")).start();
            Thread.sleep(250);

            Assertions.assertEquals(200, Requests.getCode("http://localhost:8080/server/session-server/u"));
        }

        @Test
        final void testServerSessionBypass() throws InterruptedException{
            server.createContext("server/session-server/bypass", new ThrottledHandler(ThrottlerTests.ServerSessionThrottler(server.getSessionHandler(),-1, 0, true), handler));

            Requests.getCode("http://localhost:8080/server/session-server/bypass"); // assign session first
            Thread.sleep(500);

            new Thread(() -> Requests.getCode("http://localhost:8080/server/session-server/bypass")).start();
            Thread.sleep(250);

            Assertions.assertEquals(200, Requests.getCode("http://localhost:8080/server/session-server/bypass"));
        }

    }

}