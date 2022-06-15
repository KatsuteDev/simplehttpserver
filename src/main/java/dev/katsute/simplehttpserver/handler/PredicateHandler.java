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
import dev.katsute.simplehttpserver.SimpleHttpExchange;
import dev.katsute.simplehttpserver.SimpleHttpHandler;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Handler that process requests based on the result of a predicate.
 *
 * @see Predicate
 * @see SimpleHttpExchange
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public class PredicateHandler implements SimpleHttpHandler {

    private final HttpHandler T, F;
    private final Predicate<SimpleHttpExchange> predicate;

    /**
     * Creates a predicate handler.
     *
     * @param predicate predicate
     * @param handlerIfTrue handler to use if true
     * @param handlerIfFalse handler to use if false
     *
     * @see Predicate
     * @see SimpleHttpExchange
     * @since 5.0.0
     */
    public PredicateHandler(final Predicate<SimpleHttpExchange> predicate, final HttpHandler handlerIfTrue, HttpHandler handlerIfFalse){
        this.T = Objects.requireNonNull(handlerIfTrue);
        this.F = Objects.requireNonNull(handlerIfFalse);
        this.predicate = Objects.requireNonNull(predicate);
    }

    @Override
    public final void handle(final HttpExchange exchange) throws IOException{
        SimpleHttpHandler.super.handle(exchange);
    }

    @Override
    public final void handle(final SimpleHttpExchange exchange) throws IOException{
        (predicate.test(exchange) ? T : F).handle(exchange);
    }

}
