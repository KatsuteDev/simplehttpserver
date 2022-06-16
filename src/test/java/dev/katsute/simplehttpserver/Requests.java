package dev.katsute.simplehttpserver;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Requests {

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
        return getBody(openConn(URL));
    }

    public static String getBody(final HttpURLConnection conn){
        try(final BufferedReader IN = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))){
            String buffer;
            final StringBuilder OUT = new StringBuilder();
            while((buffer = IN.readLine()) != null)
                OUT.append(buffer);
            return OUT.toString();
        }catch(final IOException e){
            throw new UncheckedIOException(e);
        }
    }

}
