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

package dev.katsute.simplehttpserver.handler.throttler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.katsute.simplehttpserver.SimpleHttpExchange;
import dev.katsute.simplehttpserver.SimpleHttpHandler;

import java.io.IOException;

/**
 * The throttled handler limits how many simultaneous connections are allowed at any given time. Throttlers are used to determine how inbound connections are handled.
 *
 * @see ConnectionThrottler
 * @see ExchangeThrottler
 * @see ServerExchangeThrottler
 * @see SessionThrottler
 * @see ServerSessionThrottler
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public class ThrottledHandler implements SimpleHttpHandler {

    private final HttpHandler handler;
    private final ConnectionThrottler throttler;

    /**
     * Creates a throttled handler.
     *
     * @param throttler connection throttler
     * @param handler handler
     *
     * @see ConnectionThrottler
     * @since 5.0.0
     */
    public ThrottledHandler(final ConnectionThrottler throttler, final HttpHandler handler){
        this.handler   = handler;
        this.throttler = throttler;
    }

    @Override
    public final void handle(final HttpExchange exchange) throws IOException{
        SimpleHttpHandler.super.handle(exchange);
    }

    @Override
    public final void handle(final SimpleHttpExchange exchange) throws IOException {
        if(throttler.addConnection(exchange))
            try{
                handler.handle(exchange);
            }finally{
                throttler.deleteConnection(exchange);
            }
        else
            exchange.send(429); // too many requests
        exchange.close();
    }

    //

    @Override
    public String toString(){
        return "ThrottledHandler{" +
               "handler=" + handler +
               ", throttler=" + throttler +
               '}';
    }

}