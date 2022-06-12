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
import java.util.function.Predicate;

public class PredicateHandler implements HttpHandler {

    private final HttpHandler T, F;
    private final Predicate<HttpExchange> predicate;

    public PredicateHandler(final Predicate<HttpExchange> predicate, final HttpHandler handlerIfTrue, HttpHandler handlerIfFalse){
        this.T = handlerIfTrue;
        this.F = handlerIfFalse;
        this.predicate = predicate;
    }

    @Override
    public final void handle(final HttpExchange exchange) throws IOException{
        (predicate.test(exchange) ? T : F).handle(exchange);
    }

}
