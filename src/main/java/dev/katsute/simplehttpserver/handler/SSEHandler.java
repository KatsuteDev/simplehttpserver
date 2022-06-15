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


package dev.katsute.simplehttpserver.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import dev.katsute.simplehttpserver.SimpleHttpExchange;
import dev.katsute.simplehttpserver.SimpleHttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A <a href="https://www.w3schools.com/html/html5_serversentevents.asp">Server sent events (SSE)</a> handler sends events from the server to a client using an <code>text/event-stream</code>. Events are sent using {@link #push(String)} or {@link #push(String, int, String)}.
 *
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public class SSEHandler implements SimpleHttpHandler {

    private final List<OutputStream> listeners  = new ArrayList<>();
    private final AtomicInteger eventID         = new AtomicInteger(-1);
    private final List<EventStreamRecord> queue = new ArrayList<>();

    /**
     * Creates a SSE handler.
     *
     * @since 5.0.0
     */
    public SSEHandler(){ }

    @Override
    public final void handle(final HttpExchange exchange) throws IOException{
        SimpleHttpHandler.super.handle(exchange);
    }

    @Override
    public final void handle(final SimpleHttpExchange exchange) throws IOException{
        final Headers responseHeaders = exchange.getResponseHeaders();

        responseHeaders.add("Access-Control-Allow-Headers", "Content-Type");
        if(exchange.getRequestHeaders().getFirst("origin") != null)
            responseHeaders.add("Access-Control-Allow-Origin", exchange.getRequestHeaders().getFirst("origin"));
        responseHeaders.add("Access-Control-Allow-Methods","GET, HEAD, POST, PUT, DELETE");
        responseHeaders.add("Access-Control-Max-Age", "3600");
        responseHeaders.add("Cache-Control","no-cache");

        if(exchange.getRequestMethod().equalsIgnoreCase("options")){
            exchange.send(HttpURLConnection.HTTP_OK);
            return;
        }

        responseHeaders.put("Content-Type", Collections.singletonList("text/event-stream"));

        int latest = 0;
        try{
            latest = Integer.parseInt(exchange.getRequestHeaders().getFirst("Last_Event-ID"));
        }catch(final NumberFormatException | NullPointerException ignored){ }

        exchange.send(HttpURLConnection.HTTP_OK);
        for(int index = latest; index < queue.size(); index++){ // write latest events
            exchange.getResponseBody().write(queue.get(index).toString(eventID.get()).getBytes(StandardCharsets.UTF_8));
            exchange.getResponseBody().flush();
        }

        listeners.add(exchange.getResponseBody()); // track events
    }

    /**
     * Pushes an event to the stream.
     *
     * @param data data to send
     *
     * @see #push(String, int, String)
     * @since 5.0.0
     */
    public synchronized final void push(final String data){
        push(data, 0, "");
    }

    /**
     * Pushes an event to the stream
     *
     * @param data data to send
     * @param retry how long to retry for
     * @param event event type
     *
     * @see #push(String)
     * @since 5.0.0
     */
    public synchronized final void push(final String data, final int retry, final String event){
        eventID.addAndGet(1);
        final EventStreamRecord record = new EventStreamRecord(retry, event, data);
        queue.add(record);
        for(final OutputStream OUT : listeners){ // push events to all clients
            try{
                OUT.write(record.toString(eventID.get()).getBytes(StandardCharsets.UTF_8));
                OUT.flush();
            }catch(final IOException ignored){ // internal error or closed
                listeners.remove(OUT); // remove from tracking
            }
        }
    }

    private static class EventStreamRecord {

        private final int retry;
        private final String event;
        private final String data;

        private EventStreamRecord(final int retry, final String event, final String data){
            this.retry = retry;
            this.event = event;
            this.data  = data;
        }

        private String toString(final int id){
            return
                "id: " + id + '\n' +
                (retry > 0 ? "retry: " + retry + '\n' : "") +
                (!event.trim().isEmpty() ? "event: " + event + '\n' : "") +
                (!data.trim().isEmpty() ? "data: " + data + '\n' : "") +
                '\n';
        }

    }

}
