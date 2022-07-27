package dev.katsute.simplehttpserver.handler.file;

import dev.katsute.simplehttpserver.Requests;
import dev.katsute.simplehttpserver.SimpleHttpServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

final class FileHandlerFilesTests {

    private static SimpleHttpServer server;

    private static final String testContent = String.valueOf(System.currentTimeMillis());

    @TempDir
    private static File dir = new File(testContent);

    private static final FileHandler handler = new FileHandler(new FileAdapter() {
        @Override
        public String getName(final File file){
            return file.getName().substring(0, file.getName().contains(".") ? file.getName().lastIndexOf('.') : file.getName().length());
        }
    });

    private static File[] files;
    private static final String empty = handler.toString();

    @BeforeAll
    static void beforeAll() throws IOException{
        server = SimpleHttpServer.create(8080);

        files = new File[]{
            new File(dir, testContent + ".txt"),
            new File(dir, testContent + '0' + ".txt")
        };

        for(final File file : files)
            Files.write(file.toPath(), testContent.getBytes(StandardCharsets.UTF_8));

        handler.addFiles(files);
        handler.addFiles(files, new FileOptions.Builder().setContext("alt").build());

        server.createContext("files", handler);

        server.start();
    }

    @AfterAll
    static void afterAll(){
        server.stop();
    }

    @Test
    final void testAddAndRemove(){
        for(final File file : files){
            Assertions.assertEquals(testContent, Requests.getBody("http://localhost:8080/files/" + file.getName().substring(0, file.getName().lastIndexOf('.'))));

            // alt

            Assertions.assertEquals(testContent, Requests.getBody("http://localhost:8080/files/alt/" + file.getName().substring(0, file.getName().lastIndexOf('.'))));
        }

        for(final File file : files){
            handler.removeFile(file);
            handler.removeFile(file, new FileOptions.Builder().setContext("alt").build());
        }

        Assertions.assertEquals(empty, handler.toString());
    }

}
