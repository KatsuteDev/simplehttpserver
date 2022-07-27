package dev.katsute.simplehttpserver.exchange;

import dev.katsute.simplehttpserver.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

final class ExchangeMultipartFormTests {

    private static SimpleHttpServer server;

    private static SimpleHttpExchange exchange;

    private static final String boundary = "d74496d66958873e";

    private static final String key = "key", value = "value";
    private static final String fkey = "fileKey", filename = "fileName.txt", fvalue = "fileValue", contentType = "text/plain";

    @BeforeAll
    static void beforeAll() throws IOException{
        server = SimpleHttpServer.create(8080);

        AtomicReference<SimpleHttpExchange> exchangeRef = new AtomicReference<>();

        server.createContext("exchange", (SimpleHttpHandler) e -> {
            exchangeRef.set(e);
            e.send(200);
            e.close();
        });

        server.start();

        final StringBuilder OUT = new StringBuilder();
        OUT.append("--------------------------").append(boundary).append("\r\n");
        OUT.append("Content-Disposition: ").append("form-data; ").append("name=\"").append(key).append('\"').append("\r\n\r\n");
        OUT.append(value).append("\r\n");
        OUT.append("--------------------------").append(boundary).append("\r\n");
        OUT.append("Content-Disposition: ").append("form-data; ").append("name=\"").append(fkey).append("\"; ");
        OUT.append("filename=\"").append(filename).append('\"').append("\r\n");
        OUT.append("Content-Type: ").append(contentType).append("\r\n\r\n");
        OUT.append(fvalue).append("\r\n");
        OUT.append("--------------------------").append(boundary).append("--");

        {
            final HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8080/exchange").openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-type", "multipart/form-data; boundary=" + boundary);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            try(final OutputStream OS = conn.getOutputStream()){
                final byte[] bytes = OUT.toString().getBytes(StandardCharsets.UTF_8);
                OS.write(bytes, 0, bytes.length);
            }

            conn.getOutputStream().flush();
            conn.getOutputStream().close();

            Requests.getBody(conn);
        }

        exchange = exchangeRef.get();

        server.stop();
    }

    @Test
    final void testPOST(){
        Assertions.assertEquals("POST", exchange.getRequestMethod().toUpperCase());
        Assertions.assertTrue(exchange.hasPost());
    }

    @Test
    final void testData(){
        Assertions.assertEquals(value, ((Map<?,?>) exchange.getPostMap().get(key)).get("value"));

        Assertions.assertEquals(filename, ((Map<?,?>) ((Map<?,?>) ((Map<?,?>) ((Map<?,?>) exchange.getPostMap().get(fkey)).get("headers")).get("Content-Disposition")).get("parameters")).get("filename"));
        Assertions.assertEquals(contentType, ((Map<?,?>) ((Map<?,?>) ((Map<?,?>) exchange.getPostMap().get(fkey)).get("headers")).get("Content-Type")).get("header-value"));
        Assertions.assertEquals(fvalue, ((Map<?,?>) exchange.getPostMap().get(fkey)).get("value"));
    }

    @Test
    final void testMultipart(){
        Assertions.assertNotNull(exchange.getMultipartFormData());
        Assertions.assertEquals(2, exchange.getMultipartFormData().getEntries().size());

        Assertions.assertEquals(value, exchange.getMultipartFormData().getEntry(key).getValue());

        Assertions.assertEquals(filename, ((FileRecord) exchange.getMultipartFormData().getEntry(fkey)).getFileName());
        Assertions.assertEquals(contentType, ((FileRecord) exchange.getMultipartFormData().getEntry(fkey)).getContentType());
        Assertions.assertEquals(fvalue, new String(((FileRecord) exchange.getMultipartFormData().getEntry(fkey)).getBytes()));
    }

}
