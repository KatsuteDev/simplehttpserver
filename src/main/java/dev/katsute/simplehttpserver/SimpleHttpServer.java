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

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dev.katsute.simplehttpserver.handler.RootHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A {@link HttpServer} with additional extensions to simplify usage.
 *
 * <h1>Creating a SimpleHttpServer</h1>
 * A SimpleHttpServer is created in the same way was a standard HttpServer, by using one of the <code>create</code> methods.
 * <ul>
 *     <li>{@link #create()}</li>
 *     <li>{@link #create(int)}</li>
 *     <li>{@link #create(int, int)}</li>
 *     <li>{@link #create(InetSocketAddress, int)}</li>
 * </ul>
 *
 * <h2>Binding to a Port</h2>
 * A SimpleHttpServer requires a port to become accessible, the port can be defined in the <code>create</code> or <code>bind</code> methods.
 * <ul>
 *     <li>{@link #create(int)}</li>
 *     <li>{@link #create(int, int)}</li>
 *     <li>{@link #create(InetSocketAddress, int)}</li>
 *     <li>{@link #bind(int)}</li>
 *     <li>{@link #bind(int, int)}</li>
 *     <li>{@link #bind(InetSocketAddress, int)}</li>
 * </ul>
 * The backlog parameter in each of these determines the maximum amount of simultaneous connections at any given time.
 *
 * <h2>Starting the Server</h2>
 * The server can be started by using the {@link #start()} method.
 *
 * <h2>Stopping the Server</h2>
 * The server can be stopped by using {@link #stop()} or {@link #stop(int)} methods.
 * <br>
 * The delay parameter determines how long the server should wait before forcefully closing the connection, by default this is 0.
 *
 * <h1>Adding Pages</h1>
 * Pages (referred to as a context) are added by using any of the <code>createContext</code> methods.
 * <ul>
 *     <li>{@link #createContext(String)}</li>
 *     <li>{@link #createContext(String, HttpHandler)}</li>
 * </ul>
 * Content for each context is determined by using a {@link HttpHandler}. SimpleHttpServer offers some simplified handlers to handle some complex operations, documentation for those can be found in {@link SimpleHttpHandler}.
 * <br>
 * Contexts in a server are <b>case sensitive</b> and will resolve to the most specific context available.
 * <br><br>
 * <b>Example:</b>
 * <br>
 * If a user goes to <code>/this/web/page</code> and there are only handlers for <code>/this</code> and <code>/this/web</code>, then the handler for <code>/this/web</code> would be used because its the most specific context that exists on the server.
 * <br><br>
 * This behavior consequentially means that any handler added to the root <code>/</code> context would handle any requests that don't have a handler, since it's the most specific one available. A {@link RootHandler} can be used to mediate this issue.
 *
 * <h1>Accessing the Server</h1>
 * <h2>Accessing on Host Machine</h2>
 * When a server is started it is immediately available at whatever port was set.
 * <br>
 * <b>Example:</b> <code>localhost:8000</code>, <code>127.0.0.1:80</code>
 * <h2>Accessing on Local Network</h2>
 * If permitted, the server should also be accessible to all computers on your immediate internet network at your <a href="https://www.google.com/search?q=How+to+find+my+local+ip+address">local IP</a>.
 * <h2>Accessing Globally</h2>
 * For clients not connected to your network, they must use your <a href="https://www.whatismyip.com/what-is-my-public-ip-address/">public IP address</a>.
 * <br>
 * People outside your network can not connect unless you <b>port forward</b> the port the server is using. This process varies depending on your ISP and can often leave your network vulnerable to attackers. It is not suggested to this unless you know what you are doing.
 * <br>
 * You can learn to port forward <a href="https://www.google.com/search?q=How+to+port+forward">here</a> (at your own risk).
 *
 * <h1>Multi-threaded Server</h1>
 * By default the server runs on a single thread. This means that only one clients exchange can be processed at a time and can lead to long queues.
 * <br>
 * For a server to be multi-threaded the executor must be changed to one that process threads in parallel. The executor can be changed using the {@link #setExecutor(Executor)} method on the server.
 * <br>
 * To process a fixed amount of threads you can use {@link Executors#newFixedThreadPool(int)}.
 * <br>
 * To process an unlimited amount of threads you can use {@link Executors#newCachedThreadPool()}.
 * <h2>Requests are still not being processed in parallel</h2>
 * Requests to the same context may not run in parallel for a user that is accessing the same page more than once. This issue is caused by the browser, where it will not send duplicate requests to the server at the same time.
 * <br>
 * This issue is better explained <a href="https://stackoverflow.com/a/58676470">here</a>.
 * <br>
 * If you still need to test multithreading then you must use an older browser like Internet Explorer or Microsoft Edge.
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
