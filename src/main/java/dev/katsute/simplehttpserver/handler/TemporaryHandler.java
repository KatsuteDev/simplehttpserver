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

import java.io.IOException;

public class TemporaryHandler implements HttpHandler {

    private final HttpHandler handler;

    private final Long expiry;

    public TemporaryHandler(final HttpHandler handler){
        this.handler = handler;
        this.expiry  = null;
    }

    public TemporaryHandler(final HttpHandler handler, final long maxTime){
        this.handler = handler;
        this.expiry  = System.currentTimeMillis() + maxTime;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException{
        if(expiry == null || System.currentTimeMillis() < expiry)
            handler.handle(exchange);
        exchange.getHttpContext().getServer().removeContext(exchange.getHttpContext());
        exchange.close();
    }

}
