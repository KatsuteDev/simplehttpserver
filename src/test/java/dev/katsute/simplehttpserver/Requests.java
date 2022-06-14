package dev.katsute.simplehttpserver;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class Requests {

    public static String getBody(final HttpURLConnection conn){
        try(final BufferedReader IN = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))){
            String buffer;
            final StringBuilder OUT = new StringBuilder();
            while((buffer = IN.readLine()) != null)
                OUT.append(buffer);
            return OUT.toString();
        }catch(final IOException ignored){}
        return null;
    }

}
