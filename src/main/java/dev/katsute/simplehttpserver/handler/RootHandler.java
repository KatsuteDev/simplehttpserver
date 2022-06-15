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

import com.sun.net.httpserver.HttpHandler;

/**
 * Handler that process requests for the root <code>/</code> context.
 *
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public class RootHandler extends PredicateHandler {

    /**
     * Creates a root handler.
     *
     * @param index handler to use for context <code>/</code>
     * @param other handler to use for all other contexts, typically a 404 page
     *
     * @since 5.0.0
     */
    public RootHandler(final HttpHandler index, final HttpHandler other){
        super(
            exchange -> exchange.getRequestURI().getPath().equals("/"),
            index,
            other
        );
    }

}
