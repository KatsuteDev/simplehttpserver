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

import java.io.IOException;
import java.util.Objects;

/**
 * Redirects a request to a different URL.
 *
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public class RedirectHandler implements HttpHandler {

    private final String link;

    /**
     * Creates a redirect to a URL.
     *
     * @param link URL to redirect to
     *
     * @since 5.0.0
     */
    public RedirectHandler(final String link){
        this.link = Objects.requireNonNull(link);
    }

    @Override
    public final void handle(final HttpExchange exchange) throws IOException{
        exchange.getResponseHeaders().set("Location", link);
        exchange.sendResponseHeaders(308, 0); // permanent redirect
        exchange.close();
    }

    //

    @Override
    public String toString(){
        return "RedirectHandler{" +
               "link='" + link + '\'' +
               '}';
    }

}
