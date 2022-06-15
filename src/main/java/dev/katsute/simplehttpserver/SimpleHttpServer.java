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

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * A {@link HttpServer} with additional extensions to simplify usage.
 *
 * @see HttpServer
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public abstract class SimpleHttpServer extends HttpServer implements HttpServerExtensions {

    SimpleHttpServer(){ }

    /**
     * Creates an unbounded http server.
     *
     * @return http server
     * @throws IOException IO exception
     *
     * @see #create(int)
     * @see #create(int, int)
     * @see #create(InetSocketAddress, int)
     * @since 5.0.0
     * @author Katsute
     */
    public static SimpleHttpServer create() throws IOException {
        return new SimpleHttpServerImpl(null, null);
    }

    /**
     * Creates an http server bounded to a port.
     *
     * @param port to bind to
     * @return http server
     * @throws IOException IO exception
     * @throws java.net.BindException if server could not be bounded
     *
     * @see #create()
     * @see #create(int, int)
     * @see #create(InetSocketAddress, int)
     * @since 5.0.0
     * @author Katsute
     */
    public static SimpleHttpServer create(final int port) throws IOException {
        return new SimpleHttpServerImpl(port, null);
    }

    /**
     * Creates an http server bounded to a port.
     *
     * @param port to bind to
     * @param backlog maximum amount of inbound connections at any given time
     * @return http server
     * @throws IOException IO exception
     * @throws java.net.BindException if server could not be bounded
     *
     * @see #create()
     * @see #create(int)
     * @see #create(InetSocketAddress, int)
     * @since 5.0.0
     * @author Katsute
     */
    public static SimpleHttpServer create(final int port, final int backlog) throws IOException {
        return new SimpleHttpServerImpl(port, backlog);
    }

    //

    /**
     * Returns the underlying http server.
     *
     * @return http server
     *
     * @see HttpServer
     * @since 5.0.0
     */
    public abstract HttpServer getHttpServer();

}
