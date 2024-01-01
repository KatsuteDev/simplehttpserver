/*
 * Copyright (C) 2024 Katsute <https://github.com/Katsute>
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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * A http handler that uses a {@link SimpleHttpExchange}. All handlers in this library can be used with a standard {@link com.sun.net.httpserver.HttpServer}.
 * <br>
 * <b>Note:</b> Http handlers will not throw an exception in the main thread, you must use a try-catch to expose them.
 * <br>
 * <b>Note:</b> An exchange must be sent or closed, otherwise the connection may resend the request until it gets response or times out.
 *
 * @see HttpHandler
 * @see SimpleHttpExchange
 * @see dev.katsute.simplehttpserver.handler.PredicateHandler
 * @see dev.katsute.simplehttpserver.handler.RedirectHandler
 * @see dev.katsute.simplehttpserver.handler.RootHandler
 * @see dev.katsute.simplehttpserver.handler.SSEHandler
 * @see dev.katsute.simplehttpserver.handler.TemporaryHandler
 * @see dev.katsute.simplehttpserver.handler.TimeoutHandler
 * @see dev.katsute.simplehttpserver.handler.throttler.ThrottledHandler
 * @see dev.katsute.simplehttpserver.handler.file.FileHandler
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public interface SimpleHttpHandler extends HttpHandler {

    /**
     * <b>Do not override this method</b>, it will cause {@link #handle(SimpleHttpExchange)} to not work. Use {@link #handle(SimpleHttpExchange)} instead.
     *
     * @param exchange the exchange containing the request from the client and used to send the response
     * @throws IOException IO exception
     *
     * @deprecated use {@link #handle(SimpleHttpExchange)}
     * @see #handle(SimpleHttpExchange)
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated @Override
    default void handle(final HttpExchange exchange) throws IOException {
        handle(SimpleHttpExchange.create(exchange));
    }

    /**
     * Handles an exchange.
     * <br>
     * Http handlers will not throw an exception in the main thread, you must use a try-catch to expose them. All requests must be closed with {@link HttpExchange#close()}, otherwise the handler will rerun the request multiple times.
     *
     * @param exchange http exchange
     * @throws IOException IO exception
     *
     * @see SimpleHttpExchange
     * @since 5.0.0
     */
    void handle(final SimpleHttpExchange exchange) throws IOException;

}