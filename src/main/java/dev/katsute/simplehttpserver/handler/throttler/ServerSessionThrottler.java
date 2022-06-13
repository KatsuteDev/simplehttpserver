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

package dev.katsute.simplehttpserver.handler.throttler;

import com.sun.net.httpserver.HttpExchange;
import dev.katsute.simplehttpserver.HttpSession;
import dev.katsute.simplehttpserver.HttpSessionHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerSessionThrottler extends ConnectionThrottler {

    private final HttpSessionHandler sessionHandler;
    private final Map<HttpSession,AtomicInteger> connections = new ConcurrentHashMap<>();

    private final AtomicInteger connCount = new AtomicInteger(0);
    private final AtomicInteger maxConn = new AtomicInteger(-1);

    public ServerSessionThrottler(final HttpSessionHandler sessionHandler){
        this.sessionHandler = sessionHandler;
    }

    public ServerSessionThrottler(final HttpSessionHandler sessionHandler, final int maxConnections){
        this.sessionHandler = sessionHandler;
        maxConn.set(maxConnections);
    }

    @Override
    final boolean addConnection(final HttpExchange exchange){
        final HttpSession session = sessionHandler.getSession(exchange); // session
        final int sessionMaxConn = getMaxConnections(session, exchange); // max allowed for this session

        connections.putIfAbsent(session, new AtomicInteger(0));

        final AtomicInteger conn = connections.get(session); // current connections
        final boolean exempt = canIgnoreConnectionLimit(session, exchange);

        if(sessionMaxConn < 0){
            if(!exempt){
                final int maxServerConn = maxConn.get();
                synchronized(this){
                    if(maxServerConn < 0 || connCount.get() < maxServerConn){ // if space for session conn
                        conn.incrementAndGet(); // increase session conn
                        connCount.incrementAndGet(); // increase server conn
                        return true;
                    }
                    return false; // no space
                }
            }else{
                conn.incrementAndGet(); // increase server conn
                return true; // always space for exempt
            }
        }else{
            if(!exempt){
                final int maxServerConn = maxConn.get();
                synchronized(this){
                    if(conn.get() < sessionMaxConn && (maxServerConn < 0 || connCount.get() < maxServerConn)){ // if space for both conn
                        conn.incrementAndGet(); // increase client conn
                        connCount.incrementAndGet(); // increase server conn
                        return true;
                    }
                    return false; // no space
                }
            }else{
                final AtomicBoolean added = new AtomicBoolean(false);
                conn.updateAndGet(operand -> {
                   if(operand < sessionMaxConn) added.set(true); // if space then allow addition
                   return operand < sessionMaxConn ? operand + 1 : operand; // add if space, otherwise no change
                });
                return added.get(); // return if space
            }
        }
    }

    @Override
    final void deleteConnection(final HttpExchange exchange){
        final HttpSession session = sessionHandler.getSession(exchange); // session
        if(connections.containsKey(session)){
            connections.get(session).decrementAndGet(); // decrease connection
            if(!canIgnoreConnectionLimit(session, exchange)) // exempt doesn't count towards server conn
                connCount.decrementAndGet(); // decrease server conn
        }
    }

    @Override
    public final int getMaxConnections(final HttpExchange exchange){
        return getMaxConnections(sessionHandler.getSession(exchange), exchange);
    }

    public int getMaxConnections(final HttpSession session, final HttpExchange exchange){
        return -1;
    }

    @SuppressWarnings("SameReturnValue")
    public boolean canIgnoreConnectionLimit(final HttpSession session, final HttpExchange exchange){
        return false;
    }

    public synchronized final void setMaxServerConnections(final int connections){
        maxConn.set(connections);
    }

    public synchronized final int getMaxServerConnections(){
        return maxConn.get();
    }


}
