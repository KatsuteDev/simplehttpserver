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

package dev.katsute.simplehttpserver;

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Additional extensions provided to a {@link SimpleHttpServer}.
 *
 * @see SimpleHttpServer
 * @see SimpleHttpsServer
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
interface HttpServerExtensions {

    /**
     * Binds the server to a port.
     *
     * @param port port to bind to
     * @return server address
     * @throws IOException internal error
     * @throws java.net.BindException if server could not be bounded
     *
     * @see HttpServer#bind(InetSocketAddress, int)
     * @see #bind(int, int)
     * @since 5.0.0
     */
    InetSocketAddress bind(final int port) throws IOException;

    /**
     * Binds the server to a port.
     *
     * @param port port to bind to
     * @param backlog maximum amount of inbound connections at any given time
     * @return server address
     * @throws IOException internal error
     * @throws java.net.BindException if server could not be bounded
     *
     * @see HttpServer#bind(InetSocketAddress, int)
     * @see #bind(int, int)
     * @since 5.0.0
     */
    InetSocketAddress bind(final int port, final int backlog) throws IOException;

    //

    /**
     * Sets as session handler to use for the server.
     *
     * @param sessionHandler session handler
     *
     * @see HttpSessionHandler
     * @see #getSessionHandler()
     * @since 5.0.0
     */
    void setSessionHandler(final HttpSessionHandler sessionHandler);

    HttpSessionHandler getSessionHandler();

    /**
     * Returns the session for a given exchange.
     *
     * @param exchange http exchange
     * @return session associated with an exchange
     *
     * @see HttpSession
     * @since 5.0.0
     */
    HttpSession getSession(final HttpExchange exchange);

    //

    /**
     * Returns the handler for a given context.
     *
     * @param context context
     * @return handler for context
     *
     * @see #getContextHandler(HttpContext)
     * @see #getContexts()
     * @since 5.0.0
     */
    HttpHandler getContextHandler(final String context);

    /**
     * Returns the handler for a given context.
     *
     * @param context http context
     * @return handler for context
     *
     * @see #getContextHandler(String)
     * @see #getContexts()
     * @since 5.0.0
     */
    HttpHandler getContextHandler(final HttpContext context);

    /**
     * Returns a map of all the contexts registered to the server.
     * @return map of contexts
     *
     * @see #getContextHandler(String)
     * @see #getContextHandler(HttpContext)
     * @since 5.0.0
     */
    Map<HttpContext,HttpHandler> getContexts();

    /**
     * Returns a random context that doesn't yet exist on the server.
     *
     * @return random unused context
     *
     * @see #getRandomContext(String)
     * @since 5.0.0
     */
    String getRandomContext();

    /**
     * Returns a random context prefixed by a set context.
     *
     * @param context context to prefix
     * @return random unused context with prefix
     *
     * @see #getRandomContext()
     * @since 5.0.0
     */
    String getRandomContext(final String context);

    //

    /**
     * Stops the server immediately without waiting.
     *
     * @see HttpServer#stop(int)
     * @since 5.0.0
     */
    void stop();

}