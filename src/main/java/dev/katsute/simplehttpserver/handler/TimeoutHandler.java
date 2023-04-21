/*
 * Copyright (C) 2023 Katsute <https://github.com/Katsute>
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
import dev.katsute.simplehttpserver.SimpleHttpExchange;
import dev.katsute.simplehttpserver.SimpleHttpHandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * The timeout handler runs a handler and times out after a set time.
 *
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public class TimeoutHandler implements SimpleHttpHandler {

    private final HttpHandler handler;

    private final TimeUnit unit;
    private final long timeout;

    private final ExecutorService service = Executors.newCachedThreadPool();

    /**
     * Creates a timeout handler.
     *
     * @param handler handler
     * @param timeout how long until timeout in seconds
     *
     * @since 5.0.0
     */
    public TimeoutHandler(final HttpHandler handler, final double timeout){
        this(handler, timeout, TimeUnit.SECONDS);
    }

    /**
     * Creates a timeout handler.
     *
     * @param handler handler
     * @param timeout how long until timeout
     * @param unit timeout units
     *
     * @see TimeUnit
     * @since 5.0.0
     */
    public TimeoutHandler(final HttpHandler handler, final double timeout, final TimeUnit unit){
        this.handler = Objects.requireNonNull(handler);
        this.timeout = (long) timeout;
        this.unit = Objects.requireNonNull(unit);
    }

    @Override
    public final void handle(final HttpExchange exchange) throws IOException{
        SimpleHttpHandler.super.handle(exchange);
    }

    @Override
    public final void handle(final SimpleHttpExchange exchange) throws IOException{
        final Future<?> future = service.submit(() -> {
            try{
                handler.handle(exchange);
            }catch(final IOException e){
                throw new RuntimeException(e);
            }
        });
        try{
            future.get(timeout, unit);
        }catch(final Throwable e){
            future.cancel(true);
            exchange.send(HttpURLConnection.HTTP_CLIENT_TIMEOUT);
            if(!(e instanceof TimeoutException))
                throw new RuntimeException(e);
        }finally{
            exchange.close();
        }
    }

    @Override
    public String toString(){
        return "TimeoutHandler{" +
               "handler=" + handler +
               ", timeout=" + timeout +
               ", unit=" + unit +
               '}';
    }

}