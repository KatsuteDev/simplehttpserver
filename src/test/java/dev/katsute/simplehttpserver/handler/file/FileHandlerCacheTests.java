package dev.katsute.simplehttpserver.handler.file;

import dev.katsute.simplehttpserver.Requests;
import dev.katsute.simplehttpserver.SimpleHttpServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
final class FileHandlerCacheTests {

    private static SimpleHttpServer server;

    private static final String testContent = String.valueOf(System.currentTimeMillis());

    @TempDir
    private static File dir = new File(testContent);

    private static final FileHandler handler = new FileHandler();

    @BeforeAll
    static void beforeAll() throws IOException{
        server = SimpleHttpServer.create(8080);

        final File cache = new File(dir, testContent);
        Files.write(cache.toPath(), testContent.getBytes(StandardCharsets.UTF_8));

        final File cache2 = new File(dir, testContent + '0');
        Files.write(cache2.toPath(), (testContent + '0').getBytes(StandardCharsets.UTF_8));

        final FileOptions opts = new FileOptions.Builder()
            .setLoadingOption(FileOptions.FileLoadingOption.CACHE)
            .setCache(2000)
            .build();

        handler.addFile(cache, opts);
        handler.addFile(cache2, opts);

        server.createContext("file", handler);

        server.start();
    }

    @AfterAll
    static void afterAll(){
        server.stop();
    }

    @Test @Order(0)
    final void testAccess(){
        Assertions.assertEquals(testContent, Requests.getBody("http://localhost:8080/file/" + testContent));
        Assertions.assertEquals(testContent + '0', Requests.getBody("http://localhost:8080/file/" + testContent + '0'));

        Assertions.assertFalse(handler.toString().contains("expired=true"));
    }

    @Test @Order(1)
    final void testClear() throws InterruptedException{
        Thread.sleep(2500);
        Assertions.assertEquals(testContent + '0', Requests.getBody("http://localhost:8080/file/" + testContent + '0'));

        Assertions.assertTrue(handler.toString().contains("expired=true"));
    }

    @Test @Order(2)
    final void testReAccess() throws InterruptedException{
        Thread.sleep(2500);
        Assertions.assertEquals(testContent, Requests.getBody("http://localhost:8080/file/" + testContent));
        Assertions.assertEquals(testContent + '0', Requests.getBody("http://localhost:8080/file/" + testContent + '0'));

        Assertions.assertFalse(handler.toString().contains("expired=true"));
    }

}