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
 * By default, exchanges will look for the closest matching context for their handler, this consequently means that the root index <code>/</code> would catch any requests without a handler instead of returning a code 404.
 * <br>
 * The RootHandler resolves this issue by only accepting requests to the exact context <code>/</code> and sending the rest to an alternative handler, typically where a 404 page would reside.
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

    //

    @Override
    public String toString(){
        return "RootHandler{}";
    }

}
