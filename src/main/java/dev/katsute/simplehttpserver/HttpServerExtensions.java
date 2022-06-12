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
import java.util.Map;

interface HttpServerExtensions {

    InetSocketAddress bind(final int port) throws IOException;

    InetSocketAddress bind(final int port, final int backlog) throws IOException;

    //

    void setSessionHandler(final HttpSessionHandler sessionHandler);

    HttpSessionHandler getSessionHandler();

    HttpSession getSession(final HttpExchange exchange);

    //

    HttpHandler getContextHandler(final String context);

    HttpHandler getContextHandler(final HttpContext context);

    Map<HttpContext,HttpHandler> getContexts();

    String getRandomContext();

    String getRandomContext(final String context);

    //

    void stop();

}
