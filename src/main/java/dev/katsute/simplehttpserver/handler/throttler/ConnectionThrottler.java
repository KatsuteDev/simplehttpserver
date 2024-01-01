/*
 * Copyright (C) 2024 Katsute <https://github.com/Katsute>
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

package dev.katsute.simplehttpserver.handler.throttler;

import dev.katsute.simplehttpserver.SimpleHttpExchange;

/**
 * Determines how connections are handled by the {@link ThrottledHandler}.
 *
 * @see ThrottledHandler
 * @see ExchangeThrottler
 * @see ServerExchangeThrottler
 * @see SessionThrottler
 * @see ServerSessionThrottler
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
abstract class ConnectionThrottler {

    ConnectionThrottler(){ }

    abstract boolean addConnection(final SimpleHttpExchange exchange);

    abstract void deleteConnection(final SimpleHttpExchange exchange);

    /**
     * Returns the maximum number of connections allowed for an exchange. Return <code>-1</code> for unlimited connections.
     *
     * @param exchange exchange
     * @return maximum connections
     *
     * @see SimpleHttpExchange
     * @since 5.0.0
     */
    public abstract int getMaxConnections(final SimpleHttpExchange exchange);

}