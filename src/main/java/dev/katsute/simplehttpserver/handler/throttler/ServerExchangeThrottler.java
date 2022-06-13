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

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerExchangeThrottler extends ConnectionThrottler {

    private final Map<InetAddress,AtomicInteger> connections = new ConcurrentHashMap<>();

    private final AtomicInteger connCount = new AtomicInteger(0);
    private final AtomicInteger maxConn = new AtomicInteger(-1);

    public ServerExchangeThrottler(){ }

    public ServerExchangeThrottler(final int maxConnections){
        maxConn.set(maxConnections);
    }

    @Override
    final boolean addConnection(final HttpExchange exchange){
        final InetAddress address = exchange.getRemoteAddress().getAddress(); // public address
        final int clientMaxConn = getMaxConnections(exchange); // max allowed for this address

        connections.putIfAbsent(address, new AtomicInteger(0));

        final AtomicInteger conn = connections.get(address); // current connections
        final boolean exempt = canIgnoreConnectionLimit(exchange); // if exempt from limit

        if(clientMaxConn < 0){ // unlimited client conn
            if(!exempt){
                final int maxServerConn = maxConn.get();
                synchronized(this){
                    if(maxServerConn < 0 || connCount.get() < maxServerConn){ // if space for client conn
                        conn.incrementAndGet(); // increase client conn
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
                    if(conn.get() < clientMaxConn && (maxServerConn < 0 || connCount.get() < maxServerConn)){ // if space for both conn
                        conn.incrementAndGet(); // increase client conn
                        connCount.incrementAndGet(); // increase server conn
                        return true;
                    }
                    return false; // no space
                }
            }else{
                final AtomicBoolean added = new AtomicBoolean(false);
                conn.updateAndGet(operand -> {
                   if(operand < clientMaxConn) added.set(true); // if space then allow addition
                   return operand < clientMaxConn ? operand + 1 : operand; // add if space, otherwise no change
                });
                return added.get(); // return if space
            }
        }
    }

    @Override
    final void deleteConnection(final HttpExchange exchange){
        final InetAddress address = exchange.getRemoteAddress().getAddress(); // public address
        if(connections.containsKey(address)){
            connections.get(address).decrementAndGet(); // decrease connection
            if(!canIgnoreConnectionLimit(exchange)) // exempt doesn't count towards server conn
                connCount.decrementAndGet(); // decrease server conn
        }
    }

    @Override
    public int getMaxConnections(final HttpExchange exchange){
        return -1;
    }

    //

    public boolean canIgnoreConnectionLimit(final HttpExchange exchange){
        return false;
    }

    public synchronized final void setMaxServerConnections(final int connections){
        maxConn.set(connections);
    }

    public synchronized final int getMaxServerConnections(){
        return maxConn.get();
    }

}
