package dev.katsute.simplehttpserver;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class Requests {

    public static CookieManager Cookies = new CookieManager();

    static{
        CookieHandler.setDefault(Cookies);
    }

    public static HttpURLConnection openConn(final String URL){
        try{
            final HttpURLConnection conn = (HttpURLConnection) new URL(URL).openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            return conn;
        }catch(final IOException e){
            throw new UncheckedIOException(e);
        }
    }

    public static int getCode(final String URL){
        try{
            return openConn(URL).getResponseCode();
        }catch(final IOException e){
            throw new UncheckedIOException(e);
        }
    }

    public static String getBody(final String URL){
        return getBody(openConn(URL), false);
    }

    public static String getBody(final String URL, final boolean ignoreError){
        return getBody(openConn(URL), ignoreError);
    }

    public static String getBody(final HttpURLConnection conn){
        return getBody(conn, false);
    }

    public static String getBody(final HttpURLConnection conn, final boolean ignoreError){
        final StringBuilder OUT = new StringBuilder();
        try(final BufferedReader IN = new BufferedReader(new InputStreamReader("gzip".equalsIgnoreCase(conn.getContentEncoding()) ? new GZIPInputStream(conn.getInputStream()) : conn.getInputStream(), StandardCharsets.UTF_8))){
            String buffer;
            while((buffer = IN.readLine()) != null)
                OUT.append(buffer).append('\n');
        }catch(final IOException e){
            if(!ignoreError)
                throw new UncheckedIOException(e);
            else
                return null;
        }
        return OUT.toString().trim();
    }

}
