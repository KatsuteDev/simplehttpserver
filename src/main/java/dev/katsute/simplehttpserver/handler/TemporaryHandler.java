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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.katsute.simplehttpserver.SimpleHttpServer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Objects;

/**
 * Handler that removes itself after a single request, or after a set time, whichever comes first. This can be used for single use downloads or disposable links. A random unused context can be created by using {@link SimpleHttpServer#getRandomContext()} or {@link SimpleHttpServer#getRandomContext(String)}.
 *
 * @see SimpleHttpServer#getRandomContext()
 * @see SimpleHttpServer#getRandomContext(String)
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public class TemporaryHandler implements HttpHandler {

    private final HttpHandler handler;

    private final Long expiry;

    /**
     * Creates a temporary handler that removes itself after the first request.
     *
     * @param handler handler
     *
     * @since 5.0.0
     */
    public TemporaryHandler(final HttpHandler handler){
        this.handler = Objects.requireNonNull(handler);
        this.expiry  = null;
    }

    /**
     * Creates a temporary handler that removes itself after the first request, or after a set time.
     *
     * @param handler handler
     * @param maxTime how long the handler should exists for in milliseconds
     *
     * @since 5.0.0
     */
    public TemporaryHandler(final HttpHandler handler, final long maxTime){
        this.handler = Objects.requireNonNull(handler);
        this.expiry  = System.currentTimeMillis() + maxTime;
    }

    @Override
    public synchronized final void handle(final HttpExchange exchange) throws IOException{
        if(expiry == null || System.currentTimeMillis() < expiry) // expire on first connection or past expiry
            handler.handle(exchange);
        else
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
        exchange.getHttpContext().getServer().removeContext(exchange.getHttpContext());
        exchange.close();
    }

    //

    @Override
    public String toString(){
        return "TemporaryHandler{" +
               "handler=" + handler +
               ", expiry=" + expiry +
               '}';
    }

}
