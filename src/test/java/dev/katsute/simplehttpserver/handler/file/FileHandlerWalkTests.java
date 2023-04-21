package dev.katsute.simplehttpserver.handler.file;

import dev.katsute.simplehttpserver.Requests;
import dev.katsute.simplehttpserver.SimpleHttpServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

final class FileHandlerWalkTests {

    private static SimpleHttpServer server;

    private static final String testContent = String.valueOf(System.currentTimeMillis());

    @TempDir
    private static File dir = new File(testContent);
    private static File subdir = new File(dir, testContent);

    private static final FileHandler handler = new FileHandler(new FileAdapter() {
        @Override
        public String getName(final File file){
            return file.getName().substring(0, file.getName().contains(".") ? file.getName().lastIndexOf('.') : file.getName().length());
        }
    });

    @BeforeAll
    static void beforeAll() throws IOException{
        server = SimpleHttpServer.create(8080);

        final File file = new File(dir, testContent + ".txt");
        Files.write(file.toPath(), testContent.getBytes());
        subdir = new File(dir, "sd");
        Assertions.assertTrue(subdir.mkdirs());
        final File walk = new File(subdir, testContent + ".txt");
        Files.write(walk.toPath(), testContent.getBytes());

        final String context = "";

        final FileOptions opts = new FileOptions.Builder().setWalk(true).build();

        handler.addDirectory(dir, opts);

        server.createContext(context, handler);

        server.start();
    }

    @AfterAll
    static void afterAll(){
        server.stop();
    }

    @Test
    final void testValid(){
        final String[] validPathsToTest = {
            dir.getName() + '/' + testContent,
            dir.getName() + '/' + subdir.getName() + '/' + testContent,
        };

        for(final String path : validPathsToTest)
            Assertions.assertEquals(testContent, Requests.getBody("http://localhost:8080/" + path));
    }

    @Test
    final void testInvalid(){
        final String[] invalidPathsToTest = {
            dir.getName() + '/' + subdir.getName()
        };

        for(final String path : invalidPathsToTest)
            Assertions.assertNull(Requests.getBody("http://localhost:8080/" + path, true));
    }

}