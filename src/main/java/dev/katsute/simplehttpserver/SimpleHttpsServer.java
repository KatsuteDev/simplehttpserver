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

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * A http handler that uses a {@link SimpleHttpExchange}
 * <br>
 * Http handlers will not throw an exception in the main thread, you must use a try-catch to expose them. All requests must be closed with {@link HttpExchange#close()}, otherwise the handler will rerun the request multiple times.
 * <br>
 * This handler can be used with a standard {@link com.sun.net.httpserver.HttpsServer}.
 *
 * @see HttpHandler
 * @see SimpleHttpExchange
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public abstract class SimpleHttpsServer extends HttpsServer implements HttpServerExtensions {

    SimpleHttpsServer(){ }

    /**
     * Creates an unbounded https server.
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
    public static SimpleHttpsServer create() throws IOException {
        return new SimpleHttpsServerImpl(null, null);
    }

    /**
     * Creates an https server bounded to a port.
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
    public static SimpleHttpsServer create(final int port) throws IOException {
        return new SimpleHttpsServerImpl(port, null);
    }

    /**
     * Creates an https server bounded to a port.
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
    public static SimpleHttpsServer create(final int port, final int backlog) throws IOException {
        return new SimpleHttpsServerImpl(port, backlog);
    }

    //

    /**
     * Returns the underlying http server.
     *
     * @return http server
     *
     * @see HttpsServer
     * @since 5.0.0
     */
    public abstract HttpsServer getHttpsServer();

}
