package dev.katsute.simplehttpserver.handler.file;

import dev.katsute.simplehttpserver.Requests;
import dev.katsute.simplehttpserver.SimpleHttpServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
final class FileHandlerDirTests {

    private static SimpleHttpServer server;

    private static final String testContent = String.valueOf(System.currentTimeMillis());

    @TempDir
    private static File dir = new File(testContent);
    private static File subdir = new File(dir, testContent);

    private static final FileHandler handler = new FileHandler();

    private static final String empty = handler.toString();

    @BeforeAll
    static void beforeAll() throws IOException{
        server = SimpleHttpServer.create(8080);

        final File file = new File(dir, testContent);
        Files.write(file.toPath(), testContent.getBytes());
        subdir = new File(dir, "sd");
        Assertions.assertTrue(subdir.mkdirs());
        final File walk = new File(subdir, testContent);
        Files.write(walk.toPath(), testContent.getBytes());

        final String context = "";

        handler.addDirectory(dir);
        handler.addDirectory(dir, new FileOptions.Builder().setContext("alt").build());
        handler.addDirectory(dir, "dirName");
        handler.addDirectory(dir, "dirName", new FileOptions.Builder().setContext("altn").build());

        server.createContext(context, handler);

        server.start();
    }

    @AfterAll
    static void afterAll(){
        server.stop();
    }

    @Test @Order(0)
    final void testValid(){
        final String[] validPathsToTest = {
            dir.getName() + '/' + testContent,
            "alt"         + '/' + dir.getName()   + '/' + testContent,
            "dirName"     + '/' + testContent,
            "altn"        + '/' + "dirName"      + '/' + testContent
        };

        for(final String path : validPathsToTest)
            Assertions.assertEquals(testContent, Requests.getBody("http://localhost:8080/" + path));
    }

    @Test @Order(1)
    final void testInvalid(){
        final String[] invalidPathsToTest = {
            dir.getName() + '/' + subdir.getName(),
            dir.getName() + '/' + subdir.getName() + '/' + testContent
        };

        for(final String path : invalidPathsToTest)
            Assertions.assertNull(Requests.getBody("http://localhost:8080/" + path, true));
    }

    @Test @Order(2)
    final void cleanup(){
        handler.removeDirectory(dir);
        handler.removeDirectory(dir, new FileOptions.Builder().setContext("alt").build());
        handler.removeDirectory("dirName");
        handler.removeDirectory("altn" + '/' + "dirName");
        Assertions.assertEquals(empty, handler.toString());
    }

}