package dev.katsute.simplehttpserver.exchange;

import dev.katsute.simplehttpserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;

final class ExchangeSendTests {

    private static SimpleHttpServer server;

    private static int testCode = HttpURLConnection.HTTP_ACCEPTED;
    private static String testContent = String.valueOf(System.currentTimeMillis());

    @TempDir
    private static File dir = new File(testContent);

    @BeforeAll
    static void beforeAll() throws IOException{
        server = SimpleHttpServer.create(8080);

        server.createContext("code", (SimpleHttpHandler) e -> {
            e.send(testCode);
            e.close();
        });

        server.createContext("bytes", (SimpleHttpHandler) e -> {
            e.send(testContent.getBytes());
            e.close();
        });

        server.createContext("bytes/gzip", (SimpleHttpHandler) e -> {
            e.send(testContent.getBytes(), true);
            e.close();
        });

        server.createContext("string", (SimpleHttpHandler) e -> {
            e.send(testContent);
            e.close();
        });

        server.createContext("string/gzip", (SimpleHttpHandler) e -> {
            e.send(testContent, true);
            e.close();
        });

        final File testFile = new File(dir, testContent);
        Files.write(testFile.toPath(), testContent.getBytes());
        server.createContext("file", (SimpleHttpHandler) e -> {
            e.send(testFile);
            e.close();
        });

        server.createContext("file/gzip", (SimpleHttpHandler) e -> {
            e.send(testFile, true);
            e.close();
        });

        server.start();
    }

    @AfterAll
    static void afterAll(){
        server.stop();
    }

    @Test
    final void testCode(){
        Assertions.assertEquals(testCode, Requests.getCode("http://localhost:8080/code"));
    }

    @Test
    final void testBytes(){
        Assertions.assertEquals(testContent, Requests.getBody("http://localhost:8080/bytes"));
        Assertions.assertEquals(testContent, Requests.getBody("http://localhost:8080/bytes/gzip"));
    }

    @Test
    final void testString(){
        Assertions.assertEquals(testContent, Requests.getBody("http://localhost:8080/string"));
        Assertions.assertEquals(testContent, Requests.getBody("http://localhost:8080/string/gzip"));
    }

    @Test
    final void testFile(){
        Assertions.assertEquals(testContent, Requests.getBody("http://localhost:8080/file"));
        Assertions.assertEquals(testContent, Requests.getBody("http://localhost:8080/file/gzip"));
    }

}
