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

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A throttler that limits the amount of simultaneous connections based on the exchange.
 *
 * @see ConnectionThrottler
 * @see ServerExchangeThrottler
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public class ExchangeThrottler extends ConnectionThrottler {

    /**
     * Creates a throttler.
     *
     * @since 5.0.0
     */
    public ExchangeThrottler(){ }

    private final Map<InetAddress,AtomicInteger> connections = new ConcurrentHashMap<>();

    @Override
    final boolean addConnection(final SimpleHttpExchange exchange){
        final InetAddress address = exchange.getRemoteAddress().getAddress(); // public address
        final int maxConn = getMaxConnections(exchange); // max allowed for this address

        connections.putIfAbsent(address, new AtomicInteger(0));

        final AtomicInteger conn = connections.get(address); // current connections

        if(maxConn < 0){ // unlimited connections allowed
            conn.incrementAndGet();
            return true;
        }else{
            final AtomicBoolean added = new AtomicBoolean(false);
            conn.updateAndGet(operand -> {
                if(operand < maxConn) added.set(true); // if space then allow addition
                return operand < maxConn ? operand + 1 : operand; // add if space, otherwise no change
            });
            return added.get(); // return if space
        }
    }

    @Override
    final void deleteConnection(final SimpleHttpExchange exchange){
        final InetAddress address = exchange.getRemoteAddress().getAddress(); // public address
        if(connections.containsKey(address))
            connections.get(address).decrementAndGet(); // decrease connections
    }

    @Override
    public int getMaxConnections(final SimpleHttpExchange exchange){
        return 0;
    }

    //

    @Override
    public String toString(){
        return "ExchangeThrottler{" +
               "connections=" + connections +
               '}';
    }

}