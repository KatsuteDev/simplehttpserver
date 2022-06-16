/*
 * Copyright (C) 2022 Katsute <https://github.com/Katsute>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package dev.katsute.simplehttpserver;

import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

final class SimpleHttpExchangeImpl extends SimpleHttpExchange {

    private static Map<String,String> parseWwwFormEnc(final String s){
        final Map<String,String> OUT = new HashMap<>();
        final String[] pairs = s.split("&");

        for(final String pair : pairs){
            if(pair.contains("=")){
                final String[] kv = pair.split("=");
                try{
                    OUT.put(
                        URLDecoder.decode(kv[0], "UTF-8"),
                        kv.length == 2 ? URLDecoder.decode(kv[1], "UTF-8") : null
                    );
                }catch(UnsupportedEncodingException e){
                    // should not occur
                }
            }
        }
        return OUT;
    }

    private static final Pattern boundaryHeaderPattern = Pattern.compile("(.*): (.*?)(?:$|; )(.*)"); // returns the headers listed after each webkit boundary
    private static final Pattern contentDispositionKVPPattern = Pattern.compile("(.*?)=\"(.*?)\"(?:; |$)"); // returns the keys, values, and parameters for the content disposition header

    //

    private final HttpExchange exchange;

    private final String rawGet;
    private final Map<String,String> getMap;

    private final String rawPost;
    private final Map<String,?> postMap;
    private final MultipartFormData multipartFormData;

    private final Map<String,String> cookies;

    //

    @SuppressWarnings("unchecked")
    SimpleHttpExchangeImpl(final HttpExchange exchange){
        this.exchange = Objects.requireNonNull(exchange);

        rawGet = exchange.getRequestURI().getRawQuery();
        getMap = rawGet == null ? new HashMap<>() : parseWwwFormEnc(rawGet);

        String OUT;
        try(final Stream<String> lns = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)).lines()){
            OUT = lns.collect(Collectors.joining("\n"));
        }catch(Throwable e){
            OUT = null;
        }

        rawPost = OUT;

        if(rawPost != null){
            final String content_type = exchange.getRequestHeaders().getFirst("Content-type");
            if(content_type != null && content_type.startsWith("multipart/form-data")){
                final String webkitBoundary = content_type.substring(content_type.indexOf("; boundary=") + 11);
                final String startBoundary = "--" + webkitBoundary + "\r\n";
                final String endBoundary = "--" + webkitBoundary + "--\r\n"; // the final boundary in the request

                final Map<String, Map<String,?>> postMap_buffer = new HashMap<>();
                final String[] pairs = OUT.replace(endBoundary, "").split(Pattern.quote(startBoundary));
                for(String pair : pairs){
                    final Map<String, Map<String,Object>> postHeaders = new HashMap<>();
                    if(pair.contains("\r\n\r\n")){
                        final String[] headers = pair.substring(0, pair.indexOf("\r\n\r\n")).split("\r\n");

                        for(String header : headers){
                            final Map<String,Object> headerMap = new HashMap<>();
                            final Map<String,String> val = new HashMap<>();

                            final Matcher headerMatcher = boundaryHeaderPattern.matcher(header);
                            if(headerMatcher.find()){
                                final Matcher contentDispositionKVPMatcher = contentDispositionKVPPattern.matcher(headerMatcher.group(3));
                                while(contentDispositionKVPMatcher.find())
                                    val.put(contentDispositionKVPMatcher.group(1), contentDispositionKVPMatcher.group(2));

                                headerMap.put("header-name", headerMatcher.group(1));
                                headerMap.put("header-value", headerMatcher.group(2));
                                headerMap.put("parameters", val);
                            }
                            postHeaders.put((String) headerMap.get("header-name"), headerMap);
                        }

                        final Map<String,Object> row = new HashMap<>();
                        row.put("headers", postHeaders);
                        row.put("value", pair.substring(pair.indexOf("\r\n\r\n") + 4, pair.lastIndexOf("\r\n")));

                        postMap_buffer.put(
                            ((Map<String, String>) postHeaders.get("Content-Disposition").get("parameters")).get("name"),
                            row
                        );
                    }
                }
                Map<String, Record> form_buffer = new HashMap<>();
                for(final Map.Entry<String, Map<String,?>> e : postMap_buffer.entrySet()){
                    try{ // try to map as file record first
                        form_buffer.put(e.getKey(), new FileRecord(e));
                    }catch(final NullPointerException ignored){
                        try{ // try to map a standard record next
                            form_buffer.put(e.getKey(), new Record(e));
                        }catch(final NullPointerException ignored2){}
                    }catch(final ClassCastException ignored){
                        form_buffer = Collections.emptyMap();
                        break;
                    }
                }

                postMap = postMap_buffer;
                multipartFormData = form_buffer.isEmpty() ? null : new MultipartFormData(form_buffer);
            }else{
                postMap = parseWwwFormEnc(rawPost);
                multipartFormData = null;
            }
        }else{
            postMap = new HashMap<>();
            multipartFormData = null;
        }

        final String rawCookie = exchange.getRequestHeaders().getFirst("Cookie");
        final Map<String,String> cookie_buffer = new HashMap<>();
        if(rawCookie != null && !rawCookie.isEmpty()){
            final String[] cookedCookie = rawCookie.split("; "); // pair
            for(final String pair : cookedCookie){
                String[] value = pair.split("=");
                cookie_buffer.put(value[0], value[1]);
            }
        }
        cookies = cookie_buffer;
    }

    //

    @Override
    public final HttpServer getHttpServer(){
        return exchange.getHttpContext().getServer();
    }

    @Override
    public final HttpExchange getHttpExchange(){
        return exchange;
    }

    //

    @Override
    public final URI getRequestURI(){
        return exchange.getRequestURI();
    }

    @Override
    public final String getRequestMethod(){
        return exchange.getRequestMethod();
    }

    @Override
    public final InetSocketAddress getLocalAddress(){
        return exchange.getLocalAddress();
    }

    @Override
    public final InetSocketAddress getRemoteAddress(){
        return exchange.getRemoteAddress();
    }

    @Override
    public final HttpContext getHttpContext(){
        return exchange.getHttpContext();
    }

    @Override
    public final HttpPrincipal getPrincipal(){
        return exchange.getPrincipal();
    }

    @Override
    public final String getProtocol(){
        return exchange.getProtocol();
    }

    @Override
    public final Headers getRequestHeaders(){
        return exchange.getRequestHeaders();
    }

    @Override
    public final InputStream getRequestBody(){
        return exchange.getRequestBody();
    }

    @Override
    public final Object getAttribute(final String name){
        return exchange.getAttribute(name);
    }

    @Override
    public final void setAttribute(final String name, final Object value){
        exchange.setAttribute(name, value);
    }

    @Override
    public final void setStreams(final InputStream i, final OutputStream o){
        exchange.setStreams(i, o);
    }

    //

    @Override
    public final String getRawGet(){
        return rawGet;
    }

    @Override
    public final Map<String,String> getGetMap(){
        return new HashMap<>(getMap);
    }

    @Override
    public final boolean hasGet(){
        return rawGet != null;
    }

    //

    @Override
    public final String getRawPost(){
        return rawPost;
    }

    @Override
    public final Map<String,Object> getPostMap(){
        return new HashMap<>(postMap);
    }

    @Override
    public final MultipartFormData getMultipartFormData(){
        return multipartFormData;
    }

    @Override
    public final boolean hasPost(){
        return rawPost != null;
    }

    //

    @Override
    public final String getCookie(final String cookie){
        return cookies.get(Objects.requireNonNull(cookie));
    }

    @Override
    public final Map<String,String> getCookies(){
        return new HashMap<>(cookies);
    }

    @Override
    public synchronized final void setCookie(final String key, final String value){
        setCookie(new HttpCookie(Objects.requireNonNull(key), Objects.requireNonNull(value)));
    }

    @Override
    public synchronized final void setCookie(final HttpCookie cookie){
        exchange.getResponseHeaders().add("Set-Cookie", Objects.requireNonNull(cookie).toString());
    }

    //

    @Override
    public final int getResponseCode(){
        return exchange.getResponseCode();
    }

    @Override
    public final Headers getResponseHeaders(){
        return exchange.getResponseHeaders();
    }

    @Override
    public final OutputStream getResponseBody(){
        return exchange.getResponseBody();
    }

    //

    @Override
    public synchronized final void sendResponseHeaders(final int code, final long length) throws IOException {
        exchange.sendResponseHeaders(code, length);
    }

    @Override
    public synchronized final void send(final int responseCode) throws IOException {
        sendResponseHeaders(responseCode, 0);
    }

    @Override
    public synchronized final void send(final byte[] response) throws IOException {
        send(response, HttpURLConnection.HTTP_OK, false);
    }

    @Override
    public final void send(final byte[] response, final boolean gzip) throws IOException {
        send(response, HttpURLConnection.HTTP_OK, gzip);
    }

    @Override
    public synchronized final void send(final byte[] response, final int responseCode) throws IOException {
        send(response, responseCode, false);
    }

    @Override
    public final void send(final byte[] response, final int responseCode, final boolean gzip) throws IOException {
        if(gzip){
            exchange.getResponseHeaders().set("Accept-Encoding","gzip");
            exchange.getResponseHeaders().set("Content-Encoding","gzip");
            exchange.getResponseHeaders().set("Connection","keep-alive");
            sendResponseHeaders(responseCode, 0);
            try(GZIPOutputStream OUT = new GZIPOutputStream(exchange.getResponseBody())){
                OUT.write(Objects.requireNonNull(response));
                OUT.finish();
                OUT.flush();
            }
        }else{
            sendResponseHeaders(responseCode, response.length);
            try(final OutputStream OUT = exchange.getResponseBody()){
                OUT.write(response);
                OUT.flush();
            }
        }
    }

    @Override
    public synchronized final void send(final String response) throws IOException {
        send(response, HttpURLConnection.HTTP_OK, false);
    }

    @Override
    public final void send(final String response, final boolean gzip) throws IOException {
        send(response, HttpURLConnection.HTTP_OK, gzip);
    }

    @Override
    public synchronized final void send(final String response, final int responseCode) throws IOException {
        send(response, responseCode, false);
    }

    @Override
    public final void send(final String response, final int responseCode, final boolean gzip) throws IOException {
        send(Objects.requireNonNull(response).getBytes(StandardCharsets.UTF_8), responseCode, gzip);
    }

    @Override
    public final void send(final File file) throws IOException {
        send(file, HttpURLConnection.HTTP_OK, false);
    }

    @Override
    public final void send(final File file, final boolean gzip) throws IOException {
        send(file, HttpURLConnection.HTTP_OK, gzip);
    }

    @Override
    public final void send(final File file, final int responseCode) throws IOException {
        send(file, responseCode, false);
    }

    @Override
    public final void send(final File file, final int responseCode, final boolean gzip) throws IOException {
        send(Files.readAllBytes(Objects.requireNonNull(file).toPath()), responseCode, gzip);
    }

    //

    @Override
    public synchronized final void close(){
        try{
            exchange.getResponseBody().close();
        }catch(final IOException ignored){
        }finally{
            exchange.close();
        }
    }

}
