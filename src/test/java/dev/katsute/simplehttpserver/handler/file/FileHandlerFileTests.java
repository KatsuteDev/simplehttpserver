package dev.katsute.simplehttpserver.handler.file;

import dev.katsute.simplehttpserver.Requests;
import dev.katsute.simplehttpserver.SimpleHttpServer;
import dev.katsute.simplehttpserver.handler.file.FileOptions.FileLoadingOption;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

final class FileHandlerFileTests {

    private static SimpleHttpServer server;

    private static final String testContent = String.valueOf(System.currentTimeMillis());

    @TempDir
    private static File dir = new File(testContent);

    private static final FileHandler handler = new FileHandler();

    private static final Map<File,FileLoadingOption> files = new HashMap<>();

    private static final String empty = handler.toString();

    @BeforeAll
    static void beforeAll() throws IOException{
        server = SimpleHttpServer.create(8080);

        Arrays.stream(FileLoadingOption.values())
              .filter(o -> o != FileLoadingOption.CACHE)
              .forEach(blop -> files.put(new File(dir, blop.name()), blop));

        files.forEach((file, loadingOption) -> {
            Assertions.assertDoesNotThrow(() ->  Files.write(file.toPath(), testContent.getBytes()));
            handler.addFile(file, new FileOptions.Builder().setLoadingOption(loadingOption).build());
        });

        server.createContext("file", handler);

        server.start();
    }

    @AfterAll
    static void afterAll(){
        server.stop();
    }

    @Test
    final void testAddAndRemove(){
        files.forEach((file, loadingOption) -> {
            final String url = "http://localhost:8080/file/" + file.getName();

            Assertions.assertEquals(testContent, Requests.getBody(url));

            // second write

            final String after = String.valueOf(System.currentTimeMillis());
            Assertions.assertDoesNotThrow(() -> Files.write(file.toPath(), after.getBytes()));

            Assertions.assertEquals( loadingOption == FileLoadingOption.PRELOAD ? testContent : after, Requests.getBody(url));
        });

        files.forEach((file, loadingOption) -> handler.removeFile(file));

        Assertions.assertEquals(empty, handler.toString());
    }

}