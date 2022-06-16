package dev.katsute.simplehttpserver.handler;

import dev.katsute.simplehttpserver.*;
import org.junit.jupiter.api.*;

import java.io.IOException;

final class PredicateTests {

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
    final void testPredicate(){
        server.createContext("predicate/true", new PredicateHandler(
            exchange -> true,
            (SimpleHttpHandler) exchange -> exchange.send("true"),
            (SimpleHttpHandler) exchange -> exchange.send("false"))
        );

        Assertions.assertEquals("true", Requests.getBody("http://localhost:8080/predicate/true"));

        server.createContext("predicate/false", new PredicateHandler(
            exchange -> false,
            (SimpleHttpHandler) exchange -> exchange.send("true"),
            (SimpleHttpHandler) exchange -> exchange.send("false"))
        );

        Assertions.assertEquals("false", Requests.getBody("http://localhost:8080/predicate/false"));
    }

    @Test
    final void testRoot(){
        server.createContext("/", new RootHandler(
            (SimpleHttpHandler) exchange -> exchange.send("root"),
            (SimpleHttpHandler) exchange -> exchange.send("else")
        ));

        Assertions.assertEquals("root", Requests.getBody("http://localhost:8080/"));
        Assertions.assertEquals("else", Requests.getBody("http://localhost:8080/else"));
    }

}
