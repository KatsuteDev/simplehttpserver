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

package dev.katsute.simplehttpserver.handler.throttler;

import dev.katsute.simplehttpserver.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A throttler that limits the amount of simultaneous connections based on the session.
 *
 * @see ConnectionThrottler
 * @see HttpSession
 * @see ServerSessionThrottler
 * @since 5.0.0
 * @version 5.0.0
 * @author Katsute
 */
public class SessionThrottler extends ConnectionThrottler {

    private final HttpSessionHandler sessionHandler;
    private final Map<HttpSession,AtomicInteger> connections = new ConcurrentHashMap<>();

    /**
     * Creates a throttler.
     *
     * @param sessionHandler session handler
     *
     * @see HttpSessionHandler
     * @since 5.0.0
     */
    public SessionThrottler(final HttpSessionHandler sessionHandler){
        this.sessionHandler = Objects.requireNonNull(sessionHandler);
    }

    @Override
    final boolean addConnection(final SimpleHttpExchange exchange){
        final HttpSession session = sessionHandler.getSession(exchange); // session
        final int maxConn = getMaxConnections(session, exchange); // max allowed for session

        connections.putIfAbsent(session, new AtomicInteger(0));

        final AtomicInteger conn = connections.get(session); // current connections

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
        final HttpSession session = sessionHandler.getSession(exchange); // session
        if(connections.containsKey(session))
            connections.get(session).decrementAndGet(); // decrease connections
    }

    @Override
    public final int getMaxConnections(final SimpleHttpExchange exchange){
        return getMaxConnections(sessionHandler.getSession(exchange), exchange);
    }

    /**
     * Returns the maximum number of connections allowed for a session. Return <code>-1</code> for unlimited connections.
     *
     * @param session session
     * @param exchange exchange
     * @return maximum connections
     *
     * @see HttpSession
     * @see SimpleHttpExchange
     * @since 5.0.0
     */
    public int getMaxConnections(final HttpSession session, final SimpleHttpExchange exchange){
        return -1;
    }

    //

    @Override
    public String toString(){
        return "SessionThrottler{" +
               "sessionHandler=" + sessionHandler +
               ", connections=" + connections +
               '}';
    }

}
